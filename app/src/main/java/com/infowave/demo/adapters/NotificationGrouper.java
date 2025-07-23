package com.infowave.demo.adapters;

import com.infowave.demo.models.NotificationEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationGrouper {

    public static List<NotificationEvent> groupLikes(List<NotificationEvent> input) {
        Map<String, GroupBucket> map = new HashMap<>();
        List<NotificationEvent> others = new ArrayList<>();

        for (NotificationEvent ev : input) {
            switch (ev.getType()) {
                case LIKE_POST:
                case LIKE_STORY:
                    String key = ev.getType().name() + "|" + safe(ev.getObjectId()) + "|" + dayBucket(ev.getCreatedAt());
                    GroupBucket bucket = map.get(key);
                    if (bucket == null) {
                        bucket = new GroupBucket(ev);
                        map.put(key, bucket);
                    }
                    bucket.add(ev);
                    break;
                default:
                    others.add(ev);
            }
        }

        List<NotificationEvent> out = new ArrayList<>(others);
        for (GroupBucket b : map.values()) {
            out.add(b.toMergedEvent());
        }
        return out;
    }

    private static String safe(String s) { return s == null ? "null" : s; }

    private static String dayBucket(String iso) {
        if (iso == null) return "null";
        int idx = iso.indexOf('T');
        return idx > 0 ? iso.substring(0, idx) : iso;
        // Simple bucket by date; adjust if needed
    }

    private static class GroupBucket {
        NotificationEvent first;
        List<NotificationEvent> list = new ArrayList<>();

        GroupBucket(NotificationEvent f) {
            this.first = f;
            list.add(f);
        }

        void add(NotificationEvent e) {
            list.add(e);
        }

        NotificationEvent toMergedEvent() {
            if (list.size() == 1) return first;

            String name1 = list.get(0).getFromUserName();
            String name2 = list.size() > 1 ? list.get(1).getFromUserName() : "";
            int others = list.size() - 2;

            String msg;
            boolean story = first.getType() == NotificationEvent.Type.LIKE_STORY;
            String target = story ? "story" : "post";

            if (others > 0) {
                msg = name1 + " and " + name2 + " and " + others + " others liked your " + target;
            } else {
                msg = name1 + " and " + name2 + " liked your " + target;
            }

            NotificationEvent ev = new NotificationEvent();
            ev.setId(first.getId());
            ev.setCreatedAt(first.getCreatedAt());
            ev.setType(first.getType());
            ev.setObjectId(first.getObjectId());
            ev.setThumbnailUrl(first.getThumbnailUrl());
            ev.setFromUserId(first.getFromUserId());
            ev.setFromUserName(name1);
            ev.setFromUserAvatarUrl(first.getFromUserAvatarUrl());
            ev.setMessage(msg);
            return ev;
        }
    }
}
