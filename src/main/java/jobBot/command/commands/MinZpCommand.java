package jobBot.command.commands;

import jobBot.command.BotCommand;

import jobBot.command.CommandType;
import jobBot.command.MakeChanges;
import jobBot.dto.VkUser;

import java.util.Map;

public class MinZpCommand implements BotCommand, MakeChanges {

    @Override
    public CommandType getType() {
        return CommandType.MIN_ZP;
    }

    @Override
    public String message(VkUser vkUser) {
        return "Выберите параметр из списка " +
                "или" +
                " введите команду: "
                + getType().getCommand() + " пробел и само значение.";
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return Map.of(MinZpType.FIFTY.getButtonName(), 1,
                MinZpType.SEVENTY.getButtonName(), 2,
                MinZpType.ONE_HUNDRED.getButtonName(), 3,
                MinZpType.ONE_HUNDRED_FIFTY.getButtonName(), 4);
    }

    @Override
    public void work(String text, VkUser vkUser) {
        vkUser.getVacancyNew().setZp(text);
        vkUser.setUpdateParameter(true);
    }
}
