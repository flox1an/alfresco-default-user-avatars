package de.fmaul.alfresco.avatars;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AvatarServiceImpl implements AvatarService {

	Log log = LogFactory.getLog(AvatarServiceImpl.class);

	private NodeService nodeService;
	private ContentService contentService;
	private AvatarGenerator avatarGenerator;
	
	@Override
	public NodeRef createDefaultUserAvatar(NodeRef person) {

		if (avatarGenerator.avatarCanBeGenerated(person)) {
			
			String userName = (String) nodeService.getProperty(person, ContentModel.PROP_USERNAME);
			
			String name = DEFAULT_AVATAR_NAME_PREFIX + userName + ".png";
			QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name));

			ChildAssociationRef avatarNodeAssoc = nodeService.createNode(person, ContentModel.ASSOC_PREFERENCE_IMAGE, assocQName,
					ContentModel.TYPE_CONTENT);
		
			NodeRef avatarNode = avatarNodeAssoc.getChildRef();
			nodeService.setProperty(avatarNode, ContentModel.PROP_NAME, name);
		
			ContentWriter writer = contentService.getWriter(avatarNode, ContentModel.PROP_CONTENT, true);
			writer.guessMimetype(name);
			
			avatarGenerator.createAvatar(person, writer.getContentOutputStream());
		
			nodeService.createAssociation(person, avatarNode, ContentModel.ASSOC_AVATAR);
			
			return avatarNode;
		}
		return null;
	}

	@Override
	public boolean avatarUpdateNeeded(Map<QName, Serializable> before, Map<QName, Serializable> after) {
		return avatarGenerator.avatarUpdateNeeded(before, after);
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	public void setAvatarGenerator(AvatarGenerator avatarGenerator) {
		this.avatarGenerator = avatarGenerator;
	}

}
