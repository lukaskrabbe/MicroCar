package com.example.lukaskrabbe.microcar;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.lukaskrabbe.microcar.AutonomousDriving.Driving;
import com.example.lukaskrabbe.microcar.AutonomousDriving.Pathfinding;
import com.example.lukaskrabbe.microcar.AutonomousDriving.PathfindingElement;
import com.example.lukaskrabbe.microcar.AutonomousDriving.PathfindingException;
import com.example.lukaskrabbe.microcar.Detection.Detection;
import com.example.lukaskrabbe.microcar.Detection.Grid;
import com.example.lukaskrabbe.microcar.Detection.GridRectangle;
import com.example.lukaskrabbe.microcar.Detection.Rectangle;
import com.example.lukaskrabbe.microcar.Detection.RectangleInterface;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AutonomousDrivingFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "AutonomousDriving";

    private CameraBridgeViewBase mOpenCvCameraView;

    private boolean calibrateOnFrame;

    private boolean isWorking;

    private Mat mRgba;
    private Mat canvas;

    private final static Scalar blank = new Scalar(0, 0, 0, 0);

    private Detection detector;
    private Pathfinding pathfinder; // Ryder
    private Driving driver;

    public AutonomousDrivingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runDetection();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) getView().findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this.getActivity()) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

    };

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this.getActivity(), mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        canvas = new Mat(height, width, CvType.CV_8UC4);
        detector = new Detection(height, width);
        pathfinder = new Pathfinding();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        canvas.release();
        detector = null;
        pathfinder = null;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        // because we do not want to draw on the image we receive, as this is then the image used
        // for detection, we copy the exact image into this canvas element and draw on that
        // preserving the original image for any detection which might occur
        mRgba.copyTo(canvas);

        if (detector.getGrid() != null) {
            for (Rectangle rect : detector.getWaypoints()) {
                tempDrawRectangle(canvas, rect, new Scalar(0, 255, 255), 4);
            }
        }

        // so, turns out, drawing things is really really expensive, like cutting framerate in half
        // expensive. therefore, we won't draw when we're driving, as we cannot affort to mess up
        // our detection because of weird timings due to drawing
        if (driver == null && detector.getGrid() != null) {
            drawGrid(canvas, detector.getGrid());
        }

        if (driver == null && pathfinder.getPath() != null) {
            drawPath(canvas, pathfinder.getPath());
        }

        if (detector.getCar() != null) {
            drawCar(canvas, detector.getCar(), detector.getCarDetectionTime());
        }

        if (driver != null) {
            if (driver.currentLane != null) {
                RectangleInterface rect = driver.currentLane.lane;
                RectangleInterface trigger = driver.currentLane.trigger;
                tempDrawRectangle(canvas, rect, new Scalar(0, 255, 0), 2);
                tempDrawRectangle(canvas, trigger, new Scalar(255, 255, 255), 2);
            }
            if (driver.nextLanes != null) {
                List<Driving.CommandRectangle> lanes = new ArrayList<>(driver.nextLanes);
                for (Driving.CommandRectangle crect : lanes) {
                    tempDrawRectangle(canvas, crect.lane, new Scalar(100, 100, 0), 2);
                    tempDrawRectangle(canvas, crect.trigger, new Scalar(200, 100, 0), 2);
                }
            }
        }
        return canvas;
    }

    /**
     * Searches for a path using current Detection state
     */
    public void runPathfinding() {
        new Thread(new Runnable() {
            public void run() {
                setWorking(true);
                Log.d(TAG, "Disabling buttons and running pathfinding");
                try {
                    pathfinder.runPathfinding(detector);
                } catch (PathfindingException e) {
                    Log.e(TAG, "Pathfinding failed");
                }
                Log.d(TAG, "Enabling buttons, pathfinding finished");
                setWorking(false);
            }
        }).start();
    }

    /**
     * Runs grid and car detection asynchronously
     */
    public void runDetection() {
        new Thread(new Runnable() {
            public void run() {
                long prev = System.currentTimeMillis();
                long now;
                while (true) {
                    now = System.currentTimeMillis();
                    // because we are running periodically in the background, we need an external
                    // variable to tell us about detecting the grid, so calibrateOnFrame was born
                    if (detector != null && getCalibrateOnFrame()) {
                        setCalibrateOnFrame(false);
                        setWorking(true);
                        Log.d(TAG, "Disabling buttons and running grid detection");
                        detector.detectObstacles(mRgba);
                        Log.d(TAG, "Enabling buttons, grid detection finished");
                        setWorking(false);
                    } else {
                        // sleep for some time after running one cycle, frames are way slower anyway
                        if (prev - now < 10 || detector == null || (detector.getGrid() == null && detector.getGrid() == null)) {
                            try {
                                Thread.sleep(10); // sleep a little
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        // when not going for the grid, we go for the car, but only if there's
                        // a grid
                        if (detector != null && detector.getGrid() != null) {
                            detector.detectCar(mRgba);
                        }
                    }
                    prev = now;
                }
            }
        }).start();
    }

    /**
     * Runs car direction detection by driving forwards and backwards
     */
    public void runCarDirectionDetection() {
        new Thread(new Runnable() {
            public void run() {
                setWorking(true);
                Log.i(TAG, "Disabling buttons and running car direction detection");
                detector.detectInitialCarDirection();
                Log.d(TAG, "Enabling buttons, direction detection finished");
                setWorking(false);
            }
        }).start();
    }

    /**
     * Starts the periodic car detection and then drives the car on the currently detected path
     */
    public void runDrive() {
        new Thread(new Runnable() {
            public void run() {
                setWorking(true);
                Log.i(TAG, "Starting periodic car detection");
                detector.startPeriodicCarDetection();
                Log.i(TAG, "Periodic car detection finished");
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                setWorking(true);
                Log.i(TAG, "Disabling buttons and driving car around, good luck mate...");
                driver = new Driving(MainActivity.car, detector, pathfinder);
                driver.drive();
                driver = null;
                // we finished driving, whatever that means, so stop checking in on the car
                detector.stopPeriodicCarDetection();
                setWorking(false);
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_autonomous_driving, container, false);


        final Button detection = (Button) view.findViewById(R.id.detection);
        detection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Detecting obstacles");
                setCalibrateOnFrame(true);
                ((ProgressBar) view.findViewById(R.id.progressBar)).setProgress(25);
            }
        });
        final Button direction = (Button) view.findViewById(R.id.carDirection);
        direction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Detecting car Direction");
                runCarDirectionDetection();
                ((ProgressBar) view.findViewById(R.id.progressBar)).setProgress(50);
            }
        });
        final Button pathfind = (Button) view.findViewById(R.id.pathfind);
        pathfind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                runPathfinding();
                ((ProgressBar) view.findViewById(R.id.progressBar)).setProgress(75);
            }
        });
        final Button drive = (Button) view.findViewById(R.id.drive);
        drive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((ProgressBar) view.findViewById(R.id.progressBar)).setProgress(85);
                runDrive();
                ((ProgressBar) view.findViewById(R.id.progressBar)).setProgress(100);
            }
        });

        direction.setEnabled(false);
        pathfind.setEnabled(false);
        drive.setEnabled(false);

        return view;
    }

    private synchronized void setCalibrateOnFrame(boolean val) {
        calibrateOnFrame = val;
    }

    private synchronized boolean getCalibrateOnFrame() {
        return calibrateOnFrame;
    }

    public synchronized boolean isWorking() {
        return isWorking;
    }

    public synchronized void setWorking(boolean working) {
        View v = getView();
        if (v != null) {
            if (working) {
                getView().post(new Runnable() {
                    public void run() {
                        ((Button) getView().findViewById(R.id.detection)).setEnabled(false);
                        ((Button) getView().findViewById(R.id.carDirection)).setEnabled(false);
                        ((Button) getView().findViewById(R.id.pathfind)).setEnabled(false);
                        ((Button) getView().findViewById(R.id.drive)).setEnabled(false);
                    }
                });
            } else {
                getView().post(new Runnable() {
                    public void run() {
                        ((Button) getView().findViewById(R.id.detection)).setEnabled(true);
                        if (MainActivity.car != null && detector.getGrid() != null) {
                            ((Button) getView().findViewById(R.id.carDirection)).setEnabled(true);
                        }
                        if (detector.getGrid() != null && detector.getCar() != null) {
                            ((Button) getView().findViewById(R.id.pathfind)).setEnabled(true);
                        }
                        if (MainActivity.car != null && detector.getGrid() != null && detector.getCar() != null && pathfinder.getPath() != null) {
                            ((Button) getView().findViewById(R.id.drive)).setEnabled(true);
                        }
                    }
                });
            }
        }
        isWorking = working;
    }


    /**
     * Draws every element in a grid on the passed Mat. does not however draw empty slots.
     * @param frame used as a canvas, drawn upon
     * @param grid the grid
     * @return reference to the passed frame, drawn upon, not really necessary though
     */
    private Mat drawGrid(Mat frame, Grid grid) {
        for (List<GridRectangle> column: grid.grid) {
            for (GridRectangle slot: column) {
                if (slot.state == GridRectangle.States.FREE) {
//                    tempDrawRectangle(frame, slot, new Scalar(255, 255, 255), 1);
                } else if (slot.state == GridRectangle.States.OBSTACLE) {
                    tempDrawRectangle(frame, slot, new Scalar(255, 0, 0), 3);
                    tempDrawCross(frame, slot, new Scalar(255, 0, 0), 3);
                } else if (slot.state == GridRectangle.States.CONE) {
                    tempDrawRectangle(frame, slot, new Scalar(255, 165, 0), 3);
                    tempDrawCross(frame, slot, new Scalar(255, 165, 0), 3);
                } else if (slot.state == GridRectangle.States.WAYPOINT) {
                    tempDrawRectangle(frame, slot, new Scalar(0, 255, 255), 3);
                    tempDrawCross(frame, slot, new Scalar(0, 255, 255), 3);
                } else if (slot.state == GridRectangle.States.AVOID) {
                    tempDrawRectangle(frame, slot, new Scalar(100, 100, 255), 2);
                }
            }
        }
        return frame;
    }

    /**
     * Draws a rectangle around the car on the passed Mat
     * @param frame used as a canvas, drawn upon
     * @param car the car
     * @return reference to the passed frame, drawn upon, not really necessary though
     */
    private Mat drawCar(Mat frame, RectangleInterface car, long detectionTime) {
        if (System.currentTimeMillis() - detectionTime > 200) {
            tempDrawRectangle(frame, car, new Scalar(200, 200, 200), 4);
        } else {
            tempDrawRectangle(frame, car, new Scalar(255, 0, 0), 4);
        }
        return frame;
    }

    /**
     * Draws every element in the path on the passed Mat
     * @param frame used as a canvas, drawn upon
     * @param path the path
     * @return reference to the passed frame, drawn upon, not really necessary though
     */
    private Mat drawPath(Mat frame, List<PathfindingElement> path) {
        for (PathfindingElement pfe : new ArrayList<>(path)) {
            tempDrawCross(frame, pfe.grid, new Scalar(0, 0, 200), 2);
        }
        return frame;
    }

    /**
     * Drawing helper, because OpenCV Java does not have a draw rectangle function
     * @param mRgba canvas
     * @param rect to be drawn element
     * @param color element color
     * @param borderWidth value >= 0
     */
    public static void tempDrawRectangle(Mat mRgba, RectangleInterface rect, Scalar color, int borderWidth) {

        Point[] points = new Point[4];
        // since this can't ever be called with anything not a Rectangle, we can cast it into one
        Rectangle castedRect = (Rectangle) rect;
        castedRect.points(points);
        for (int i = 0; i < 4; i++) {
            Imgproc.line(mRgba, points[i], points[(i + 1) % 4], color, borderWidth);
        }
    }

    /**
     * Drawing helper, draws a cross through a rectangle instead of a frame around it
     * @param mRgba canvas
     * @param rect to be drawn element
     * @param color cross color
     * @param borderWidth value >= 0
     */
    public static void tempDrawCross(Mat mRgba, RectangleInterface rect, Scalar color, int borderWidth) {
        Point[] points = new Point[4];
        // since this can't ever be called with anything not a Rectangle, we can cast it into one
        Rectangle castedRect = (Rectangle) rect;
        castedRect.points(points);
        Imgproc.line(mRgba, points[0], points[2], color, borderWidth);
        Imgproc.line(mRgba, points[1], points[3], color, borderWidth);
    }

}
