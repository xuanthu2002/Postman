package postman.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import postman.model.Cookie;
import postman.model.HttpMethod;
import postman.model.HttpRequest;
import postman.model.HttpResponse;
import postman.model.HttpUrl;
import postman.service.HttpClient;

public class PostmanView extends javax.swing.JFrame {

    public PostmanView() {

        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 20));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 20));
        UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 20));
        initComponents();

        JTextField cell = new JTextField();
        cell.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        DefaultCellEditor singleclick = new DefaultCellEditor(cell);
        singleclick.setClickCountToStart(1);
        //set the editor as default on every column
        for (int i = 1; i < inParamsTable.getColumnCount(); i++) {
            inParamsTable.setDefaultEditor(inParamsTable.getColumnClass(i), singleclick);
        }
        for (int i = 1; i < inHeaderTable.getColumnCount(); i++) {
            inHeaderTable.setDefaultEditor(inHeaderTable.getColumnClass(i), singleclick);
        }
        for (int i = 1; i < outHeaders.getColumnCount(); i++) {
            outHeaders.setDefaultEditor(outHeaders.getColumnClass(i), singleclick);
        }
        for (int i = 1; i < outCookiesTable.getColumnCount(); i++) {
            outCookiesTable.setDefaultEditor(outCookiesTable.getColumnClass(i), singleclick);
        }

        inUrlTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                    return;
                }
                Map<String, String> param = new HttpUrl(inUrlTxt.getText().trim()).getParam();
                DefaultTableModel model = (DefaultTableModel) inParamsTable.getModel();
                model.setRowCount(0);
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    model.addRow(new Object[]{true, entry.getKey(), entry.getValue()});
                }
                if (model.getRowCount() < 1) {
                    model.addRow(new Object[]{false, "", ""});
                } else if ((boolean) model.getValueAt(model.getRowCount() - 1, 0) || !model.getValueAt(model.getRowCount() - 1, 1).toString().isEmpty() || !model.getValueAt(model.getRowCount() - 1, 2).toString().isEmpty()) {
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
                    String key = model.getValueAt(i, 1).toString();
                    String value = model.getValueAt(i, 2).toString();
                    if ((boolean) model.getValueAt(i, 0) == true) {
                        if (key.isEmpty() && value.isEmpty()) {
                            query = query + "&";
                        } else {
                            query = query + "&" + key + "=" + value;
                        }
                    }
                }
                String url = inUrlTxt.getText();
                url = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
                if (!query.isEmpty()) {
                    url += "?" + query.substring(1);
                }
                inUrlTxt.setText(url);
                if ((boolean) model.getValueAt(model.getRowCount() - 1, 0)
                        || !model.getValueAt(model.getRowCount() - 1, 1).toString().isEmpty()
                        || !model.getValueAt(model.getRowCount() - 1, 2).toString().isEmpty()) {
                    model.addRow(new Object[]{false, "", ""});
                }
                inParamsTable.setModel(model);
            }
        });

        inHeaderTable.getModel().addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                DefaultTableModel model = (DefaultTableModel) inHeaderTable.getModel();
                if ((boolean) model.getValueAt(model.getRowCount() - 1, 0)
                        || !model.getValueAt(model.getRowCount() - 1, 1).toString().isEmpty()
                        || !model.getValueAt(model.getRowCount() - 1, 2).toString().isEmpty()) {
                    model.addRow(new Object[]{false, "", ""});
                }
                inHeaderTable.setModel(model);
            }
        });

        jRadioButton1.addActionListener((ActionEvent e) -> {
            DefaultTableModel model = (DefaultTableModel) inHeaderTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 1).equals("Content-Type")) {
                    model.removeRow(i);
                }
            }
            inHeaderTable.setModel(model);
        });

        jRadioButton2.addActionListener((ActionEvent e) -> {
            DefaultTableModel model = (DefaultTableModel) inHeaderTable.getModel();
            boolean check = true;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 1).equals("Content-Type")) {
                    model.setValueAt("text/plain", i, 2);
                    check = false;
                }
            }
            if (check) {
                model.removeRow(model.getRowCount() - 1);
                model.addRow(new Object[]{true, "Content-Type", "text/plain"});
                model.addRow(new Object[]{false, "", ""});
            }
            inHeaderTable.setModel(model);
        });

        jRadioButton3.addActionListener((ActionEvent e) -> {
            DefaultTableModel model = (DefaultTableModel) inHeaderTable.getModel();
            boolean check = true;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 1).equals("Content-Type")) {
                    model.setValueAt("application/json", i, 2);
                    check = false;
                }
            }
            if (check) {
                model.removeRow(model.getRowCount() - 1);
                model.addRow(new Object[]{true, "Content-Type", "application/json"});
                model.addRow(new Object[]{false, "", ""});
            }
            inHeaderTable.setModel(model);
        });

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typeBodyInput = new javax.swing.ButtonGroup();
        headerPanel = new javax.swing.JPanel();
        inputPanel = new javax.swing.JPanel();
        inUrlPanel = new javax.swing.JPanel();
        methodCb = new javax.swing.JComboBox<>();
        inUrlTxt = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
        inputExtendPanel = new javax.swing.JTabbedPane();
        inputParamsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        inParamsTable = new javax.swing.JTable();
        inputAuthorizationPanel = new javax.swing.JPanel();
        inputHeadersPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        inHeaderTable = new javax.swing.JTable();
        inputBodyPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        inBodyTxt = new javax.swing.JTextArea();
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
        outHeaders = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Postman");
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(739, 1000));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        headerPanel.setMinimumSize(new java.awt.Dimension(0, 40));
        headerPanel.setPreferredSize(new java.awt.Dimension(673, 40));

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 673, Short.MAX_VALUE)
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        getContentPane().add(headerPanel);

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
                .addComponent(inUrlTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
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

        inputParamsPanel.setLayout(new java.awt.BorderLayout());

        inParamsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
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
        inParamsTable.setRowHeight(40);
        jScrollPane1.setViewportView(inParamsTable);

        inputParamsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        inputExtendPanel.addTab("Params", inputParamsPanel);

        inputAuthorizationPanel.setLayout(new java.awt.BorderLayout());
        inputExtendPanel.addTab("Authorization", inputAuthorizationPanel);

        inputHeadersPanel.setLayout(new java.awt.BorderLayout());

        inHeaderTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                { new Boolean(true), "User-Agent", "Postman"},
                { new Boolean(true), "Accept", "*/*"},
                { new Boolean(true), "Accept-Encoding", "gzip, deflate, br"},
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
        inHeaderTable.setRowHeight(40);
        jScrollPane3.setViewportView(inHeaderTable);

        inputHeadersPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        inputExtendPanel.addTab("Headers", inputHeadersPanel);

        inputBodyPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 18, 5));

        typeBodyInput.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("none");
        jRadioButton1.setFocusPainted(false);
        jPanel1.add(jRadioButton1);

        typeBodyInput.add(jRadioButton2);
        jRadioButton2.setText("text");
        jRadioButton2.setFocusPainted(false);
        jPanel1.add(jRadioButton2);

        typeBodyInput.add(jRadioButton3);
        jRadioButton3.setText("json");
        jRadioButton3.setFocusPainted(false);
        jPanel1.add(jRadioButton3);

        inputBodyPanel.add(jPanel1, java.awt.BorderLayout.PAGE_START);

        inBodyTxt.setColumns(20);
        inBodyTxt.setRows(5);
        inBodyTxt.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jScrollPane5.setViewportView(inBodyTxt);

        inputBodyPanel.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        inputExtendPanel.addTab("Body", inputBodyPanel);

        inputPanel.add(inputExtendPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(inputPanel);

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
                .addContainerGap(227, Short.MAX_VALUE))
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
        outCookiesTable.setRowHeight(40);
        jScrollPane6.setViewportView(outCookiesTable);

        jPanel7.add(jScrollPane6);

        jTabbedPane1.addTab("Cookies", jPanel7);

        outHeaderPanel.setLayout(new java.awt.BorderLayout());

        outHeaders.setModel(new javax.swing.table.DefaultTableModel(
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
        outHeaders.setRowHeight(40);
        jScrollPane2.setViewportView(outHeaders);

        outHeaderPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Headers", outHeaderPanel);

        outDetailPanel.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        outputPanel.add(outDetailPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(outputPanel);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        // TODO add your handling code here:
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setMethod((HttpMethod) methodCb.getSelectedItem());
        httpRequest.setUrl(new HttpUrl(inUrlTxt.getText()));
        if(!jRadioButton1.isSelected()) {
            httpRequest.setBody(inBodyTxt.getText());
            httpRequest.addHeader("Content-Length", httpRequest.getBody().length() + "");
        }

        DefaultTableModel modelHeaders = (DefaultTableModel) inHeaderTable.getModel();
        for(int i = 0; i < modelHeaders.getRowCount(); i++) {
            String key = modelHeaders.getValueAt(i, 1).toString();
            String value = modelHeaders.getValueAt(i, 2).toString();
            if ((boolean) modelHeaders.getValueAt(i, 0) == true) {
                httpRequest.addHeader(key, value);
            }
        }
        try {
            HttpResponse httpResponse = new HttpClient().send(httpRequest);
            outBodyTxt.setText(httpResponse.getBody());
            outStatusLabel.setText(httpResponse.getStatusCode() + " " + httpResponse.getStatusMessage());

            DefaultTableModel model = (DefaultTableModel) outHeaders.getModel();
            model.setRowCount(0);
            for(Map.Entry<String, List<String>> entry : httpResponse.getHeaders().entrySet()) {
                for (String value : entry.getValue()) {
                    model.addRow(new Object[]{entry.getKey(), value});
                }
            }
            outHeaders.setModel(model);
            
            DefaultTableModel modelCookieTable = (DefaultTableModel) outCookiesTable.getModel();
            modelCookieTable.setRowCount(0);
            for(Cookie cookie : httpResponse.getCookies()) {
                modelCookieTable.addRow(
                        new Object[] {
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

        } catch (IOException ex) {
            Logger.getLogger(PostmanView.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_sendButtonActionPerformed

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
    private javax.swing.JPanel headerPanel;
    private javax.swing.JTextArea inBodyTxt;
    private javax.swing.JTable inHeaderTable;
    private javax.swing.JTable inParamsTable;
    private javax.swing.JPanel inUrlPanel;
    private javax.swing.JTextField inUrlTxt;
    private javax.swing.JPanel inputAuthorizationPanel;
    private javax.swing.JPanel inputBodyPanel;
    private javax.swing.JTabbedPane inputExtendPanel;
    private javax.swing.JPanel inputHeadersPanel;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JPanel inputParamsPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox<String> methodCb;
    private javax.swing.JPanel outBodyPanel;
    private javax.swing.JTextArea outBodyTxt;
    private javax.swing.JTable outCookiesTable;
    private javax.swing.JPanel outDetailPanel;
    private javax.swing.JPanel outHeaderPanel;
    private javax.swing.JTable outHeaders;
    private javax.swing.JLabel outStatusLabel;
    private javax.swing.JPanel outStatusPanel;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JButton sendButton;
    private javax.swing.ButtonGroup typeBodyInput;
    // End of variables declaration//GEN-END:variables
}
