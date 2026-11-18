package jobBot.service;

import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

import jobBot.BotVk;
import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Task;
import jobBot.dto.Vacancy;
import jobBot.dto.VacancyResponse;
import jobBot.repository.task.TasksRepository;
import jobBot.repository.vacancyResponse.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ServiceDeliveryResultTest {
    @Mock
    private BotVk botVkMock;
    @Mock
    private TasksRepository tasksRepositoryMock;
    @Mock
    private TaskRepository taskRepositoryMock;
    @Spy
    private Map<Long, Task> taskMapSpy = new HashMap<>();
    private ServiceDeliveryResult serviceDeliveryResult;

    void setUpWith() {
        when(tasksRepositoryMock.getTaskRepository()).thenReturn(taskRepositoryMock);
        when(tasksRepositoryMock.getTaskMapSorted()).thenReturn(taskMapSpy);
        doAnswer(invocation -> {
            Task taskArg = invocation.getArgument(0);
            Long idToCheck = taskArg.getIdTask();
            return taskMapSpy.containsKey(idToCheck);
        }).when(taskRepositoryMock).exists(any(Task.class));

       setUp();
    }

    void setUp() {
        serviceDeliveryResult = new ServiceDeliveryResult(botVkMock, tasksRepositoryMock);
    }

    @AfterEach
    void tearDown() {
        if (serviceDeliveryResult != null) {
            serviceDeliveryResult.close();
        }
    }

    @Test
    void testCheckAndFetchWhenNoTasks() {
        setUp();
        taskMapSpy.clear();
        serviceDeliveryResult.checkAndFetch();
        verify(botVkMock, never()).writeMessages(anyLong(), any());
        verify(tasksRepositoryMock, never()).update(anyLong(), any(Task.class));
        verify(taskRepositoryMock, never()).exists(any(Task.class));
    }

    @Test
    void testProcessTaskWhenIntervalNotReached() {
        setUpWith();
        Long peerId = 123L;
        Long taskId = 1L;
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(5);
        Task task = createTask(peerId, taskId, localDateTime);
        taskMapSpy.put(taskId, task);
        serviceDeliveryResult.checkAndFetch();
        verify(botVkMock, times(1)).writeMessages(eq(peerId), any());
        verify(tasksRepositoryMock,times(1)).update(eq(taskId), any(Task.class));
    }
    @Test
    void testProcessTaskWhenIntervalReached() {
        setUpWith();
        Long peerId = 123L;
        Long taskId = 1L;
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(25);
        Task task = createTask(peerId, taskId, localDateTime);
        taskMapSpy.put(taskId, task);
        serviceDeliveryResult.checkAndFetch();
        verify(botVkMock, times(1)).writeMessages(eq(peerId), any());
        verify(tasksRepositoryMock, times(1)).update(eq(taskId), any(Task.class));
    }

    @Test
    void testProcessTaskHandlesExceptionWithoutStoppingLoop() {
        setUpWith();
        Long peerId1 = 111L;
        Long peerId2 = 222L;
        Long taskId1 = 5L;
        Long taskId2 = 6L;

        LocalDateTime localDateTime = LocalDateTime.now().minusHours(25);
        Task task1 = createTask(peerId1, taskId1, localDateTime);
        Task task2 = createTask(peerId2, taskId2, localDateTime);
        doThrow(new RuntimeException("Simulated DB Error")).when(tasksRepositoryMock)
                .update(eq(taskId1), any(Task.class));
        taskMapSpy.put(taskId1, task1);
        taskMapSpy.put(taskId2, task2);

        serviceDeliveryResult.checkAndFetch();
        verify(botVkMock, times(1)).writeMessages(eq(peerId2), any());

        Task t1 = taskMapSpy.get(taskId1);
       assertThat(t1.getLastSendingMessages()).isCloseTo(
               localDateTime, within(3, ChronoUnit.SECONDS));

        Task t2 = taskMapSpy.get(taskId2);
        assertThat(t2.getLastSendingMessages()).isCloseTo(
                        localDateTime.plusHours(25), within(3, ChronoUnit.SECONDS));
    }

    @Test
    void testLastSendingMessagesUpdatedOnlyOnSuccess() {
        setUpWith();
        Long peerId = 999L;
        Long taskId = 7L;
        List<VacancyResponse> vacancies = createVacancies(3);
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(25);
        Task task = createTask(peerId, taskId, localDateTime);
        task.setVacancyResponseList(vacancies);

        taskMapSpy.put(taskId, task);

        serviceDeliveryResult.checkAndFetch();
        Task updatedTask = taskMapSpy.get(taskId);
        assertThat(updatedTask.getLastSendingMessages()).isCloseTo(
                localDateTime.plusHours(25), within(3, ChronoUnit.SECONDS));
        serviceDeliveryResult.checkAndFetch();

        Task stillOldTask = taskMapSpy.get(taskId);
        assertThat(stillOldTask.getLastSendingMessages()).isCloseTo(
                localDateTime.plusHours(25), within(3, ChronoUnit.SECONDS));
        verify(botVkMock, times(1)).writeMessages(eq(peerId), any());
    }

    @Test
    void testSendingResultWhenNoVacancies() {
        setUpWith();
        Long peerId = 123L;
        Long taskId = 2L;
        LocalDateTime oldTime = LocalDateTime.now().minusHours(50);

        Task task = createTask(peerId, taskId, oldTime);
        task.setVacancyResponseList(new ArrayList<>());
        taskMapSpy.put(taskId, task);
        serviceDeliveryResult.checkAndFetch();

        verify(botVkMock).writeMessages(eq(peerId), any());
        ArgumentCaptor<StringBuilder> captor = ArgumentCaptor.forClass(StringBuilder.class);
        verify(botVkMock).writeMessages(eq(peerId), captor.capture());

        String message = captor.getValue().toString();
        assertThat(message).contains("Вакансий нет на сайте по данному поиску");
        Task updatedTask = taskMapSpy.get(taskId);
        LocalDateTime now = LocalDateTime.now();
        assertThat(updatedTask.getLastSendingMessages())
                .isCloseTo(now, within(5, ChronoUnit.SECONDS));
    }

    @Test
    void testSendingResultWithSmallListOfVacancies() {
        setUpWith();
        Long peerId = 456L;
        Long taskId = 3L;
        List<VacancyResponse> vacancies = createVacancies(5);
        System.out.println(vacancies.size());

        LocalDateTime localDateTime = LocalDateTime.now().minusHours(25);
        Task task = createTask(peerId, taskId, localDateTime);
        task.setVacancyResponseList(vacancies);
        taskMapSpy.put(taskId, task);
        serviceDeliveryResult.checkAndFetch();

        verify(botVkMock, times(1)).writeMessages(eq(peerId), any());
        ArgumentCaptor<StringBuilder> captor = ArgumentCaptor.forClass(StringBuilder.class);
        verify(botVkMock).writeMessages(eq(peerId), captor.capture());

        String message = captor.getValue().toString();
        assertThat(message).contains("Вакансии по поиску");
        assertThat(message).contains("- ");
        System.out.println(message);
        for (int i = 0; i < 5; i++) {
            assertThat(message).contains("- Вакансия: " + i);
        }
    }

    @Test
    void testSendingResultWithLargeListOfVacancies() {
        setUpWith();
        Long peerId = 789L;
        Long taskId = 4L;

        List<VacancyResponse> vacancies = createVacancies(25);
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(25);
        Task task = createTask(peerId, taskId, localDateTime);
        task.setVacancyResponseList(vacancies);
        taskMapSpy.put(taskId, task);

        serviceDeliveryResult.checkAndFetch();
        verify(botVkMock, times(3)).writeMessages(eq(peerId), any());

        ArgumentCaptor<StringBuilder> captor = ArgumentCaptor.forClass(StringBuilder.class);
        verify(botVkMock, times(3)).writeMessages(eq(peerId), captor.capture());

        List<String> messagesTexts = captor.getAllValues()
                .stream()
                .map(StringBuilder::toString)
                .collect(java.util.stream.Collectors.toList());

        assertThat(messagesTexts).hasSize(3);

        String firstMsg = messagesTexts.get(0);
        assertThat(firstMsg).contains("Вакансии по поиску");
        assertThat(firstMsg).doesNotContain("продолжение");
        String secondMsg = messagesTexts.get(1);
        assertThat(secondMsg).contains("Вакансии (продолжение)");
        String thirdMsg = messagesTexts.get(2);
        assertThat(thirdMsg).contains("Вакансии (продолжение)");
    }

        private Task createTask(Long peerId, Long taskId, LocalDateTime lastSendTime) {
            Vacancy vacancy = mock(Vacancy.class);
            SearchTaskVacancy searchTask = new SearchTaskVacancy(vacancy);
            Task task = new Task(peerId, searchTask);
            task.setIdTask(taskId);
            task.setLastSendingMessages(lastSendTime);
            return task;
        }


    private List<VacancyResponse> createVacancies(int count) {
        List<VacancyResponse> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            VacancyResponse vacancyResponse = mock(VacancyResponse.class);
            when(vacancyResponse.toString()).thenReturn("Вакансия: " + i);
            list.add(vacancyResponse);
        }
        return list;
    }
}