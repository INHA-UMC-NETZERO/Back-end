package com.inhabada.event;

import com.inhabada.entity.Post;

public class PostClosedEvent {

    private final Post post;
    private final Long postId;

    public PostClosedEvent(Post post, Long postId) {
        this.post = post;
        this.postId = postId;
    }

    public Post getPost() {
        return post;
    }

    public Long getPostId() {
        return postId;
    }
}
