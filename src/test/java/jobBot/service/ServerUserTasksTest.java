package jobBot.service;

import com.vk.api.sdk.actions.Users;
import com.vk.api.sdk.actions.Users.*;
import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.City;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import com.vk.api.sdk.queries.messages.MessagesSendQueryWithUserIds;
import com.vk.api.sdk.queries.users.UsersGetQuery;
import jobBot.command.commands.RegionCommandType;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Task;
import jobBot.dto.Vacancy;
import jobBot.dto.VkUser;
import jobBot.repository.UserRepository;
import jobBot.repository.task.TasksRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerUserTasksTest {

    @Mock
    private VkApiClient vk;
    @Mock
    private Users usersMock;
    @Mock
    private UsersGetQuery usersGetQuery;
    @Mock
    private Messages messagesMock;
    @Mock
    private GroupActor actor;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private RegionCommandType regionCommandType;
    @Mock
    private ListVacancyType listVacancyType;

    private ServerUserTasks sut;
    private final Long peerId = 123L;

    void setUpWhen() {
        MessagesSendQueryWithUserIds sendQueryMock = mock(MessagesSendQueryWithUserIds.class);
        when(sendQueryMock.message(anyString())).thenReturn(sendQueryMock);
        when(sendQueryMock.keyboard(any())).thenReturn(sendQueryMock);
        when(sendQueryMock.peerId(anyLong())).thenReturn(sendQueryMock);

        when(sendQueryMock.dontParseLinks(anyBoolean())).thenReturn(sendQueryMock);
        when(sendQueryMock.randomId(any())).thenReturn(sendQueryMock);
        when(vk.messages()).thenReturn(messagesMock);
        when(messagesMock.sendUserIds(eq(actor))).thenReturn(sendQueryMock);
    }

    void setUp() {
        String text = "/start";
        sut = new ServerUserTasks(vk, actor, text, peerId, userRepository,
                regionCommandType, listVacancyType, tasksRepository);
    }

    @AfterEach
    void tearDown() {
        sut = null;
    }

    @Test
    void testProcessWhenNewUserCreatesTask() throws ClientException, ApiException {
        setUpWhen();
        setUp();
        when(vk.users()).thenReturn(usersMock);
        when(usersMock.get(eq(actor))).thenReturn(usersGetQuery);
        when(usersGetQuery.userIds(any(String.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.fields(any(Fields.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.lang(any(Lang.class))).thenReturn(usersGetQuery);
        when(userRepository.exists(peerId)).thenReturn(false);

        GetResponse mockResponse = mock(GetResponse.class);
        City mockCity = mock(City.class);

        when(mockResponse.getFirstName()).thenReturn("Ivan");
        when(mockResponse.getLastName()).thenReturn("Petrov");
        when(mockResponse.getCity()).thenReturn(mockCity);
        when(mockCity.getTitle()).thenReturn("Moscow");
        when(mockCity.getId()).thenReturn(1);

        List<GetResponse> vkResult = List.of(mockResponse);
        when(usersGetQuery.execute()).thenReturn(vkResult);

        VkUser user = new VkUser(peerId, "Ivan", "Petrov");
        Map<Integer, SearchTaskVacancy> vacancies = new TreeMap<>();
        vacancies.put(1, new SearchTaskVacancy(new Vacancy()));
        user.setVacancyMap((TreeMap<Integer, SearchTaskVacancy>) vacancies);

        when(userRepository.findById(peerId)).thenReturn(Optional.of(user));
        when(tasksRepository.findTaskForPeerId(peerId)).thenReturn(new HashMap<>());
        sut.process();
        verify(userRepository).exists(peerId);
        verify(userRepository, times(2)).save(any(VkUser.class));

        verify(listVacancyType).setUserVk(any(VkUser.class));

        verify(tasksRepository).findTaskForPeerId(anyLong());
        verify(tasksRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testProcessWhenExistingUserNoVKCall() {
        setUpWhen();
        setUp();
        when(userRepository.exists(peerId)).thenReturn(true);
        VkUser existingUser = new VkUser(peerId, "Anna", "Smirnova");
        Map<Integer, SearchTaskVacancy> vacancies = new TreeMap<>();
        vacancies.put(2, new SearchTaskVacancy(new Vacancy()));
        existingUser.setVacancyMap((TreeMap<Integer, SearchTaskVacancy>) vacancies);
        when(userRepository.findById(peerId)).thenReturn(Optional.of(existingUser));
        when(tasksRepository.findTaskForPeerId(peerId)).thenReturn(new HashMap<>());
        sut.process();

        verify(vk, never()).users();
        verify(userRepository, times(1)).save(any());
        verify(tasksRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testProcessWhenUserWithDoneCommandAndDeletesUserAndTasks() {
        setUpWhen();
        setUp();
        when(userRepository.exists(peerId)).thenReturn(true);

        VkUser existingUser = mock(VkUser.class);

        when(existingUser.getId()).thenReturn(peerId);
        when(existingUser.lastCommand()).thenReturn("/done");
        Map<Integer, SearchTaskVacancy> emptyVacancies = new TreeMap<>();
        existingUser.setVacancyMap((TreeMap<Integer, SearchTaskVacancy>) emptyVacancies);

        when(userRepository.findById(peerId)).thenReturn(Optional.of(existingUser));
        sut.process();
        verify(userRepository).delete(peerId);
        verify(tasksRepository).deleteByPeerId(peerId);
    }

    @Test
    void testProcessWhenNewUserNoVacanciesAddNoTasksCreated() throws ClientException, ApiException {
        setUpWhen();
        setUp();
        when(vk.users()).thenReturn(usersMock);
        when(usersMock.get(eq(actor))).thenReturn(usersGetQuery);
        when(usersGetQuery.userIds(any(String.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.fields(any(Fields.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.lang(any(Lang.class))).thenReturn(usersGetQuery);
        when(userRepository.exists(peerId)).thenReturn(false);

        GetResponse mockResponse = mock(GetResponse.class);
        City mockCity = mock(City.class);

        when(mockResponse.getFirstName()).thenReturn("Olga");
        when(mockResponse.getLastName()).thenReturn("Volkova");
        when(mockResponse.getCity()).thenReturn(mockCity);
        when(mockCity.getTitle()).thenReturn("Kazan");
        when(mockCity.getId()).thenReturn(2);

        List<GetResponse> vkResult = List.of(mockResponse);
        when(usersGetQuery.execute()).thenReturn(vkResult);

        VkUser user = new VkUser(peerId, "Olga", "Volkova");
        user.setVacancyMap(new TreeMap<>());
        when(userRepository.findById(peerId)).thenReturn(Optional.of(user));
        sut.process();

        verify(userRepository).exists(peerId);
        verify(userRepository, times(2)).save(any(VkUser.class));
        verify(tasksRepository, never()).save(any(Task.class));
    }

    @Test
    void testProcessWhenVkExceptionAddThrowsRuntimeException() {
        setUp();
        when(vk.users()).thenReturn(usersMock);
        when(usersMock.get(eq(actor))).thenReturn(usersGetQuery);
        when(usersGetQuery.userIds(any(String.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.fields(any(Fields.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.lang(any(Lang.class))).thenReturn(usersGetQuery);
        when(userRepository.exists(peerId)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> sut.process());
    }

    @Test
    void testProcessWhenUpdateTasksAddRemoveOldAndAddNew() {
        setUpWhen();
        setUp();

        when(userRepository.exists(peerId)).thenReturn(true);

        VkUser existingUser = new VkUser(peerId, "Maria", "Ivanova");
        Map<Integer, SearchTaskVacancy> userVacancies = new TreeMap<>();
        Vacancy mockVacancyOld = mock(Vacancy.class);
        Vacancy mockVacancyNew = mock(Vacancy.class);

        SearchTaskVacancy stwOld = mock(SearchTaskVacancy.class);
        SearchTaskVacancy stwNew = mock(SearchTaskVacancy.class);

        when(stwOld.getVacancy()).thenReturn(mockVacancyOld);
        when(stwNew.getVacancy()).thenReturn(mockVacancyNew);
        userVacancies.put(3, stwNew);
        existingUser.setVacancyMap((TreeMap<Integer, SearchTaskVacancy>) userVacancies);
        when(userRepository.findById(peerId)).thenReturn(Optional.of(existingUser));
        Map<Long, Task> existingTasks = new HashMap<>();

        Task oldTask = mock(Task.class);
        when(oldTask.getSearchTaskVacancy()).thenReturn(stwOld);
        existingTasks.put(100L, oldTask);
        when(tasksRepository.findTaskForPeerId(peerId)).thenReturn(existingTasks);
        sut.process();
        verify(tasksRepository, times(1)).delete(100L);
        verify(tasksRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testProcessWhenServiceBotSendsMessage() throws ClientException, ApiException {
        MessagesSendQueryWithUserIds sendQueryMock = mock(MessagesSendQueryWithUserIds.class);
        when(sendQueryMock.message(anyString())).thenReturn(sendQueryMock);
        when(sendQueryMock.keyboard(any())).thenReturn(sendQueryMock);
        when(sendQueryMock.peerId(anyLong())).thenReturn(sendQueryMock);
        when(sendQueryMock.dontParseLinks(anyBoolean())).thenReturn(sendQueryMock);
        when(sendQueryMock.randomId(any())).thenReturn(sendQueryMock);
        when(vk.messages()).thenReturn(messagesMock);
        when(messagesMock.sendUserIds(eq(actor))).thenReturn(sendQueryMock);

        GetResponse mockResponse = mock(GetResponse.class);
        City mockCity = mock(City.class);
        when(mockResponse.getFirstName()).thenReturn("Dmitry");
        when(mockResponse.getLastName()).thenReturn("Sokolov");
        when(mockResponse.getCity()).thenReturn(mockCity);
        when(mockCity.getTitle()).thenReturn("SPb");
        when(mockCity.getId()).thenReturn(3);

        List<GetResponse> vkResult = List.of(mockResponse);
        when(vk.users()).thenReturn(usersMock);
        when(usersMock.get(eq(actor))).thenReturn(usersGetQuery);
        when(usersGetQuery.userIds(anyString())).thenReturn(usersGetQuery);
        when(usersGetQuery.fields(any(Fields.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.lang(any(Lang.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.execute()).thenReturn(vkResult);

        setUp();
        when(userRepository.exists(peerId)).thenReturn(false);
        VkUser savedUser = new VkUser(peerId, "Dmitry", "Sokolov");
        savedUser.setVacancyMap(new TreeMap<>());
        when(userRepository.findById(peerId)).thenReturn(Optional.of(savedUser));
        sut.process();
        verify(sendQueryMock, times(1)).message(anyString());
        verify(sendQueryMock, times(1)).peerId(eq(peerId));
        verify(sendQueryMock, times(1)).keyboard(any());
        verify(userRepository, times(2)).save(any(VkUser.class));
    }

}

