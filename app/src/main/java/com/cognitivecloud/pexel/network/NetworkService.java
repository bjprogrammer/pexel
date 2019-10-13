package com.cognitivecloud.pexel.network;

import com.cognitivecloud.pexel.model.ImageListResponse;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

//ALL API calls endpoints
public interface NetworkService {
    @GET("v1/search")
    Observable<ImageListResponse> getImageList(@Header("Authorization") String apiKey,  @Query("per_page") int pagesize, @Query("query") String query, @Query("page") int page);

    @Streaming
    @GET
    Observable<Response<ResponseBody>> downloadFile(@Url String fileUrl);
}

