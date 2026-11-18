package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.dto.Region;
import jobBot.dto.VkUser;

import java.util.HashMap;
import java.util.Map;

public record NextCommand(RegionCommandType regionCommandType,
                          VkUser vkUser,
                          ListVacancyType listVacancyType) implements BotCommand {

    @Override
    public CommandType getType() {
        return CommandType.NEXT;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        Map<String, Integer> map = new HashMap<>();
        int i = 0;
        switch (vkUserRedactorLastCommand()) {
            case REGION -> {
                for (Region region : regionCommandType().getSubListRegion()) {
                    i++;
                    map.put(region.name() + ", " + region.number(), i);
                }
            }
            case UPDATE_TASK, DELETE_TASK -> {
                for (String string
                        : listVacancyType().getSubList()) {
                    i++;
                    map.put(string, i);
                }
            }
        }
        map.put(getType().getButtonName(), i + 1);
        return map;

    }

    @Override
    public String message(VkUser vkUser) {
        String text = "Выберите параметр из списка или" +
                " введите команду: ";
        switch (vkUserRedactorLastCommand()) {
            case REGION -> text = text + CommandType.REGION.getCommand();
            case UPDATE_TASK -> text = text + CommandType.UPDATE_TASK.getCommand();
            case DELETE_TASK -> text = text + CommandType.DELETE_TASK.getCommand();
        }
        return "Продолжение: \n" + text + " пробел и числовое значение";
    }

    @Override
    public void work(String text, VkUser vkUser) {

    }

    private CommandType vkUserRedactorLastCommand() {
        CommandType commandType = null;
        String lastCommand = vkUser.lastCommand();
        if (CommandType.fromText(lastCommand) != CommandType.NONE) {
            commandType = CommandType.fromText(lastCommand);
        } else {
            String t = lastCommand.replace("[", ";");
            String st = t.replace("]", "");
            String[] textWithVacancy = st.split(";");
            if (textWithVacancy.length == 2) {
                commandType = CommandType.fromText(textWithVacancy[0]);
            }
        }
        return commandType;
    }

}
