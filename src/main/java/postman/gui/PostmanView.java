package postman.gui;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import postman.exception.DecompressException;
import postman.exception.URLFormatException;
import postman.service.Decompress;
import postman.service.HttpClient;
import postman.service.Storage;
import postman.util.Cookie;
import postman.util.HttpMethod;
import postman.util.HttpRequest;
import postman.util.HttpRequestStorage;
import postman.util.HttpResponse;
import postman.util.HttpUrl;

public class PostmanView extends javax.swing.JFrame {

    public PostmanView() {

        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 20));

        initComponents();

        LookAndFeel previousLF = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            fileChooser = new JFileChooser();
            UIManager.setLookAndFeel(previousLF);
        } catch (IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException | ClassNotFoundException e) {
        }

        JTextField cell = new JTextField();
        cell.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        DefaultCellEditor singleclick = new DefaultCellEditor(cell);
        singleclick.setClickCountToStart(1);
        //set the editor as default on every column
        for (int i = 1; i < inParamsTable.getColumnCount(); i++) {
            inParamsTable.setDefaultEditor(inParamsTable.getColumnClass(i), singleclick);
        }
        for (int i = 1; i < inHeadersTable.getColumnCount(); i++) {
            inHeadersTable.setDefaultEditor(inHeadersTable.getColumnClass(i), singleclick);
        }
        for (int i = 1; i < outHeadersTable.getColumnCount(); i++) {
            outHeadersTable.setDefaultEditor(outHeadersTable.getColumnClass(i), singleclick);
        }
        for (int i = 1; i < outCookiesTable.getColumnCount(); i++) {
            outCookiesTable.setDefaultEditor(outCookiesTable.getColumnClass(i), singleclick);
        }

        inParamsTable.putClientProperty("terminateEditOnFocusLost", true);
        inHeadersTable.putClientProperty("terminateEditOnFocusLost", true);
        outCookiesTable.putClientProperty("terminateEditOnFocusLost", true);
        outHeadersTable.putClientProperty("terminateEditOnFocusLost", true);

        inUrlTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                    return;
                }
                Map<String, String> params = HttpUrl.extractParams(inUrlTxt.getText().trim());
                DefaultTableModel model = (DefaultTableModel) inParamsTable.getModel();
                model.setRowCount(0);
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    model.addRow(new Object[]{true, entry.getKey(), entry.getValue()});
                }
                if (model.getRowCount() < 1) {
                    model.addRow(new Object[]{false, "", ""});
                } else if ((boolean) Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 0)).orElse(false)
                        || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 1)).orElse("").toString().isEmpty()
                        || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 2)).orElse("").toString().isEmpty()) {
                    model.addRow(new Object[]{false, "", ""});
                }
                inParamsTable.setModel(model);

            }
        });

        inParamsTable.getModel().addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                DefaultTableModel model = (DefaultTableModel) inParamsTable.getModel();
                String query = "";
                for (int i = 0; i < model.getRowCount(); i++) {
                    String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                    String value = Optional.ofNullable(model.getValueAt(i, 2)).orElse("").toString();
                    boolean check = (boolean) Optional.ofNullable(model.getValueAt(i, 0)).orElse(false);
                    if (check) {
                        if (key.isEmpty() && value.isEmpty()) {
                            query = query + "&";
                        } else {
                            query = query + "&" + key + "=" + value;
                        }
                    }
                }
                String url = Optional.ofNullable(inUrlTxt.getText()).orElse("");
                url = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
                if (!query.isEmpty()) {
                    url += "?" + query.substring(1);
                }
                inUrlTxt.setText(url);
                if ((boolean) Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 0)).orElse(false)
                        || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 1)).orElse("").toString().isEmpty()
                        || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 2)).orElse("").toString().isEmpty()) {
                    model.addRow(new Object[]{false, "", ""});
                }
                inParamsTable.setModel(model);
            }
        });

        inHeadersTable.getModel().addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                DefaultTableModel model = (DefaultTableModel) inHeadersTable.getModel();
                if ((boolean) Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 0)).orElse(false)
                        || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 1)).orElse("").toString().isEmpty()
                        || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 2)).orElse("").toString().isEmpty()) {
                    model.addRow(new Object[]{false, "", ""});
                }
                inHeadersTable.setModel(model);
            }
        });

        typeBodyNoneButton.addActionListener((ActionEvent e) -> {
            CardLayout cardLayout = (CardLayout) panelInputBody.getLayout();
            cardLayout.show(panelInputBody, "card2");
            DefaultTableModel model = (DefaultTableModel) inHeadersTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                if (key.equals("Content-Type")) {
                    model.removeRow(i);
                }
            }
            inHeadersTable.setModel(model);
        });

        typeBodyTextButton.addActionListener((ActionEvent e) -> {
            CardLayout cardLayout = (CardLayout) panelInputBody.getLayout();
            cardLayout.show(panelInputBody, "card5");
            DefaultTableModel model = (DefaultTableModel) inHeadersTable.getModel();
            boolean check = true;
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                if (key.equals("Content-Type")) {
                    model.setValueAt("text/plain", i, 2);
                    check = false;
                }
            }
            if (check) {
                model.removeRow(model.getRowCount() - 1);
                model.addRow(new Object[]{true, "Content-Type", "text/plain"});
                model.addRow(new Object[]{false, "", ""});
            }
            inHeadersTable.setModel(model);
        });

        typeBodyJsonButton.addActionListener((ActionEvent e) -> {
            CardLayout cardLayout = (CardLayout) panelInputBody.getLayout();
            cardLayout.show(panelInputBody, "card3");
            DefaultTableModel model = (DefaultTableModel) inHeadersTable.getModel();
            boolean check = true;
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                if (key.equals("Content-Type")) {
                    model.setValueAt("application/json", i, 2);
                    check = false;
                }
            }
            if (check) {
                model.removeRow(model.getRowCount() - 1);
                model.addRow(new Object[]{true, "Content-Type", "application/json"});
                model.addRow(new Object[]{false, "", ""});
            }
            inHeadersTable.setModel(model);
        });

        typeBodyBinaryButton.addActionListener((ActionEvent e) -> {
            CardLayout cardLayout = (CardLayout) panelInputBody.getLayout();
            cardLayout.show(panelInputBody, "card4");
            DefaultTableModel model = (DefaultTableModel) inHeadersTable.getModel();
            boolean check = true;
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                if (key.equals("Content-Type")) {
                    model.setValueAt("application/octet-stream", i, 2);
                    check = false;
                }
            }
            if (check) {
                model.removeRow(model.getRowCount() - 1);
                model.addRow(new Object[]{true, "Content-Type", "application/octet-stream"});
                model.addRow(new Object[]{false, "", ""});
            }
            inHeadersTable.setModel(model);
        });

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typeBodyInput = new javax.swing.ButtonGroup();
        fileChooser = new javax.swing.JFileChooser();
        headerPanel = new javax.swing.JPanel();
        subNew = new javax.swing.JButton();
        subImport = new javax.swing.JButton();
        subExport = new javax.swing.JButton();
        inputPanel = new javax.swing.JPanel();
        inUrlPanel = new javax.swing.JPanel();
        methodCb = new javax.swing.JComboBox<>();
        inUrlTxt = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
        inputExtendPanel = new javax.swing.JTabbedPane();
        inputParamsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        inParamsTable = new javax.swing.JTable();
        inputHeadersPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        inHeadersTable = new javax.swing.JTable();
        inputBodyPanel = new javax.swing.JPanel();
        typeBodyPanel = new javax.swing.JPanel();
        typeBodyNoneButton = new javax.swing.JRadioButton();
        typeBodyTextButton = new javax.swing.JRadioButton();
        typeBodyJsonButton = new javax.swing.JRadioButton();
        typeBodyBinaryButton = new javax.swing.JRadioButton();
        panelInputBody = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        inBodyJsonTxt = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        chooseInputFileBody = new javax.swing.JButton();
        inFileNameTxt = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        inBodyText = new javax.swing.JTextArea();
        outputPanel = new javax.swing.JPanel();
        outStatusPanel = new javax.swing.JPanel();
        outStatusLabel = new javax.swing.JLabel();
        outDetailPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        outBodyPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        outBodyTxt = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        outCookiesTable = new javax.swing.JTable();
        outHeaderPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        outHeadersTable = new javax.swing.JTable();

        fileChooser.setMinimumSize(new java.awt.Dimension(700, 509));
        fileChooser.setPreferredSize(new java.awt.Dimension(700, 509));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Postman");
        setBackground(new java.awt.Color(255, 255, 255));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        headerPanel.setMinimumSize(new java.awt.Dimension(0, 60));
        headerPanel.setPreferredSize(new java.awt.Dimension(673, 60));
        headerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));

        subNew.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        subNew.setText("New");
        subNew.setFocusPainted(false);
        subNew.setMinimumSize(new java.awt.Dimension(108, 40));
        subNew.setPreferredSize(new java.awt.Dimension(108, 40));
        subNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subNewActionPerformed(evt);
            }
        });
        headerPanel.add(subNew);

        subImport.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        subImport.setText("Import");
        subImport.setFocusPainted(false);
        subImport.setMinimumSize(new java.awt.Dimension(108, 40));
        subImport.setPreferredSize(new java.awt.Dimension(108, 40));
        subImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subImportActionPerformed(evt);
            }
        });
        headerPanel.add(subImport);

        subExport.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        subExport.setText("Export");
        subExport.setFocusPainted(false);
        subExport.setMinimumSize(new java.awt.Dimension(108, 40));
        subExport.setPreferredSize(new java.awt.Dimension(108, 40));
        subExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subExportActionPerformed(evt);
            }
        });
        headerPanel.add(subExport);

        getContentPane().add(headerPanel);

        inputPanel.setPreferredSize(new java.awt.Dimension(760, 450));
        inputPanel.setLayout(new java.awt.BorderLayout());

        methodCb.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        methodCb.setModel(new DefaultComboBoxModel(HttpMethod.values())
        );

        inUrlTxt.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        inUrlTxt.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 1, 1));

        sendButton.setBackground(new java.awt.Color(9, 123, 237));
        sendButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        sendButton.setForeground(new java.awt.Color(255, 255, 255));
        sendButton.setText("SEND");
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout inUrlPanelLayout = new javax.swing.GroupLayout(inUrlPanel);
        inUrlPanel.setLayout(inUrlPanelLayout);
        inUrlPanelLayout.setHorizontalGroup(
            inUrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inUrlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(methodCb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inUrlTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sendButton)
                .addContainerGap())
        );
        inUrlPanelLayout.setVerticalGroup(
            inUrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inUrlPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(inUrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(methodCb, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                    .addComponent(sendButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(inUrlTxt, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        inputPanel.add(inUrlPanel, java.awt.BorderLayout.PAGE_START);

        inputExtendPanel.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        inputExtendPanel.setPreferredSize(new java.awt.Dimension(627, 430));

        inputParamsPanel.setLayout(new java.awt.BorderLayout());

        inParamsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {false, "", ""}
            },
            new String [] {
                "", "Key", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        inParamsTable.setCellSelectionEnabled(true);
        inParamsTable.setRowHeight(40);
        jScrollPane1.setViewportView(inParamsTable);

        inputParamsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        inputExtendPanel.addTab("Params", inputParamsPanel);

        inputHeadersPanel.setLayout(new java.awt.BorderLayout());

        inHeadersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                { true, "User-Agent", "Postman"},
                { true, "Accept", "*/*"},
                { false, "Accept-Encoding", "gzip, deflate, br"},
                {false, "", ""}
            },
            new String [] {
                "", "Key", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        inHeadersTable.setCellSelectionEnabled(true);
        inHeadersTable.setRowHeight(40);
        jScrollPane3.setViewportView(inHeadersTable);

        inputHeadersPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        inputExtendPanel.addTab("Headers", inputHeadersPanel);

        inputBodyPanel.setLayout(new java.awt.BorderLayout());

        typeBodyPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 18, 5));

        typeBodyInput.add(typeBodyNoneButton);
        typeBodyNoneButton.setSelected(true);
        typeBodyNoneButton.setText("none");
        typeBodyNoneButton.setFocusPainted(false);
        typeBodyPanel.add(typeBodyNoneButton);

        typeBodyInput.add(typeBodyTextButton);
        typeBodyTextButton.setText("text");
        typeBodyTextButton.setFocusPainted(false);
        typeBodyPanel.add(typeBodyTextButton);

        typeBodyInput.add(typeBodyJsonButton);
        typeBodyJsonButton.setText("json");
        typeBodyJsonButton.setFocusPainted(false);
        typeBodyPanel.add(typeBodyJsonButton);

        typeBodyInput.add(typeBodyBinaryButton);
        typeBodyBinaryButton.setText("binary");
        typeBodyPanel.add(typeBodyBinaryButton);

        inputBodyPanel.add(typeBodyPanel, java.awt.BorderLayout.PAGE_START);

        panelInputBody.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 608, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 85, Short.MAX_VALUE)
        );

        panelInputBody.add(jPanel1, "card2");

        jPanel2.setLayout(new java.awt.BorderLayout());

        inBodyJsonTxt.setColumns(20);
        inBodyJsonTxt.setRows(5);
        inBodyJsonTxt.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jScrollPane5.setViewportView(inBodyJsonTxt);

        jPanel2.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        panelInputBody.add(jPanel2, "card3");

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanel4.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.PAGE_AXIS));

        chooseInputFileBody.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        chooseInputFileBody.setText("Select file");
        chooseInputFileBody.setFocusPainted(false);
        chooseInputFileBody.setMinimumSize(new java.awt.Dimension(120, 40));
        chooseInputFileBody.setPreferredSize(new java.awt.Dimension(130, 40));
        chooseInputFileBody.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseInputFileBodyActionPerformed(evt);
            }
        });
        jPanel4.add(chooseInputFileBody);

        inFileNameTxt.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 1, 1, 1));
        jPanel4.add(inFileNameTxt);

        panelInputBody.add(jPanel4, "card4");

        jPanel3.setLayout(new java.awt.BorderLayout());

        inBodyText.setColumns(20);
        inBodyText.setRows(5);
        inBodyText.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jScrollPane7.setViewportView(inBodyText);

        jPanel3.add(jScrollPane7, java.awt.BorderLayout.CENTER);

        panelInputBody.add(jPanel3, "card5");

        inputBodyPanel.add(panelInputBody, java.awt.BorderLayout.CENTER);

        inputExtendPanel.addTab("Body", inputBodyPanel);

        inputPanel.add(inputExtendPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(inputPanel);

        outputPanel.setMinimumSize(new java.awt.Dimension(107, 500));
        outputPanel.setPreferredSize(new java.awt.Dimension(693, 500));
        outputPanel.setLayout(new java.awt.BorderLayout());

        outStatusLabel.setBackground(new java.awt.Color(255, 255, 255));
        outStatusLabel.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        outStatusLabel.setForeground(new java.awt.Color(0, 127, 49));
        outStatusLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 1, 1));
        outStatusLabel.setOpaque(true);

        javax.swing.GroupLayout outStatusPanelLayout = new javax.swing.GroupLayout(outStatusPanel);
        outStatusPanel.setLayout(outStatusPanelLayout);
        outStatusPanelLayout.setHorizontalGroup(
            outStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outStatusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(outStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 440, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(162, Short.MAX_VALUE))
        );
        outStatusPanelLayout.setVerticalGroup(
            outStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outStatusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(outStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                .addContainerGap())
        );

        outputPanel.add(outStatusPanel, java.awt.BorderLayout.PAGE_START);

        outDetailPanel.setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N

        outBodyPanel.setLayout(new java.awt.BorderLayout());

        outBodyTxt.setColumns(20);
        outBodyTxt.setRows(5);
        outBodyTxt.setTabSize(4);
        outBodyTxt.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jScrollPane4.setViewportView(outBodyTxt);

        outBodyPanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Body", outBodyPanel);

        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.LINE_AXIS));

        outCookiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Name", "Value", "Domain", "Path", "Expires", "HttpOnly", "Secure"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        outCookiesTable.setCellSelectionEnabled(true);
        outCookiesTable.setRowHeight(40);
        jScrollPane6.setViewportView(outCookiesTable);

        jPanel7.add(jScrollPane6);

        jTabbedPane1.addTab("Cookies", jPanel7);

        outHeaderPanel.setLayout(new java.awt.BorderLayout());

        outHeadersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Key", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        outHeadersTable.setCellSelectionEnabled(true);
        outHeadersTable.setRowHeight(40);
        jScrollPane2.setViewportView(outHeadersTable);

        outHeaderPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Headers", outHeaderPanel);

        outDetailPanel.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        outputPanel.add(outDetailPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(outputPanel);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed

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
                    .orElse(Arrays.asList("")).get(0);

            switch (contentEncoding.toLowerCase()) {
                case "gzip":
                    outBodyTxt.setText(Decompress.decompressGzip(httpResponse.getBody(), charset));
                    break;
                case "deflate":
                    outBodyTxt.setText(Decompress.decompressDeflate(httpResponse.getBody(), charset));
                    break;
                case "br":
                    outBodyTxt.setText(Decompress.decompressBrotli(httpResponse.getBody(), charset));
                    break;
                default:
                    outBodyTxt.setText(new String(httpResponse.getBody(), charset));
            }

            outBodyTxt.setCaretPosition(0);
            outStatusLabel.setText(httpResponse.getStatusCode() + " " + httpResponse.getStatusMessage());

            if (httpResponse.getHeader("Content-Type").get(0).equals("image/png")) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter("png", "PNG");
                fileChooser.setFileFilter(filter);
                fileChooser.setSelectedFile(new File(""));
                int x = fileChooser.showSaveDialog(this);

                if (x == JFileChooser.APPROVE_OPTION) {
                    String direct = fileChooser.getSelectedFile().toString();
                    if (!direct.endsWith(".png")) {
                        direct += ".png";
                    }
                    File file = new File(direct);
                    try ( FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        fileOutputStream.write(httpResponse.getBody());
                    }
                }
            }

            DefaultTableModel model = (DefaultTableModel) outHeadersTable.getModel();
            model.setRowCount(0);
            for (Map.Entry<String, List<String>> entry : httpResponse.getHeaders().entrySet()) {
                for (String value : entry.getValue()) {
                    model.addRow(new Object[]{entry.getKey(), value});
                }
            }
            outHeadersTable.setModel(model);

            DefaultTableModel modelCookieTable = (DefaultTableModel) outCookiesTable.getModel();
            modelCookieTable.setRowCount(0);
            for (Cookie cookie : httpResponse.getCookies()) {
                modelCookieTable.addRow(
                        new Object[]{
                            cookie.getKey(),
                            cookie.getValue(),
                            cookie.getDomain(),
                            cookie.getPath(),
                            cookie.getExpires(),
                            cookie.isHttp(),
                            cookie.isSecure()
                        }
                );
            }
            outCookiesTable.setModel(modelCookieTable);

        } catch (HeadlessException | IOException | URLFormatException | DecompressException | DataFormatException ex) {
            Logger.getLogger(PostmanView.class.getName()).log(Level.SEVERE, null, ex);
            outStatusLabel.setText(ex.getClass().getSimpleName());
            outBodyTxt.setText(ex.getMessage());
        }

    }//GEN-LAST:event_sendButtonActionPerformed

    private HttpRequest createRequest() {
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setMethod((HttpMethod) methodCb.getSelectedItem());
        httpRequest.setUrl(Optional.ofNullable(inUrlTxt.getText().trim()).orElse(""));
        if (!typeBodyNoneButton.isSelected()) {
            httpRequest.setBody(Optional.ofNullable(inBodyJsonTxt.getText().trim()).orElse(""));
            httpRequest.addHeader("Content-Length", String.valueOf(httpRequest.getBody().length()));
        }

        if (typeBodyBinaryButton.isSelected()) {
            httpRequest.setBody(inFileNameTxt.getText());
            File file = new File(inFileNameTxt.getText());
            httpRequest.addHeader("Content-Length", String.valueOf(file.length()));
            httpRequest.setSendingFile(true);
        }

        DefaultTableModel modelHeaders = (DefaultTableModel) inHeadersTable.getModel();
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
        outStatusLabel.setText("");
        outBodyTxt.setText("");
        DefaultTableModel model = (DefaultTableModel) outCookiesTable.getModel();
        model.setRowCount(0);
        outCookiesTable.setModel(model);

        model = (DefaultTableModel) outHeadersTable.getModel();
        model.setRowCount(0);
        outHeadersTable.setModel(model);
    }

    private void resetInput() {
        methodCb.setSelectedIndex(0);

        inUrlTxt.setText("");

        DefaultTableModel model = (DefaultTableModel) inParamsTable.getModel();
        model.setRowCount(0);
        model.addRow(new Object[]{false, "", ""});
        inParamsTable.setModel(model);

        model = (DefaultTableModel) inHeadersTable.getModel();
        model.setRowCount(0);
        model.addRow(new Object[]{true, "User-Agent", "Postman"});
        model.addRow(new Object[]{true, "Accept", "*/*"});
        model.addRow(new Object[]{false, "Accept-Encoding", "gzip, deflate, br"});
        model.addRow(new Object[]{false, "", ""});
        inHeadersTable.setModel(model);

        inBodyJsonTxt.setText("");
        typeBodyNoneButton.doClick();
    }

    private void subNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subNewActionPerformed
        resetInput();
        resetOutput();
    }//GEN-LAST:event_subNewActionPerformed

    private void subImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subImportActionPerformed
        FileNameExtensionFilter filter = new FileNameExtensionFilter("req", "req");
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File(""));
        int x = fileChooser.showOpenDialog(null);
        if (x == JFileChooser.APPROVE_OPTION) {
            String direct = fileChooser.getSelectedFile().toString();
            System.out.println(direct);
            Storage storage = new Storage();
            HttpRequestStorage httpRequestStorage = storage.importPostman(direct);

            methodCb.setSelectedItem(httpRequestStorage.getMethod());

            inUrlTxt.setText(httpRequestStorage.getUrl());

            DefaultTableModel model = (DefaultTableModel) inParamsTable.getModel();
            model.setRowCount(0);
            for (Object[] i : httpRequestStorage.getParams()) {
                model.addRow(i);
            }
            inParamsTable.setModel(model);

            model = (DefaultTableModel) inHeadersTable.getModel();
            model.setRowCount(0);
            for (Object[] i : httpRequestStorage.getHeaders()) {
                model.addRow(i);
            }
            inHeadersTable.setModel(model);

            inBodyJsonTxt.setText(httpRequestStorage.getBody());

            List<AbstractButton> listButton = Collections.list(typeBodyInput.getElements());

            for (AbstractButton button : listButton) {
                if (button.getText().equals(httpRequestStorage.getTypeBody())) {
                    button.doClick();
                }
            }

            inFileNameTxt.setText(httpRequestStorage.getFileName());
        }

    }//GEN-LAST:event_subImportActionPerformed

    private void subExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subExportActionPerformed
        FileNameExtensionFilter filter = new FileNameExtensionFilter("req", "req");
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File(""));
        int x = fileChooser.showSaveDialog(null);
        if (x == JFileChooser.APPROVE_OPTION) {
            String direct = fileChooser.getSelectedFile().toString();
            if (!direct.endsWith(".req")) {
                direct += ".req";
            }
            HttpRequestStorage httpRequestStorage = new HttpRequestStorage();
            httpRequestStorage.setMethod((HttpMethod) methodCb.getSelectedItem());
            httpRequestStorage.setUrl(Optional.ofNullable(inUrlTxt.getText().trim()).orElse(""));

            httpRequestStorage.setBody(inBodyJsonTxt.getText());

            DefaultTableModel model = (DefaultTableModel) inParamsTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                String value = Optional.ofNullable(model.getValueAt(i, 2)).orElse("").toString();
                boolean check = (boolean) Optional.ofNullable(model.getValueAt(i, 0)).orElse(false);
                httpRequestStorage.addParam(new Object[]{check, key, value});
            }

            model = (DefaultTableModel) inHeadersTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                String value = Optional.ofNullable(model.getValueAt(i, 2)).orElse("").toString();
                boolean check = (boolean) Optional.ofNullable(model.getValueAt(i, 0)).orElse(false);
                httpRequestStorage.addHeader(new Object[]{check, key, value});
            }

            List<AbstractButton> listButton = Collections.list(typeBodyInput.getElements());

            for (AbstractButton button : listButton) {
                if (button.isSelected()) {
                    httpRequestStorage.setTypeBody(button.getText());
                }
            }

            httpRequestStorage.setFileName(Optional.ofNullable(inFileNameTxt.getText()).orElse(""));

            Storage storage = new Storage();
            storage.export(httpRequestStorage, direct);
        }

    }//GEN-LAST:event_subExportActionPerformed

    private void chooseInputFileBodyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseInputFileBodyActionPerformed
        int x = fileChooser.showOpenDialog(this);
        if (x == JFileChooser.APPROVE_OPTION) {
            String direction = fileChooser.getSelectedFile().toString();
            inFileNameTxt.setText(direction);
            String contentType = URLConnection.guessContentTypeFromName(new File(direction).getName());
            DefaultTableModel model = (DefaultTableModel) inHeadersTable.getModel();
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
            inHeadersTable.setModel(model);
        }
    }//GEN-LAST:event_chooseInputFileBodyActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PostmanView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PostmanView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PostmanView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PostmanView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PostmanView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseInputFileBody;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JTextArea inBodyJsonTxt;
    private javax.swing.JTextArea inBodyText;
    private javax.swing.JLabel inFileNameTxt;
    private javax.swing.JTable inHeadersTable;
    private javax.swing.JTable inParamsTable;
    private javax.swing.JPanel inUrlPanel;
    private javax.swing.JTextField inUrlTxt;
    private javax.swing.JPanel inputBodyPanel;
    private javax.swing.JTabbedPane inputExtendPanel;
    private javax.swing.JPanel inputHeadersPanel;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JPanel inputParamsPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox<String> methodCb;
    private javax.swing.JPanel outBodyPanel;
    private javax.swing.JTextArea outBodyTxt;
    private javax.swing.JTable outCookiesTable;
    private javax.swing.JPanel outDetailPanel;
    private javax.swing.JPanel outHeaderPanel;
    private javax.swing.JTable outHeadersTable;
    private javax.swing.JLabel outStatusLabel;
    private javax.swing.JPanel outStatusPanel;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JPanel panelInputBody;
    private javax.swing.JButton sendButton;
    private javax.swing.JButton subExport;
    private javax.swing.JButton subImport;
    private javax.swing.JButton subNew;
    private javax.swing.JRadioButton typeBodyBinaryButton;
    private javax.swing.ButtonGroup typeBodyInput;
    private javax.swing.JRadioButton typeBodyJsonButton;
    private javax.swing.JRadioButton typeBodyNoneButton;
    private javax.swing.JPanel typeBodyPanel;
    private javax.swing.JRadioButton typeBodyTextButton;
    // End of variables declaration//GEN-END:variables
}
