package jobBot;

import jobBot.config.Config;
import jobBot.config.ConfigReader;
import jobBot.config.ConfigReaderEnvironment;
import jobBot.dto.VacancyResponse;
import jobBot.repository.JsonUserRepository;
import jobBot.repository.UserRepository;

import jobBot.repository.vacancyResponse.JsonTaskRepository;
import jobBot.repository.vacancyResponse.TaskRepository;
import jobBot.repository.task.TasksRepository;
import jobBot.restApi.AbstractRestApiClient;
import jobBot.restApi.TrudVsemApiClient;
import jobBot.service.ServiceDeliveryResult;
import jobBot.service.VacancyFetchScheduler;

public class Main {
    static void main() throws InterruptedException {
        ConfigReader configReader = new ConfigReaderEnvironment();
        Config config = configReader.read();
        String botApiToken = config.botApiToken();
        Long groupId = config.groupID();
        UserRepository userRepository = new JsonUserRepository("src/main/resources/user");
        TaskRepository taskRepository = new JsonTaskRepository("src/main/resources/response");
        TasksRepository tasksRepository = new TasksRepository(taskRepository);
        AbstractRestApiClient<VacancyResponse> apiClient = new TrudVsemApiClient();
        BotVk botVk = new BotVk(groupId, botApiToken, tasksRepository, userRepository);

        VacancyFetchScheduler scheduler =
                new VacancyFetchScheduler(apiClient, tasksRepository);
        ServiceDeliveryResult serviceDeliveryResult =
                new ServiceDeliveryResult(botVk, tasksRepository);

        scheduler.start();
        serviceDeliveryResult.start();

        Thread botThread = Thread.startVirtualThread(botVk);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Получен сигнал завершения, останавливаем сервисы...");
            botVk.stop();
            scheduler.shutdown();
            serviceDeliveryResult.shutdown();
        }));

        botThread.join();
        scheduler.shutdown();
        serviceDeliveryResult.shutdown();
    }
}



