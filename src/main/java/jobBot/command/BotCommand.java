package jobBot.command;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Keyboard;
import jobBot.dto.VkUser;
import jobBot.dto.keyboard.VkKeyboard;

import java.util.Map;

public interface BotCommand extends MakeChanges {
    Map<String, Integer> parameter = Map.of(
            CommandType.REGION.getButtonName(), 1,
            CommandType.MIN_EXP.getButtonName(), 2,
            CommandType.MIN_ZP.getButtonName(), 3,
            CommandType.KEYWORD.getButtonName(), 4,
            CommandType.DONE.getButtonName(), 5);
    Map<String, Integer> menu = Map.of(
            CommandType.NEW_TASK.getButtonName(), 1,
            CommandType.CURRENT_TASKS.getButtonName(), 2,
            CommandType.UPDATE_TASK.getButtonName(), 3,
            CommandType.DELETE_TASK.getButtonName(), 4);

    CommandType getType();

    Map<String, Integer> commandsKeyboard();

    String message(VkUser vkUser);

    default void execute(VkApiClient vk,
                         GroupActor actor, VkUser vkUser) {
        String message = message(vkUser);
        Map<String, Integer> command = commandsKeyboard();
        VkKeyboard vkKeyboard = new VkKeyboard(command);
        Keyboard keyboard = vkKeyboard.getDefaultKeyBoard(vkUser);
        try {
            vk.messages().sendUserIds(actor)
                    .peerId(vkUser.getId())
                    .keyboard(keyboard)
                    .message(message)
                    .dontParseLinks(true)
                    .randomId((int) Math.random())
                    .executeAsString();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }

    }

}
