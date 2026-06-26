package com.inhabada.repository;

public interface PostSimilarityProjection {

    Long getId();

    String getTitle();

    String getThumbnailKey();

    Integer getRemainingQuantity();

    Double getSimilarity();
}
