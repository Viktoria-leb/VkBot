package jobBot.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jobBot.dto.Region;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class RegionParserTest {
    private RegionParser regionParser;

    @BeforeEach
    void setUp(){regionParser = new RegionParser(); }
    private static final String VALID_FILE = "/regionTest.csv";

    @Test
    void testParseRegionWhenFileExistsAndContainsValidData() {
        List<Region> result = regionParser.parseRegion(VALID_FILE);
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        Region first = result.getFirst();
        assertThat(first.name()).isEqualTo("Moscow");
        assertThat(first.number()).isEqualTo("77");

        Region second = result.get(1);
        assertThat(second.name()).isEqualTo("Kazan");
        assertThat(second.number()).isEqualTo("16");
    }

    @Test
    void testParseRegionWhenFileContainsEmptyLines() {
        List<Region> result = regionParser.parseRegion(VALID_FILE);
        assertThat(result).extracting(Region::name).contains("Moscow", "Kazan", "SPb");
    }

    @Test
    void testParseRegionWhenFileNotFoundThrowsRuntimeException() {
        String nonExistentFile = "/fake_file_that_does_not_exist.csv";
        assertThatThrownBy(() -> regionParser.parseRegion(nonExistentFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не получилось прочитать файл")
                .hasMessageContaining(nonExistentFile);
    }

    @Test
    void testParseRegionWhenFileIsEmptyReturnsEmptyList() {
        String headerOnlyFile = "/header_only_regions.csv";
            List<Region> result = regionParser.parseRegion(headerOnlyFile);
            assertThat(result).isEmpty();
    }

    @Test
    void testParseRegionWhenIOExceptionOccursHandlesGracefully(){
        List<Region> result = regionParser.parseRegion(VALID_FILE);
        assertThat(result).isNotEmpty();
    }

    @Test
    void testParseRegionWhenCsvHasMissingColumnsThrowsExceptionOrHandles() {
        String badFile = "bad_format.csv";
            assertThatThrownBy(() -> regionParser.parseRegion(badFile))
                    .isInstanceOf(RuntimeException.class);

    }

}