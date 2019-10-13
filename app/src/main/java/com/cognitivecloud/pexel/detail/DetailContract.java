package com.cognitivecloud.pexel.detail;

import java.io.File;

public class DetailContract {
    interface DetailView {
        void showWait();
        void removeWait();
        void onFailure(String appErrorMessage);
        void onSuccess(File file);
    }

    interface DetailPresenter{
         void downloadImage(String url);
         void unSubscribe();
    }
}
