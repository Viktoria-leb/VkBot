package jobBot.service;

import jobBot.BotVk;
import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Task;
import jobBot.dto.Vacancy;
import jobBot.dto.VacancyResponse;
import jobBot.repository.task.TasksRepository;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ServiceDeliveryResult implements AutoCloseable {
    private static final long CHECK_INTERVAL_MINUTES = 5;
    private static final long FETCH_INTERVAL_HOURS = 24;

    @Getter
    private final BotVk botVk;
    @Getter
    private final TasksRepository tasksRepository;
    @Getter
    private final ScheduledExecutorService scheduler;
    @Getter
    private ScheduledFuture<?> taskFuture;

    public ServiceDeliveryResult(BotVk botVk, TasksRepository tasksRepository) {
        this.botVk = botVk;
        this.tasksRepository = tasksRepository;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ServiceDeliveryResult-Scheduler");
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

    public void checkAndFetch() {
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
        boolean existTask = getTasksRepository().getTaskRepository().exists(task);
        if (!existTask) {
            return;
        }
        SearchTaskVacancy search = task.getSearchTaskVacancy();
        LocalDateTime lastMessage = task.getLastSendingMessages();

        if (lastMessage == null) {
            lastMessage = search.getLocalDateTime();
        }
        if (lastMessage != null && Duration.between(lastMessage, now).toHours() < FETCH_INTERVAL_HOURS) {
            return;
        }

        List<VacancyResponse> vacancies = task.getVacancyResponseList();

        sendingTheResult(vacancies, task);
        task.setLastSendingMessages(now);
        task.setVacancyResponseList(vacancies);
     //   getTasksRepository().update(task.getIdTask(), task);
    }

    private void sendingTheResult(List<VacancyResponse> vacancies, Task task) {
        Long peerId = task.getPeerId();
        SearchTaskVacancy search = task.getSearchTaskVacancy();
        if (search == null) {
            return;
        }
        Vacancy vacancy = search.getVacancy();
        if (vacancy == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Вакансии по поиску: ").append(vacancy).append(":\n\n");

        if (vacancies.isEmpty()) {
            builder.append("Вакансий нет на сайте по данному поиску.");
            writeMessages(peerId, builder, task);
            System.out.println("Отправка сообщения (нет вакансий) — Task ID: " + task.getIdTask());
            return;
        }

        int limitPerMessage = 4000;
        int start = 0;
        int size = vacancies.size();

        if (size <= 10) {
            for (VacancyResponse vr : vacancies) {
                appendVacancyToBuilder(builder, vr);
            }
            writeMessages(peerId, builder, task);
            System.out.println("Отправка сообщения — Task ID: " + task.getIdTask() + ", вакансий: " + size);
            return;
        }

        while (start < size) {
            int end = Math.min(start + 10, size);
            StringBuilder part = new StringBuilder();
            if (start == 0) {
                part.append(builder);
            } else {
                part.append("Вакансии (продолжение):\n\n");
            }
            for (int i = start; i < end; i++) {
                appendVacancyToBuilder(part, vacancies.get(i));
            }

            if (part.length() > limitPerMessage) {
                String truncated = part.substring(0, limitPerMessage);
                writeMessages(peerId, new StringBuilder(truncated), task);
            } else {
                writeMessages(peerId, part, task);
            }

            start = end;
        }
    }

    private void appendVacancyToBuilder(StringBuilder builder, VacancyResponse vr) {
        builder.append("- ").append(vr.toString());
    }

    private void writeMessages(Long peerId, StringBuilder builder,Task task) {
        getBotVk().writeMessages(peerId, builder);
        getTasksRepository().update(task.getIdTask(), task);
    }

    @Override
    public void close() {
        shutdown();
    }

    public void shutdown() {
        System.out.println("Остановка ServiceDeliveryResult...");

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
        System.out.println("ServiceDeliveryResult остановлен.");
    }
}

