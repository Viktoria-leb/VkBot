package jobBot.repository;

import jobBot.dto.VkUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class JsonUserRepository extends AbstractJsonRepository
        implements UserRepository {

    private static final String FILE_SUFFIX = "_user.json";

    public JsonUserRepository(String dataDirectory) {
        super(dataDirectory);
    }

    private String fileName(Long id) {
        return id + FILE_SUFFIX;
    }

    @Override
    public Optional<VkUser> findById(Long id) {
        return Optional.ofNullable(readFromFile(fileName(id), VkUser.class));
    }

    @Override
    public boolean exists(Long id) {
        return Files.exists(filePath(fileName(id)));
    }

    @Override
    public void save(VkUser user) {
        writeToFile(fileName(user.getId()), user);
    }

    @Override
    public void delete(Long id) {
        deleteFile(fileName(id));
    }

    @Override
    public List<VkUser> findAll() {
        ArrayList<VkUser> users = new ArrayList<>();
        try (Stream<Path> paths = Files.list(Paths.get(dataDirectory))) {
            paths
                    .filter(p -> p.getFileName().toString().endsWith(FILE_SUFFIX))
                    .forEach(p -> {
                        try {
                            var json = Files.readString(p);
                            var user = gson.fromJson(json, VkUser.class);
                            if (user != null) users.add(user);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException e) {
            System.err.println("Ошибка чтения списка пользователей: " + e.getMessage());
        }
        return users;
    }
}

