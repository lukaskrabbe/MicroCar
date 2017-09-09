package com.example.lukaskrabbe.microcar.AutonomousDriving;

import android.util.Log;

import com.example.lukaskrabbe.microcar.Detection.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A* based pathfinding on {@link Grid} using Waypoints.
 */
public class Pathfinding {

    private static final String TAG = "Pathfinding";

    private final static int SEARCH_FACTOR = 4000;
    private final static int MAX_SEARCH_DEPTH = SEARCH_FACTOR * 7;


    private volatile List<PathfindingElement> path;

    public List<PathfindingElement> getPath() {
        return this.path;
    }

    /**
     * Priority Queue Container, encapsulating a PathfindingElement and a cost.
     */
    private class PriorityQueueElement implements Comparable<PriorityQueueElement> {
        public PathfindingElement element;
        public int cost;
        public PriorityQueueElement(PathfindingElement el, int cost) {
            this.element = el;
            this.cost = cost;
        }
        public int compareTo(PriorityQueueElement cmp) {
            return this.cost - cmp.cost;
        }
    }

    public Pathfinding() {
        this.path = null;
    }


    /**
     * Simple distance heuristic to guide search towards waypoint
     * @param current
     * @param next
     * @return
     */
    public int heuristic(PathfindingElement current, PathfindingElement next) {
        return (int) (current.grid.distance(next.grid) / 2);
    }


    /**
     * Determines cost going from current to next
     * @param cameFrom
     * @param current
     * @param next
     * @return
     */
    public int cost(Map<PathfindingElement, PathfindingElement> cameFrom, PathfindingElement current, PathfindingElement next) {
        if (next.grid.state == GridRectangle.States.OBSTACLE) {
            return Parameters.OBSTACLE_COST;
        }
        if (next.grid.state == GridRectangle.States.AVOID) {
            return Parameters.AVOID_COST;
        }

        PathfindingElement oldPathfindingElement = cameFrom.get(current);

        if (current.direction != next.direction && (oldPathfindingElement == null || (oldPathfindingElement != null && oldPathfindingElement.direction != next.direction))) {
            // if this was an illegal direction change, it'll cost crazy amounts
            Direction n[] = current.direction.getDirectionalNeighbours();
            if (next.direction != n[0] && next.direction != n[1] && next.direction != n[2]) {
                Log.d(TAG, "Illegal turn in cost detected");
                return Parameters.ILLEGAL_DIRECTION_COST;
            }
            //if any of the last n moves was a direction change, this will be very expensive
            PathfindingElement prev = current;
            int lookedAtElements = 0;
            do {
                prev = cameFrom.get(prev);
                if (prev == null || current.direction != prev.direction) {
                    return Parameters.REPEATED_DIRECTION_CHANGE_COST;
                }
                lookedAtElements++;
            } while (cameFrom.containsKey(prev) && lookedAtElements < Parameters.REPETITION_TOLERANCE);
            // otherwise, it'll cost just a little
            return Parameters.DIRECTION_CHANGE_COST;
        }
        return 0;
    }

    /**
     * Searches for a path from start to target. basic A* algorithm
     * see: http://www.gamasutra.com/view/feature/131505/toward_more_realistic_pathfinding.php?print=1
     * @param start start element to start from, may be restricted by its direction
     * @param target target element to be reached, may be restricted by its direction
     * @return a valid path from start to target
     * @throws PathfindingException if no path can be determined
     */
    public PathfindingResult search(int depth, Grid grid, PathfindingElement start, PathfindingElement target) throws PathfindingException {
        PriorityQueue<PriorityQueueElement> frontier = new PriorityQueue<>();
        Map<PathfindingElement, PathfindingElement> cameFrom = new HashMap<>();
        Map<PathfindingElement, Integer> costSoFar = new HashMap<>();

        frontier.add(new PriorityQueueElement(start, 0));
        cameFrom.put(start, null);
        costSoFar.put(start, 0);

        PriorityQueueElement previous;
        PriorityQueueElement current = null;

        int priority;
        int new_cost;
        int attempts = 0;

        while (!frontier.isEmpty()) {
            previous = current;
            current = frontier.poll();

            // handle never ending searches by going back one step and changing the targets
            // direction clockwise to the previous direction
            if (attempts > depth) {
                throw new PathfindingException("Could not find path after " + attempts + " attempts, please try changing the incoming direction.");
            }

            if (current.element.is_equal(target)) {
                Log.d(TAG, "target reached, breaking");
                break;
            }
//            Log.d(TAG, "Checking neighbours for " + current.element);

            for (PathfindingElement next : grid.getNeighbours(current.element.grid, current.element.direction)) {
                new_cost = costSoFar.get(current.element) + this.cost(cameFrom, current.element, next);
                if (!costSoFar.containsKey(next) || new_cost < costSoFar.get(next)) {
                    costSoFar.put(next, new_cost);
                    priority = new_cost + this.heuristic(current.element, next);
                    frontier.add(new PriorityQueueElement(next, priority));
                    cameFrom.put(next, current.element);
                }
            }
            attempts++;
        }

        if (current != null) {
            return new PathfindingResult(cameFrom, costSoFar, current.element, attempts);
        } else {
            throw new PathfindingException("Result element is null.");
        }

    }

