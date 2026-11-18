package jobBot.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class RegionTest {
    Region region;

    @BeforeEach
    void setUp() {
        region = new Region("Москва", "77");
    }

    @Test
    @DisplayName("Test get name region")
    public void testGetNameRegion() {
        assertEquals("Москва", region.name());
        assertThat(region.name()).isEqualTo("Москва");
    }

    @Test
    @DisplayName("Test get number region")
    public void testGetNumberRegion() {
        assertThat(region.number()).isEqualTo("77");
    }

    @Test
    @DisplayName("Test of the equals method")
    void testEquals() {
        Region region2 = new Region("Москва", "77");
        assertThat(region).isEqualTo(region2);
    }

    @Test
    @DisplayName("Test of the hashCode method")
    void testHashCode() {
        Region region2 = new Region("Москва", "77");
        assertThat(region.hashCode()).isEqualTo(region2.hashCode());
    }

    @Test
    @DisplayName("Test with different names")
    void testEqualsDifferentNames() {
        Region region2 = new Region("Московская область", "77");
        assertThat(region).isNotEqualTo(region2);
    }

    @Test
    @DisplayName("Test with different number")
    void testEqualsDifferentNumber() {
        Region region2 = new Region("Москва", "177");
        assertThat(region).isNotEqualTo(region2);
    }
}
