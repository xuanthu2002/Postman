package postman.gui;

import postman.exception.DecompressException;
import postman.exception.URLFormatException;
import postman.gui.constants.Colors;
import postman.gui.constants.Fonts;
import postman.gui.constants.Strings;
import postman.gui.constants.Values;
import postman.util.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

public class PostmanView extends javax.swing.JFrame {

    public PostmanView() {

        UIManager.put("TabbedPane.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("TableHeader.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("Table.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("TextArea.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("RadioButton.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("Label.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("TextField.font", Fonts.GENERAL_PLAIN_12);

        initComponents();

        LookAndFeel previousLF = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            mFileChoose = new JFileChooser();
            UIManager.setLookAndFeel(previousLF);
        } catch (IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException |
                 ClassNotFoundException e) {
            e.printStackTrace();
        }

        initializeRequestUIComponents();
    }

    public static void main(String args[]) {
        new PostmanView().setVisible(true);
    }

    private void initializeRequestUIComponents() {
        configureGeneralTables();
        configureUrlTextField();
        configureRequestParamsTable();
        configureRequestHeadersTable();
        configureRequestBodyActions();
    }

    private void configureUrlTextField() {
        mTextFieldRequestUrl.requestFocus();

        mTextFieldRequestUrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    mButtonSendRequest.doClick();
                    return;
                }
                updateRequestParamsTable();
            }
        });
    }

    private void configureRequestParamsTable() {
        mTableRequestParams.getModel().addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                updateUrlTextField();
                ensureEmptyRowInParamsTable();
            }
        });
    }

    private void configureRequestHeadersTable() {
        mTableRequestHeaders.getModel().addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                ensureEmptyRowInHeadersTable();
            }
        });
    }

    private void configureRequestBodyActions() {
        mRadioButtonRequestBodyNone.addActionListener((ActionEvent e) -> configureRequestBodyType("none", "none"));
        mRadioButtonRequestBodyText.addActionListener((ActionEvent e) -> configureRequestBodyType("text/plain", "text"));
        mRadioButtonRequestBodyJson.addActionListener((ActionEvent e) -> configureRequestBodyType("application/json", "text"));
        mRadioButtonBodyBinary.addActionListener((ActionEvent e) -> configureRequestBodyType("application/octet-stream", "binary"));
    }

    private void updateRequestParamsTable() {
        Map<String, String> params = HttpUtils.extractParams(mTextFieldRequestUrl.getText().trim());
        DefaultTableModel model = (DefaultTableModel) mTableRequestParams.getModel();
        model.setRowCount(0);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            model.addRow(new Object[]{true, entry.getKey(), entry.getValue()});
        }
        if (model.getRowCount() < 1) {
            model.addRow(new Object[]{false, "", ""});
        } else {
            ensureEmptyRowInParamsTable();
        }
        mTableRequestParams.setModel(model);
    }

    private void updateUrlTextField() {
        DefaultTableModel model = (DefaultTableModel) mTableRequestParams.getModel();
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < model.getRowCount(); i++) {
            String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
            String value = Optional.ofNullable(model.getValueAt(i, 2)).orElse("").toString();
            boolean check = (boolean) Optional.ofNullable(model.getValueAt(i, 0)).orElse(false);
            if (check) {
                query.append("&").append(key.isEmpty() ? "" : key + "=").append(value);
            }
        }
        String url = Optional.ofNullable(mTextFieldRequestUrl.getText()).orElse("");
        url = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        if (!query.isEmpty()) {
            url += "?" + query.substring(1);
        }
        mTextFieldRequestUrl.setText(url);
    }

    private void ensureEmptyRowInParamsTable() {
        DefaultTableModel model = (DefaultTableModel) mTableRequestParams.getModel();
        if ((boolean) Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 0)).orElse(false)
                || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 1)).orElse("").toString().isEmpty()
                || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 2)).orElse("").toString().isEmpty()) {
            model.addRow(new Object[]{false, "", ""});
        }
    }

    private void ensureEmptyRowInHeadersTable() {
        DefaultTableModel model = (DefaultTableModel) mTableRequestHeaders.getModel();
        if ((boolean) Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 0)).orElse(false)
                || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 1)).orElse("").toString().isEmpty()
                || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 2)).orElse("").toString().isEmpty()) {
            model.addRow(new Object[]{false, "", ""});
        }
    }

    private void showRequestBodyPanel(String panelName) {
        CardLayout cardLayout = (CardLayout) mPanelRequestBodyDetail.getLayout();
        cardLayout.show(mPanelRequestBodyDetail, panelName);
    }

    private void configureRequestBodyType(String contentType, String panelName) {
        showRequestBodyPanel(panelName);
        DefaultTableModel model = (DefaultTableModel) mTableRequestHeaders.getModel();

        if (contentType.equalsIgnoreCase("none")) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                if (key.equals("Content-Type")) {
                    model.removeRow(i);
                    break;
                }
            }
        } else {
            boolean found = false;
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                if (key.equals("Content-Type")) {
                    model.setValueAt(contentType, i, 2);
                    found = true;
                    break;
                }
            }
            if (!found) {
                model.removeRow(model.getRowCount() - 1);
                model.addRow(new Object[]{true, "Content-Type", contentType});
                model.addRow(new Object[]{false, "", ""});
            }
        }

        mTableRequestHeaders.setModel(model);
    }

    private void configureGeneralTables() {
        JTextField cell = new JTextField();
        cell.setFont(Fonts.GENERAL_PLAIN_12);
        DefaultCellEditor singleClick = new DefaultCellEditor(cell);
        singleClick.setClickCountToStart(1);

        for (int i = 1; i < mTableRequestParams.getColumnCount(); i++) {
            mTableRequestParams.setDefaultEditor(mTableRequestParams.getColumnClass(i), singleClick);
        }
        for (int i = 1; i < mTableRequestHeaders.getColumnCount(); i++) {
            mTableRequestHeaders.setDefaultEditor(mTableRequestHeaders.getColumnClass(i), singleClick);
        }
        for (int i = 1; i < mTableResponseHeaders.getColumnCount(); i++) {
            mTableResponseHeaders.setDefaultEditor(mTableResponseHeaders.getColumnClass(i), singleClick);
        }
        for (int i = 1; i < mTableResponseCookies.getColumnCount(); i++) {
            mTableResponseCookies.setDefaultEditor(mTableResponseCookies.getColumnClass(i), singleClick);
        }

        mTableRequestParams.putClientProperty("terminateEditOnFocusLost", true);
        mTableRequestHeaders.putClientProperty("terminateEditOnFocusLost", true);
        mTableResponseCookies.putClientProperty("terminateEditOnFocusLost", true);
        mTableResponseHeaders.putClientProperty("terminateEditOnFocusLost", true);
    }

    private void initComponents() {
        mButtonGroupRequestBodyType = new javax.swing.ButtonGroup();
        mFileChoose = new javax.swing.JFileChooser();
        mPanelMenuAbove = new javax.swing.JPanel();
        mButtonNewRequest = new javax.swing.JButton();
        mButtonImportRequest = new javax.swing.JButton();
        mButtonExportRequest = new javax.swing.JButton();
        mPanelRequest = new javax.swing.JPanel();
        mPanelRequestUrl = new javax.swing.JPanel();
        mComboBoxRequestMethod = new javax.swing.JComboBox<>();
        mTextFieldRequestUrl = new javax.swing.JTextField();
        mButtonSendRequest = new javax.swing.JButton();
        mPanelRequestDetail = new javax.swing.JTabbedPane();
        mPanelRequestParams = new javax.swing.JPanel();
        scrollPaneRequestParams = new javax.swing.JScrollPane();
        mTableRequestParams = new javax.swing.JTable();
        mPanelRequestHeaders = new javax.swing.JPanel();
        scrollPaneRequestHeaders = new javax.swing.JScrollPane();
        mTableRequestHeaders = new javax.swing.JTable();
        mPanelRequestBody = new javax.swing.JPanel();
        mPanelRequestBodyType = new javax.swing.JPanel();
        mRadioButtonRequestBodyNone = new javax.swing.JRadioButton();
        mRadioButtonRequestBodyText = new javax.swing.JRadioButton();
        mRadioButtonRequestBodyJson = new javax.swing.JRadioButton();
        mRadioButtonBodyBinary = new javax.swing.JRadioButton();
        mPanelRequestBodyDetail = new javax.swing.JPanel();
        mPanelRequestBodyNone = new javax.swing.JPanel();
        mPanelRequestBodyText = new javax.swing.JPanel();
        mPanelRequestBodyBinary = new javax.swing.JPanel();
        mButtonChooseInputFile = new javax.swing.JButton();
        mLabelUploadedFile = new javax.swing.JLabel();
        mPanelRequestBodyText = new javax.swing.JPanel();
        scrollPaneRequestBodyText = new javax.swing.JScrollPane();
        mTextAreaBodyText = new javax.swing.JTextArea();
        mPanelResponse = new javax.swing.JPanel();
        mPanelResponseStatus = new javax.swing.JPanel();
        mLabelResponseStatus = new javax.swing.JLabel();
        mPanelResponseDetail = new javax.swing.JPanel();
        mTabbedPaneResponse = new javax.swing.JTabbedPane();
        mPanelResponseBody = new javax.swing.JPanel();
        scrollPaneResponseBody = new javax.swing.JScrollPane();
        mTextAreaResponseBody = new javax.swing.JTextArea();
        mPanelResponseCookies = new javax.swing.JPanel();
        scrollPaneResponseCookies = new javax.swing.JScrollPane();
        mTableResponseCookies = new javax.swing.JTable();
        mPanelResponseHeaders = new javax.swing.JPanel();
        scrollPaneResponseHeaders = new javax.swing.JScrollPane();
        mTableResponseHeaders = new javax.swing.JTable();

        setTitle(Strings.APP_NAME);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        setupMenuAbove();

        setupPanelRequest();

        setupPanelResponse();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setTopComponent(mPanelRequest);
        splitPane.setBottomComponent(mPanelResponse);

        getContentPane().add(mPanelMenuAbove, BorderLayout.PAGE_START);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        pack();
        setSize(Values.DEFAULT_LAUNCH_SIZE);
        setLocationRelativeTo(null);
    }

    private void setupPanelResponse() {
        mPanelResponse.setLayout(new BorderLayout());

        setupPanelResponseStatus();
        mPanelResponse.add(mPanelResponseStatus, BorderLayout.PAGE_START);

        setupPanelResponseDetail();
        mPanelResponse.add(mPanelResponseDetail, BorderLayout.CENTER);
    }

    private void setupPanelResponseDetail() {
        mPanelResponseDetail.setLayout(new BorderLayout());

        mTabbedPaneResponse.setFont(Fonts.GENERAL_PLAIN_12);

        setupPanelResponseBody();
        mTabbedPaneResponse.addTab("Body", mPanelResponseBody);

        setupPanelResponseCookies();
        mTabbedPaneResponse.addTab("Cookies", mPanelResponseCookies);

        setupPanelResponseHeaders();
        mTabbedPaneResponse.addTab("Headers", mPanelResponseHeaders);

        mPanelResponseDetail.add(mTabbedPaneResponse, BorderLayout.CENTER);
    }

    private void setupPanelResponseHeaders() {
        mPanelResponseHeaders.setLayout(new BorderLayout());

        mTableResponseHeaders.setModel(new DefaultTableModel(
                new Object[][]{
                        {null, null}
                },
                new String[]{
                        "Key", "Value"
                }
        ) {
            Class[] types = new Class[]{
                    String.class, String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });
        mTableResponseHeaders.setCellSelectionEnabled(true);
        mTableResponseHeaders.setRowHeight(Values.DEFAULT_TABLE_ROW_HEIGHT);
        scrollPaneResponseHeaders.setViewportView(mTableResponseHeaders);

        mPanelResponseHeaders.add(scrollPaneResponseHeaders, BorderLayout.CENTER);
    }

    private void setupPanelResponseCookies() {
        mPanelResponseCookies.setLayout(new BoxLayout(mPanelResponseCookies, BoxLayout.LINE_AXIS));

        mTableResponseCookies.setModel(new DefaultTableModel(
                new Object[][]{
                        {null, null, null, null, null, null, null}
                },
                new String[]{
                        "Name", "Value", "Domain", "Path", "Expires", "HttpOnly", "Secure"
                }
        ) {
            Class[] types = new Class[]{
                    String.class, String.class, String.class, String.class, String.class, String.class, String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });
        mTableResponseCookies.setCellSelectionEnabled(true);
        mTableResponseCookies.setRowHeight(Values.DEFAULT_TABLE_ROW_HEIGHT);
        scrollPaneResponseCookies.setViewportView(mTableResponseCookies);

        mPanelResponseCookies.add(scrollPaneResponseCookies);
    }

    private void setupPanelResponseBody() {
        mPanelResponseBody.setLayout(new BorderLayout());

        mTextAreaResponseBody.setFont(Fonts.GENERAL_PLAIN_12);
        mTextAreaResponseBody.setTabSize(4);
        mTextAreaResponseBody.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        scrollPaneResponseBody.setViewportView(mTextAreaResponseBody);

        mPanelResponseBody.add(scrollPaneResponseBody, BorderLayout.CENTER);
    }

    private void setupPanelResponseStatus() {
        mLabelResponseStatus.setBackground(Colors.WHITE_COLOR);
        mLabelResponseStatus.setFont(Fonts.GENERAL_PLAIN_12);
        mLabelResponseStatus.setForeground(Colors.GREEN_COLOR);
        mLabelResponseStatus.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
        mLabelResponseStatus.setOpaque(true);

        GroupLayout mPanelResponseStatusLayout = new GroupLayout(mPanelResponseStatus);
        mPanelResponseStatus.setLayout(mPanelResponseStatusLayout);
        mPanelResponseStatusLayout.setHorizontalGroup(
                mPanelResponseStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(mPanelResponseStatusLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(mLabelResponseStatus, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                                .addContainerGap())
        );
        mPanelResponseStatusLayout.setVerticalGroup(
                mPanelResponseStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, mPanelResponseStatusLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(mLabelResponseStatus, GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                                .addContainerGap())
        );
    }

    private void setupPanelRequest() {
        mPanelRequest.setLayout(new BorderLayout());

        setupPanelRequestUrl();
        mPanelRequest.add(mPanelRequestUrl, BorderLayout.PAGE_START);

        setupPanelRequestDetail();
        mPanelRequest.add(mPanelRequestDetail, BorderLayout.CENTER);
    }

    private void setupPanelRequestDetail() {
        mPanelRequestDetail.setFont(Fonts.GENERAL_PLAIN_12);

        setupPanelRequestParams();
        mPanelRequestDetail.addTab("Params", mPanelRequestParams);

        setupPanelRequestHeaders();
        mPanelRequestDetail.addTab("Headers", mPanelRequestHeaders);

        setupPanelRequestBody();
        mPanelRequestDetail.addTab("Body", mPanelRequestBody);
    }

    private void setupPanelRequestBody() {
        mPanelRequestBody.setLayout(new BorderLayout());

        mPanelRequestBodyType.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 5));

        mButtonGroupRequestBodyType.add(mRadioButtonRequestBodyNone);
        mRadioButtonRequestBodyNone.setSelected(true);
        mRadioButtonRequestBodyNone.setText("none");
        mRadioButtonRequestBodyNone.setFocusPainted(false);
        mPanelRequestBodyType.add(mRadioButtonRequestBodyNone);

        mButtonGroupRequestBodyType.add(mRadioButtonRequestBodyText);
        mRadioButtonRequestBodyText.setText("text");
        mRadioButtonRequestBodyText.setFocusPainted(false);
        mPanelRequestBodyType.add(mRadioButtonRequestBodyText);

        mButtonGroupRequestBodyType.add(mRadioButtonRequestBodyJson);
        mRadioButtonRequestBodyJson.setText("json");
        mRadioButtonRequestBodyJson.setFocusPainted(false);
        mPanelRequestBodyType.add(mRadioButtonRequestBodyJson);

        mButtonGroupRequestBodyType.add(mRadioButtonBodyBinary);
        mRadioButtonBodyBinary.setText("binary");
        mRadioButtonBodyBinary.setFocusPainted(false);
        mPanelRequestBodyType.add(mRadioButtonBodyBinary);

        mPanelRequestBody.add(mPanelRequestBodyType, BorderLayout.PAGE_START);

        mPanelRequestBodyDetail.setLayout(new CardLayout());

        GroupLayout mPanelRequestBodyNoneLayout = new GroupLayout(mPanelRequestBodyNone);
        mPanelRequestBodyNone.setLayout(mPanelRequestBodyNoneLayout);
        mPanelRequestBodyNoneLayout.setHorizontalGroup(
                mPanelRequestBodyNoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        mPanelRequestBodyNoneLayout.setVerticalGroup(
                mPanelRequestBodyNoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        mPanelRequestBodyDetail.add(mPanelRequestBodyNone, "none");

        mTextAreaBodyText.setFont(Fonts.GENERAL_PLAIN_12);
        mTextAreaBodyText.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        scrollPaneRequestBodyText.setViewportView(mTextAreaBodyText);
        mPanelRequestBodyText.setLayout(new BorderLayout());
        mPanelRequestBodyText.add(scrollPaneRequestBodyText, BorderLayout.CENTER);
        mPanelRequestBodyDetail.add(mPanelRequestBodyText, "text");

        mPanelRequestBodyBinary.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mPanelRequestBodyBinary.setLayout(new BoxLayout(mPanelRequestBodyBinary, BoxLayout.PAGE_AXIS));

        mButtonChooseInputFile.setFont(Fonts.GENERAL_PLAIN_12);
        mButtonChooseInputFile.setText(Strings.SELECT_FILE);
        mButtonChooseInputFile.setFocusPainted(false);
        mButtonChooseInputFile.addActionListener(evt -> mButtonChooseInputFileActionPerformed());
        mPanelRequestBodyBinary.add(mButtonChooseInputFile);

        mLabelUploadedFile.setBorder(BorderFactory.createEmptyBorder(16, 1, 1, 1));
        mPanelRequestBodyBinary.add(mLabelUploadedFile);

        mPanelRequestBodyDetail.add(mPanelRequestBodyBinary, "binary");

        mPanelRequestBody.add(mPanelRequestBodyDetail, BorderLayout.CENTER);
    }

    private void setupPanelRequestHeaders() {
        mPanelRequestHeaders.setLayout(new BorderLayout());

        mTableRequestHeaders.setModel(new DefaultTableModel(
                new Object[][]{
                        {Boolean.TRUE, "User-Agent", "Postman"},
                        {Boolean.TRUE, "Accept", "*/*"},
                        {Boolean.TRUE, "Accept-Encoding", "gzip, deflate, br"},
                        {Boolean.FALSE, "", ""}
                },
                new String[]{
                        "", "Key", "Value"
                }
        ) {
            Class[] types = new Class[]{
                    Boolean.class, String.class, String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });
        mTableRequestHeaders.setCellSelectionEnabled(true);
        mTableRequestHeaders.setRowHeight(Values.DEFAULT_TABLE_ROW_HEIGHT);
        scrollPaneRequestHeaders.setViewportView(mTableRequestHeaders);

        mPanelRequestHeaders.add(scrollPaneRequestHeaders, BorderLayout.CENTER);
    }

    private void setupPanelRequestParams() {
        mPanelRequestParams.setLayout(new BorderLayout());

        mTableRequestParams.setModel(new DefaultTableModel(
                new Object[][]{
                        {false, "", ""}
                },
                new String[]{
                        "", "Key", "Value"
                }
        ) {
            Class[] types = new Class[]{
                    Boolean.class, String.class, String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });
        mTableRequestParams.setCellSelectionEnabled(true);
        mTableRequestParams.setRowHeight(Values.DEFAULT_TABLE_ROW_HEIGHT);
        scrollPaneRequestParams.setViewportView(mTableRequestParams);

        mPanelRequestParams.add(scrollPaneRequestParams, BorderLayout.CENTER);
    }

    private void setupPanelRequestUrl() {
        mComboBoxRequestMethod.setFont(Fonts.GENERAL_BOLD_12);
        mComboBoxRequestMethod.setModel(new DefaultComboBoxModel(HttpMethod.values()));

        mTextFieldRequestUrl.setFont(Fonts.GENERAL_PLAIN_12);
        mTextFieldRequestUrl.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));

        mButtonSendRequest.setBackground(Colors.BLUE_COLOR);
        mButtonSendRequest.setFont(Fonts.GENERAL_BOLD_12);
        mButtonSendRequest.setForeground(Colors.WHITE_COLOR);
        mButtonSendRequest.setText(Strings.SEND);
        mButtonSendRequest.setFocusPainted(false);
        mButtonSendRequest.addActionListener(evt -> mButtonSendRequestActionPerformed());

        GroupLayout mPanelRequestUrlLayout = new GroupLayout(mPanelRequestUrl);
        mPanelRequestUrl.setLayout(mPanelRequestUrlLayout);
        mPanelRequestUrlLayout.setHorizontalGroup(
                mPanelRequestUrlLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(mPanelRequestUrlLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(mComboBoxRequestMethod, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mTextFieldRequestUrl, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mButtonSendRequest)
                                .addContainerGap())
        );
        mPanelRequestUrlLayout.setVerticalGroup(
                mPanelRequestUrlLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(mPanelRequestUrlLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(mPanelRequestUrlLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(mComboBoxRequestMethod, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(mButtonSendRequest, GroupLayout.Alignment.TRAILING, Values.BUTTON_SEND_HEIGHT, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(mTextFieldRequestUrl, GroupLayout.Alignment.TRAILING))
                                .addContainerGap())
        );
    }

    private void setupMenuAbove() {
        mPanelMenuAbove.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 4));

        mButtonNewRequest.setFont(Fonts.GENERAL_PLAIN_12);
        mButtonNewRequest.setText(Strings.NEW);
        mButtonNewRequest.setFocusPainted(false);
        mButtonNewRequest.addActionListener(evt -> mButtonNewRequestActionPerformed());
        mPanelMenuAbove.add(mButtonNewRequest);

        mButtonImportRequest.setFont(Fonts.GENERAL_PLAIN_12);
        mButtonImportRequest.setText(Strings.IMPORT);
        mButtonImportRequest.setFocusPainted(false);
        mButtonImportRequest.addActionListener(evt -> mButtonImportRequestActionPerformed());
        mPanelMenuAbove.add(mButtonImportRequest);

        mButtonExportRequest.setFont(Fonts.GENERAL_PLAIN_12);
        mButtonExportRequest.setText(Strings.EXPORT);
        mButtonExportRequest.setFocusPainted(false);
        mButtonExportRequest.addActionListener(evt -> mButtonExportRequestActionPerformed());
        mPanelMenuAbove.add(mButtonExportRequest);
    }

    private void mButtonSendRequestActionPerformed() {
        resetOutput();

        HttpRequest httpRequest = createRequest();

        try {
            HttpResponse httpResponse = new HttpClient().send(httpRequest);

            String charset = "utf8";
            for (String i : Optional.ofNullable(httpResponse.getHeader("Content-Type")).orElse(new ArrayList<>())) {
                if (i.toLowerCase().contains("charset")) {
                    charset = i.substring(i.indexOf("charset=") + 8);
                }
            }

            String contentEncoding = Optional
                    .ofNullable(httpResponse.getHeader("Content-Encoding"))
                    .orElse(List.of("")).get(0);

            switch (contentEncoding.toLowerCase()) {
                case "gzip" ->
                        mTextAreaResponseBody.setText(Decompress.decompressGzip(httpResponse.getBody(), charset));
                case "deflate" ->
                        mTextAreaResponseBody.setText(Decompress.decompressDeflate(httpResponse.getBody(), charset));
                case "br" ->
                        mTextAreaResponseBody.setText(Decompress.decompressBrotli(httpResponse.getBody(), charset));
                default -> mTextAreaResponseBody.setText(new String(httpResponse.getBody(), charset));
            }

            mTextAreaResponseBody.setCaretPosition(0);
            mLabelResponseStatus.setText(httpResponse.getStatusCode() + " " + httpResponse.getStatusMessage());

            if (httpResponse.getHeader("Content-Type").get(0).equals("image/png")) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter("png", "PNG");
                mFileChoose.setFileFilter(filter);
                mFileChoose.setSelectedFile(new File(""));
                int x = mFileChoose.showSaveDialog(this);

                if (x == JFileChooser.APPROVE_OPTION) {
                    String direct = mFileChoose.getSelectedFile().toString();
                    if (!direct.endsWith(".png")) {
                        direct += ".png";
                    }
                    File file = new File(direct);
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        fileOutputStream.write(httpResponse.getBody());
                    }
                }
            }

            DefaultTableModel model = (DefaultTableModel) mTableResponseHeaders.getModel();
            model.setRowCount(0);
            for (Map.Entry<String, List<String>> entry : httpResponse.getHeaders().entrySet()) {
                for (String value : entry.getValue()) {
                    model.addRow(new Object[]{entry.getKey(), value});
                }
            }
            mTableResponseHeaders.setModel(model);

            DefaultTableModel modelCookieTable = (DefaultTableModel) mTableResponseCookies.getModel();
            modelCookieTable.setRowCount(0);
            for (Cookie cookie : httpResponse.getCookies()) {
                modelCookieTable.addRow(
                        new Object[]{
                                cookie.getKey(),
                                cookie.getValue(),
                                cookie.getDomain(),
                                cookie.getPath(),
                                cookie.getExpires(),
                                cookie.isHttpOnly(),
                                cookie.isSecure()
                        }
                );
            }
            mTableResponseCookies.setModel(modelCookieTable);

        } catch (HeadlessException | IOException | URLFormatException | DecompressException | DataFormatException ex) {
            Logger.getLogger(PostmanView.class.getName()).log(Level.SEVERE, null, ex);
            mLabelResponseStatus.setText(ex.getClass().getSimpleName());
            mTextAreaResponseBody.setText(ex.getMessage());
        }

    }

    private HttpRequest createRequest() {
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setMethod((HttpMethod) mComboBoxRequestMethod.getSelectedItem());
        httpRequest.setUrl(Optional.ofNullable(mTextFieldRequestUrl.getText()).orElse(""));
        if (!mRadioButtonRequestBodyNone.isSelected()) {
            httpRequest.setBody(Optional.ofNullable(mTextAreaBodyText.getText()).orElse("").getBytes());
            httpRequest.addHeader("Content-Length", String.valueOf(httpRequest.getBody().length));
        }

        if (mRadioButtonBodyBinary.isSelected()) {
            httpRequest.setBody(mLabelUploadedFile.getText().getBytes());
            File file = new File(mLabelUploadedFile.getText());
            httpRequest.addHeader("Content-Length", String.valueOf(file.length()));
        }

        DefaultTableModel modelHeaders = (DefaultTableModel) mTableRequestHeaders.getModel();
        for (int i = 0; i < modelHeaders.getRowCount(); i++) {
            String key = Optional.ofNullable(modelHeaders.getValueAt(i, 1)).orElse("").toString();
            String value = Optional.ofNullable(modelHeaders.getValueAt(i, 2)).orElse("").toString();
            boolean check = (boolean) Optional.ofNullable(modelHeaders.getValueAt(i, 0)).orElse(false);
            if (check && !key.isEmpty() && !value.isEmpty()) {
                httpRequest.addHeader(key, value);
            }
        }

        return httpRequest;
    }

    private void resetOutput() {
        mLabelResponseStatus.setText("");
        mTextAreaResponseBody.setText("");
        DefaultTableModel model = (DefaultTableModel) mTableResponseCookies.getModel();
        model.setRowCount(0);
        mTableResponseCookies.setModel(model);

        model = (DefaultTableModel) mTableResponseHeaders.getModel();
        model.setRowCount(0);
        mTableResponseHeaders.setModel(model);
    }

    private void resetInput() {
        mComboBoxRequestMethod.setSelectedIndex(0);

        mTextFieldRequestUrl.setText("");

        DefaultTableModel model = (DefaultTableModel) mTableRequestParams.getModel();
        model.setRowCount(0);
        model.addRow(new Object[]{false, "", ""});
        mTableRequestParams.setModel(model);

        model = (DefaultTableModel) mTableRequestHeaders.getModel();
        model.setRowCount(0);
        model.addRow(new Object[]{true, "User-Agent", "Postman"});
        model.addRow(new Object[]{true, "Accept", "*/*"});
        model.addRow(new Object[]{false, "Accept-Encoding", "gzip, deflate, br"});
        model.addRow(new Object[]{false, "", ""});
        mTableRequestHeaders.setModel(model);

        mRadioButtonRequestBodyNone.doClick();
    }

    private void mButtonNewRequestActionPerformed() {
        resetInput();
        resetOutput();
        mTextFieldRequestUrl.requestFocus();
    }

    private void mButtonImportRequestActionPerformed() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("req", "req");
        mFileChoose.setFileFilter(filter);
        mFileChoose.setSelectedFile(new File(""));
        int x = mFileChoose.showOpenDialog(null);
        if (x == JFileChooser.APPROVE_OPTION) {
            String direct = mFileChoose.getSelectedFile().toString();
            HttpRequestStorage httpRequestStorage = Storage.importRequest(direct);

            if (httpRequestStorage == null) {
                mButtonNewRequest.doClick();
                return;
            }

            mComboBoxRequestMethod.setSelectedItem(httpRequestStorage.getMethod());

            mTextFieldRequestUrl.setText(httpRequestStorage.getUrl());

            DefaultTableModel model = (DefaultTableModel) mTableRequestParams.getModel();
            model.setRowCount(0);
            for (Object[] i : httpRequestStorage.getParams()) {
                model.addRow(i);
            }
            mTableRequestParams.setModel(model);

            model = (DefaultTableModel) mTableRequestHeaders.getModel();
            model.setRowCount(0);
            for (Object[] i : httpRequestStorage.getHeaders()) {
                model.addRow(i);
            }
            mTableRequestHeaders.setModel(model);

            mTextAreaBodyText.setText(httpRequestStorage.getBody());

            List<AbstractButton> listButton = Collections.list(mButtonGroupRequestBodyType.getElements());

            for (AbstractButton button : listButton) {
                if (button.getText().equals(httpRequestStorage.getTypeBody())) {
                    button.doClick();
                }
            }

            mLabelUploadedFile.setText(httpRequestStorage.getFileName());
        }
    }

    private void mButtonExportRequestActionPerformed() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("req", "req");
        mFileChoose.setFileFilter(filter);
        mFileChoose.setSelectedFile(new File(""));
        int x = mFileChoose.showSaveDialog(null);
        if (x == JFileChooser.APPROVE_OPTION) {
            String direct = mFileChoose.getSelectedFile().toString();
            if (!direct.endsWith(".req")) {
                direct += ".req";
            }
            HttpRequestStorage httpRequestStorage = new HttpRequestStorage();
            httpRequestStorage.setMethod((HttpMethod) mComboBoxRequestMethod.getSelectedItem());
            httpRequestStorage.setUrl(Optional.ofNullable(mTextFieldRequestUrl.getText()).orElse(""));

            httpRequestStorage.setBody(mTextAreaBodyText.getText());

            DefaultTableModel model = (DefaultTableModel) mTableRequestParams.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                String value = Optional.ofNullable(model.getValueAt(i, 2)).orElse("").toString();
                boolean check = (boolean) Optional.ofNullable(model.getValueAt(i, 0)).orElse(false);
                httpRequestStorage.addParam(new Object[]{check, key, value});
            }

            model = (DefaultTableModel) mTableRequestHeaders.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                String value = Optional.ofNullable(model.getValueAt(i, 2)).orElse("").toString();
                boolean check = (boolean) Optional.ofNullable(model.getValueAt(i, 0)).orElse(false);
                httpRequestStorage.addHeader(new Object[]{check, key, value});
            }

            List<AbstractButton> listButton = Collections.list(mButtonGroupRequestBodyType.getElements());

            for (AbstractButton button : listButton) {
                if (button.isSelected()) {
                    httpRequestStorage.setTypeBody(button.getText());
                }
            }

            httpRequestStorage.setFileName(Optional.ofNullable(mLabelUploadedFile.getText()).orElse(""));

            Storage.exportRequest(httpRequestStorage, direct);
        }
    }

    private void mButtonChooseInputFileActionPerformed() {
        int x = mFileChoose.showOpenDialog(this);
        if (x == JFileChooser.APPROVE_OPTION) {
            String direction = mFileChoose.getSelectedFile().toString();
            mLabelUploadedFile.setText(direction);
            String contentType = URLConnection.guessContentTypeFromName(new File(direction).getName());
            DefaultTableModel model = (DefaultTableModel) mTableRequestHeaders.getModel();
            boolean check = true;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 1).equals("Content-Type")) {
                    model.setValueAt(contentType, i, 2);
                    check = false;
                }
            }
            if (check) {
                model.removeRow(model.getRowCount() - 1);
                model.addRow(new Object[]{true, "Content-Type", contentType});
                model.addRow(new Object[]{false, "", ""});
            }
            mTableRequestHeaders.setModel(model);
        }
    }

    private JButton mButtonChooseInputFile;
    private JFileChooser mFileChoose;
    private JPanel mPanelMenuAbove;
    private JTextArea mTextAreaBodyText;
    private JLabel mLabelUploadedFile;
    private JTable mTableRequestHeaders;
    private JTable mTableRequestParams;
    private JPanel mPanelRequestUrl;
    private JTextField mTextFieldRequestUrl;
    private JPanel mPanelRequestBody;
    private JTabbedPane mPanelRequestDetail;
    private JPanel mPanelRequestHeaders;
    private JPanel mPanelRequest;
    private JPanel mPanelRequestParams;
    private JPanel mPanelRequestBodyNone;
    private JPanel mPanelRequestBodyText;
    private JPanel mPanelRequestBodyBinary;
    private JPanel mPanelResponseCookies;
    private JScrollPane scrollPaneRequestParams;
    private JScrollPane scrollPaneResponseHeaders;
    private JScrollPane scrollPaneRequestHeaders;
    private JScrollPane scrollPaneResponseBody;
    private JScrollPane scrollPaneResponseCookies;
    private JScrollPane scrollPaneRequestBodyText;
    private JTabbedPane mTabbedPaneResponse;
    private JComboBox<String> mComboBoxRequestMethod;
    private JPanel mPanelResponseBody;
    private JTextArea mTextAreaResponseBody;
    private JTable mTableResponseCookies;
    private JPanel mPanelResponseDetail;
    private JPanel mPanelResponseHeaders;
    private JTable mTableResponseHeaders;
    private JLabel mLabelResponseStatus;
    private JPanel mPanelResponseStatus;
    private JPanel mPanelResponse;
    private JPanel mPanelRequestBodyDetail;
    private JButton mButtonSendRequest;
    private JButton mButtonExportRequest;
    private JButton mButtonImportRequest;
    private JButton mButtonNewRequest;
    private JRadioButton mRadioButtonBodyBinary;
    private ButtonGroup mButtonGroupRequestBodyType;
    private JRadioButton mRadioButtonRequestBodyJson;
    private JRadioButton mRadioButtonRequestBodyNone;
    private JPanel mPanelRequestBodyType;
    private JRadioButton mRadioButtonRequestBodyText;
}
