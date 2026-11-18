package jobBot.service;

import jobBot.command.commands.MinExpType;
import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Task;
import jobBot.dto.Vacancy;
import jobBot.dto.VacancyResponse;
import jobBot.repository.task.TasksRepository;
import jobBot.restApi.AbstractRestApiClient;
import jobBot.restApi.TrudVsemApiClient;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class VacancyFetchScheduler implements AutoCloseable {
    private static final long CHECK_INTERVAL_MINUTES = 5;
    private static final long FETCH_INTERVAL_HOURS = 22;

    @Getter
    private final AbstractRestApiClient<VacancyResponse> apiClient;
    @Getter
    private final TasksRepository tasksRepository;
    @Getter
    private final ScheduledExecutorService scheduler;
    @Getter
    private ScheduledFuture<?> taskFuture;


    public VacancyFetchScheduler(AbstractRestApiClient<VacancyResponse> apiClient, TasksRepository tasksRepository) {
        this.apiClient = apiClient;
        this.tasksRepository = tasksRepository;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "VacancyFetchScheduler-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        taskFuture = scheduler.scheduleAtFixedRate(
                this::checkAndFetch,
                0,
                CHECK_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    protected void checkAndFetch() {
        Map<Long, Task> taskMap = getTasksRepository().getTaskMapSorted();
        if (taskMap.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<Long, Task> entry : taskMap.entrySet()) {
            Task task = entry.getValue();
            try {
                processTask(task, now);
            } catch (Exception e) {
                System.err.println("Ошибка при обработке задачи " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    private void processTask(Task task, LocalDateTime now) {
        SearchTaskVacancy search = task.getSearchTaskVacancy();
        if (search == null) {
            return;
        }
        Vacancy vacancy = search.getVacancy();
        if (vacancy == null) {
            return;
        }
        LocalDateTime lastRequest = task.getLastApiRequestAt();

        if (lastRequest == null) {
            lastRequest = search.getLocalDateTime();
        }
        if (lastRequest != null && Duration.between(lastRequest, now).toHours() < FETCH_INTERVAL_HOURS) {
            return;
        }
        String region = vacancy.getRegion();

        String parameters;
        Map<String, String> param = new LinkedHashMap<>();
        parameters = vacancy.getExperience();
        String resultExperienceFrom = "";
        MinExpType minExpType = MinExpType.fromText(parameters);
        switch (minExpType) {
            case WITHOUT -> resultExperienceFrom = "0";
            case ONE -> resultExperienceFrom = "1";
            case THREE -> resultExperienceFrom = "3";
            case MORE -> resultExperienceFrom = "5";
        }
        if (parameters != null && !parameters.trim().isEmpty()) {
            param.put("experienceFrom", resultExperienceFrom);
        }
        parameters = vacancy.getZp();
        if (parameters != null && !parameters.trim().isEmpty()) {
            param.put("salary", parameters);

        }
        parameters = vacancy.getKeyword();
        if (parameters != null && !parameters.trim().isEmpty()) {
            param.put("text", parameters);
        }
          TrudVsemApiClient trudClient = (TrudVsemApiClient) apiClient;
        List<VacancyResponse> vacancies = trudClient.fetchAll(param, region);
        task.setLastApiRequestAt(now);
        task.setVacancyResponseList(vacancies);
        getTasksRepository().update(task.getIdTask(), task);
    }

    @Override
    public void close() {
        shutdown();
    }

    public void shutdown() {
        System.out.println("Остановка VacancyFetchScheduler...");
        if (taskFuture != null) {
            taskFuture.cancel(false);
        }
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Планировщик не остановился вовремя, принудительно завершаем.");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Прервано при остановке планировщика.");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("VacancyFetchScheduler остановлен.");
    }
}