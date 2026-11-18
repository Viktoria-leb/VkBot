package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.dto.VkUser;

import java.util.Map;

public class StartCommand implements BotCommand {
    @Override
    public CommandType getType() {
        return CommandType.DEFAULT;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return menu;
    }

    @Override
    public String message(VkUser vkUser) {
        return "Добро пожаловать! Вы зарегистрированы в боте" +
                " по подбору вакансий. Для того, чтобы начать поиск вакансий," +
                "выберите соответствующую кнопку меню: ";
    }

    @Override
    public void work(String text, VkUser vkUser) {

    }
}

