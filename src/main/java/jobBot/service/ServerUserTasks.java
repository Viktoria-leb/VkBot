package jobBot.service;

import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.base.City;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import jobBot.command.CommandType;
import jobBot.command.commands.RegionCommandType;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Task;
import jobBot.dto.Vacancy;
import jobBot.dto.VkUser;
import jobBot.repository.UserRepository;
import jobBot.repository.task.TasksRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ServerUserTasks(VkApiClient vk, GroupActor actor, String text, Long peerId, UserRepository userRepository,
                              RegionCommandType regionCommandType, ListVacancyType listVacancyType,
                              TasksRepository tasksRepository) {

    public void process() {
        VkUser vkUser = obtainingUserData(vk, actor, peerId, userRepository);
        listVacancyType.setUserVk(vkUser);
        new ServiceBot(vk, actor, regionCommandType, listVacancyType, vkUser, text);
        saveUserRepository(vkUser, tasksRepository);
    }

    private VkUser obtainingUserData(VkApiClient vk,
                                     GroupActor actor,
                                     Long peerId, UserRepository userRepository) {
        if (!userRepository.exists(peerId)) {
            VkUser vkUser = countryUser(vk, actor, peerId);
            userRepository.save(vkUser);
        }
        return userRepository.findById(peerId).get();
    }

    private VkUser countryUser(VkApiClient vk, GroupActor actor, Long peerId) {
        VkUser vkUser;
        try {
            List<GetResponse> execute = vk.users().get(actor)
                    .userIds(String.valueOf(peerId))
                    .fields(Fields.CITY)
                    .lang(Lang.RU)
                    .execute();

            GetResponse getResponse = execute.getFirst();
            vkUser = new VkUser(peerId,
                    getResponse.getFirstName(),
                    getResponse.getLastName());
            City city = getResponse.getCity();
            String titleCity = city.getTitle();
            Integer idCity = city.getId();
            return vkUser;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUserRepository(VkUser vkUser, TasksRepository tasksRepository) {
        userRepository().save(vkUser);

        if (!vkUser.getVacancyMap().isEmpty()) {
            updateTasksInRepository(vkUser, tasksRepository);
        } else if (CommandType.fromText(vkUser.lastCommand()) == CommandType.DONE) {
            deleteUserAndTasks(vkUser, tasksRepository);
        }
    }

    private void deleteUserAndTasks(VkUser vkUser, TasksRepository tasksRepository) {
        userRepository().delete(vkUser.getId());
        tasksRepository.deleteByPeerId(vkUser.getId());
    }

    private void updateTasksInRepository(VkUser vkUser, TasksRepository tasksRepository) {
        Long peerId = vkUser.getId();
        Map<Long, Task> existingTasks = tasksRepository.findTaskForPeerId(peerId);
        Map<Integer, SearchTaskVacancy> userVacancies = vkUser.getVacancyMap();

        existingTasks.forEach((id, task) -> {
            Vacancy taskVacancy = task.getSearchTaskVacancy().getVacancy();
            boolean isStillNeeded = userVacancies.values().stream()
                    .anyMatch(stw -> stw.getVacancy().equals(taskVacancy));
            if (!isStillNeeded) {
                tasksRepository.delete(id);
            }
        });

        userVacancies.forEach((key, stw) -> {
            Vacancy vacancy = stw.getVacancy();
            boolean alreadyExists = existingTasks.values().stream()
                    .anyMatch(t -> t.getSearchTaskVacancy().getVacancy().equals(vacancy));
            if (!alreadyExists) {
                Task newTask = new Task(peerId, stw);
                tasksRepository.save(newTask);
            }
        });
    }
}