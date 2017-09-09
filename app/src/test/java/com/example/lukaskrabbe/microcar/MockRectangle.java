package com.example.lukaskrabbe.microcar;

import com.example.lukaskrabbe.microcar.Detection.RectangleInterface;

public class MockRectangle implements RectangleInterface {

    private double centerX;
    private double centerY;
    private double width;
    private double height;
    private double angle;

    MockRectangle(double centerX, double centerY, double width, double height, double angle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.angle = angle;
    }

    @Override
    public double distance(RectangleInterface otherRect) {
        double a = this.getCenterX() - otherRect.getCenterX();
        double b = this.getCenterY() - otherRect.getCenterY();

        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    @Override
    public boolean isIntersecting(RectangleInterface otherRect) {
        // this will need to be mocked depending on the case, because implementing it is a PITA
        // https://github.com/opencv/opencv/blob/05b15943d6a42c99e5f921b7dbaa8323f3c042c6/modules/imgproc/src/intersection.cpp
        // knock yourself out if you feel like trying that...
        return false;
    }

    @Override
    public double getCenterX() {
        return this.centerX;
    }

    @Override
    public double getCenterY() {
        return this.centerY;
    }

    @Override
    public double getWidth() {
        return this.width;
    }

    @Override
    public double getHeight() {
        return this.height;
    }

    @Override
    public double getAngle() {
        return this.angle;
    }

    @Override
    public MockRectangle getCenterRectangle() {
        return new MockRectangle(this.getCenterX(), this.getCenterY(), 1, 1, this.getAngle());
    }

    @Override
    public double getReadableRotation() {
        if(this.getWidth() < this.getHeight()){
            return this.getAngle() + 180;
        } else {
            return this.getAngle() + 90;
        }
    }
}
