package com.cognitivecloud.pexel.network;

import android.content.Context;

import com.cognitivecloud.pexel.model.ImageListResponse;
import com.cognitivecloud.pexel.utils.Constants;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Response;


//Networking using RxJava
public class Service {
    private Context context;
    public Service(Context context){
        this.context=context;
    }


    public void getImageList(final ImageCallback callback, int page, String query){
        NetworkAPI.getClient(context).create(NetworkService.class).getImageList(Constants.API_KEY,Constants.PER_PAGE, query,page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<ImageListResponse>() {
                @Override
                public void onSubscribe(Disposable d) {
                    callback.getDisposable(d);
                }

                @Override
                public void onNext(ImageListResponse response) {
                    callback.onSuccess(response);
                }

                @Override
                public void onError(Throwable e) {
                    callback.onError(new NetworkError(e));
                }

                @Override
                public void onComplete() { }
            });
    }


    public void getImage(final DownloadCallback callback, String url, Context context) {
        NetworkAPI.getClient(context).create(NetworkService.class).downloadFile(url)
                .flatMap(new Function<Response<ResponseBody>, Observable<File>>() {
                    @Override
                    public Observable<File> apply(Response<ResponseBody> response) throws Exception {

                        try {
                            File directory  = new File(context.getFilesDir(),"Pexel");
                            if(!directory.exists()){
                                directory.mkdir();
                            }
                            File file = new File(directory,System.currentTimeMillis()+"."+ getExtention(url));

                            BufferedSink sink = Okio.buffer(Okio.sink(file));
                            sink.writeAll(response.body().source());
                            sink.close();
                            return Observable.just(file);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        callback.getDisposable(d);
                    }

                    @Override
                    public void onNext(File file) {
                        callback.onSuccess(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(new NetworkError(e));
                    }

                    @Override
                    public void onComplete() { }
                });
    }



    public interface ImageCallback{
        void onSuccess(ImageListResponse response);
        void onError(NetworkError networkError);
        void getDisposable(Disposable disposable);
    }
    public interface DownloadCallback{
        void onSuccess(File file);
        void onError(NetworkError networkError);
        void getDisposable(Disposable disposable);
    }

    private  String getExtention(String fileName){
        char[] arrayOfFilename = fileName.toCharArray();
        for(int i = arrayOfFilename.length-1; i > 0; i--){
            if(arrayOfFilename[i] == '.'){
                return fileName.substring(i+1, fileName.length());
            }
        }
        return "";
    }
}

