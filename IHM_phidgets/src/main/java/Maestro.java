import com.phidget22.*;

import java.util.ArrayList;

/**
 * Project : IHM_phidgets
 * Date : 30.12.17
 */
public class Maestro implements Runnable {
    private final int ACTIVATION_COOLDOWN = 50;

    private ArrayList<Instrument> instruments; // paires Input <-> fichier son

    public Maestro(ArrayList<Instrument> instruments) {
        this.instruments = instruments;
    }

    @Override
    public void run() {
        try {
            long lastLoop = System.currentTimeMillis();

            while (true) {
                // si la dernière itération a duré moins que 12 ms, attend jusqu'à 12 ms
                long currentTime = System.currentTimeMillis();
                if(currentTime - lastLoop < 12) {
                    Thread.sleep(12 - (currentTime - lastLoop));
                }
                lastLoop = currentTime;

                // recherche un instrument actif, sous le seuil d'activation, qui a détecté le plus gros choc
                Instrument triggeredInstrument = null;
                double triggeredVal = Double.MAX_VALUE;
                for (Instrument ins : instruments) {
                    double val = ins.getSensorValue();
                    if (ins.isEnabled() // instrument actif
                            && val < ins.getLowerThreshold() // valeur sous le seuil d'activation
                            && (triggeredInstrument == null
                            || ins.getLowerThreshold() - val > triggeredInstrument.getLowerThreshold() - triggeredVal)
                        // écart au seuil plus grand que les autres instuments activés
                            ) {
                        triggeredInstrument = ins;
                        triggeredVal = val;
                    }
                }

                if (triggeredInstrument != null) {
                    triggeredInstrument.play();
                    Thread.sleep(ACTIVATION_COOLDOWN);
                }
            }
        } catch (PhidgetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
