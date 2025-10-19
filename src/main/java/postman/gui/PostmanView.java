package postman.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postman.gui.components.MenuPanel;
import postman.gui.components.RequestPanel;
import postman.gui.components.ResponsePanel;
import postman.gui.constants.Fonts;
import postman.gui.constants.Strings;
import postman.gui.constants.Values;

import javax.swing.*;
import java.awt.*;

public class PostmanView extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(PostmanView.class);

    private final PostmanContract.Presenter mPresenter;

    public PostmanView() {
        UIManager.put("TabbedPane.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("TableHeader.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("Table.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("TextArea.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("RadioButton.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("Label.font", Fonts.GENERAL_PLAIN_12);
        UIManager.put("TextField.font", Fonts.GENERAL_PLAIN_12);

        mPresenter = new PostmanPresenter();

        mPanelMenuAbove = new MenuPanel(mPresenter);
        mPanelRequest = new RequestPanel(mPresenter);
        mPanelResponse = new ResponsePanel();

        ((PostmanPresenter) mPresenter).setRequestView(mPanelRequest);
        ((PostmanPresenter) mPresenter).setResponseView(mPanelResponse);

        initComponents();
    }

    private void initComponents() {
        setTitle(Strings.APP_NAME);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setTopComponent(mPanelRequest);
        splitPane.setBottomComponent(mPanelResponse);

        setJMenuBar(mPanelMenuAbove);
        add(splitPane, BorderLayout.CENTER);

        pack();
        setSize(Values.DEFAULT_LAUNCH_SIZE);
        setMinimumSize(Values.DEFAULT_LAUNCH_SIZE);
        setLocationRelativeTo(null);
    }

    private final RequestPanel mPanelRequest;
    private final JMenuBar mPanelMenuAbove;
    private final ResponsePanel mPanelResponse;
}
