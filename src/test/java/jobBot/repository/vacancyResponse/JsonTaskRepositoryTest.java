package jobBot.repository.vacancyResponse;

import com.google.gson.Gson;
import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Task;
import jobBot.dto.Vacancy;
import jobBot.dto.VacancyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JsonTaskRepositoryTest {

    @TempDir
    Path tempDir;

    private JsonTaskRepository repository;
    private Task task1;
    private Task task2;
    private SearchTaskVacancy vacancy1;
    private SearchTaskVacancy vacancy2;

    @BeforeEach
    void setUp() {
        vacancy1 = new SearchTaskVacancy(new Vacancy());
        vacancy1.getVacancy().setRegion("77");
        vacancy1.getVacancy().setKeyword("Java");

        vacancy2 = new SearchTaskVacancy(new Vacancy());
        vacancy2.getVacancy().setRegion("16");
        vacancy2.getVacancy().setKeyword("Kotlin");

        task1 = new Task(100L, vacancy1);
        task1.setIdTask(1L);
        task1.setLastApiRequestAt(LocalDateTime.now());
        task1.getVacancyResponseList().add(new VacancyResponse());

        task2 = new Task(200L, vacancy2);
        task2.setIdTask(2L);

        repository = new JsonTaskRepository(tempDir.toString());
    }

    @Test
    void testFindByIdWhenTaskExistsInFile() {
        repository.save(task1);
        Optional<Task> result = repository.findById(task1);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(task1);
    }

    @Test
    void testFindByIdWhenTaskDoesNotExist() {
        Task nonExistingTask = new Task(999L, new SearchTaskVacancy(new Vacancy()));
        nonExistingTask.setIdTask(999L);
        Optional<Task> result = repository.findById(nonExistingTask);
        assertThat(result).isEmpty();
    }

    @Test
    void testExistsWhenFilePresent() {
        repository.save(task1);
        boolean result = repository.exists(task1);
        assertThat(result).isTrue();
    }

    @Test
    void testExistsWhenFileAbsent() {
        Task dummyTask = new Task(555L, new SearchTaskVacancy(new Vacancy()));
        dummyTask.setIdTask(555L);
        boolean result = repository.exists(dummyTask);
        assertThat(result).isFalse();
    }
    @Test
    void testSaveWhenTaskSavedSuccessfully() {
        String expectedFileName = task1.getIdTask() + "_" + task1.getPeerId() + "_task.json";
        Path expectedPath = tempDir.resolve(expectedFileName);
        repository.save(task1);
        assertThat(Files.exists(expectedPath)).isTrue();
        try {
            String content = Files.readString(expectedPath);
            Gson gson = new Gson();
            Task loadedTask = gson.fromJson(content, Task.class);
            assertThat(loadedTask).isEqualTo(task1);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл для проверки", e);
        }
    }

    @Test
    void testSaveWhenOverwritingExistingFile() {
        repository.save(task1);
        LocalDateTime newTime = LocalDateTime.MAX;
        task1.setLastApiRequestAt(newTime);
        repository.save(task1);
        String fileName = task1.getIdTask() + "_" + task1.getPeerId() + "_task.json";
        Path path = tempDir.resolve(fileName);
        assertThat(Files.exists(path)).isTrue();

        try {
            String content = Files.readString(path);
            Gson gson = new Gson();
            Task loadedTask = gson.fromJson(content, Task.class);
            assertThat(loadedTask.getLastApiRequestAt()).isEqualTo(newTime);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать обновленный файл", e);
        }
    }

    @Test
    void testDeleteWhenFileRemovedSuccessfully() {
        repository.save(task1);
        String fileName = task1.getIdTask() + "_" + task1.getPeerId() + "_task.json";
        Path path = tempDir.resolve(fileName);
        assertThat(Files.exists(path)).isTrue();
        repository.delete(task1);
        assertThat(Files.exists(path)).isFalse();
        assertThat(repository.exists(task1)).isFalse();
    }

    @Test
    void testDeleteWhenFileAlreadyAbsent() {

        Task dummyTask = new Task(777L, new SearchTaskVacancy(new Vacancy()));
        dummyTask.setIdTask(777L);
        repository.delete(dummyTask);
        assertThat(repository.exists(dummyTask)).isFalse();
    }

    @Test
    void testFindAllWhenMultipleTasksPresent() {
        repository.save(task1);
        repository.save(task2);

        List<Task> allTasks = repository.findAll();
        assertThat(allTasks).hasSize(2);
        assertThat(allTasks).containsExactlyInAnyOrder(task1, task2);
    }

    @Test
    void testFindAllWhenNoTasksInDirectory() {
        List<Task> allTasks = repository.findAll();
        assertThat(allTasks).isEmpty();
    }

    @Test
    void testFindAllWhenDirectoryContainsNonTaskFiles() {
        Path extraFile = tempDir.resolve("not_a_task.txt");
        try {
            Files.writeString(extraFile, "some random text");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        repository.save(task1);
        List<Task> allTasks = repository.findAll();
        assertThat(allTasks).hasSize(1);
        assertThat(allTasks).contains(task1);
    }

    @Test
    void testFindAllWhenEmptyJsonFileExists() {
        Path emptyFile = tempDir.resolve("123_456_task.json");
        try {
            Files.writeString(emptyFile, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Task> tasks = repository.findAll();
        assertThat(tasks).isEmpty();
    }

    @Test
    void testFindAllWhenJsonContainsNullInsteadOfObject() {
        Path nullFile = tempDir.resolve("777_888_task.json");
        try {
            Files.writeString(nullFile, "null");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Task> tasks = repository.findAll();
        assertThat(tasks).isEmpty();
    }

    @Test
    void testFileNameGenerationWhenIdAndPeerIdAreLargeNumbers() {
        Task largeTask = new Task(999999999L, new SearchTaskVacancy(new Vacancy()));
        largeTask.setIdTask(888888888L);
        repository.save(largeTask);
        String expectedFileName = largeTask.getIdTask() + "_" + largeTask.getPeerId() + "_task.json";
        Path path = tempDir.resolve(expectedFileName);
        assertThat(Files.exists(path)).isTrue();
    }

    @Test
    void testReadFromFileWhenIOExceptionOccursSimulated() {
        repository.save(task1);
        Path file = tempDir.resolve(task1.getIdTask() + "_" + task1.getPeerId() + "_task.json");
        try {
            Files.delete(file);
        } catch (IOException ignored) {}
        assertThatThrownBy(() -> repository.findById(task1).orElseThrow())
                .isInstanceOf(RuntimeException.class);
    }
}
