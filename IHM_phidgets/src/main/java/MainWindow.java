import com.phidget22.PhidgetException;
import com.phidget22.VoltageRatioInput;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Project : IHM_phidgets
 * Date : 05.01.18
 * Authors : Antoine FRIANT, Lawrence STALDER, Valentin FININI
 * <p>
 * Fenêtre principale de l'application
 */
public class MainWindow {

    // noms des noeuds de préfécences pour la sauvegarde des profils (Java Preferences API)
    private static final String ROOT_NODE = "tremor";
    private static final String FILE_NODE = "file";
    private static final String ENABLED_NODE = "enabled";
    private static final String LOWERTHRESHOLD_NODE = "lowerThreshold";

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
     * @param args ignoré
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
     * Constructeur
     */
    public MainWindow() {
        // Créé les intruments
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

        // sélecteur de fichier
        final JFileChooser fc = new JFileChooser();

        // Pour chaque ligne d'interface contrôlant un instrument ...
        for (int i = 0; i < instrumentUIs.size(); i++) {
            try {
                // définit le canal du capteur de vibration
                VoltageRatioInput ch = new VoltageRatioInput();
                ch.setChannel(i);

                // assigne le capteur à l'instrument
                instrumentUIs.get(i).instrument.setSensorInput(ch);
            } catch (PhidgetException e) {
                e.printStackTrace();
            }

            // configure le textfield listener (inputs des noms de fichiers audio)
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
                        // définit le fichier source de l'instrument
                        instrumentUIs.get(index).instrument.setAudioInputStream(AudioSystem.getAudioInputStream(
                                new File(instrumentUIs.get(index).textField.getText())));

                        // en cas d'erreur (fichier inexistant ou mal formatté), disable l'instrument
                    } catch (UnsupportedAudioFileException e) {
                        if (instrumentUIs.get(index).enabled.isSelected()) {
                            instrumentUIs.get(index).enabled.doClick();
                        }

                        // message d'erreur en cas de format non reconnu
                        JOptionPane.showMessageDialog(mainPanel,
                                "Instrument " + index + " sound file format unsupported (expected AIFF, AU or WAV) !",
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
                // ouvre le sélecteur de fichiers
                int returnVal = fc.showOpenDialog(mainPanel);

                // remplit le textfield avec l'url du fichier
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    instrumentUIs.get(index).textField.setText(file.getAbsolutePath());
                }
            });

