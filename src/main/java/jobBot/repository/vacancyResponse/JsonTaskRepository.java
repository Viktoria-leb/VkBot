package jobBot.repository.vacancyResponse;

import jobBot.dto.Task;
import jobBot.repository.AbstractJsonRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class JsonTaskRepository extends AbstractJsonRepository implements TaskRepository {
    private static final String FILE_SUFFIX = "_task.json";

    public JsonTaskRepository(String dataDirectory) {
        super(dataDirectory);
    }

    private String fileName(Task task) {
        return task.getIdTask() + "_" + task.getPeerId() + FILE_SUFFIX;
    }

    @Override
    public Optional<Task> findById(Task task) {
        return Optional.ofNullable(readFromFile(fileName(task), Task.class));
    }

    @Override
    public boolean exists(Task task) {
        return Files.exists(filePath(fileName(task)));
    }


    @Override
    public void save(Task task) {
        writeToFile(fileName(task), task);
    }

    @Override
    public void delete(Task task) {
        deleteFile(fileName(task));
    }

    @Override
    public List<Task> findAll() {
        ArrayList<Task> tasks = new ArrayList<>();
        try (Stream<Path> paths = Files.list(Paths.get(dataDirectory))) {
            paths
                    .filter(p -> p.getFileName().toString().endsWith(FILE_SUFFIX))
                    .forEach(p -> {
                        try {
                            String json = Files.readString(p);
                            Task task = gson.fromJson(json, Task.class);
                            if (task != null) tasks.add(task);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException e) {
            System.err.println("Ошибка чтения списка задач: " + e.getMessage());
        }
        return tasks;
    }
}
