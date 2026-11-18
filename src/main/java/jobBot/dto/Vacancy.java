package jobBot.dto;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
public class Vacancy {
    @Expose
    private String region;
    @Expose
    private String experience;
    @Expose
    private String zp;
    @Expose
    private String keyword;

    public Vacancy() {
        region = "";
        experience = "";
        zp = "";
        keyword = "";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vacancy vacancy)) return false;
        return Objects.equals(region, vacancy.region)
                && Objects.equals(experience, vacancy.experience)
                && Objects.equals(zp, vacancy.zp)
                && Objects.equals(keyword, vacancy.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, experience, zp, keyword);
    }

    @Override
    public String toString() {
        return "Region - " + toStringIsEmpty(region) +
                ", Experience - " + toStringIsEmpty(experience) +
                ", Salary - " + toStringIsEmpty(zp) +
                ", Keyword - " + toStringIsEmpty(keyword);
    }

    private String toStringIsEmpty(String string) {
        if (string.isEmpty()) {
            return "не указано";
        }
        return string;
    }

}

