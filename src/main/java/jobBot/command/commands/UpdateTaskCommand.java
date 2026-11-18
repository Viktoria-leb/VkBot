package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.command.MakeChanges;
import jobBot.dto.Vacancy;
import jobBot.dto.VkUser;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class UpdateTaskCommand implements BotCommand, MakeChanges {
    @Getter
    private final VkUser vkUser;
    private final Map<String, Integer> mapParameter;

    public UpdateTaskCommand(VkUser vkUser) {
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
        return CommandType.UPDATE_TASK;
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return generatingKeyword(getVkUser());
    }

    private Map<String, Integer> generatingKeyword(VkUser vkUser) {
        if (!vkUser.getVacancyMap().isEmpty()) {
            if (vkUser.getVacancyMap().size() == 1) {
                return getMapParameter(getVkUser());
            }
        }
        return menu;
    }

    private String parameterVacancyIsEmpty(String param) {
        if (param.isEmpty()) {
            return "";
        }
        return "[" + param + "]";
    }

    @Override
    public String message(VkUser user) {
        user = getVkUser();
        if (!user.getVacancyMap().isEmpty()) {
            if (user.getVacancyMap().size() == 1) {
                return " Выберите параметры вакансии для изменения : ";
            } else {
                return " Выберите какой номер поиска для обновления "
                        + user.listOfAllVacancies();
            }
        }
        return "У вас нет заявок для обнавления.\n" +
                "Необходимо сначала создать заявку на поиск";
    }

    @Override
    public void work(String text, VkUser vkUser) {
        vkUser.setVacancyNew(new Vacancy());
        if (!vkUser.getVacancyMap().isEmpty()) {
            if (vkUser.getVacancyMap().size() == 1) {
                vkUser.setVacancyNew(vkUser.getVacancyMap().get(1).getVacancy());
                vkUser.setUpdateVacancy(true);
                vkUser.setVacancyUpdateId(1);
            } else {
                if (text.matches("\\d+")) {
                    vkUser.setVacancyNew(vkUser.getVacancyMap()
                            .get(Integer.parseInt(text)).getVacancy());
                    vkUser.setUpdateVacancy(true);
                    vkUser.setVacancyUpdateId(Integer.parseInt(text));
                }
            }
        }
    }
}

