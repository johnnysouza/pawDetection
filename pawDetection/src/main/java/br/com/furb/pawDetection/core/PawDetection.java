package br.com.furb.pawDetection.core;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvAvg;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvNot;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_core.cvSplit;
import static org.bytedeco.javacpp.opencv_core.cvSub;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_NONE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_CCOMP;
import static org.bytedeco.javacpp.opencv_imgproc.MORPH_RECT;
import static org.bytedeco.javacpp.opencv_imgproc.THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.cvBoundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.cvCreateStructuringElementEx;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvDilate;
import static org.bytedeco.javacpp.opencv_imgproc.cvErode;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindContours;
import static org.bytedeco.javacpp.opencv_imgproc.cvThreshold;

import java.io.File;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplConvKernel;
import org.bytedeco.javacpp.opencv_core.IplImage;

public class PawDetection {
    
    public static final String PATH = System.getProperty("User.dir");
    private static final int POW_THRESHOLDING = 150;
    private static final double MAXVAL = 255;
    
    private IplImage srcImage;
    private String imagePath;
    
    public PawDetection(String imagePath) {
	super();
	File file =  new File(imagePath);
	if (file.exists()) {
	    srcImage = cvLoadImage(imagePath, CV_LOAD_IMAGE_COLOR);
	}
	this.imagePath = imagePath;
    }

    public static IplImage loadImage(String path) {
	File file =  new File(path);
	if (file.exists()) {
	    return cvLoadImage(path, CV_LOAD_IMAGE_COLOR);
	}
	return null;
    }
    
    public static IplImage separateGreenChannel(IplImage src) {
	IplImage channelR = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 1);
	IplImage channelG = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 1);
	IplImage channelB = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 1);
	cvSplit(src, channelB, channelR, channelG, null);
	
	cvSaveImage(PATH + "_channel_Red.jpg", channelR);
	cvSaveImage(PATH + "_channel_Green.jpg", channelG);
	cvSaveImage(PATH + "_channel_Blue.jpg", channelB);
	
	return src;
    }
    
    public static IplImage getMouseThresholding(IplImage greenChannel) {
	CvScalar avg = cvAvg(greenChannel);

	IplImage mouseThresholding = cvCreateImage(cvGetSize(greenChannel), 8, 1);
	double thresh = avg.val(0);
	cvThreshold(greenChannel, mouseThresholding, thresh, MAXVAL, THRESH_BINARY);
	cvSaveImage(PATH + "_mouse.jpg", mouseThresholding);
	
	return mouseThresholding;
    }
    
    public static IplImage getFilterPaws(IplImage mouseThresholding, IplImage src) {
	IplImage contourTemp = cvCreateImage(cvGetSize(mouseThresholding), 8, 1);
	cvCopy(mouseThresholding, contourTemp);

	CvMemStorage storage = CvMemStorage.create();
	CvSeq contours = new CvContour(null);
	cvFindContours(contourTemp, storage, contours,
			Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
			CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));

	CvSeq bigContour = Operations.findBigContour(contours);
	CvRect mouseBounding = cvBoundingRect(bigContour);

	IplImage hsv = cvCreateImage(cvGetSize(src), 8, 3);
	cvCvtColor(src, hsv, COLOR_BGR2HSV);

	IplImage h = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 1);
	IplImage s = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 1);
	IplImage v = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 1);
	cvSplit(hsv, h, s, v, null);

	cvSaveImage(PATH + "_hue.jpg", h);
	
	IplImage pawThresholding = cvCreateImage(cvGetSize(src), 8, 1);
	cvThreshold(h, pawThresholding, POW_THRESHOLDING, MAXVAL, THRESH_BINARY);
	cvSaveImage(PATH + "_pawThresholding.jpg", pawThresholding);

	// Dilata e erode a imagem para juntar pedaços da pata que ficaram um
	// pouco separados
	IplConvKernel element = cvCreateStructuringElementEx(17, 17, 0, 0,
			MORPH_RECT);
	IplImage dilate = cvCreateImage(cvGetSize(pawThresholding), 8, 1);
	cvDilate(pawThresholding, dilate, element, 1);
	cvSaveImage(PATH + "_dilate.jpg", dilate);

	IplImage erode = cvCreateImage(cvGetSize(pawThresholding), 8, 1);
	cvErode(dilate, erode, element, 1);
	cvSaveImage(PATH + "_erode.jpg", erode);

	cvSetImageROI(erode, mouseBounding);
	cvSetImageROI(mouseThresholding, mouseBounding);
	IplImage sub = cvCreateImage(cvGetSize(erode), 8, 1);
	cvNot(erode, erode);
	cvSub(mouseThresholding, erode, sub);
	cvSaveImage(PATH + "_pawOfMouse.jpg", sub);

	// Erode e dilata a imagem para remover ruidos restantes
	erode = cvCreateImage(cvGetSize(sub), 8, 1);
	cvErode(sub, erode, element, 1);

	dilate = cvCreateImage(cvGetSize(sub), 8, 1);
	cvDilate(erode, dilate, element, 1);
	cvSaveImage(PATH + "_pawOfMouseFiltered.jpg", dilate);
	
	return dilate;
    }

}
