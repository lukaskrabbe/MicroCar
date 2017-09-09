package com.example.lukaskrabbe.microcar.Detection;

/**
 * This enum is used to associate a direction with a change in grid rows and columns.
 * Also, determining which directions are a valid neighbour is done here.
 */
public enum Direction {
    N(0, -1),
    E(1, 0),
    S(0, 1),
    W(-1, 0),
    NE(1, -1),
    SE(1, 1),
    SW(-1, 1),
    NW(-1, -1);

    private int x;
    private int y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int[] getValue() {
        int[] returnArray = new int[2];
        returnArray[0] = this.x;
        returnArray[1] = this.y;
        return returnArray;
    }

    public static Direction[] inOrder() {
        return new Direction[] {
                Direction.N,
                Direction.NE,
                Direction.E,
                Direction.SE,
                Direction.S,
                Direction.SW,
                Direction.W,
                Direction.NW
        };
    }

    public Direction next() {
        return this.getDirectionalNeighbours()[2];
    }

    public Direction[] getDirectionalNeighbours() {
        Direction[] neighbour_directions = new Direction[3];

        switch (this) {
            case N:
                neighbour_directions[0] = Direction.NW;
                neighbour_directions[1] = Direction.N;
                neighbour_directions[2] = Direction.NE;
                break;
            case NE:
                neighbour_directions[0] = Direction.N;
                neighbour_directions[1] = Direction.NE;
                neighbour_directions[2] = Direction.E;
                break;
            case E:
                neighbour_directions[0] = Direction.NE;
                neighbour_directions[1] = Direction.E;
                neighbour_directions[2] = Direction.SE;
                break;
            case SE:
                neighbour_directions[0] = Direction.E;
                neighbour_directions[1] = Direction.SE;
                neighbour_directions[2] = Direction.S;
                break;
            case S:
                neighbour_directions[0] = Direction.SE;
                neighbour_directions[1] = Direction.S;
                neighbour_directions[2] = Direction.SW;
                break;
            case SW:
                neighbour_directions[0] = Direction.S;
                neighbour_directions[1] = Direction.SW;
                neighbour_directions[2] = Direction.W;
                break;
            case W:
                neighbour_directions[0] = Direction.SW;
                neighbour_directions[1] = Direction.W;
                neighbour_directions[2] = Direction.NW;
                break;
            case NW:
                neighbour_directions[0] = Direction.W;
                neighbour_directions[1] = Direction.NW;
                neighbour_directions[2] = Direction.N;
                break;
        }
        return neighbour_directions;
    }
}
