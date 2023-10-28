package gui;

import io.HeatReader;
import planners.RegattaPlanner;
import rowing.Athlete;
import rowing.Boat;
import rowing.Heat;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

public class RegattaPlannerFrame extends JFrame implements ListSelectionListener {

    private JPanel panelMain;
    private JPanel panelHeats;
    private JPanel panelConfig;
    private JPanel panelModifyHeats;
    private JPanel panelModifyButtons;
    private JButton btAdd;
    private JButton btDiscard;
    private JPanel panelModify;
    private JLabel txtDistance;
    private JComboBox<String> cbxDistance;
    private JPanel panelDistance;
    private JPanel panelLane1;
    private JLabel labelCrew;
    private JTextField textCrew1;
    private JLabel labelBoat;
    private JPanel panelLane2;
    private JPanel panelLane3;
    private JPanel panelLane4;
    private JLabel labelCrew2;
    private JTextField textCrew2;
    private JTextField textBoat2;
    private JTextField textCrew3;
    private JTextField textBoat3;
    private JTextField textCrew4;
    private JTextField textBoat4;
    private JLabel labelBoat4;
    private JLabel labelCrew4;
    private JLabel labelBoat3;
    private JLabel labelCrew3;
    private JLabel labelBoat2;
    private JTextField textBoat1;
    private JPanel panelRegatta;
    private JButton btPlan;
    private JSpinner spinnerLunchBreakSTart;
    private JSpinner spinnerDuration;
    private JSpinner spinnerRegattaStart;
    private JButton btDelete;
    private JTable tableHeats;

    private DefaultTableModel tableModel;

    // Initialize global variable
    private final List<Heat> heatList = new ArrayList<Heat>();
    private final HeatReader reader = new HeatReader();
    private final JTextField[] heatEditorFields = {textCrew1, textBoat1,
            textCrew2, textBoat2,
            textCrew3, textBoat3,
            textCrew4, textBoat4
    };

    //States of editing
    static final int EDITOR_STATE_ADD = 0;
    static final int EDITOR_STATE_EDIT = 1;
    static int EDITOR_STATE = EDITOR_STATE_ADD;

    private Color defaultTextFieldBackGroundColor = Color.white;


    public RegattaPlannerFrame() {
        super("Regatta Planer");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);

            defaultTextFieldBackGroundColor = textBoat1.getBackground();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        initGUIComponents();

