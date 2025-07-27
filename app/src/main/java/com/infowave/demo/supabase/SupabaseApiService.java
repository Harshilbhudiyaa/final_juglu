package com.infowave.demo.supabase;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApiService {
    // Profile Image (uses PUT for 'profile-images' bucket)
    @Multipart
    @PUT("storage/v1/object/profile-images/{fileName}")
    Call<ResponseBody> uploadProfileImage(
            @Path("fileName") String fileName,
            @Part MultipartBody.Part file,
            @Header("Authorization") String bearerToken,
            @Header("x-upsert") boolean upsert
    );

    // Chat Image (uses POST for 'chat-media' bucket)
    @Multipart
    @POST("storage/v1/object/chat-media/{fileName}")
    Call<ResponseBody> uploadChatImage(
            @Path("fileName") String fileName,
            @Part MultipartBody.Part file,
            @Header("Authorization") String authorization,
            @Query("upsert") boolean upsert
    );

    // Generic media upload (video, audio, any file) to 'chat-media' or any bucket
    @Multipart
    @POST("storage/v1/object/{bucket}/{filename}")
    Call<ResponseBody> uploadFileToStorage(
            @Path("bucket") String bucket,
            @Path("filename") String filename,
            @Part MultipartBody.Part file,
            @Header("Authorization") String authHeader,
            @Query("upsert") boolean upsert
    );
}
