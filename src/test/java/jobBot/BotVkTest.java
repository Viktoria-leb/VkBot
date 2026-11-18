package jobBot;

import com.vk.api.sdk.actions.Groups;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import com.vk.api.sdk.queries.groups.GroupsGetLongPollServerQuery;
import com.vk.api.sdk.queries.groups.GroupsSetLongPollSettingsQuery;
import com.vk.api.sdk.queries.messages.MessagesSendQueryWithUserIds;
import jobBot.repository.UserRepository;
import jobBot.repository.task.TasksRepository;
import jobBot.service.VkMainProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotVkTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private VkApiClient vkApiClient;
    @Mock
    private GroupActor groupActor;
    private BotVk botVk;

    @BeforeEach
    void setUp() throws Exception {
        botVk = new BotVk(123L, "test_token", tasksRepository, userRepository);
        injectField(botVk, "vk", vkApiClient);
        injectField(botVk, "actor", groupActor);
    }

    @Test
    @DisplayName("BotVk: корректный запуск, работа и остановка")
    void testLifecycle() throws Exception {
        VkMainProcessor vkMainProcessor = mock(VkMainProcessor.class);
        Groups groupsMock = mock(Groups.class);
        when(vkApiClient.groups()).thenReturn(groupsMock);
        GetLongPollServerResponse responseMock = mock(GetLongPollServerResponse.class);
        when(responseMock.getServer()).thenReturn(URI.create("https://mock-server.com"));
        when(responseMock.getKey()).thenReturn("mock-key");
        when(responseMock.getTs()).thenReturn("100");

        GroupsGetLongPollServerQuery queryMock = mock(GroupsGetLongPollServerQuery.class);
        when(queryMock.execute()).thenReturn(responseMock);
        when(groupsMock.getLongPollServer(eq(groupActor), anyLong())).thenReturn(queryMock);
        GroupsSetLongPollSettingsQuery settingsQueryMock = mock(GroupsSetLongPollSettingsQuery.class);
        when(settingsQueryMock.enabled(true)).thenReturn(settingsQueryMock);
        when(settingsQueryMock.messageNew(true)).thenReturn(settingsQueryMock);
        when(settingsQueryMock.execute()).thenReturn(null);
        when(groupsMock.setLongPollSettings(eq(groupActor), anyLong())).thenReturn(settingsQueryMock);
        Thread botThread = new Thread(botVk);
        botThread.start();
        Thread.sleep(300);
        injectField(botVk, "vkMainProcessor", vkMainProcessor);
        verify(groupsMock, atLeastOnce()).setLongPollSettings(eq(groupActor), anyLong());
        verify(groupsMock, atLeastOnce()).getLongPollServer(eq(groupActor), anyLong());
        botVk.stop();
        assertThat(botVk.isRunning()).as("Бот должен быть остановлен").isFalse();
        botThread.join(2000);
        assertThat(botThread.getState()).isEqualTo(Thread.State.TERMINATED);
        verify(vkMainProcessor, times(1)).stop();
    }

    @Test
    @DisplayName("BotVk: writeMessages отправляет сообщение через VK API")
    void testWriteMessages() throws Exception {
        Messages messagesMock = mock(Messages.class);
        MessagesSendQueryWithUserIds sendQueryMock = mock(MessagesSendQueryWithUserIds.class);

        when(vkApiClient.messages()).thenReturn(messagesMock);
        when(messagesMock.sendUserIds(eq(groupActor))).thenReturn(sendQueryMock);
        when(sendQueryMock.peerId(anyLong())).thenReturn(sendQueryMock);
        when(sendQueryMock.message(anyString())).thenReturn(sendQueryMock);
        when(sendQueryMock.dontParseLinks(anyBoolean())).thenReturn(sendQueryMock);
        when(sendQueryMock.randomId(anyInt())).thenReturn(sendQueryMock);
        when(sendQueryMock.executeAsString()).thenReturn("ok");
        Long peerId = 555L;
        StringBuilder messageBuilder = new StringBuilder("Привет, это тест!");
        botVk.writeMessages(peerId, messageBuilder);
        verify(sendQueryMock, times(1)).peerId(eq(peerId));
        verify(sendQueryMock, times(1)).message(eq("Привет, это тест!"));
        verify(sendQueryMock, times(1)).dontParseLinks(eq(true));
        verify(sendQueryMock, times(1)).executeAsString();
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}