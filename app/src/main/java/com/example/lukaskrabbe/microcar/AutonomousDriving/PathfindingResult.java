package com.example.lukaskrabbe.microcar.AutonomousDriving;

import java.util.Map;

/**
 * Container class for returning the result of the pathfinding operation
 */
class PathfindingResult {

    Map<PathfindingElement, PathfindingElement> cameFrom;
    Map<PathfindingElement, Integer> cost;
    PathfindingElement target;
    int attempts;

    PathfindingResult(Map<PathfindingElement, PathfindingElement> cameFrom, Map<PathfindingElement, Integer> costSoFar, PathfindingElement finish, int attempts) {
        this.cameFrom = cameFrom;
        this.cost = costSoFar;
        this.target = finish;
        this.attempts = attempts;
    }

}
