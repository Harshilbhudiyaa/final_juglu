package com.infowave.demo.supabase;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface SupabaseApiService {
    @Multipart
    @PUT("storage/v1/object/profile-images/{fileName}")
    Call<ResponseBody> uploadProfileImage(
            @Path("fileName") String fileName,
            @Part MultipartBody.Part file,
            @Header("Authorization") String bearerToken,
            @Header("x-upsert") boolean upsert
    );
}
