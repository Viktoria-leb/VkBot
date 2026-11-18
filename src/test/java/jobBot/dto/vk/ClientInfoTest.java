package jobBot.dto.vk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientInfoTest {
    private ClientInfo clientInfo;

    @BeforeEach
    void setUp() {
        clientInfo = new ClientInfo();
    }

    @Test
    void testSetButtonActionsWhenPassedNonEmptyList() {
        List<String> actions = new ArrayList<>();
        actions.add("ACTION_1");
        actions.add("ACTION_2");
        clientInfo.setButtonActions(actions);
        assertThat(clientInfo.getButtonActions()).isEqualTo(actions);
        assertThat(clientInfo.getButtonActions()).containsExactly("ACTION_1", "ACTION_2");
    }

    @Test
    void testSerializedNameFieldWhenSerialized() {
        clientInfo.setButtonActions(List.of("TestAction"));
        assertThat(clientInfo.getButtonActions()).hasSize(1);
    }

    @Test
    void testGetLangIdWhenDefaultConstructorUsed() {
        assertThat(clientInfo.getLangId()).isEqualTo(0);
    }

    @Test
    void testSetLangIdWhenValidPositiveValue() {
        int expectedLangId = 101;
        clientInfo.setLangId(expectedLangId);
        assertThat(clientInfo.getLangId()).isEqualTo(101);
    }

    @Test
    void testSetLangIdWhenNegativeValue() {
        int negativeLangId = -5;
        clientInfo.setLangId(negativeLangId);
        assertThat(clientInfo.getLangId()).isEqualTo(negativeLangId);
    }

    @Test
    void testClientInfoWhenAllFlagsEnabledAndCustomLangId() {
        List<String> actions = List.of("SEND_MSG", "OPEN_LINK");
        int langId = 42;
        clientInfo.setButtonActions(actions);
        clientInfo.setKeyboard(true);
        clientInfo.setInlineKeyboard(true);
        clientInfo.setCarousel(true);
        clientInfo.setLangId(langId);
        assertThat(clientInfo.getButtonActions()).isEqualTo(actions);
        assertThat(clientInfo.isKeyboard()).isTrue();
        assertThat(clientInfo.isInlineKeyboard()).isTrue();
        assertThat(clientInfo.isCarousel()).isTrue();
        assertThat(clientInfo.getLangId()).isEqualTo(langId);
    }

    @Test
    void testClientInfoWhenResetToDefaultsViaNewInstance() {
        ClientInfo newClient = new ClientInfo();
        assertThat(newClient.getButtonActions()).isNull();
        assertThat(newClient.isKeyboard()).isFalse();
        assertThat(newClient.isInlineKeyboard()).isFalse();
        assertThat(newClient.isCarousel()).isFalse();
        assertThat(newClient.getLangId()).isEqualTo(0);
    }
}