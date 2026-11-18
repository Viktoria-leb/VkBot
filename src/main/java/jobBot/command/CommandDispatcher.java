package jobBot.command;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import jobBot.command.commands.*;
import jobBot.command.commands.additionalCommand.ListVacancyCommand;
import jobBot.command.commands.additionalCommand.ListVacancyType;
import jobBot.command.commands.additionalCommand.ShowMenuCommand;
import jobBot.command.commands.additionalCommand.UnknownCommand;
import jobBot.dto.Task;
import jobBot.dto.VacancyResponse;
import jobBot.dto.VkUser;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CommandDispatcher {
    private final VkApiClient vk;
    private final GroupActor actor;
    private final VkUser vkUser;
    private final RegionCommandType regionCommandType;
    private final ListVacancyType listVacancyType;
    private final String text;
    private final Map<CommandType, BotCommand> commands
            = new EnumMap<>(CommandType.class);
    private final BotCommand unknownCommand;
    private final BotCommand showMenuVacancyCommand;
    private final BotCommand listVacancyCommand;

    public CommandDispatcher(VkApiClient vk,
                             GroupActor actor,
                             RegionCommandType regionCommandType,
                             ListVacancyType listVacancyType,
                             VkUser vkUser,
                             String text) {
        this.vk = vk;
        this.actor = actor;
        this.vkUser = vkUser;
        this.text = text;
        this.regionCommandType = regionCommandType;
        this.listVacancyType = listVacancyType;
        unknownCommand = new UnknownCommand();
        showMenuVacancyCommand = new ShowMenuCommand(vkUser);
        listVacancyCommand = new ListVacancyCommand(listVacancyType);

        register(new StartCommand());
        register(new NewTaskCommand());
        register(new CurrentTasksCommand());
        register(new DeleteTaskCommand());
        register(new UpdateTaskCommand(vkUser));
        register(new RegionCommand(regionCommandType));
        register(new MinExpCommand());
        register(new MinZpCommand());
        register(new KeywordCommand());
        register(new DoneCommand(regionCommandType));
        register(new NextCommand(regionCommandType, vkUser, listVacancyType));
        register(new StopCommand(vkUser));
        handler(vk, actor, regionCommandType, vkUser, text);
    }

    private void register(BotCommand command) {
        commands.put(command.getType(), command);
    }

    private void handler(VkApiClient vk,
                         GroupActor actor, RegionCommandType regionCommandType,
                         VkUser vkUser, String text) {
        BotCommand command;
        if (text.isEmpty()) {
            unknownCommand.execute(vk, actor, vkUser);
        }
        CommandType type = CommandType.fromText(text);
        if (type != CommandType.NONE) {
            if (vkUser.getVacancyMap().size() > 1) {
                if (type.equals(CommandType.UPDATE_TASK)
                        || type.equals(CommandType.DELETE_TASK)) {
                    command = listVacancyCommand;
                } else {
                    command = commands.getOrDefault(CommandType.fromText(text), unknownCommand);
                }
            } else {
                command = commands.getOrDefault(CommandType.fromText(text), unknownCommand);
            }

            command.work(text, vkUser);
            command.execute(vk, actor, vkUser);
        } else {
            if (text.startsWith("/")) {
                String[] split = text.split(" ", 2);
                CommandType commandType = CommandType.fromText(split[0]);
                switch (commandType) {
                    case REGION: {
                        command = new RegionCommand(regionCommandType);
                        command.work(split[1], vkUser);
                        showMenuVacancyCommand.execute(vk, actor, vkUser);
                        break;
                    }
                    case MIN_EXP: {
                        command = new MinExpCommand();
                        command.work(split[1], vkUser);
                        showMenuVacancyCommand.execute(vk, actor, vkUser);
                        break;
                    }
                    case MIN_ZP: {
                        command = new MinZpCommand();
                        command.work(split[1], vkUser);
                        showMenuVacancyCommand.execute(vk, actor, vkUser);
                        break;
                    }
                    case KEYWORD: {
                        command = new KeywordCommand();
                        command.work(split[1], vkUser);
                        showMenuVacancyCommand.execute(vk, actor, vkUser);
                        break;
                    }
                    case DELETE_TASK: {
                        command = new DeleteTaskCommand();
                        command.work(split[1], vkUser);
                        command.execute(vk, actor, vkUser);
                        break;
                    }
                    case UPDATE_TASK: {
                        command = new UpdateTaskCommand(vkUser);
                        command.work(split[1], vkUser);
                        showMenuVacancyCommand.execute(vk, actor, vkUser);
                        break;
                    }
                    default: {
                        unknownCommand.execute(vk, actor, vkUser);
                    }
                }
            }
        }
    }
}
