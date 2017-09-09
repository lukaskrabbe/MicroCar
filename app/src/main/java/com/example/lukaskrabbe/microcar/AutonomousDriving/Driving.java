package com.example.lukaskrabbe.microcar.AutonomousDriving;

import android.util.Log;

import com.example.lukaskrabbe.microcar.Detection.*;
import com.example.lukaskrabbe.microcar.Microcar.Car;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * So basically, what this class is trying to do is: make everything a rectangle and compare them.
 * It is relatively easy to compare rectangles, even if they are rotated, which allows us to correct
 * the wonky car acceptably and drive on a path
 */
public class Driving {

    private static final String TAG = "AutonomousDriving";

    public static class CommandRectangle {
        public RectangleInterface lane;
        public RectangleInterface trigger;
        Direction direction;
        public CommandRectangle(RectangleInterface lane, RectangleInterface trigger, Direction direction) {
            this.lane = lane;
            this.trigger = trigger;
            this.direction = direction;
        }
    }

    private Car car;
    private Detection detector;
    private Pathfinding pathfinder;
    private static List<Direction> directions = Arrays.asList(Direction.inOrder());
    public CommandRectangle currentLane;
    public List<CommandRectangle> nextLanes;
    private boolean justSwitched;
    private long switchTime;


    /**
     * Spans CommandRectangles over the calculated path
     * @return List of CommandRectangles
     */
    private List<CommandRectangle> spanRectanglesOnPath() {
        Log.d(TAG, "Spanning rectangles over path");
        List<CommandRectangle> lanes = new ArrayList<>();
        List<PathfindingElement> path = new ArrayList<>(pathfinder.getPath());
        PathfindingElement start = path.remove(0);
        PathfindingElement end = null;
        // the smaller one will be the cars width
        double carWidth = detector.getCar().getWidth() < detector.getCar().getHeight() ? detector.getCar().getWidth() : detector.getCar().getHeight();
        carWidth = carWidth * Parameters.CAR_WIDTH_FACTOR;


        while (!path.isEmpty()) {
            if (start.direction != path.get(0).direction &&
                    (path.size() >= 2 && start.direction != path.get(1).direction) || path.size() <= 1) {


                if (path.size() == 0 || end == null) {
                    end = start;
                }
                double centerX = start.grid.getCenterX() + (end.grid.getCenterX() - start.grid.getCenterX()) / 2;
                double centerY = start.grid.getCenterY() + (end.grid.getCenterY() - start.grid.getCenterY()) / 2;
                double distance = start.grid.distance(end.grid);
                if (distance == 0) {
                    distance = carWidth;
                }
                switch (start.direction) {
                    case N:
                    case S:
                        lanes.add(new CommandRectangle(new Rectangle(centerX, centerY, distance, carWidth, -Parameters.SINGLE_DIRECTION_ANGLE), end.grid, start.direction));
                        break;
                    case E:
                    case W:
                        lanes.add(new CommandRectangle(new Rectangle(centerX, centerY, carWidth, distance, -Parameters.SINGLE_DIRECTION_ANGLE), end.grid, start.direction));
                        break;
                    case NE:
                    case SW:
                        lanes.add(new CommandRectangle(new Rectangle(centerX, centerY, distance, carWidth, -Parameters.DUAL_DIRECTION_ANGLE), end.grid, start.direction));
                        break;
                    case NW:
                    case SE:
                        lanes.add(new CommandRectangle(new Rectangle(centerX, centerY, carWidth, distance, -Parameters.DUAL_DIRECTION_ANGLE), end.grid, start.direction));
                        break;
                }
                if (!path.isEmpty()) {
                    start = path.remove(0);
                }
                end = null;

            } else {
                end = path.remove(0);
            }
        }
        return lanes;
    }


    public Driving(Car car, Detection detector, Pathfinding pathfinder) {
        this.car = car;
        this.detector = detector;
        this.pathfinder = pathfinder;
        this.justSwitched = false;
        this.switchTime = 0;
    }


