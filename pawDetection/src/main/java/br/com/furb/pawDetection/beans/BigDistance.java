package br.com.furb.pawDetection.beans;

import org.bytedeco.javacpp.opencv_core.CvPoint;

public class BigDistance {

	private double distance;
	private CvPoint pointA;
	private CvPoint pointB;

	public BigDistance(double distance, CvPoint pointA, CvPoint pointB) {
		super();
		this.distance = distance;
		this.pointA = pointA;
		this.pointB = pointB;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public CvPoint getPointA() {
		return pointA;
	}

	public void setPointA(CvPoint pointA) {
		this.pointA = pointA;
	}

	public CvPoint getPointB() {
		return pointB;
	}

	public void setPointB(CvPoint pointB) {
		this.pointB = pointB;
	}

	@Override
	public String toString() {
		return "PawLength [distance=" + distance + ", pointA=(" + pointA.x() + ", " + pointA.y()
				+ "), pointB=(" + pointB.x() + ", " + pointB.y() + ")]";
	}

}
