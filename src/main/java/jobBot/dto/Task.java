package jobBot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class Task {
    private Long idTask;
    private Long peerId;
    private SearchTaskVacancy searchTaskVacancy;
    private LocalDateTime lastApiRequestAt;
    private LocalDateTime lastSendingMessages;
    private List<VacancyResponse> vacancyResponseList;

    public Task(Long peerId, SearchTaskVacancy searchTaskVacancy) {
        this.peerId = peerId;
        this.searchTaskVacancy = searchTaskVacancy;
        lastApiRequestAt = null;
        lastSendingMessages = null;
        vacancyResponseList = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Task task)) return false;
        return Objects.equals(peerId, task.peerId) && Objects.equals(searchTaskVacancy, task.searchTaskVacancy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerId, searchTaskVacancy);
    }

    @Override
    public String toString() {
        return "Task{" +
                "idTask=" + idTask +
                ", peerId=" + peerId +
                ", searchTaskVacancy=" + searchTaskVacancy +
                '}';
    }
}
