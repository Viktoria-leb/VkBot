package jobBot.restApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jobBot.dto.VacancyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrudVsemApiClientTest {
    @Mock
    private HttpClient mockHttpClient;
    @Spy
    private TrudVsemApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        apiClient = Mockito.spy(new TrudVsemApiClient(mockHttpClient));
    }

    @Test
    void testGetWhenStatusCodeIsErrorThenRuntimeExceptionIsThrown() throws Exception {
        var mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(404);
        String mockBody = "{\"error\": \"Not Found\"}";
        when(mockResponse.body()).thenReturn(mockBody);

        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(mockResponse);

        assertThatThrownBy(() -> apiClient.get("http://opendata.trudvsem.ru/api/v1/vacancies"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TrudVsem Api error: HTTP 404");

        verify(mockHttpClient).send(any(), any());
        verify(apiClient).validateResponse(eq(404), eq(mockBody));
    }

    @Test
    void testParseResponseExtractsArrayByPathAndWrapper() {
        String rawJson = """
                {
                    "results": {
                        "vacancies": [
                            {"vacancy": {"id": 101, "job-name": "Java Dev"}},
                            {"vacancy": {"id": 102, "job-name": "QA Engineer"}},
                            {"vacancy": null},
                            null
                        ]
                    }
                }
                """;

        List<VacancyResponse> result = apiClient.parseResponse(rawJson);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("101");
        assertThat(result.get(1).getJsonName()).isEqualTo("QA Engineer");
    }

    @Test
    void testParseResponseReturnsEmptyListWhenPathNotFound() {
        String rawJson = "{\"data\": []}";
        List<VacancyResponse> result = apiClient.parseResponse(rawJson);
        assertThat(result).isEmpty();
    }

    @Test
    void testParseResponseThrowsRuntimeExceptionOnInvalidJson() {
        String invalidJson = "{ invalid json }";
        assertThatThrownBy(() -> apiClient.parseResponse(invalidJson))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(JsonProcessingException.class);
    }

    @Test
    void testBuildUrlWithRegionCode() {
        Map<String, String> param = new HashMap<>();
        String url = apiClient.buildUrlWithParams("77", param, 0, 100);
        assertThat(url).isEqualTo("http://opendata.trudvsem.ru/api/v1/vacancies/region/77?offset=0");
    }

    @Test
    void testBuildUrlWithoutRegionCode() {
        Map<String, String> param = new HashMap<>();
        String url = apiClient.buildUrlWithParams(null, param, 50, 100);
        assertThat(url).isEqualTo("http://opendata.trudvsem.ru/api/v1/vacancies?offset=50");
    }

    @Test
    void testBuildUrlWithParamsAndEncoding() {
        Map<String, String> param = new HashMap<>();
        param.put("text", "java разработчик");
        param.put("city", "Москва");
        String url = apiClient.buildUrlWithParams(null, param, 0, 100);

        assertThat(url).contains("text=java+%D1%80%D0%B0%D0%B7%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%87%D0%B8%D0%BA");
        assertThat(url).contains("city=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0");
        assertThat(url).startsWith("http://opendata.trudvsem.ru/api/v1/vacancies?offset=0&");
    }

    @Test
    void testBuildUrlWithEmptyParams() {
        Map<String, String> param = new HashMap<>();
        String url = apiClient.buildUrlWithParams("77", param, 10, 100);
        assertThat(url).isEqualTo("http://opendata.trudvsem.ru/api/v1/vacancies/region/77?offset=10");
    }

    @Test
    void testFetchAllStopsWhenNextPageIsEmpty() throws Exception {
        int limit = TrudVsemApiClient.RESPONSE_LIMIT_PER_PAGE;
        var response1 = mock(HttpResponse.class);
        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn(pageJson(limit));
        var response2 = mock(HttpResponse.class);
        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn(pageJson(0));
        when(mockHttpClient.send(any(), any())).thenReturn(response1, response2);
        List<VacancyResponse> result = apiClient.fetchAll(new HashMap<>(), null);
        assertThat(result).hasSize(limit);
        verify(mockHttpClient, times(2)).send(any(), any());
    }

    @Test
    void testFetchAllStopsWhenLastPageSizeLessThanLimit() throws Exception {
        int limit = TrudVsemApiClient.RESPONSE_LIMIT_PER_PAGE;
        int smallCount = 50;

        var response1 = mock(HttpResponse.class);
        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn(pageJson(limit));

        var response2 = mock(HttpResponse.class);
        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn(pageJson(smallCount));
        when(mockHttpClient.send(any(), any())).thenReturn(response1, response2);
        List<VacancyResponse> result = apiClient.fetchAll(new HashMap<>(), null);

        assertThat(result).hasSize(limit + smallCount);
        verify(mockHttpClient, times(2)).send(any(), any());
    }

    @Test
    void testFetchAllRespectsMaxNumberOfPage() throws Exception {
        final int MAX_PAGES = TrudVsemApiClient.MAX_NUMBER_OF_PAGE;
        final int ITEMS_PER_PAGE = 100;
        List<HttpResponse<String>> responses = new ArrayList<>();
        for (int i = 0; i < MAX_PAGES; i++) {
            var r = mock(HttpResponse.class);
            when(r.statusCode()).thenReturn(200);
            when(r.body()).thenReturn(pageJson(ITEMS_PER_PAGE));
            responses.add(r);
        }

        AtomicInteger callIndex = new AtomicInteger(0);

        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenAnswer(invocation -> {
                    int index = callIndex.getAndIncrement();
                    return responses.get(index);
                });

        Map<String, String> param = new HashMap<>();
        List<VacancyResponse> all = apiClient.fetchAll(param, null);

        assertThat(all).hasSize(MAX_PAGES * ITEMS_PER_PAGE);
        verify(mockHttpClient, times(MAX_PAGES)).send(any(), any());
    }

    @Test
    void testFetchAllCallsWaitBetweenVacancyResponse() throws Exception {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(pageJson(100));
        when(mockHttpClient.send(any(), any()))
                .thenReturn(response, response);
        doNothing().when(apiClient).waitBetweenPages();
        apiClient.fetchAll(new HashMap<>(), null);
        verify(apiClient, times(100)).waitBetweenPages();
    }

    private String pageJson(int count) throws Exception {
        List<Map<String, Object>> vacancies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> vacancy = new HashMap<>();
            vacancy.put("vacancy", Map.of("id", 1000 + i, "job-name", "Vacancy " + i));
            vacancies.add(vacancy);
        }
        Map<String, Object> results = Map.of("vacancies", vacancies);
        Map<String, Object> root = Map.of("results", results);
        return objectMapper.writeValueAsString(root);
    }
}
