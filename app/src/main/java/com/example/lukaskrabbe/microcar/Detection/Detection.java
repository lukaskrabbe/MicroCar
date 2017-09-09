package com.example.lukaskrabbe.microcar.Detection;

import android.util.Log;

import com.example.lukaskrabbe.microcar.MainActivity;

import org.opencv.core.Mat;

import java.util.List;


/**
 * Container class taking care of all detection operations
 */
public class Detection {

    private CarDetection carDetector;
    private CarDirectionDetection carDirectionDetection;
    private ObstacleDetection obstacleDetector;

    /**
     * Detection
     * @param height Height of camera frames
     * @param width Width of camera frames
     */
    public Detection(int height, int width) {

        this.obstacleDetector = new ObstacleDetection(height, width);
        this.carDetector = new CarDetection(height, width);
        this.carDirectionDetection = new CarDirectionDetection();

    }

    /**
     * Performs a grid detection operation on a frame
     * @param frame to be analyzed
     */
    public void detectObstacles(Mat frame) {
        obstacleDetector.runDetection(frame);
    }

    /**
     * Performs a car detection operation on a frame
     * @param frame to be analyzed
     */
    public void detectCar(Mat frame) {
        carDetector.runDetection(frame, obstacleDetector.getAverageConeSize());
    }

    /**
     * Performs a car direction detection, takes about 2x
     * {@link Parameters#DIRECTION_DETECTION_FORWARD_TIME} milliseconds and requires some space.
     */
    public void detectInitialCarDirection() {
        carDirectionDetection.runStartDetection(MainActivity.car, this);
    }

    public void startPeriodicCarDetection() {
        carDirectionDetection.runDetection(this);
    }

    public void stopPeriodicCarDetection() {
        carDirectionDetection.stopDetection();
    }

    public Grid getGrid() {
        return this.obstacleDetector.getGrid();
    }

    public List<Waypoint> getWaypoints() {
        return this.obstacleDetector.getWaypoints();
    }

    public RectangleInterface getCar() {
        return this.carDetector.getCar();
    }

    public long getCarDetectionTime() {
        return this.carDetector.getDetectionTime();
    }

    public Direction getCarDirection() {
        return this.carDirectionDetection.getDirection();
    }

}