        setEditorState(EDITOR_STATE_ADD);
        pack();
    }

    /**
     * Starts GUI-init methods in parallel.
     */
    private void initGUIComponents() {

        final int numberOfComponentGroups = 3;

        for (int i = 0; i < numberOfComponentGroups; ++i) {
            int finalI = i;
            Thread t = new Thread(() -> {

                switch (finalI) {
                    case 0:
                        initMainComponents();
                        break;
                    case 1:
                        initTable();
                        break;
                    case 2:
                        initMenu();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + finalI);
                }
            });
            t.start();

            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            setContentPane(panelMain);
        }

    }

    private void initMainComponents() {

        // Configure elements

        SpinnerNumberModel spinnerModelDuration = new SpinnerNumberModel();
        spinnerModelDuration.setMinimum(0);
        spinnerModelDuration.setMaximum(120);
        spinnerModelDuration.setStepSize(5);

        spinnerDuration.setModel(spinnerModelDuration);
        spinnerDuration.setValue(30);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        Date startTime = calendar.getTime();
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        Date endTime = calendar.getTime();

        LocalDate currentDate = LocalDate.now();

        SpinnerDateModel dateModelRegattaStart = new SpinnerDateModel(startTime, null, endTime, Calendar.MINUTE);
        SpinnerDateModel dateModelLunchBreak = new SpinnerDateModel(startTime, null, endTime, Calendar.MINUTE);

        spinnerRegattaStart.setModel(dateModelRegattaStart);
        spinnerRegattaStart.setEditor(new JSpinner.DateEditor(spinnerRegattaStart, "HH:mm"));
        spinnerRegattaStart.setValue(new Date(currentDate.getYear(), currentDate.getMonthValue(),
                currentDate.getDayOfMonth() + 1, 9, 0));

        spinnerLunchBreakSTart.setModel(dateModelLunchBreak);
        spinnerLunchBreakSTart.setEditor(new JSpinner.DateEditor(spinnerLunchBreakSTart, "HH:mm"));
        spinnerLunchBreakSTart.setValue(new Date(currentDate.getYear(), currentDate.getMonthValue(),
                currentDate.getDayOfMonth() + 1, 11, 0));


        String[] distances = {"500m", "1000m"};

        for (String str : distances) {
            cbxDistance.addItem(str);
        }


        // Action Listeners
        btAdd.addActionListener(e -> {

            if (EDITOR_STATE == EDITOR_STATE_ADD) {

                for (JTextField txt : heatEditorFields) {
                    if (txt.getText().isEmpty()) continue;

                    if (addHeat()) {
                        clearHeatEditFields();
                    }
                    break;
                }
            } else if (EDITOR_STATE == EDITOR_STATE_EDIT) {

                if (updateHeat()) {
                    clearHeatEditFields();
                }
            }


            setEditorState(EDITOR_STATE_ADD);
        });

        btDelete.addActionListener(e -> {

            if (EDITOR_STATE == EDITOR_STATE_EDIT) {
                removeSelectedData(tableHeats);
            }

            clearHeatEditFields();
            setEditorState(EDITOR_STATE_ADD);
        });

        btDiscard.addActionListener(e -> {
            clearHeatEditFields();
            setEditorState(EDITOR_STATE_ADD);

        });


        btPlan.addActionListener(e -> {

            if (heatList.size() < 3) {
                JOptionPane.showMessageDialog(null,
                        "Nicht genügend Rennen hinzugefügt.\nMind. 3 Rennen werden für die Planung benötigt.",
                        "Fehler",
                        JOptionPane.WARNING_MESSAGE);

                return;
            }

            Date regattaStart = (Date) spinnerRegattaStart.getValue();
            Date lunch = (Date) spinnerLunchBreakSTart.getValue();
            double lunchDuration = (double) ((int) spinnerDuration.getValue());

            planRegatta(regattaStart, lunch, lunchDuration);
        });


    }


    private void initTable() {
        tableModel = new DefaultTableModel();

        String[] columnNames = {"Distanz",
                "Mannschaft 1", "Boot 1",
                "Mannschaft 2", "Boot 2",
                "Mannschaft 3", "Boot 3",
                "Mannschaft 4", "Boot 4"};

        Arrays.stream(columnNames).forEach(c -> tableModel.addColumn(c));

        tableHeats = new JTable(tableModel);

        tableHeats.setDefaultEditor(Object.class, null);
        tableHeats.getTableHeader().setReorderingAllowed(false);

        ListSelectionModel selectionModel = tableHeats.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        JScrollPane scrollPaneTable = new JScrollPane(tableHeats);
        tableHeats.setFillsViewportHeight(true);
        panelHeats.add(scrollPaneTable, BorderLayout.CENTER);

        selectionModel.addListSelectionListener(this);

    }


    private void initMenu() {

        // Create MenuBar

        JMenuBar menuBar = new JMenuBar();

        // Menu File
        JMenu menuFile = new JMenu("Datei");
        JMenuItem menuItemImport = new JMenuItem("Importieren...");
        JMenuItem menuItemExit = new JMenuItem("Beenden");

        menuFile.add(menuItemImport);
        menuFile.add(menuItemExit);

        // Menu Help
        JMenu menuHelp = new JMenu("Hilfe");
        JMenuItem menuItemAbout = new JMenuItem("Info");

        menuHelp.add(menuItemAbout);


        menuBar.add(menuFile);
        menuBar.add(menuHelp);
        panelMain.add(menuBar, BorderLayout.NORTH);


        // ActionListener
        menuItemExit.addActionListener(e -> System.exit(0));
        menuItemImport.addActionListener(e -> importFile());
        menuItemAbout.addActionListener(e -> showInfo());
    }

    private void clearHeatEditFields() {
        resetRequiredEditorFields();
        Arrays.stream(heatEditorFields).forEach(heatEditorField -> heatEditorField.setText(""));

        removeTableListener();
        tableHeats.clearSelection();
        addTableListener();
    }


    private File openFile() {

        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileFilter filter = new FileNameExtensionFilter("*.csv", "csv");
        chooser.setFileFilter(filter);

        chooser.setVisible(true);

        final int result = chooser.showOpenDialog(null);

        return result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }


    private Vector<String> getHeatData(Heat heat) {

        Vector<String> row = new Vector<>();

        row.addElement(String.format("%dm", heat.getDistance()));

        for (int i = 0; i < heat.getLanes(); i++) {

            Boat b = heat.getBoats().get(i);
            Athlete[] athletes = b.getAthletes();

            StringBuilder builder = new StringBuilder();

            final int N = athletes.length - 1;

            for (int j = 0; j < N; j++) {
                builder.append(String.format("%s, ", athletes[j].getName()));
            }

            builder.append(athletes[N].getName());

            if (b.getCox() != null) {
                builder.append(String.format(", St. %s", b.getCox().getName()));
            }

            row.addElement(builder.toString());
            row.addElement(b.getName());
        }

        return row;
    }

    /**
     * Plans regatta
     */
    private void planRegatta(Date regattaStart, Date lunch, double lunchDuration) {

        final Heat finalHeat = getFinalHeat();

        if (finalHeat == null) return;

        RegattaPlanner planner = new RegattaPlanner(RegattaPlanner.HEAT_1000_OFFSET);
        List<Heat> regattaPlan = planner.organizeRegatta(heatList, finalHeat, regattaStart, lunch, lunchDuration);


        if (regattaPlan.size() != heatList.size()) {
            /*
             * Planning failed
             */

            StringBuilder info = new StringBuilder();

            // extract not included heats
            List<Heat> listMissingOnes = new ArrayList<>();
            for (Heat h : heatList) {
                if (!regattaPlan.contains(h)) {
                    listMissingOnes.add(h);
                    info.append(h.toString()).append("\n");
                }
            }

            // show error message
            Object[] choices = {"Regattaplan anzeigen", "Abbrechen"};

            int choice = JOptionPane.showOptionDialog(null,
                    String.format("Folgende Rennen konnten nicht eingeplant werden:\n\n%s\nVersuchen Sie eine längere Mittagspause oder ein anderes Finalrennen zu wählen!\n\nRegattaplan trotzdem anzeigen?\n", info.toString()),
                    "Regattaplanung fehlgeschlagen",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    choices,
                    choices[0]);

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

        }

        Thread thread = new Thread(() -> {

            // show result
            RegattaResultFrame frame = new RegattaResultFrame(regattaPlan);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);


        });

        thread.start();

    }


    /**
     * Determines a final heat in order to plan the regatta
     */
    private Heat getFinalHeat() {

        final Heat[] objects = new Heat[heatList.size()];

        int preselected = 0;
        int counter = 0;

        //The last heat of the regatta should be Eight vs Eight.
        for (int i = 0; i < objects.length; ++i) {

            //Initialize objects for dialog
            objects[i] = heatList.get(i);

            //Find Eight vs. Eight

            int c = 0;

            for (Boat b : heatList.get(i).getBoats()) {
                if (b.getAthletes().length != 8) continue;

                c++;
            }

            if (c > counter) {
                counter = c;
                preselected = i;
            }
        }

        return (Heat) JOptionPane.showInputDialog(null,
                "Wählen Sie das letzte Rennen der Regatta:",
                "Letztes Rennen",
                JOptionPane.QUESTION_MESSAGE,
                null,
                objects,
                objects[preselected]);

    }


    private void setEditorState(int editorState) {
        EDITOR_STATE = editorState;

        switch (EDITOR_STATE) {
            case EDITOR_STATE_EDIT:
                btAdd.setText("Änderungen speichern");
                btAdd.setToolTipText("Änderungen am aktuellen Rennens speichern");
                btDiscard.setVisible(true);

                break;

            case EDITOR_STATE_ADD:
            default:
                btAdd.setText("Rennen hinzufügen");
                btAdd.setToolTipText("Aktuelles Rennen hinzufügen");
                btDiscard.setVisible(false);
                break;

        }
    }

    private boolean isErrorFlagSet() {
        // Error handling: some required data not entered

        Color colorRequired = Color.pink;

        if (textCrew1.getText().isEmpty() && !textBoat1.getText().isEmpty()) {
            textCrew1.setBackground(colorRequired);
            return true;
        }
        if (!textCrew1.getText().isEmpty() && textBoat1.getText().isEmpty()) {
            textBoat1.setBackground(colorRequired);
            return true;
        }
        if (textCrew2.getText().isEmpty() && !textBoat2.getText().isEmpty()) {
            textCrew2.setBackground(colorRequired);
            return true;
        }
        if (!textCrew2.getText().isEmpty() && textBoat2.getText().isEmpty()) {
            textBoat2.setBackground(colorRequired);
            return true;
        }
        if (textCrew3.getText().isEmpty() && !textBoat3.getText().isEmpty()) {
            textCrew3.setBackground(colorRequired);
            return true;
        }
        if (!textCrew3.getText().isEmpty() && textBoat3.getText().isEmpty()) {
            textBoat3.setBackground(colorRequired);
            return true;
        }
        if (textCrew4.getText().isEmpty() && !textBoat4.getText().isEmpty()) {
            textCrew4.setBackground(colorRequired);
            return true;
        }
        if (!textCrew4.getText().isEmpty() && textBoat4.getText().isEmpty()) {
            textBoat4.setBackground(colorRequired);
            return true;
        }

        return false;
    }

    private Heat getHeatFromEntry() {
        // read entries
        String heatStringRegEx = "%d;%s;%s;%s;%s";
        final String lanePattern = "%s (%s)";

        String lane1 = "";
        String lane2 = "";
        String lane3 = "";
        String lane4 = "";

        if (!textCrew1.getText().isEmpty() && !textBoat1.getText().isEmpty()) {
            lane1 = String.format(lanePattern, textCrew1.getText(), textBoat1.getText());
        }
        if (!textCrew2.getText().isEmpty() && !textBoat2.getText().isEmpty()) {
            lane2 = String.format(lanePattern, textCrew2.getText(), textBoat2.getText());
        }
        if (!textCrew3.getText().isEmpty() && !textBoat3.getText().isEmpty()) {
            lane3 = String.format(lanePattern, textCrew3.getText(), textBoat3.getText());
        }
        if (!textCrew4.getText().isEmpty() && !textBoat4.getText().isEmpty()) {
            lane4 = String.format(lanePattern, textCrew4.getText(), textBoat4.getText());
        }

        int distance = Integer.parseInt(Objects.requireNonNull(cbxDistance.getSelectedItem()).toString().replace("m", ""));

        try {
            return new HeatReader().stringToHeat(String.format(heatStringRegEx, distance, lane1, lane2, lane3, lane4));

        } catch (IllegalComponentStateException e) {
            JOptionPane.showMessageDialog(null,
                    String.format("Eingabe nicht lesbar.\n\nFehlermeldung:\n%s", e.getMessage()),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);

            return null;
        }
    }

    private boolean updateHeat() {

        if (isErrorFlagSet()) return false;

        Heat newHeat = getHeatFromEntry();

        if (newHeat == null) return false;

        final int row = tableHeats.getSelectedRow();

        Heat currentHeat = heatList.get(row);

        //update heat
        currentHeat.setDistance(newHeat.getDistance());
        currentHeat.getBoats().clear();

        for (Boat b : newHeat.getBoats()) {
            currentHeat.addBoat(b);
        }

        removeTableListener();

        tableModel.removeRow(row);
        tableModel.insertRow(row, getHeatData(currentHeat));

        addTableListener();

        return true;
    }

    private boolean addHeat() {

        // Error handling: some required data not entered
        if (isErrorFlagSet()) return false;

        Heat heat = getHeatFromEntry();

        if (heat == null) return false;

        // Add heat to list
        heatList.add(heat);
        tableModel.addRow(getHeatData(heat));

        return true;
    }

    private void resetRequiredEditorFields() {
        for (JTextField txtF : heatEditorFields) {
            txtF.setBackground(defaultTextFieldBackGroundColor);
        }
    }

    private void removeSelectedData(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        final int row = table.getSelectedRow();

        if (row != -1) {
            table.getSelectionModel().removeListSelectionListener(this);
            model.removeRow(row);
            table.getSelectionModel().addListSelectionListener(this);

            heatList.remove(row);
        }
    }

    private void importFile() {
        File file = openFile();

        if (file == null) return;

        try {
            List<Heat> ls = reader.readHeats(file);

            if (ls == null) return;

            removeTableListener();
            tableHeats.clearSelection();

            if (heatList.size() > 0) {

                Object[] options = {"Ja, überschreiben", "Nein", "Abbrechen"};

                int choice = JOptionPane.showOptionDialog(null,
                        "Sollen die bereits hinzugefügten Rennen überschrieben werden?",
                        "Rennen überschreiben?",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, null);

                if (choice == JOptionPane.CANCEL_OPTION) return;

                if (choice == JOptionPane.YES_OPTION) {
                    // Clear table and remove the corresponding heats


                    while (tableModel.getRowCount() > 0) {
                        tableModel.removeRow(0);
                    }

                    heatList.clear();
                }
            }

            heatList.addAll(ls);

            for (Heat h : ls) {
                tableModel.addRow(getHeatData(h));
            }

            addTableListener();

        } catch (
                IOException exception) {
            JOptionPane.showMessageDialog(null,
                    "Auf die gewählte Datei kann nicht zugegriffen werden.\nSchließen Sie alle Programme, die auf diese Datei zugreifen.",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);

        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(null,
                    exception.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
        }

        clearHeatEditFields();
        setEditorState(EDITOR_STATE_ADD);

    }

    private void removeTableListener() {
        tableHeats.getSelectionModel().removeListSelectionListener(this);
    }

    private void addTableListener() {
        tableHeats.getSelectionModel().addListSelectionListener(this);
    }


    @Override
    public void valueChanged(ListSelectionEvent e) {
        setEditorState(EDITOR_STATE_EDIT);

        int row = tableHeats.getSelectedRow();

        cbxDistance.setSelectedItem(tableHeats.getValueAt(row, 0).toString());

        for (int column = 0; column < heatEditorFields.length; column++) {
            heatEditorFields[column].setText(tableHeats.getValueAt(row, column + 1) != null ?
                    tableHeats.getValueAt(row, column + 1).toString() : "");
        }
    }

    /**
     * shows About Dialog
     */
    private void showInfo() {

        final String about = "Regatta Planer\nVersion: 1.0\n\nEntwickler: Laurent Rodemerk\n\nDer Regatta Planer unterstützt die Trainer beim Planen der Abschlussregatta im Trainingslager des PRC.";

        JOptionPane.showMessageDialog(null, about, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