            // Boutons de lancement de calibration
            instrumentUIs.get(i).calibrate.addActionListener(actionEvent -> {
                Thread t = new Thread(() -> {
                    // Fenêtre de progression (5 secondes)
                    final JDialog dlg = new JDialog(new JFrame(), "Calibrating", true);
                    final JProgressBar dpb = new JProgressBar(0, 500);
                    dlg.add(BorderLayout.CENTER, dpb);
                    dlg.add(BorderLayout.NORTH, new JLabel("Stomp the ground !"));
                    dlg.setLocationRelativeTo(null);
                    dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    dlg.setSize(300, 75);
                    dlg.setResizable(false);

                    Thread t12 = new Thread(() -> dlg.setVisible(true));
                    t12.start();

                    double min = Double.MAX_VALUE;
                    for (int i1 = 0; i1 < 500; i1++) {
                        // fait avancer la barre de progression
                        dpb.setValue(i1);
                        // récupère la valeur du capteur
                        double val = instrumentUIs.get(index).instrument.getSensorValue();

                        // garde la valeur min (qui n'est pas 0.0, car il arrive que 0.0 soit lu, c'est une erreur)
                        if (val < min && val > 0.0) {
                            min = val;
                        }

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // ferme la fenêtre de progression
                    dlg.dispose();

                    // le seuil d'activation est la plus petite valeur observée
                    instrumentUIs.get(index).instrument.setLowerThreshold(min);


                });
                t.start();
            });

            // "enabled" checkbox
            instrumentUIs.get(i).enabled.addActionListener(actionEvent -> {
                try {
                    // essaie d'activer l'instrument
                    instrumentUIs.get(index).instrument.setEnabled(instrumentUIs.get(index).enabled.isSelected());
                } catch (Exception e) {
                    // en cas d'erreur, déselectionne la checkbox et affiche un message d'erreur
                    instrumentUIs.get(index).enabled.setSelected(false);

                    // message d'erreur
                    JOptionPane.showMessageDialog(mainPanel,
                            "Sensor " + index + " isn't plugged in or sound file is invalid.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        // désactive tous les instruments (bouton mute all)
        muteAll.addActionListener(actionEvent -> {
            for (InstrumentUI ins : instrumentUIs) {
                if (ins.enabled.isSelected()) {
                    ins.enabled.doClick();
                }
            }
        });

        // calibre tous les instruments
        calibrateAll.addActionListener(actionEvent -> {
            Thread t = new Thread(() -> {
                // Fenêtre de progression
                final JDialog dlg = new JDialog(new JFrame(), "Calibrating", true);
                final JProgressBar dpb = new JProgressBar(0, 500);
                dlg.add(BorderLayout.CENTER, dpb);
                dlg.add(BorderLayout.NORTH, new JLabel("Stomp the ground !"));
                dlg.setLocationRelativeTo(null);
                dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dlg.setSize(300, 75);
                dlg.setResizable(false);

                Thread dialogThread = new Thread(() -> dlg.setVisible(true));
                dialogThread.start();

                // pour chaque instrument, prépare la calibration sur un autre thread
                ArrayList<Thread> threads = new ArrayList<>();
                for (int j = 0; j < instrumentUIs.size(); j++) {
                    final int finalJ = j;

                    threads.add(new Thread(() -> {
                        double min = Double.MAX_VALUE;
                        for (int i = 0; i < 500; i++) {
                            // fait avancer la barre de progression
                            dpb.setValue(i);
                            // récupère la valeur du capteur
                            double val = instrumentUIs.get(finalJ).instrument.getSensorValue();

                            // garde la valeur min (qui n'est pas 0.0, car il arrive que 0.0 soit lu, c'est une erreur)
                            if (val < min && val > 0.0) {
                                min = val;
                            }

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // ferme la fenêtre de progression
                        dlg.dispose();

                        // le seuil d'activation est la plus petite valeur observée
                        instrumentUIs.get(finalJ).instrument.setLowerThreshold(min);

                    }));
                }

                // lance les threads
                for (Thread t1 : threads) {
                    t1.start();
                }

                // attend la fin des calibrations
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
        saveProfileBtn.addActionListener(actionEvent -> {

            // Si le profil n'a pas de nom, on affiche un message d'erreur
            if (profilesComboBox.getSelectedItem() == null) {
                // message d'erreur
                JOptionPane.showMessageDialog(mainPanel,
                        "Please enter a profile name.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Preferences prefs = Preferences.userRoot().node(ROOT_NODE);

                    // Si le profil existe, on demande confirmation
                    if (prefs.nodeExists(profilesComboBox.getSelectedItem().toString())) {
                        int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
                                "Profile \"" + profilesComboBox.getSelectedItem() + "\" already exists, are you " +
                                        "sure you want to overwrite the existing profile ?",
                                "Confirm overwrite",
                                JOptionPane.YES_NO_OPTION);
                        if (dialogResult != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }

                    // sauvegarde tous les InstrumentUIs
                    Preferences profileNode = prefs.node(profilesComboBox.getSelectedItem().toString());

                    for (int i = 0; i < instrumentUIs.size(); i++) {
                        profileNode.node(String.valueOf(i)).put(FILE_NODE, instrumentUIs.get(i).textField.getText
                                ());
                        profileNode.node(String.valueOf(i)).putBoolean(ENABLED_NODE, instrumentUIs.get(i).enabled
                                .isSelected());
                        profileNode.node(String.valueOf(i)).putDouble(LOWERTHRESHOLD_NODE, instrumentUIs.get(i)
                                .instrument.getLowerThreshold());
                    }

                    // applique les changements
                    prefs.flush();

                    // recharge la liste des profils
                    refreshProfiles();
                } catch (BackingStoreException e) {
                    e.printStackTrace();
                }
            }
        });

        // load a profile
        loadProfileBtn.addActionListener(actionEvent -> {
            Preferences prefs = Preferences.userRoot().node(ROOT_NODE);

            try {
                if (profilesComboBox.getSelectedItem() == null || !prefs.nodeExists(profilesComboBox
                        .getSelectedItem().toString())) {
                    // message d'erreur
                    JOptionPane.showMessageDialog(mainPanel,
                            "Profile \"" + profilesComboBox.getSelectedItem() + "\" doesn't exist.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {

                    Preferences profileNode = prefs.node(profilesComboBox.getSelectedItem().toString());

                    for (int i = 0; i < instrumentUIs.size(); i++) {
                        instrumentUIs.get(i).textField.setText(profileNode.node(String.valueOf(i)).get(FILE_NODE,
                                ""));
                        instrumentUIs.get(i).instrument.setLowerThreshold(profileNode.node(String.valueOf(i))
                                .getDouble(LOWERTHRESHOLD_NODE, 0.5));
                        if (profileNode.node(String.valueOf(i)).getBoolean
                                (ENABLED_NODE, false) != instrumentUIs.get(i).enabled.isSelected()) {
                            instrumentUIs.get(i).enabled.doClick();
                        }
                    }
                }

            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        });

        // supprime un profile
        deleteProfileBtn.addActionListener(actionEvent -> {
            try {
                Preferences prefs = Preferences.userRoot().node(ROOT_NODE);

                if (profilesComboBox.getSelectedItem() == null || !prefs.nodeExists(profilesComboBox
                        .getSelectedItem().toString())) {
                    // message d'erreur
                    JOptionPane.showMessageDialog(mainPanel,
                            "Profile \"" + profilesComboBox.getSelectedItem() + "\" doesn't exist.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    // confirmation de suppression
                    int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
                            "Are you sure you want to delete profile \"" + profilesComboBox.getSelectedItem() +
                                    "\" ?",
                            "Confirm deletion",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        // suppression
                        prefs.node(profilesComboBox.getSelectedItem().toString()).removeNode();
                    }

                    // mise à jour de la suppression
                    prefs.flush();
                }

            } catch (BackingStoreException e) {
                e.printStackTrace();
            }

            // vide la combobox
            profilesComboBox.setSelectedItem(null);

            // rechage la liste des profils
            refreshProfiles();
        });

        // charge les profiles dans la combobox
        refreshProfiles();

        new Thread(maestro).start();
    }

    public void refreshProfiles() {
        // sauvegarde le dernier élément sélectionné
        Object selected = profilesComboBox.getSelectedItem();

        // retire tous les profils de la combobox
        profilesComboBox.removeAllItems();

        try {
            // lit tous les noms de profils et ajoute les noms de profil à la combobox
            Preferences prefs = Preferences.userRoot().node(ROOT_NODE);
            Arrays.asList(prefs.childrenNames()).forEach(profilesComboBox::addItem);
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        // resélectionne le dernier élément sélectionné
        profilesComboBox.setSelectedItem(selected);
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