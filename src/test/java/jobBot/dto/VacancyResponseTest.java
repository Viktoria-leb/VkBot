package jobBot.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class VacancyResponseTest {
    VacancyResponse vacancyResponse;
    @BeforeEach
    public void setUp() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
        {
          "id": "123",
          "job-name": "Java Dev",
          "vac_url": "https://example.com",
          "creation-date": "2024-04-04",
          "company": {"name": "Test Company"},
          "contact_list": [
            {"contact_type": "email", "contact_value": "hr@test.com"}
          ]
        }
        """;

       vacancyResponse = mapper.readValue(json, VacancyResponse.class);

    }

    @Test
    public void testCompanyName(){
        VacancyResponse.CompanyVacancyResponse company = vacancyResponse.getCompany();
        assertThat(company.getName()).isEqualTo("Test Company");
    }
    @Test
    public void testContactVacancyResponse(){
        List<VacancyResponse.Contact> contact =
                vacancyResponse.getContactList();
        StringBuilder builder = new StringBuilder();
        contact.forEach(v-> builder.append(v.getValue()));
        assertThat(builder).contains("hr@test.com");
    }


    @Test
   public void testGetVacancyResponseWhenJsonIsRead(){
        assertThat(vacancyResponse.getId()).isEqualTo("123");
        assertThat(vacancyResponse.getJsonName()).isEqualTo("Java Dev");
        assertThat(vacancyResponse.getCompany().getName()).isEqualTo("Test Company");
    }

    @Test
    public void testToStringWhenJsonIsRead(){
        String str = vacancyResponse.toString();
        assertThat(str).contains("Вакансия:");
        assertThat(str).contains("Java Dev");
        assertThat(str).contains(vacancyResponse.toString());
        assertThat(str).contains("company:");
    }
}
