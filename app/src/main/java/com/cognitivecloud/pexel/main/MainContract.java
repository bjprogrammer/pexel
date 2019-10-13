package com.cognitivecloud.pexel.main;

import com.cognitivecloud.pexel.model.ImageListResponse;

class MainContract{

    interface MainView {
        void showWait();
        void removeWait();
        void onFailure(String appErrorMessage);
        void onSuccess(ImageListResponse response);
    }

    interface MainPresenter{
         void getImageList(int page, String query);
         void unSubscribe();
    }
}
