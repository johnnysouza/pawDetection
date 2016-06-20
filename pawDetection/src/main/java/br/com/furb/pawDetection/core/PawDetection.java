package br.com.furb.pawDetection.core;

import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvAvg;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvNot;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.cvResetImageROI;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_core.cvSplit;
import static org.bytedeco.javacpp.opencv_core.cvSub;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_NONE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FILLED;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_CCOMP;
import static org.bytedeco.javacpp.opencv_imgproc.MORPH_RECT;
import static org.bytedeco.javacpp.opencv_imgproc.THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.cvBoundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.cvCreateStructuringElementEx;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvDilate;
import static org.bytedeco.javacpp.opencv_imgproc.cvDrawContours;
import static org.bytedeco.javacpp.opencv_imgproc.cvErode;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindContours;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_imgproc.cvThreshold;

import java.io.File;
import java.util.List;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvReleaseFunc;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplConvKernel;
import org.bytedeco.javacpp.opencv_core.IplImage;

import br.com.furb.pawDetection.beans.BigDistance;
import br.com.furb.pawDetection.beans.Component;
import br.com.furb.pawDetection.beans.PawDimension;
import br.com.furb.pawDetection.types.Orientation;

public class PawDetection {
    
    public static final String PATH = System.getProperty("user.dir") + System.getProperty("file.separator");
    private static final int POW_THRESHOLDING = 150;
    private static final double MAXVAL = 255;
    
    private boolean isDebugMode;
    private IplImage srcImage;
    private CvRect mouseBounding;
    
    public PawDetection(String imagePath, boolean isDebugMode) {
	this.isDebugMode = isDebugMode;
	
	File file =  new File(imagePath);
	if (file.exists()) {
	    srcImage = cvLoadImage(imagePath, CV_LOAD_IMAGE_COLOR);
	}
    }

    public IplImage separateGreenChannel() {
	IplImage channelR = cvCreateImage(cvGetSize(srcImage), IPL_DEPTH_8U, 1);
	IplImage channelG = cvCreateImage(cvGetSize(srcImage), IPL_DEPTH_8U, 1);
	IplImage channelB = cvCreateImage(cvGetSize(srcImage), IPL_DEPTH_8U, 1);
	cvSplit(srcImage, channelB, channelR, channelG, null);
	
	if (isDebugMode) {
	    cvSaveImage(PATH + "_channel_Red.jpg", channelR);
	    cvSaveImage(PATH + "_channel_Green.jpg", channelG);
	    cvSaveImage(PATH + "_channel_Blue.jpg", channelB);
	}
	
	return channelG;
    }
    
    public IplImage getMouseThresholding(IplImage greenChannel) {
	CvScalar avg = cvAvg(greenChannel);

	IplImage mouseThresholding = cvCreateImage(cvGetSize(greenChannel), 8, 1);
	double thresh = avg.val(0);
	cvThreshold(greenChannel, mouseThresholding, thresh, MAXVAL, THRESH_BINARY);
	if (isDebugMode) {
	    cvSaveImage(PATH + "_mouse.jpg", mouseThresholding);
	}
	
	return mouseThresholding;
    }
    
    public IplImage getFilterPaws(IplImage mouseThresholding) {
	IplImage contourTemp = cvCreateImage(cvGetSize(mouseThresholding), 8, 1);
	cvCopy(mouseThresholding, contourTemp);

	CvMemStorage storage = CvMemStorage.create();
	CvSeq contours = new CvContour(null);
	cvFindContours(contourTemp, storage, contours,
			Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
			CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));

	CvSeq bigContour = Operations.findBigContour(contours);
	mouseBounding = cvBoundingRect(bigContour);

	IplImage hsv = cvCreateImage(cvGetSize(srcImage), 8, 3);
	cvCvtColor(srcImage, hsv, COLOR_BGR2HSV);

	IplImage h = cvCreateImage(cvGetSize(srcImage), IPL_DEPTH_8U, 1);
	IplImage s = cvCreateImage(cvGetSize(srcImage), IPL_DEPTH_8U, 1);
	IplImage v = cvCreateImage(cvGetSize(srcImage), IPL_DEPTH_8U, 1);
	cvSplit(hsv, h, s, v, null);

	if (isDebugMode) {
	    cvSaveImage(PATH + "_hue.jpg", h);
	}
	
	IplImage pawThresholding = cvCreateImage(cvGetSize(srcImage), 8, 1);
	cvThreshold(h, pawThresholding, POW_THRESHOLDING, MAXVAL, THRESH_BINARY);
	if (isDebugMode) {
	    cvSaveImage(PATH + "_pawThresholding.jpg", pawThresholding);
	}

