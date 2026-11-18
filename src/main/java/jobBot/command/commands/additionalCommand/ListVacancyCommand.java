package jobBot.command.commands.additionalCommand;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.dto.VkUser;

import java.util.*;

public record ListVacancyCommand(ListVacancyType listVacancyType) implements BotCommand {

    @Override
    public CommandType getType() {
        return CommandType.NONE;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        if (listVacancyType().getVacancyMap().size() > 5) {
            List<String> list = listVacancyType().getSubList();
            int i = 0;
            for (String string : list) {
                i++;
                map.put(string, i);
            }
            map.put(CommandType.NEXT.getButtonName(), i + 1);
        } else {
            listVacancyType().getVacancyMap().forEach((k, v) ->
                    map.put(String.valueOf(k), map.size() + 1)
            );
        }
        return map;
    }

    @Override
    public String message(VkUser user) {
        System.out.println(user.lastCommand());
        if (user.isDeleteVacancy()) {
            return "Выберите номер поика из списка. \n" +
                    "Для удаления всех поисков введите команду " + CommandType.STOP.getCommand()
                    + " ," +
                    "или введите номер поиска: \n "
                    + user.listOfAllVacancies();
        }
        return "Выберите номер поика из списка \n" +
                "или введите номер поиска \n " + user.listOfAllVacancies();
    }

    @Override
    public void work(String text, VkUser vkUser) {
    }
}
