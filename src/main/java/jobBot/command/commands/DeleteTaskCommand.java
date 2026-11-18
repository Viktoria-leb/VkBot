package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;

import jobBot.command.MakeChanges;
import jobBot.dto.VkUser;

import java.util.Map;

public class DeleteTaskCommand implements BotCommand, MakeChanges {

    @Override
    public CommandType getType() {
        return CommandType.DELETE_TASK;
    }

    @Override
    public String message(VkUser user) {
        String message = "";
        if (user.isDeleteVacancy()) {
            message = "Заявка успешно удалена";
        } else if (user.getVacancyMap().isEmpty()) {
            message = "У вас нет заявок для удаления." +
                    " Необходимо сначала создать заявку на поиск.";
        }
        return message;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return menu;
    }


    @Override
    public void work(String text, VkUser vkUser) {
        if (!vkUser.getVacancyMap().isEmpty()) {
            vkUser.setDeleteVacancy(true);
            if (vkUser.getVacancyMap().size() == 1) {
                vkUser.getVacancyMap().remove(vkUser.getVacancyMap().lastKey());
            } else {
                if (text.matches("\\d+")) {
                    vkUser.getVacancyMap().remove(Integer.parseInt(text));
                }
            }
        }
    }
}

