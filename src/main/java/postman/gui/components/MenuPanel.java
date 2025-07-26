package postman.gui.components;

import postman.gui.PostmanContract;
import postman.gui.constants.Fonts;
import postman.gui.constants.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPanel extends JPanel implements ActionListener {
    private final PostmanContract.Presenter mPresenter;

    private final JButton mButtonNewRequest;
    private final JButton mButtonImportRequest;
    private final JButton mButtonExportRequest;

    public MenuPanel(PostmanContract.Presenter mPresenter) {
        super();
        this.mPresenter = mPresenter;

        mButtonNewRequest = new JButton();
        mButtonImportRequest = new JButton();
        mButtonExportRequest = new JButton();

        initComponents();
    }

    private void initComponents() {
        this.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 4));

        mButtonNewRequest.setFont(Fonts.GENERAL_PLAIN_12);
        mButtonNewRequest.setText(Strings.NEW);
        mButtonNewRequest.setFocusPainted(false);
        mButtonNewRequest.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mButtonNewRequest.addActionListener(this);
        this.add(mButtonNewRequest);

        mButtonImportRequest.setFont(Fonts.GENERAL_PLAIN_12);
        mButtonImportRequest.setText(Strings.IMPORT);
        mButtonImportRequest.setFocusPainted(false);
        mButtonImportRequest.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mButtonImportRequest.addActionListener(this);
        this.add(mButtonImportRequest);

        mButtonExportRequest.setFont(Fonts.GENERAL_PLAIN_12);
        mButtonExportRequest.setText(Strings.EXPORT);
        mButtonExportRequest.setFocusPainted(false);
        mButtonExportRequest.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mButtonExportRequest.addActionListener(this);
        this.add(mButtonExportRequest);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton clickedButton = (JButton) e.getSource();
        if (clickedButton.equals(mButtonNewRequest)) {
            mPresenter.onClickCreateNewRequest();
        } else if (clickedButton.equals(mButtonImportRequest)) {
            mPresenter.onClickImport();
        } else if (clickedButton.equals(mButtonExportRequest)) {
            mPresenter.onClickExport();
        }
    }
}
