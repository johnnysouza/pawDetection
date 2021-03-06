package br.com.furb.pawDetection.view;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import br.com.furb.pawDetection.beans.PawDimension;
import br.com.furb.pawDetection.core.PawDetection;

public class PawDetectionView extends JFrame {

    private JPanel contentPane;
    private File imageFile;
    private JButton btnProcess;
    private ImagePanel srcImage;
    private ImagePanel mouseImage;
    private ImagePanel pawImageI;
    private ImagePanel pawImageII;
    private JCheckBox chkDebugMode;
    private JTextArea textResult;

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
    	setIconImage(Toolkit.getDefaultToolkit().getImage(PawDetectionView.class.getResource("/br/com/furb/pawDetection/image/furb_logo.jpg")));
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(100, 100, 450, 420);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);
	contentPane.setLayout(null);
	
	srcImage = new ImagePanel();
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
		loadFile();
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
	
	btnProcess = new JButton("Processar");
	btnProcess.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
		    process();
		}
	});
	btnProcess.setBounds(10, 191, 150, 23);
	btnProcess.setEnabled(false);
	contentPane.add(btnProcess);
	
	chkDebugMode = new JCheckBox("Modo debug");
	chkDebugMode.setBounds(6, 221, 97, 23);
	contentPane.add(chkDebugMode);
	
	JLabel lblResult = new JLabel("Resultados");
	lblResult.setBounds(10, 251, 93, 14);
	contentPane.add(lblResult);
	
	textResult = new JTextArea();
	textResult.setBounds(10, 276, 150, 95);
	textResult.setEditable(false);
	contentPane.add(textResult);	
	mouseImage = new ImagePanel();
	mouseImage.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
	mouseImage.setBounds(233, 55, 150, 150);
	contentPane.add(mouseImage);
	
	JLabel lblSeparaoDoRato = new JLabel("Separação do rato");
	lblSeparaoDoRato.setBounds(233, 30, 150, 14);
	contentPane.add(lblSeparaoDoRato);
	
	pawImageI = new ImagePanel();
	pawImageI.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
	pawImageI.setBounds(198, 251, 100, 125);
	contentPane.add(pawImageI);
	
	pawImageII = new ImagePanel();
	pawImageII.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
	pawImageII.setBounds(308, 251, 100, 125);
	contentPane.add(pawImageII);
	
	JLabel lblPata = new JLabel("Pata 1");
	lblPata.setBounds(198, 225, 46, 14);
	contentPane.add(lblPata);
	
	JLabel lblPata_1 = new JLabel("Pata 2");
	lblPata_1.setBounds(308, 225, 46, 14);
	contentPane.add(lblPata_1);

    }
    
    private void loadFile() {
	JFileChooser loadImage = new JFileChooser();
	loadImage.setFileFilter(new FileNameExtensionFilter("Images", new String[] {"jpg", "jpeg", "png"}));
	int loadImageRet = loadImage.showOpenDialog(contentPane);
	
	if (loadImageRet == JFileChooser.APPROVE_OPTION) {
	    btnProcess.setEnabled(true);
	    
	    imageFile  = loadImage.getSelectedFile();
	    BufferedImage image = new BufferedImage(150, 150, BufferedImage.TYPE_3BYTE_BGR);
	    try {
		image = ImageIO.read(imageFile);
		srcImage.setImage(image);
		srcImage.repaint();
	    } catch (Exception ex) {
		JOptionPane.showMessageDialog(contentPane, "Falha na carga do arquivo");
	    }
	}
    }

    private void process() {
	PawDetection pawDetection = new PawDetection(imageFile.getAbsolutePath(), chkDebugMode.isSelected());
	IplImage greenChannel = pawDetection.separateGreenChannel();
	IplImage mouseThresholding = pawDetection.getMouseThresholding(greenChannel);
	
	OpenCVFrameConverter.ToIplImage sourceConverter = new OpenCVFrameConverter.ToIplImage();
	Java2DFrameConverter frameConverter = new Java2DFrameConverter();

        BufferedImage mouseThresholdingImg = frameConverter.getBufferedImage(sourceConverter.convert(mouseThresholding));
        mouseImage.setImage(mouseThresholdingImg);
        mouseImage.repaint();
	
	IplImage dilate = pawDetection.getFilterPaws(mouseThresholding);
	PawDimension[] pawDimensions = pawDetection.findPaws(dilate);
	
	int ratio = PawDetection.difineRatio(mouseThresholding, greenChannel);
	
	StringBuilder result = new StringBuilder();
	for (int i = 0; i < pawDimensions.length; i++) {
	    PawDimension pawDimension = pawDimensions[i];
	    
	    BufferedImage pawImg = frameConverter.getBufferedImage(sourceConverter.convert(pawDimension.getPaw()));
	    if (i == 1) {
		pawImageI.setImage(pawImg);
		pawImageI.repaint();
	    } else {
		pawImageII.setImage(pawImg);
		pawImageII.repaint();
	    }
	    
	    pawDimension.apllyRatio(ratio);
	    result.append("Pata ");
	    result.append(i + 1);
	    result.append(": \r\n");
	    result.append("Altura: ");
	    result.append(String.format("%1$,.2f", pawDimension.getPawHeight()));
	    result.append(" / Largura: ");
	    result.append(String.format("%1$,.2f", pawDimension.getPawWidth()));
	    result.append("\r\n\r\n");
	}
	
	textResult.setText("");
	textResult.setText(result.toString());
    }

}
