package jobBot.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class SearchTaskVacancy {
    private Vacancy vacancy;
    private LocalDateTime localDateTime;


    public SearchTaskVacancy(Vacancy vacancy) {
        this.vacancy = vacancy;
        localDateTime = LocalDateTime.now();
    }

    public void setVacancy(Vacancy vacancy) {
        this.vacancy = vacancy;
        localDateTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SearchTaskVacancy that)) return false;
        return Objects.equals(vacancy, that.vacancy);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(vacancy);
    }

    @Override
    public String toString() {
        return "vacancy=" + vacancy +
                ", localDateTime=" + localDateTime;
    }
}
