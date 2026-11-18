package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.dto.VkUser;

import java.util.Map;

public class CurrentTasksCommand implements BotCommand {

    @Override
    public CommandType getType() {
        return CommandType.CURRENT_TASKS;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return menu;
    }

    @Override
    public String message(VkUser user) {
        if (user.getVacancyMap().isEmpty()) {
            return "У вас нет заявок на поиск." +
                    " Необходимо сначала создать заявку на поиск.";
        }
        return "Ваши текущие заявки на поиск: \n" + user.listOfAllVacancies();
    }

    @Override
    public void work(String text, VkUser vkUser) {
    }
}
