package com.example.lukaskrabbe.microcar.Detection;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.lukaskrabbe.microcar.AutonomousDriving.PathfindingElement;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    private static final String TAG = "Grid";

    private int x;
    private int y;
    private int width;
    private int height;

    public int getGridSize() {
        return gridSize;
    }

    private int gridSize;
    private int spacing;
    private int rows;
    private int columns;
    private  int offset;

    /**
     * List of List of Grid elements, whereby the inner list is rows, the outer list is columns.
     * Therefore elements are accessible using x,y pairs.
     */
    public List<List<GridRectangle>> grid;

    /**
     * Initializes a grid for use on an image surface
     * @param topLeftX top left coordinate for the grid to start at
     * @param topLeftY top left coordnate for the grod to start at
     * @param width
     * @param height
     * @param gridSize size of each element in the grid
     * @param spacing number of extra grid elements to add on each side, just to be safe
     */
    public Grid(int topLeftX, int topLeftY, int width, int height, int gridSize, int spacing) {
        this.x = topLeftX;
        this.y = topLeftY;
        this.width = width;
        this.height = height;
        this.gridSize = gridSize;
        this.spacing = spacing;

        this.columns = this.width / this.gridSize + this.spacing * 2;
        this.rows = this.height / this.gridSize + this.spacing * 2;
        this.offset = spacing * gridSize;

        this.grid = new ArrayList<>();
        for (int col = 0; col < this.columns; col++) {
            this.grid.add(col, new ArrayList<GridRectangle>());
            for (int row = 0; row < this.rows; row++) {
                double center_x = this.x - this.offset + col * gridSize + gridSize / 2;
                double center_y = this.y - this.offset + row * gridSize + gridSize / 2;
                GridRectangle rect = new GridRectangle(center_x, center_y, gridSize, col, row);
                this.grid.get(col).add(row, rect);
            }
        }
    }

    private List<GridRectangle> getGridNeighbours(GridRectangle gr, int step) {
        List<GridRectangle> returnList = new ArrayList<>();

        if (step < 1) {
            return returnList;
        }
        List<Integer> innerList = new ArrayList<>();
        List<Integer> outerList = new ArrayList<>();
        outerList.add(step);
        outerList.add((-1) * step);
        innerList.add(0);
        for (int i = 1; i < step; i++) {
            innerList.add(i);
            innerList.add((-1) * i);
        }

        for (int i = 0; i < 2; i++) {
            if (isRectangleInBounds(gr.column + outerList.get(i), gr.row + outerList.get(i))) {
                returnList.add(this.grid.get(gr.column + outerList.get(i)).get(gr.row + outerList.get(i)));
            }
            if (isRectangleInBounds(gr.column + outerList.get(i), gr.row - outerList.get(i))) {
                returnList.add(this.grid.get(gr.column + outerList.get(i)).get(gr.row - outerList.get(i)));
            }
            for (int k = 0; k < innerList.size(); k++) {
                if (isRectangleInBounds(gr.column + outerList.get(i), gr.row + innerList.get(k))) {
                    returnList.add(this.grid.get(gr.column + outerList.get(i)).get(gr.row + innerList.get(k)));
                }
                if (isRectangleInBounds(gr.column + innerList.get(k), gr.row + outerList.get(i))) {
                    returnList.add(this.grid.get(gr.column + innerList.get(k)).get(gr.row + outerList.get(i)));
                }
            }
        }
        return returnList;
    }

    /**
     * Adds an obstacle with type obstacleType to the grid by marking every slot in the grid the obstacle intersects as obstacleType
     * @param obstacle
     * @param obstacleType
     */
    public void addObstacle(Rectangle obstacle, GridRectangle.States obstacleType) {
        Log.d(TAG, "Adding obstacle " + obstacleType);

        List<GridRectangle> testedRectangles = new ArrayList<>();
        List<GridRectangle> toTestRectangles = new ArrayList<>();
        List<GridRectangle> intersectingRectangles = new ArrayList<>();

        int[] approx = this.getIndexFromPosition(obstacle.getCenterX(), obstacle.getCenterY());
        int approxColumn = approx[0];
        int approxRow = approx[1];
        Log.d(TAG, "Approximate position - x: " + approxColumn + " y: " + approxRow);

        GridRectangle start = this.grid.get(approxColumn).get(approxRow);
        start.set_state(obstacleType);
        toTestRectangles.add(start);

        int depth = 0;
        while (!toTestRectangles.isEmpty() && depth < Parameters.MAX_OBSTACLE_DEPTH) {
            depth++;
            GridRectangle rect = toTestRectangles.remove(0);
            testedRectangles.add(rect);
            if (rect.isIntersecting(obstacle)) {
                rect.set_state(obstacleType);
                intersectingRectangles.add(rect);
                List<PathfindingElement> neighbours = getNeighbours(rect, null);
                for (PathfindingElement pf: neighbours) {
                    if (!testedRectangles.contains(pf.grid) && !toTestRectangles.contains(pf.grid)) {
                        toTestRectangles.add(pf.grid);
                    }
                }
            } else if (testedRectangles.isEmpty() && toTestRectangles.isEmpty()) {
                Log.e(TAG, "Adding obstacle failed, invalid start");
            }
        }

        if (obstacleType == GridRectangle.States.OBSTACLE || obstacleType == GridRectangle.States.CONE) {
            for (GridRectangle rect : intersectingRectangles) {
                List<GridRectangle> neighbours = getGridNeighbours(rect, 1);
                if (obstacleType == GridRectangle.States.OBSTACLE) {
                    neighbours.addAll(getGridNeighbours(rect, 2));
                }
                for (GridRectangle neighbour : neighbours) {
                    if (neighbour.state == GridRectangle.States.FREE) {
                        neighbour.set_state(GridRectangle.States.AVOID);
                    }
                }
            }
        }
    }

    /**
     * Approximates the grid row and column for a given image coordinate.
     * @param x image coordinate
     * @param y image coordinate
     * @return Array with [row, column]
     */
    public int[] getIndexFromPosition(double x, double y) {
        int[] returnArray = new int[2];

        int column = (int) (x - (this.x - this.offset)) / this.gridSize;
        int row = (int) (y - (this.y - this.offset)) / this.gridSize;

        returnArray[0] = column;
        returnArray[1] = row;

        return returnArray;
    }

    /**
     * Determines whether a given grid-position is in the grid.
     * @param column
     * @param row
     * @return
     */
    public boolean isRectangleInBounds(int column, int row) {
        return 0 <= column && column < this.columns && 0 <= row && row < this.rows;
    }

    /**
     * Returns all valid neighbours when no direction is given, otherwise only valid neighbours which are deemed reachable given the direction
     * @param rectangle current rectangle
     * @param direction current direction, can be null
     * @return List of neighbours
     */
    public List<PathfindingElement> getNeighbours(GridRectangle rectangle, @Nullable Direction direction) {
        List<PathfindingElement> neighbours = new ArrayList<>();
        Direction[] neighbour_directions;
        int column = rectangle.column;
        int row = rectangle.row;

        if (direction != null) {
            neighbour_directions = direction.getDirectionalNeighbours();
        } else {
            neighbour_directions = new Direction[8];
            neighbour_directions[0] = Direction.N;
            neighbour_directions[1] = Direction.NE;
            neighbour_directions[2] = Direction.E;
            neighbour_directions[3] = Direction.SE;
            neighbour_directions[4] = Direction.S;
            neighbour_directions[5] = Direction.SW;
            neighbour_directions[6] = Direction.W;
            neighbour_directions[7] = Direction.NW;
        }

        for (Direction d : neighbour_directions) {

            int[] offsets = d.getValue();
            int new_column = column + offsets[0];
            int new_row = row + offsets[1];
            if (this.isRectangleInBounds(new_column, new_row)) {
                GridRectangle slot = this.grid.get(new_column).get(new_row);
                if (slot.passable()) {
                    neighbours.add(new PathfindingElement(slot, d, null));
                }
            }

        }

        return neighbours;
    }

}
