package jobBot.repository.vacancyResponse;

import jobBot.dto.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Optional<Task> findById(Task task);

    boolean exists(Task task);

    void save(Task task);

    void delete(Task task);

    List<Task> findAll();
}
