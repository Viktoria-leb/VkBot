package jobBot.restApi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractRestApiClientTest {
    @Mock
    private HttpClient mockHttpClient;
    private AbstractRestApiClient<TestDto> apiClient;

    @BeforeEach
    void setUp() {
        AbstractRestApiClient<TestDto> realClient= new AbstractRestApiClient<>(mockHttpClient) {
            @Override
            protected String buildUrlWithParams(String regionCode, Map<String, String> param, int offset, int limit) {
                return "https://api.test.com/data";
            }
            @Override
            protected void validateResponse(int statusCode, String body) {
                if (statusCode < 200 || statusCode >= 300) {
                    throw new RuntimeException("Invalid status: " + statusCode);
                }
            }
            @Override
            public String getBaseUrl() {
                return "https://api.test/";
            }
            @Override
            public List<TestDto> parseResponse(String rawJson) {
                return new ArrayList<>();
            }
        };
        apiClient = spy(realClient);
    }

    @AfterEach
    void tearDown() {
       apiClient = null;
    }

    @Test
    void testGetWhenResponseIsOkThenValidateAndParseAreCalled() throws IOException, InterruptedException {
        var mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        String mockBody = "{\"data\": []}";
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(mockResponse);
        apiClient.get("https://api.test.com/v1/items");
        verify(apiClient).validateResponse(eq(200), eq(mockBody));
        verify(apiClient).parseResponse(eq(mockBody));
    }
    @Test
    void testGetWhenResponseStatusCodeIsNotOkThenRuntimeExceptionIsThrown() throws IOException, InterruptedException {
        var mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(404);
        String mockBody = "{\"error\": \"Not Found\"}";
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(mockResponse);

        assertThatThrownBy(() -> apiClient.get("https://api.test.com/v1/items"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid status: 404");

        verify(mockHttpClient).send(any(), any());
        verify(apiClient).validateResponse(eq(404), eq(mockBody));
    }

    @Test
    void testGetWhenHttpClientThrowsExceptionThenRuntimeExceptionIsWrapped() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenThrow(new java.io.IOException("Network error"));

        assertThatThrownBy(() -> apiClient.get("https://api.test.com/v1/items"))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(java.io.IOException.class);
    }
    @Test
    void testExtractArrayWhenJsonHasNestedPathThenListIsExtracted(){
        String rawJson = """
            {
                "response": {
                    "data": [
                        {"id": 101, "name": "ItemA"},
                        {"id": 102, "name": "ItemB"}
                    ]
                }
            }
            """;
        String[] path = {"response", "data"};
        List<TestDto> result = apiClient.extractArray(rawJson, path, null, TestDto.class);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(101);
        assertThat(result.get(1).getName()).isEqualTo("ItemB");
    }

    @Test
    void testExtractArrayWhenPathDoesNotExistThenEmptyListIsReturned(){
        String rawJson = "{\"data\": []}";
        String[] path = {"wrong", "path", "to", "array"};
        List<TestDto> result = apiClient.extractArray(rawJson, path, null, TestDto.class);

        assertThat(result).isEmpty();
    }

    @Test
    void testExtractArrayWhenElementIsNullThenItIsSkipped(){
        String rawJson = """
            {
                "items": [
                    {"id": 201, "name": "Valid"},
                    {"wrapper": null},
                    null
                ]
            }
            """;
        String[] path = {"items"};
        List<TestDto> result = apiClient.extractArray(rawJson, path, "wrapper", TestDto.class);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Valid");
    }

    @Test
    void testExtractArrayWhenRawJsonIsInvalidThenRuntimeExceptionIsThrown() {
        String rawJson = "{ invalid json structure }";
        String[] path = {"data"};

        assertThrows(RuntimeException.class, () ->
            apiClient.extractArray(rawJson, path, null, TestDto.class)
        );
    }

    @Test
    void testExtractArrayWhenWrapperKeyIsProvidedThenInnerObjectIsUsed(){
        String rawJson = """
            {
                "list": [
                    {"meta": {"id": 301, "name": "WrappedItem"}},
                    {"meta": {"id": 302, "name": "Another"}}
                ]
            }
            """;
        String[] path = {"list"};
        List<TestDto> result = apiClient.extractArray(rawJson, path, "meta", TestDto.class);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(301);
    }
    @Test
    void testGetTimeoutWhenCalledThenReturnsDefaultDuration() {
        Duration result = apiClient.getTimeout();
        assertThat(result).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    void testGetWhenCalledThenHttpRequestContainsCorrectHeadersAndTimeout() throws IOException, InterruptedException {
        var mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(mockResponse);


        apiClient.get("https://api.test.com/v1/items");
        verify(mockHttpClient).send(checkHttpRequest("https://api.test.com/v1/items",
                60), any());
    }
    private HttpRequest checkHttpRequest(String expectedUrl, int expectedTimeoutSeconds) {
        return ArgumentMatchers.argThat(req -> {
            assertThat(req.uri().toString()).isEqualTo(expectedUrl);
            assertThat(req.headers().map().getOrDefault("Accept", List.of()))
                    .containsExactly("application/json");

            Duration timeout = req.timeout().orElseThrow();
            return timeout.getSeconds() == expectedTimeoutSeconds;
        });
    }

}