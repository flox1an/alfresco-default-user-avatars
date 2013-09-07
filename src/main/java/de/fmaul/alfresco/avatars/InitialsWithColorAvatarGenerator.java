package de.fmaul.alfresco.avatars;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InitialsWithColorAvatarGenerator implements AvatarGenerator {

	Log log = LogFactory.getLog(InitialsWithColorAvatarGenerator.class);

	String colorPalette = "#6E7702,#CCC429,#BBA40E,#F9EA6D,#F8C847,#32444E,#83A2B1,#A6CADB,#C6E8FD,#99BBCD";

	private boolean showInitials = true;

	private boolean blackFontForLightBackgrounds = false;
	
	private NodeService nodeService;
	
	/* (non-Javadoc)
	 * @see de.fmaul.alfresco.avatars.AvatarGenerator#avatarCanBeGenerated(org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public boolean avatarCanBeGenerated(NodeRef person) {
		String letters = getAvatarInitialsFromProperties(person);
		return (letters != null);
	}

	/* (non-Javadoc)
	 * @see de.fmaul.alfresco.avatars.AvatarGenerator#createAvatar(org.alfresco.service.cmr.repository.NodeRef, java.io.OutputStream)
	 */
	@Override
	public boolean createAvatar(NodeRef person, OutputStream avatarPngOutputStream) {
		String letters = getAvatarInitialsFromProperties(person);

		if (letters != null) {
			createAvatar(letters, avatarPngOutputStream);
			return true;
		}
		return false;
	}

	private String getAvatarInitialsFromProperties(NodeRef person) {
		String personFirstName = (String) nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
		String personLastName = (String) nodeService.getProperty(person, ContentModel.PROP_LASTNAME);

		if (personFirstName != null && personFirstName.length() > 0) {
			String letters = personFirstName.substring(0, 1);
			if (showInitials && personLastName != null && personLastName.length() > 0) {
				letters += personLastName.substring(0, 1);
			}
			return letters;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.fmaul.alfresco.avatars.AvatarGenerator#avatarUpdateNeeded(java.util.Map, java.util.Map)
	 */
	@Override
	public boolean avatarUpdateNeeded(Map<QName, Serializable> before, Map<QName, Serializable> after) {
		String firstNameBefore = (String) before.get(ContentModel.PROP_FIRSTNAME);
		String firstNameAfter = (String) after.get(ContentModel.PROP_FIRSTNAME);
		String lastNameBefore = (String) before.get(ContentModel.PROP_LASTNAME);
		String lastNameAfter = (String) after.get(ContentModel.PROP_LASTNAME);

		if (showInitials) {
			return (firstNameBefore != null && firstNameAfter != null && !firstNameBefore.equals(firstNameAfter))
					|| (lastNameBefore != null && lastNameAfter != null && !lastNameBefore.equals(lastNameAfter));
		} else {
			// Only show first name letter
			return (firstNameBefore != null && firstNameAfter != null && !firstNameBefore.equals(firstNameAfter));
		}
	}

	/**
	 * Selects a random color from the configured color palette. Which is a
	 * String of comma speparated html color codes.
	 * 
	 * @return A HTML color code for the selected random Color.
	 */
	private String getRandomColorFromPalette() {
		String[] palette = colorPalette.split(",");
		int paletteIndex = (int) (Math.random() * palette.length);
		return palette[paletteIndex];
	}

	/**
	 * Renders an avatar image and writes it as PNG into the outputStream.
	 * @param letters
	 * @param outputStream
	 */
	private void createAvatar(String letters, OutputStream outputStream) {
		Color backgroundColor = Color.decode(getRandomColorFromPalette());
		Color textColor = Color.white;

		if (blackFontForLightBackgrounds && brightness(backgroundColor) > 180) {
			textColor = Color.black;
		}
		BufferedImage bi = drawAvatar(letters, backgroundColor, textColor);

		try {
			ImageIO.write(bi, "PNG", outputStream);
			outputStream.close();
		} catch (IOException e) {
			log.error("Error writing avatar image to output stream.", e);
		}

	}

	/**
	 * Draws an Avatar image using Graphics2D
	 * @param letters
	 * @param backgroundColor
	 * @param textColor
	 * @return
	 */
	private BufferedImage drawAvatar(String letters, Color backgroundColor, Color textColor) {
		int width = 64, height = 64;

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = bi.createGraphics();

		g.setPaint(backgroundColor);
		g.fillRect(0, 0, width, height);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int fontSize = 50;
		if (showInitials) {
			fontSize = 30;
		}

		Font font = new Font("Arial", Font.BOLD, fontSize);
		g.setFont(font);
		g.setPaint(textColor);
		FontMetrics fontMetrics = g.getFontMetrics();
		int stringWidth = fontMetrics.stringWidth(letters);
		int stringHeight = fontMetrics.getAscent() + 20;
		g.drawString(letters, (width - stringWidth) / 2, height / 2 + stringHeight / 4);

		return bi;
	}

	/**
	 * Calculates the brightness (average from all three color channels)
	 * @param color
	 * @return A double with 0 for black, 255 for white
	 */
	private double brightness(Color color) {

		return ((double) color.getBlue() + color.getRed() + color.getGreen()) / 3;
	}

	public void setShowInitials(boolean showInitials) {
		this.showInitials = showInitials;
	}

	public void setBlackFontForLightBackgrounds(boolean blackFontForLightBackgrounds) {
		this.blackFontForLightBackgrounds = blackFontForLightBackgrounds;
	}
	
	public void setColorPalette(String colorPalette) {
		this.colorPalette = colorPalette;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
}
