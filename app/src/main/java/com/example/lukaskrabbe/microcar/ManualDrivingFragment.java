package com.example.lukaskrabbe.microcar;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.example.lukaskrabbe.microcar.Microcar.BlinkState;
import com.example.lukaskrabbe.microcar.Microcar.CarCodes;
import com.example.lukaskrabbe.microcar.Microcar.LightState;

import static android.content.ContentValues.TAG;
import static com.example.lukaskrabbe.microcar.MainActivity.car;
import static com.example.lukaskrabbe.microcar.MainActivity.microcarService;


/**
 * A simple {@link Fragment} subclass.
 */
public class ManualDrivingFragment extends Fragment {

    private static final int HORNDELAY = 1000;


    public ManualDrivingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manual_driving, container, false);

        final SeekBar speedSlider = (SeekBar) view.findViewById(R.id.speed);
        speedSlider.setMax(CarCodes.SPEED_FRONT.length + CarCodes.SPEED_BACK.length + 1);
        speedSlider.setProgress(CarCodes.SPEED_FRONT.length + 1);
        speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                       if (microcarService != null && microcarService.getState() == MicrocarService.STATE_CONNECTED) {
                                                          car.Drive(progress - CarCodes.SPEED_FRONT.length);
                                                       }
                                                   }
                                                   public void onStartTrackingTouch(SeekBar seekBar) {

                                                   }
                                                   public void onStopTrackingTouch(SeekBar seekBar) {
                                                       seekBar.setProgress(CarCodes.SPEED_BACK.length + 1);
                                                   }

                                               }
        );

        final SeekBar steeringSlider = (SeekBar) view.findViewById(R.id.steer);
        steeringSlider.setMax(CarCodes.STEER_LEFT.length + CarCodes.STEER_RIGHT.length + 1);
        steeringSlider.setProgress(CarCodes.STEER_LEFT.length + 1);
        steeringSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {



                                                          if (microcarService != null && microcarService.getState() == MicrocarService.STATE_CONNECTED) {


                                                              car.Steer(progress - CarCodes.STEER_RIGHT.length);
                                                          }



                                                      }
                                                      public void onStartTrackingTouch(SeekBar seekBar) {

                                                      }
                                                      public void onStopTrackingTouch(SeekBar seekBar) {
                                                          seekBar.setProgress(CarCodes.STEER_LEFT.length + 1);
                                                      }

                                                  }
        );


        final ToggleButton lightsOn = (ToggleButton) view.findViewById(R.id.light);
        lightsOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (microcarService != null && microcarService.getState() == MicrocarService.STATE_CONNECTED) {
                    Log.d(TAG, "Turning on lights");
                    if(lightsOn.isChecked()){
                        car.Light(LightState.HIGH);
                    }
                    if(!lightsOn.isChecked()){
                        car.Light(LightState.OFF);
                    }

                }
            }
        });


        final ToggleButton blink_left = (ToggleButton) view.findViewById(R.id.blink_left);
        blink_left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (microcarService != null && microcarService.getState() == MicrocarService.STATE_CONNECTED) {
                    Log.d(TAG, "blink left");
                    if(lightsOn.isChecked()){
                        car.Blink(BlinkState.BLINK_LEFT);
                    }
                    if(!lightsOn.isChecked()){
                        car.Blink(BlinkState.OFF);
                    }

                }
            }
        });
        final ToggleButton blink_right = (ToggleButton) view.findViewById(R.id.blink_right);
        blink_right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (microcarService != null && microcarService.getState() == MicrocarService.STATE_CONNECTED) {
                    Log.d(TAG, "blink left");
                    if(lightsOn.isChecked()){
                       car.Blink(BlinkState.BLINK_RIGHT);
                    }
                    if(!lightsOn.isChecked()){
                        car.Blink(BlinkState.OFF);
                    }

                }
            }
        });

        Button horn = (Button) view.findViewById(R.id.horn);
        horn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                car.Horn(true);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                       car.Horn(false);
                    }
                }, HORNDELAY);
            }
        });


        return view;
    }

}
