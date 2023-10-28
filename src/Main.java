
import gui.RegattaPlannerFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        //Start GUI
        JFrame frame = new RegattaPlannerFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
