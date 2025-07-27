package com.infowave.demo.supabase;

import android.content.Context;
import android.net.Uri;
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

    // Helper: read bytes from input stream
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

    public static void uploadProfileImage(Context context, Uri imageUri, String userId, ImageUploadCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onFailure("Cannot open input stream from URI!");
                return;
            }
            byte[] bytes = getBytes(inputStream);

            String extension = "jpg";
            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType != null) {
                if (mimeType.contains("png")) extension = "png";
                else if (mimeType.contains("jpeg") || mimeType.contains("jpg")) extension = "jpg";
            }

            String fileName = "user_" + userId + "_" + System.currentTimeMillis() + "." + extension;
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType != null ? mimeType : "image/jpeg"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            SupabaseApiService apiService = getApiService();
            String anonKey = SupabaseClient.getAnonKey();

            Call<ResponseBody> call = apiService.uploadProfileImage(
                    fileName, body, "Bearer " + anonKey, true
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        String baseUrl = SupabaseClient.getBaseUrl();
                        if (!baseUrl.endsWith("/")) baseUrl += "/";
                        String publicUrl = baseUrl + "storage/v1/object/public/profile-images/" + fileName;
                        callback.onSuccess(publicUrl);
                    } else {
                        String errorMsg = "";
                        try { if (response.errorBody() != null) errorMsg = response.errorBody().string(); }
                        catch (Exception ex) { errorMsg = ex.getMessage(); }
                        callback.onFailure("Upload failed: HTTP " + response.code() + ", " + errorMsg);
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onFailure("Network/Retrofit error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onFailure("Upload failed: " + e.getMessage());
        }
    }

    public static void uploadChatImage(Context context, Uri imageUri, String userId, ImageUploadCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onFailure("Cannot open input stream from URI!");
                return;
            }
            byte[] bytes = getBytes(inputStream);

            String extension = "jpg";
            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType != null) {
                if (mimeType.contains("png")) extension = "png";
                else if (mimeType.contains("jpeg") || mimeType.contains("jpg")) extension = "jpg";
            }

            String fileName = "images/chat_" + userId + "_" + System.currentTimeMillis() + "." + extension;
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType != null ? mimeType : "image/jpeg"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            SupabaseApiService apiService = getApiService();
            String jwt = SupabaseClient.getJwtFromPrefs(context);
            if (jwt == null || jwt.isEmpty()) jwt = SupabaseClient.getAnonKey();

            Call<ResponseBody> call = apiService.uploadChatImage(
                    fileName, body, "Bearer " + jwt, true
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        String baseUrl = SupabaseClient.getBaseUrl();
                        if (!baseUrl.endsWith("/")) baseUrl += "/";
                        String publicUrl = baseUrl + "storage/v1/object/public/chat-media/" + fileName;
                        callback.onSuccess(publicUrl);
                    } else {
                        String errorMsg = "";
                        try { if (response.errorBody() != null) errorMsg = response.errorBody().string(); }
                        catch (Exception ex) { errorMsg = ex.getMessage(); }
                        callback.onFailure("Chat image upload failed: HTTP " + response.code() + ", " + errorMsg);
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onFailure("Network/Retrofit error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onFailure("Chat image upload failed: " + e.getMessage());
        }
    }

    public static void uploadChatVideo(Context context, Uri videoUri, String userId, ImageUploadCallback callback) {
        try {
            android.util.Log.d("MEDIA_UPLOAD", "Starting chat video upload: userId=" + userId + ", uri=" + videoUri);

            InputStream inputStream = context.getContentResolver().openInputStream(videoUri);
            if (inputStream == null) {
                android.util.Log.e("MEDIA_UPLOAD", "Cannot open input stream from URI: " + videoUri);
                callback.onFailure("Cannot open input stream from URI!");
                return;
            }
            byte[] bytes = getBytes(inputStream);
            android.util.Log.d("MEDIA_UPLOAD", "Read " + bytes.length + " bytes from video input stream.");

            String extension = "mp4";
            String mimeType = context.getContentResolver().getType(videoUri);
            if (mimeType != null && mimeType.contains("webm")) extension = "webm";

            String fileName = "videos/chat_" + userId + "_" + System.currentTimeMillis() + "." + extension;
            android.util.Log.d("MEDIA_UPLOAD", "Uploading as filename: " + fileName + " (" + mimeType + ")");

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType != null ? mimeType : "video/mp4"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            SupabaseApiService apiService = getApiService();
            String jwt = SupabaseClient.getJwtFromPrefs(context);
            android.util.Log.d("MEDIA_UPLOAD", "Using JWT: " + (jwt != null && !jwt.isEmpty() ? "Present" : "Missing! (should not happen)"));

            Call<ResponseBody> call = apiService.uploadFileToStorage(
                    "chat-media", fileName, body, "Bearer " + jwt, true
            );
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    android.util.Log.d("MEDIA_UPLOAD", "Upload response received: HTTP " + response.code());
                    if (response.isSuccessful()) {
                        String baseUrl = SupabaseClient.getBaseUrl();
                        if (!baseUrl.endsWith("/")) baseUrl += "/";
                        String publicUrl = baseUrl + "storage/v1/object/public/chat-media/" + fileName;
                        android.util.Log.d("MEDIA_UPLOAD", "Chat video upload success: " + publicUrl);
                        callback.onSuccess(publicUrl);
                    } else {
                        String errorMsg = "";
                        try { if (response.errorBody() != null) errorMsg = response.errorBody().string(); }
                        catch (Exception ex) { errorMsg = ex.getMessage(); }
                        android.util.Log.e("MEDIA_UPLOAD", "Video upload failed: HTTP " + response.code() + ", body: " + errorMsg);
                        callback.onFailure("Video upload failed: HTTP " + response.code() + ", " + errorMsg);
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    android.util.Log.e("MEDIA_UPLOAD", "Network/Retrofit error during video upload: " + t.getMessage(), t);
                    callback.onFailure("Network/Retrofit error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            android.util.Log.e("MEDIA_UPLOAD", "Exception during chat video upload: " + e.getMessage(), e);
            callback.onFailure("Video upload failed: " + e.getMessage());
        }
    }

    public static void uploadChatAudio(Context context, String audioPath, String userId, ImageUploadCallback callback) {
        try {
            java.io.File audioFile = new java.io.File(audioPath);
            if (!audioFile.exists()) {
                callback.onFailure("Audio file does not exist!");
                return;
            }
            byte[] bytes = getBytes(new java.io.FileInputStream(audioFile));

            String extension = "m4a"; // Or derive from filename if needed
            String fileName = "audio/chat_" + userId + "_" + System.currentTimeMillis() + "." + extension;
            String mimeType = "audio/mp4"; // For .m4a, this is generally fine

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            SupabaseApiService apiService = getApiService();
            String jwt = SupabaseClient.getJwtFromPrefs(context);
            if (jwt == null || jwt.isEmpty()) jwt = SupabaseClient.getAnonKey();

            // Use your API to upload to "chat-media" bucket
            Call<ResponseBody> call = apiService.uploadFileToStorage(
                    "chat-media", fileName, body, "Bearer " + jwt, true
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        String baseUrl = SupabaseClient.getBaseUrl();
                        if (!baseUrl.endsWith("/")) baseUrl += "/";
                        String publicUrl = baseUrl + "storage/v1/object/public/chat-media/" + fileName;
                        callback.onSuccess(publicUrl);
                    } else {
                        String errorMsg = "";
                        try { if (response.errorBody() != null) errorMsg = response.errorBody().string(); }
                        catch (Exception ex) { errorMsg = ex.getMessage(); }
                        callback.onFailure("Audio upload failed: HTTP " + response.code() + ", " + errorMsg);
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onFailure("Network/Retrofit error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onFailure("Audio upload failed: " + e.getMessage());
        }
    }

}
