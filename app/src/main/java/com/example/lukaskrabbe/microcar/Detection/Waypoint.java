package com.example.lukaskrabbe.microcar.Detection;

/**
 * Waypoint is a GridRectangle with additional Direction information.
 */
public class Waypoint extends Rectangle{
    private Direction[] directions;

    /**
     * Creates and initializes a new Waypoint.
     * @param center_x
     * @param center_y
     * @param directions
     */
    public Waypoint(double center_x, double center_y, Direction[] directions) {
        super(center_x, center_y, 1, 1, 0);
        this.directions = directions;
    }

    /**
     * Returns the directions of the Waypoint.
     * @return Directions of the Waypoint.
     */
    public Direction[] getDirections() {
        return this.directions;
    }
}
