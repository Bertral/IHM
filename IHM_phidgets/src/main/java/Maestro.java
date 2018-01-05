import com.phidget22.*;

import java.util.ArrayList;

public class Maestro implements Runnable {


    private ArrayList<Instrument> instruments; // paires Input <-> fichier son

    public Maestro(ArrayList<Instrument> instruments) {
        this.instruments = instruments;

    }

    @Override
    public void run() {
        try {
            System.out.println("Calibrating ... Please stomp the ground a few times (this will take 5 seconds) !");

            ArrayList<Thread> threads = new ArrayList<>();
            for (final Instrument ins : instruments) {
                threads.add(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ins.calibrate();
                    }
                }));
            }

            for (Thread t : threads) {
                t.start();
            }

            for (Thread t : threads) {
                t.join();
            }

            System.out.println("Calibrated !");

            long lastTriggered = 0;
            while (true) {
                ArrayList<Instrument> triggeredInstruments = new ArrayList<>();

                for (Instrument ins : instruments) {
                    if(ins.isTriggered()) {
                        triggeredInstruments.add(ins);
                    }
                }

                Instrument triggeredInstrument = null;
                for (Instrument ins : triggeredInstruments) {
                    if ((triggeredInstrument == null || ins.getSensorValue() - ins.getThreshold() >
                            triggeredInstrument.getSensorValue() - triggeredInstrument.getThreshold())) {
                        triggeredInstrument = ins;
                    }
                }

                if (triggeredInstrument != null && System.currentTimeMillis() - lastTriggered > 200) {
                    triggeredInstrument.play();
                    lastTriggered = System.currentTimeMillis();
                }

                Thread.sleep(10);
            }
        } catch (PhidgetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //ch.close();
    }
}
