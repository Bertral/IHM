import javax.swing.*;

/**
 * Project : IHM_phidgets
 * Date : 05.01.18
 */
public class MainWindow {

    private JPanel mainPanel;
    private JButton calibrateAll;
    private JSpinner spinner0;
    private JButton muteAll;
    private JButton calibrate0;
    private JButton browse0;
    private JTextField textField1;
    private JComboBox profilesComboBox;
    private JButton loadProfile;
    private JButton saveProfile;
    private JButton deleteProfile;
    private JPanel profilePanel;
    private JSpinner spinner1;
    private JSpinner spinner3;
    private JSpinner spinner2;
    private JSpinner spinner4;
    private JSpinner spinner5;
    private JSpinner spinner6;
    private JSpinner spinner7;
    private JCheckBox enabled0;
    private JCheckBox enabled1;
    private JCheckBox enabled2;
    private JCheckBox enabled3;
    private JCheckBox enabled4;
    private JCheckBox enabled5;
    private JCheckBox enabled6;
    private JCheckBox enabled7;
    private JButton browse7;
    private JButton browse6;
    private JButton browse5;
    private JButton browse4;
    private JButton browse3;
    private JButton browse2;
    private JButton browse1;
    private JButton calibrate1;
    private JButton calibrate2;
    private JButton calibrate3;
    private JButton calibrate4;
    private JButton calibrate5;
    private JButton calibrate6;
    private JButton calibrate7;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainWindow");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public MainWindow() {
    }

    private void createUIComponents() {
        spinner0 = new JSpinner(new SpinnerNumberModel(0.5,0 ,1,0.05));
        spinner1 = new JSpinner(new SpinnerNumberModel(0.5,0 ,1,0.05));
        spinner2 = new JSpinner(new SpinnerNumberModel(0.5,0 ,1,0.05));
        spinner3 = new JSpinner(new SpinnerNumberModel(0.5,0 ,1,0.05));
        spinner4 = new JSpinner(new SpinnerNumberModel(0.5,0 ,1,0.05));
        spinner5 = new JSpinner(new SpinnerNumberModel(0.5,0 ,1,0.05));
        spinner6 = new JSpinner(new SpinnerNumberModel(0.5,0 ,1,0.05));
        spinner7 = new JSpinner(new SpinnerNumberModel(0.5,0 ,1,0.05));
    }

    //        //Enable logging to stdout
//        com.phidget22.Log.enable(LogLevel.INFO, null);
//
//        ArrayList<Instrument> instruments = new ArrayList<>();
//
//        VoltageRatioInput ch0 = new VoltageRatioInput();
//        ch0.setChannel(0);
//
//        VoltageRatioInput ch1 = new VoltageRatioInput();
//        ch1.setChannel(1);
//
//        instruments.add(new Instrument(ch0,
//                AudioSystem.getAudioInputStream(Maestro.class.getResourceAsStream("a1.wav"))));
//
//
//        instruments.add(new Instrument(ch1,
//                AudioSystem.getAudioInputStream(Maestro.class.getResourceAsStream("f1.wav"))));
//
//        Thread thread = new Thread(new Maestro(instruments));
//        thread.start();
//        thread.join();



/* UI :

Main :
Liste des 8 canaux avec boutons "add/edit sound" (-> parcourir), "mute" (-> checkbox!), "threshold" (numberBox bornée 0-1),
    "Quick calibration" (pop up de 5 sec)
Quick calibration sur 5 secondes (tous d'un coup sur plusieurs threads) -> pop up de 5 sec
(opt) Sauvegarder/charger le profil (instruments et calibrations)

 */

}
