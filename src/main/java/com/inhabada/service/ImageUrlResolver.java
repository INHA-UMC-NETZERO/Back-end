package com.inhabada.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ImageUrlResolver {

    private final String baseUrl;

    public ImageUrlResolver(@Value("${aws.s3.public-base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String toUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        return baseUrl + "/" + key;
    }

    public List<String> toUrls(String[] keys) {
        if (keys == null) {
            return List.of();
        }
        return Arrays.stream(keys).map(this::toUrl).toList();
    }

    public String firstUrl(String[] keys) {
        if (keys == null || keys.length == 0) {
            return null;
        }
        return toUrl(keys[0]);
    }
}