	// Dilata e erode a imagem para juntar pedaços da pata que ficaram um
	// pouco separados
	IplConvKernel element = cvCreateStructuringElementEx(17, 17, 0, 0,
			MORPH_RECT);
	IplImage dilate = cvCreateImage(cvGetSize(pawThresholding), 8, 1);
	cvDilate(pawThresholding, dilate, element, 1);
	if (isDebugMode) {
	    cvSaveImage(PATH + "_dilate.jpg", dilate);
	}

	IplImage erode = cvCreateImage(cvGetSize(pawThresholding), 8, 1);
	cvErode(dilate, erode, element, 1);
	if (isDebugMode) {
	    cvSaveImage(PATH + "_erode.jpg", erode);
	}

	cvSetImageROI(erode, mouseBounding);
	cvSetImageROI(mouseThresholding, mouseBounding);
	IplImage sub = cvCreateImage(cvGetSize(erode), 8, 1);
	cvNot(erode, erode);
	cvSub(mouseThresholding, erode, sub);
	if (isDebugMode) {
	    cvSaveImage(PATH + "_pawOfMouse.jpg", sub);
	}
	cvResetImageROI(mouseThresholding);

	// Erode e dilata a imagem para remover ruidos restantes
	erode = cvCreateImage(cvGetSize(sub), 8, 1);
	cvErode(sub, erode, element, 1);

	dilate = cvCreateImage(cvGetSize(sub), 8, 1);
	cvDilate(erode, dilate, element, 1);
	if (isDebugMode) {
	    cvSaveImage(PATH + "_pawOfMouseFiltered.jpg", dilate);
	}
	
	return dilate;
    }
    
    public PawDimension[] findPaws(IplImage dilate) {
	PawDimension[] pawDimensions = new PawDimension[2];
	
	CvMemStorage storage = CvMemStorage.create();
	CvSeq contours = new CvContour(null);
	IplImage contourTemp = cvCreateImage(cvGetSize(dilate), 8, 1);
	cvCopy(dilate, contourTemp);
	cvFindContours(contourTemp, storage, contours,
			Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
			CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));

	List<Component> paws = Operations.findBigTwoContour(contours);
	
