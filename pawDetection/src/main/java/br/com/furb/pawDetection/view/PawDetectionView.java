package br.com.furb.pawDetection.view;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PawDetectionView extends JFrame {

    private JPanel contentPane;
    private File imageFile;
    private ImagemPanel srcImage;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    PawDetectionView frame = new PawDetectionView();
		    frame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Create the frame.
     */
    public PawDetectionView() {
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(100, 100, 450, 300);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);
	contentPane.setLayout(null);
	
	srcImage = new ImagemPanel();
	srcImage.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
	srcImage.setBounds(10, 30, 150, 150);
	contentPane.add(srcImage);
	srcImage.setLayout(null);
	
	JMenuBar mainMenu = new JMenuBar();
	mainMenu.setBounds(0, 0, 434, 21);
	contentPane.add(mainMenu);
	
	JMenu menuActions = new JMenu("Ações");
	mainMenu.add(menuActions);
	
	JMenuItem menuLoadImage = new JMenuItem("Carregar Imagem");
	menuLoadImage.addActionListener(new ActionListener() {
	    
	    public void actionPerformed(ActionEvent e) {
		JFileChooser loadImage = new JFileChooser();
		loadImage.setFileFilter(new FileNameExtensionFilter("Images", new String[] {"jpg", "jpeg", "png"}));
		int loadImageRet = loadImage.showOpenDialog(contentPane);
		
		if (loadImageRet == JFileChooser.APPROVE_OPTION) {
		    imageFile  = loadImage.getSelectedFile();
		    Graphics g = srcImage.getGraphics();
		    BufferedImage image = new BufferedImage(150, 150, BufferedImage.TYPE_3BYTE_BGR);
		    try {
			image = ImageIO.read(imageFile);
			g.drawImage(image, 0, 0, image.getWidth() / 2,
				image.getHeight() / 2, null);
			srcImage.setImage(image);
			srcImage.repaint();
		    } catch (Exception ex) {
			JOptionPane.showMessageDialog(contentPane, "Falha na carga do arquivo");
		    }
		}
	    }
	});
	menuActions.add(menuLoadImage);
	
	JMenuItem menuExit = new JMenuItem("Sair");
	menuExit.addActionListener(new ActionListener() {
	    
	    public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	});
	menuActions.add(menuExit);
	
	JButton btnProcess = new JButton("Processar");
	btnProcess.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
		}
	});
	btnProcess.setBounds(10, 191, 150, 23);
	contentPane.add(btnProcess);
    }
    
    static class ImagemPanel extends JPanel {

	private BufferedImage image;
	
	public ImagemPanel() {
	    image = null;
	}

	public ImagemPanel(BufferedImage image) {
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
}
