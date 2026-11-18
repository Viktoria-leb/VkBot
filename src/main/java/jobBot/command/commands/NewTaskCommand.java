package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.command.MakeChanges;
import jobBot.dto.Vacancy;
import jobBot.dto.VkUser;

import java.util.Map;

public class NewTaskCommand implements BotCommand, MakeChanges {

    @Override
    public CommandType getType() {
        return CommandType.NEW_TASK;
    }

    @Override
    public String message(VkUser vkUser) {
        return "Выберите параметр для заполнения из списка :";
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return parameter;
    }

    @Override
    public void work(String text, VkUser vkUser) {
        vkUser.setVacancyNew(new Vacancy());
    }
}
