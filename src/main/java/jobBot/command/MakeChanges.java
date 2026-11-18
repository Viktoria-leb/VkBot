package jobBot.command;

import jobBot.dto.VkUser;

public interface MakeChanges {
    void work(String text, VkUser vkUser);
}
