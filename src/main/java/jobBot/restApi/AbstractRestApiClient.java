package jobBot.restApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractRestApiClient<T> implements RestApiClient<T> {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    public AbstractRestApiClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        objectMapper = new ObjectMapper();
    }
    public AbstractRestApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<T> get(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(getTimeout())
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        validateResponse(response.statusCode(), response.body());
        return parseResponse(response.body());
    }

    protected Duration getTimeout() {
        return DEFAULT_TIMEOUT;
    }

    protected abstract String buildUrlWithParams(String regionCode, Map<String, String> param, int offset, int limit);

    protected abstract void validateResponse(int statusCode, String body);

    protected List<T> extractArray(String rawJson, String[] pathToJsonArray, String wrapperKey,
                                   Class<T> clazz) {
        JsonNode root;
        try {
            root = objectMapper.readTree(rawJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode current = root;
        for (String key : pathToJsonArray) {
            if (current == null || !current.has(key)) return new ArrayList<>();
            current = current.get(key);
        }
        List<T> result = new ArrayList<>();
        if (current.isArray()) {
            for (JsonNode element : current) {
                JsonNode target = (wrapperKey != null && element.has(wrapperKey))
                        ? element.get(wrapperKey) : element;
                if (target != null && !target.isNull()) {
                    try {
                        result.add(objectMapper.treeToValue(target, clazz));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return result;
    }

}