    /**
     * Sort list of waypoints by minimal distance, starting with {@code start} element
     * @param waypoints list of waypoints
     * @param start starting rectangle
     * @return
     */
    private List<GridRectangle> sortWaypointsByDistance(List<GridRectangle> waypoints, GridRectangle start) {
        // created list of waypoints the next closest unused element following the previous element
        List<GridRectangle> sortedWaypoints = new ArrayList<>();
        List<GridRectangle> usedWaypoints = new ArrayList<>();
        sortedWaypoints.add(start);
        while (sortedWaypoints.size() < waypoints.size() + 1) {
            GridRectangle bestRect = null;
            for (GridRectangle rect: waypoints) {
                if (!usedWaypoints.contains(rect)) {
                    GridRectangle lastRect = sortedWaypoints.get(sortedWaypoints.size() - 1);
                    if (bestRect == null) {
                        bestRect = rect;
                    }
                    if (bestRect.distance(lastRect) > rect.distance(lastRect)) {
                        bestRect = rect;
                    }
                }
            }
            sortedWaypoints.add(bestRect);
            usedWaypoints.add(bestRect);
        }
        return sortedWaypoints;
    }


    /**
     * Recursively finds a path from {@code previousTarget} to the final target in {@code targets}
     * @param previousTarget previously reached target. if target is not in {@code targets}, first element in {@code targets} will be next, otherwise next element in {@code targets}
     * @param currentPath path up to and including previousTarget
     * @param targets list target waypoints
     * @param detector Detection element containing grid
     * @return List of PathfindingElements from {@code previousTarget} to final target
     * @throws PathfindingException if no path could be found from {@code previousTarget} to final target
     */
    private List<PathfindingElement> findPathRecursive(PathfindingElement previousTarget, List<PathfindingElement> currentPath, List<PathfindingElement> targets, Detection detector) throws PathfindingException{

        if (targets.get(targets.size() - 1).is_equal(previousTarget)) {
            return currentPath;
        }

        PathfindingElement nextTarget = targets.get(targets.indexOf(previousTarget) + 1);
        Direction[] directions = null;
        if (nextTarget.directions != null) {
            directions = nextTarget.directions;
        } else {
            directions = Direction.values();
        }
        for (Direction d : directions) {
            List<PathfindingElement> localPathCopy = new ArrayList<>(currentPath);
            try {
                // if we are not looking at the final element, set the direction
                // because we only want to reach the final element, we do not care about
                // the direction we reach it at
                if (!targets.get(targets.size() - 1).is_equal(previousTarget)) {
                    nextTarget.direction = d;
                } else {
                    nextTarget.direction = null;
                }
                PathfindingResult result = search(MAX_SEARCH_DEPTH, detector.getGrid(), previousTarget, nextTarget);
                PathfindingElement lastStep = result.target;

                int previousLength = localPathCopy.size();
                Log.d(TAG, "Detected path from pos " + targets.indexOf(previousTarget) + " (" + previousTarget + " to " + targets.indexOf(nextTarget) + " (" + nextTarget + " in " + result.attempts + " steps");
                while (lastStep != null) {
//                    Log.d(TAG, lastStep.toString());
                    localPathCopy.add(previousLength, lastStep);
                    lastStep = result.cameFrom.get(lastStep);
                }
                this.path = localPathCopy;
                return findPathRecursive(result.target, localPathCopy, targets, detector);
            } catch (PathfindingException e) {
                // if we could not find a path for the final element, we've gotta go back
                if (targets.get(targets.size() - 1).is_equal(previousTarget)) {
                    break;
                }
            }
        }
        throw new PathfindingException("Failed to find path for " + previousTarget);
    }

    /**
     * Performs pathfinding using a given detector.
     * @param detector containing grid and car
     */
    public void runPathfinding(Detection detector) throws PathfindingException {
        if (detector.getCar() != null && detector.getGrid() != null && detector.getWaypoints() != null && !detector.getWaypoints().isEmpty()) {

            List<GridRectangle> waypoints = new ArrayList<>();
            Map<GridRectangle, Direction[]> waypointDirections = new HashMap<>();
            this.path = new ArrayList<>();

            for (Waypoint waypoint : detector.getWaypoints()) {
                int index[] = detector.getGrid().getIndexFromPosition(waypoint.getCenterX(), waypoint.getCenterY());
                if (detector.getGrid().isRectangleInBounds(index[0], index[1])) {
                    GridRectangle gridElement = detector.getGrid().grid.get(index[0]).get(index[1]);
                    waypoints.add(gridElement);
                    waypointDirections.put(gridElement, waypoint.getDirections());
                }
            }

            // determine car position
            int[] carPosition = detector.getGrid().getIndexFromPosition(detector.getCar().getCenterX(), detector.getCar().getCenterY());
            GridRectangle carRect = null;
            if (detector.getGrid().isRectangleInBounds(carPosition[0], carPosition[1])) {
                carRect = detector.getGrid().grid.get(carPosition[0]).get(carPosition[1]);
            } else {
                Log.e(TAG, "Car out of grid bounds");
                throw new PathfindingException("Car out of grid bounds");
            }
            waypoints = sortWaypointsByDistance(waypoints, carRect);

            // car is the first waypoint element
            PathfindingElement car = new PathfindingElement(waypoints.remove(0), detector.getCarDirection(), null);
            List<PathfindingElement> targets = new ArrayList<>();

            for (GridRectangle rect: waypoints) {
                PathfindingElement target = new PathfindingElement(rect, null, waypointDirections.get(rect));
                targets.add(target);
            }

            try {
                this.path.addAll(findPathRecursive(car, new ArrayList<PathfindingElement>(), targets, detector));
            } catch (PathfindingException e) {
                this.path.clear();
                Log.d(TAG, "Could not find path");
            }
            Log.d(TAG, "Finished pathfinding");
        }
    }

}
