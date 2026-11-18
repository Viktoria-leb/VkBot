package jobBot.command.commands;

import lombok.Getter;

@Getter
public enum MinZpType {
    FIFTY("50000", "0"),
    SEVENTY("70000", "2"),
    ONE_HUNDRED("100000", "4"),
    ONE_HUNDRED_FIFTY("150000", "6"),
    NONE("", "");

    private final String buttonName;
    private final String years;

    MinZpType(String buttonName, String years) {
        this.buttonName = buttonName;
        this.years = years;
    }

    public static MinZpType fromText(String text) {
        for (MinZpType c : values()) {
            if (text != null && text.equals(c.years)
                    || text.equals(c.buttonName)) {
                return c;
            }
        }
        return NONE;
    }
}

