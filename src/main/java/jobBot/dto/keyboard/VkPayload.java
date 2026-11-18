package jobBot.dto.keyboard;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VkPayload {


    private String button;


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
