package jobBot.command;

import lombok.Getter;

@Getter
public enum CommandType {
    DEFAULT("Начать", "/start"),
    NEW_TASK("Создать заявку на поиск вакансий", "/newTask"),
    CURRENT_TASKS("Посмотреть текущие заявки на поиск", "/current"),
    DELETE_TASK("Удалить заявку на поиск", "/delete"),
    UPDATE_TASK("Обновить задачу на поиск", "/update"),
    STOP("Удаление всех поисков", "/stop"),

    REGION("Регион", "/region"),
    MIN_EXP("Опыт работы по профессии", "/experience"),
    MIN_ZP("Минимальная зарплата", "/salary"),
    KEYWORD("Слово для поиска", "/keyword"),
    DONE("Готово", "/done"),
    NEXT("Далее", "/next"),
    NONE(null, null);

    private final String buttonName;
    private final String command;

    CommandType(String buttonName, String command) {
        this.buttonName = buttonName;
        this.command = command;
    }


    public static CommandType fromText(String text) {
        for (CommandType c : values()) {
            if (text != null && (text.equals(c.command)
                    || text.equals(c.buttonName))) {
                return c;
            }
        }
        return NONE;
    }
}


