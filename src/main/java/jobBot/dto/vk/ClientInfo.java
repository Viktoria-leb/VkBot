package jobBot.dto.vk;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ClientInfo {

    @SerializedName("button_actions")
    private List<String> buttonActions;

    private boolean keyboard;
    private boolean inlineKeyboard;
    private boolean carousel;

    @SerializedName("lang_id")
    private int langId;
}