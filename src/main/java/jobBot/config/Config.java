package jobBot.config;

public record Config(
        Long groupID,
        String botApiToken
) {
    public Config {
        if (botApiToken == null || botApiToken.isBlank()) {
            throw new IllegalArgumentException("Токен не задан или пуст: botApiToken");
        }
        if (groupID == null) {
            throw new IllegalArgumentException("ID группы не задан: groupID");
        }
    }
}
