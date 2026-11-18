package jobBot.repository.task;

import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Task;
import jobBot.dto.Vacancy;
import jobBot.repository.vacancyResponse.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TasksRepositoryTest {

    @InjectMocks
    private TasksRepository tasksRepository;

    @Mock
    private TaskRepository taskRepository;

    private Vacancy vacancy1;
    private SearchTaskVacancy searchTask1;
    private Task taskWithoutId;
    private Task taskWithId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        vacancy1 = new Vacancy();
        vacancy1.setRegion("77");
        vacancy1.setKeyword("Java");

        searchTask1 = new SearchTaskVacancy(vacancy1);

        taskWithoutId = new Task(100L, searchTask1);

        taskWithId = new Task(100L, searchTask1);
        taskWithId.setIdTask(5L);
    }

    @Test
    void testSaveWhenTaskDoesNotHaveId() {
        tasksRepository.save(taskWithoutId);
        assertThat(taskWithoutId.getIdTask()).isNotNull();
        assertThat(taskWithoutId.getIdTask()).isEqualTo(0L);
        Optional<Task> inMemory = tasksRepository.findById(0L);
        assertThat(inMemory).isPresent();
        assertThat(inMemory.get().getIdTask()).isEqualTo(0L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testSaveWhenTaskAlreadyHasId() {
        Task task = new Task(200L, searchTask1);
        task.setIdTask(99L);
        tasksRepository.save(task);
        assertThat(task.getIdTask()).isEqualTo(99L);
        Optional<Task> inMemory = tasksRepository.findById(99L);
        assertThat(inMemory).isPresent();
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testSaveWhenMultipleTasksSavedAndIdsAreUnique() {
        Task t1 = new Task(300L, new SearchTaskVacancy(new Vacancy()));
        Task t2 = new Task(300L, new SearchTaskVacancy(new Vacancy()));
        tasksRepository.save(t1);
        tasksRepository.save(t2);
        assertThat(t1.getIdTask()).isEqualTo(0L);
        assertThat(t2.getIdTask()).isEqualTo(1L);

        assertThat(tasksRepository.findById(0L)).isPresent();
        assertThat(tasksRepository.findById(1L)).isPresent();
    }

    @Test
    void testFindByIdWhenTaskExistsInMemory() {
        tasksRepository.save(taskWithId);

        Optional<Task> result = tasksRepository.findById(5L);

        assertThat(result).isPresent();
        assertThat(result.get().getIdTask()).isEqualTo(5L);
        assertThat(result.get().getPeerId()).isEqualTo(100L);
    }

    @Test
    void testFindByIdWhenTaskDoesNotExist() {
        Optional<Task> result = tasksRepository.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void testFindTaskForPeerIdWhenMultipleTasksBelongToSamePeer() {
        Task t1 = new Task(555L, new SearchTaskVacancy(new Vacancy()));
        t1.setIdTask(10L);
        Task t2 = new Task(555L, new SearchTaskVacancy(new Vacancy()));
        t2.setIdTask(11L);

        tasksRepository.save(t1);
        tasksRepository.save(t2);

        Map<Long, Task> result = tasksRepository.findTaskForPeerId(555L);
        assertThat(result).hasSize(2);
        assertThat(result.keySet()).containsExactly(10L, 11L);
        assertThat(result.values().stream().map(Task::getPeerId).distinct().count()).isEqualTo(1);
    }

    @Test
    void testFindTaskForPeerIdWhenNoTasksBelongToPeer() {
        Task t = new Task(999L, new SearchTaskVacancy(new Vacancy()));
        t.setIdTask(20L);
        tasksRepository.save(t);
        Map<Long, Task> result = tasksRepository.findTaskForPeerId(123L);
        assertThat(result).isEmpty();
    }
    @Test
    void testUpdateWhenTaskExistsAndDataChanged() {
        tasksRepository.save(taskWithId);
        Task updatedTask = new Task(100L, new SearchTaskVacancy(new Vacancy()));
        updatedTask.setIdTask(5L);
        updatedTask.setLastApiRequestAt(LocalDateTime.now());

        tasksRepository.update(5L, updatedTask);

        Optional<Task> found = tasksRepository.findById(5L);
        assertThat(found).isPresent();
        assertThat(found.get().getLastApiRequestAt()).isNotNull();
        verify(taskRepository, times(1)).delete(any(Task.class));
        verify(taskRepository, times(1)).save(updatedTask);
    }

    @Test
    void testUpdateWhenTaskIdIsNullThrowsException() {

        Task invalidTask = new Task(100L, new SearchTaskVacancy(new Vacancy()));
        invalidTask.setIdTask(null);
      assertThrows(IllegalArgumentException.class, () -> {
            tasksRepository.update(1L, invalidTask);
        });

        verify(taskRepository, never()).save(any());
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void testUpdateWhenTaskNotFoundThrowsException() {
        Task nonExistentTask = new Task(100L, new SearchTaskVacancy(new Vacancy()));
        nonExistentTask.setIdTask(999L);
     assertThrows(IllegalArgumentException.class, () -> {
            tasksRepository.update(999L, nonExistentTask);
        });
    }

    @Test
    void testGetTaskMapSortedWhenTasksHaveDifferentTimestamps() throws InterruptedException {

        Vacancy v1 = new Vacancy(); v1.setRegion("A");
        Vacancy v2 = new Vacancy(); v2.setRegion("B");
        Vacancy v3 = new Vacancy(); v3.setRegion("C");

        SearchTaskVacancy st1 = new SearchTaskVacancy(v1);
        st1.setVacancy(v1);
        Thread.sleep(10);
        SearchTaskVacancy st2 = new SearchTaskVacancy(v2);
       Thread.sleep(10);
        SearchTaskVacancy st3 = new SearchTaskVacancy(v3);

        Task t3 = new Task(1L, st3); t3.setIdTask(3L);
        Task t1 = new Task(1L, st1); t1.setIdTask(1L);
        Task t2 = new Task(1L, st2); t2.setIdTask(2L);

        tasksRepository.save(t1);
        tasksRepository.save(t2);
        tasksRepository.save(t3);

        Map<Long, Task> sortedMap = tasksRepository.getTaskMapSorted();

        List<Long> keys = new ArrayList<>(sortedMap.keySet());
        assertThat(keys).containsExactly(1L, 2L, 3L);
        assertThat(sortedMap).isInstanceOf(java.util.LinkedHashMap.class);
    }

    @Test
    void testGetTaskMapSortedWhenMapIsEmpty() {
        Map<Long, Task> sortedMap = tasksRepository.getTaskMapSorted();
        assertThat(sortedMap).isEmpty();
    }

    @Test
    void testDeleteWhenTaskExists() {

        tasksRepository.save(taskWithId);

        tasksRepository.delete(5L);

        assertThat(tasksRepository.findById(5L)).isEmpty();
        verify(taskRepository, times(1)).delete(taskWithId);
    }

    @Test
    void testDeleteWhenTaskDoesNotExist() {
        tasksRepository.delete(999L);
        verify(taskRepository, never()).delete(any());
        assertThat(tasksRepository.findById(999L)).isEmpty();
    }

    @Test
    void testDeleteByPeerIdWhenMultipleTasksExistForPeer() {
        Task t1 = new Task(777L, new SearchTaskVacancy(new Vacancy())); t1.setIdTask(100L);
        Task t2 = new Task(777L, new SearchTaskVacancy(new Vacancy())); t2.setIdTask(101L);
        Task t3 = new Task(888L, new SearchTaskVacancy(new Vacancy())); t3.setIdTask(102L); // Другой peer

        tasksRepository.save(t1);
        tasksRepository.save(t2);
        tasksRepository.save(t3);

        tasksRepository.deleteByPeerId(777L);
        assertThat(tasksRepository.findById(100L)).isEmpty();
        assertThat(tasksRepository.findById(101L)).isEmpty();
        assertThat(tasksRepository.findById(102L)).isPresent();
        verify(taskRepository, times(2)).delete(any(Task.class));
    }

    @Test
    void testDeleteByPeerIdWhenNoTasksExistForPeer() {
        Task t = new Task(999L, new SearchTaskVacancy(new Vacancy()));
        t.setIdTask(50L);
        tasksRepository.save(t);
        tasksRepository.deleteByPeerId(111L);
        assertThat(tasksRepository.findById(50L)).isPresent();
        verify(taskRepository, never()).delete(any());
    }
}