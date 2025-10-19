package postman.gui;

import postman.util.HttpRequest;
import postman.util.HttpRequestStorage;
import postman.util.HttpResponse;

import java.io.Serializable;

public class PostmanContract {
    public interface RequestView extends Serializable {
        HttpRequest getCurrentRequest();

        HttpRequestStorage getCurrentRequestStorage();

        void setRequest(HttpRequestStorage httpRequest);

        void onSendingRequest();

        void onSent();
    }

    public interface ResponseView extends Serializable {
        void onSuccess(HttpResponse response);

        void onSendingRequest();

        void onFailure(Exception e);
    }

    public interface Presenter extends Serializable {
        void onClickCreateNewRequest();

        void onClickImport(String filePath);

        void onClickExport(String filePath);

        void initRequest();

        void onClickSendRequest();

        void onClickCancel();
    }
}
