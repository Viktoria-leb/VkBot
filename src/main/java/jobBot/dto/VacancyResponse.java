package jobBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VacancyResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("job-name")
    private String jsonName;
    @JsonProperty("vac_url")
    private String vacUrl;
    @JsonProperty("creation-date")
    private String creationDate;
    @JsonProperty("company")
    private CompanyVacancyResponse company;
    @JsonProperty("contact_list")
    private List<Contact> contactList;
@Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyVacancyResponse {
        @JsonProperty("name")
        private String name;
    }
@Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        @JsonProperty("contact_type")
        private String type;
        @JsonProperty("contact_value")
        private String value;

        @Override
        public String toString() {
            return type + ": " + value;
        }
    }

    @Override
    public String toString() {
        StringBuilder contact = new StringBuilder();
        contactList.forEach(n -> contact.append(n.toString()).append("\n"));
        return
                "Вакансия: " + jsonName + "\n" +
                        ", url: " + vacUrl + "\n" +
                        ", company: " + company.name + "\n" +
                        ", Контакты: " + contact + "\n";
    }
}
