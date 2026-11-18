package jobBot.dto.vk;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.messages.Message;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VkMessageInfo {
    @SerializedName("client_info")
    private ClientInfo clientInfo;
    private Message message;
}
