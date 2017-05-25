/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes;

import enotes.doc.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainForm extends javax.swing.JFrame {

    static final int OPT_SAVE = 1;
    static final int OPT_NOSAVE = 2;
    static final int OPT_CANCEL = 3;

    static final int WHYSAVE_SAVE = 1;
    static final int WHYSAVE_SAVEAS = 2;
    static final int WHYSAVE_CLOSE = 3;
    public static final int NUM_BACKUPS = 5;

    private DocMetadata docm = new DocMetadata();
    private UndoManager undoManager;

    private WordSearcher searcher;
    int tp_line, tp_col;

    /** Creates new form fmain */
    public MainForm() {
        initComponents();
        updateTitle();
        tp.addCaretListener( new CaretListener(){
          public void caretUpdate(CaretEvent e ){
              Document doc = tp.getDocument();
                Element root = doc.getDefaultRootElement();
                int dot = e.getDot();
                tp_line = root.getElementIndex( dot );
                tp_col = dot - root.getElement( tp_line ).getStartOffset();
                updateCaretStatus();
            }
          } );
        tp.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onDocumentUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onDocumentUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onDocumentUpdate(e);
            }
        });
        undoManager = new UndoManager();
        tp.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });
        updateCaretStatus();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lbCaret = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        tfFind = new javax.swing.JTextField();
        btFind = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tp = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        miNew = new javax.swing.JMenuItem();
        miOpen = new javax.swing.JMenuItem();
        miSave = new javax.swing.JMenuItem();
        miSaveAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        miExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        miFind = new javax.swing.JMenuItem();
        miUndo = new javax.swing.JMenuItem();
        miRedo = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        miAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Encrypted Notes");
        setMinimumSize(new java.awt.Dimension(400, 300));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        jPanel1.setLayout(new java.awt.BorderLayout());

        lbCaret.setText("00:00");
        jPanel1.add(lbCaret, java.awt.BorderLayout.WEST);

        jPanel2.setLayout(new java.awt.BorderLayout());

        tfFind.setForeground(java.awt.SystemColor.inactiveCaption);
        tfFind.setText("Find...");
        tfFind.setMinimumSize(new java.awt.Dimension(150, 19));
        tfFind.setPreferredSize(new java.awt.Dimension(150, 19));
        tfFind.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfFindFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfFindFocusLost(evt);
            }
        });
        tfFind.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfFindKeyReleased(evt);
            }
        });
        jPanel2.add(tfFind, java.awt.BorderLayout.CENTER);

        btFind.setText("Find");
        btFind.setFocusable(false);
        btFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFindActionPerformed(evt);
            }
        });
        jPanel2.add(btFind, java.awt.BorderLayout.EAST);

        jPanel1.add(jPanel2, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        tp.setFont(new java.awt.Font("Monospaced", 0, 12));
        tp.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                tpCaretPositionChanged(evt);
            }
        });
        tp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tpKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tpKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(tp);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");
        jMenu1.setMnemonic('F');

        miNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        miNew.setText("New document...");
        miNew.setMnemonic('N');
        miNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miNewActionPerformed(evt);
            }
        });
        jMenu1.add(miNew);

        miOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        miOpen.setText("Open...");
        miOpen.setMnemonic('O');
        miOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miOpenActionPerformed(evt);
            }
        });
        jMenu1.add(miOpen);

        miSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        miSave.setText("Save");
        miSave.setMnemonic('S');
        miSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSaveActionPerformed(evt);
            }
        });
        jMenu1.add(miSave);

        miSaveAs.setText("Save As...");
        miSaveAs.setMnemonic('A');
        miSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSaveAsActionPerformed(evt);
            }
        });
        jMenu1.add(miSaveAs);
        jMenu1.add(jSeparator1);

        miExit.setText("Exit");
        miExit.setMnemonic('x');
        miExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miExitActionPerformed(evt);
            }
        });
        jMenu1.add(miExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenu2.setMnemonic('E');

        miFind.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        miFind.setText("Find...");
        miFind.setMnemonic('F');
        miFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miFindActionPerformed(evt);
            }
        });
        jMenu2.add(miFind);

        miUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        miUndo.setText("Undo");
        miUndo.setMnemonic('U');
        miUndo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                MainForm.this.miUndoActionPerformed(evt);
            }
        });
        jMenu2.add(miUndo);

        miRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        miRedo.setText("Redo");
        miRedo.setMnemonic('R');
        miRedo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                MainForm.this.miRedoActionPerformed(evt);
            }
        });
        jMenu2.add(miRedo);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");
        jMenu3.setMnemonic('H');

        miAbout.setText("About");
        miAbout.setMnemonic('A');
        miAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAboutActionPerformed(evt);
            }
        });
        jMenu3.add(miAbout);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        pack();
    }

    private void miExitActionPerformed(java.awt.event.ActionEvent evt) {
        if (!canExit())
            return;
        this.setVisible(false);
        System.exit(0);
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        if (!canExit())
            return;
        this.setVisible(false);
        System.exit(0);
    }

    private void miNewActionPerformed(java.awt.event.ActionEvent evt) {
        if (checkSave(WHYSAVE_CLOSE) == OPT_CANCEL)
            return;
        tp.setText("");
        docm = new DocMetadata();
        updateTitle();
    }

    private void miSaveActionPerformed(java.awt.event.ActionEvent evt) {
        if (docm.filename == null) {
            miSaveAsActionPerformed(evt);
            return;
        }
        checkSave(WHYSAVE_SAVE);
    }

    private void miSaveAsActionPerformed(java.awt.event.ActionEvent evt) {
        checkSave(WHYSAVE_SAVEAS);
    }

    private void tpKeyTyped(java.awt.event.KeyEvent evt) {

    }

    private void tpCaretPositionChanged(java.awt.event.InputMethodEvent evt) {
        updateCaretStatus();
    }

    private void tpKeyPressed(java.awt.event.KeyEvent evt) {

    }

    private void tfFindFocusGained(java.awt.event.FocusEvent evt) {
        if (tfFind.getText().equals("Find...")) {
            tfFind.setForeground(java.awt.SystemColor.controlText);
            tfFind.setText("");
        }
    }

    private void tfFindFocusLost(java.awt.event.FocusEvent evt) {
        if (tfFind.getText().equals("")) {
            tfFind.setText("Find...");
            tfFind.setForeground(java.awt.SystemColor.inactiveCaption);
        }
        if (searcher != null) {
            searcher.removeHighlights();
            searcher = null;
        }
    }

    private void miOpenActionPerformed(java.awt.event.ActionEvent evt) {
        openFile();
    }

    private void miFindActionPerformed(java.awt.event.ActionEvent evt) {
        tfFind.requestFocus();
    }

    private void tfFindKeyReleased(java.awt.event.KeyEvent evt) {
        if (evt.getKeyChar() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F3) {
            find((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0);
            evt.consume();
        }
        if (evt.getKeyChar() == KeyEvent.VK_ESCAPE ) {
            tp.requestFocus();
            evt.consume();
        }
    }

    private void btFindActionPerformed(java.awt.event.ActionEvent evt) {
        find(false);
    }

    private void miUndoActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } catch (CannotUndoException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    private void miRedoActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } catch (CannotRedoException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    private void miAboutActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Encrypted Notepad "+Main.VERSION+"\n" +
                "Copyright (c) 2010-2014 Ivan Voras <ivoras@gmail.com>\n"+
                "Copyright (c) 2017 github.com/mantun\n"+
                "Released under the BSD License\n" +
                "Project web: https://github.com/mantun/enotes\n\nUsing " + Util.CRYPTO_MODE);
    }


    private javax.swing.JButton btFind;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lbCaret;
    private javax.swing.JMenuItem miAbout;
    private javax.swing.JMenuItem miExit;
    private javax.swing.JMenuItem miFind;
    private javax.swing.JMenuItem miUndo;
    private javax.swing.JMenuItem miRedo;
    private javax.swing.JMenuItem miNew;
    private javax.swing.JMenuItem miOpen;
    private javax.swing.JMenuItem miSave;
    private javax.swing.JMenuItem miSaveAs;
    private javax.swing.JTextField tfFind;
    private javax.swing.JTextPane tp;


    private boolean canExit() {
        return checkSave(WHYSAVE_CLOSE) != OPT_CANCEL;
    }
    

    private void updateTitle() {
        String fn;
        if (docm.filename == null) {
            fn = "New Document";
        } else {
            fn = new File(docm.filename).getName();
        }
        if (docm.modified) {
            fn += "*";
        }
        this.setTitle(fn + " - Encrypted Notepad");
    }


    private void updateCaretStatus() {
        docm.caretPosition = tp.getCaretPosition();
        lbCaret.setText(String.format("L:%d C:%s", tp_line, tp_col));
    }

    private void onDocumentUpdate(DocumentEvent e) {
        if (!docm.modified) {
            docm.modified = true;
            updateTitle();
        }
    }

    /**
     * Returns true if the document was saved or the user said he doesn't want
     * to save it.
     *
     * @return
     */
    private int checkSave(int whySave) {
        if ((whySave == WHYSAVE_SAVE || whySave == WHYSAVE_CLOSE) && !docm.modified) {
            return OPT_NOSAVE;
        }

        if (whySave == WHYSAVE_CLOSE) {
            int opt = JOptionPane.showConfirmDialog(this, "Do you want to save the file " + (docm.filename != null ? docm.filename : ""), "Save file?",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opt == JOptionPane.CANCEL_OPTION) {
                return OPT_CANCEL;
            }
            if (opt == JOptionPane.NO_OPTION) {
                return OPT_NOSAVE;
            }
        }

        File fSave;
        if (whySave == WHYSAVE_SAVEAS || docm.filename == null) {
            JFileChooser fch = new JFileChooser();
            if (docm.filename != null) {
                fch.setCurrentDirectory(new File(docm.filename).getParentFile());
            } else {
                fch.setCurrentDirectory(new File("."));
            }
            fch.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory())
                        return true;
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".txt");
                }
                @Override
                public String getDescription() {
                    return "Plain text files (*.txt)";
                }
            });
            fch.addChoosableFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".etxt");
                }
                @Override
                public String getDescription() {
                    return "Encrypted Notepad files (*.etxt)";
                }
            });
            int ret = fch.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                fSave = fch.getSelectedFile();
                if (!fSave.getName().contains(".")) {
                    fSave = new File(fSave.getAbsolutePath() + ".etxt");
                }
            } else {
                return OPT_NOSAVE;
            }
        } else {
            fSave = new File(docm.filename);
        }

        if (docm.key == null) {
            String pwd = PasswordDialog.getPassword(true);
            if (pwd == null) {
                return OPT_CANCEL;
            }
            docm.setKey(pwd);
        }

        docm.filename = fSave.getAbsolutePath();
        try {
            Doc doc = new Doc(tp.getText(), docm);
            bakFile(NUM_BACKUPS).delete();
            for (int i = NUM_BACKUPS - 1; i > 0; i--) {
                bakFile(i).renameTo(bakFile(i + 1));
            }
            if (!fSave.exists() || fSave.renameTo(bakFile(1))) {
                boolean saved = doc.save(new FileOutputStream(fSave));
                if (saved) {
                    docm.modified = false;
                    updateTitle();
                    return OPT_SAVE;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Save failed, could not create backup file");
            }
            return OPT_CANCEL;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return OPT_CANCEL;
        }
    }

    private File bakFile(int i) {
        return new File(docm.filename + "." + i + ".bak");
    }


    /**
     * Returns true if a file was loaded.
     */
    private boolean openFile() {
        if (checkSave(WHYSAVE_CLOSE) == OPT_CANCEL) {
            return false;
        }

        JFileChooser fch = new JFileChooser();
        if (docm.filename != null) {
            fch.setCurrentDirectory(new File(docm.filename).getParentFile());
        } else {
            fch.setCurrentDirectory(new File("."));
        }
        fch.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".txt");
            }
            @Override
            public String getDescription() {
                return "Plain text files (*.txt)";
            }
        });
        fch.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".etxt");
            }
            @Override
            public String getDescription() {
                return "Encrypted Notepad files (*.etxt)";
            }
        });

        int ret = fch.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File fOpen = fch.getSelectedFile();
        return internalOpenFile(fOpen);
    }


    /*
     * Open a file that's certainly there.
     */
    boolean internalOpenFile(File fOpen) {
        Doc doc;
        try {
            while (true) {
                String pwd = PasswordDialog.getPassword(false);
                if (pwd == null) {
                    return false;
                }
                try (InputStream in = new FileInputStream(fOpen)) {
                    doc = Doc.open(in, pwd);
                    break;
                } catch (DocPasswordException ex) {
                    // continue
                }
            }
        } catch (DocException | FileNotFoundException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return false;
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, "IOException: " + ex.getMessage());
            return false;
        }

        docm = doc.getDocMetadata();
        int caretPos = docm.caretPosition;
        tp.setText(doc.getText()); // modifies docm.caretPosition
        setCaretPosition(caretPos);
        docm.modified = false;
        docm.filename = fOpen.getAbsolutePath();
        docm.caretPosition = caretPos;
        updateTitle();
        return true;
    }


    /**
     * Highlight search words.
     * @param backwards
     */
    private void find(boolean backwards) {
        String findText = tfFind.getText();
        if (searcher != null && !findText.equalsIgnoreCase(searcher.getWord())) {
            searcher.removeHighlights();
            searcher = null;
        }
        if (findText.isEmpty()) {
            return;
        }
        if (searcher == null) {
            searcher = new WordSearcher(tp, findText, tp.getCaretPosition());
        }
        int found = backwards ? searcher.findPrev() : searcher.findNext();
        if (found == -1) {
            JOptionPane.showMessageDialog(this, "Not found: " + findText);
        } else {
            setCaretPosition(found);
        }
    }

    private void setCaretPosition(int caretPosition) {
        try {
            Rectangle r = tp.modelToView(caretPosition);
            JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, tp);
            int extentHeight = viewport.getExtentSize().height;
            int viewHeight = viewport.getViewSize().height;
            int y = Math.max(0, r.y - ((extentHeight - r.height) / 3));
            y = Math.min(y, viewHeight - extentHeight);
            viewport.setViewPosition(new Point(0, y));
            tp.setCaretPosition(caretPosition);
        } catch (BadLocationException ignored) {
            // ignored
        }
    }
}
