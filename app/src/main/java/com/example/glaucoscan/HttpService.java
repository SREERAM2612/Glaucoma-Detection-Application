package com.example.glaucoscan;
import okhttp3.MultipartBody;
import retrofit2.Call;
//import retrofit2.http.Multipart;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface HttpService {
//    @Multipart
    @POST("/prod/customlabels")
    Call<FileModel> callUploadAPI(@Body FileModel fileModel);

}
