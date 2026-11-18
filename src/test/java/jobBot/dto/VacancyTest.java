package jobBot.dto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VacancyTest {
    Vacancy vacancy;
    @BeforeEach
    void setUp() {vacancy = new Vacancy();
    }

    @AfterEach
    void tearDown() {
vacancy = null;
    }
    @Test
    @DisplayName("Test notNull vacancy")
    public void testNotNullWhenItWasCreated(){
        assertThat(vacancy == null).isFalse();
        assertThat(vacancy.equals(new Object())).isFalse();
    }
    @Test
    @DisplayName("Test constructor when creating")
    public void testConstructorWhenCreating() {
        assertThat(vacancy.getRegion()).isEqualTo("");
        assertThat(vacancy.getExperience()).isEqualTo("");
        assertThat(vacancy.getZp()).isEqualTo("");
        assertThat(vacancy.getKeyword()).isEqualTo("");
    }



    @Test
    @DisplayName("Test equals when identical vacancies")
    void TestEqualsWhenIdenticalVacancies() {
        vacancy.setRegion("78");
        vacancy.setExperience("до 1 года");
        vacancy.setZp("80000");
        vacancy.setKeyword("Spring");

        Vacancy vacancy2 = new Vacancy();
        vacancy2.setRegion("78");
        vacancy2.setExperience("до 1 года");
        vacancy2.setZp("80000");
        vacancy2.setKeyword("Spring");

        assertThat(vacancy).isEqualTo(vacancy2);

    }
    @Test
    @DisplayName("Test hashCode when identical vacancies")
    void TestHashCodeWhenIdenticalVacancies() {
        vacancy.setRegion("78");
        vacancy.setExperience("до 1 года");
        vacancy.setZp("80000");
        vacancy.setKeyword("Spring");

        Vacancy vacancy2 = new Vacancy();
        vacancy2.setRegion("78");
        vacancy2.setExperience("до 1 года");
        vacancy2.setZp("80000");
        vacancy2.setKeyword("Spring");
        assertThat(vacancy.hashCode()).isEqualTo(vacancy2.hashCode());}

    @Test
    @DisplayName("Test equals when one parameter is different")
    public void testEqualsWhenOneParameterIsDifferent() {

        vacancy.setRegion("77");
        vacancy.setExperience("1-3 года");
        vacancy.setZp("200000");
        vacancy.setKeyword("Kotlin");

        Vacancy v2 = new Vacancy();
        v2.setRegion("14");
        v2.setExperience("1-3 года");
        v2.setZp("200000");
        v2.setKeyword("Kotlin");

        assertThat(vacancy).isNotEqualTo(v2);
    }
    @Test
    @DisplayName("Test equals when two parameter is different")
    public void testEqualsWhenTw0ParameterIsDifferent() {

        vacancy.setRegion("77");
        vacancy.setExperience("1-3 года");
        vacancy.setZp("200000");
        vacancy.setKeyword("Kotlin");

        Vacancy v2 = new Vacancy();
        v2.setRegion("14");
        v2.setExperience("до года");
        v2.setZp("200000");
        v2.setKeyword("Kotlin");

        assertThat(vacancy).isNotEqualTo(v2);
    }
    @Test
    @DisplayName("Test equals when tree parameter is different")
    public void testEqualsWhenTreeParameterIsDifferent() {

        vacancy.setRegion("77");
        vacancy.setExperience("1-3 года");
        vacancy.setZp("200000");
        vacancy.setKeyword("Kotlin");

        Vacancy v2 = new Vacancy();
        v2.setRegion("14");
        v2.setExperience("до года");
        v2.setZp("50000");
        v2.setKeyword("Kotlin");

        assertThat(vacancy).isNotEqualTo(v2);
    }



    @Test
    @DisplayName("Test toString when was created")
    public void testToStringWhenWasCreated() {
        String result = vacancy.toString();

        assertThat(result).contains("Region - не указано");
        assertThat(result).contains("Experience - не указано");
        assertThat(result).contains("Salary - не указано");
        assertThat(result).contains("Keyword - не указано");
    }

    @Test
    @DisplayName("Test toString when the data was entered")
    void testToStringWhenTheDataWasEntered() {
        vacancy.setRegion("85");
        vacancy.setExperience("1–3 года");
        vacancy.setZp("120000");
        vacancy.setKeyword("Backend");

        String result = vacancy.toString();

        assertThat(result).contains("Region - 85");
        assertThat(result).contains("Experience - 1–3 года");
        assertThat(result).contains("Salary - 120000");
        assertThat(result).contains("Keyword - Backend");
    }
}

