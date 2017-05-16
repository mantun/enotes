/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */


package enotes;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {

    static final String VERSION = "2.0";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        MainForm mf = new MainForm();
        mf.setSize(800, 550);
        mf.setLocationRelativeTo(null);
        mf.setVisible(true);
        mf.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("icon.png")));

        if (args.length == 1) {
            File f = new File(args[0]);
            if (!f.canRead()) {
                System.err.println("File not found or access denied: "+args[0]);
                return;
            }
            if (!mf.internalOpenFile(new File(args[0]))) {
                System.err.println("Cannot open file: " + args[0]);
            }
        }
    }

}
