package com.inhabada.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.ThrottlingException;

/**
 * Bedrock Converse API 호출 래퍼.
 * ThrottlingException 발생 시 지수 백오프로 최대 3회 재시도하며,
 * 재시도가 모두 실패하면 빈 JSON 배열을 반환한다(@Recover).
 */
@Component
public class BedrockClient {

    private static final Logger log = LoggerFactory.getLogger(BedrockClient.class);

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final String modelId;
    private final int maxTokens;

    public BedrockClient(BedrockRuntimeClient bedrockRuntimeClient,
                         @Value("${aws.bedrock.model-id}") String modelId,
                         @Value("${aws.bedrock.max-tokens:1024}") int maxTokens) {
        this.bedrockRuntimeClient = bedrockRuntimeClient;
        this.modelId = modelId;
        this.maxTokens = maxTokens;
    }

    @Retryable(retryFor = ThrottlingException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0))
    public String converse(String prompt) {
        ConverseRequest request = ConverseRequest.builder()
                .modelId(modelId)
                .messages(Message.builder()
                        .role(ConversationRole.USER)
                        .content(ContentBlock.fromText(prompt))
                        .build())
                .inferenceConfig(InferenceConfiguration.builder()
                        .maxTokens(maxTokens)
                        .temperature(0.0f)
                        .build())
                .build();

        ConverseResponse response = bedrockRuntimeClient.converse(request);
        return response.output().message().content().get(0).text();
    }

    @Recover
    public String recover(ThrottlingException ex, String prompt) {
        log.warn("Bedrock throttling persisted after retries, returning empty result", ex);
        return "[]";
    }
}
