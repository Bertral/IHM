import com.phidget22.AttachEvent;
import com.phidget22.AttachListener;
import com.phidget22.PhidgetException;
import com.phidget22.VoltageRatioInput;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * Project : IHM_phidgets
 * Date : 05.01.18
 */
public class MainWindow {

    // Elements d'interface (initialisés par Intellij UI Designer)
    private JPanel mainPanel;
    private JButton calibrateAll;
    private JButton muteAll;
    private JComboBox profilesComboBox;
    private JButton loadProfileBtn;
    private JButton saveProfileBtn;
    private JButton deleteProfileBtn;
    private JPanel profilePanel;
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
    private JButton browse0;
    private JButton calibrate0;
    private JButton calibrate1;
    private JButton calibrate2;
    private JButton calibrate3;
    private JButton calibrate4;
    private JButton calibrate5;
    private JButton calibrate6;
    private JButton calibrate7;
    private JTextField textField0;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;

    // Liste de correspondances instrument-contrôles
    private ArrayList<InstrumentUI> instrumentUIs;

    // Coordinateur entre instruments (nécessaire pour éviter l'activation multiple lorsque plusieurs capteurs
    // détectent la même vibration)
    private Maestro maestro;

    /**
     * Point d'entrée de l'application
     * Ouvre la fenêtre principale
     *
     * @param args ignorés
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tremor");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /**
     * Appelé avant le contructeur pour initialiser manuellement des éléments d'interface
     */
    private void createUIComponents() {

    }

    /**
     * Constructeur
     */
    public MainWindow() {
        final ArrayList<Instrument> instruments = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            instruments.add(new Instrument());
        }
        maestro = new Maestro(instruments);

        // Associe les éléments d'interface avec leur instrument
        instrumentUIs = new ArrayList<>();
        instrumentUIs.add(new InstrumentUI(instruments.get(0), textField0, browse0, calibrate0, enabled0));
        instrumentUIs.add(new InstrumentUI(instruments.get(1), textField1, browse1, calibrate1, enabled1));
        instrumentUIs.add(new InstrumentUI(instruments.get(2), textField2, browse2, calibrate2, enabled2));
        instrumentUIs.add(new InstrumentUI(instruments.get(3), textField3, browse3, calibrate3, enabled3));
        instrumentUIs.add(new InstrumentUI(instruments.get(4), textField4, browse4, calibrate4, enabled4));
        instrumentUIs.add(new InstrumentUI(instruments.get(5), textField5, browse5, calibrate5, enabled5));
        instrumentUIs.add(new InstrumentUI(instruments.get(6), textField6, browse6, calibrate6, enabled6));
        instrumentUIs.add(new InstrumentUI(instruments.get(7), textField7, browse7, calibrate7, enabled7));

        // file chooser
        final JFileChooser fc = new JFileChooser();

