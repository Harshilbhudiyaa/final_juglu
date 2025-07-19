package com.infowave.demo.supabase;

import android.content.Context;
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

public class ChatRepository {

    // ======= Callback interface =======
    public interface ChatCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    // ========= 1️⃣ Send a new message =========
    public static void sendMessage(
            Context context,
            String senderId,
            String receiverId,
            String content,
            ChatCallback<ChatMessage> callback
    ) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/messages";
        JSONObject body = new JSONObject();
        try {
            body.put("sender_id", senderId);
            body.put("receiver_id", receiverId);
            body.put("content", content);
            Log.d("SEND_MESSAGE", "Request body: " + body.toString());
        } catch (JSONException e) {
            Log.e("SEND_MESSAGE", "JSONException in request body: " + e.getMessage());
            callback.onFailure(e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.d("SEND_MESSAGE", "Volley onResponse (JSONObject): " + response.toString());
                    try {
                        ChatMessage msg = ChatMessage.fromJson(response, senderId);
                        Log.d("SEND_MESSAGE", "Parsed message in onResponse: " + msg.toString());
                        callback.onSuccess(msg);
                    } catch (JSONException e) {
                        Log.e("SEND_MESSAGE", "JSONException in onResponse: " + e.getMessage());
                        callback.onFailure("JSON Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("SEND_MESSAGE", "Volley onErrorResponse: " + error.toString());
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorBody = new String(error.networkResponse.data);
                        Log.e("SEND_MESSAGE", "Body: " + errorBody);
                    }
                    callback.onFailure(error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = SupabaseClient.getHeaders(context); // ✅ Fix: pass context
                headers.put("Prefer", "return=representation");
                Log.d("SEND_MESSAGE", "Request headers: " + headers.toString());
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, com.android.volley.toolbox.HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    Log.d("SEND_MESSAGE", "parseNetworkResponse jsonString: " + jsonString);
                    if (jsonString.trim().startsWith("[")) {
                        JSONArray arr = new JSONArray(jsonString);
                        if (arr.length() > 0) {
                            JSONObject obj = arr.getJSONObject(0);
                            Log.d("SEND_MESSAGE", "parseNetworkResponse: returning first object of array: " + obj.toString());
                            return Response.success(obj, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                        } else {
                            Log.e("SEND_MESSAGE", "parseNetworkResponse: empty array returned from server.");
                            return Response.success(new JSONObject(), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        return Response.success(jsonObject, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                    }
                } catch (Exception e) {
                    Log.e("SEND_MESSAGE", "parseNetworkResponse exception: " + e.getMessage());
                    return Response.error(new com.android.volley.ParseError(e));
                }
            }
        };

        Log.d("SEND_MESSAGE", "Adding request to Volley queue: " + url);
        SupabaseClient.getInstance(context).getRequestQueue().add(request);
    }

    // ========= 2️⃣ Fetch all messages between two users =========
    public static void fetchMessagesBetweenUsers(
            Context context,
            String currentUserId,
            String otherUserId,
            ChatCallback<List<ChatMessage>> callback
    ) {
        String url = SupabaseClient.getBaseUrl() +
                "/rest/v1/messages?or=(and(sender_id.eq." + currentUserId +
                ",receiver_id.eq." + otherUserId + "),and(sender_id.eq." + otherUserId +
                ",receiver_id.eq." + currentUserId + "))&order=created_at.asc";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<ChatMessage> messages = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            messages.add(ChatMessage.fromJson(obj, currentUserId));
                        }
                        callback.onSuccess(messages);
                    } catch (JSONException e) {
                        callback.onFailure(e.getMessage());
                    }
                },
                error -> callback.onFailure(error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context); // ✅ Fix: pass context
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
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/friendships?or=(user_one.eq." + currentUserId
                + ",user_two.eq." + currentUserId + ")&status=eq.accepted";

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
                        callback.onFailure(e.getMessage());
                        return;
                    }

                    if (friendIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
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
                                    callback.onSuccess(profiles);
                                } catch (JSONException e) {
                                    callback.onFailure(e.getMessage());
                                }
                            },
                            error -> callback.onFailure(error.toString())
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            return SupabaseClient.getHeaders(context); // ✅ Fix: pass context
                        }
                    };
                    SupabaseClient.getInstance(context).getRequestQueue().add(usersReq);
                },
                error -> callback.onFailure(error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context); // ✅ Fix: pass context
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(friendshipsReq);
    }

    // ========= 4️⃣ Fetch latest message with each friend (preview) =========
    public static void fetchLastMessageWithFriend(
            Context context,
            String currentUserId,
            String friendUserId,
            ChatCallback<ChatMessage> callback
    ) {
        String url = SupabaseClient.getBaseUrl() +
                "/rest/v1/messages?or=(and(sender_id.eq." + currentUserId +
                ",receiver_id.eq." + friendUserId + "),and(sender_id.eq." + friendUserId +
                ",receiver_id.eq." + currentUserId + "))&order=created_at.desc&limit=1";

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() == 0) {
                        callback.onSuccess(null);
                        return;
                    }
                    try {
                        JSONObject obj = response.getJSONObject(0);
                        callback.onSuccess(ChatMessage.fromJson(obj, currentUserId));
                    } catch (JSONException e) {
                        callback.onFailure(e.getMessage());
                    }
                },
                error -> callback.onFailure(error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context); // ✅ Fix: pass context
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
                    callback.onSuccess(new ArrayList<>());
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
                                callback.onSuccess(previews);
                            }
                        }
                        @Override
                        public void onFailure(String error) {
                            completed[0]++;
                            if (completed[0] == friendProfiles.size()) {
                                callback.onSuccess(previews);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
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
}
