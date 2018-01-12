import com.phidget22.*;

import java.util.ArrayList;

public class Maestro implements Runnable {
    private final int ACTIVATION_COOLDOWN = 300;

    private ArrayList<Instrument> instruments; // paires Input <-> fichier son

    public Maestro(ArrayList<Instrument> instruments) {
        this.instruments = instruments;

    }

    @Override
    public void run() {
        try {
            long lastTriggered = 0;
            while (true) {
                ArrayList<Instrument> triggeredInstruments = new ArrayList<>();

                // sample sur 100 ms
                for(int i = 0; i < 10; i++){
                    Thread.sleep(10);
                }

                for (Instrument ins : instruments) {
                    if (ins.isEnabled() && ins.isTriggered()) {
                        triggeredInstruments.add(ins);
                    }
                }

                Instrument triggeredInstrument = null;
                for (Instrument ins : triggeredInstruments) {
                    if ((triggeredInstrument == null ||
                            Math.abs(ins.getSensorValue() - (ins.getUpperThreshold() + ins.getLowerThreshold()) / 2) >
                                    Math.abs(triggeredInstrument.getSensorValue() - (triggeredInstrument
                                            .getUpperThreshold() + triggeredInstrument.getLowerThreshold()) / 2))) {
                        triggeredInstrument = ins;
                    }
                }

                if (triggeredInstrument != null && System.currentTimeMillis() - lastTriggered > ACTIVATION_COOLDOWN) {
                    triggeredInstrument.play();
                    lastTriggered = System.currentTimeMillis();
                }
            }
        } catch (PhidgetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //ch.close();
    }
}
