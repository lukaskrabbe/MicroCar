package com.example.lukaskrabbe.microcar.Detection;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ObstacleDetection {

    private static final String TAG = "ObstacleDetection";

    private List<Rectangle> obstacles;
    private List<Rectangle> cones;
    private List<Waypoint> waypoints;
    private Grid grid;
    private double averageConeSize;

    private Mat mHsv;

    public ObstacleDetection(int height, int width) {
        this.mHsv = new Mat(height, width, CvType.CV_8UC4);
        this.obstacles = null;
        this.cones = null;
        this.waypoints = null;
        this.grid = null;
        this.averageConeSize = -1;
    }

    /**
     * Calibrates a grid on a given frame, finding all obstacles and spanning the grid.
     * @param frame OpenCV Frame containing obstacles and cones
     */
    public void runDetection(Mat frame) {

        // convert to hsv
        Imgproc.cvtColor(frame, mHsv, Imgproc.COLOR_RGB2HSV);
        // apply range filters
        Core.inRange(mHsv, Parameters.lowerBoundsObstacles, Parameters.upperBoundsObstacles, mHsv);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(mHsv, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // filter elements by minimal size, because very small elements are likely noise
        List<MatOfPoint> filteredElements = new ArrayList<>();
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(idx).toArray()));
                if (rect.size.height >= Parameters.MIN_RECT_SIZE_OBST && rect.size.width >= Parameters.MIN_RECT_SIZE_OBST) {
                    filteredElements.add(contours.get(idx));
                }
            }
        }

        Log.d(TAG, String.format("Found %d elements after filtering by size", filteredElements.size()));

        List<Rectangle> cones = getCones(filteredElements);
        List<Rectangle> obstacles = getObstacles(filteredElements);
        List<Waypoint> waypoints = null;
        List<Rectangle> all_obstacles = new ArrayList<>();
        all_obstacles.addAll(cones);
        all_obstacles.addAll(obstacles);

        double averageConeSize = 0;

        for (Rectangle rect: cones) {
            averageConeSize += (rect.size.width + rect.size.height) / 2;
        }

        if (averageConeSize != 0) {
            averageConeSize = averageConeSize / cones.size();
        }

        Rectangle gridBounds = getObstacleBounds(all_obstacles);

        // if there is literally anything to span a grid around, do it
        if (gridBounds != null) {

            if (averageConeSize != 0 && averageConeSize < 5) {
                averageConeSize = 5;
            }

            int topLeftX = (int) (gridBounds.getCenterX() - (gridBounds.size.width / 2));
            int topLeftY = (int) (gridBounds.getCenterY() - (gridBounds.size.height / 2));


            Grid grid = new Grid(topLeftX, topLeftY, (int) gridBounds.size.width, (int) gridBounds.size.height, (int) averageConeSize / 2, Parameters.GRID_SPACING);

            if (cones.size() > 0) {
                waypoints = determineConeWaypoints(cones, averageConeSize);
            }
            for (Rectangle cone: cones) {
                grid.addObstacle(cone, GridRectangle.States.CONE);
            }
            for (Rectangle obstacle: obstacles) {
                grid.addObstacle(obstacle, GridRectangle.States.OBSTACLE);
            }
            if (waypoints != null) {
                for (Rectangle waypoint : waypoints) {
                    grid.addObstacle(waypoint, GridRectangle.States.WAYPOINT);
                }
                all_obstacles.addAll(waypoints);
            }
            this.grid = grid;
        }
        this.averageConeSize = averageConeSize;
        this.obstacles = obstacles;
        this.cones = cones;
        this.waypoints = waypoints;


    }

    /**
     * Retrieves cones from a list of elements by checking whether they are approximately the same width and height
     * @param obstacles
     * @return List of detected cones
     */
    private static List<Rectangle> getCones(List<MatOfPoint> obstacles) {
        List<Rectangle> cones = new ArrayList<>();

        for (MatOfPoint contour: obstacles) {

            RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            Rectangle rectangle = new Rectangle(rotRect);
            double deviation = (rectangle.size.width / 100) * Parameters.CONE_DEVIATION;
            if (rectangle.size.width - deviation < rectangle.size.height && rectangle.size.height < rectangle.size.width + deviation) {
                cones.add(rectangle);
            }
        }
        Log.d(TAG, String.format("Found %d cones", cones.size()));
        return cones;
    }

    /**
     * Retrieves obstacles from a list of elements by checking whether they are not a square.
     * @param obstacles
     * @return
     */
    private static List<Rectangle> getObstacles(List<MatOfPoint> obstacles) {
        List<Rectangle> obst = new ArrayList<>();

        for (MatOfPoint contour: obstacles) {

            RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            Rectangle rectangle = new Rectangle(rotRect);
            double deviation = (rectangle.size.width / 100) * Parameters.CONE_DEVIATION;
            if (!(rectangle.size.width - deviation < rectangle.size.height && rectangle.size.height < rectangle.size.width + deviation)) {
                obst.add(rectangle);
            }
        }
        Log.d(TAG, String.format("Found %d obstacles", obst.size()));
        return obst;
    }

    /**
     * Retrieves the min and max coordinates for all elements in the list
     * @param obstacles
     * @return {@link Rectangle} using the min and max values
     */
    public static Rectangle getObstacleBounds(List<Rectangle> obstacles) {

        double min_x = -1;
        double min_y = -1;
        double max_x = -1;
        double max_y = -1;

        for (Rectangle obstacle : obstacles) {
            Point[] points = new Point[4];
            obstacle.points(points);
            for (Point point: points) {
                if (min_y == -1 || (point.y < min_y)) {
                    min_y = point.y;
                }
                if (max_y == -1 || (point.y > max_y)) {
                    max_y = point.y;
                }
                if (min_x == -1 || (point.x < min_x)) {
                    min_x = point.x;
                }
                if (max_x == -1 || (point.x > max_x)) {
                    max_x = point.x;
                }
            }
        }

        if (min_x != -1) {

            double width = max_x - min_x;
            double height = max_y - min_y;

            double center_x = min_x + width / 2;
            double center_y = min_y + height / 2;

            return new Rectangle(center_x, center_y, width, height, 0);

        }

        return null;

    }

    /**
     * Determines best fitting partner cone for passed cone
     * @param cones
     * @param cone
     * @return
     */
    private static Rectangle determineBestPairing(List<Rectangle> cones, Rectangle cone) {
        Rectangle bestRectangle = null;
        double bestRectangleDistance = 0;

        for (Rectangle rect : cones) {
            if (rect != cone) {
                double distance = rect.distance(cone);
                if (bestRectangle == null || bestRectangleDistance > distance) {
                    bestRectangle = rect;
                    bestRectangleDistance = distance;
                }
            }
        }
        return bestRectangle;
    }

    /**
     * Determines pairs for list of cones
     * @param cones
     * @param averageConeSize
     * @return List of array of rectangles containing pairs of cones
     */
    private static List<Rectangle[]> determineGatePairs(List<Rectangle> cones, double averageConeSize) {

        double MIN_DISTANCE = averageConeSize * Parameters.MIN_DISTANCE_FACTOR;
        double MAX_DISTANCE = averageConeSize * Parameters.MAX_DISTANCE_FACTOR;

        List<Rectangle> unusedCones = new ArrayList<>(cones);
        List<Rectangle[]> conePaires = new ArrayList<>();

        if (cones.size() < 2) {
            Log.d(TAG, "Cannot determine gate pairs, not enough cones.");
            return conePaires;
        }

        for (Rectangle cone: cones) {
            if (unusedCones.contains(cone)) {

                Rectangle bestForCone = determineBestPairing(unusedCones, cone);
                if (bestForCone != null && determineBestPairing(unusedCones, bestForCone) == cone) {
                    double distance = cone.distance(bestForCone);

                    if (MIN_DISTANCE <= distance && distance <= MAX_DISTANCE) {

                        Rectangle[] pair = new Rectangle[2];
                        pair[0] = cone;
                        pair[1] = bestForCone;
                        conePaires.add(pair);
                        unusedCones.remove(cone);
                        unusedCones.remove(bestForCone);

                        Log.d(TAG, "Determined pair is in range: " + distance + " - MIN: " + MIN_DISTANCE + " - MAX: " + MAX_DISTANCE);

                    } else {
                        Log.d(TAG, "Determined pair is out of range: " + distance + " - MIN: " + MIN_DISTANCE + " - MAX: " + MAX_DISTANCE);
                    }
                }
            }

        }

        Log.d(TAG, "Unable to determine pairs for " + unusedCones.size() + " cone(s)");

        return conePaires;
    }

    /**
     * Calculates the gates and waypoints for all passed cones
     * @param cones
     * @param averageConeSize
     * @return List of waypoints
     */
    public static List<Waypoint> determineConeWaypoints(List<Rectangle> cones, double averageConeSize) {
        List<Waypoint> waypoints = new ArrayList<>();
        List<Rectangle[]> pairs = determineGatePairs(cones, averageConeSize);

        for (Rectangle[] pair : pairs) {

            double vector_x = (pair[1].getCenterX() - pair[0].getCenterX()) / 2;
            double vector_y = (pair[1].getCenterY() - pair[0].getCenterY()) / 2;

            double x = pair[0].getCenterX() + vector_x;
            double y = pair[0].getCenterY() + vector_y;
            double distance = pair[0].distance(pair[1]);

            float rotation = -90;

            Rectangle rotationRectangle1 = new Rectangle(x, y, distance, pair[0].getHeight(), rotation);
            Rectangle rotationRectangle2 = new Rectangle(x, y, pair[0].getHeight(), distance, rotation);

            Rectangle intersectingRectangle = null;
            while (rotation <= 0) {
                rotationRectangle1.angle = rotation;
                rotationRectangle2.angle = rotation;
                if (rotationRectangle1.isIntersecting(pair[0])) {
                    intersectingRectangle = rotationRectangle2;
                    break;
                } else if (rotationRectangle2.isIntersecting(pair[0])) {
                    intersectingRectangle = rotationRectangle1;
                    break;
                }
                rotation += 5;
            }

            Direction[] dir = CarDirectionDetection.getPossibleDirectionsFromRect(intersectingRectangle);
            Direction[] dir0neighbours = dir[0].getDirectionalNeighbours();
            Direction[] dir1neighbours = dir[1].getDirectionalNeighbours();

            Direction[] possibleDirections = new Direction[6];

            possibleDirections[0] = dir[0];
            possibleDirections[1] = dir[1];
            possibleDirections[2] = dir0neighbours[0];
            possibleDirections[3] = dir0neighbours[2];
            possibleDirections[4] = dir1neighbours[0];
            possibleDirections[5] = dir1neighbours[2];

            Waypoint waypoint = new Waypoint(x, y, possibleDirections);
            waypoints.add(waypoint);
        }
        Log.d(TAG, "Found " + waypoints.size() + " waypoints using " + cones.size() + " cones");
        return waypoints;
    }

    public List<Rectangle> getObstacles() {
        return obstacles;
    }

    public List<Rectangle> getCones() {
        return cones;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Grid getGrid() {
        return grid;
    }

    public double getAverageConeSize() {
        return averageConeSize;
    }
}
