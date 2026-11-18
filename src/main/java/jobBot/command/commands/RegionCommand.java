package jobBot.command.commands;

import jobBot.command.BotCommand;
import jobBot.command.CommandType;
import jobBot.command.MakeChanges;
import jobBot.dto.Region;
import jobBot.dto.VkUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record RegionCommand(RegionCommandType regionCommandType) implements BotCommand, MakeChanges {
    @Override
    public CommandType getType() {
        return CommandType.REGION;
    }

    @Override
    public String message(VkUser vkUser) {
        return "Выберите параметр из списка " +
                "или" +
                " введите команду: "
                + getType().getCommand() + " пробел и числовое значение региона.";
    }

    @Override
    public Map<String, Integer> commandsKeyboard() {
        return regionCommand();
    }

    private LinkedHashMap<String, Integer> regionCommand() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        List<Region> regionList = regionCommandType().getSubListRegion();
        int i = 0;
        for (Region region : regionList) {
            i++;
            map.put(region.name() + ", " + region.number(), i);
        }
        map.putLast(CommandType.NEXT.getButtonName(), 6);
        return map;
    }

    @Override
    public void work(String text, VkUser vkUser) {
        if (regionCommandType.numberSearchFromText(text)) {
            vkUser.getVacancyNew().setRegion(text);
            vkUser.setUpdateParameter(true);
        }
        regionCommandType.setPage(0);
    }
}
