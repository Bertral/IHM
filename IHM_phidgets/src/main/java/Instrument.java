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
    private boolean enabled = false;
    private double lowerThreshold = 0.45;
    private double previousValue = lowerThreshold;

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

        // lorsque le hub usb est détecté, configure son taux de rafraichissement au plus rapide (16 ms)
        sensorInput.addAttachListener(attachEvent -> {
            VoltageRatioInput source = (VoltageRatioInput) attachEvent.getSource();
            try {
                source.setDataInterval(source.getMinDataInterval());
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
        });

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
//        double val = sensorInput.getSensorValue();
//        lowerThreshold = (val + previousValue)/2;
//        previousValue = val;
        return sensorInput.getSensorValue();
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean enabled) throws PhidgetException, NullPointerException {
        if (enabled) {
            // teste le fonctionnement de getSensorValue()
            sensorInput.getSensorValue();
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
