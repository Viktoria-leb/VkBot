package jobBot.dto.keyboard;

import com.vk.api.sdk.objects.messages.*;
import jobBot.dto.VkUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VkKeyboard {

    private final Map<String, Integer> defaultMenu;

    public VkKeyboard(Map<String, Integer> defaultMenu) {
        this.defaultMenu = defaultMenu;
    }

    public Keyboard getDefaultKeyBoard(VkUser user) {
        Keyboard keyboard = new Keyboard();
        keyboard.setInline(true);
        keyboard.setOneTime(false);
        keyboard.setButtons(createAdditionalButtons(user, defaultMenu));

        return keyboard;
    }


    private static List<List<KeyboardButton>> createAdditionalButtons(VkUser user, Map<String, Integer> menuButtons) {
        List<List<KeyboardButton>> buttons = new ArrayList<>();
        menuButtons.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
            KeyboardButtonActionText keyboardButtonActionText = new KeyboardButtonActionText();
            keyboardButtonActionText.setLabel(entry.getKey());
            keyboardButtonActionText.setType(KeyboardButtonActionTextType.TEXT);
            keyboardButtonActionText.setPayload(VkPayload.builder()
                    .button(entry.getValue().toString())
                    .build().toString());


            KeyboardButton keyboardButton = new KeyboardButton();
            keyboardButton.setColor(KeyboardButtonColor.DEFAULT);
            keyboardButton.setAction(keyboardButtonActionText);
            buttons.add(List.of(keyboardButton));
        });


        return buttons;
    }
}
