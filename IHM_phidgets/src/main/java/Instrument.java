import com.phidget22.PhidgetException;
import com.phidget22.VoltageRatioInput;

import javax.sound.sampled.*;
import java.io.IOException;

/**
 * Project : IHM_phidgets
 * Date : 30.12.17
 */
public class Instrument {
    private VoltageRatioInput sensorInput;
    private Clip clip;
    private double threshold;
    private boolean triggered = false;

    public Instrument(VoltageRatioInput sensorInput, AudioInputStream inStream) throws IOException,
            LineUnavailableException, PhidgetException {
        this.sensorInput = sensorInput;
        sensorInput.open();
        clip = AudioSystem.getClip();
        clip.open(inStream);
    }

    public void play() {
        clip.setFramePosition(0);
        clip.start();
    }

    public double getSensorValue() throws PhidgetException {
        return sensorInput.getSensorValue();
    }

    public boolean isTriggered() throws PhidgetException {
        if(!triggered) {
            triggered = sensorInput.getSensorValue() > threshold;
        } else if(sensorInput.getSensorValue() > threshold) {
            return false;
        } else {
            triggered = false;
        }

        return triggered;
    }

    public double getThreshold() {
        return threshold;
    }

    public void calibrate() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for(int i = 0; i < 500; i++) {
            double val = 0;
            try {
                val = getSensorValue();
            } catch (PhidgetException e) {
                e.printStackTrace();
            }

            if(val > max) {
                max = val;
            }

            if(val < min && val > 0.0) {
                min = val;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        threshold = (min+max)/2;// + 0.05*(min+max)/2);
    }
}
