package com.example.lukaskrabbe.microcar;

import android.support.test.runner.AndroidJUnit4;

import com.example.lukaskrabbe.microcar.AutonomousDriving.Driving;
import com.example.lukaskrabbe.microcar.AutonomousDriving.Pathfinding;
import com.example.lukaskrabbe.microcar.Detection.Detection;
import com.example.lukaskrabbe.microcar.Detection.Direction;
import com.example.lukaskrabbe.microcar.Detection.Rectangle;
import com.example.lukaskrabbe.microcar.Microcar.Car;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestDrivingInstrumentation {

    Detection detector;
    Car car;
    Pathfinding path;
    Driving driving;

    @Before
    public void setUp() throws Exception {
        if (!OpenCVLoader.initDebug()) {
            // There is no internal OpenCV libs

            throw new Exception("Internal OpenCV library not found");
        } else {
            System.out.println("OpenCV library found inside test package. Using it!");
        }
        detector = mock(Detection.class);
        car = mock(Car.class);
        path = mock(Pathfinding.class);
        driving = new Driving(car, detector, path);
    }

    @Test
    public void test_CarAboveLaneEast() {

        Rectangle lane = new Rectangle(100, 100, 20, 400, -90);
        Rectangle carRect = new Rectangle(100, 80, 30, 90, -90);

        Driving.CommandRectangle laneRect = new Driving.CommandRectangle(lane, lane, Direction.E);

        when(detector.getCar()).thenReturn(carRect);

        assertEquals(-70.0, driving.getDeviation(carRect, laneRect));

    }

}
