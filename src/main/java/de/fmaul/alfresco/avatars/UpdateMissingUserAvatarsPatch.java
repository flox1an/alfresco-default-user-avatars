package de.fmaul.alfresco.avatars;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

public class UpdateMissingUserAvatarsPatch extends AbstractPatch {

	private static final Log logger = LogFactory.getLog(UpdateMissingUserAvatarsPatch.class);
	private static final String MSG_SUCCESS = "User avatars have been updated.";
	private AvatarService avatarService;

	public UpdateMissingUserAvatarsPatch() {
	}

	@Override
	protected String applyInternal() throws Exception {
		// Get the ChildAssociationRefs
		List<ChildAssociationRef> toProcess = nodeService.getChildAssocs(getUserFolderLocation(), ContentModel.ASSOC_CHILDREN,
				RegexQNamePattern.MATCH_ALL, false);
		BatchProcessor<ChildAssociationRef> batchProcessor = new BatchProcessor<ChildAssociationRef>("UpdateMissingUserAvatarsPatch",
				transactionHelper, toProcess, 2, 20, this.applicationEventPublisher, logger, 1000);

		final String runAsUser = AuthenticationUtil.getRunAsUser();

		int updated = batchProcessor.process(new BatchProcessWorker<ChildAssociationRef>() {
			public void beforeProcess() throws Throwable {
				AuthenticationUtil.setRunAsUserSystem();
			}

			public void afterProcess() throws Throwable {
				AuthenticationUtil.clearCurrentSecurityContext();
			}

			public String getIdentifier(ChildAssociationRef entry) {
				return entry.getChildRef().toString();
			}

			public void process(ChildAssociationRef entry) throws Throwable {
				if (nodeService.exists(entry.getChildRef()) && nodeService.getType(entry.getChildRef()).equals(ContentModel.TYPE_PERSON)) {
					List<AssociationRef> avatarAssocs = nodeService.getTargetAssocs(entry.getChildRef(), ContentModel.ASSOC_AVATAR);

					String userName = (String) nodeService.getProperty(entry.getChildRef(), ContentModel.PROP_USERNAME);

					if (avatarAssocs == null || avatarAssocs.isEmpty()) {
						logger.info("Creating new default avatar image for user:" + userName);
						avatarService.createDefaultUserAvatar(entry.getChildRef());
					}
				}
			}
		}, true);
		return I18NUtil.getMessage(MSG_SUCCESS, updated);
	}

	private NodeRef getUserFolderLocation() {
		NodeRef rootNode = this.nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		QName qnameAssocSystem = QName.createQName("sys", "system", namespaceService);
		QName qnameAssocUsers = QName.createQName("sys", "people", namespaceService);
		List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNode, RegexQNamePattern.MATCH_ALL, qnameAssocSystem);
		NodeRef sysNodeRef = null;
		if (results.size() == 0) {
			throw new AlfrescoRuntimeException("Required authority system folder path not found: " + qnameAssocSystem);
		} else {
			sysNodeRef = results.get(0).getChildRef();
		}
		results = nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocUsers);
		if (results.size() == 0) {
			throw new AlfrescoRuntimeException("Required user folder path not found: " + qnameAssocUsers);
		} else {
			return results.get(0).getChildRef();
		}
	}

	public void setAvatarService(AvatarService avatarService) {
		this.avatarService = avatarService;
	}
}
