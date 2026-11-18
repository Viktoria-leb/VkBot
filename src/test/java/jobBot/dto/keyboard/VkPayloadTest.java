package jobBot.dto.keyboard;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class VkPayloadTest {
    @Test
    void testToStringWhenButtonIsSetViaBuilder() {
        String expectedButtonValue = "123";
        VkPayload payload = VkPayload.builder()
                .button(expectedButtonValue)
                .build();
        String result = payload.toString();
        assertThat(result).isEqualTo("{\"button\":\"" + expectedButtonValue + "\"}");
    }

    @Test
    void testToStringWhenButtonIsNull() {
        VkPayload payload = VkPayload.builder()
                .button(null)
                .build();
        String result = payload.toString();

        assertThat(result).isEqualTo("{}");
    }

    @Test
    void testToStringWhenButtonContainsSpecialCharacters() {
        String specialValue = "button\"with\"quotes & ampersand";
        VkPayload payload = VkPayload.builder()
                .button(specialValue)
                .build();
        String result = payload.toString();
        assertThat(result).startsWith("{\"button\":");
        assertThat(result).contains("\\\"");
    }

    @Test
    void testBuilderWhenAllFieldsAreDefault() {
        VkPayload payload = VkPayload.builder().build();
        assertThat(payload.getButton()).isNull();
    }

    @Test
    void testDataAnnotationWhenAccessingFieldDirectly() {
        String value = "direct_access_test";
        VkPayload payload = new VkPayload(value);
        assertThat(payload.getButton()).isEqualTo("direct_access_test");
        assertThat(payload.toString()).isEqualTo("{\"button\":\"" + "direct_access_test" + "\"}");
    }

    @Test
    void testJsonStructureWhenUsingGsonExplicitly() {
        String buttonVal = "test_json_structure";
        VkPayload payload = VkPayload.builder().button(buttonVal).build();
        Gson gson = new Gson();
        String manualJson = gson.toJson(payload);
        String overriddenToString = payload.toString();
        assertThat(manualJson).isEqualTo(overriddenToString);
    }

}