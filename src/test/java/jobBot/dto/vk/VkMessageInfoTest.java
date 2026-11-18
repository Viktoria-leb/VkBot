package jobBot.dto.vk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vk.api.sdk.objects.messages.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class VkMessageInfoTest {

    @Mock
    private ClientInfo clientInfoMock;

    @Mock
    private Message messageMock;

    private VkMessageInfo vkMessageInfo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vkMessageInfo = new VkMessageInfo();
    }


    @Test
    void testSetClientInfoWhenProvidedValidObject() {
        when(clientInfoMock.isKeyboard()).thenReturn(true);
        when(clientInfoMock.getLangId()).thenReturn(100);
        vkMessageInfo.setClientInfo(clientInfoMock);

        assertThat(vkMessageInfo.getClientInfo()).isSameAs(clientInfoMock);
        assertThat(vkMessageInfo.getClientInfo().isKeyboard()).isTrue();
        assertThat(vkMessageInfo.getClientInfo().getLangId()).isEqualTo(100);
    }

    @Test
    void testSetMessageWhenProvidedValidObject() {
        String expectedText = "Hello VK!";
        when(messageMock.getText()).thenReturn(expectedText);
        vkMessageInfo.setMessage(messageMock);
        assertThat(vkMessageInfo.getMessage()).isSameAs(messageMock);
        assertThat(vkMessageInfo.getMessage().getText()).isEqualTo(expectedText);
    }

    @Test
    void testCreateWithConstructorLikeBehaviorWhenSettingBothFields() {
        ClientInfo info = new ClientInfo();
        info.setKeyboard(true);
        info.setInlineKeyboard(false);
        info.setLangId(5);
        Message msg = new Message();
        msg.setText("Test message body");

        VkMessageInfo fullInfo = new VkMessageInfo();
        fullInfo.setClientInfo(info);
        fullInfo.setMessage(msg);
        assertThat(fullInfo.getClientInfo()).isNotNull();
        assertThat(fullInfo.getClientInfo().isKeyboard()).isTrue();
        assertThat(fullInfo.getMessage()).isNotNull();
        assertThat(fullInfo.getMessage().getText()).isEqualTo("Test message body");
    }

    @Test
    void testClientInfoFieldsWhenInitializedWithNullList() {
        ClientInfo info = new ClientInfo();
        info.setButtonActions(null);
        info.setLangId(1);
        VkMessageInfo target = new VkMessageInfo();
        target.setClientInfo(info);
        assertThat(target.getClientInfo().getButtonActions()).isNull();
        assertThat(target.getClientInfo().getLangId()).isEqualTo(1);
    }

    @Test
    void testClientInfoFieldsWhenInitializedWithEmptyList() {
        List<String> emptyList = Collections.emptyList();
        ClientInfo info = new ClientInfo();
        info.setButtonActions(emptyList);
        info.setCarousel(true);
        VkMessageInfo target = new VkMessageInfo();
        target.setClientInfo(info);

        assertThat(target.getClientInfo().getButtonActions()).isEmpty();
        assertThat(target.getClientInfo().isCarousel()).isTrue();
    }

    @Test
    void testSerializationStructureWhenFieldsAreSet() {
        ClientInfo info = new ClientInfo();
        info.setLangId(42);
        info.setKeyboard(true);
        info.setInlineKeyboard(true);
        VkMessageInfo target = new VkMessageInfo();
        target.setClientInfo(info);
        target.setMessage(messageMock);

        assertThat(target.getClientInfo()).isNotNull();
        assertThat(target.getClientInfo().getLangId()).isEqualTo(42);
        assertThat(target.getClientInfo().isKeyboard()).isTrue();
    }

    @Test
    void testStateResetWhenSettingNullToClientInfo() {
        ClientInfo initialInfo = new ClientInfo();
        initialInfo.setLangId(10);
        vkMessageInfo.setClientInfo(initialInfo);
        vkMessageInfo.setClientInfo(null);
        assertThat(vkMessageInfo.getClientInfo()).isNull();
    }

    @Test
    void testStateResetWhenSettingNullToMessage() {
        vkMessageInfo.setMessage(messageMock);
        vkMessageInfo.setMessage(null);
        assertThat(vkMessageInfo.getMessage()).isNull();
    }
   }