package jobBot.dto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class VkUserTest {
    private VkUser user;
    private Vacancy vacancyMoscow;
    private Vacancy vacancySPB;
    private Vacancy vacancyDuplicate;

    @BeforeEach
    void setUp() {
        user = new VkUser(1234567L, "Ivan", "Petrov");

            vacancyMoscow = new Vacancy();
            vacancyMoscow.setRegion("77");
            vacancyMoscow.setKeyword("Java");

            vacancySPB = new Vacancy();
            vacancySPB.setRegion("78");
            vacancySPB.setKeyword("Kotlin");

            vacancyDuplicate = new Vacancy();
            vacancyDuplicate.setRegion("77");
            vacancyDuplicate.setKeyword("Java");
        }


    @AfterEach
    void tearDown() {
        user = null;
    }


    @Test
    void testGetVacancyNewWhenUserCreated() {
        assertThat(user.getVacancyNew()).isNotNull();
        assertThat(user.getVacancyNew().getRegion()).isEmpty();
    }

    @Test
    void testGetVacancyMapWhenUserCreated() {
        assertThat(user.getVacancyMap()).isNotNull();
        assertThat(user.getVacancyMap()).isEmpty();
        assertThat(user.getVacancyMap()).isInstanceOf(TreeMap.class);
    }

    @Test
    void testGetHistoryCommandWhenUserCreated() {
        assertThat(user.getHistoryCommand()).isNotNull();
        assertThat(user.getHistoryCommand()).isEmpty();
        assertThat(user.getHistoryCommand()).isInstanceOf(LinkedHashMap.class);
    }

    @Test
    void testIsAddVacancyWhenUserCreated() {
        assertThat(user.isAddVacancy()).isFalse();
    }

    @Test
    void testGetVacancyUpdateIdWhenUserCreated() {
        assertThat(user.getVacancyUpdateId()).isEqualTo(0);
    }
    @Test
    public void testAddVacancyWhenFirstWasAddedVacancy(){
        user.addVacancy(vacancyMoscow);
        assertThat(user.getVacancyMap().isEmpty()).isFalse();
        assertThat(user.getVacancyMap()).hasSize(1);
        assertThat(user.getVacancyMap().get(1).getVacancy())
                .isEqualTo(vacancyMoscow);
    }

    @Test
    public void testEqualsVacancyAddVacancyWhenFirstWasAddedVacancy() {
        user.addVacancy(vacancyMoscow);
        assertThat(user.getVacancyMap().get(1).getVacancy().getRegion())
                .isEqualTo("77");
    }

    @Test
    public void testAddVacancyTrueVacancyAddVacancyWhenFirstWasAddedVacancy(){
        user.addVacancy(vacancyMoscow);
        assertThat(user.isAddVacancy()).isTrue();
    }

    @Test
    public void testAddVacancyWhenAddedTwoDifferentVacancy(){
        user.addVacancy(vacancyMoscow);
        user.addVacancy(vacancySPB);
        TreeMap<Integer, SearchTaskVacancy> map = user.getVacancyMap();

        assertThat(map).hasSize(2);
        assertThat(map.keySet()).containsExactly(1, 2);
        assertThat(map.get(1).getVacancy().getRegion())
                .isEqualTo("77");
        assertThat(map.get(2).getVacancy().getKeyword())
                .isEqualTo("Kotlin");
        assertThat(user.isAddVacancy()).isTrue();
    }

    @Test
    public void testAddVacancyWhenAddedVacancyDuplicate() throws InterruptedException {
        user.addVacancy(vacancyMoscow);
        user.addVacancy(vacancySPB);
        LocalDateTime timeVacancy1 = user.getVacancyMap().get(1).getLocalDateTime();
        Vacancy vacancy3 = new Vacancy();
        vacancy3.setRegion("77");
        vacancy3.setKeyword("Java");
        assertThat(vacancyMoscow).isEqualTo(vacancy3);
        Thread.sleep(5000);
        user.addVacancy(vacancy3);
        LocalDateTime timeNow = LocalDateTime.now();
        TreeMap<Integer, SearchTaskVacancy> map = user.getVacancyMap();
        assertThat(map).hasSize(2);
        assertThat(timeVacancy1).isNotEqualTo(timeNow);
    }

    @Test
    void testUpdatesVacancyWhenDuplicateFoundAndReplacedOneSearchVacancy() {

        user.addVacancy(vacancyMoscow);
        user.setVacancyUpdateId(1);
        user.updatesVacancy(vacancyDuplicate);

        TreeMap<Integer, SearchTaskVacancy> map = user.getVacancyMap();
        assertThat(map).hasSize(1);
        assertThat(map.get(1).getVacancy().getRegion()).isEqualTo("77");
        assertThat(user.isDeleteDuplicate()).isTrue();
    }
    @Test
    void testUpdatesVacancyWhenDuplicateFoundAndReplacedTwoSearchVacancy() {
        user.addVacancy(vacancySPB);
        user.addVacancy(vacancyMoscow);
        user.setVacancyUpdateId(1);
        user.updatesVacancy(vacancyDuplicate);
        TreeMap<Integer, SearchTaskVacancy> map = user.getVacancyMap();
        assertThat(map).hasSize(2);
        assertThat(map.get(2).getVacancy().getRegion()).isEqualTo("77");
        assertThat(user.isDeleteDuplicate()).isTrue();
    }

    @Test
    void testUpdatesVacancyWhenNoDuplicateButReplaceById() {
        user.addVacancy(vacancyMoscow);
        user.addVacancy(vacancySPB);

        user.setVacancyUpdateId(2);

        Vacancy updatedVacancy = new Vacancy();
        updatedVacancy.setRegion("54");
        updatedVacancy.setKeyword("Scala");

        user.updatesVacancy(updatedVacancy);

        TreeMap<Integer, SearchTaskVacancy> map = user.getVacancyMap();

        assertThat(map).hasSize(2);
        assertThat(map.get(2).getVacancy().getRegion()).isEqualTo("54");
        assertThat(user.isDeleteDuplicate()).isFalse();
    }

    @Test
    void testAddHistoryWhenAddedLessThanLimit() {
        user.addHistory("1");
        user.addHistory("2");
        user.addHistory("3");

        Map<Integer, String> history = user.getHistoryCommand();
        assertThat(history).hasSize(3);
        assertThat(history.get(3)).isEqualTo("3");
    }

    @Test
    void testAddHistoryWhenReachedLimitAndTrimmed() {
        for (int i = 1; i <= 50; i++) {
            user.addHistory("cmd_" + i);
        }
        assertThat(user.getHistoryCommand()).hasSize(50);
        user.addHistory("cmd_51");

        Map<Integer, String> history = user.getHistoryCommand();
        assertThat(history).hasSize(41);
        assertThat(history.get(1)).isEqualTo("cmd_11");
        assertThat(history.get(41)).isEqualTo("cmd_51");
    }

    @Test
    void testLastCommandWhenHistoryNotEmpty() {
        user.addHistory("first");
        user.addHistory("middle");
        user.addHistory("LAST_COMMAND_TEXT");
        String last = user.lastCommand();
        assertThat(last).isEqualTo("LAST_COMMAND_TEXT");
    }

    @Test
    void testLastCommandWhenHistoryEmpty() {
        String last = user.lastCommand();
        assertThat(last).isEmpty();
    }


    @Test
    void testListOfAllVacanciesWhenMapNotEmpty() {
        user.addVacancy(vacancyMoscow);
        user.addVacancy(vacancySPB);

        String list = user.listOfAllVacancies();

        assertThat(list).contains("1 : ");
        assertThat(list).contains("2 : ");
        assertThat(list).contains("77");
        assertThat(list).contains("78");
        assertThat(list).endsWith("\n");
    }

    @Test
    void testListOfAllVacanciesWhenMapEmpty() {
        String list = user.listOfAllVacancies();
        assertThat(list).isEmpty();
    }

    @Test
    void testToStringWhenUserHasData() {
        user.addVacancy(vacancyMoscow);
        user.addHistory("test_cmd");

        String str = user.toString();

        assertThat(str).contains("VkUser{");
        assertThat(str).contains("id=1234567");
        assertThat(str).contains("vacancyMap=");
        assertThat(str).contains("historyCommand=");
        assertThat(str).endsWith("}");
    }
}