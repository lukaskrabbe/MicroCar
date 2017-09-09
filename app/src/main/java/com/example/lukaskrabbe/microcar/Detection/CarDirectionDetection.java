package com.example.lukaskrabbe.microcar.Detection;

import android.util.Log;

import com.example.lukaskrabbe.microcar.Microcar.Car;

import java.util.Arrays;

public class CarDirectionDetection {
    private static final String TAG = "CarDirectionDetection";

    private Direction direction;
    private boolean stopDetection;
    private final Object lock = new Object();

    CarDirectionDetection() {
        direction = null;
        stopDetection = true;
    }

    /**
     * runDetection periodically calculates the cars current direction using the car rectangles current angle
     * this assumes that the car will not abruptly change directions and a new direction will always be
     * a neighbour of the previous direction.
     * @param detector Detection containing car
     */
    void runDetection(Detection detector) {
        if (direction == null) {
            Log.e(TAG, "runDetection started without previously known position, bailing out");
            return;
        }
        synchronized (lock) {
            stopDetection = false;
        }
        while (true) {
            // check if we were asked to stop
            synchronized (lock) {
                if (stopDetection) {
                    break;
                }
            }
            RectangleInterface car = detector.getCar();
            Direction[] possibleDirections = getPossibleDirectionsFromRect(car);
            for (Direction newDirection : possibleDirections) {
                if (this.direction == newDirection) {
                    break;
                } else if (Arrays.asList(this.direction.getDirectionalNeighbours()).contains(newDirection)) {
                    Log.d(TAG, "Car changed direction from " + this.direction + " to " + newDirection);
                    this.direction = newDirection;
                    break;
                }
            }
            try {
                Thread.sleep(Parameters.DIRECTION_DETECTION_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * stops periodic car direction detection
     */
    void stopDetection() {
        synchronized (lock) {
            stopDetection = true;
        }
    }

    /**
     * Calculates the {@link Direction}s a rotated rectangle can be in, e.g.
     * |--------|
     * |        |
     * |--------|
     *
     * can be both {@link Direction#E} or {@link Direction#W}
     *
     * @param rect
     * @return
     */
    public static Direction[] getPossibleDirectionsFromRect(RectangleInterface rect) {
        Direction directions[] = new Direction[2];
        double sectionSize = 360 / 8;
        double angle = rect.getReadableRotation();

        Log.d(TAG, "Readable rotation: " + angle);

        if ((0 <= angle && angle < 0 + (sectionSize / 2)) ||
                180 - (sectionSize / 2) <= angle && angle <= 180) {
            directions[0] = Direction.S;
            directions[1] = Direction.N;
        } else if (0 + (sectionSize / 2) <= angle && angle < 0 + (sectionSize / 2) + sectionSize) {
            directions[0] = Direction.NE;
            directions[1] = Direction.SW;
        } else if (0 + (sectionSize / 2) + sectionSize <= angle &&  angle < 0 + (sectionSize / 2) + 2 * sectionSize) {
            directions[0] = Direction.E;
            directions[1] = Direction.W;
        } else if (0 + (sectionSize / 2) + 2 * sectionSize <= angle &&  angle < 0 + (sectionSize / 2) + 3 * sectionSize) {
            directions[0] = Direction.SE;
            directions[1] = Direction.NW;
        }
        return directions;
    }

    /**
     * Determines the direction of the car by driving forwards and backwards and comparing the
     * difference of the observed car positions
     * @param car
     * @param detector
     */
    void runStartDetection(Car car, Detection detector) {
        car.Steer(0);
        // Gather data.
        RectangleInterface oldPos = detector.getCar();
        car.Drive(Parameters.DIRECTION_DETECTION_SPEED);
        try {
            Thread.sleep(Parameters.DIRECTION_DETECTION_FORWARD_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RectangleInterface newPos = detector.getCar();
        // Set car back to approximate old position.
        car.Drive(-Parameters.DIRECTION_DETECTION_SPEED);
        try {
            Thread.sleep(Parameters.DIRECTION_DETECTION_FORWARD_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        car.Drive(0);

        double angle = oldPos.getReadableRotation();
        double sectionSize = 360 / 8;

        // Evaluate gathered data.
        int x = (int) (newPos.getCenterX() - oldPos.getCenterX());
        int y = (int) (newPos.getCenterY() - oldPos.getCenterY());

        if ((0 < angle && angle < 0 + (sectionSize / 2)) ||
                180 - (sectionSize / 2) <= angle && angle <= 180) {
            if (0 < y) {
                direction = Direction.S;
            } else {
                direction = Direction.N;
            }
        } else if (0 + (sectionSize / 2) <= angle && angle < 0 + (sectionSize / 2) + sectionSize) {
            if (y < 0 && 0 < x) {
                direction = Direction.NE;
            } else {
                direction = Direction.SW;
            }
        } else if (0 + (sectionSize / 2) + sectionSize <= angle &&  angle < 0 + (sectionSize / 2) + 2 * sectionSize) {
            if (0 < x) {
                direction = Direction.E;
            } else {
                direction = Direction.W;
            }
        } else if (0 + (sectionSize / 2) + 2 * sectionSize <= angle &&  angle < 0 + (sectionSize / 2) + 3 * sectionSize) {
            if (0 < y && 0 < x) {
                direction = Direction.SE;
            } else {
                direction = Direction.NW;
            }
        }
        Log.d(TAG, "Detected car direction " + direction);
    }

    public Direction getDirection() {
        return this.direction;
    }

}
