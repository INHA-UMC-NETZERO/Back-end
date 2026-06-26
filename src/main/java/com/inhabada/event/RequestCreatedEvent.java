package com.inhabada.event;

import com.inhabada.entity.ShareRequest;

public class RequestCreatedEvent {

    private final ShareRequest request;
    private final Long postId;
    private final Long giverId;

    public RequestCreatedEvent(ShareRequest request, Long postId, Long giverId) {
        this.request = request;
        this.postId = postId;
        this.giverId = giverId;
    }

    public ShareRequest getRequest() {
        return request;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getGiverId() {
        return giverId;
    }
}
