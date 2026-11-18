package jobBot.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.api.sdk.actions.Groups;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.actions.Users;
import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import com.vk.api.sdk.objects.users.responses.GetResponse;

import com.vk.api.sdk.objects.base.City;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.queries.groups.GroupsGetLongPollServerQuery;
import com.vk.api.sdk.queries.messages.MessagesSendQueryWithUserIds;
import com.vk.api.sdk.queries.users.UsersGetQuery;
import jobBot.command.commands.RegionCommandType;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.dto.VkUser;
import jobBot.repository.UserRepository;
import jobBot.repository.task.TasksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VkMainProcessorTest {
    @Mock
    private VkApiClient vk;
    @Mock
    private GroupActor actor;
    @Mock
    private RegionCommandType regionCommandType;
    @Mock
    private ListVacancyType listVacancyType;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TasksRepository tasksRepository;
    private ServerUserTasks serverUserTasks;
    private VkMainProcessor processor;

    private final String initialServer = "https://initial.server";
    private final String initialKey = "initial_key";
    private final int initialTs = 100;

    @BeforeEach
    void setUp() {
        serverUserTasks = new ServerUserTasks(
                vk, actor, "test", 123L, userRepository,
                regionCommandType, listVacancyType, tasksRepository
        );
        serverUserTasks = Mockito.spy(serverUserTasks);

        processor = new VkMainProcessor(
                initialServer, initialKey, initialTs,
                vk, actor, 123L, regionCommandType,
                userRepository, listVacancyType, tasksRepository
        );
        processor = Mockito.spy(processor);

    }

    @Test
    void testVkUpdateInfoWhenHandlesNullTextSafely() {
        String json = """
                {
                  "ts": 9999,
                  "updates": [
                    {
                      "type": "message_new",
                      "object": {
                        "message": {
                          "peer_id": 555,
                          "text": null
                        }
                      }
                    }
                  ]
                }
                """;

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        assertDoesNotThrow(() -> {
            VkMainProcessor.vkUpdateInfo(
                    vk, actor, root,
                    regionCommandType, listVacancyType,
                    userRepository, tasksRepository
            );
        });
        verify(userRepository, never()).exists(anyLong());
        verify(tasksRepository, never()).findTaskForPeerId(anyLong());
    }

    @Test
    void testVkUpdateInfoWhenHandlesTextSafely() throws Exception {
        String json = """
                {
                  "ts": 9999,
                  "updates": [
                    {
                      "type": "message_new",
                      "object": {
                        "message": {
                          "peer_id": 555,
                          "text": "1"
                        }
                      }
                    }
                  ]
                }
                """;
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        setupVkApiMocks();
        Long peerId = 555L;
        when(userRepository.exists(peerId)).thenReturn(false);
        VkUser mockUser = new VkUser(peerId, "Ivan", "Petrov");
        when(userRepository.findById(peerId)).thenReturn(Optional.of(mockUser));
        assertDoesNotThrow(() -> {
            VkMainProcessor.vkUpdateInfo(
                    vk, actor, root,
                    regionCommandType, listVacancyType,
                    userRepository, tasksRepository
            );
        });


        verify(userRepository, times(1)).findById((555L));
        verify(userRepository, times(1)).exists(anyLong());
        verify(userRepository, atLeastOnce()).save(any(VkUser.class));
    }

    @Test
    void testVkUpdateInfoWhenProcessesAllUpdatesAndCallsProcess() throws Exception {
        String json = """
                {
                  "ts": 12345,
                  "updates": [
                    {
                      "type": "message_new",
                      "object": {
                        "message": {
                          "peer_id": 1001,
                          "text": "/start"
                        }
                      }
                    },
                    {
                      "type": "message_new",
                      "object": {
                        "message": {
                          "peer_id": 1002,
                          "text": "вакансии москва"
                        }
                      }
                    }
                  ]
                }
                """;
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Long peerId = 1001L;
        when(userRepository.exists(peerId)).thenReturn(false);
        VkUser mockUser = new VkUser(peerId, "Ivan", "Petrov");
        when(userRepository.findById(peerId)).thenReturn(Optional.of(mockUser));
        Long peerId2 = 1002L;
        when(userRepository.exists(peerId2)).thenReturn(false);
        VkUser mockUser2 = new VkUser(peerId2, "Anna", "Petrova");
        when(userRepository.findById(peerId2)).thenReturn(Optional.of(mockUser2));
        setupVkApiMocks();
        setupVkApiMessageMock();
        int newTs = VkMainProcessor.vkUpdateInfo(
                vk, actor, root,
                regionCommandType, listVacancyType,
                userRepository, tasksRepository
        );
        assertThat(newTs).isEqualTo(12345);
        verify(userRepository, atLeastOnce()).findById(anyLong());
        verify(userRepository, atLeastOnce()).save(any(VkUser.class));
    }

    @Test
    void testVkUpdateInfoWhenSkipsMessageWithNullTextAndContinuesToNext() throws Exception {
        String json = """
                {
                  "ts": 12345,
                  "updates": [
                    {
                      "type": "message_new",
                      "object": {
                        "message": {
                          "peer_id": 1001,
                          "text": null
                        }
                      }
                    },
                    {
                      "type": "message_new",
                      "object": {
                        "message": {
                          "peer_id": 1002,
                          "text": "/start"
                        }
                      }
                    }
                  ]
                }
                """;
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Long peerId2 = 1002L;
        when(userRepository.exists(peerId2)).thenReturn(false);
        VkUser mockUser2 = new VkUser(peerId2, "Anna", "Sidorova");
        when(userRepository.findById(peerId2)).thenReturn(Optional.of(mockUser2));
        setupVkApiMocks();
        setupVkApiMessageMock();
        assertDoesNotThrow(() -> {
            VkMainProcessor.vkUpdateInfo(
                    vk, actor, root,
                    regionCommandType, listVacancyType,
                    userRepository, tasksRepository
            );
        });
        verify(userRepository, never()).exists(eq(1001L));
        verify(userRepository, times(1)).exists(eq(1002L));
    }

    @Test
    void testVkUpdateInfoWhenReturnsCorrectTsValue() throws Exception {
        String json = """
                {
                  "ts": 7777,
                  "updates": []
                }
                """;
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        int result = VkMainProcessor.vkUpdateInfo(vk, actor, root,
                regionCommandType, listVacancyType, userRepository, tasksRepository);
        assertThat(result).isEqualTo(7777);
    }

    @Test
    void testGeneratingUrlWhenSpecialCharsInKeyOrServer() {
        String server = "https://lp.vk.com/wh";
        String key = "secret_key_with&special=chars";
        int ts = 999;
        String result = VkMainProcessor.generatingUrl(server, key, ts);
        assertThat(result).contains("act=a_check");
        assertThat(result).contains("key=" + key);
        assertThat(result).contains("ts=" + ts);
        assertThat(result).endsWith("&wait=60");
    }

    @Test
    void testLoadJsonWhenNetworkFailsThrowsException() {
        String badUrl = "http://invalid-url-that-does-not-exist-12345.com";
        assertThrows(Exception.class, () ->
                VkMainProcessor.loadJson(badUrl)
        );
    }

    @Test
    void testMainCycleWhenHandlesFailedCode1() throws Exception {
        String errorJson = "{\"failed\": 1, \"ts\": 200}";
        JsonObject mockRoot = JsonParser.parseString(errorJson).getAsJsonObject();
        doReturn(mockRoot).when(processor).fetchAndParseUpdates(anyString());
        Groups groupsMock = mock(Groups.class);
        Thread thread = new Thread(() ->
                processor.mainCycle(initialServer, initialKey, initialTs, vk, actor, 123L,
                        regionCommandType, userRepository, listVacancyType, tasksRepository));
        thread.start();
        Thread.sleep(150);
        processor.stop();
        thread.join();
        verify(groupsMock, never()).getLongPollServer((UserActor) any(), any());
        assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    void testMainCycleWhenHandlesFailedCode2And3() throws Exception {
        String errorJson = "{\"failed\": 2}";
        JsonObject mockRoot = JsonParser.parseString(errorJson).getAsJsonObject();
        doReturn(mockRoot).when(processor).fetchAndParseUpdates(anyString());
        Groups groupsMock = mock(Groups.class);
        when(vk.groups()).thenReturn(groupsMock);
        GetLongPollServerResponse responseMock = mock(GetLongPollServerResponse.class);
        GroupsGetLongPollServerQuery queryMock = mock(GroupsGetLongPollServerQuery.class);
        when(groupsMock.getLongPollServer(eq(actor), eq(123L))).thenReturn(queryMock);
        when(queryMock.execute()).thenReturn(responseMock);
        Thread thread = new Thread(() -> processor.mainCycle(
                initialServer, initialKey, initialTs, vk, actor, 123L,
                regionCommandType, userRepository, listVacancyType, tasksRepository
        ));
        thread.start();
        Thread.sleep(100);

        processor.stop();
        thread.join();
        verify(groupsMock, atLeastOnce()).getLongPollServer(eq(actor), eq(123L));
        verify(queryMock, atLeastOnce()).execute();
        assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    void testMainCycleWhenHandlesNetworkException() throws Exception {
        doThrow(new IOException("Network error")).when(processor).fetchAndParseUpdates(anyString());
        Thread thread = new Thread(() -> processor
                .mainCycle(initialServer, initialKey,
                        initialTs, vk, actor, 123L,
                        regionCommandType, userRepository, listVacancyType, tasksRepository));
        thread.start();
        Thread.sleep(300);
        processor.stop();
        thread.join();
    }

    private void setupVkApiMocks() throws ClientException, ApiException {
        Users usersAction = mock(Users.class);
        when(vk.users()).thenReturn(usersAction);
        UsersGetQuery usersGetQuery = mock(UsersGetQuery.class);
        when(usersAction.get(actor)).thenReturn(usersGetQuery);
        when(usersGetQuery.userIds(anyString())).thenReturn(usersGetQuery);
        when(usersGetQuery.fields(any(Fields.class))).thenReturn(usersGetQuery);
        when(usersGetQuery.lang(any(Lang.class))).thenReturn(usersGetQuery);

        GetResponse mockResponse = mock(GetResponse.class);
        City mockCity = mock(City.class);
        when(mockCity.getTitle()).thenReturn("TestCity");
        when(mockResponse.getFirstName()).thenReturn("Ivan");
        when(mockResponse.getLastName()).thenReturn("Petrov");
        when(mockResponse.getCity()).thenReturn(mockCity);
        when(mockCity.getTitle()).thenReturn("Moscow");
        when(mockCity.getId()).thenReturn(1);
        when(usersGetQuery.execute()).thenReturn(List.of(mockResponse));

    }

    private void setupVkApiMessageMock() {
        Messages messagesMock = mock(Messages.class);
        MessagesSendQueryWithUserIds sendQueryMock = mock(MessagesSendQueryWithUserIds.class);
        when(sendQueryMock.message(anyString())).thenReturn(sendQueryMock);
        when(sendQueryMock.keyboard(any())).thenReturn(sendQueryMock);
        when(sendQueryMock.peerId(anyLong())).thenReturn(sendQueryMock);

        when(sendQueryMock.dontParseLinks(anyBoolean())).thenReturn(sendQueryMock);
        when(sendQueryMock.randomId(any())).thenReturn(sendQueryMock);
        when(vk.messages()).thenReturn(messagesMock);
        when(messagesMock.sendUserIds(eq(actor))).thenReturn(sendQueryMock);
    }
}

