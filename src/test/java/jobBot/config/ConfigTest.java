package jobBot.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;


class ConfigTest {
    @Test
    void testCreateConfigWhenValidGroupIdAndTokenProvided() {
        Long validGroupId = 123456789L;
        String validToken = "my_secret_token_123";
        Config config = new Config(validGroupId, validToken);
        assertThat(config.groupID()).isEqualTo(validGroupId);
        assertThat(config.botApiToken()).isEqualTo(validToken);
    }

    @Test
    void testCreateConfigWhenGroupIdIsNull() {
        Long nullGroupId = null;
        String validToken = "my_secret_token_123";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Config(nullGroupId, validToken))
                .withMessageContaining("ID группы не задан: groupID");
    }

    @Test
    void testCreateConfigWhenTokenIsNull() {
        Long validGroupId = 123456789L;
        String nullToken = null;
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Config(validGroupId, nullToken))
                .withMessageContaining("Токен не задан или пуст: botApiToken");
    }

    @Test
    void testCreateConfigWhenTokenIsBlank() {

        Long validGroupId = 123456789L;
        String blankToken = "   ";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Config(validGroupId, blankToken))
                .withMessageContaining("Токен не задан или пуст: botApiToken");
    }

    @Test
    void testCreateConfigWhenTokenIsEmpty() {
        Long validGroupId = 123456789L;
        String emptyToken = "";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Config(validGroupId, emptyToken))
                .withMessageContaining("Токен не задан или пуст: botApiToken");
    }

    @Test
    void testConfigRecordFieldsAreImmutableAndAccessible() {
        Config config = new Config(987654321L, "another_token");
        Long retrievedId = config.groupID();
        String retrievedToken = config.botApiToken();
        assertThat(retrievedId).isEqualTo(987654321L);
        assertThat(retrievedToken).isEqualTo("another_token");
    }

}