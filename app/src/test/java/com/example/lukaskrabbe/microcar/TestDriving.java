package com.example.lukaskrabbe.microcar;

import com.example.lukaskrabbe.microcar.AutonomousDriving.Driving;
import com.example.lukaskrabbe.microcar.AutonomousDriving.Pathfinding;
import com.example.lukaskrabbe.microcar.Detection.Detection;
import com.example.lukaskrabbe.microcar.Detection.Direction;
import com.example.lukaskrabbe.microcar.Detection.RectangleInterface;
import com.example.lukaskrabbe.microcar.Microcar.Car;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TestDriving {

    Detection detector;
    Car car;
    Pathfinding path;
    Driving driving;

    @Before
    public void setUp() {
        detector = mock(Detection.class);
        car = mock(Car.class);
        path = mock(Pathfinding.class);
        driving = new Driving(car, detector, path);
    }

    @Test
    public void test_CarAboveLaneEast() {

        RectangleInterface lane = spy(new MockRectangle(100, 100, 20, 400, -90));
        RectangleInterface carRect = new MockRectangle(100, 80, 30, 90, -90);

        // because we have no working intersection, but we do know that we don't want to intersect here
        when(lane.isIntersecting(carRect)).thenReturn(false);

        Driving.CommandRectangle laneRect = new Driving.CommandRectangle(lane, lane, Direction.E);

        when(detector.getCar()).thenReturn(carRect);

        assertEquals(-70.0, driving.getDeviation(carRect, laneRect));

    }

    @Test
    public void test_CarAboveLaneWest() {

        RectangleInterface lane = spy(new MockRectangle(100, 100, 20, 400, -90));
        RectangleInterface carRect = new MockRectangle(100, 80, 30, 90, -90);

        // because we have no working intersection, but we do know that we don't want to intersect here
        when(lane.isIntersecting(carRect)).thenReturn(false);

        Driving.CommandRectangle laneRect = new Driving.CommandRectangle(lane, lane, Direction.W);

        when(detector.getCar()).thenReturn(carRect);

        assertEquals(70.0, driving.getDeviation(carRect, laneRect));

    }

    @Test
    public void test_CarAboveLaneNorthWest() {

        RectangleInterface lane = spy(new MockRectangle(100, 100, 20, 400, -45));
        RectangleInterface carRect = new MockRectangle(120, 80, 30, 90, -90);

        // because we have no working intersection, but we do know that we don't want to intersect here
        when(lane.isIntersecting(carRect)).thenReturn(false);

        Driving.CommandRectangle laneRect = new Driving.CommandRectangle(lane, lane, Direction.W);

        when(detector.getCar()).thenReturn(carRect);

        assertEquals(70.0, driving.getDeviation(carRect, laneRect));

    }

    @Test
    public void test_CarBelowLaneNorthWest() {

        RectangleInterface lane = spy(new MockRectangle(100, 100, 20, 400, -45));
        RectangleInterface carRect = new MockRectangle(120, 150, 30, 90, -90);

        // because we have no working intersection, but we do know that we don't want to intersect here
        when(lane.isIntersecting(carRect)).thenReturn(false);

        Driving.CommandRectangle laneRect = new Driving.CommandRectangle(lane, lane, Direction.W);

        when(detector.getCar()).thenReturn(carRect);

        assertEquals(-70.0, driving.getDeviation(carRect, laneRect));

    }


}
