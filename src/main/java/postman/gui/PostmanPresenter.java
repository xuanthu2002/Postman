package postman.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postman.gui.components.RequestPanel;
import postman.gui.components.ResponsePanel;
import postman.util.HttpClient;
import postman.util.HttpRequest;
import postman.util.HttpResponse;

public class PostmanPresenter implements PostmanContract.Presenter {
    private static final Logger log = LoggerFactory.getLogger(PostmanPresenter.class);
    private PostmanContract.RequestView mRequestView;
    private PostmanContract.ResponseView mResponseView;

    public void setRequestView(PostmanContract.RequestView requestView) {
        this.mRequestView = requestView;
    }

    public void setResponseView(PostmanContract.ResponseView responseView) {
        this.mResponseView = responseView;
    }

    @Override
    public void onClickCreateNewRequest() {
        ((RequestPanel) mRequestView).resetInput();
        ((ResponsePanel) mResponseView).resetOutput();
    }

    @Override
    public void onClickImport() {

    }

    @Override
    public void onClickExport() {
    }

    @Override
    public void initRequest() {
//        HttpRequest request = Values.DEFAULT_REQUEST;
//        mRequestView.setRequest(request);
    }

    @Override
    public void onClickSendRequest() {
        HttpRequest request = mRequestView.getCurrentRequest();
        mRequestView.onSendingRequest();
        mResponseView.onSendingRequest();
        HttpClient.send(request, new HttpClient.OnResultListener() {
            @Override
            public void onSuccess(HttpResponse response) {
                mResponseView.onSuccess(response);
                mRequestView.onSent();
            }

            @Override
            public void onFailure(Exception e) {
                mResponseView.onFailure(e);
                mRequestView.onSent();
            }
        });
    }

    @Override
    public void onClickCancel() {
        HttpClient.cancel();
        mRequestView.onSent();
    }
}
