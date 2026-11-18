package jobBot.repository.task;

import jobBot.dto.Task;
import jobBot.repository.vacancyResponse.TaskRepository;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TasksRepository {

    private final Map<Long, Task> taskMap;
    @Getter
    private final AtomicLong idGenerator;
    @Getter
    private final TaskRepository taskRepository;

    public TasksRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        taskMap = new ConcurrentHashMap<>();
        idGenerator = new AtomicLong();
    }

    public Map<Long, Task> getTaskMap() {
        List<Task> taskList = taskRepository.findAll();
        taskList.forEach(this::save);
        return taskMap;
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(taskMap.get(id));
    }

    public void save(Task task) {
        if (task.getIdTask() == null) {
            task.setIdTask(idGenerator.getAndIncrement());
        }
        taskMap.put(task.getIdTask(), task);
        getTaskRepository().save(task);
    }

    public void update(Long idTask, Task task) {
        if(taskMap.get(idTask)!=null){
            getTaskRepository().delete(taskMap.get(idTask));}
        if (task.getIdTask() == null || !taskMap.containsKey(task.getIdTask())) {
            throw new IllegalArgumentException(
                    "Задача с id " + task.getIdTask() + " не найдена для обновления."
            );
        }
        taskMap.replace(idTask, task);
        getTaskRepository().save(task);
    }

    public Map<Long, Task> findTaskForPeerId(Long peerId) {
        List<Task> tasks = taskMap.values().stream()
                .filter(t -> peerId.equals(t.getPeerId()))
                .toList();

        return tasks.stream()
                .collect(Collectors.toMap(
                        Task::getIdTask,
                        task -> task
                ));
    }

    public Map<Long, Task> getTaskMapSorted() {
        return getTaskMap().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(
                        Comparator.comparing(t -> t.getSearchTaskVacancy().getLocalDateTime())
                ))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    public void delete(Long id) {
        if(taskMap.get(id)!=null){
        getTaskRepository().delete(taskMap.get(id));
        taskMap.remove(id);
    }}

    public void deleteByPeerId(Long peerId) {
        taskMap.values().forEach(task -> {
            if (peerId.equals(task.getPeerId())) {
                delete(task.getIdTask());
            }
        });
    }
}

