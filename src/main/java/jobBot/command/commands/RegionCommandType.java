package jobBot.command.commands;

import com.vk.api.sdk.client.VkApiClient;
import jobBot.dto.Region;
import jobBot.parser.RegionParser;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class RegionCommandType {
    @Getter
    private final List<Region> regions;
    private List<Region> subListRegion;
    @Getter
    int page;

    private static final int PAGE_SIZE = 5;


    public RegionCommandType() {
        regions = new RegionParser().parseRegion("/regions.csv");
        subListRegion = new ArrayList<>();
        page = 0;
    }

    public List<Region> getSubListRegion() {
        addRegions();
        return subListRegion;
    }

    public void setPage(int page) {
        if (page * PAGE_SIZE >= getRegions().size()) {
            this.page = 0;
        } else {
            this.page = page;
        }
    }

    private void addRegions() {
        subListRegion = new ArrayList<>();
        int from = getPage() * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, regions.size());

        for (int i = from; i < to; i++) {
            subListRegion.add(regions.get(i));
        }
        setPage(getPage() + 1);
    }

    public boolean searchFromText(String text) {
        String[] regionName = text.split(", ");
        if (regionName.length != 2) {
            return false;
        } else {
            Region region = new Region(regionName[0], regionName[1]);
            return getRegions().contains(region);
        }
    }

    public boolean numberSearchFromText(String text) {
        List<String> number = new ArrayList<>();
        for (Region region : getRegions()) {
            number.add(region.number());
        }
        return number.contains(text);
    }
}
