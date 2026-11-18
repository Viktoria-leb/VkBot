package jobBot.config;

public class ConfigReaderEnvironment implements ConfigReader {

    @Override
    public Config read() {
        var token = System.getenv("BOT_TOKEN");
        String id = System.getenv("GROUP_ID");
        long groupId;
        try {
            groupId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Не верно указан Id группы");
        }
        return new Config(groupId, token);
    }
}
