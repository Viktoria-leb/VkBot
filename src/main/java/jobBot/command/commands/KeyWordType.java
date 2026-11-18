package jobBot.command.commands;

import java.util.ArrayList;
import java.util.List;

public class KeyWordType {
    private final List<String> commonKeywords = new ArrayList<>();

    public KeyWordType() {
    }

    public List<String> getCommonKeywords() {
        commonKeywords.add("Продавец");
        commonKeywords.add("Разнорабочий");
        commonKeywords.add("Разработчик на java");
        commonKeywords.add("Ученик backend на java");
        commonKeywords.add("Java");
        return commonKeywords;
    }

    public void addCommonKeywords(String keyword) {
        commonKeywords.add(keyword);
    }

    public boolean searchFromText(String text) {
        return getCommonKeywords().contains(text);
    }
}

