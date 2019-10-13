package com.cognitivecloud.pexel.main;

import android.content.Context;
import android.os.Handler;

import com.cognitivecloud.pexel.model.ImageListResponse;
import com.cognitivecloud.pexel.network.NetworkError;
import com.cognitivecloud.pexel.network.Service;

import io.reactivex.disposables.Disposable;

public class MainPresenter implements MainContract.MainPresenter{

    private MainContract.MainView view;
    private Disposable disposable;
    private Context context;

    MainPresenter(Context context, MainContract.MainView view) {
        this.view=view;
        this.context=context;
    }

    public void getImageList(int page, String query) {
        if(page==1) {
            view.showWait();
        }

        new Service(context).getImageList(new Service.ImageCallback() {
            @Override
            public void onSuccess(ImageListResponse response)  {

                if(page==1) {
                    view.removeWait();
                }

                if(response!=null) {
                    view.onSuccess(response);
                }
            }

            @Override
            public void onError(final NetworkError networkError) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(page==1) {
                            view.removeWait();
                        }
                        view.onFailure(networkError.getAppErrorMessage());
                    }
                }, 300);
            }

            @Override
            public void getDisposable(Disposable d) {
                disposable=d;
            }
        }, page, query);
    }

    public void unSubscribe(){
        view=null;

        if(disposable!=null){
            disposable.dispose();
        }
    }
}
