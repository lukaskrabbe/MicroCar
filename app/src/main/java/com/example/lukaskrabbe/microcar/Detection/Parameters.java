package com.example.lukaskrabbe.microcar.Detection;

import org.opencv.core.Scalar;

public class Parameters {

    /**
     * Lower and upper color bounds for obstacle detection
     *
     * These defaults can be overridden using the application options.
     */

    public static Scalar lowerBoundsObstacles = new Scalar(0, 81, 95);
    public static Scalar upperBoundsObstacles = new Scalar(179, 255, 255);

    /**
     * Max values for hue, saturation and value, as required by OpenCV
     */
    public static int MAX_H = 179;
    public static int MAX_S = 255;
    public static int MAX_V = 255;


    /**
     * Size filter for obstacles, eliminating small artifacts
     */
    public static final int MIN_RECT_SIZE_OBST = 10;

    /**
     * Lower and upper color bounds for car detection
     */
    public static Scalar lowerBoundsCar = new Scalar(0, 0, 0);
    public static Scalar upperBoundsCar = new Scalar(179, 255, 50);

    /**
     * Amount of deviation still recognized as a cone. In percent.
     */
    public static final double CONE_DEVIATION = 40; // 40%

    /**
     * Amount of deviation still recognized as a car. In percent.
     */
    public static final double CAR_DEVIATION = 60; // 60%

    /**
     * Actual size of cones in mm
     */
    public static double CONE_SIZE = 11; // 11mm

    /**
     * Actual size of jenga blocks
     */
    public static double JENGA_LENGTH = 75; // 75mm
    public static double JENGA_WIDTH = 25; // 25mm

    /**
     * Size of car relative to cones
     */
    public static double CAR_LENGTH = 55 / CONE_SIZE; // 55mm - real size size relative to cone
    public static double CAR_WIDTH = 30 / CONE_SIZE; // 30mm - real size size relative to cone

    /**
     * Deviation car may have from previous detected car to still count as car
     */
    public static final double CAR_RECTANGLE_POSITION_DEVIATION = 80; // position units
    public static final double CAR_RECTANGLE_ROTATION_DEVIATION = 15; // degrees


    /**
     * Lower and upper size bounds for car detection
     */
    public static double MAX_CAR_LENGTH = (CAR_LENGTH / 100) * (100 + CAR_DEVIATION);
    public static double MIN_CAR_LENGTH = (CAR_LENGTH / 100) * (100 - CAR_DEVIATION);
    public static double MAX_CAR_WIDTH = (CAR_WIDTH / 100) * (100 + CAR_DEVIATION);
    public static double MIN_CAR_WIDTH = (CAR_WIDTH / 100) * (100 - CAR_DEVIATION);


    /**
     * Constants for direction detection.
     */
    public static final long DIRECTION_DETECTION_FORWARD_TIME = 1000;
    public static final int DIRECTION_DETECTION_SPEED = 10;

    /**
     * Number of extra grid elements added on EACH side
     */
    public static final int GRID_SPACING = 10;


    /**
     * Timeout values
     */
    public static final int COMPARE_FRAMES = 3;
    public static final int DETECTION_TIMEOUT = 200;
    public static final int DIRECTION_DETECTION_TIMEOUT = 16;

    /**
     * Max Depth for Obstacle search on Grid.
     */
    public static final int MAX_OBSTACLE_DEPTH = 200;

    /**
     * Distance factors for Obstacle detection.
     */
    public static final int MIN_DISTANCE_FACTOR = 4;
    public static final int MAX_DISTANCE_FACTOR = 10;

    /**
     * Size of car center rectangle.
     */
    public static final int CAR_CENTER_RECTANGLE_SIZE = 1;
}
