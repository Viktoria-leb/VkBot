package jobBot.command.commands;

import jobBot.command.BotCommand;

import jobBot.command.CommandType;
import jobBot.command.MakeChanges;
import jobBot.dto.VkUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeywordCommand implements BotCommand, MakeChanges {

    @Override
    public CommandType getType() {
        return CommandType.KEYWORD;
    }

    @Override
    public String message(VkUser vkUser) {
        return "Выберите параметр из списка " +
                "или" +
                " введите команду: "
                + getType().getCommand() + " пробел и ключевое слово.";
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return keywordList();
    }

    private Map<String, Integer> keywordList() {
        Map<String, Integer> keywords = new HashMap<>();
        KeyWordType word = new KeyWordType();
        List<String> type = word.getCommonKeywords();
        for (int i = 0; i < type.size(); i++) {
            keywords.put(type.get(i), i);
        }
        return keywords;
    }

    @Override
    public void work(String text, VkUser vkUser) {
        vkUser.getVacancyNew().setKeyword(text);
        vkUser.setUpdateParameter(true);
    }
}
