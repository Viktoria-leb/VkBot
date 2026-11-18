package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.command.MakeChanges;
import jobBot.dto.Vacancy;
import jobBot.dto.VkUser;

import java.util.Map;

public record DoneCommand(RegionCommandType regionCommandType)
        implements BotCommand, MakeChanges {

    @Override
    public CommandType getType() {
        return CommandType.DONE;
    }

    @Override
    public String message(VkUser vkUser) {
        String message;
        if (vkUser.isAddVacancy()) {
            message = "Запущен поиск вакансий с параметрами: " +
                    vkUser.getVacancyNew().toString();
        } else if (vkUser.isUpdateVacancy()) {
            if (vkUser.isDeleteDuplicate()) {
                message = "Обновление совпала с параметрами уже существующего поиска." +
                        "Дублирующий поиск удален.";
            } else {
                message = "Параметры поиска обнoвлены" +
                        " запущен поиск с параметрами: " +
                        vkUser.getVacancyNew().toString();
            }
        } else {
            message = "Уже запущен поиск с такими параметрами.";
        }

        return message;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return menu;
    }


    @Override
    public void work(String text, VkUser vkUser) {
        Vacancy vacancy = vkUser.getVacancyNew();
        if (!vkUser.isUpdateVacancy()) {
            vkUser.addVacancy(vacancy);
        } else {
            vkUser.updatesVacancy(vacancy);
        }
    }
}