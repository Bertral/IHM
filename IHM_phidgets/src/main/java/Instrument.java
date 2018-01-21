import com.phidget22.PhidgetException;
import com.phidget22.VoltageRatioInput;

import javax.sound.sampled.*;
import java.io.IOException;

/**
 * Project : IHM_phidgets
 * Date : 30.12.17
 * Authors : Antoine FRIANT, Lawrence STALDER, Valentin FININI
 *
 * Représente un capteur et son fichier audio
 */
public class Instrument {
    private VoltageRatioInput sensorInput;  // input du capteur
    private Clip clip;                      // clip audio
    private boolean enabled = false;        // instrument muet ou non
    private double lowerThreshold = 0.45;   // seuil d'activation

    /**
     * Constructeur
     */
    public Instrument() {
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Assigne un capteur à l'instrument
     *
     * @param sensorInput capteur
     * @throws PhidgetException
     */
    public void setSensorInput(VoltageRatioInput sensorInput) throws PhidgetException {
        // ferme le capteur précédent s'il était déjà ouvert
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

        // ouvre le canal du capteur
        sensorInput.open();

        this.sensorInput = sensorInput;
    }

    /**
     * Assigne un fichier audio à l'instrument
     *
     * @param inputStream
     */
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

    /**
     * Joue le fichier son
     */
    public void play() {
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Récupère la reçue par le capteur
     * @return
     */
    public double getSensorValue() {
        try {
            return sensorInput.getSensorValue();
        } catch (PhidgetException e) {
            return 0.5;
        }
    }

    /**
     * Retourne faux si l'instrument est rendu muet par l'utilisateur
     * @return
     */
    public synchronized boolean isEnabled() {
        return enabled;
    }

    /**
     * Active ou rend muet le capteur
     *
     * @param enabled
     * @throws PhidgetException levée si le capteur n'est pas branché
     * @throws NullPointerException levée si le fichier audio n'est pas défini
     */
    public synchronized void setEnabled(boolean enabled) throws PhidgetException, NullPointerException {
        if (enabled) {
            // teste le fonctionnement de getSensorValue (s'il lance une exception ou non)
            sensorInput.getSensorValue();
            if (!clip.isOpen()) {
                throw new NullPointerException();
            }
        }

        this.enabled = enabled;
    }

    /**
     * Définit un seuil d'activation
     * @param lowerThreshold
     */
    public void setLowerThreshold(double lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    /**
     * @return seuil d'activation
     */
    public double getLowerThreshold() {
        return lowerThreshold;
    }
}
