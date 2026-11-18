package jobBot.service;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import jobBot.command.CommandDispatcher;
import jobBot.command.CommandType;
import jobBot.command.commands.*;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.dto.VkUser;
import lombok.Getter;


@Getter
public class ServiceBot {
    private final VkApiClient vk;
    private final GroupActor actor;
    private final String text;
    private final VkUser vkUser;
    private final RegionCommandType regionCommandType;
    private final ListVacancyType listVacancyType;
    private final KeyWordType keyWordType;

    public ServiceBot(VkApiClient vk, GroupActor actor, RegionCommandType regionCommandType,
                      ListVacancyType listVacancyType,
                      VkUser vkUser, String text) {
        this.vk = vk;
        this.actor = actor;
        this.text = text;
        this.vkUser = vkUser;
        this.regionCommandType = regionCommandType;
        keyWordType = new KeyWordType();
        this.listVacancyType = listVacancyType;
        commandRecognition(vk, actor, text, regionCommandType, listVacancyType, vkUser);
    }

    protected void commandRecognition(VkApiClient vk, GroupActor actor, String text,
                                    RegionCommandType regionCommandType,
                                    ListVacancyType listVacancyType, VkUser userVk) {

        String textResult = redactorText(text, userVk);
        CommandType commandType = CommandType.fromText(textResult);
        CommandDispatcher dispatcher = new CommandDispatcher(vk, actor,
                regionCommandType, listVacancyType, userVk, textResult);
        removingUsageFlags(commandType, userVk, listVacancyType);
        userCommandHistory(commandType, userVk);
    }

    private void removingUsageFlags(CommandType commandType, VkUser vkUser,
                                    ListVacancyType listVacancyType) {
        if (commandType == CommandType.DONE) {
            vkUser.setAddVacancy(false);
            vkUser.setUpdateVacancy(false);
            vkUser.setDeleteDuplicate(false);
            vkUser.setUpdateParameter(false);
            regionCommandType.setPage(0);
            listVacancyType.setPage(0);
        }
        if (commandType == CommandType.DELETE_TASK) {
            vkUser.setDeleteVacancy(false);
            listVacancyType.setPage(0);
        }
    }

    private void userCommandHistory(CommandType commandType, VkUser vkUser) {
        if (commandType != CommandType.NEXT) {
            vkUser.addHistory(text);
        }
    }

    private String redactorText(String text, VkUser user) {
        String result;
        if (CommandType.fromText(text) != CommandType.NONE) {
            result = CommandType.fromText(text).getCommand();
        } else result = noneCommandRedactorText(text, user);
        return result;
    }

    private String noneCommandRedactorText(String text, VkUser user) {
        String result;
        if (text.startsWith("/")) {
            String[] split = text.split(" ", 2);
            if (split.length < 2) {
                result = "";
            } else {
                if (split[0].equals(CommandType.fromText(
                        split[0]).getCommand())) {
                    result = CommandType.fromText(split[0])
                            .getCommand() + " " + split[1];
                } else {
                    result = "";
                }
            }
        } else if (text.matches("\\d+")) {
            if (CommandType.fromText(user.lastCommand()) != CommandType.NONE) {
                result = CommandType.fromText(user.lastCommand()).getCommand() + " " + text;
            } else {
                result = textLastCommandIsUpdateVacancy(user.lastCommand()) + " " + text;
            }
        } else {
            result = notNumberNotCommandText(text);
        }

        return result;
    }

    private String notNumberNotCommandText(String text) {
        String result;
        if (text.startsWith(CommandType.REGION.getButtonName())
                || text.startsWith(CommandType.MIN_EXP.getButtonName())
                || text.startsWith(CommandType.MIN_ZP.getButtonName())
                || text.startsWith(CommandType.KEYWORD.getButtonName())) {
            result = textLastCommandIsUpdateVacancy(text);
        } else if (MinZpType.fromText(text) != MinZpType.NONE) {
            result = CommandType.MIN_ZP.getCommand() + " " + text;
        } else if (MinExpType.fromText(text) != MinExpType.NONE) {
            result = CommandType.MIN_EXP.getCommand() + " " + text;
        } else if (getRegionCommandType().searchFromText(text)) {
            String[] textRegion = text.split(", ");
            if (textRegion.length < 2) {
                result = "";
            } else {
                result = CommandType.REGION.getCommand()
                        + " " + textRegion[1];
            }
        } else if (keyWordType.searchFromText(text)) {
            result = CommandType.KEYWORD.getCommand() + " " + text;
        } else {
            result = "";
        }
        return result;
    }

    private String textLastCommandIsUpdateVacancy(String text) {
        String result;
        String t = text.replace("[", ";");
        String st = t.replace("]", "");
        String[] textWithVacancy = st.split(";");
        if (textWithVacancy.length != 2) {
            result = "";
        } else {
            result = CommandType.fromText(textWithVacancy[0]).getCommand();
        }
        return result;
    }
}



