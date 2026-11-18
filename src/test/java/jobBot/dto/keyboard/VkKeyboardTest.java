package jobBot.dto.keyboard;

import com.vk.api.sdk.objects.messages.*;
import jobBot.dto.VkUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class VkKeyboardTest {
    @InjectMocks
    private VkKeyboard vkKeyboard;

    @Mock
    private Map<String, Integer> defaultMenu;

    @Mock
    private VkUser user;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    void testGetDefaultKeyBoardWhenMenuIsEmpty() {
        when(defaultMenu.isEmpty()).thenReturn(true);
        Keyboard result = vkKeyboard.getDefaultKeyBoard(user);
        assertThat(result).isNotNull();
        assertThat(result.getButtons()).isNotNull();
        assertThat(result.getButtons()).isEmpty();
    }

    @Test
    void testGetDefaultKeyBoardWhenMenuHasOneButton() {
        Map<String, Integer> singleButtonMap = new LinkedHashMap<>();
        singleButtonMap.put("Кнопка 1", 10);

        VkKeyboard keyboardUnderTest = new VkKeyboard(singleButtonMap);

        Keyboard result = keyboardUnderTest.getDefaultKeyBoard(user);
        assertThat(result).isNotNull();
        assertThat(result.getButtons()).hasSize(1);

        List<KeyboardButton> row = result.getButtons().getFirst();
        assertThat(row).hasSize(1);

        KeyboardButton button = row.getFirst();
        assertThat(button.getColor()).isEqualTo(KeyboardButtonColor.DEFAULT);
        assertThat(button.getAction()).isNotNull();

        KeyboardButtonActionText action = (KeyboardButtonActionText) button.getAction();
        assertThat(action.getLabel()).isEqualTo("Кнопка 1");
        assertThat(action.getType()).isEqualTo(KeyboardButtonActionTextType.TEXT);
        assertThat(action.getPayload()).contains("\"button\":\"10\"");
    }

    @Test
    void testGetDefaultKeyBoardWhenMenuHasMultipleButtonsAndSortedByValue() {
        Map<String, Integer> unsortedMap = new LinkedHashMap<>();
        unsortedMap.put("Кнопка C", 30);
        unsortedMap.put("Кнопка A", 10);
        unsortedMap.put("Кнопка B", 20);

        VkKeyboard keyboardUnderTest = new VkKeyboard(unsortedMap);
        Keyboard result = keyboardUnderTest.getDefaultKeyBoard(user);
        assertThat(result.getButtons()).hasSize(3);
        List<String> labels = result.getButtons().stream()
                .map(row -> row.getFirst().getAction())
                .map(action -> ((KeyboardButtonActionText) action).getLabel())
                .toList();
        assertThat(labels).containsExactly("Кнопка A", "Кнопка B", "Кнопка C");
    }

    @Test
    void testCreateAdditionalButtonsWhenMenuContainsNullValues() {

        Map<String, Integer> mapWithNull = new LinkedHashMap<>();
        mapWithNull.put("Valid Button", 5);
        mapWithNull.put("Null Button", null);
        VkKeyboard keyboardUnderTest = new VkKeyboard(mapWithNull);
       assertThrows(NullPointerException.class, () -> {
            keyboardUnderTest.getDefaultKeyBoard(user);
        });
    }

    @Test
    void testCreateAdditionalButtonsWhenPayloadIsBuiltCorrectly() {
        Map<String, Integer> simpleMap = new LinkedHashMap<>();
        simpleMap.put("TestBtn", 999);
        VkKeyboard keyboardUnderTest = new VkKeyboard(simpleMap);
        Keyboard result = keyboardUnderTest.getDefaultKeyBoard(user);

        KeyboardButtonActionText action = (KeyboardButtonActionText) result.getButtons().getFirst().getFirst().getAction();
        String payload = action.getPayload();

        assertThat(payload).contains("\"button\":\"999\"");
        assertThat(payload).startsWith("{");
        assertThat(payload).endsWith("}");
    }

    @Test
    void testGetDefaultKeyBoardWhenUserObjectIsUsedButNotRequired() {

        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Menu Item", 1);
        VkKeyboard keyboardUnderTest = new VkKeyboard(map);
        when(user.getId()).thenReturn(123L);
        Keyboard result = keyboardUnderTest.getDefaultKeyBoard(user);
        assertThat(result.getButtons()).hasSize(1);
        verify(user, times(0)).getId();
    }
}