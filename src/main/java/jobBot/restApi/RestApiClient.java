package jobBot.restApi;

import java.util.List;

public interface RestApiClient<T> {
    List<T> get(String url);

    String getBaseUrl();

    List<T> parseResponse(String rawJson);


}
