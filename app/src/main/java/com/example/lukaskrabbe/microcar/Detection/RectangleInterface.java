package com.example.lukaskrabbe.microcar.Detection;

public interface RectangleInterface {
    /**
     * Calculates distance between the center of this and a given {@link Rectangle}
     * @param otherRect
     * @return Distance between the Rectangles
     */
    double distance(RectangleInterface otherRect);

    /**
     * Determines if two {@link Rectangle}s are intersecting
     * @param otherRect
     * @return True if Rectangles are intersecting, otherwise false.
     */
    boolean isIntersecting(RectangleInterface otherRect);


    /**
     * Returns the width of the Rectangle.
     * @return width
     */
    double getWidth();

    /**
     * Returns the height of the Rectangle
     * @return height
     */
    double getHeight();

    /**
     * Returns the rotation of the Rectangle
     * @return Center point.
     */
    double getAngle();

    /**
     * Returns the x value of the center point of the RectangleInterface.
     * @return Center x
     */
    double getCenterX();

    /**
     * Returns the y value of the center point of the RectangleInterface.
     * @return Center y
     */
    double getCenterY();

    /**
     * Returns a 1x1 Rectangle with the center of the Rectangle as origin.
     * @return 1x1 Rectangle in the current Rectangles center
     */
    RectangleInterface getCenterRectangle();

    /**
     * Returns a human-readable rotation between 0 and 180 degrees
     * ONLY use this for comparisons, do not create any objects using this or store it in any way
     * @return rotation in degrees
     */
    double getReadableRotation();



}
