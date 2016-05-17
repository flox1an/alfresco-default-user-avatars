package de.fmaul.alfresco.avatars;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface AvatarGenerator {

	/**
	 * Tests of a avatar Image can be generated with this Generator for the person Node. The generator
	 * can test here if certain Person properties are present that are needed for the image 
	 * generation.
	 * 
	 * The createAvatar method is not called if this method returns false.
	 * 
	 * @param person A person object whose avatar should be created.
	 * @return true if the avatar can be generated, false if not
	 */
	public abstract boolean avatarCanBeGenerated(NodeRef person);

	/**
	 * Generates a custom avatar for a person. A person {@link NodeRef} and an {@link OutputStream} are 
	 * propvided. This method should create a PNG image of 64x64 pixels and write it to the outputStream.
	 * 
	 * If an avatar image was written to the OutputStream true should be returned, false if no image was written.
	 * 
	 * @param person A person object whose avatar should be created.
	 * @param avatarPngOutputStream An outputstream where the 64x64px PNG should be written
	 * @return true if the image was created, false if no image was written.
	 */
	public abstract boolean createAvatar(NodeRef person, OutputStream avatarPngOutputStream);

	/**
	 * Tests if an avatar image update is needed when the properties of the
	 * person have changed. If the certain properties of the Person object are used
	 * to generate the avatar this method should test for changes and return true.
	 * 
	 * If false is returned, then the avatar image is not updated when the Person's
	 * properties change.
	 * 
	 * @param before The properties of the person before the update
	 * @param after The properties of the person after the update
	 */
	public abstract boolean avatarUpdateNeeded(Map<QName, Serializable> before,
			Map<QName, Serializable> after);

}