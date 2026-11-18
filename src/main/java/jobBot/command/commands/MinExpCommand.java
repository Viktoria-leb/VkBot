package jobBot.command.commands;

import jobBot.command.BotCommand;

import jobBot.command.CommandType;
import jobBot.command.MakeChanges;
import jobBot.dto.VkUser;

import java.util.Map;

public class MinExpCommand implements BotCommand, MakeChanges {

    @Override
    public CommandType getType() {
        return CommandType.MIN_EXP;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return Map.of(MinExpType.WITHOUT.getButtonName(), 0,
                MinExpType.ONE.getButtonName(), 2,
                MinExpType.THREE.getButtonName(), 4,
                MinExpType.MORE.getButtonName(), 6);
    }


    @Override
    public String message(VkUser vkUser) {
        return "Выберите параметр из списка " +
                "или" +
                " введите команду: "
                + getType().getCommand() + " пробел и само значение.";
    }

    @Override
    public void work(String text, VkUser vkUser) {
        vkUser.getVacancyNew().setExperience(text);
        vkUser.setUpdateParameter(true);
    }
}