        // Pour chaque ligne d'interface contrôlant un instrument ...
        for (int i = 0; i < instrumentUIs.size(); i++) {
            try {
                VoltageRatioInput ch = new VoltageRatioInput();
                ch.setChannel(i);

                // lorsque le hub usb est branché, configure son taux de rafraichissement au plus rapide
                ch.addAttachListener(attachEvent -> {
                    VoltageRatioInput source = (VoltageRatioInput) attachEvent.getSource();
                    try {
                        source.setDataInterval(source.getMinDataInterval());
                    } catch (PhidgetException e) {
                        e.printStackTrace();
                    }
                });
                instrumentUIs.get(i).instrument.setSensorInput(ch);
            } catch (PhidgetException e) {
                e.printStackTrace();
            }

            // configure le textfield listener (noms de fichiers)
            final int index = i;
            instrumentUIs.get(i).textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent documentEvent) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent documentEvent) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    update();
                }

                private void update() {
                    try {
                        // définit le fichier source de l'instrument puis l'active
                        instrumentUIs.get(index).instrument.setAudioInputStream(AudioSystem.getAudioInputStream(
                                new File(instrumentUIs.get(index).textField.getText())));
                        if (!instrumentUIs.get(index).enabled.isSelected()) {
                            instrumentUIs.get(index).enabled.doClick();
                        }

                        // en cas d'erreur, disable l'instrument
                    } catch (UnsupportedAudioFileException e) {
                        if (instrumentUIs.get(index).enabled.isSelected()) {
                            instrumentUIs.get(index).enabled.doClick();
                        }

                        // message d'erreur
                        JOptionPane.showMessageDialog(mainPanel,
                                "Instrument " + index + " sound file format unsupported !",
                                "File error",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (IOException e) {
                        if (instrumentUIs.get(index).enabled.isSelected()) {
                            instrumentUIs.get(index).enabled.doClick();
                        }
                    }
                }
            });

            // Browse button listener
            instrumentUIs.get(i).browse.addActionListener(actionEvent -> {
                int returnVal = fc.showOpenDialog(mainPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    //This is where a real application would open the file.
                    instrumentUIs.get(index).textField.setText(file.getAbsolutePath());
                }
            });

            // Boutons de lancement de calibration
            instrumentUIs.get(i).calibrate.addActionListener(actionEvent -> {
                Thread t = new Thread(() -> {
                    // Fenêtre de progression
                    final JDialog dlg = new JDialog(new JFrame(), "Calibrating", true);
                    final JProgressBar dpb = new JProgressBar(0, 500);
                    dlg.add(BorderLayout.CENTER, dpb);
                    dlg.add(BorderLayout.NORTH, new JLabel("Stand still, then stomp the ground !"));
                    dlg.setLocationRelativeTo(null);
                    dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    dlg.setSize(300, 75);
                    dlg.setResizable(false);

                    Thread t12 = new Thread(() -> dlg.setVisible(true));
                    t12.start();

                    try {
                        double min = Double.MAX_VALUE;
                        double max = Double.MIN_VALUE;
                        for (int i1 = 0; i1 < 500; i1++) {
                            dpb.setValue(i1);
                            double val = instrumentUIs.get(index).instrument.getSensorValue();

                            if (val > max) {
                                max = val;
                            }

                            if (val < min && val > 0.0) {
                                min = val;
                            }

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        dlg.dispose();

                        //instrumentUIs.get(index).spinner.setValue((min + max) / 2);

                        instrumentUIs.get(index).instrument.setUpperThreshold(max);

                        instrumentUIs.get(index).instrument.setLowerThreshold(min);

                    } catch (PhidgetException e) {
                        try {
                            // Attend l'affichage de la fenêtre de progression avant de la fermer
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                        }
                        dlg.dispose();

                        // message d'erreur
                        JOptionPane.showMessageDialog(mainPanel,
                                "Sensor " + index + " isn't plugged in !",
                                "Sensor error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                });
                t.start();
            });

            // Enabled checkbox
            instrumentUIs.get(i).enabled.addActionListener(actionEvent -> {
                try {
                    instrumentUIs.get(index).instrument.setEnabled(instrumentUIs.get(index).enabled.isSelected());
                } catch (Exception e) {
                    instrumentUIs.get(index).enabled.setSelected(false);

                    // message d'erreur
                    JOptionPane.showMessageDialog(mainPanel,
                            "Sensor " + index + " isn't plugged in or sound file is invalid.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        // désactive tous les instruments
        muteAll.addActionListener(actionEvent -> {
            for (InstrumentUI ins : instrumentUIs) {
                if (ins.enabled.isSelected()) {
                    ins.enabled.doClick();
                }
            }
        });

        // calibre tous les instruments TODO
        calibrateAll.addActionListener(actionEvent -> {
            Thread t = new Thread(() -> {
                // Fenêtre de progression
                final JDialog dlg = new JDialog(new JFrame(), "Calibrating", true);
                final JProgressBar dpb = new JProgressBar(0, 500);
                dlg.add(BorderLayout.CENTER, dpb);
                dlg.add(BorderLayout.NORTH, new JLabel("Stand still, then stomp the ground !"));
                dlg.setLocationRelativeTo(null);
                dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dlg.setSize(300, 75);
                dlg.setResizable(false);

                Thread dialogThread = new Thread(() -> dlg.setVisible(true));
                dialogThread.start();


                ArrayList<Thread> threads = new ArrayList<>();
                for (int j = 0; j < instrumentUIs.size(); j++) {
                    final int finalJ = j;

                    threads.add(new Thread(() -> {
                        try {
                            double min = Double.MAX_VALUE;
                            double max = Double.MIN_VALUE;
                            for (int i = 0; i < 500; i++) {
                                dpb.setValue(i);
                                double val = instrumentUIs.get(finalJ).instrument.getSensorValue();


                                if (val > max) {
                                    max = val;
                                }

                                if (val < min && val > 0.0) {
                                    min = val;
                                }

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            dlg.dispose();

                            // instrumentUIs.get(finalJ).spinner.setValue((min + max) / 2);

                        } catch (PhidgetException e) {
                            try {
                                // Attend l'affichage de la fenêtre de progression avant de la fermer
                                Thread.sleep(100);
                            } catch (InterruptedException e1) {
                            }
                            dlg.dispose();
                        }
                    }));
                }

                for (Thread t1 : threads) {
                    t1.start();
                }

                for (Thread t1 : threads) {
                    try {
                        t1.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        });

        // sauvegarder un profil
        saveProfileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // Si le profil n'a pas de nom, on affiche un message d'erreur
                if (profilesComboBox.getSelectedItem() == null || profilesComboBox.getSelectedItem().equals("")) {
                    // message d'erreur
                    JOptionPane.showMessageDialog(mainPanel,
                            "Please enter a profile name.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {

                    Properties properties = new Properties();
                    try {
                        properties.load(this.getClass().getClassLoader().getResourceAsStream("profiles.properties"));

                        // Si le profil existe, on demande confirmation
                        if (properties.getProperty(profilesComboBox.getSelectedItem() + "instrument" + 0 + "file") !=
                                null) {
                            int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
                                    "Profile \"" + profilesComboBox.getSelectedItem() + "\" already exists, are you " +
                                            "sure you want to overwrite the existing profile ?",
                                    "Confirm overwrite",
                                    JOptionPane.YES_NO_OPTION);
                            if (dialogResult != JOptionPane.YES_OPTION) {
                                return;
                            }
                        }

                        for (int i = 0; i < instrumentUIs.size(); i++) {
                            String propertyName = profilesComboBox.getSelectedItem() + "instrument" + i;
                            properties.setProperty(propertyName + "file", instrumentUIs.get(i).textField.getText());
                            properties.setProperty(propertyName + "enabled",
                                    String.valueOf(instrumentUIs.get(i).enabled.isSelected()));
                            properties.setProperty(propertyName + "upperThreshold",
                                    String.valueOf(instrumentUIs.get(i).instrument.getUpperThreshold()));
                            properties.setProperty(propertyName + "lowerThreshold",
                                    String.valueOf(instrumentUIs.get(i).instrument.getLowerThreshold()));
                        }

                        properties.store(new FileWriter(new File(this.getClass().getClassLoader().getResource
                                ("profiles.properties").toURI())), "");

                        refreshProfiles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // load a profile
        loadProfileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                Properties properties = new Properties();
                try {
                    properties.load(this.getClass().getClassLoader().getResourceAsStream("profiles.properties"));

                    if (properties.getProperty(profilesComboBox.getSelectedItem() + "instrument" + 0 + "file") ==
                            null) {
                        // message d'erreur
                        JOptionPane.showMessageDialog(mainPanel,
                                "Profile \"" + profilesComboBox.getSelectedItem() + "\" doesn't exist.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        for (int i = 0; i < instrumentUIs.size(); i++) {
                            String propertyName = profilesComboBox.getSelectedItem() + "instrument" + i;
                            instrumentUIs.get(i).textField.setText(properties.getProperty(propertyName + "file"));
                            instrumentUIs.get(i).enabled.setSelected(Boolean.parseBoolean(properties.getProperty
                                    (propertyName + "enabled")));
                            instrumentUIs.get(i).instrument.setUpperThreshold(Double.parseDouble(properties.getProperty
                                    (propertyName + "upperThreshold")));
                            instrumentUIs.get(i).instrument.setLowerThreshold(Double.parseDouble(properties.getProperty
                                    (propertyName + "lowerThreshold")));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // supprime un profile
        deleteProfileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                Properties properties = new Properties();
                try {
                    properties.load(this.getClass().getClassLoader().getResourceAsStream("profiles.properties"));

                    if (properties.getProperty(profilesComboBox.getSelectedItem() + "instrument" + 0 + "file") ==
                            null) {
                        // message d'erreur
                        JOptionPane.showMessageDialog(mainPanel,
                                "Profile \"" + profilesComboBox.getSelectedItem() + "\" doesn't exist.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
                                "Are you sure you want to delete profile \"" + profilesComboBox.getSelectedItem() +
                                        "\" ?",
                                "Confirm deletion",
                                JOptionPane.YES_NO_OPTION);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            for (int i = 0; i < instrumentUIs.size(); i++) {
                                String propertyName = profilesComboBox.getSelectedItem() + "instrument" + i;
                                properties.remove(propertyName + "file");
                                properties.remove(propertyName + "enabled");
                                properties.remove(propertyName + "upperThreshold");
                                properties.remove(propertyName + "lowerThreshold");
                            }
                        }

                        properties.store(new FileWriter(new File(this.getClass().getClassLoader().getResource
                                ("profiles.properties").toURI())), "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                refreshProfiles();
            }
        });

        // charge les profiles dans la combobox
        refreshProfiles();

        // TODO : load a profile

        new Thread(maestro).start();
    }

    public void refreshProfiles() {
        profilesComboBox.removeAllItems();
        Set<String> names = new TreeSet<>();

        Properties properties = new Properties();

        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("profiles.properties"));

            for (String name : properties.stringPropertyNames()) {
                name = name.substring(0, name.lastIndexOf("instrument"));
                names.add(name);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        names.forEach(profilesComboBox::addItem);
    }

    /**
     * Classe regroupant un instrument et les éléments d'interface qui permettent de le configurer
     */
    private class InstrumentUI {
        private Instrument instrument;
        private JTextField textField;
        private JButton browse;
        private JButton calibrate;
        private JCheckBox enabled;

        public InstrumentUI(Instrument instrument, JTextField textField, JButton browse,
                            JButton calibrate, JCheckBox enabled) {
            this.instrument = instrument;
            this.textField = textField;
            this.browse = browse;
            this.calibrate = calibrate;
            this.enabled = enabled;
        }
    }
}