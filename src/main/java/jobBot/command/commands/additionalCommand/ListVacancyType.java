package jobBot.command.commands.additionalCommand;

import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.VkUser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class ListVacancyType {
    @Getter
    @Setter
    private VkUser userVk;
    private TreeMap<Integer, SearchTaskVacancy> vacancyMap;
    private List<Integer> keyOfVacancyMap;
    private List<String> subList;
    @Getter
    int page;
    private static final int PAGE_SIZE = 5;

    public ListVacancyType() {
        page = 0;
    }

    public Map<Integer, SearchTaskVacancy> getVacancyMap() {
        vacancyMap = new TreeMap<>();
        vacancyMap = getUserVk().getVacancyMap();
        return vacancyMap;
    }

    public List<Integer> getKeyOfVacancyMap() {
        keyOfVacancyMap = new ArrayList<>();
        getVacancyMap().forEach((k, v) -> keyOfVacancyMap.add(k));
        return keyOfVacancyMap;
    }

    public List<String> getSubList() {
        addSubVacancy();
        return subList;
    }

    public void setPage(int page) {
        if (page * PAGE_SIZE >= getVacancyMap().size()) {
            this.page = 0;
        } else {
            this.page = page;
        }
    }

    private void addSubVacancy() {
        subList = new ArrayList<>();
        int from = getPage() * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, getVacancyMap().size());
        for (int i = from; i < to; i++) {

            subList.add(getKeyOfVacancyMap().get(i).toString());
        }
        setPage(getPage() + 1);
    }
}
