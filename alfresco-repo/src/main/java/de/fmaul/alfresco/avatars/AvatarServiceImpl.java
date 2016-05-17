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
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.security.authentication.AuthenticationUtil;

public class AvatarServiceImpl implements AvatarService {

	Log log = LogFactory.getLog(AvatarServiceImpl.class);

	private NodeService nodeService;
	private ContentService contentService;
    private TransactionService transactionService;
	private AvatarGenerator avatarGenerator;
	
	@Override
	public NodeRef createDefaultUserAvatar(final NodeRef person) {

		if (avatarGenerator.avatarCanBeGenerated(person)) {

            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setForceWritable(true);
            boolean requiresNew = false;
            if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE)
            {
                //We can be in a read-only transaction, so force a new transaction
                requiresNew = true;
            }

            return txnHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
            {

                @Override
                public NodeRef execute() throws Throwable
                {
                    return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
                    {
                        public NodeRef doWork() throws Exception
                        {
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
                    }, AuthenticationUtil.getSystemUserName());
                }

            }, false, requiresNew);


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

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

}
