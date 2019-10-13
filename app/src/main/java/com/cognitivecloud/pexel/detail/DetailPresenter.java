package com.cognitivecloud.pexel.detail;

import android.content.Context;
import android.os.Handler;

import com.cognitivecloud.pexel.network.NetworkError;
import com.cognitivecloud.pexel.network.Service;

import java.io.File;

import io.reactivex.disposables.Disposable;

public class DetailPresenter implements DetailContract.DetailPresenter{
    private DetailContract.DetailView view;
    private Disposable dispose;

    private Context context;

    public DetailPresenter(Context context, DetailContract.DetailView view) {
        this.view=view;
        this.context=context;
    }

    public void downloadImage(String url) {
        view.showWait();


        new Service(context).getImage(new Service.DownloadCallback() {
            @Override
            public void onSuccess(File file) {
                view.removeWait();
                view.onSuccess(file);
            }

            @Override
            public void onError(NetworkError networkError) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.removeWait();
                        view.onFailure(networkError.getAppErrorMessage());
                    }
                }, 300);
            }

            @Override
            public void getDisposable(Disposable disposable) {
                dispose=disposable;
            }
        },url, context);
    }

    public void unSubscribe(){
        view = null;

        if(dispose!=null){
            dispose.dispose();
        }
    }
}
