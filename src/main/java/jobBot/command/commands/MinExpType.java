package jobBot.command.commands;

import lombok.Getter;

@Getter
public enum MinExpType {
    WITHOUT("до года", 0),
    ONE("1-3 года", 2),
    THREE("3-5 лет", 4),
    MORE("более 5 лет", 6),
    NONE("", null);

    private final String buttonName;
    private final Integer years;

    MinExpType(String buttonName, Integer years) {
        this.buttonName = buttonName;
        this.years = years;
    }

    public static MinExpType fromText(String text) {
        for (MinExpType c : values()) {
            if (text != null
                    && text.equals(String.valueOf(c.years))
                    || text.equals(c.buttonName)) {
                return c;
            }
        }
        return NONE;
    }
}