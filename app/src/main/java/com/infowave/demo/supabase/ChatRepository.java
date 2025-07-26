package com.infowave.demo.supabase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import com.infowave.demo.models.ChatMessage;

import timber.log.Timber;

public class ChatRepository {

    private static final String TAG = "JugluChatRepo";
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ======= Callback interface =======
    public interface ChatCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    // ========= 1️⃣ Send a new message =========
    @SuppressLint("BinaryOperationInTimber")
    public static void sendMessage(
            Context context,
            String senderId,
            String receiverId,
            String content,
            ChatCallback<ChatMessage> callback
    ) {
        Timber.tag(TAG).d("sendMessage: senderId=" + senderId + ", receiverId=" + receiverId + ", content=" + content);

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/messages";
        @SuppressLint("LogNotTimber")
        JSONObject body = new JSONObject();
        try {
            body.put("sender_id", senderId);
            body.put("receiver_id", receiverId);
            body.put("content", content);
        } catch (JSONException e) {
            Timber.tag(TAG).e("sendMessage: JSONException in request body: " + e.getMessage());
            postFailure(callback, "JSON Error: " + e.getMessage());
            return;
        }
        @SuppressLint("LogNotTimber")
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.d(TAG, "sendMessage: Network response: " + response.toString());
                    try {
                        ChatMessage msg = ChatMessage.fromJson(response, senderId);
                        postSuccess(callback, msg);
                    } catch (JSONException e) {
                        Log.e(TAG, "sendMessage: JSONException in onResponse: " + e.getMessage());
                        postFailure(callback, "JSON Parse Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "sendMessage: Volley error: " + error.toString());
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorBody = new String(error.networkResponse.data);
                        Log.e(TAG, "sendMessage: Body: " + errorBody);
                    }
                    postFailure(callback, "Network Error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = SupabaseClient.getHeaders(context);
                headers.put("Prefer", "return=representation");
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, com.android.volley.toolbox.HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    if (jsonString.trim().startsWith("[")) {
                        JSONArray arr = new JSONArray(jsonString);
                        if (arr.length() > 0) {
                            JSONObject obj = arr.getJSONObject(0);
                            return Response.success(obj, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                        } else {
                            return Response.success(new JSONObject(), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        return Response.success(jsonObject, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "sendMessage: parseNetworkResponse exception: " + e.getMessage());
                    return Response.error(new com.android.volley.ParseError(e));
                }
            }
        };

        // ⚠️ RECOMMEND: In your UI, disable the send button until this callback returns (prevents multi-tap bugs)
        SupabaseClient.getInstance(context).getRequestQueue().add(request);
    }
    // ========= Send a call invite message =========
    public static void sendCallInvite(
            Context context,
            String senderId,
            String receiverId,
            String roomName,
            String callType, // "audio" or "video"
            ChatCallback<ChatMessage> callback
    ) {
        Timber.tag(TAG).d("sendCallInvite: senderId=" + senderId + ", receiverId=" + receiverId
                + ", roomName=" + roomName + ", callType=" + callType);

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/messages";
        JSONObject body = new JSONObject();
        try {
            body.put("sender_id", senderId);
            body.put("receiver_id", receiverId);
            body.put("type", "call_invite");
            // Store room and callType as JSON string in 'content'
            JSONObject callContent = new JSONObject();
            callContent.put("room", roomName);
            callContent.put("call_type", callType);
            body.put("content", callContent.toString());
        } catch (JSONException e) {
            Timber.tag(TAG).e("sendCallInvite: JSONException in request body: " + e.getMessage());
            postFailure(callback, "JSON Error: " + e.getMessage());
            return;
        }
        @SuppressLint({"LogNotTimber", "BinaryOperationInTimber"})
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.d(TAG, "sendCallInvite: Network response: " + response.toString());
                    try {
                        ChatMessage msg = ChatMessage.fromJson(response, senderId);
                        postSuccess(callback, msg);
                    } catch (JSONException e) {
                        Timber.tag(TAG).e("sendCallInvite: JSONException in onResponse: " + e.getMessage());
                        postFailure(callback, "JSON Parse Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "sendCallInvite: Volley error: " + error.toString());
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorBody = new String(error.networkResponse.data);
                        Log.e(TAG, "sendCallInvite: Body: " + errorBody);
                    }
                    postFailure(callback, "Network Error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = SupabaseClient.getHeaders(context);
                headers.put("Prefer", "return=representation");
                return headers;
            }
            @SuppressLint("LogNotTimber")
            @Override
            protected Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, com.android.volley.toolbox.HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    if (jsonString.trim().startsWith("[")) {
                        JSONArray arr = new JSONArray(jsonString);
                        if (arr.length() > 0) {
                            JSONObject obj = arr.getJSONObject(0);
                            return Response.success(obj, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                        } else {
                            return Response.success(new JSONObject(), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        return Response.success(jsonObject, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "sendCallInvite: parseNetworkResponse exception: " + e.getMessage());
                    return Response.error(new com.android.volley.ParseError(e));
                }
            }
        };

        SupabaseClient.getInstance(context).getRequestQueue().add(request);
    }


    // ========= 2️⃣ Fetch all messages between two users =========
    public static void fetchMessagesBetweenUsers(
            Context context,
            String currentUserId,
            String otherUserId,
            ChatCallback<List<ChatMessage>> callback
    ) {
        Timber.tag(TAG).d("fetchMessagesBetweenUsers: currentUserId=" + currentUserId + ", otherUserId=" + otherUserId);

        String url = SupabaseClient.getBaseUrl() +
                "/rest/v1/messages?or=(and(sender_id.eq." + currentUserId +
                ",receiver_id.eq." + otherUserId + "),and(sender_id.eq." + otherUserId +
                ",receiver_id.eq." + currentUserId + "))&order=created_at.asc";
        @SuppressLint("LogNotTimber")
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<ChatMessage> messages = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            messages.add(ChatMessage.fromJson(obj, currentUserId));
                        }
                        postSuccess(callback, messages);
                        Log.d(TAG, "fetchMessagesBetweenUsers: loaded " + messages.size() + " messages");
                    } catch (JSONException e) {
                        Log.e(TAG, "fetchMessagesBetweenUsers: JSON Error: " + e.getMessage());
                        postFailure(callback, "JSON Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "fetchMessagesBetweenUsers: Network Error: " + error.toString());
                    postFailure(callback, "Network Error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(request);
    }

    // ========= 3️⃣ Fetch friend list WITH user profile (accepted only) =========
    public static void fetchFriendsWithProfiles(
            Context context,
            String currentUserId,
            ChatCallback<List<FriendProfile>> callback
    ) {
        Log.d(TAG, "fetchFriendsWithProfiles: currentUserId=" + currentUserId);

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/friendships?or=(user_one.eq." + currentUserId
                + ",user_two.eq." + currentUserId + ")&status=eq.accepted";

        @SuppressLint("LogNotTimber")
        JsonArrayRequest friendshipsReq = new JsonArrayRequest(Request.Method.GET, url, null,
                friendshipsArr -> {
                    Set<String> friendIds = new HashSet<>();
                    try {
                        for (int i = 0; i < friendshipsArr.length(); i++) {
                            JSONObject obj = friendshipsArr.getJSONObject(i);
                            String userOne = obj.getString("user_one");
                            String userTwo = obj.getString("user_two");
                            String friendId = userOne.equals(currentUserId) ? userTwo : userOne;
                            if (!friendId.equals(currentUserId)) friendIds.add(friendId);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "fetchFriendsWithProfiles: JSON Error: " + e.getMessage());
                        postFailure(callback, "JSON Error: " + e.getMessage());
                        return;
                    }

                    if (friendIds.isEmpty()) {
                        postSuccess(callback, new ArrayList<>());
                        return;
                    }

                    StringBuilder idsIn = new StringBuilder();
                    for (String id : friendIds) {
                        if (idsIn.length() > 0) idsIn.append(",");
                        idsIn.append(id);
                    }

                    String usersUrl = SupabaseClient.getBaseUrl()
                            + "/rest/v1/users?id=in.(" + idsIn + ")";

                    JsonArrayRequest usersReq = new JsonArrayRequest(Request.Method.GET, usersUrl, null,
                            usersArr -> {
                                List<FriendProfile> profiles = new ArrayList<>();
                                try {
                                    for (int i = 0; i < usersArr.length(); i++) {
                                        JSONObject userObj = usersArr.getJSONObject(i);
                                        profiles.add(FriendProfile.fromJson(userObj));
                                    }
                                    postSuccess(callback, profiles);
                                } catch (JSONException e) {
                                    Log.e(TAG, "fetchFriendsWithProfiles: JSON Error: " + e.getMessage());
                                    postFailure(callback, "JSON Error: " + e.getMessage());
                                }
                            },
                            error -> {
                                Log.e(TAG, "fetchFriendsWithProfiles: Network Error: " + error.toString());
                                postFailure(callback, "Network Error: " + error.toString());
                            }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            return SupabaseClient.getHeaders(context);
                        }
                    };
                    SupabaseClient.getInstance(context).getRequestQueue().add(usersReq);
                },
                error -> {
                    Log.e(TAG, "fetchFriendsWithProfiles: Network Error: " + error.toString());
                    postFailure(callback, "Network Error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(friendshipsReq);
    }

    // ========= 4️⃣ Fetch latest message with each friend (preview) =========
    @SuppressLint("LogNotTimber")
    public static void fetchLastMessageWithFriend(
            Context context,
            String currentUserId,
            String friendUserId,
            ChatCallback<ChatMessage> callback
    ) {
        Log.d(TAG, "fetchLastMessageWithFriend: currentUserId=" + currentUserId + ", friendUserId=" + friendUserId);

        String url = SupabaseClient.getBaseUrl() +
                "/rest/v1/messages?or=(and(sender_id.eq." + currentUserId +
                ",receiver_id.eq." + friendUserId + "),and(sender_id.eq." + friendUserId +
                ",receiver_id.eq." + currentUserId + "))&order=created_at.desc&limit=1";

        @SuppressLint("LogNotTimber") JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() == 0) {
                        postSuccess(callback, null);
                        return;
                    }
                    try {
                        JSONObject obj = response.getJSONObject(0);
                        postSuccess(callback, ChatMessage.fromJson(obj, currentUserId));
                    } catch (JSONException e) {
                        Log.e(TAG, "fetchLastMessageWithFriend: JSON Error: " + e.getMessage());
                        postFailure(callback, "JSON Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "fetchLastMessageWithFriend: Network Error: " + error.toString());
                    postFailure(callback, "Network Error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(req);
    }

    // ========= 5️⃣ Fetch all chat people with their latest message (Inbox Preview) =========
    public static void fetchChatPeople(
            Context context,
            String currentUserId,
            ChatCallback<List<ChatPersonPreview>> callback
    ) {
        fetchFriendsWithProfiles(context, currentUserId, new ChatCallback<List<FriendProfile>>() {
            @Override
            public void onSuccess(List<FriendProfile> friendProfiles) {
                if (friendProfiles.isEmpty()) {
                    postSuccess(callback, new ArrayList<>());
                    return;
                }

                List<ChatPersonPreview> previews = new ArrayList<>();
                int[] completed = {0};
                for (FriendProfile profile : friendProfiles) {
                    fetchLastMessageWithFriend(context, currentUserId, profile.id, new ChatCallback<ChatMessage>() {
                        @Override
                        public void onSuccess(ChatMessage lastMessage) {
                            ChatPersonPreview preview = new ChatPersonPreview();
                            preview.friendProfile = profile;
                            preview.lastMessage = lastMessage;
                            previews.add(preview);

                            completed[0]++;
                            if (completed[0] == friendProfiles.size()) {
                                previews.sort((a, b) -> {
                                    if (a.lastMessage == null && b.lastMessage == null) return 0;
                                    if (a.lastMessage == null) return 1;
                                    if (b.lastMessage == null) return -1;
                                    return b.lastMessage.getCreatedAt().compareTo(a.lastMessage.getCreatedAt());
                                });
                                postSuccess(callback, previews);
                            }
                        }
                        @Override
                        public void onFailure(String error) {
                            completed[0]++;
                            if (completed[0] == friendProfiles.size()) {
                                postSuccess(callback, previews);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                postFailure(callback, error);
            }
        });
    }

    // ========= Handler for UI thread-safe callback =========
    private static <T> void postSuccess(ChatCallback<T> callback, T result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private static void postFailure(ChatCallback<?> callback, String error) {
        mainHandler.post(() -> callback.onFailure(error));
    }

    // ========= Model: FriendProfile =========
    public static class FriendProfile {
        public String id, fullName, username, phone, bio, profileImage;
        public boolean isPrivate;
        public String lastActive;
        public String status;

        public static FriendProfile fromJson(JSONObject obj) throws JSONException {
            FriendProfile f = new FriendProfile();
            f.id = obj.optString("id");
            f.fullName = obj.optString("full_name");
            f.username = obj.optString("username");
            f.phone = obj.optString("phone");
            f.bio = obj.optString("bio");
            f.profileImage = obj.optString("profile_image");
            f.isPrivate = obj.optBoolean("is_private");
            f.lastActive = obj.optString("last_active");
            f.status = obj.optString("status");
            return f;
        }
    }

    // ========= Model: ChatPersonPreview (for Inbox/Chats list) =========
    public static class ChatPersonPreview {
        public FriendProfile friendProfile;
        public ChatMessage lastMessage;
    }

    // ========= Send media (image/video/audio) message =========
    @SuppressLint("LogNotTimber")
    public static void sendMediaMessage(
            Context context,
            String senderId,
            String receiverId,
            String mediaUrl,
            String type, // "image", "video", "audio"
            ChatCallback<ChatMessage> callback
    ) {
        Log.d(TAG, "sendMediaMessage: senderId=" + senderId + ", receiverId=" + receiverId + ", mediaUrl=" + mediaUrl + ", type=" + type);

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/messages";
        JSONObject body = new JSONObject();
        try {
            body.put("sender_id", senderId);
            body.put("receiver_id", receiverId);
            body.put("content", ""); // No text content
            body.put("type", type);  // "image", "video", "audio"
            body.put("media_url", mediaUrl); // URL to uploaded file
        } catch (JSONException e) {
            Log.e(TAG, "sendMediaMessage: JSONException in request body: " + e.getMessage());
            postFailure(callback, "JSON Error: " + e.getMessage());
            return;
        }

        @SuppressLint("LogNotTimber") JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.d(TAG, "sendMediaMessage: Network response: " + response.toString());
                    try {
                        ChatMessage msg = ChatMessage.fromJson(response, senderId);
                        postSuccess(callback, msg);
                    } catch (JSONException e) {
                        Log.e(TAG, "sendMediaMessage: JSONException in onResponse: " + e.getMessage());
                        postFailure(callback, "JSON Parse Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "sendMediaMessage: Volley error: " + error.toString());
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorBody = new String(error.networkResponse.data);
                        Log.e(TAG, "sendMediaMessage: Body: " + errorBody);
                    }
                    postFailure(callback, "Network Error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = SupabaseClient.getHeaders(context);
                headers.put("Prefer", "return=representation");
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, com.android.volley.toolbox.HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    if (jsonString.trim().startsWith("[")) {
                        JSONArray arr = new JSONArray(jsonString);
                        if (arr.length() > 0) {
                            JSONObject obj = arr.getJSONObject(0);
                            return Response.success(obj, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                        } else {
                            return Response.success(new JSONObject(), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        return Response.success(jsonObject, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                    }
                } catch (Exception e) {
                    Timber.tag(TAG).e("sendMediaMessage: parseNetworkResponse exception: %s", e.getMessage());
                    return Response.error(new com.android.volley.ParseError(e));
                }
            }
        };

        SupabaseClient.getInstance(context).getRequestQueue().add(request);
    }

}
