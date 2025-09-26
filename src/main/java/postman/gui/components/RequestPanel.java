package postman.gui.components;

import org.apache.tika.Tika;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import postman.gui.PostmanContract;
import postman.gui.constants.Colors;
import postman.gui.constants.Fonts;
import postman.gui.constants.Strings;
import postman.gui.constants.Values;
import postman.util.HttpMethod;
import postman.util.HttpRequest;
import postman.util.URIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class RequestPanel extends JPanel implements PostmanContract.RequestView {
    private final PostmanContract.Presenter mPresenter;

    private boolean isEditingURL;
    private byte[] bodyBytes;

    public RequestPanel(PostmanContract.Presenter mPresenter) {
        super();
        this.mPresenter = mPresenter;

        mButtonGroupRequestBodyType = new ButtonGroup();
        mPanelRequestUrl = new JPanel();
        mComboBoxRequestMethod = new ComboBox<>();
        mTextFieldRequestUrl = new JTextField();
        mButtonSendRequest = new JButton();

        mPanelRequestDetail = new JTabbedPane();
        mPanelRequestParams = new JPanel();
        mTableRequestParams = new EditableTable();
        mScrollPaneRequestParams = new JScrollPane(mTableRequestParams, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mPanelRequestHeaders = new JPanel();
        mTableRequestHeaders = new EditableTable();
        mScrollPaneRequestHeaders = new JScrollPane(mTableRequestHeaders, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mPanelRequestBody = new JPanel();
        mPanelRequestBodyType = new JPanel();
        mRadioButtonRequestBodyNone = new JRadioButton();
        mRadioButtonRequestBodyText = new JRadioButton();
        mRadioButtonRequestBodyJson = new JRadioButton();
        mRadioButtonBodyBinary = new JRadioButton();

        mPanelRequestBodyDetail = new JPanel();
        mPanelRequestBodyNone = new JPanel();
        mPanelRequestBodyText = new JPanel();
        mPanelRequestBodyJson = new JPanel();
        mPanelRequestBodyBinary = new JPanel();

        mButtonChooseInputFile = new JButton();
        mLabelUploadedFile = new JLabel();
        mTextAreaRequestBodyText = new TextEditor();
        mTextAreaRequestBodyJson = new TextEditor();
        mScrollPaneRequestBodyText = new ScrollPaneEditor(mTextAreaRequestBodyText);
        mScrollPaneRequestBodyJson = new ScrollPaneEditor(mTextAreaRequestBodyJson);

        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        initPanelRequestUrl();
        this.add(mPanelRequestUrl, BorderLayout.PAGE_START);

        initPanelRequestDetail();
        this.add(mPanelRequestDetail, BorderLayout.CENTER);
    }

    private void initPanelRequestUrl() {
        mComboBoxRequestMethod.setFont(Fonts.GENERAL_BOLD_12);
        mComboBoxRequestMethod.setModel(new DefaultComboBoxModel<>(HttpMethod.values()));

        mTextFieldRequestUrl.setFont(Fonts.GENERAL_PLAIN_12);
        mTextFieldRequestUrl.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));

        mButtonSendRequest.setBackground(Colors.BLUE_COLOR);
        mButtonSendRequest.setFont(Fonts.GENERAL_BOLD_12);
        mButtonSendRequest.setForeground(Colors.WHITE_COLOR);
        mButtonSendRequest.setText(Strings.SEND);
        mButtonSendRequest.setFocusPainted(false);
        mButtonSendRequest.setEnabled(false);
        mButtonSendRequest.addActionListener(e -> onClickSendRequest());

        mPanelRequestUrl.setLayout(new GridBagLayout());
        mPanelRequestUrl.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0;
        mPanelRequestUrl.add(mComboBoxRequestMethod, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        mPanelRequestUrl.add(mTextFieldRequestUrl, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        mPanelRequestUrl.add(mButtonSendRequest, constraints);

        configureUrlTextField();
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
                isEditingURL = true;
                if (mTextFieldRequestUrl.getText().trim().isEmpty()) {
                    mButtonSendRequest.setEnabled(false);
                    return;
                }
                mButtonSendRequest.setEnabled(true);
                updateRequestParamsTable();
            }
        });
        mTextFieldRequestUrl.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                isEditingURL = false;
            }
        });
    }

    private void initPanelRequestDetail() {
        mPanelRequestDetail.setFont(Fonts.GENERAL_PLAIN_12);
        mPanelRequestDetail.setFocusable(false);

        initPanelRequestParams();
        mPanelRequestDetail.addTab("Params", mPanelRequestParams);

        initPanelRequestHeaders();
        mPanelRequestDetail.addTab("Headers", mPanelRequestHeaders);

        initPanelRequestBody();
        mPanelRequestDetail.addTab("Body", mPanelRequestBody);
    }

    private void initPanelRequestParams() {
        mPanelRequestParams.setLayout(new BorderLayout());

        mTableRequestParams.setShowDeleteOption(true);
        mTableRequestParams.setModel(new FlexibleDefaultTableModel(
                new Object[][]{
                        {false, "", ""}
                },
                new String[]{
                        "", "Key", "Value"
                },
                new Class[]{
                        Boolean.class, String.class, String.class
                }
        ));
        mTableRequestParams.setCellSelectionEnabled(true);
        mTableRequestParams.setRowHeight(Values.DEFAULT_TABLE_ROW_HEIGHT);

        mPanelRequestParams.add(mScrollPaneRequestParams, BorderLayout.CENTER);

        configureRequestParamsTable();
    }

    private void configureRequestParamsTable() {
        mTableRequestParams.getModel().addTableModelListener((TableModelEvent e) -> {
            if (!isEditingURL
                    && (e.getType() == TableModelEvent.UPDATE
                    || e.getType() == TableModelEvent.DELETE
                    || e.getType() == TableModelEvent.INSERT)) {
                updateUrlTextField();
                ensureEmptyRowInTable(mTableRequestParams);
            }
        });
    }

    private void initPanelRequestHeaders() {
        mPanelRequestHeaders.setLayout(new BorderLayout());

        mTableRequestHeaders.setShowDeleteOption(true);
        mTableRequestHeaders.setModel(new FlexibleDefaultTableModel(
                new Object[][]{
                        {Boolean.TRUE, "User-Agent", "Postman"},
                        {Boolean.TRUE, "Accept", "*/*"},
                        {Boolean.TRUE, "Accept-Encoding", "gzip, deflate, br"},
                        {Boolean.FALSE, "", ""}
                },
                new String[]{
                        "", "Key", "Value"
                },
                new Class[]{
                        Boolean.class, String.class, String.class
                }
        ));
        mTableRequestHeaders.setCellSelectionEnabled(true);
        mTableRequestHeaders.setRowHeight(Values.DEFAULT_TABLE_ROW_HEIGHT);

        mPanelRequestHeaders.add(mScrollPaneRequestHeaders, BorderLayout.CENTER);

        configureRequestHeadersTable();
    }

    private void configureRequestHeadersTable() {
        mTableRequestHeaders.getModel().addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE || e.getType() == TableModelEvent.DELETE) {
                ensureEmptyRowInTable(mTableRequestHeaders);
            }
        });
    }

    private void initPanelRequestBody() {
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

        mPanelRequestBodyDetail.add(mPanelRequestBodyNone, "none");

        mTextAreaRequestBodyText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        mTextAreaRequestBodyText.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mPanelRequestBodyText.setLayout(new BorderLayout());
        mPanelRequestBodyText.add(mScrollPaneRequestBodyText, BorderLayout.CENTER);
        mPanelRequestBodyDetail.add(mPanelRequestBodyText, "text");

        mTextAreaRequestBodyJson.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS);
        mTextAreaRequestBodyJson.setCodeFoldingEnabled(true);
        mTextAreaRequestBodyJson.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mScrollPaneRequestBodyJson.setFoldIndicatorEnabled(true);
        mPanelRequestBodyJson.setLayout(new BorderLayout());
        mPanelRequestBodyJson.add(mScrollPaneRequestBodyJson, BorderLayout.CENTER);
        mPanelRequestBodyDetail.add(mPanelRequestBodyJson, "json");

        mPanelRequestBodyBinary.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mPanelRequestBodyBinary.setLayout(new BoxLayout(mPanelRequestBodyBinary, BoxLayout.PAGE_AXIS));

        mButtonChooseInputFile.setFont(Fonts.GENERAL_PLAIN_12);
        mButtonChooseInputFile.setText(Strings.SELECT_FILE);
        mButtonChooseInputFile.setFocusPainted(false);
        mButtonChooseInputFile.addActionListener(evt -> onClickChooseBodyFile());
        mPanelRequestBodyBinary.add(mButtonChooseInputFile);

        mLabelUploadedFile.setBorder(BorderFactory.createEmptyBorder(16, 1, 1, 1));
        mPanelRequestBodyBinary.add(mLabelUploadedFile);

        mPanelRequestBodyDetail.add(mPanelRequestBodyBinary, "binary");

        mPanelRequestBody.add(mPanelRequestBodyDetail, BorderLayout.CENTER);

        configureRequestBodyActions();
    }

    private void configureRequestBodyActions() {
        mRadioButtonRequestBodyNone.addActionListener((ActionEvent e) -> configureRequestBodyType("none", "none"));
        mRadioButtonRequestBodyText.addActionListener((ActionEvent e) -> configureRequestBodyType("text/plain", "text"));
        mRadioButtonRequestBodyJson.addActionListener((ActionEvent e) -> configureRequestBodyType("application/json", "json"));
        mRadioButtonBodyBinary.addActionListener((ActionEvent e) -> configureRequestBodyType("application/octet-stream", "binary"));
    }

    private void configureRequestBodyType(String contentType, String panelName) {
        showRequestBodyPanel(panelName);
        FlexibleDefaultTableModel model = (FlexibleDefaultTableModel) mTableRequestHeaders.getModel();

        if (contentType.equalsIgnoreCase("none")) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                if (key.equalsIgnoreCase("Content-Type")) {
                    model.removeRow(i);
                    break;
                }
            }
        } else {
            int rowIndex = model.getRowCount() - 1;
            for (int i = 0; i < model.getRowCount(); i++) {
                String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
                if (key.equalsIgnoreCase("Content-Type")) {
                    rowIndex = i;
                    break;
                }
            }
            model.setRow(new Object[]{true, "Content-Type", contentType}, rowIndex);
        }

        mTableRequestHeaders.setModel(model);
    }

    private void updateRequestParamsTable() {
        Map<String, String> params = URIUtils.extractParams(mTextFieldRequestUrl.getText().trim());
        DefaultTableModel model = (DefaultTableModel) mTableRequestParams.getModel();
        model.setRowCount(0);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            model.addRow(new Object[]{true, entry.getKey(), entry.getValue()});
        }
        if (model.getRowCount() < 1) {
            model.addRow(new Object[]{false, "", ""});
        } else {
            ensureEmptyRowInTable(mTableRequestParams);
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
        if (!query.toString().isEmpty()) {
            url += "?" + query.substring(1);
        }
        mTextFieldRequestUrl.setText(url);
    }

    private void ensureEmptyRowInTable(EditableTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        if (model.getRowCount() < 1
                || (boolean) Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 0)).orElse(false)
                || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 1)).orElse("").toString().isEmpty()
                || !Optional.ofNullable(model.getValueAt(model.getRowCount() - 1, 2)).orElse("").toString().isEmpty()) {
            model.addRow(new Object[]{false, "", ""});
        }
    }

    private void showRequestBodyPanel(String panelName) {
        CardLayout cardLayout = (CardLayout) mPanelRequestBodyDetail.getLayout();
        cardLayout.show(mPanelRequestBodyDetail, panelName);
    }

    private void onClickChooseBodyFile() {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        FileDialog fileDialog = new FileDialog(frame, "Open", FileDialog.LOAD);
        fileDialog.setDirectory(System.getProperty("user.home"));
        fileDialog.setVisible(true);
        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();
        String direction = Paths.get(directory, filename).toString();
        mLabelUploadedFile.setText(direction);
        String contentType;
        try {
            contentType = new Tika().detect(direction);
        } catch (Exception e) {
            contentType = "application/octet-stream";
        }
        try {
            bodyBytes = Files.readAllBytes(Paths.get(direction));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Đã xảy ra lỗi khi mở tệp.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE
            );
        }
        DefaultTableModel model = (DefaultTableModel) mTableRequestHeaders.getModel();
        configureRequestBodyType(contentType, "binary");
        mTableRequestHeaders.setModel(model);
    }

    private byte[] getRequestBodyBytes() {
        byte[] content;
        if (mRadioButtonRequestBodyText.isSelected()) {
            content = Optional.ofNullable(mTextAreaRequestBodyText.getText()).orElse("").trim().getBytes();
        } else if (mRadioButtonRequestBodyJson.isSelected()) {
            content = Optional.ofNullable(mTextAreaRequestBodyJson.getText()).orElse("").trim().getBytes();
        } else {
            content = bodyBytes;
        }
        return content;
    }

    public void resetInput() {
        mComboBoxRequestMethod.setSelectedIndex(0);
        mTextFieldRequestUrl.setText("");
        mTableRequestParams.clear();

        FlexibleDefaultTableModel model = (FlexibleDefaultTableModel) mTableRequestHeaders.getModel();
        model.setRowCount(0);
        model.setRow(new Object[]{true, "User-Agent", "Postman"}, 0);
        model.setRow(new Object[]{true, "Accept", "*/*"}, 1);
        model.setRow(new Object[]{true, "Accept-Encoding", "gzip, deflate, br"}, 2);
        model.setRow(new Object[]{false, "", ""}, 3);
        mTableRequestHeaders.setModel(model);

        mRadioButtonRequestBodyNone.doClick();
        mTextAreaRequestBodyText.setText("");
        mTextAreaRequestBodyJson.setText("");
        mLabelUploadedFile.setText("");
        bodyBytes = null;
    }

    private Map<String, String> getHeaders() {
        DefaultTableModel model = (DefaultTableModel) mTableRequestHeaders.getModel();
        Map<String, String> headers = new HashMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String key = Optional.ofNullable(model.getValueAt(i, 1)).orElse("").toString();
            String value = Optional.ofNullable(model.getValueAt(i, 2)).orElse("").toString();
            boolean check = (boolean) Optional.ofNullable(model.getValueAt(i, 0)).orElse(false);
            if (check && !key.isEmpty() && !value.isEmpty()) {
                headers.put(key, value);
            }
        }
        return headers;
    }

    @Override
    public HttpRequest getCurrentRequest() {
        HttpMethod method = (HttpMethod) mComboBoxRequestMethod.getSelectedItem();
        String url = mTextFieldRequestUrl.getText().trim();
        HttpRequest request = new HttpRequest(method, url);
        request.setHeaders(getHeaders());
        byte[] body = getRequestBodyBytes();
        if (!mRadioButtonRequestBodyNone.isSelected()) {
            request.putHeader("Content-Length", String.valueOf(body.length));
        }
        request.setBody(body);
        return request;
    }

    private void onClickSendRequest() {
        if (mButtonSendRequest.getText().equals("CANCEL")) {
            mPresenter.onClickCancel();
        } else mPresenter.onClickSendRequest();
    }

    @Override
    public void setRequest(HttpRequest httpRequest) {
        mTextFieldRequestUrl.setText(httpRequest.getUrl());
        Map<String, String> headers = httpRequest.getHeaders();
//        mTableRequestHeaders.
    }

    @Override
    public void onSendingRequest() {
        mButtonSendRequest.setText("CANCEL");
    }

    @Override
    public void onSent() {
        mButtonSendRequest.setText("SEND");
    }

    private final ButtonGroup mButtonGroupRequestBodyType;
    private final JPanel mPanelRequestUrl;
    private final JComboBox<HttpMethod> mComboBoxRequestMethod;
    private final JTextField mTextFieldRequestUrl;
    private final JButton mButtonSendRequest;

    private final JTabbedPane mPanelRequestDetail;
    private final JPanel mPanelRequestParams;
    private final EditableTable mTableRequestParams;
    private final JScrollPane mScrollPaneRequestParams;

    private final JPanel mPanelRequestHeaders;
    private final EditableTable mTableRequestHeaders;
    private final JScrollPane mScrollPaneRequestHeaders;

    private final JPanel mPanelRequestBody;
    private final JPanel mPanelRequestBodyType;
    private final JRadioButton mRadioButtonRequestBodyNone;
    private final JRadioButton mRadioButtonRequestBodyText;
    private final JRadioButton mRadioButtonRequestBodyJson;
    private final JRadioButton mRadioButtonBodyBinary;

    private final JPanel mPanelRequestBodyDetail;
    private final JPanel mPanelRequestBodyNone;
    private final JPanel mPanelRequestBodyText;
    private final JPanel mPanelRequestBodyJson;
    private final JPanel mPanelRequestBodyBinary;

    private final JButton mButtonChooseInputFile;
    private final JLabel mLabelUploadedFile;
    private final TextEditor mTextAreaRequestBodyText;
    private final TextEditor mTextAreaRequestBodyJson;
    private final ScrollPaneEditor mScrollPaneRequestBodyText;
    private final ScrollPaneEditor mScrollPaneRequestBodyJson;
}
