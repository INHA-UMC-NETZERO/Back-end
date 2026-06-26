package com.inhabada.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EmbeddingClientTest {

    @Test
    void embedQuery_returnsPgvectorLiteralFromEmbeddingResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        EmbeddingClient client = new EmbeddingClient(
                builder.build(),
                "https://seoki.cloud/v1/embeddings",
                "bge-m3",
                3
        );

        server.expect(requestTo("https://seoki.cloud/v1/embeddings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "model": "bge-m3",
                          "input": "cup ramen"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "data": [
                            {
                              "embedding": [0.1, 0.2, 0.3]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.embedQuery("cup ramen")).isEqualTo("[0.1,0.2,0.3]");
        server.verify();
    }

    @Test
    void embedQuery_rejectsEmbeddingWithUnexpectedDimension() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        EmbeddingClient client = new EmbeddingClient(
                builder.build(),
                "https://seoki.cloud/v1/embeddings",
                "bge-m3",
                3
        );

        server.expect(requestTo("https://seoki.cloud/v1/embeddings"))
                .andRespond(withSuccess("""
                        {
                          "data": [
                            {
                              "embedding": [0.1, 0.2]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.embedQuery("cup ramen"))
                .isInstanceOf(EmbeddingException.class);
        server.verify();
    }
}
