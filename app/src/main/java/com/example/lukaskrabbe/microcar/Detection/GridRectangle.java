package com.example.lukaskrabbe.microcar.Detection;

public class GridRectangle extends Rectangle {

    /**
     * States which a GridRectangle can be in, defaults to FREE on creation
     */
    public enum States {
        FREE, CONE, OBSTACLE, AVOID, WAYPOINT
    }

    public States state;
    public int column;
    public int row;

    public GridRectangle(double center_x, double center_y, double size, int column, int row) {
        super(center_x, center_y, size, size, 0);
        this.column = column;
        this.row = row;
        this.state = States.FREE;
    }

    /**
     * Determines if the grid element can be driven through
     * @return
     */
    public boolean passable() {
        return this.state == States.FREE || this.state == States.WAYPOINT;
    }

    public void set_state(States new_state) {
        this.state = new_state;
    }

    /**
     * Determines equality of GridRectangles by comparing their row and column
     * @param other_rect
     * @return
     */
    public boolean same_rect(GridRectangle other_rect) {
        return this.column == other_rect.column && this.row == other_rect.row;
    }

    @Override
    public String toString() {
        return "Coordinates: " + this.column + "," + this.row + " Position: " + this.center + " - Size: " + this.size + " - Angle : " + this.angle;
    }


}
