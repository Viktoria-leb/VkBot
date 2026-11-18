package jobBot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractJsonRepository {
    protected final Gson gson = new GsonBuilder().setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd kk:mm").create();
    protected final String dataDirectory;

    protected AbstractJsonRepository(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        ensureDirectory();
    }

    private void ensureDirectory() {
        try {
            Files.createDirectories(Paths.get(dataDirectory));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию: " + dataDirectory, e);
        }
    }

    protected Path filePath(String fileName) {
        return Paths.get(dataDirectory, fileName);
    }

    protected <T> T readFromFile(String fileName, Class<T> clazz) {
        var path = filePath(fileName);
        if (!Files.exists(path)) return null;
        try {
            var json = Files.readString(path);
            return gson.fromJson(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + path, e);
        }
    }

    protected <T> void writeToFile(String fileName, T object) {
        var path = filePath(fileName);
        try {
            var json = gson.toJson(object);
            Files.writeString(path, json);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи файла: " + path, e);
        }
    }

    protected void deleteFile(String fileName) {
        var path = filePath(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка удаления файла: " + path, e);
        }
    }
}

