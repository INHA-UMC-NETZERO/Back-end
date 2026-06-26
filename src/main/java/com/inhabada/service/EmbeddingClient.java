package com.inhabada.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmbeddingClient {

    private final RestClient restClient;
    private final String url;
    private final String model;
    private final int dimension;

    @Autowired
    public EmbeddingClient(RestClient.Builder builder,
                           @Value("${app.embedding.url}") String url,
                           @Value("${app.embedding.model}") String model,
                           @Value("${app.embedding.dimension}") int dimension,
                           @Value("${app.embedding.timeout-seconds}") long timeoutSeconds) {
        this(buildRestClient(builder, timeoutSeconds), url, model, dimension);
    }

    EmbeddingClient(RestClient restClient, String url, String model, int dimension) {
        this.restClient = restClient;
        this.url = url;
        this.model = model;
        this.dimension = dimension;
    }

    public String embedPost(String title, String description) {
        return embedToVectorLiteral(title + "\n\n" + description);
    }

    public String embedQuery(String query) {
        return embedToVectorLiteral(query);
    }

    private String embedToVectorLiteral(String input) {
        if (!StringUtils.hasText(input)) {
            throw new EmbeddingException("임베딩 입력값은 비어 있을 수 없습니다");
        }

        EmbeddingResponse response;
        try {
            response = restClient.post()
                    .uri(url)
                    .body(new EmbeddingRequest(model, input))
                    .retrieve()
                    .body(EmbeddingResponse.class);
        } catch (RestClientException ex) {
            throw new EmbeddingException("임베딩 서버 호출에 실패했습니다", ex);
        }

        List<Double> embedding = extractEmbedding(response);
        if (embedding.size() != dimension) {
            throw new EmbeddingException("임베딩 차원이 설정값과 다릅니다");
        }

        return toVectorLiteral(embedding);
    }

    private List<Double> extractEmbedding(EmbeddingResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new EmbeddingException("임베딩 응답에 data가 없습니다");
        }

        List<Double> embedding = response.data().get(0).embedding();
        if (embedding == null || embedding.isEmpty()) {
            throw new EmbeddingException("임베딩 응답에 embedding이 없습니다");
        }
        return embedding;
    }

    private String toVectorLiteral(List<Double> embedding) {
        return embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static RestClient buildRestClient(RestClient.Builder builder, long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = Math.toIntExact(Duration.ofSeconds(timeoutSeconds).toMillis());
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);
        return builder.requestFactory(requestFactory).build();
    }

    private record EmbeddingRequest(String model, String input) {
    }

    private record EmbeddingResponse(List<EmbeddingData> data) {
    }

    private record EmbeddingData(List<Double> embedding) {
    }
}
