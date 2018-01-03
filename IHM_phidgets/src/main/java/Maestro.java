import com.phidget22.*;

import javax.sound.sampled.AudioSystem;
import java.util.ArrayList;

public class Maestro implements Runnable {

    public static void main(String[] args) throws Exception {
        //Enable logging to stdout
        com.phidget22.Log.enable(LogLevel.INFO, null);

        ArrayList<Instrument> instruments = new ArrayList<>();

        VoltageRatioInput ch0 = new VoltageRatioInput();
        ch0.setChannel(0);

        VoltageRatioInput ch1 = new VoltageRatioInput();
        ch1.setChannel(1);

        instruments.add(new Instrument(ch0,
                AudioSystem.getAudioInputStream(Maestro.class.getResourceAsStream("a1.wav"))));


        instruments.add(new Instrument(ch1,
                AudioSystem.getAudioInputStream(Maestro.class.getResourceAsStream("f1.wav"))));

        Thread thread = new Thread(new Maestro(instruments));
        thread.start();
        thread.join();
    }

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
