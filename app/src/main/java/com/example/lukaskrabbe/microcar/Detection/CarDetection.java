package com.example.lukaskrabbe.microcar.Detection;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class CarDetection {

    private static final String TAG = "CarDetection";

    /**
     * A LinkedHashMap can be uses as a finite queue which removes the oldest element
     */
    private LinkedHashMap<Long, Rectangle> previousCarPositions;
    private Mat mHsv;

    private Rectangle car;
    private long detectionTime;

    Rectangle getCar() {
        return this.car;
    }

    Long getDetectionTime() {
        return this.detectionTime;
    }


    /**
     * Car Detector
     * @param height of camera frames
     * @param width of camera frames
     */
    CarDetection(int height, int width) {
        previousCarPositions = new LinkedHashMap<Long, Rectangle>()
        {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Rectangle> eldest)
            {
                return this.size() > Parameters.COMPARE_FRAMES; // we only want to compare our current frame with the last 3 frames
            }
        };
        this.mHsv = new Mat(height, width, CvType.CV_8UC4);
    }

    /**
     * Performs a car detection on the given frame
     * @param frame to be analyzed
     * @param averageConeSize average size of detected cones, used for approximating sizes
     */
    void runDetection(Mat frame, double averageConeSize) {
        long detectionTime = System.currentTimeMillis();

        Imgproc.cvtColor(frame, mHsv, Imgproc.COLOR_RGB2HSV);
        Core.inRange(mHsv, Parameters.lowerBoundsCar, Parameters.upperBoundsCar, mHsv);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(mHsv, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rectangle> filteredElements = new ArrayList<>();

        double min_car_length = Parameters.MIN_CAR_LENGTH * averageConeSize;
        double min_car_width = Parameters.MIN_CAR_WIDTH   * averageConeSize;
        double max_car_length = Parameters.MAX_CAR_LENGTH * averageConeSize;
        double max_car_width = Parameters.MAX_CAR_WIDTH   * averageConeSize;

        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(idx).toArray()));
                double length = rect.size.height > rect.size.width ? rect.size.height : rect.size.width;
                double width = rect.size.height < rect.size.width ? rect.size.height : rect.size.width;
                if (max_car_length >= length && length >= min_car_length &&
                        max_car_width >= width && width >= min_car_width) {
                    filteredElements.add(new Rectangle(rect));
                }
            }
        }

        Rectangle detectedCar = null;

        if (filteredElements.size() > 1) {
            Log.e(TAG, "Detected too many (" + filteredElements.size() + ") car-like objects, filtering by finding first match " +
                    "according to relative size");

            Rectangle bestRect = filteredElements.remove(0);
            double bestDeviation = minimalCarDimensionDeviation(bestRect, averageConeSize);

            for (Rectangle rect : filteredElements) {
                double deviation = minimalCarDimensionDeviation(rect, averageConeSize);
                if (deviation < bestDeviation) {
                    bestRect = rect;
                    bestDeviation = deviation;
                }
            }
            detectedCar = bestRect;
        } else if (filteredElements.size() == 1) {
            detectedCar = filteredElements.get(0);
            Log.d(TAG, "Car detected");
        } else {
            Log.e(TAG, "Could not detect car");
        }

        if (detectedCar != null && isRectangleProbablyCar(detectedCar)) {
            this.car = detectedCar; // our last frame was likely the car, so push that one out
            this.detectionTime = detectionTime;
            this.previousCarPositions.put(detectionTime, detectedCar);
        }

    }

    /**
     * Determines minimal sum of length and width deviation the rectangle has from known
     * car dimensions. It checks both rect width and length as car width and length, because we
     * may not know our orientation.
     * @param rect
     * @param averageConeSize
     * @return minimal deviation
     */
    private double minimalCarDimensionDeviation(Rectangle rect, double averageConeSize) {

        double dev1 = Math.abs(Parameters.CAR_LENGTH - rect.size.width / averageConeSize) +
                Math.abs(Parameters.CAR_WIDTH - rect.size.height / averageConeSize);
        double dev2 = Math.abs(Parameters.CAR_WIDTH - rect.size.width / averageConeSize) +
                Math.abs(Parameters.CAR_LENGTH - rect.size.height / averageConeSize);

        return dev1 < dev2 ? dev1 : dev2;
    }

    /**
     * Determines whether a rectangle is probably the car using previous known positions
     * @param rect
     * @return
     */
    private boolean isRectangleProbablyCar(Rectangle rect) {

        long currentTime = System.currentTimeMillis();

        int acceptableCarPositions = 0;
        int currentCarPositions = 0;

        for (Entry<Long, Rectangle> entry : this.previousCarPositions.entrySet()) {
            long key = entry.getKey();
            Rectangle value = entry.getValue();

            // if entries are too old, we do not care about them
            if (currentTime - key > Parameters.DETECTION_TIMEOUT) {
                break;
            }

            currentCarPositions++;

            if (Math.abs(value.getCenterX() - rect.getCenterX()) < Parameters.CAR_RECTANGLE_POSITION_DEVIATION &&
                    Math.abs(value.getCenterY() - rect.getCenterY()) < Parameters.CAR_RECTANGLE_POSITION_DEVIATION &&
                    Math.abs(Math.abs(360 - value.getAngle()) - Math.abs(360 - rect.getAngle())) < Parameters.CAR_RECTANGLE_ROTATION_DEVIATION) {
                acceptableCarPositions++;
            }

        }

        // only assume it's the car if we have less than one frame telling us otherwise
        return currentCarPositions - acceptableCarPositions < 1;

    }
}
