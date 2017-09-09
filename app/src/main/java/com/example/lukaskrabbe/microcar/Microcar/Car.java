package com.example.lukaskrabbe.microcar.Microcar;

import com.example.lukaskrabbe.microcar.MicrocarService;

/**
 * Car is an abstraction layer to simplify interaction with the microcar.
 */
public class Car {
    private MicrocarService microcarService;

    /**
     * Initializes a Car.
     *
     * @param microcarService The service of the microcar.
     */
    public Car(MicrocarService microcarService) {
        this.microcarService = microcarService;
    }

    /**
     * Steer sets the steering angle of the microcar.
     * 0 is the default value to drive forward.
     * Negative values are used to steer to the left.
     * Positive values are used to steer to the right.
     * The value itself represents the angle.
     *
     * @param angle The steering angle.
     *
     * @return Returns false if angle is set to low/high. Otherwise true.
     */
    public boolean Steer(int angle) {
        if (angle == 0) {
            microcarService.write(CarCodes.NO_STEER);
        } else if(angle < 0) {
            if (-angle >= CarCodes.STEER_LEFT.length) {
                return false;
            }
            microcarService.write(CarCodes.STEER_LEFT[-angle - 1]);
        } else {
            if (angle >= CarCodes.STEER_RIGHT.length) {
                return false;
            }
            microcarService.write(CarCodes.STEER_RIGHT[angle - 1]);
        }

        return true;
    }

    /**
     * Drive sets the speed and driving direction of the microcar.
     * 0 stops the microcar.
     * Negative values are used to drive backwards.
     * Positive values are used to drive forwards.
     * The value itself represents the speed.
     *
     * @param speed The speed of the microcar.
     *
     * @return Returns false if speed is set to high. Otherwise true.
     */
    public boolean Drive(int speed) {
        if (speed == 0) {
            microcarService.write(CarCodes.NO_SPEED);
        } else if (speed < 0) {
            if (-speed >= CarCodes.SPEED_BACK.length) {
                return false;
            }
            microcarService.write(CarCodes.SPEED_BACK[-speed - 1]);
        } else {
            if (speed >= CarCodes.SPEED_FRONT.length) {
                return false;
            }
            microcarService.write(CarCodes.SPEED_FRONT[speed - 1]);
        }

        return true;
    }

    /**
     * Horn enables/disables the Horn of the microcar.
     *
     * @param enabled If true the horn will be enabled. Otherwise it will be disabled.
     */
    public void Horn(boolean enabled) {
        if (enabled) {
            microcarService.write(CarCodes.HORN_ON);
        } else {
            microcarService.write(CarCodes.HORN_OFF);
        }
    }

    /**
     * Light sets the light of the microcar to the given state.
     *
     * @param state State of the light.
     */
    public void Light(LightState state) {
        switch (state) {
            case OFF:
                microcarService.write(CarCodes.LIGHTS_OFF);
                break;
            case LOW:
                microcarService.write(CarCodes.LIGHTS_SOFT);
                break;
            case HIGH:
                microcarService.write(CarCodes.LIGHTS);
                break;
        }
    }

    /**
     * Blink sets the blinkers of the microcar to the given state.
     *
     * @param state State of the blinkers.
     */
    public void Blink(BlinkState state) {
        switch (state) {
            case OFF:
                microcarService.write(CarCodes.BLINK_LEFT_OFF);
                microcarService.write(CarCodes.BLINK_RIGHT_OFF);
                break;
            case BLINK_LEFT:
                microcarService.write(CarCodes.BLINK_LEFT);
                microcarService.write(CarCodes.BLINK_RIGHT_OFF);
                break;
            case BLINK_RIGHT:
                microcarService.write(CarCodes.BLINK_RIGHT);
                microcarService.write(CarCodes.BLINK_LEFT_OFF);
                break;
            case BLINK_ALL:
                microcarService.write(CarCodes.BLINK_LEFT);
                microcarService.write(CarCodes.BLINK_RIGHT);
        }
    }

    /**
     * Indicates whether this instance of car still has a valid connection
     * @return
     */
    public boolean hasConnection() {
        return this.microcarService.getState() == MicrocarService.STATE_CONNECTED;
    }
}