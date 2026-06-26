package com.inhabada.event;

import com.inhabada.entity.Post;

public class PostCreatedEvent {

    private final Post post;

    public PostCreatedEvent(Post post) {
        this.post = post;
    }

    public Post getPost() {
        return post;
    }
}
