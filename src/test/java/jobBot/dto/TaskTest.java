package jobBot.dto;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TaskTest {
    @Mock
    private SearchTaskVacancy searchTaskVacancyMock;
    private Task task;
    private final Long peerId = 123456789L;

    @BeforeEach
    void setUp() {
        task = new Task(peerId, searchTaskVacancyMock);

    }

    @AfterEach
    void tearDown() {
        task = null;
    }

    @Test
    @DisplayName("Test get")
    public void testTaskChecks() {
        assertThat(task == null).isFalse();
        assertThat(task.equals(new Object())).isFalse();
    }


    @Test
    @DisplayName("Test get peerId")
    public void testGetPeerId() {
        assertThat(task.getPeerId()).isEqualTo(123456789L);
    }

    @Test
    @DisplayName("Test get SearchTaskVacancy")
    public void testGetSearchTaskVacancy() {
        assertThat(task.getSearchTaskVacancy())
                .isEqualTo(searchTaskVacancyMock);
    }

    @Test
    @DisplayName("Test get idTask")
    public void testGetIdTask() {
        assertThat(task.getIdTask()).isNull();
    }

    @Test
    @DisplayName("Test get lastApiRequestAt")
    public void testGetLastApiRequestAt() {
        assertThat(task.getLastApiRequestAt()).isNull();
    }

    @Test
    @DisplayName("Test get lastSendingMessages")
    public void testGetLastSendingMessages() {
        assertThat(task.getLastSendingMessages()).isNull();
    }

    @Test
    @DisplayName("Test get vacancyResponseList")
    public void testGetVacancyResponseList() {
        List<VacancyResponse> list = task.getVacancyResponseList();
        assertThat(list).isNotNull();
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("Test equals two tasks with the same content")
    public void testEqualTwoTasksWithSameContent() {
        Task task2 = new Task(peerId, searchTaskVacancyMock);
        assertThat(task).isEqualTo(task2);
    }

    @Test
    @DisplayName("Test hashCode two tasks with the same content")
    public void testHashCodeTwoTasksWithSameContent() {
        Task task2 = new Task(peerId, searchTaskVacancyMock);
        assertThat(task.hashCode()).isEqualTo(task2.hashCode());
    }

    @Test
    @DisplayName("Test equals two tasks with modified time")
    public void testEqualTwoTasksWithModifiedTime() {
        Task task2 = new Task(peerId, searchTaskVacancyMock);
        LocalDateTime time = LocalDateTime.now();
        task.setLastSendingMessages(time);
        assertThat(task).isEqualTo(task2);
        assertThat(task.getLastSendingMessages()).isEqualTo(time);
        assertThat(task2.getLastSendingMessages()).isNotEqualTo(time);
    }

    @Test
    @DisplayName("Test hashCode two tasks with modified time")
    public void testHashCodeTwoTasksWithModifiedTime() {
        Task task2 = new Task(peerId, searchTaskVacancyMock);
        LocalDateTime time = LocalDateTime.now();
        task.setLastApiRequestAt(time);
        assertThat(task.hashCode()).isEqualTo(task2.hashCode());
        assertThat(task.getLastApiRequestAt()).isEqualTo(time);
        assertThat(task2.getLastApiRequestAt()).isNotEqualTo(time);
    }

    @Test
    @DisplayName("Test equals two tasks with different peerId")
    public void testNotEqualTwoTasksWithDifferentPeerId() {
        Long peerId2 = 9999L;
        Task task3 = new Task(peerId2, searchTaskVacancyMock);
        assertThat(task).isNotEqualTo(task3);
    }

    @Test
    @DisplayName("Test hashCode two tasks with different peerId")
    public void testNotHashCodeTwoTasksWithDifferentPeerId() {
        Long peerId2 = 9999L;
        Task task3 = new Task(peerId2, searchTaskVacancyMock);
        assertThat(task.hashCode()).isNotEqualTo(task3.hashCode());
    }

    @Test
    @DisplayName("Test equals two tasks with different searchTaskVacancy")
    public void testNotEqualTwoTasksWithDifferentSearchTaskVacancy() {
        SearchTaskVacancy search = new SearchTaskVacancy(new Vacancy());
        Task task4 = new Task(peerId, search);
        assertThat(task).isNotEqualTo(task4);
    }

    @Test
    @DisplayName("Test hashCode two tasks with different searchTaskVacancy ")
    public void testNotHashCodeTwoTasksWithDifferentSearchTaskVacancy() {
        SearchTaskVacancy search = new SearchTaskVacancy(new Vacancy());
        Task task4 = new Task(peerId, search);
        assertThat(task.hashCode()).isNotEqualTo(task4.hashCode());
    }

    @Test
    @DisplayName("Test setters task")
    void testSettersTask() {
        Long newId = 555L;
        LocalDateTime now = LocalDateTime.now();
        List<VacancyResponse> newList = new ArrayList<>();
        newList.add(new VacancyResponse());

        task.setIdTask(newId);
        task.setLastApiRequestAt(now);
        task.setLastSendingMessages(now.plusHours(1));
        task.setVacancyResponseList(newList);

        assertThat(task.getIdTask()).isEqualTo(newId);
        assertThat(task.getLastApiRequestAt()).isEqualTo(now);
        assertThat(task.getLastSendingMessages()).isEqualTo(now.plusHours(1));
        assertThat(task.getVacancyResponseList().size()).isEqualTo(1);
        assertThat(task.getVacancyResponseList()).isEqualTo(newList);
    }


    @Test
    @DisplayName("Test get vacancyResponseList with two task after adding")
    public void testGetVacancyResponseListWithTwoTask() {

        Task t2 = new Task(2L, searchTaskVacancyMock);
        task.getVacancyResponseList().add(new VacancyResponse());

        assertThat(task.getVacancyResponseList().size()).isEqualTo(1);
        assertThat(t2.getVacancyResponseList()).isEmpty();
    }

    @Test
    @DisplayName("Test toString task")
    public void toString_shouldContainExpectedFields() {
        Long idTask = 1L;
        task.setIdTask(idTask);
        String str = task.toString();

        assertThat(str).contains("idTask=" + idTask);
        assertThat(str).contains("peerId=" + peerId);
        assertThat(str).contains("searchTaskVacancy=");
    }
}

