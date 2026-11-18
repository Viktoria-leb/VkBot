package jobBot.command.commands.additionalCommand;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.dto.VkUser;

import java.util.Map;

public class UnknownCommand implements BotCommand {

    @Override
    public CommandType getType() {
        return CommandType.NONE;
    }

    @Override
    public String message(VkUser vkUser) {
        return "Команда не распознана. Напоминаю: Вы зарегистрированы в боте " +
                "по подбору вакансий. Что вы хотите сделать?" +
                "(выберите соответствующую кнопку меню) ";
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return menu;
    }

    @Override
    public void work(String text, VkUser vkUser) {
    }
}

