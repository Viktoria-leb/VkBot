package jobBot.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AbstractJsonRepositoryTest {
    private Path tempDir;
    @Spy
    @InjectMocks
    private TestJsonRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("json-repo-test-");
        repository = new TestJsonRepository(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null) {
            Files.walk(tempDir).sorted((o1, o2) -> o2.compareTo(o1))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    void testConstructorEnsuresDirectoryExists() {
        assertThat(tempDir).isDirectory();
    }

    @Test
    void testReadFromFileWhenFileDoesNotExistReturnsNull() {
        String fileName = "non-existent.json";
        Object result = repository.readFromFile(fileName, Object.class);
        assertThat(result).isNull();
    }

    @Test
    void testWriteToFileCreatesFileAndPersistsValidJson() throws IOException {
        String fileName = "test-write.json";
        var data = new TestData("task-1", "Do something", LocalDateTime.now());
        repository.writeToFile(fileName, data);
        Path filePath = repository.filePath(fileName);
        assertThat(filePath).isRegularFile();
        String content = Files.readString(filePath);
        assertThat(content).isNotEmpty();
    }

    @Test
    void testReadFromFileSuccessfullyParsesJson() {
        String fileName = "test-read.json";
        var original = new TestData("task-2", "Read test", LocalDateTime.now().withNano(0));
        repository.writeToFile(fileName, original);
        var result = repository.readFromFile(fileName, TestData.class);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(original.id());
        assertThat(result.title()).isEqualTo(original.title());
        assertThat(result.createdAt().withNano(0)).isEqualTo(original.createdAt().withNano(0));
    }

    @Test
    void testDeleteFileRemovesFile() {
        String fileName = "test-delete.json";
        var data = new TestData("task-3", "Delete test", LocalDateTime.now());
        repository.writeToFile(fileName, data);
        Path filePath = repository.filePath(fileName);
        assertThat(filePath).isRegularFile();
        repository.deleteFile(fileName);
        assertThat(Files.exists(filePath)).isFalse();
    }



    @Test
    void testWriteToFileThrowsRuntimeExceptionOnIoException() {
        var path = tempDir.resolve("broken-write.json");
        try {
            Files.createFile(path);
            Files.setAttribute(path, "dos:readonly", true);
        } catch (IOException e) {
        }
        var data = new TestData("task-4", "Write test", LocalDateTime.now());
        assertThatThrownBy(() -> repository.writeToFile("broken-write.json", data))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка записи файла");
    }

    @Test
    void testDeleteFileThrowsRuntimeExceptionOnIoException() {
        var path = tempDir.resolve("broken-delete.json");
        try {
            Files.createFile(path);
            Files.setAttribute(path, "dos:readonly", true);
        } catch (IOException e) {
        }

        assertThatThrownBy(() -> repository.deleteFile("broken-delete.json"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка удаления файла");
    }

    static class TestJsonRepository extends AbstractJsonRepository {
        TestJsonRepository(String dataDirectory) {
            super(dataDirectory);
        }
    }

    record TestData(String id, String title, LocalDateTime createdAt) {}
}

