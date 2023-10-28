package gui;

import io.RegattaWriter;
import rowing.Boat;
import rowing.Heat;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class RegattaResultFrame extends JFrame {

    private JPanel mainPanel;

    public RegattaResultFrame(List<Heat> regattaPlan) {
        super("Regattaplan");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        init(regattaPlan);

        pack();
    }

    private void init(List<Heat> regattaPlan) {

        mainPanel = new JPanel(new BorderLayout());

        final int N = 3;

        for (int i = 0; i < N; ++i) {
            int finalI = i;

            Thread t = new Thread(() -> {
                switch (finalI) {
                    case 0:
                        initExportButton(regattaPlan);
                        break;
                    case 1:
                        initRegattaPlanTable(regattaPlan);
                        break;
                    default:
                        break;
                }
            });

            t.start();

            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        setContentPane(mainPanel);

    }

    private void initRegattaPlanTable(List<Heat> regattaPlan) {

        String[] columnNames = {"Rennen", "Start", "Distanz", "Boot 1", "Boot 2", "Boot 3", "Boot 4"};

        DefaultTableModel tableModel = new DefaultTableModel();
        Arrays.stream(columnNames).forEach(tableModel::addColumn);

        JTable resultTable = new JTable(tableModel);
        resultTable.setDefaultEditor(Object.class, null);
        resultTable.getTableHeader().setReorderingAllowed(false);

        resultTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultTable.setFillsViewportHeight(true);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        for (Vector<String> row : getHeatData(regattaPlan)) {
            tableModel.addRow(row);
        }

    }

    private List<Vector<String>> getHeatData(List<Heat> heats) {

        List<Vector<String>> list = new ArrayList<>();

        for (int i = 0; i < heats.size(); i++) {
            Vector<String> vector = new Vector<>();

            Heat h = heats.get(i);

            // heat number
            vector.addElement(String.valueOf(i + 1));

            // time
            String pattern = "HH:mm";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            vector.addElement(simpleDateFormat.format(h.getTime()));

            // distance
            vector.addElement(String.format("%dm", h.getDistance()));

            // lane 1-4
            for (Boat boat : h.getBoats()) {
                vector.addElement(boat.toString());
            }

            list.add(vector);
        }

        return list;
    }


    private void initExportButton(List<Heat> regattaPlan) {

        JButton btExport = new JButton("Speichern");

        btExport.addActionListener(e -> {
            exportTable(regattaPlan);
        });

        mainPanel.add(btExport, BorderLayout.SOUTH);

    }

    private void exportTable(List<Heat> list) {

        File file = getSavingDestinationFile();
        if (file == null) return;

        RegattaWriter writer = new RegattaWriter();
        writer.write(list, file, RegattaWriter.STANDARD_SEPARATOR);

        JOptionPane.showMessageDialog(null,
                "Der Regattaplan wurde erfolgreich gespeichert.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);

        setVisible(false);
        dispose();

    }

    private File getSavingDestinationFile() {

        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        chooser.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                final File f = (File) evt.getNewValue();
            }
        });

        FileFilter filter = new FileNameExtensionFilter(".csv", "csv");
        chooser.setFileFilter(filter);

        chooser.setVisible(true);

        final int result = chooser.showSaveDialog(null);

        File f = chooser.getSelectedFile();
        if (f != null && !f.getName().endsWith(".csv")) {
            String path = String.format("%s.csv", f.getAbsolutePath());
            f = new File(path);
        }

        return result == JFileChooser.APPROVE_OPTION ? f : null;
    }


}
