package com.example.lukaskrabbe.microcar.AutonomousDriving;

import android.support.annotation.Nullable;

import com.example.lukaskrabbe.microcar.Detection.GridRectangle;
import com.example.lukaskrabbe.microcar.Detection.Direction;

/**
 * Container class to store a GridRectangle with a direction.
 */
public class PathfindingElement {

    public GridRectangle grid;
    public Direction direction;
    public Direction[] directions;

    public PathfindingElement(GridRectangle slot, @Nullable Direction direction, @Nullable Direction[] directions) {
        this.grid = slot;
        this.direction = direction;
        this.directions = directions;
    }

    /**
     * Determins equality by comparing GridRectangle and direction. If one or both elements do not
     * have a direction, only GridRectangle determines equality.
     * @param pfe PathfindingElement to compare to
     * @return is equal element
     */
    boolean is_equal(PathfindingElement pfe) {

        if (this.direction != null && pfe.direction != null) {
            return this.grid.same_rect(pfe.grid) && this.direction == pfe.direction;
        } else {
            return this.grid.same_rect(pfe.grid);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PathfindingElement))
            return false;
        if (obj == this)
            return true;
        PathfindingElement pobj = (PathfindingElement) obj;
        return this.grid.same_rect(pobj.grid) && this.direction == pobj.direction;
    }

    @Override
    public String toString() {
        return this.grid.toString() + " in direction " + this.direction;
    }

}
