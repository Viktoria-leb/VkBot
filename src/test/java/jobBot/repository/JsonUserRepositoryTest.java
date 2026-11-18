package jobBot.repository;

import com.google.gson.Gson;


import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Vacancy;
import jobBot.dto.VkUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;


import static org.assertj.core.api.Assertions.assertThat;


class JsonUserRepositoryTest {
    private Path tempDir;
    private JsonUserRepository repository;
    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("json-user-repo-test-");
        repository = new JsonUserRepository(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null) {
            Files.walk(tempDir)
                    .sorted()
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {}
                    });
        }
    }


    @Test
    void testFindByIdWhenFileExistsReturnsUser() throws IOException {
        Long id = 1L;
        String fileName = id + "_user.json";
        Path path = tempDir.resolve(fileName);
        VkUser user = new VkUser(id, "Илья", "Петров");
        new Gson().toJson(user);
        Files.writeString(path, new Gson().toJson(user));
        Optional<VkUser> result = repository.findById(id);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getId()).isEqualTo(user.getId());
        assertThat(result.get().getFirstName()).isEqualTo(user.getFirstName());
        assertThat(result.get().getLastName()).isEqualTo(user.getLastName());
    }

    @Test
    void testFindByIdWhenFileDoesNotExistReturnsEmpty() {
        Long id = 999L;
        Optional<VkUser> result = repository.findById(id);
        assertThat(result).isEmpty();
    }

    @Test
    void testExistsWhenFileExistsReturnsTrue() throws IOException {
        Long id = 2L;
        String fileName = id + "_user.json";
        Path path = tempDir.resolve(fileName);
        Files.createFile(path);
        boolean result = repository.exists(id);
        assertThat(result).isTrue();
    }

    @Test
    void testExistsWhenFileDoesNotExistReturnsFalse() {
        Long id = 3L;
        boolean result = repository.exists(id);
        assertThat(result).isFalse();
    }

    @Test
    void testSaveWhenUserProvidedCreatesFileWithValidJson() throws IOException {
        Long id = 4L;
        var user = new VkUser(id, "Вася", "Кудрявцев");
        repository.save(user);
        String fileName = id + "_user.json";
        Path path = tempDir.resolve(fileName);
        assertThat(path).isRegularFile();
        String content = Files.readString(path);
        assertThat(content).isNotEmpty();
        assertThat(content).contains(" \"id\": 4");
        assertThat(content).contains("\"firstName\": \"Вася\"");
        assertThat(content).contains("\"lastName\": \"Кудрявцев\"");
    }

    @Test
    void testDeleteWhenUserExistsRemovesFile() throws IOException {
        Long id = 5L;
        String fileName = id + "_user.json";
        Path path = tempDir.resolve(fileName);
        Files.createFile(path);
        assertThat(Files.exists(path)).isTrue();
        repository.delete(id);
        assertThat(Files.exists(path)).isFalse();
    }

    @Test
    void testDeleteWhenUserDoesNotExistDoesNothing() {
        Long id = 6L;
        repository.delete(id);
    }

    @Test
    void testFindAllWhenNoUsersReturnsEmptyList() {
        List<VkUser> users = repository.findAll();
        assertThat(users).isEmpty();
    }

    @Test
    void testSaveWhenOverwritingExistingUserUpdatesFile() throws IOException {
        Long id = 7L;
        var original = new VkUser(id, "Oleg", "Olegov");
        var updated = new VkUser(id, "Nusha", "Nushkina");

        repository.save(original);
        String fileName = id + "_user.json";
        Path path = tempDir.resolve(fileName);
        String originalContent = Files.readString(path);
        assertThat(originalContent).contains("Oleg");

        repository.save(updated);
        String updatedContent = Files.readString(path);
        assertThat(updatedContent).contains("Nusha");
        assertThat(updatedContent).doesNotContain("Oleg");
    }

    @Test
    void testFindByIdWhenFileExistsReturnsUserWithFullState(){
        Long id = 101L;
        String fileName = id + "_user.json";
        Path path = tempDir.resolve(fileName);
        VkUser user = new VkUser(id, "Ivan", "Ivanov");
        TreeMap<Integer, SearchTaskVacancy> vacancyMap = new TreeMap<>();
        Vacancy v1 = new Vacancy();
        v1.setRegion("Moscow");
        v1.setZp("150000");
        SearchTaskVacancy stv1 = new SearchTaskVacancy(v1);
        vacancyMap.put(1, stv1);
        user.setVacancyMap(vacancyMap);
        LinkedHashMap<Integer, String> history = new LinkedHashMap<>();
        history.put(1, "/start");
        history.put(2, "/search java");
        user.setHistoryCommand(history);
        user.setAddVacancy(true);
        user.setDeleteVacancy(false);
        user.setVacancyUpdateId(5);
        repository.save(user);
        assertThat(path).isRegularFile();
        Optional<VkUser> result = repository.findById(id);
        assertThat(result).isNotEmpty();
        var loadedUser = result.get();

        assertThat(loadedUser.getId()).isEqualTo(id);
        assertThat(loadedUser.getFirstName()).isEqualTo("Ivan");
        assertThat(loadedUser.getLastName()).isEqualTo("Ivanov");

        assertThat(loadedUser.getVacancyMap()).hasSize(1);
        assertThat(loadedUser.getHistoryCommand()).hasSize(2);
        assertThat(loadedUser.isAddVacancy()).isTrue();
        assertThat(loadedUser.isDeleteVacancy()).isFalse();
        assertThat(loadedUser.getVacancyUpdateId()).isEqualTo(5);
    }

    @Test
    void testFindByIdWhenFileDoesNotExistReturnsEmptyOptional() {
        Long nonExistingId = 999L;
        Optional<VkUser> result = repository.findById(nonExistingId);
        assertThat(result).isEmpty();
    }


    @Test
    void testSaveWhenUserProvidedCreatesValidJsonFile() throws Exception {
        Long id = 404L;
        var user = new VkUser(id, "Anna", "Smirnova");

        Vacancy vacancy = new Vacancy();
        vacancy.setZp("200000");
        vacancy.setRegion("SPb");
        user.setVacancyNew(vacancy);
        user.setUpdateParameter(true);
        repository.save(user);
        String fileName = id + "_user.json";
        Path path = tempDir.resolve(fileName);
        assertThat(path).isRegularFile();
        String content = Files.readString(path);
        assertThat(content).isNotEmpty();

        assertThat(content).contains("\"id\": " + id);
        assertThat(content).contains("\"firstName\": \"Anna\"");
        assertThat(content).contains("\"zp\": \"200000\"");
    }

    @Test
    void testFindAllWhenMultipleUsersExistReturnsAllWithCorrectData() throws Exception {
        var u1 = new VkUser(10L, "User1", "One");
        var u2 = new VkUser(20L, "User2", "Two");
        var u3 = new VkUser(30L, "User3", "Three");
        u1.setAddVacancy(true);
        u2.setDeleteDuplicate(true);
        u3.setUpdateVacancy(true);

        repository.save(u1);
        repository.save(u2);
        repository.save(u3);

        Files.writeString(tempDir.resolve("40_other.json"), "{}");

        List<VkUser> users = repository.findAll();

        assertThat(users)
                .hasSize(3)
                .extracting("id")
                .containsExactlyInAnyOrder(10L, 20L, 30L);

        var foundU1 = users.stream().filter(u -> u.getId().equals(10L)).findFirst().orElseThrow();
        assertThat(foundU1.isAddVacancy()).isTrue();
    }


    @Test
    void testSaveWhenOverwritingExistingUserUpdatesFileCompletely() {
        Long id = 707L;
        var original = new VkUser(id, "OldName", "OldSurname");
        original.setAddVacancy(true);
        original.setVacancyUpdateId(1);
        repository.save(original);
        var updated = new VkUser(id, "NewName", "NewSurname");
        updated.setAddVacancy(false);
        updated.setDeleteVacancy(true);
        updated.setVacancyUpdateId(99);
        repository.save(updated);
        Optional<VkUser> loaded = repository.findById(id);
        assertThat(loaded).isNotEmpty();
        var user = loaded.get();

        assertThat(user.getFirstName()).isEqualTo("NewName");
        assertThat(user.getLastName()).isEqualTo("NewSurname");
        assertThat(user.isAddVacancy()).isFalse();
        assertThat(user.isDeleteVacancy()).isTrue();
        assertThat(user.getVacancyUpdateId()).isEqualTo(99);
    }
}


