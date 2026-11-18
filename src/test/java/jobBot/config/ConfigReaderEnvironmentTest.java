package jobBot.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ConfigReaderEnvironmentTest {
    private ConfigReaderEnvironment reader;
    @BeforeEach
    void setUp() {
        reader = new ConfigReaderEnvironment();
    }

    @Test
    void testReadWhenTokenAndGroupIdAreValid() {
        String expectedToken = "my_secret_token_123";
        String expectedGroupId = "123456789";
        reader = new TestConfigReader(expectedToken, expectedGroupId);
        Config config = reader.read();
        assertThat(config.groupID()).isEqualTo(123456789L);
        assertThat(config.botApiToken()).isEqualTo(expectedToken);
    }

    @Test
    void testReadWhenGroupIdIsNotANumber() {
        String token = "valid_token";
        String invalidGroupId = "not_a_number";
        reader = new TestConfigReader(token, invalidGroupId);
        assertThatThrownBy(reader::read)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не верно указан Id группы");
    }

    @Test
    void testReadWhenGroupIdIsNull() {
        String token = "valid_token";
        String nullGroupId = null;
        reader = new TestConfigReader(token, nullGroupId);
        assertThatThrownBy(reader::read)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не верно указан Id группы");
    }

    @Test
    void testReadWhenTokenIsNull() {
        String nullToken = null;
        String groupId = "987654321";
        reader = new TestConfigReader(nullToken, groupId);
        assertThatThrownBy(reader::read)
                .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Токен не задан или пуст");

    }

    private static class TestConfigReader extends ConfigReaderEnvironment {
            private final String mockToken;
            private final String mockGroupId;

            public TestConfigReader(String mockToken, String mockGroupId) {
                this.mockToken = mockToken;
                this.mockGroupId = mockGroupId;
            }


            public String getEnv(String key) {
                if ("BOT_TOKEN".equals(key)) return mockToken;
                if ("GROUP_ID".equals(key)) return mockGroupId;
                return null;
            }

            @Override
            public Config read() {
                var token = getEnv("BOT_TOKEN");
                String id = getEnv("GROUP_ID");

                if (id == null) {
                    throw new RuntimeException("Не верно указан Id группы");
                }

                long groupId;
                try {
                    groupId = Long.parseLong(id);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Не верно указан Id группы", e);
                }
                return new Config(groupId, token);
            }
        }
}