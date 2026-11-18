package jobBot;

import jobBot.config.Config;
import jobBot.dto.VacancyResponse;
import jobBot.repository.UserRepository;
import jobBot.repository.task.TasksRepository;
import jobBot.repository.vacancyResponse.TaskRepository;
import jobBot.restApi.AbstractRestApiClient;
import jobBot.service.ServiceDeliveryResult;
import jobBot.service.VacancyFetchScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MainTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private AbstractRestApiClient<VacancyResponse> apiClient;

    @Test
    @DisplayName("Сборка всех компонентов приложения без запуска потоков")
    void testComponentsInstantiation() {
        Config config = new Config(12345L, "dummy_token");
        TasksRepository tasksRepositoryWrapper = new TasksRepository(taskRepository);
        BotVk botVk = new BotVk(config.groupID(), config.botApiToken(), tasksRepositoryWrapper, userRepository);
        VacancyFetchScheduler scheduler = new VacancyFetchScheduler(apiClient, tasksRepositoryWrapper);
        ServiceDeliveryResult deliveryResult = new ServiceDeliveryResult(botVk, tasksRepositoryWrapper);
        assertNotNull(botVk, "BotVk должен быть создан");
        assertNotNull(scheduler, "Scheduler должен быть создан");
        assertNotNull(deliveryResult, "DeliveryResult должен быть создан");
    }
}

