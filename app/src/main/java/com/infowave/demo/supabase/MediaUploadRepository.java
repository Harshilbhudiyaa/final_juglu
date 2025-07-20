package com.infowave.demo.supabase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MediaUploadRepository {

    private static Retrofit retrofit = null;

    // The ONLY callback interface used everywhere
    public interface ImageUploadCallback {
        void onSuccess(String publicUrl);
        void onFailure(String error);
    }

    public static Retrofit getRetrofitClient() {
        if (retrofit == null) {
            String baseUrl = SupabaseClient.getBaseUrl();
            if (!baseUrl.endsWith("/")) baseUrl += "/";
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static SupabaseApiService getApiService() {
        return getRetrofitClient().create(SupabaseApiService.class);
    }

    /**
     * Uploads a profile image to Supabase Storage and returns the public URL.
     */
    public static void uploadProfileImage(Context context, Uri imageUri, String userId, ImageUploadCallback callback) {
        try {
            Log.d("MEDIA_UPLOAD", "Starting upload: userId=" + userId + ", uri=" + imageUri);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                String err = "Cannot open input stream from URI!";
                Log.e("MEDIA_UPLOAD", err);
                callback.onFailure(err);
                return;
            }

            byte[] bytes = getBytes(inputStream);

            // Infer file extension from URI/mime
            String extension = "jpg"; // default
            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType != null) {
                if (mimeType.contains("png")) extension = "png";
                else if (mimeType.contains("jpeg") || mimeType.contains("jpg")) extension = "jpg";
            }

            String fileName = "user_" + userId + "_" + System.currentTimeMillis() + "." + extension;
            Log.d("MEDIA_UPLOAD", "Uploading as filename: " + fileName + " (size=" + bytes.length + " bytes)");

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType != null ? mimeType : "image/jpeg"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            SupabaseApiService apiService = getApiService();
            String anonKey = SupabaseClient.getAnonKey();

            // Upload to "profile-images" bucket
            Call<ResponseBody> call = apiService.uploadProfileImage(
                    fileName,
                    body,
                    "Bearer " + anonKey,
                    true // upsert
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("MEDIA_UPLOAD", "Upload response received: HTTP " + response.code());
                    if (response.isSuccessful()) {
                        String baseUrl = SupabaseClient.getBaseUrl();
                        if (!baseUrl.endsWith("/")) baseUrl += "/";
                        String publicUrl = baseUrl + "storage/v1/object/public/profile-images/" + fileName;
                        Log.d("MEDIA_UPLOAD", "Upload success, publicUrl=" + publicUrl);
                        callback.onSuccess(publicUrl);
                    } else {
                        String errorMsg = "";
                        try {
                            if (response.errorBody() != null) {
                                errorMsg = response.errorBody().string();
                            }
                        } catch (Exception ex) {
                            errorMsg = "Error reading errorBody: " + ex.getMessage();
                        }
                        String failMsg = "Upload failed: HTTP " + response.code() + ", " + response.message() + ", body: " + errorMsg;
                        Log.e("MEDIA_UPLOAD", failMsg);
                        callback.onFailure(failMsg);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("MEDIA_UPLOAD", "Retrofit failure: ", t);
                    callback.onFailure("Network/Retrofit error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e("MEDIA_UPLOAD", "Exception during upload: ", e);
            callback.onFailure("Upload failed: " + e.getMessage());
        }
    }

    // Helper to read bytes from InputStream
    private static byte[] getBytes(InputStream inputStream) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
