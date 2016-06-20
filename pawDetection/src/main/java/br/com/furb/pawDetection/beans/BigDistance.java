package br.com.furb.pawDetection.beans;

import org.bytedeco.javacpp.opencv_core.CvPoint;

public class BigDistance {

    private int distance;
    private CvPoint pointA;
    private CvPoint pointB;

    public BigDistance(int distance, CvPoint pointA, CvPoint pointB) {
	super();
	this.distance = distance;
	this.pointA = pointA;
	this.pointB = pointB;
    }

    public int getDistance() {
	return distance;
    }

    public CvPoint getPointA() {
	return pointA;
    }

    public CvPoint getPointB() {
	return pointB;
    }

}
