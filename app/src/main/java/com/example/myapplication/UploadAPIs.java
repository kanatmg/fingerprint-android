package com.example.myapplication;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadAPIs {
    @Multipart
    @POST("/upload/save")
    Call<ResponseBody> uploadFingerprint(@Part MultipartBody.Part file,
                                         @Part("name") RequestBody name,
                                         @Part("surname") RequestBody surname,
                                         @Part("iin") RequestBody iin);

    @Multipart
    @POST("/upload/register")
    Call<User> ComeInImage(@Part MultipartBody.Part file);

}