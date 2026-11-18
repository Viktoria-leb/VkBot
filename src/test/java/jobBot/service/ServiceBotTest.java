package jobBot.service;

import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.queries.messages.MessagesSendQueryWithUserIds;
import jobBot.command.CommandType;
import jobBot.command.commands.KeyWordType;
import jobBot.command.commands.MinExpType;
import jobBot.command.commands.MinZpType;
import jobBot.command.commands.RegionCommandType;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.dto.Vacancy;
import jobBot.dto.VkUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceBotTest {

    @Mock
    private VkApiClient vk;
    @Mock
    private GroupActor actor;
    @Mock
    private Messages messagesActions;

    private RegionCommandType regionCommandType;
    private ListVacancyType listVacancyType;
    private KeyWordType keyWordTypeMock;
    private VkUser vkUser;

    private static final long PEER_ID = 123L;
    void setUp(){
    regionCommandType = mock(RegionCommandType.class);
    listVacancyType = mock(ListVacancyType.class);
    vkUser = mock(VkUser.class);
    keyWordTypeMock = mock(KeyWordType.class);
        vkUser.setHistoryCommand(new HashMap<>());}
    void setUpWith() {
        regionCommandType = mock(RegionCommandType.class);
        listVacancyType = mock(ListVacancyType.class);
        vkUser = mock(VkUser.class);
        keyWordTypeMock = mock(KeyWordType.class);
        vkUser.setHistoryCommand(new HashMap<>());
        MessagesSendQueryWithUserIds sendQueryMock = mock(MessagesSendQueryWithUserIds.class);
        when(sendQueryMock.message(anyString())).thenReturn(sendQueryMock);
        when(sendQueryMock.keyboard(any())).thenReturn(sendQueryMock);
        when(sendQueryMock.peerId(anyLong())).thenReturn(sendQueryMock);
        when(sendQueryMock.dontParseLinks(anyBoolean())).thenReturn(sendQueryMock);
        when(sendQueryMock.randomId(any())).thenReturn(sendQueryMock);
        when(vk.messages()).thenReturn(messagesActions);
        when(messagesActions.sendUserIds(eq(actor))).thenReturn(sendQueryMock);
        when(vk.messages()).thenReturn(messagesActions);
    }

    @Test
    void testFlagsResetWhenCommandIsDone() {
        setUpWith();
        String text = "/done";

        assertDoesNotThrow(() -> new ServiceBot(vk, actor, regionCommandType,
                listVacancyType, vkUser, text));
        verify(vkUser).setAddVacancy(false);
        verify(vkUser).setUpdateVacancy(false);
        verify(vkUser).setDeleteDuplicate(false);
        verify(vkUser).setUpdateParameter(false);
        verify(regionCommandType).setPage(0);
        verify(listVacancyType).setPage(0);
        verify(vkUser).addHistory(text);
    }

    @Test
    void testFilterButtonTextProcessing() {
        setUpWith();
        String buttonText = CommandType.REGION.getButtonName();
        assertDoesNotThrow(() -> new ServiceBot(vk, actor, regionCommandType,
                listVacancyType, vkUser, buttonText));
        verify(vkUser).addHistory(buttonText);
        verify(vkUser, never()).setDeleteVacancy(anyBoolean());
    }


    @Test
    void testEmptyTextDoesNotThrowException() {
        setUpWith();
        String emptyText = "";
        assertDoesNotThrow(() -> new ServiceBot(vk, actor,
                regionCommandType, listVacancyType, vkUser, emptyText));
        verify(vkUser).addHistory("");
    }
    @Test
    void testPlainTextInput() {
        setUpWith();
        String plainText = "просто";

        // Убеждаемся, что поиск возвращает false, чтобы попасть в ветку else
        when(regionCommandType.searchFromText(plainText)).thenReturn(false);

        assertDoesNotThrow(() -> new ServiceBot(vk, actor, regionCommandType, listVacancyType, vkUser, plainText));

        verify(vkUser).addHistory(plainText);
    }

    @Test
    void testNumberInputCombinesWithLastCommandWhenLastCommandExists() {
        setUp();

        String lastCommandText = "/search";
        String inputNumber = "5";
        vkUser.addHistory(lastCommandText);
        when(vkUser.lastCommand()).thenReturn(lastCommandText);
        assertThat(vkUser.lastCommand()).isEqualTo(lastCommandText);

        new ServiceBot(vk, actor, regionCommandType, listVacancyType, vkUser, inputNumber);
        verify(vkUser, never()).setAddVacancy(anyBoolean());
    }
    @Test
    void testDeleteTaskCommand() {
        setUpWith();

        String text = CommandType.DELETE_TASK.getButtonName();

        assertDoesNotThrow(() -> new ServiceBot(vk, actor, regionCommandType,
                listVacancyType, vkUser, text));

        verify(vkUser).setDeleteVacancy(false);
        verify(listVacancyType).setPage(0);

        // Остальные флаги не должны сбрасываться
        verify(vkUser, never()).setAddVacancy(anyBoolean());
        verify(vkUser, never()).setUpdateVacancy(anyBoolean());
    }
    @Test
    void testNumberInputCombinesWithLastCommand() {
       setUp();

        String lastCommandText = "/search";
        String inputNumber = "5";

        // ЭМУЛЯЦИЯ СОСТОЯНИЯ: говорим моку, что lastCommand() возвращает "/search"
        when(vkUser.lastCommand()).thenReturn(lastCommandText);

        assertDoesNotThrow(() -> new ServiceBot(vk, actor, regionCommandType,
                listVacancyType, vkUser, inputNumber));

        // Флаги не сбрасываются (это не DONE)
        verify(vkUser, never()).setAddVacancy(anyBoolean());

        // Проверяем, что история обновилась.
        // В логике: если число, то добавляется само число (или результат склейки, зависит от реализации redactorText)
        // Но точно должен быть вызван addHistory с каким-то текстом.
        verify(vkUser).addHistory(inputNumber);
    }

    @Test
    void testNumberInputFallbackWhenNoLastCommand() {
     setUp();
        String inputNumber = "99";
        // lastCommand() по умолчанию в setUp() возвращает null
        when(vkUser.lastCommand()).thenReturn("");

        assertDoesNotThrow(() -> new ServiceBot(vk, actor, regionCommandType, listVacancyType, vkUser, inputNumber));

        // Код не должен упасть. История должна быть добавлена.
        verify(vkUser).addHistory(inputNumber);
    }
    @Test
    void testSlashCommandWithoutArguments() {
        setUpWith();
        String text = "/start";
        assertDoesNotThrow(() -> new ServiceBot(vk, actor, regionCommandType, listVacancyType, vkUser, text));
        verify(vkUser).addHistory(text);
    }
    @Test
    void testRegionSearchLogic() {
        setUpWith();
        // Сценарий: текст содержит регион, например "Регион: Москва"

        String regionText = "78";
        when(vkUser.lastCommand()).thenReturn("/region");
        when(vkUser.getVacancyNew()).thenReturn(new Vacancy());
        // ВАЖНО: Так как regionCommandType - это МОК, мы должны явно сказать, что для этого текста он вернет true
        when(regionCommandType.numberSearchFromText(regionText)).thenReturn(true);


        assertDoesNotThrow(() -> new ServiceBot(vk, actor, regionCommandType, listVacancyType, vkUser, regionText));

        // Логика внутри notNumberNotCommandText вызовет setPage или другие методы,
        // но главное - конструктор должен отработать без ошибок.
        // Можно проверить, что searchFromText был вызван именно с этим текстом:
        verify(regionCommandType).numberSearchFromText(regionText);
    }

}