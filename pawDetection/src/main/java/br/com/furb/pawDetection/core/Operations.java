package br.com.furb.pawDetection.core;

import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvTranspose;
import static org.bytedeco.javacpp.opencv_imgproc.cvContourArea;
import static org.bytedeco.javacpp.opencv_imgproc.cvGetQuadrangleSubPix;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.CvSize;
import org.bytedeco.javacpp.opencv_core.IplImage;

import br.com.furb.pawDetection.beans.BigDistance;
import br.com.furb.pawDetection.beans.Component;
import br.com.furb.pawDetection.types.Orientation;

public class Operations {

    public static BigDistance findBigDistanceOnContour(CvSeq contour) {
	BigDistance pawLength = null;
	int countourSize = contour.total();
	for (int j = 0; j < countourSize; j++) { // Percorre todos os pontos da
						 // pegada para encontrar os
						 // dois mais distantes que
						 // são as pontas do
						 // calcanhar e do terceiro
						 // dedo
	    CvPoint pointA = new CvPoint(cvGetSeqElem(contour, j));
	    for (int k = j; k < countourSize; k++) {
		CvPoint pointB = new CvPoint(cvGetSeqElem(contour, k));

		// Caucula a distância euclidiana entre dois pontos da pata,
		// para encontrar o seu comprimento.
		double distance = Math
			.sqrt(Math.pow(pointA.x() - pointB.x(), 2)
				+ Math.pow(pointA.y() - pointB.y(), 2));
		if (pawLength == null || distance > pawLength.getDistance()) {
		    pawLength = new BigDistance(distance, pointA, pointB);
		}
	    }
	}
	return pawLength;
    }

    private static int radianToDegressRotate(double radians,
	    Orientation orientation) {
	int degress = (int) (radians * (180 / Math.PI));
	if (orientation == Orientation.VERTICAL) {
	    if (degress > 90) {
		return 90 - degress;
	    } else {
		return 270 - degress;
	    }
	} else {
	    return 180 - degress;
	}
    }

    public static IplImage rotate(IplImage src, int angle) {
	IplImage img = IplImage.create(src.roi().height(), src.roi().width(),
		src.depth(), src.nChannels());

	cvTranspose(src, img);
	cvFlip(img, img, 0);
	return img;
    }

    public static IplImage rotateImage(IplImage src, double angleDegrees) {
	// Create a map_matrix, where the left 2x2 matrix
	// is the transform and the right 2x1 is the dimensions.
	float[] m = new float[6];
	CvMat M = CvMat.create(2, 3, CV_32F);
	int w = src.roi().width();
	int h = src.roi().height();
	double angleRadians = angleDegrees * (Math.PI / 180.0f);
	m[0] = (float) (Math.cos(angleRadians));
	m[1] = (float) (Math.sin(angleRadians));
	m[3] = -m[1];
	m[4] = m[0];
	m[2] = w * 0.5f;
	m[5] = h * 0.5f;
	M.put(0, m[0]);
	M.put(1, m[1]);
	M.put(2, m[2]);
	M.put(3, m[3]);
	M.put(4, m[4]);
	M.put(5, m[5]);

	// Make a spare image for the result
	CvSize sizeRotated = new CvSize();
	sizeRotated.width(Math.round(w));
	sizeRotated.height(Math.round(h));

	// Rotate
	IplImage imageRotated = cvCreateImage(sizeRotated, src.depth(),
		src.nChannels());

	// Transform the image
	cvGetQuadrangleSubPix(src, imageRotated, M);

	return imageRotated;
    }

    public static CvSeq findBigContour(CvSeq contours) {
	CvSeq ptr = new CvSeq();
	double bigArea = 0;
	CvSeq bigContour = null;
	for (ptr = contours; ptr != null && ptr.address() != 0; ptr = ptr
		.h_next()) {
	    double size = cvContourArea(ptr);
	    if (size > bigArea) {
		bigArea = size;
		bigContour = ptr;
	    }
	}
	return bigContour;
    }

    public static List<Component> findBigFourContour(CvSeq contours) {
	return findBigContours(contours, 4);
    }

    public static List<Component> findBigTwoContour(CvSeq contours) {
	return findBigContours(contours, 2);
    }

    public static List<Component> findBigContours(CvSeq contours,
	    int maxContours) {
	CvSeq ptr = null;
	List<Component> bigPawList = new ArrayList<Component>();

	for (ptr = contours; ptr != null; ptr = ptr.h_next()) {
	    double size = cvContourArea(ptr);
	    Component newPaw = new Component(size, ptr);
	    boolean isBig = bigPawList.size() == 0; // Na primeira execução
	    // inicializa como true
	    for (Component paw : bigPawList) {
		if (newPaw.getSize() > paw.getSize()) {
		    isBig = true;
		}
	    }
	    if (isBig) {
		if (bigPawList.size() >= maxContours) {
		    bigPawList.remove(maxContours - 1);
		}
		bigPawList.add(newPaw);
		bigPawList.sort(newPaw);
	    }
	}

	return bigPawList;
    }

    public static int radianToDegressRotateSquare(double radians) {
	int degress = (int) (radians * (180 / Math.PI));
	return 45 - degress;
    }

}
