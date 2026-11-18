package jobBot.dto.vk;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VkFullMessage {

    @SerializedName("group_id")
    private long groupId;

    private String type;

    @SerializedName("event_id")
    private String eventId;

    private String v;

    @SerializedName("object")
    private VkMessageInfo vkMessageInfo;


    public VkFullMessage(long groupId, String type, String eventId, String v, VkMessageInfo vkMessageInfo) {
        this.groupId = groupId;
        this.type = type;
        this.eventId = eventId;
        this.v = v;
        this.vkMessageInfo = vkMessageInfo;
    }

    @Override
    public String toString() {
        return "VkFullMessage{" +
                "groupId=" + groupId +
                ", type='" + type + '\'' +
                ", eventId='" + eventId + '\'' +
                ", v='" + v + '\'' +
                ", vkMessageInfo=" + vkMessageInfo +
                '}';
    }
}

