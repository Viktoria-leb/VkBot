package jobBot.parser;

import jobBot.dto.Region;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RegionParser {
    private static final int REGION_NAME = 0;
    private static final int REGION_CODE = 1;

    public List<Region> parseRegion(String csvFilePath) {
        InputStream inputStream = getClass().getResourceAsStream(csvFilePath);

        if (inputStream == null) {
            throw new RuntimeException("Не получилось прочитать файл %s".formatted(csvFilePath));
        }

        List<Region> regionList = new ArrayList<>();

        try {
            CSVFormat csvFormat = CSVFormat.Builder.create()
                    .setHeader()
                    .setSkipHeaderRecord(true).get();

            CSVParser csvParser = csvFormat.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            for (CSVRecord movieRecord : csvParser) {
                String title = movieRecord.get(REGION_NAME);
                String codeRegion = movieRecord.get(REGION_CODE);
                Region region = new Region(title, codeRegion);
                regionList.add(region);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return regionList;
    }
}

