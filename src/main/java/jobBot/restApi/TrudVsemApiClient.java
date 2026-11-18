package jobBot.restApi;

import jobBot.dto.VacancyResponse;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TrudVsemApiClient extends AbstractRestApiClient<VacancyResponse> {
    private static final String BASE_URL = "http://opendata.trudvsem.ru/api/v1";
    private static final String[] JSON_PATH = {"results", "vacancies"};
    private static final String WRAPPER_KEY = "vacancy";
    protected static final Integer MAX_NUMBER_OF_PAGE = 100;
    protected static final Integer RESPONSE_LIMIT_PER_PAGE = 100;

    public TrudVsemApiClient() {
    }

    public TrudVsemApiClient(HttpClient httpClient) {
        super(httpClient);
    }



    @Override
    protected void validateResponse(int statusCode, String body) {
        if (statusCode < 200 || statusCode >= 300) {
            try {
                throw new IOException("TrudVsem Api error: HTTP " + statusCode);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public List<VacancyResponse> parseResponse(String rawJson) {
        return extractArray(rawJson, JSON_PATH, WRAPPER_KEY, VacancyResponse.class);
    }

    @Override
    protected String buildUrlWithParams(String regionCode, Map<String, String> param,
                                        int offset, int limit) {
        StringBuilder urlBuilder = new StringBuilder(getBaseUrl());
        urlBuilder.append((regionCode != null && !regionCode.isEmpty())
                ? "/vacancies/region/" + regionCode : "/vacancies");
        urlBuilder.append("?offset=").append(offset);
        //  urlBuilder.append("&limit=").append(limit);
        if (param != null && !param.isEmpty()) {
            urlBuilder.append("&");
            List<Map.Entry<String, String>> entries = new ArrayList<>(param.entrySet());
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<String, String> map = entries.get(i);
                urlBuilder.append(map.getKey());
                urlBuilder.append("=");
                urlBuilder.append(encode(map.getValue()));
                if (i < entries.size() - 1) {
                    urlBuilder.append("&");
                }
            }
        }
        return urlBuilder.toString();
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public List<VacancyResponse> fetchAll(Map<String, String> param, String regionCode) {
        List<VacancyResponse> allVacancies = new ArrayList<>();
        int offset = 0;
        for (int page = 0; page < MAX_NUMBER_OF_PAGE; page++) {
            String pathUrl = buildUrlWithParams(regionCode, param, offset, RESPONSE_LIMIT_PER_PAGE);
            List<VacancyResponse> pageVacancies = get(pathUrl);
            if (pageVacancies.isEmpty()) {
                break;
            }
            allVacancies.addAll(pageVacancies);

            if (pageVacancies.size() < RESPONSE_LIMIT_PER_PAGE) {
                break;
            }
            offset += 1;
        waitBetweenPages();}
        return allVacancies;
    }

    protected void waitBetweenPages() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Прервано во время пагинации", e);
        }
    }
}
