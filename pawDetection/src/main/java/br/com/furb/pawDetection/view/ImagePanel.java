package br.com.furb.pawDetection.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    
    private BufferedImage image;
	
	public ImagePanel() {
	    image = null;
	}

	public ImagePanel(BufferedImage image) {
	    this.image = image;
	}
	
	public void setImage(BufferedImage image) {
	    this.image = image;
	}

	@Override
	public void paint(Graphics g) {
	    super.paint(g);
	    if (image != null) {
		BufferedImage resized = new BufferedImage(getWidth(), getHeight(), image.getType());
		Graphics2D graps = resized.createGraphics();
		graps.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graps.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(),
			image.getHeight(), null);
		graps.dispose();
		
	    	g.drawImage(resized, 0, 0, getWidth(), getHeight(), null);
	    }
	}

}
