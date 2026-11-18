package jobBot;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.LongPollServer;
import com.vk.api.sdk.queries.groups.GroupsGetLongPollServerQuery;
import jobBot.command.commands.RegionCommandType;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.repository.UserRepository;
import jobBot.repository.task.TasksRepository;
import jobBot.service.VkMainProcessor;
import lombok.Getter;

@Getter
public final class BotVk implements Runnable {

    private final Long groupId;
    private final String botApiToken;
    private final UserRepository userRepository;
    private final TasksRepository tasksRepository;
    private TransportClient transportClient;
    private VkApiClient vk;
    private GroupActor actor;
    private RegionCommandType regionCommandType;
    private ListVacancyType listVacancyType;
    private volatile boolean running = true;
    private VkMainProcessor vkMainProcessor;


    public BotVk(Long groupId, String botApiToken,
                 TasksRepository tasksRepository,
                 UserRepository userRepository) {
        this.groupId = groupId;
        this.botApiToken = botApiToken;
        this.tasksRepository = tasksRepository;
        this.userRepository = userRepository;
        transportClient = new HttpTransportClient();
        vk = new VkApiClient(transportClient);
        actor = new GroupActor(groupId, botApiToken);
        regionCommandType = new RegionCommandType();
        listVacancyType = new ListVacancyType();
    }


    private void startBot(VkApiClient vk, GroupActor actor, Long groupId,
                          RegionCommandType regionCommandType, UserRepository userRepository,
                          ListVacancyType listVacancyType, TasksRepository tasksRepository) {
        try {
            vk.groups().setLongPollSettings(actor, groupId)
                    .enabled(true)
                    .messageNew(true)
                    .execute();

            GroupsGetLongPollServerQuery serverQuery =
                    vk.groups().getLongPollServer(actor, groupId);
            LongPollServer longPollServer = serverQuery.execute();

            String server = String.valueOf(longPollServer.getServer());
            String key = longPollServer.getKey();
            int ts = Integer.parseInt(longPollServer.getTs());

            System.out.println("Бот запущен. Ожидание сообщений...");

            vkMainProcessor = new VkMainProcessor(
                    server, key, ts, vk, actor, groupId,
                    regionCommandType,
                    userRepository,
                    listVacancyType,
                    tasksRepository);

//            vkMainProcessor.mainCycle(server, key, ts, vk, actor,
//                    groupId, regionCommandType, userRepository, listVacancyType, tasksRepository);
            Thread processorThread = new Thread(() -> {
                vkMainProcessor.mainCycle(server, key, ts, vk, actor,
                        groupId, regionCommandType, userRepository, listVacancyType, tasksRepository);
            }, "VkMainProcessor-Thread");
            processorThread.start();

        } catch (Exception e) {
            System.err.println("Ошибка при запуске Long Poll: " + e.getMessage());

        }
    }

    public void stop() {
        getVkMainProcessor().stop();
        running = false;
    }

    public void writeMessages(Long peerId, StringBuilder builder) {
        String message = builder.toString();
        if (message.isBlank()) {
            return;
        }
        try {
            getVk().messages().sendUserIds(getActor())
                    .peerId(peerId)
                    .message(message)
                    .dontParseLinks(true)
                    .randomId((int) Math.random())
                    .executeAsString();
        } catch (ClientException e) {
            System.err.println("Ошибка отправки сообщения в VK: " + e.getMessage());
        }
    }
    @Override
    public void run() {
        startBot(getVk(), getActor(), getGroupId(), getRegionCommandType(),
                getUserRepository(), getListVacancyType(), getTasksRepository());
        while (running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}