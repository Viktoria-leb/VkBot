package jobBot.service;

import com.google.gson.*;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.groups.LongPollServer;
import jobBot.command.commands.RegionCommandType;

import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.dto.vk.VkFullMessage;
import jobBot.repository.UserRepository;
import jobBot.repository.task.TasksRepository;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class VkMainProcessor {
    private static final String FAILED = "failed";
    public static final String TS = "ts";
    private static final String UPDATES = "updates";
    private static final Gson gson = new Gson();

    private final String server;
    private final String key;
    private final int ts;
    @Getter
    private final VkApiClient vk;
    @Getter
    private final GroupActor actor;
    private final Long groupID;
    private final UserRepository userRepository;
    private final RegionCommandType regionCommandType;
    private final ListVacancyType listVacancyType;
    private TasksRepository taskRepository;
    private boolean running = true;


    public VkMainProcessor(String server, String key, int ts,
                           VkApiClient vk, GroupActor actor,
                           Long groupID,  RegionCommandType regionCommandType,
                           UserRepository userRepository,
                           ListVacancyType listVacancyType,
                           TasksRepository taskRepository) {
        this.server = server;
        this.key = key;
        this.ts = ts;
        this.vk = vk;
        this.actor = actor;
        this.groupID = groupID;
        this.regionCommandType = regionCommandType;
        this.userRepository = userRepository;
        this.listVacancyType = listVacancyType;
        this.taskRepository = taskRepository;

    //    mainCycle(server, key, ts, vk, actor, groupID, regionCommandType, userRepository, listVacancyType, taskRepository);
    }

    public void mainCycle(String server, String key, int ts, VkApiClient vk, GroupActor actor,
                           Long groupID, RegionCommandType regionCommandType , UserRepository userRepository,
                           ListVacancyType listVacancyType, TasksRepository taskRepository) {
        LongPollServer longPollServer;
        while (running) {
            try {
                String url = generatingUrl(server, key, ts);
                JsonObject root = fetchAndParseUpdates(url);

                if (root.has(FAILED)) {
                    int failedCode = root.get(FAILED).getAsInt();
                    switch (failedCode) {
                        case 1:
                            ts = root.get(TS).getAsInt();
                            continue;
                        case 2:
                        case 3:
                            System.out.println("Переподключение к Long Poll...");
                            longPollServer = vk.groups().getLongPollServer(actor, groupID).execute();
                            server = String.valueOf(longPollServer.getServer());
                            key = longPollServer.getKey();
                            ts = Integer.parseInt(longPollServer.getTs());
                            continue;
                        default:
                            System.err.println("Неизвестная ошибка Long Poll: " + failedCode);
                            Thread.sleep(5000);
                            continue;
                    }
                }
                ts = vkUpdateInfo(vk, actor, root,
                        regionCommandType, listVacancyType,
                        userRepository, taskRepository);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Пeреподключeние....");
            }
        }
    }


    static String generatingUrl(String server, String key, int ts) {
        return server + "?act=a_check&key=" + key + "&ts=" + ts + "&wait=60";
    }


    static String loadJson(String urlString) throws Exception {
     //   URL url = new URL(urlString);
        URI uri = URI.create(urlString);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);


        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }

    static int vkUpdateInfo(VkApiClient vk, GroupActor actor, JsonObject root,
                            RegionCommandType regionCommandType,
                            ListVacancyType listVacancyType,
                            UserRepository userRepository,
                            TasksRepository taskRepository) throws InterruptedException{
        int ts;
        ts = root.get(TS).getAsInt();
        JsonArray updates = root.getAsJsonArray(UPDATES);
        for (JsonElement updateElement : updates) {
            VkFullMessage message = gson.fromJson(updateElement, VkFullMessage.class);

            long peerId = message.getVkMessageInfo().getMessage().getPeerId();
            String text = message.getVkMessageInfo().getMessage().getText();
         //   String safeText = (text == null) ? "" : text;
            System.out.println("Входящее сообщение от peer_id=" + peerId + ": " + text);

           if (text==null) {
                continue;
            }

            ServerUserTasks serviceUserVk = new ServerUserTasks(vk, actor, text, peerId,
                    userRepository, regionCommandType, listVacancyType, taskRepository);
            serviceUserVk.process();
        }
        Thread.sleep(100);
        return ts;
    }
    protected JsonObject fetchAndParseUpdates(String url) throws Exception {
        String jsonResponse = loadJson(url);
        return JsonParser.parseString(jsonResponse).getAsJsonObject();
    }
    public void stop() {
        running = false;
    }
}

