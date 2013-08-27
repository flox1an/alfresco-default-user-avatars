package de.fmaul.alfresco.avatars;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface AvatarService {
	
	public static final String DEFAULT_AVATAR_NAME_PREFIX = "default_avatar_";
	
	NodeRef createDefaultUserAvatar(NodeRef person);
	
	public boolean avatarUpdateNeeded(Map<QName, Serializable> before, Map<QName, Serializable> after);

}
