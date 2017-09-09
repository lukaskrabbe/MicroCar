package com.example.lukaskrabbe.microcar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.lukaskrabbe.microcar.Detection.Parameters;

public class ColorSettings extends Fragment {
    public static final int MAX_COLOR = 255;

    public ColorSettings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_settings, container, false);

        final SeekBar obstacleLowerH = (SeekBar) view.findViewById(R.id.seekBarlowX);
        obstacleLowerH.setMax(Parameters.MAX_H);
        obstacleLowerH.setProgress((int) Parameters.lowerBoundsObstacles.val[0]);
        obstacleLowerH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                                                      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                          double[] values = Parameters.lowerBoundsObstacles.val;
                                                          values[0] = progress;
                                                          Parameters.lowerBoundsObstacles.set(values);

                                                      }
                                                      public void onStartTrackingTouch(SeekBar seekBar) {

                                                      }
                                                      public void onStopTrackingTouch(SeekBar seekBar) {
                                                          Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                                      }

                                                  }
        );
        final SeekBar obstacleLowerS = (SeekBar) view.findViewById(R.id.seekBarlowY);
        obstacleLowerS.setMax(Parameters.MAX_S);
        obstacleLowerS.setProgress((int) Parameters.lowerBoundsObstacles.val[1]);

        obstacleLowerS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                double[] values = Parameters.lowerBoundsObstacles.val;
                                                values[1] = progress;
                                                Parameters.lowerBoundsObstacles.set(values);

                                            }
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }
                                            public void onStopTrackingTouch(SeekBar seekBar) {
                                                Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
        );
        final SeekBar obstacleLowerV = (SeekBar) view.findViewById(R.id.seekBarlowZ);
        obstacleLowerV.setMax(Parameters.MAX_V);
        obstacleLowerV.setProgress((int) Parameters.lowerBoundsObstacles.val[2]);
        obstacleLowerV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                double[] values = Parameters.lowerBoundsObstacles.val;
                                                values[2] = progress;
                                                Parameters.lowerBoundsObstacles.set(values);

                                            }
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }
                                            public void onStopTrackingTouch(SeekBar seekBar) {
                                                Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
        );
        final SeekBar obstacleUpperH = (SeekBar) view.findViewById(R.id.seekBarupX);
        obstacleUpperH.setMax(Parameters.MAX_H);
        obstacleUpperH.setProgress((int) Parameters.upperBoundsObstacles.val[0]);
        obstacleUpperH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                double[] values = Parameters.upperBoundsObstacles.val;
                                                values[0] = progress;
                                                Parameters.upperBoundsObstacles.set(values);

                                            }
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }
                                            public void onStopTrackingTouch(SeekBar seekBar) {
                                                Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
        );
        final SeekBar obstacleUpperS = (SeekBar) view.findViewById(R.id.seekBarupY);
        obstacleUpperS.setMax(Parameters.MAX_S);
        obstacleUpperS.setProgress((int) Parameters.upperBoundsObstacles.val[1]);
        obstacleUpperS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                               double[] values = Parameters.upperBoundsObstacles.val;
                                               values[1] = progress;
                                               Parameters.upperBoundsObstacles.set(values);

                                           }
                                           public void onStartTrackingTouch(SeekBar seekBar) {

                                           }
                                           public void onStopTrackingTouch(SeekBar seekBar) {

                                               Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                           }

                                       }
        );
        final SeekBar obstacleUpperV = (SeekBar) view.findViewById(R.id.seekBarupZ);
        obstacleUpperV.setMax(Parameters.MAX_V);
        obstacleUpperV.setProgress((int) Parameters.upperBoundsObstacles.val[2]);
        obstacleUpperV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                               double[] values = Parameters.upperBoundsObstacles.val;
                                               values[2] = progress;
                                               Parameters.upperBoundsObstacles.set(values);

                                           }
                                           public void onStartTrackingTouch(SeekBar seekBar) {

                                           }
                                           public void onStopTrackingTouch(SeekBar seekBar) {
                                               Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                           }

                                       }
        );

        //Car
        final SeekBar lowerBoundsCarHue = (SeekBar) view.findViewById(R.id.seekBarCarlowX);
        lowerBoundsCarHue.setMax(Parameters.MAX_V);
        lowerBoundsCarHue.setProgress((int) Parameters.lowerBoundsCar.val[0]);
        lowerBoundsCarHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                          double[] values = Parameters.lowerBoundsCar.val;
                                                          values[0] = progress;
                                                          Parameters.lowerBoundsCar.set(values);

                                                      }
                                                      public void onStartTrackingTouch(SeekBar seekBar) {

                                                      }
                                                      public void onStopTrackingTouch(SeekBar seekBar) {
                                                          Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                                      }

                                                  }
        );
        final SeekBar lowerBoundsCarSaturation = (SeekBar) view.findViewById(R.id.seekBarCarlowY);
        lowerBoundsCarSaturation.setMax(Parameters.MAX_S);
        lowerBoundsCarSaturation.setProgress((int) Parameters.lowerBoundsCar.val[1]);
        lowerBoundsCarSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                          double[] values = Parameters.lowerBoundsCar.val;
                                                          values[1] = progress;
                                                          Parameters.lowerBoundsCar.set(values);

                                                      }
                                                      public void onStartTrackingTouch(SeekBar seekBar) {

                                                      }
                                                      public void onStopTrackingTouch(SeekBar seekBar) {
                                                          Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                                      }

                                                  }
        );

        final SeekBar lowerBoundsCarValue = (SeekBar) view.findViewById(R.id.seekBarCarlowZ);
        lowerBoundsCarValue.setMax(Parameters.MAX_V);
        lowerBoundsCarValue.setProgress((int) Parameters.lowerBoundsCar.val[2]);
        lowerBoundsCarValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                                    double[] values = Parameters.lowerBoundsCar.val;
                                                                    values[2] = progress;
                                                                    Parameters.lowerBoundsCar.set(values);

                                                                }
                                                                public void onStartTrackingTouch(SeekBar seekBar) {

                                                                }
                                                                public void onStopTrackingTouch(SeekBar seekBar) {
                                                                    Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
        );

        final SeekBar upperBoundsCarHue = (SeekBar) view.findViewById(R.id.seekBarCarUpX);
        upperBoundsCarHue.setMax(Parameters.MAX_H);
        upperBoundsCarHue.setProgress((int) Parameters.upperBoundsCar.val[0]);
        upperBoundsCarHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                               double[] values = Parameters.lowerBoundsCar.val;
                                                               values[0] = progress;
                                                               Parameters.lowerBoundsCar.set(values);

                                                           }
                                                           public void onStartTrackingTouch(SeekBar seekBar) {

                                                           }
                                                           public void onStopTrackingTouch(SeekBar seekBar) {
                                                               Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                                           }

                                                       }
        );

        final SeekBar upperBoundsCarSaturation = (SeekBar) view.findViewById(R.id.seekBarCarUpY);
        upperBoundsCarSaturation.setMax(Parameters.MAX_S);
        upperBoundsCarSaturation.setProgress((int) Parameters.upperBoundsCar.val[1]);
        upperBoundsCarSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                             double[] values = Parameters.lowerBoundsCar.val;
                                                             values[1] = progress;
                                                             Parameters.lowerBoundsCar.set(values);

                                                         }
                                                         public void onStartTrackingTouch(SeekBar seekBar) {

                                                         }
                                                         public void onStopTrackingTouch(SeekBar seekBar) {
                                                             Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                                         }

                                                     }
        );

        final SeekBar upperBoundsCarValue = (SeekBar) view.findViewById(R.id.seekBarCarUpZ);
        upperBoundsCarValue.setMax(Parameters.MAX_V);
        upperBoundsCarValue.setProgress((int) Parameters.upperBoundsCar.val[2]);
        upperBoundsCarValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                                    double[] values = Parameters.lowerBoundsCar.val;
                                                                    values[2] = progress;
                                                                    Parameters.lowerBoundsCar.set(values);

                                                                }
                                                                public void onStartTrackingTouch(SeekBar seekBar) {

                                                                }
                                                                public void onStopTrackingTouch(SeekBar seekBar) {
                                                                    Toast.makeText(getActivity(), ""+ seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
        );

        return view;
    }

}
