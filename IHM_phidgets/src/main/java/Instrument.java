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
    private double upperThreshold = 0.55;
    private boolean triggered = false;
    private boolean enabled = false;
    private double lowerThreshold = 0.45;

    public Instrument() {
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void setSensorInput(VoltageRatioInput sensorInput) throws PhidgetException {
        if (this.sensorInput != null) {
            this.sensorInput.close();
        }
        sensorInput.open();
        this.sensorInput = sensorInput;
    }

    public void setAudioInputStream(AudioInputStream inputStream) {
        try {
            if (clip.isOpen()) {
                clip.close();
            }
            clip.open(inputStream);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        clip.setFramePosition(0);
        clip.start();
    }

    public double getSensorValue() throws PhidgetException {
        return sensorInput.getSensorValue();
    }

    public boolean isTriggered() throws PhidgetException {
        if (!triggered) {
            triggered = sensorInput.getSensorValue() > upperThreshold || lowerThreshold > sensorInput.getSensorValue();
        } else if (sensorInput.getSensorValue() > upperThreshold || lowerThreshold > sensorInput.getSensorValue()) {
            return false;
        } else {
            triggered = false;
        }

        return triggered;
    }

    public double getUpperThreshold() {
        return upperThreshold;
    }

    public void setUpperThreshold(double upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) throws PhidgetException, NullPointerException {
        if(enabled) {
            double testSensor = sensorInput.getSensorValue();
            if (!clip.isOpen()) {
                throw new NullPointerException();
            }
        }

        this.enabled = enabled;
    }

    public void setLowerThreshold(double lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    public double getLowerThreshold() {
        return lowerThreshold;
    }
}
