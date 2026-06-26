package com.inhabada.event;

import com.inhabada.entity.ShareRequest;

public class RequestApprovedEvent {

    private final ShareRequest request;
    private final Long receiverId;

    public RequestApprovedEvent(ShareRequest request, Long receiverId) {
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
