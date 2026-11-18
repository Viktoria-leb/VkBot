package jobBot.command;

import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.queries.messages.MessagesSendQueryWithUserIds;
import jobBot.command.commands.RegionCommandType;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.dto.SearchTaskVacancy;
import jobBot.dto.Vacancy;
import jobBot.dto.VkUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {
    @Mock
    private VkApiClient vk;
    @Mock
    private GroupActor actor;
    @Mock
    private RegionCommandType regionCommandType;
    @Mock
    private ListVacancyType listVacancyType;
    @Mock
    private VkUser vkUser;
    @Mock
    private Messages messagesMock;
    @Mock
    private MessagesSendQueryWithUserIds sendQueryMock;

    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() throws ClientException {
        when(vk.messages()).thenReturn(messagesMock);
        when(messagesMock.sendUserIds(eq(actor))).thenReturn(sendQueryMock);
        when(sendQueryMock.peerId(anyLong())).thenReturn(sendQueryMock);
        when(sendQueryMock.keyboard(any())).thenReturn(sendQueryMock);
        when(sendQueryMock.message(anyString())).thenReturn(sendQueryMock);
        when(sendQueryMock.dontParseLinks(anyBoolean())).thenReturn(sendQueryMock);
        when(sendQueryMock.randomId(anyInt())).thenReturn(sendQueryMock);
        when(sendQueryMock.executeAsString()).thenReturn("OK");
        when(vkUser.getId()).thenReturn(123L);
    }

    @ParameterizedTest(name = "Обработка команды: {0}")
    @CsvSource({
            "'', Команда не распознана",
            "/start, Добро пожаловать",
            "/probe, Команда не распознана"
    })
    @DisplayName("Проверка обработки известных и неизвестных команд")
    void testCommandDispatching(String inputText, String expectedSubstring) throws ClientException {
        dispatcher = new CommandDispatcher(
                vk, actor, regionCommandType, listVacancyType, vkUser, inputText
        );

        verify(vk, times(1)).messages();
        verify(messagesMock, times(1)).sendUserIds(eq(actor));

        verify(sendQueryMock, times(1)).peerId(anyLong());
        verify(sendQueryMock, times(1)).keyboard(any());
        verify(sendQueryMock, times(1)).message(anyString());
        verify(sendQueryMock, times(1)).dontParseLinks(true);
        verify(sendQueryMock, times(1)).randomId(anyInt());
        verify(sendQueryMock, times(1)).executeAsString();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendQueryMock).message(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();

        assertThat(actualMessage)
                .contains(expectedSubstring);
    }


    @ParameterizedTest(name = "Обработка команды: {0}")
    @CsvSource({
            "/region 78, Данные заполнены: ",
            "/keyword Java, Данные заполнены: ",
            "/salary  100000, Данные заполнены: ",
            "/experience 1-3 года, Данные заполнены: "
    })
    @DisplayName("Обработка команд с параметрами")
    void testCommandWithParameter(String inputText, String expectedSubstring) throws ClientException {
        Vacancy mockVacancy = mock(Vacancy.class);
        when(mockVacancy.getRegion()).thenReturn("");
        when(mockVacancy.getExperience()).thenReturn("");
        when(mockVacancy.getZp()).thenReturn("");
        when(mockVacancy.getKeyword()).thenReturn("");
        when(vkUser.getVacancyNew()).thenReturn(mockVacancy);
        dispatcher = new CommandDispatcher(
                vk, actor, regionCommandType, listVacancyType, vkUser, inputText
        );
        verify(vk, times(1)).messages();
        verify(messagesMock, times(1)).sendUserIds(eq(actor));
        verify(sendQueryMock, times(1)).peerId(anyLong());
        verify(sendQueryMock, times(1)).keyboard(any());
        verify(sendQueryMock, times(1)).message(anyString());
        verify(sendQueryMock, times(1)).dontParseLinks(true);
        verify(sendQueryMock, times(1)).randomId(anyInt());
        verify(sendQueryMock, times(1)).executeAsString();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendQueryMock).message(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();

        assertThat(actualMessage)
                .contains(expectedSubstring);
    }

    @ParameterizedTest(name = "Обработка команды: {0}")
    @CsvSource({
            "Слово для поиска, /keyword ",
            "Минимальная зарплата, /salary ",
            "Опыт работы по профессии, /experience "
    })
    @DisplayName("Обработка команд ")
    void testCommandWithoutParameter(String inputText, String expectedSubstring) throws ClientException {
        Vacancy vacancy = new Vacancy();
        when(vkUser.getVacancyNew()).thenReturn(vacancy);
        dispatcher = new CommandDispatcher(
                vk, actor, regionCommandType, listVacancyType, vkUser, inputText
        );
        verify(vk.messages(), times(1)).sendUserIds(eq(actor));
        verify(vk.messages().sendUserIds(actor)).keyboard(any());
        verify(messagesMock, times(2)).sendUserIds(eq(actor));
        verify(sendQueryMock, times(1)).peerId(anyLong());
        verify(sendQueryMock, times(1)).keyboard(any());
        verify(sendQueryMock, times(1)).message(anyString());
        verify(sendQueryMock, times(1)).dontParseLinks(true);
        verify(sendQueryMock, times(1)).randomId(anyInt());
        verify(sendQueryMock, times(1)).executeAsString();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendQueryMock).message(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();
        assertThat(actualMessage)
                .contains("Выберите параметр из списка ");
        assertThat(actualMessage)
                .contains(expectedSubstring);
    }

    @Test
    void testCommandWithParameter_regionCommand() throws ClientException {
        String text = "Регион";
        dispatcher = new CommandDispatcher(
                vk, actor, regionCommandType, listVacancyType, vkUser, text
        );
        verify(vk.messages(), times(1)).sendUserIds(eq(actor));
        verify(vk.messages().sendUserIds(actor)).keyboard(any());
        verify(messagesMock, times(2)).sendUserIds(eq(actor));
        verify(sendQueryMock, times(1)).peerId(anyLong());
        verify(sendQueryMock, times(1)).keyboard(any());
        verify(sendQueryMock, times(1)).message(anyString());
        verify(sendQueryMock, times(1)).dontParseLinks(true);
        verify(sendQueryMock, times(1)).randomId(anyInt());
        verify(sendQueryMock, times(1)).executeAsString();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendQueryMock).message(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();
        assertThat(actualMessage)
                .contains("Выберите параметр из списка ");
        assertThat(actualMessage)
                .contains("/region");
    }

    @ParameterizedTest(name = "Обработка команды: {0}")
    @CsvSource({
            "/update, У вас нет заявок для обнавления",
            "/current, У вас нет заявок на поиск.",
            "/delete, У вас нет заявок для удаления."
    })
    void testCommandVacanciesWhenNotSearchVacancy(String text, String expectedSubstring) throws ClientException {
        when(vkUser.getVacancyMap()).thenReturn(new TreeMap<>());
        dispatcher = new CommandDispatcher(
                vk, actor, regionCommandType, listVacancyType, vkUser, text
        );
        verify(vk.messages(), times(1)).sendUserIds(eq(actor));
        verify(messagesMock, times(1)).sendUserIds(eq(actor));
        verify(sendQueryMock, times(1)).peerId(anyLong());
        verify(sendQueryMock, times(1)).keyboard(any());
        verify(sendQueryMock, times(1)).message(anyString());
        verify(sendQueryMock, times(1)).dontParseLinks(true);
        verify(sendQueryMock, times(1)).randomId(anyInt());
        verify(sendQueryMock, times(1)).executeAsString();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendQueryMock).message(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();
        assertThat(actualMessage)
                .contains(expectedSubstring);
    }

    private void prepareMocksForCommand(String text) {
        Vacancy vacancy = new Vacancy();
        vacancy.setKeyword("Java");
        TreeMap<Integer, SearchTaskVacancy> map = new TreeMap<>();
        map.put(1, new SearchTaskVacancy(vacancy));
        when(vkUser.getVacancyMap()).thenReturn(map);
        switch (text) {
            case "/update":
                when(vkUser.getVacancyNew()).thenReturn(vacancy);
                break;
            case "/current":
                break;
            case "/delete":
                when(vkUser.isDeleteVacancy()).thenReturn(true);
                break;
        }
    }

    @ParameterizedTest(name = "Обработка команды: \"{0}\"")
    @CsvSource({
            "/update, Выберите параметры вакансии для изменения : ",
            "/current, Ваши текущие заявки на поиск:",
            "/delete, Заявка успешно удалена"
    })
    @DisplayName("Проверка команд управления вакансиями")
    void testCommandVacanciesWhenOneSearchVacancy(String text, String expectedSubstring) throws ClientException {
        prepareMocksForCommand(text);
        dispatcher = new CommandDispatcher(
                vk, actor, regionCommandType, listVacancyType, vkUser, text
        );
        verify(vk, times(1)).messages();
        verify(messagesMock, times(1)).sendUserIds(eq(actor));

        verify(sendQueryMock, times(1)).peerId(anyLong());
        verify(sendQueryMock, times(1)).keyboard(any());
        verify(sendQueryMock, times(1)).message(anyString());
        verify(sendQueryMock, times(1)).dontParseLinks(true);
        verify(sendQueryMock, times(1)).randomId(anyInt());
        verify(sendQueryMock, times(1)).executeAsString();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendQueryMock).message(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();

        assertThat(actualMessage)
                .contains(expectedSubstring);
    }

    @ParameterizedTest(name = "Обработка команды: {0}")
    @CsvSource({
            "/update,  или введите номер поиска",
            "/delete, Для удаления всех поисков введите команду "
    })
    void testCommandVacanciesWhenManySearchVacancy(String text, String expectedSubstring) throws ClientException {
        when(vkUser.isDeleteVacancy()).thenReturn(true);
        Vacancy vacancy = new Vacancy();
        vacancy.setKeyword("Java");
        Vacancy vacancy1 = new Vacancy();
        vacancy1.setKeyword("Kotlen");
        Vacancy vacancy2 = new Vacancy();
        vacancy.setKeyword("C++");
        TreeMap<Integer, SearchTaskVacancy> map = new TreeMap<>();
        map.put(1, new SearchTaskVacancy(vacancy));
        map.put(2, new SearchTaskVacancy(vacancy1));
        map.put(3, new SearchTaskVacancy(vacancy2));
        when(vkUser.getVacancyMap()).thenReturn(map);
        StringBuilder builder = new StringBuilder();
        map.forEach((key, value) ->
                builder.append(key).append(" : ").append(value.toString()).append("\n"));
        when(vkUser.listOfAllVacancies()).thenReturn(builder.toString());

        dispatcher = new CommandDispatcher(
                vk, actor, regionCommandType, listVacancyType, vkUser, text
        );
        verify(vk.messages(), times(1)).sendUserIds(eq(actor));
        verify(messagesMock, times(1)).sendUserIds(eq(actor));
        verify(sendQueryMock, times(1)).peerId(anyLong());
        verify(sendQueryMock, times(1)).keyboard(any());
        verify(sendQueryMock, times(1)).message(anyString());
        verify(sendQueryMock, times(1)).dontParseLinks(true);
        verify(sendQueryMock, times(1)).randomId(anyInt());
        verify(sendQueryMock, times(1)).executeAsString();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendQueryMock).message(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();
        assertThat(actualMessage)
                .contains("Выберите номер поика из списка.");
        assertThat(actualMessage)
                .contains(expectedSubstring);
    }

    @Test
    @DisplayName("Проверка обработки команды /delete с ID")
    void testDeleteCommandWithId() {
        Vacancy vacancy = new Vacancy();
        vacancy.setKeyword("Java");
        TreeMap<Integer, SearchTaskVacancy> map = new TreeMap<>();
        map.put(55, new SearchTaskVacancy(vacancy));
        when(vkUser.getVacancyMap()).thenReturn(map);
        String text = "/delete 55";
        when(vkUser.isDeleteVacancy()).thenReturn(true);
        dispatcher = new CommandDispatcher(
                vk, actor, regionCommandType, listVacancyType, vkUser, text
        );
        verify(vk.messages(), times(1)).sendUserIds(eq(actor));
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendQueryMock).message(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();
        assertThat(actualMessage)
                .contains("Заявка успешно удалена");
    }
}
