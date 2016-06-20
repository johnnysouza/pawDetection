package br.com.furb.pawDetection.beans;

import static org.bytedeco.javacpp.opencv_imgproc.cvBoundingRect;

import java.util.Comparator;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;

public class Component implements Comparator<Component> {

    private double size;
    private CvSeq contour;
    private BigDistance length;

    public Component(double size, CvSeq contour) {
	super();
	this.size = size;
	this.contour = contour;
    }

    public double getSize() {
	return size;
    }

    public CvSeq getContour() {
	return contour;
    }

    public BigDistance getLength() {
	return length;
    }

    public void setLength(BigDistance length) {
	this.length = length;
    }

    public CvRect getBoundingBox() {
	if (contour == null) {
	    return null;
	}
	return cvBoundingRect(contour);
    }

    public int compare(Component o1, Component o2) {
	if (o1.getSize() < o2.getSize()) {
	    return 1;
	}
	if (o1.getSize() > o2.getSize()) {
	    return -1;
	}
	return 0;
    }

    @Override
    public String toString() {
	return "Paw [size=" + size + "]";
    }

}
