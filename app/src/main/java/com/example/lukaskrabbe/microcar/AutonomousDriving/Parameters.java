package com.example.lukaskrabbe.microcar.AutonomousDriving;

public class Parameters {
    /**
     * Car width factor for autonomous driving detection.
     */
    public static final float CAR_WIDTH_FACTOR = 0.8f;

    /**
     * Lane rectangle angles.
     */
    public static final int SINGLE_DIRECTION_ANGLE = 90;
    public static final int DUAL_DIRECTION_ANGLE = 45;

    /**
     * Car behavior
     */
    public static final int CAR_DRIVE_SPEED = 10;
    public static final int CAR_FINISH_HORN1 = 150;
    public static final int CAR_FINISH_HORN2 = 400;
    public static final int CAR_FINISH_DRIVING_TIME = 1000;

    /**
     * Pathfinding costs
     */
    public static final int OBSTACLE_COST = 50000;
    public static final int AVOID_COST = 15000;
    public static final int ILLEGAL_DIRECTION_COST = 50000;
    public static final int REPEATED_DIRECTION_CHANGE_COST = 25000;
    public static final int DIRECTION_CHANGE_COST = 200;

    /**
     * REPETITION_CHANGE defines at what point a direction change is not a repeated direction change.
     */
    public static final int REPETITION_TOLERANCE = 5;

    /**
     * DEVIATION_VALUE will be returned on when the deviation.
     */
    public static final float DEVIATION_VALUE = 70.0f;

    /**
     * Deviation tolerances
     */
    public static final int TOLERATED_DEVIATION = 5;
    public static final int EXTREME_DEVIATION = 70;
    public static final int LARGE_DEVIATION = 40;
    public static final int MEDIUM_DEVIATON = 20;
    public static final int MINOR_DEVIATION = 10;

    /**
     * Correction values
     */
    public static final int CORRECTION_DISTANCE_TOLERANCE = 4;
    public static final int CORRECTION_DISTANCE_VALUE = 8;
    public static final int EXTREME_CORRECTION = 50;

    /**
     * Wait a half frame.
     */
    public static final int FRAME_SKIP = 10;

    /**
     * Ignore rectangle intersection detection for the given milliseconds.
     */
    public static final int TRIGGER_INTERSECTION_DELAY = 800;
}