    /**
     * Calculates the deviation the car has from the lane
     *
     * This does three things to get deviation:
     * 1. it detects if the car is within the bounds of the lane, if not, it calculates whether it is too far to the right or left
     *    this is however disabled for some time after changing lanes, because the car will not have been in the new lane at all,
     *    so the other mechanisms can correct that
     * 2. if it is within the lane, but the cars direction is not the same as the lanes, it will calculate how far off it is
     * 3. if it is within the lane and also in the correct direction, the angle of the lane and car are compared and their deviation
     *    gets returned. remember to have some tolerance here, because the camera can be shaky, the image distorted and so on
     *    to understand the weird angle calculations going on, you need to understand opencv rectangle rotation,
     *    see here: https://stackoverflow.com/a/16042780
     * @param car car rectangle
     * @param lane lane to calculate deviation from
     * @return value indicating deviation. a positive value means the car is too far to the right,
     *         a negative value to the left
     */
    public double getDeviation(RectangleInterface car, CommandRectangle lane) {
        Direction carDirection = detector.getCarDirection();
        double deviation;
        RectangleInterface carCenter = car.getCenterRectangle();

        // car position correction based on whether the car is in the lane
        if (!lane.lane.isIntersecting(carCenter) && !this.justSwitched) {
            Log.d(TAG, "Center not within lane");
            switch (lane.direction) {
                case N:
                    if (car.getCenterX() < lane.lane.getCenterX()) {
                        Log.d(TAG, lane.direction + ": Center is right");
                        return -Parameters.DEVIATION_VALUE;
                    } else {
                        Log.d(TAG, lane.direction + ": Center is left");
                        return Parameters.DEVIATION_VALUE;
                    }
                case S:
                    if (car.getCenterX() > lane.lane.getCenterX()) {
                        Log.d(TAG, lane.direction + ": Center is right");
                        return -Parameters.DEVIATION_VALUE;
                    } else {
                        Log.d(TAG, lane.direction + ": Center is left");
                        return Parameters.DEVIATION_VALUE;
                    }
                case E:
                    if (car.getCenterY() < lane.lane.getCenterY()) {
                        Log.d(TAG, lane.direction + ": Center is left");
                        return -Parameters.DEVIATION_VALUE;
                    } else {
                        Log.d(TAG, lane.direction + ": Center is left");
                        return Parameters.DEVIATION_VALUE;
                    }
                case W:
                    if (car.getCenterY() > lane.lane.getCenterY()) {
                        Log.d(TAG, lane.direction + ": Center is right");
                        return -Parameters.DEVIATION_VALUE;
                    } else {
                        Log.d(TAG, lane.direction + ": Center is left");
                        return Parameters.DEVIATION_VALUE;
                    }
                /*
                 The easiest approach is to compose three transformations:

                 A translation that brings point 1 to the origin
                 Rotation around the origin by the required angle
                 A translation that brings point 1 back to its original position
                 When you work this all out, you end up with the following transformation:

                 newX = centerX + (point2x-centerX)*Math.cos(x) - (point2y-centerY)*Math.sin(x);

                 newY = centerY + (point2x-centerX)*Math.sin(x) + (point2y-centerY)*Math.cos(x);

                 https://stackoverflow.com/a/12161405
                */
                case NE:
                case SE:
                case SW:
                case NW:
                    double centerX = lane.lane.getCenterX();
                    double centerY = lane.lane.getCenterY();
                    double newX = centerX + (car.getCenterX()-centerX)*Math.cos(-45) - (car.getCenterY() - centerY)*Math.sin(-45);
                    double newY = centerY + (car.getCenterX()-centerX)*Math.sin(-45) + (car.getCenterY() - centerY)*Math.cos(-45);
                    switch (lane.direction) {
                        case NE:
                            if (newX > centerX) {
                                Log.d(TAG, lane.direction + ": Center is right");
                                return Parameters.DEVIATION_VALUE;
                            } else {
                                Log.d(TAG, lane.direction + ": Center is left");
                                return -Parameters.DEVIATION_VALUE;
                            }
                        case SE:
                            if (newY > centerY) {
                                Log.d(TAG, lane.direction + ": Center is right");
                                return Parameters.DEVIATION_VALUE;
                            } else {
                                Log.d(TAG, lane.direction + ": Center is left");
                                return -Parameters.DEVIATION_VALUE;
                            }
                        case SW:
                            if (newX < centerX) {
                                Log.d(TAG, lane.direction + ": Center is right");
                                return Parameters.DEVIATION_VALUE;
                            } else {
                                Log.d(TAG, lane.direction + ": Center is left");
                                return -Parameters.DEVIATION_VALUE;
                            }
                        case NW:
                            if (newY < centerY) {
                                Log.d(TAG, lane.direction + ": Center is right");
                                return Parameters.DEVIATION_VALUE;
                            } else {
                                Log.d(TAG, lane.direction + ": Center is left");
                                return -Parameters.DEVIATION_VALUE;
                            }
                    }

            }

        }
        // correction based on car and lane direction
        if (carDirection != lane.direction) {
            Log.d(TAG, "getDeviation: direction difference, car: " + carDirection + " lane: " + lane.direction);

            int distance = directions.indexOf(lane.direction) - directions.indexOf(carDirection);
            if (distance > Parameters.CORRECTION_DISTANCE_TOLERANCE) {
                distance = -(distance - Parameters.CORRECTION_DISTANCE_VALUE);
            } else if (distance < -Parameters.CORRECTION_DISTANCE_TOLERANCE) {
                distance = -(distance + Parameters.CORRECTION_DISTANCE_VALUE);
            } else {
                distance = -distance;
            }
            return ((float) Parameters.DUAL_DIRECTION_ANGLE) * distance;

        } else {
        // correction based on difference of angles
            switch (lane.direction) {
                case N:
                case S:
                    if (car.getWidth() > car.getHeight()) { // deviaton to the right
                        Log.d(TAG, "N, S: getDeviation: deviation to the right");
                        deviation = car.getAngle() - lane.lane.getAngle();
                    } else {
                        Log.d(TAG, "N, S: getDeviation: deviation to the left");
                        deviation = car.getAngle() + lane.lane.getAngle();
                    }
                    break;
                case E:
                case W:
                    if (car.getHeight() > car.getWidth()) { // deviation to the right
                        Log.d(TAG, "E, W: getDeviation: deviation to the right");
                        deviation = car.getAngle() - lane.lane.getAngle();
                    } else {
                        Log.d(TAG, "E, W: getDeviation: deviation to the left");
                        deviation = car.getAngle() + lane.lane.getAngle();
                    }
                    break;
                default:
                    Log.d(TAG, "default deviation");
                    deviation = car.getAngle() - lane.lane.getAngle();
                    break;
            }
        }
        return deviation;

    }

