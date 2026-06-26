package com.inhabada.event;

import com.inhabada.entity.ShareRequest;

public class RequestRejectedEvent {

    private final ShareRequest request;
    private final Long receiverId;

    public RequestRejectedEvent(ShareRequest request, Long receiverId) {
        this.request = request;
        this.receiverId = receiverId;
    }

    public ShareRequest getRequest() {
        return request;
    }

    public Long getReceiverId() {
        return receiverId;
    }
}
