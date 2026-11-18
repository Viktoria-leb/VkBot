package jobBot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Setter
@Getter
public class VkUser {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private Vacancy vacancyNew;
    private TreeMap<Integer, SearchTaskVacancy> vacancyMap;
    private Map<Integer, String> historyCommand = new LinkedHashMap<>();
    boolean addVacancy;
    boolean updateVacancy;
    boolean updateParameter;
    boolean deleteDuplicate;
    boolean deleteVacancy;
    int vacancyUpdateId;


    public VkUser(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        vacancyNew = new Vacancy();
        vacancyMap = new TreeMap<>();
        addVacancy = false;
        updateVacancy = false;
        deleteDuplicate = false;
        deleteVacancy = false;
        updateParameter = false;
        vacancyUpdateId = 0;
    }

    public void addVacancy(Vacancy vacancy) {
        SearchTaskVacancy searchTaskVacancy = new SearchTaskVacancy(vacancy);
        if (getVacancyMap().isEmpty()) {
            getVacancyMap().put(1, searchTaskVacancy);
            addVacancy = true;
        } else {
            if (!getVacancyMap().containsValue(searchTaskVacancy)) {
                int lastKey = getVacancyMap().lastKey();
                getVacancyMap().put(lastKey + 1, searchTaskVacancy);
                addVacancy = true;
            }
        }
    }

    public void updatesVacancy(Vacancy vacancy) {
        SearchTaskVacancy searchTaskVacancy = new SearchTaskVacancy(vacancy);
        if (getVacancyMap().containsValue(searchTaskVacancy)) {
            int key = keyDuplicateVacancy(vacancy);
            if(key!=0){
                deleteDuplicate = true;
                getVacancyMap().replace(getVacancyUpdateId(), searchTaskVacancy);
            }
        } getVacancyMap().replace(getVacancyUpdateId(), searchTaskVacancy);
    }


    private int keyDuplicateVacancy(Vacancy vacancy) {
        AtomicReference<Integer> i = new AtomicReference<>(0);
        getVacancyMap().forEach((key, value) -> {
            if (value.getVacancy().equals(vacancy)) {
                i.set(key);
            }
        });
        return i.get();
    }

    public void addHistory(String text) {
        if (historyCommand.size() == 50) {
            Map<Integer, String> newHistory = new LinkedHashMap<>();
            for (int i = 10; i < getHistoryCommand().size(); i++) {
                newHistory.put(newHistory.size() + 1, getHistoryCommand().get(i + 1));
            }
            historyCommand = new LinkedHashMap<>(newHistory);
        }
        historyCommand.put(historyCommand.size() + 1, text);
    }

    public String lastCommand() {
        StringBuilder builder = new StringBuilder();
        getHistoryCommand().forEach((k, v) -> {
            if (k.equals(getHistoryCommand().size())) {
                builder.append(v);
            }
        });
        return builder.toString();
    }

    public String listOfAllVacancies() {
        TreeMap<Integer, SearchTaskVacancy> resultMap = getVacancyMap();
        StringBuilder builder = new StringBuilder();
        resultMap.forEach((key, value) ->
                builder.append(key).append(" : ").append(value.toString()).append("\n"));
        return builder.toString();
    }

    @Override
    public String toString() {
        return "VkUser{" +
                "id=" + id +
                ", vacancyMap=" + vacancyMap +
                ", historyCommand=" + historyCommand +
                '}';
    }
}