    public void drive() {
        Log.d(TAG, "Preparing drive");
        this.nextLanes = spanRectanglesOnPath();
        this.currentLane = this.nextLanes.remove(0);
        RectangleInterface carRect;

        Log.d(TAG, "Starting drive");
        this.car.Drive(Parameters.CAR_DRIVE_SPEED);

        // as long as we have a lane to drive on, try driving on the lane...
        while (currentLane != null && this.car.hasConnection()) {
            carRect = detector.getCar();
            double deviation = getDeviation(carRect, currentLane);
            double absDeviation = Math.abs(deviation);
            int rotation;

            if (absDeviation <= Parameters.TOLERATED_DEVIATION) {
                rotation = 0;
            } else if (absDeviation > Parameters.EXTREME_DEVIATION) {
                rotation = Parameters.EXTREME_CORRECTION;
                Log.d(TAG, "Extreme correction");
                Log.d(TAG, "\tdeviation: " + deviation);
                Log.d(TAG, "\tcar direction: " + detector.getCarDirection());
                Log.d(TAG, "\trotation: " + rotation);
            } else if (absDeviation > Parameters.LARGE_DEVIATION) {
                rotation = Parameters.LARGE_DEVIATION;
                Log.d(TAG, "Large correction");
                Log.d(TAG, "\tdeviation: " + deviation);
                Log.d(TAG, "\tcar direction: " + detector.getCarDirection());
                Log.d(TAG, "\trotation: " + rotation);
            } else if (absDeviation > Parameters.MEDIUM_DEVIATON) {
                rotation = Parameters.MEDIUM_DEVIATON;
                Log.d(TAG, "Medium correction");
                Log.d(TAG, "\tdeviation: " + deviation);
                Log.d(TAG, "\tcar direction: " + detector.getCarDirection());
                Log.d(TAG, "\trotation: " + rotation);
            } else if (absDeviation > Parameters.MINOR_DEVIATION) {
                rotation = Parameters.TOLERATED_DEVIATION;
                Log.d(TAG, "Minor correction");
                Log.d(TAG, "\tdeviation: " + deviation);
                Log.d(TAG, "\tcar direction: " + detector.getCarDirection());
                Log.d(TAG, "\trotation: " + rotation);
            } else {
                rotation = 0;
            }

            if (deviation > Parameters.TOLERATED_DEVIATION) {
                this.car.Steer(-rotation);
                Log.d(TAG, "Turning " + (-rotation) + " degrees");
            } else {
                this.car.Steer(rotation);
                Log.d(TAG, "Turning " + rotation + " degrees");
            }

            if (carRect.isIntersecting(currentLane.trigger)) {
                if (!nextLanes.isEmpty()) {
                    Log.d(TAG, "Hit trigger for " + currentLane.lane + " at " + currentLane.trigger);
                    currentLane = nextLanes.remove(0);
                    Log.d(TAG, "New lane: " + currentLane.lane + " with trigger at " + currentLane.trigger);
                    justSwitched = true;
                    switchTime = System.currentTimeMillis();
                } else {
                    Log.d(TAG, "Hit final trigger");
                    currentLane = null;
                }
            }
            try {
                Thread.sleep(Parameters.FRAME_SKIP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (System.currentTimeMillis() - switchTime > Parameters.TRIGGER_INTERSECTION_DELAY) {
                justSwitched = false;
            }
        }
        this.car.Steer(0);
        try {
            Thread.sleep(Parameters.CAR_FINISH_DRIVING_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.car.Drive(0);

        // because why not!
        this.car.Horn(true);
        try {
            Thread.sleep(Parameters.CAR_FINISH_HORN1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.car.Horn(false);
        this.car.Horn(true);
        try {
            Thread.sleep(Parameters.CAR_FINISH_HORN2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.car.Horn(false);

    }

}
