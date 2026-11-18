package jobBot.dto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class SearchTaskVacancyTest {
    private SearchTaskVacancy searchTask;
    private Vacancy vacancy;

    @BeforeEach
    public void setUp() {
        vacancy = new Vacancy();
        vacancy.setRegion("78");
        searchTask = new SearchTaskVacancy(vacancy);
    }

    @AfterEach
    public void tearDown() {
        searchTask = null;
    }
    @Test
    void testEqualsNotNullWhenCreatedSearchTack() {
        assertThat(searchTask == null).isFalse();
        assertThat(searchTask.equals(new Object())).isFalse();
    }

    @Test
    public void testConstructorWhenCreatedSearchTask() {
        assertThat(searchTask.getVacancy()).isEqualTo(vacancy);
        assertThat(searchTask.getLocalDateTime()).isNotNull();
        assertThat(searchTask.getLocalDateTime()).isCloseTo(
                LocalDateTime.now(), within(1, ChronoUnit.MINUTES));
    }

    @Test
    public void testGetLocalDateTimeWhenUpdateVacancyInSearchTask() throws InterruptedException {
        LocalDateTime searchTaskLocalDateTime = searchTask.getLocalDateTime();

        Vacancy updatedVacancy = new Vacancy();
        updatedVacancy.setRegion("17");
        Thread.sleep(5000);

        searchTask.setVacancy(updatedVacancy);

        assertThat(searchTask.getVacancy()).isEqualTo(updatedVacancy);
        assertThat(searchTask.getLocalDateTime())
                .isNotEqualTo(searchTaskLocalDateTime)
               .isCloseTo(LocalDateTime.now(),
                        within(2, ChronoUnit.SECONDS));
    }
    @Test
    public void testVacancyWhenUpdateVacancyInSearchTask(){
        Vacancy updateVacancy = new Vacancy();
        updateVacancy.setRegion("54");
        searchTask.setVacancy(updateVacancy);
        assertThat(searchTask.getVacancy().getRegion()).isEqualTo("54");
    }

    @Test
    void testEqualsWhenTwoIdenticalSearchTask() {
        searchTask.getVacancy().setKeyword("Java");
        Vacancy vacancy1 = new Vacancy();
        vacancy1.setRegion("78");
        vacancy1.setKeyword("Java");
        SearchTaskVacancy searchTask2 = new SearchTaskVacancy(vacancy1);

        assertThat(searchTask).isEqualTo(searchTask2);
    }
    @Test
    void testHashCodeWhenTwoIdenticalSearchTask() {
        searchTask.getVacancy().setKeyword("Java");
        Vacancy vacancy1 = new Vacancy();
        vacancy1.setRegion("78");
        vacancy1.setKeyword("Java");
        SearchTaskVacancy searchTask2 = new SearchTaskVacancy(vacancy1);

        assertThat(searchTask.hashCode()).isEqualTo(searchTask2.hashCode());}

    @Test
    void testEqualsWhenTwoDifferentSearchTask() {
        Vacancy vacancy2 = new Vacancy();
        vacancy2.setRegion("17");
        SearchTaskVacancy searchTask2 = new SearchTaskVacancy(vacancy2);

        assertThat(searchTask).isNotEqualTo(searchTask2);
    }

    @Test
    void testEqualsTrueWhenTwoIdenticalSearchTask() {
        searchTask.getVacancy().setZp("100000");
        Vacancy vacancy1 = new Vacancy();
        vacancy1.setZp("100000");
        vacancy1.setRegion("78");

        SearchTaskVacancy searchTaskVacancy = new SearchTaskVacancy(vacancy1);
        assertThat(searchTask.equals(searchTaskVacancy)).isTrue();
        assertThat(searchTask.hashCode()).isEqualTo(searchTaskVacancy.hashCode());
    }


    @Test
    void toString_shouldIncludeVacancyAndLocalDateTime() {

        String str = searchTask.toString();
        assertThat(str).contains("vacancy=");
        assertThat(str).contains(searchTask.toString());
        assertThat(str).contains("localDateTime=");
    }
}

