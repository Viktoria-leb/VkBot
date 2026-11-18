package jobBot.command.commands.additionalCommand;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.dto.VkUser;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ShowMenuCommand implements BotCommand {
    @Getter
    private final VkUser vkUser;
    private final Map<String, Integer> mapParameter;

    public ShowMenuCommand(VkUser vkUser) {
        this.vkUser = vkUser;
        mapParameter = new HashMap<>();
    }


    public Map<String, Integer> getMapParameter(VkUser vkUser) {
        mapParameter.put(CommandType.REGION.getButtonName() +
                parameterVacancyIsEmpty(vkUser.getVacancyNew().getRegion()), 1);
        mapParameter.put(CommandType.MIN_EXP.getButtonName() +
                parameterVacancyIsEmpty(vkUser.getVacancyNew().getExperience()), 2);
        mapParameter.put(CommandType.MIN_ZP.getButtonName() +
                parameterVacancyIsEmpty(vkUser.getVacancyNew().getZp()), 3);
        mapParameter.put(CommandType.KEYWORD.getButtonName() +
                parameterVacancyIsEmpty(vkUser.getVacancyNew().getKeyword()), 4);
        mapParameter.put(CommandType.DONE.getButtonName(), 5);
        return mapParameter;
    }

    @Override
    public CommandType getType() {
        return CommandType.NONE;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return getMapParameter(getVkUser());
    }

    private String parameterVacancyIsEmpty(String param) {
        if (param.isEmpty()) {
            return "";
        }
        return "[" + param + "]";
    }

    @Override
    public String message(VkUser vkUser) {
        String message;
        if (vkUser.isUpdateVacancy() && vkUser.isUpdateParameter()) {
            message = "Выберете параметры для обновления";
        } else message = "Данные заполнены:";
        return message;
    }

    @Override
    public void work(String text, VkUser vkUser) {

    }
}
