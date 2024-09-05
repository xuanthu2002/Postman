package postman.gui.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import postman.exception.DecompressException;
import postman.gui.PostmanContract;
import postman.gui.constants.Colors;
import postman.gui.constants.Fonts;
import postman.util.Cookie;
import postman.util.Decompress;
import postman.util.HttpResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.zip.DataFormatException;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class ResponsePanel extends JPanel implements PostmanContract.ResponseView {
    public ResponsePanel() {
        super();

        mPanelResponseStatus = new JPanel();
        mLabelResponseStatus = new JLabel();
        mPanelResponseDetail = new JPanel();
        mTabbedPaneResponse = new JTabbedPane();

        mPanelResponseBody = new JPanel();
        mTextAreaResponseBody = new TextEditor();
        mScrollPaneResponseBody = new ScrollPaneEditor(mTextAreaResponseBody);

        mPanelResponseCookies = new JPanel();
        mTableResponseCookies = new EditableTable();
        mScrollPaneResponseCookies = new JScrollPane(mTableResponseCookies, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mPanelResponseHeaders = new JPanel();
        mTableResponseHeaders = new EditableTable();
        mScrollPaneResponseHeaders = new JScrollPane(mTableResponseHeaders, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        setupPanelResponseStatus();
        this.add(mPanelResponseStatus, BorderLayout.PAGE_START);

        setupPanelResponseDetail();
        this.add(mPanelResponseDetail, BorderLayout.CENTER);
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
                new String[]{
                        "Key", "Value"
                }, 0
        ) {
            final Class[] types = new Class[]{
                    String.class, String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });

        mPanelResponseHeaders.add(mScrollPaneResponseHeaders, BorderLayout.CENTER);
    }

    private void setupPanelResponseCookies() {
        mPanelResponseCookies.setLayout(new BoxLayout(mPanelResponseCookies, BoxLayout.LINE_AXIS));

        mTableResponseCookies.setModel(new DefaultTableModel(
                new String[]{
                        "Name", "Value", "Domain", "Path", "Expires", "HttpOnly", "Secure"
                }, 0
        ) {
            final Class[] types = new Class[]{
                    String.class, String.class, String.class, String.class, String.class, String.class, String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });

        mPanelResponseCookies.add(mScrollPaneResponseCookies);
    }

    private void setupPanelResponseBody() {
        mPanelResponseBody.setLayout(new BorderLayout());
        mTextAreaResponseBody.setHighlightCurrentLine(false);
        mTextAreaResponseBody.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mPanelResponseBody.add(mScrollPaneResponseBody, BorderLayout.CENTER);
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

    private void showResponse(HttpResponse response) throws DecompressException, DataFormatException, IOException {
        String charset = "utf8";
        for (String i : Optional.ofNullable(response.getHeader("Content-Type")).orElse(new ArrayList<>())) {
            if (i.toLowerCase().contains("charset")) {
                charset = i.substring(i.indexOf("charset=") + 8);
            }
            if (i.toLowerCase().contains("text/html")) {
                mTextAreaResponseBody.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            } else if (i.toLowerCase().contains("application/json")) {
                mTextAreaResponseBody.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            } else {
                mTextAreaResponseBody.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            }
        }

        String contentEncoding = Optional
                .ofNullable(response.getHeader("Content-Encoding"))
                .orElse(Collections.singletonList("")).get(0);

        String content;
        switch (contentEncoding.toLowerCase()) {
            case "gzip": {
                content = Decompress.decompressGzip(response.getBody(), charset);
                break;
            }
            case "deflate": {
                content = Decompress.decompressDeflate(response.getBody(), charset);
                break;
            }
            case "br": {
                content = Decompress.decompressBrotli(response.getBody(), charset);
                break;
            }
            default: {
                content = new String(response.getBody(), charset);
            }
        }
        if (mTextAreaResponseBody.getSyntaxEditingStyle().equals(SyntaxConstants.SYNTAX_STYLE_JSON)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            content = writer.writeValueAsString(mapper.readTree(content));
        }
        mTextAreaResponseBody.setText(content);
        mTextAreaResponseBody.setCaretPosition(0);

        mLabelResponseStatus.setText(response.getStatusCode() + " " + response.getStatusMessage());

//        if (response.getHeader("Content-Type").get(0).equals("image/png")) {
//            FileNameExtensionFilter filter = new FileNameExtensionFilter("png", "PNG");
//            mFileChoose.setFileFilter(filter);
//            mFileChoose.setSelectedFile(new File(""));
//            int x = mFileChoose.showSaveDialog(this);
//
//            if (x == JFileChooser.APPROVE_OPTION) {
//                String direct = mFileChoose.getSelectedFile().toString();
//                if (!direct.endsWith(".png")) {
//                    direct += ".png";
//                }
//                File file = new File(direct);
//                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
//                    fileOutputStream.write(response.getBody());
//                }
//            }
//        }

        DefaultTableModel model = (DefaultTableModel) mTableResponseHeaders.getModel();
        model.setRowCount(0);
        for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
            for (String value : entry.getValue()) {
                model.addRow(new Object[]{entry.getKey(), value});
            }
        }
        mTableResponseHeaders.setModel(model);

        DefaultTableModel modelCookieTable = (DefaultTableModel) mTableResponseCookies.getModel();
        modelCookieTable.setRowCount(0);
        for (Cookie cookie : response.getCookies()) {
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
    }

    private void resetOutput() {
        mLabelResponseStatus.setText("");
        mTextAreaResponseBody.setText("");
        mTableResponseCookies.clear();
        mTableResponseHeaders.clear();
    }

    private JPanel mPanelResponseStatus;
    private JLabel mLabelResponseStatus;

    private JPanel mPanelResponseDetail;
    private JTabbedPane mTabbedPaneResponse;

    private JPanel mPanelResponseBody;
    private TextEditor mTextAreaResponseBody;
    private ScrollPaneEditor mScrollPaneResponseBody;

    private JPanel mPanelResponseCookies;
    private EditableTable mTableResponseCookies;
    private JScrollPane mScrollPaneResponseCookies;

    private JPanel mPanelResponseHeaders;
    private EditableTable mTableResponseHeaders;
    private JScrollPane mScrollPaneResponseHeaders;

    @Override
    public void onSuccess(HttpResponse response) {
        try {
            showResponse(response);
        } catch (DecompressException e) {
            throw new RuntimeException(e);
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSendingRequest() {
        resetOutput();
    }

    @Override
    public void onFailure(Exception e) {

    }
}
