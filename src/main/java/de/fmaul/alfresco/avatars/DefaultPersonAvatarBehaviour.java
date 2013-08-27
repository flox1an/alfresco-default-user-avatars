package de.fmaul.alfresco.avatars;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

public class DefaultPersonAvatarBehaviour implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

	private NodeService nodeService;
	private PolicyComponent policyComponent;
	private AvatarService avatarService;

	/**
	 * Initialises an instance of behaviour class.
	 */
	public void init() {

		Behaviour onCreateNode = new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT);
		Behaviour onUpdateProperties = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT);

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON, onCreateNode);
		policyComponent
				.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_PERSON, onUpdateProperties);
	}

	@Override
	public void onUpdateProperties(NodeRef person, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (avatarService.avatarUpdateNeeded(before, after)) {

			if (nodeService.exists(person) && person.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {

				List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(person, ContentModel.ASSOC_AVATAR);

				if (targetAssocs.size() == 1) {
					AssociationRef as = targetAssocs.get(0);

					String avatarName = (String) nodeService.getProperty(as.getTargetRef(), ContentModel.PROP_NAME);
					
					// Only update avatar of it is a generated default avatar
					if (avatarName.startsWith(AvatarServiceImpl.DEFAULT_AVATAR_NAME_PREFIX)) {
	
						// remove old avatar
						nodeService.removeAssociation(as.getSourceRef(), as.getTargetRef(), as.getTypeQName());
						nodeService.removeChild(as.getSourceRef(), as.getTargetRef());
						
						avatarService.createDefaultUserAvatar(person);
					}

				}
			}
		}
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		NodeRef person = childAssocRef.getChildRef();

		if (nodeService.exists(person) && person.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {

			List<AssociationRef> avatarAssocs = nodeService.getTargetAssocs(person, ContentModel.ASSOC_AVATAR);

			if (avatarAssocs.size() == 0) {
				avatarService.createDefaultUserAvatar(person);
			}
		}
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setAvatarService(AvatarService avatarService) {
		this.avatarService = avatarService;
	}
}
