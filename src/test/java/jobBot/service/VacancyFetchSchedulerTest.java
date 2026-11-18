package jobBot.service;

import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Task;
import jobBot.dto.Vacancy;
import jobBot.dto.VacancyResponse;
import jobBot.repository.task.TasksRepository;
import jobBot.repository.vacancyResponse.TaskRepository;
import jobBot.restApi.AbstractRestApiClient;
import jobBot.restApi.TrudVsemApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@ExtendWith(MockitoExtension.class)
class VacancyFetchSchedulerTest {
    private VacancyFetchScheduler scheduler;
    @Mock
    private AbstractRestApiClient<VacancyResponse> apiClient;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private TaskRepository taskRepositoryMock;
    @Spy
    private Map<Long, Task> taskMapSpy  = new ConcurrentHashMap<>();
   private TrudVsemApiClient client;

    @BeforeEach
    void setUp() {
       client = spy(new TrudVsemApiClient());
       scheduler = new VacancyFetchScheduler(client, tasksRepository);
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.close();
        }
    }

    @Test
    void testStartSchedulesTaskWhenSchedulerInitialized() throws Exception {
        scheduler.start();
        Thread.sleep(1000);
        assertThat(scheduler.getScheduler().isShutdown()).isFalse();
        verify(tasksRepository, atLeastOnce()).getTaskMapSorted();
    }

    @Test
    void testCheckAndFetchSkipsWhenNoTasksInRepository() {
        when(tasksRepository.getTaskMapSorted()).thenReturn(Collections.emptyMap());
        scheduler.start();
        scheduler.checkAndFetch();
        verify(tasksRepository, never()).update(anyLong(), any());
    }
    @Test
    void testStartSchedulesTaskWithCorrectInterval() {
        scheduler.start();
        assertThat(new ScheduledFuture[]{scheduler.getTaskFuture()}).isNotNull();
    }

    @Test
    void testShutdownCancelsTaskAndShutsDownScheduler() {
        scheduler.start();
        scheduler.shutdown();
        assertThat(scheduler.getTaskFuture().isCancelled()).isTrue();
        assertThat(scheduler.getScheduler().isShutdown()).isTrue();
    }

    @Test
    void testProcessTaskSkipsWhenTimeIntervalNotReached() {
        Vacancy v = new Vacancy();
        v.setRegion("77");
        SearchTaskVacancy s = new SearchTaskVacancy(v);
        Task task = new Task(1L, s);
        task.setLastApiRequestAt(LocalDateTime.now().minusMinutes(10));
        Map<Long, Task> taskMap = new HashMap<>();
        taskMap.put(1L, task);
        when(tasksRepository.getTaskMapSorted()).thenReturn(taskMap);
        scheduler.checkAndFetch();
        verify(apiClient, never()).get(anyString());
        verify(tasksRepository, never()).update(anyLong(), any());
    }

    @Test
    void testShutdownStopsSchedulerAndCancelsFuture() {
        scheduler.start();
        scheduler.close();
        assertThat(scheduler.getScheduler().isShutdown()).isTrue();
    }

    @Test
    void testProcessTaskHandlesNullSearchAndNullVacancySafely() {
        Task taskWithoutSearch = new Task(123L, null);
        Task taskWithNullVacancy = new Task(123L, new SearchTaskVacancy(null));

        Map<Long, Task> taskMap = new HashMap<>();
        taskMap.put(1L, taskWithoutSearch);
        taskMap.put(2L, taskWithNullVacancy);
        when(tasksRepository.getTaskMapSorted()).thenReturn(taskMap);
        scheduler.checkAndFetch();
        verify(apiClient, never()).get(anyString());
        verify(tasksRepository, never()).update(anyLong(), any(Task.class));
    }

    @Test
    void testCheckAndFetchWhenNoTasks() {
        taskMapSpy.clear();
        scheduler.checkAndFetch();

        verify(apiClient, never()).get(anyString());
        verify(tasksRepository, never()).update(anyLong(), any(Task.class));
        verify(taskRepositoryMock, never()).save(any(Task.class));
    }

    @Test
    void testProcessTaskSkipsWhenIntervalNotReached() {
        Long peerId = 123L;
        Long taskId = 1L;
        LocalDateTime lastRequest = LocalDateTime.now().minusMinutes(10);
        Vacancy vacancy = mock(Vacancy.class);
        vacancy.setRegion("77");
        SearchTaskVacancy search = new SearchTaskVacancy(vacancy);
        Task task = new Task(peerId, search);
        task.setIdTask(taskId);
        task.setLastApiRequestAt(lastRequest);

        taskMapSpy.put(taskId, task);
        scheduler.checkAndFetch();
        verify(apiClient, never()).get(anyString());
        verify(tasksRepository, never()).update(eq(taskId), any(Task.class));
    }

    @Test
    void testProcessTaskHandlesExceptionWithoutStoppingLoop(){
        Long peerId1 = 111L;
        Long peerId2 = 222L;
        Long taskId1 = 5L;
        Long taskId2 = 6L;
        LocalDateTime oldTime = LocalDateTime.now().minusHours(30);
        Vacancy v1 = new Vacancy();
        v1.setRegion("77");
        Vacancy v2 = new Vacancy();
        v2.setRegion("78");
        Task t1 = new Task(peerId1, new SearchTaskVacancy(v1));
        t1.setIdTask(taskId1);
        t1.setLastApiRequestAt(oldTime);

        Task t2 = new Task(peerId2, new SearchTaskVacancy(v2));
        t2.setIdTask(taskId2);
        t2.setLastApiRequestAt(oldTime);
        doThrow(new RuntimeException("Simulated API Error"))
                .when(client)
                .fetchAll(anyMap(), anyString());
        scheduler = new VacancyFetchScheduler(client, tasksRepository);
        taskMapSpy.put(taskId1, t1);
        taskMapSpy.put(taskId2, t2);
        when(tasksRepository.getTaskMapSorted()).thenReturn(taskMapSpy);
        scheduler.checkAndFetch();
        verify(tasksRepository, never()).update(eq(taskId1), any(Task.class));
        reset(client);
        doThrow(new RuntimeException("Simulated API Error"))
                .doReturn(createVacancies(2))
                .when(client)
                .fetchAll(anyMap(), anyString());
        taskMapSpy.clear();
        taskMapSpy.put(taskId1, t1);
        taskMapSpy.put(taskId2, t2);
        when(tasksRepository.getTaskMapSorted()).thenReturn(taskMapSpy);
        scheduler.checkAndFetch();

        verify(tasksRepository, times(1)).update(eq(taskId2),
                any(Task.class));
        verify(tasksRepository, never()).update(eq(taskId1), any(Task.class));
    }
    @Test
    void testProcessTaskFetchesAndUpdatesWhenIntervalReached() {
        Long peerId = 456L;
        Long taskId = 2L;
        LocalDateTime lastRequest = LocalDateTime.now().minusHours(24);

        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getRegion()).thenReturn("78");
        when(vacancy.getExperience()).thenReturn("до года");

        SearchTaskVacancy search = new SearchTaskVacancy(vacancy);
        Task task = new Task(peerId, search);
        task.setIdTask(taskId);
        task.setLastApiRequestAt(lastRequest);
        List<VacancyResponse> fetchedVacancies = createVacancies(3);
        doReturn(fetchedVacancies).when(client).fetchAll(anyMap(), anyString());
        taskMapSpy.clear();
        taskMapSpy.put(taskId, task);
        when(tasksRepository.getTaskMapSorted()).thenReturn(taskMapSpy);
        scheduler.checkAndFetch();
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(tasksRepository, times(1)).update(eq(taskId),
                taskCaptor.capture());
        Task updatedTask = taskCaptor.getValue();
        assertThat(updatedTask.getLastApiRequestAt()).isCloseTo(LocalDateTime.now(),
                within(5, SECONDS));
        assertThat(updatedTask.getVacancyResponseList()).isEqualTo(fetchedVacancies);
    }

    @Test
    void testParametersAreBuiltCorrectly() {
        Long peerId = 789L;
        Long taskId = 3L;
        LocalDateTime oldTime = LocalDateTime.now().minusHours(30);
        Vacancy vacancy = new Vacancy();
        vacancy.setRegion("50");
        vacancy.setExperience("3-5 лет");
        vacancy.setZp("80000");
        vacancy.setKeyword("backend");

        SearchTaskVacancy search = new SearchTaskVacancy(vacancy);
        Task task = new Task(peerId, search);
        task.setIdTask(taskId);
        task.setLastApiRequestAt(oldTime);
        assertThat(task.getSearchTaskVacancy()).isNotNull();
        assertThat(task.getSearchTaskVacancy().getVacancy()).isNotNull();
        List<VacancyResponse> dummy = new ArrayList<>();
        doReturn(dummy).when(client).fetchAll(anyMap(), anyString());
        taskMapSpy.clear();
        taskMapSpy.put(taskId, task);
        when(tasksRepository.getTaskMapSorted()).thenReturn(taskMapSpy);
        scheduler.checkAndFetch();
        ArgumentCaptor<Map<String, String>> paramCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> regionCaptor = ArgumentCaptor.forClass(String.class);
        verify(client, times(1)).fetchAll(paramCaptor.capture(),
                regionCaptor.capture());
        Map<String, String> params = paramCaptor.getValue();
        assertThat(regionCaptor.getValue()).isEqualTo("50");
        assertThat(params.get("experienceFrom")).isEqualTo("3");
        assertThat(params.get("salary")).isEqualTo("80000");
        assertThat(params.get("text")).isEqualTo("backend");
    }

    @Test
    void testLastApiRequestAtUpdatedOnlyOnSuccess() {
        Long peerId = 999L;
        Long taskId = 7L;
        LocalDateTime oldTime = LocalDateTime.now().minusHours(30);
        Vacancy vacancy = new Vacancy();
        vacancy.setRegion("77");
        SearchTaskVacancy search = new SearchTaskVacancy(vacancy);
        Task task = new Task(peerId, search);
        task.setIdTask(taskId);
        task.setLastApiRequestAt(oldTime);
        List<VacancyResponse> vacancies = createVacancies(2);
        doReturn(vacancies).when(client).fetchAll(anyMap(), anyString());
        taskMapSpy.put(taskId, task);
        when(tasksRepository.getTaskMapSorted()).thenReturn(taskMapSpy);
        scheduler.checkAndFetch();
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(tasksRepository, times(1)).update(eq(taskId), captor.capture());
        Task updated = captor.getValue();
        assertThat(updated.getLastApiRequestAt())
                .isCloseTo(LocalDateTime.now(), within(5, SECONDS));
        assertThat(updated.getVacancyResponseList()).isEqualTo(vacancies);
    }
    private List<VacancyResponse> createVacancies(int count) {
        List<VacancyResponse> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            VacancyResponse vr = new VacancyResponse();
            list.add(vr);
        }
        return list;
    }
}
