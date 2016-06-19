package br.com.furb.pawDetection.core;

import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvResetImageROI;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_NONE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_CCOMP;
import static org.bytedeco.javacpp.opencv_imgproc.MORPH_RECT;
import static org.bytedeco.javacpp.opencv_imgproc.THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.cvBoundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.cvCreateStructuringElementEx;
import static org.bytedeco.javacpp.opencv_imgproc.cvErode;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindContours;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_imgproc.cvThreshold;

import java.util.List;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplConvKernel;
import org.bytedeco.javacpp.opencv_core.IplImage;

import br.com.furb.pawDetection.beans.BigDistance;
import br.com.furb.pawDetection.beans.Component;

public class RatioDefinition {

    public static int difeneRatio(IplImage mouseThresholding, CvRect mouseBounding) {

	// Calcula o valor médio
//	CvScalar avg = cvAvg(greenLayer);
//	System.out.println(avg.val(0));
//
//	IplImage mouseThresholding = cvCreateImage(cvGetSize(greenLayer), 8, 1);
//	double thresh = avg.val(0) * 1;
//	double maxval = 255;
//	cvThreshold(greenLayer, mouseThresholding, thresh, maxval, THRESH_BINARY);
//	cvSaveImage(path + "_mouse.jpg", mouseThresholding);
//
//	IplImage filtered = cvCreateImage(cvGetSize(mouseThresholding), 8, 1);
//	cvNot(mouseThresholding, mouseThresholding);
//	cvSub(greenLayer, mouseThresholding, filtered);
//	cvSaveImage(path + "_mouseFilter.jpg", filtered);
//
//	IplImage filteredTemp = cvCreateImage(cvGetSize(filtered),
//		filtered.depth(), 1);
//	cvCopy(filtered, filteredTemp);
//	CvMemStorage storage = CvMemStorage.create();
//	CvSeq contours = new CvContour(null);
//	cvFindContours(filteredTemp, storage, contours,
//		Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
//		CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));
//
//	CvSeq bigContour = Operations.findBigContour(contours);
//	CvRect mouseBounding = cvBoundingRect(bigContour);

	int pixelsForSquare = 0;
	double maxval = 255;

	for (double thresh = 129; pixelsForSquare == 0 && thresh < 180; thresh += 5) {
	    IplImage filteredThresholding = cvCreateImage(cvGetSize(mouseThresholding),
		    8, 1);
	    cvThreshold(mouseThresholding, filteredThresholding, thresh, maxval,
		    THRESH_BINARY);
	    cvSaveImage(PawDetection.PATH + "_mouseFilterThresholding.jpg",
		    filteredThresholding);

	    IplConvKernel element = cvCreateStructuringElementEx(7, 7, 0, 0,
		    MORPH_RECT);
	    IplImage erode = cvCreateImage(cvGetSize(filteredThresholding), 8,
		    1);
	    cvErode(filteredThresholding, erode, element, 1);
	    cvSaveImage(PawDetection.PATH + "_erode.jpg", erode);

	    IplImage squares = cvCreateImage(cvGetSize(erode),
		    erode.depth(), 1);
	    cvCopy(erode, squares);
	    CvMemStorage storage = CvMemStorage.create();
	    CvSeq contours = new CvContour(null);
	    cvFindContours(squares, storage, contours,
		    Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
		    CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));

	    List<Component> possibleSquares = Operations
		    .findBigFourContour(contours);

	    for (int i = 0, amount = possibleSquares.size(); i < amount; i++) {
		Component component = possibleSquares.get(i);
		CvSeq contour = component.getContour();
		BigDistance bigDistance = Operations
			.findBigDistanceOnContour(contour);

		double x = bigDistance.getPointA().x()
			- bigDistance.getPointB().x();
		double y = bigDistance.getPointA().y()
			- bigDistance.getPointB().y();
		double theta = Math.atan2(y, x);
		double angle = Operations.radianToDegressRotateSquare(theta);

		CvRect boundingBox = cvBoundingRect(contour);
		cvRectangle(squares,
			new CvPoint(boundingBox.x(), boundingBox.y()),
			new CvPoint(boundingBox.x() + boundingBox.width(),
				boundingBox.y() + boundingBox.height()),
			CV_RGB(255, 0, 0), 2, CV_AA, 0);
		cvSetImageROI(squares, boundingBox);

		IplImage rotatedPossibleSquare = Operations.rotateImage(
			squares, angle);
		cvSaveImage(PawDetection.PATH + "_square_rotated_" + i + ".jpg",
			rotatedPossibleSquare);
		cvResetImageROI(squares);

		storage = CvMemStorage.create();
		contours = new CvContour(null);
		IplImage contourTemp = cvCreateImage(
			cvGetSize(rotatedPossibleSquare), 8, 1);
		cvCopy(rotatedPossibleSquare, contourTemp);
		cvFindContours(contourTemp, storage, contours,
			Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
			CV_CHAIN_APPROX_NONE, new CvPoint(0, 0));

		if (contours.address() != 0) {
		    CvSeq square = Operations
			    .findBigContour(contours);
		    if (square != null) {
			CvRect boudingSquare = cvBoundingRect(square);
			int width = boudingSquare.width();
			int height = boudingSquare.height();
			int size = width * height;
			int relationDifference = Math.abs(width - height);
			int proportion = (width + height) / 2;
			if (relationDifference < 10 && size > 5000) {
			    if (pixelsForSquare == 0
				    || proportion < pixelsForSquare) {
				pixelsForSquare = proportion;
			    }
			}
		    }
		}
	    }
	    System.err.println(pixelsForSquare);
	}
	
	return pixelsForSquare;
    }
}
