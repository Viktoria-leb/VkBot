package jobBot.dto.vk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class VkFullMessageTest {

    private VkFullMessage vkFullMessage;

    @Mock
    private VkMessageInfo vkMessageInfoMock;
    private long testGroupId;
    private String testType;
    private String testEventId;
    private String testV;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testGroupId = 12345L;
        testType = "message_new";
        testEventId = "event_999";
        testV = "5.192";
        when(vkMessageInfoMock.toString()).thenReturn("MockVkMessageInfo{}");
    }

    @Test
    void testToStringWhenObjectHasData() {
        vkFullMessage = new VkFullMessage(testGroupId, testType, testEventId, testV, vkMessageInfoMock);

        String result = vkFullMessage.toString();

        assertThat(result).isNotNull();
        assertThat(result).contains("VkFullMessage{");
        assertThat(result).contains("groupId=" + testGroupId);
        assertThat(result).contains("type='" + testType + "'");
        assertThat(result).contains("eventId='" + testEventId + "'");
        assertThat(result).contains("v='" + testV + "'");
        assertThat(result).contains("vkMessageInfo=");
        assertThat(result).endsWith("}");
    }

    @Test
    void testToStringWhenVkMessageInfoIsNull() {
        vkFullMessage = new VkFullMessage(testGroupId, testType, testEventId, testV, null);
        String result = vkFullMessage.toString();
        assertThat(result).isNotNull();
        assertThat(result).contains("vkMessageInfo=null");
    }


    @Test
    void testFieldMappingWhenUsedBySerializationLibrary() {
        vkFullMessage = new VkFullMessage(testGroupId, testType, testEventId, testV, vkMessageInfoMock);
        assertThat(vkFullMessage.getGroupId()).isEqualTo(testGroupId);
        assertThat(vkFullMessage.getEventId()).isEqualTo(testEventId);
    }
}
