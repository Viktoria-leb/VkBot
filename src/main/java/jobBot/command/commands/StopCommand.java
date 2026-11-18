package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.command.MakeChanges;
import jobBot.dto.VkUser;

import java.util.Map;

public record StopCommand(VkUser vkUser) implements BotCommand, MakeChanges {

    @Override
    public CommandType getType() {
        return CommandType.STOP;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return menu;
    }

    @Override
    public String message(VkUser vkUser) {
        return "Все заявки на поиск удалены";
    }

    @Override
    public void work(String text, VkUser vkUser) {
        vkUser.getVacancyMap().clear();
    }
}
