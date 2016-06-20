package br.com.furb.pawDetection.beans;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class PawDimension {

    private IplImage paw;
    private double pawWidth;
    private double pawHeight;
    private boolean isRatioAplly;

    public PawDimension(double pawWidth, double pawHeight, IplImage paw) {
	this.paw = paw;
	this.pawWidth = pawWidth;
	this.pawHeight = pawHeight;
	isRatioAplly = false;
    }

    public double getPawWidth() {
	return pawWidth;
    }

    public void setPawWidth(double pawWidth) {
	this.pawWidth = pawWidth;
    }

    public double getPawHeight() {
	return pawHeight;
    }

    public void setPawHeight(double pawHeight) {
	this.pawHeight = pawHeight;
    }

    public IplImage getPaw() {
        return paw;
    }

    public void setPaw(IplImage paw) {
        this.paw = paw;
    }

    public void apllyRatio(double pawRatio) {
	if (!isRatioAplly && pawRatio != 0) {
	    pawWidth /= pawRatio;
	    pawHeight /= pawRatio;

	    isRatioAplly = true;
	}

    }

}