//	CvSeq ptr = new CvSeq();
//	cvSetImageROI(srcImage, mouseBounding);
//	for (ptr = paws.get(0).getContour(); ptr != null && ptr.address() != 0; ptr = ptr.h_prev()) {
//		cvDrawContours(srcImage, ptr, new CvScalar(0, 0, 255, 0), new CvScalar(0, 0, 255, 0), -1, CV_FILLED, 8, cvPoint(0, 0));
//	}
//	cvSaveImage(PATH + "_withPaws.jpg", srcImage);

	for (int i = 0, size = paws.size(); i < size; i++) {
		Component paw = paws.get(i);
		StringBuilder sb = new StringBuilder();
		sb.append("Pata: ");
		sb.append(i);
		sb.append(", size: ");
		sb.append(paw.getSize());
		sb.append(", total: ");
		sb.append(paw.getContour().total());
		// System.err.println(sb.toString());

		IplImage pata = cvCreateImage(cvGetSize(dilate), 8, 1);
		cvCopy(dilate, pata);
		System.out.println("teste");
		CvRect boundingBox = paw.getBoundingBox();
		System.out.println("teste");
		int xBBox = boundingBox.x();
		int yBBox = boundingBox.y();
		CvPoint offesetPoint = new CvPoint(xBBox, yBBox);
		CvPoint endPoint = new CvPoint(xBBox + boundingBox.width(), yBBox + boundingBox.height());
		cvRectangle( pata, offesetPoint, endPoint, CvScalar.BLACK, 2, CV_AA, 0);
		cvSetImageROI(pata, boundingBox);
		if (isDebugMode) {
		    cvSaveImage(PATH + "pata" + (i + 1) + ".jpg", pata);
		}
		
		CvSeq contour = paw.getContour();
		BigDistance pawLength = Operations.findBigDistanceOnContour(contour);
		paw.setLength(pawLength);
		
		double x = pawLength.getPointA().x() - pawLength.getPointB().x();
		double y = pawLength.getPointA().y() - pawLength.getPointB().y();
		double theta = Math.atan2(y, x);
		Orientation orientation = Math.abs(y) > Math.abs(x) ? Orientation.VERTICAL : Orientation.HORIZONTAL;
		double angle = Operations.radianToDegressRotate(theta, orientation);
		System.out.println(theta);
		System.out.println(angle);

		IplImage rotatedPaw = Operations.rotateImage(pata, angle, orientation);
		if (isDebugMode) {
		    cvSaveImage(PATH + "pata" + (i + 1) + "_rotated.jpg", rotatedPaw);
		}

		storage = CvMemStorage.create();
		contours = new CvSeq(null);
		contourTemp = cvCreateImage(cvGetSize(rotatedPaw), 8, 1);
		cvCopy(rotatedPaw, contourTemp);
		cvFindContours(contourTemp, storage, contours,
				Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
				CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));
		
		CvSeq alignedPaw = Operations.findBigContour(contours);
		int pawWidth = cvBoundingRect(alignedPaw).width();
		int pawHeight = pawLength.getDistance();
		
		pawDimensions[i] = new PawDimension(pawWidth, pawHeight, rotatedPaw);
		
		System.err.println("largura: " + pawWidth);
	}
	
	return pawDimensions;
    }
    
    public static int difeneRatio(IplImage mouseThresholding, IplImage channelG) {
	IplImage filtered = cvCreateImage(cvGetSize(mouseThresholding), 8, 1);
	cvNot(mouseThresholding, mouseThresholding);
	cvSub(channelG, mouseThresholding, filtered);
	cvSaveImage(PATH + "_mouseFilter.jpg", filtered);

	IplImage filteredTemp = cvCreateImage(cvGetSize(filtered),
			filtered.depth(), 1);
	cvCopy(filtered, filteredTemp);
	CvMemStorage storage = CvMemStorage.create();
	CvSeq contours = new CvContour(null);
	cvFindContours(filteredTemp, storage, contours,
			Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
			CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));

	CvSeq bigContour = Operations.findBigContour(contours);
	CvRect mouseBounding = cvBoundingRect(bigContour);
	int pixelsForSquare = 0;

	for (int thresh = 129; pixelsForSquare == 0 && thresh < 160; thresh += 5) {
		System.out.println("thresh: " + thresh);
		IplImage filteredThresh = cvCreateImage(cvGetSize(filtered), 8, 1);
		cvThreshold(filtered, filteredThresh, thresh, MAXVAL,
				THRESH_BINARY);
		cvSaveImage(PATH + "_mouseFilterThresholding.jpg", filteredThresh);

		IplConvKernel element = cvCreateStructuringElementEx(7, 7, 0, 0,
				MORPH_RECT);
		IplImage erode = cvCreateImage(cvGetSize(filteredThresh), 8, 1);
		cvErode(filteredThresh, erode, element, 1);
		cvSaveImage(PATH + "_erode.jpg", erode);
		cvSetImageROI(erode, mouseBounding);

		IplImage squares = cvCreateImage(cvGetSize(erode), erode.depth(), 1);
		cvCopy(erode, squares);
		storage = CvMemStorage.create();
		contours = new CvContour(null);
		cvFindContours(squares, storage, contours, Loader.sizeof(CvContour.class), CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));

		List<Component> possibleSquares = Operations.findBigFourContour(contours);
		
		for (int i = 0, amount = possibleSquares.size(); i < amount; i++) {
			Component component = possibleSquares.get(i);
			CvSeq contour = component.getContour();
			BigDistance bigDistance = Operations.findBigDistanceOnContour(contour);

			CvPoint pointA = bigDistance.getPointA();
			CvPoint pointB = bigDistance.getPointB();
			double x = pointA.x() - pointB.x();
			double y = pointA.y() - pointB.y();
			double theta = Math.atan2(y, x);
			double angle = Operations.radianToDegressRotateSquare(theta);

			CvRect boundingBox = cvBoundingRect(contour);
			cvRectangle( squares, new CvPoint(boundingBox.x(), boundingBox.y()), /* */
				new CvPoint(boundingBox.x() + boundingBox.width(), boundingBox .y() + boundingBox.height()), /* */
				CV_RGB(255, 0, 0), 2, CV_AA, 0);
			cvSetImageROI(squares, boundingBox);

			IplImage rotatedPossibleSquare = Operations.rotateImage(squares, angle, Orientation.VERTICAL);
			cvSaveImage(PATH + "_square_rotated_" + i + ".jpg", rotatedPossibleSquare);
			cvResetImageROI(squares);

			storage = CvMemStorage.create();
			contours = new CvContour(null);
			IplImage contourTemp = cvCreateImage(cvGetSize(rotatedPossibleSquare), 8, 1);
			cvCopy(rotatedPossibleSquare, contourTemp);
			cvFindContours(contourTemp, storage, contours,
					Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
					CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));

			CvSeq square = Operations.findBigContour(contours);
			if (square != null) {
				CvRect boudingSquare = cvBoundingRect(square);
				int width = boudingSquare.width();
				int height = boudingSquare.height();
				int relationDifference = Math.abs(width - height);
				int proportion = (width + height) / 2;
				int size = width * height;
				if (relationDifference < 13 && size > (300 * 300) && size < (500 * 500)) {
					if (pixelsForSquare == 0 || proportion < pixelsForSquare) {
						pixelsForSquare = proportion;
					}
				}
			}
		}
		System.err.println(pixelsForSquare);
	}
	
	//Cada quadrado possui 2x2 centímetros de tamanho, então divide por dois para retornar a escala por centimetro
	return pixelsForSquare / 2; 
    }

}
