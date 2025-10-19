package postman.gui.components;

import postman.gui.PostmanContract;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;

public class MenuPanel extends JMenuBar {

    public MenuPanel(PostmanContract.Presenter mPresenter) {
        super();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        newMenuItem.addActionListener((e) -> {
            mPresenter.onClickCreateNewRequest();
        });

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        openMenuItem.addActionListener((e) -> {
            Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
            FileDialog fileDialog = new FileDialog(frame, "Open", FileDialog.LOAD);
            fileDialog.setDirectory(System.getProperty("user.home"));
            fileDialog.setVisible(true);
            String directory = fileDialog.getDirectory();
            String fileName = fileDialog.getFile();
            if (directory == null || fileName == null) {
                return;
            }
            String filePath = Paths.get(directory, fileName).toString();
            mPresenter.onClickImport(filePath);
        });

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveMenuItem.addActionListener((e) -> {
            Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
            FileDialog fileDialog = new FileDialog(frame, "Save", FileDialog.SAVE);
            fileDialog.setDirectory(System.getProperty("user.home"));
            fileDialog.setVisible(true);
            String directory = fileDialog.getDirectory();
            String fileName = fileDialog.getFile();
            if (directory == null || fileName == null) {
                return;
            }
            String filePath = Paths.get(directory, fileName).toString();
            mPresenter.onClickExport(filePath);
        });

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.addActionListener((e) -> {
            System.exit(0);
        });

        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(exitMenuItem);

        add(fileMenu);
    }
}
