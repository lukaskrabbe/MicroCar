package com.example.lukaskrabbe.microcar;

import com.example.lukaskrabbe.microcar.Detection.Direction;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestDirection {
    @Test
    public void test_all_directions() throws Exception {
        assertArrayEquals(Direction.N.getDirectionalNeighbours(), new Direction[]{Direction.NW, Direction.N, Direction.NE});
        assertArrayEquals(Direction.NE.getDirectionalNeighbours(), new Direction[]{Direction.N, Direction.NE, Direction.E});
        assertArrayEquals(Direction.E.getDirectionalNeighbours(), new Direction[]{Direction.NE, Direction.E, Direction.SE});
        assertArrayEquals(Direction.SE.getDirectionalNeighbours(), new Direction[]{Direction.E, Direction.SE, Direction.S});
        assertArrayEquals(Direction.S.getDirectionalNeighbours(), new Direction[]{Direction.SE, Direction.S, Direction.SW});
        assertArrayEquals(Direction.SW.getDirectionalNeighbours(), new Direction[]{Direction.S, Direction.SW, Direction.W});
        assertArrayEquals(Direction.W.getDirectionalNeighbours(), new Direction[]{Direction.SW, Direction.W, Direction.NW});
        assertArrayEquals(Direction.NW.getDirectionalNeighbours(), new Direction[]{Direction.W, Direction.NW, Direction.N});
    }
}