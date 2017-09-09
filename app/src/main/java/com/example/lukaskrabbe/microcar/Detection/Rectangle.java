package com.example.lukaskrabbe.microcar.Detection;

import com.example.lukaskrabbe.microcar.AutonomousDriving.*;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

public class Rectangle extends RotatedRect implements RectangleInterface {

    // this is only a temp variable we don't want to create for every intersection
    private static Mat intersectingRegion = new Mat();

    /**
     * Create Rectangle from parameters
     * @param center_x
     * @param center_y
     * @param width
     * @param height
     * @param angle
     */
    public Rectangle(double center_x, double center_y, double width, double height, double angle) {
        super(new Point(center_x, center_y), new Size(width, height), angle);
    }

    /**
     * Create Rectangle from {@link RotatedRect}
     * @param rect
     */
    public Rectangle(RotatedRect rect) {
        super(rect.center, rect.size, rect.angle);
    }

    /**
     * Calculates distance between the center of this and a given {@link Rectangle}
     * @param otherRect
     * @return Distance between the Rectangles
     */
    public double distance(RectangleInterface otherRect) {

        double a = this.getCenterX() - otherRect.getCenterX();
        double b = this.getCenterY() - otherRect.getCenterY();

        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));

    }

    /**
     * Determines if two {@link Rectangle}s are intersecting
     * @param otherRect
     * @return True if Rectangles are intersecting, otherwise false.
     */
    public boolean isIntersecting(RectangleInterface otherRect) {
        return Imgproc.rotatedRectangleIntersection(this, (RotatedRect) otherRect, Rectangle.intersectingRegion) > 0;
    }

    @Override
    public double getReadableRotation() {
        if(this.size.width < this.size.height){
            return this.angle + 180;
        } else {
            return this.angle + 90;
        }
    }

    @Override
    public String toString() {
        return "Position: " + this.center + " - Size: " + this.size + " - Angle : " + this.angle;
    }

    @Override
    public double getCenterX() {
        return this.center.x;
    }

    @Override
    public double getCenterY() {
        return this.center.y;
    }

    @Override
    public double getWidth() {
        return this.size.width;
    }

    @Override
    public double getHeight() {
        return this.size.height;
    }

    @Override
    public double getAngle() {
        return this.angle;
    }

    @Override
    public Rectangle getCenterRectangle() {
        return new Rectangle(this.getCenterX(), this.getCenterY(), Parameters.CAR_CENTER_RECTANGLE_SIZE, Parameters.CAR_CENTER_RECTANGLE_SIZE, this.getAngle());
    }
}
