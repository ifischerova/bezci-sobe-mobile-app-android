package cz.bezcisobe.backend.controller;

import cz.bezcisobe.backend.dto.response.PageResponse;
import cz.bezcisobe.backend.dto.response.RaceResponse;
import cz.bezcisobe.backend.exception.ResourceNotFoundException;
import cz.bezcisobe.backend.service.RaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RaceService raceService;

    @Test
    void getAllRaces() throws Exception {
        RaceResponse race = new RaceResponse(
                "1", "Test Race", "Praha", "2026-05-10", "09:00", null,
                new RaceResponse.RefItem("1", "Maraton"),
                new RaceResponse.RefItem("1", "Silnice"),
                List.of(), "1", false
        );
        when(raceService.getAllRaces()).thenReturn(List.of(race));

        mockMvc.perform(get("/api/races"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Race"));
    }

    @Test
    void getRaceById_found() throws Exception {
        RaceResponse race = new RaceResponse(
                "1", "Test Race", "Praha", "2026-05-10", "09:00", null,
                new RaceResponse.RefItem("1", "Maraton"),
                new RaceResponse.RefItem("1", "Silnice"),
                List.of(), "1", false
        );
        when(raceService.getRaceById(1L)).thenReturn(race);

        mockMvc.perform(get("/api/races/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Race"));
    }

    @Test
    void getRaceById_notFound() throws Exception {
        when(raceService.getRaceById(999L))
                .thenThrow(new ResourceNotFoundException("Závod nenalezen"));

        mockMvc.perform(get("/api/races/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchRaces_returnsPagedEnvelope() throws Exception {
        RaceResponse race = new RaceResponse(
                "1", "Pražský maraton", "Praha", "2026-05-10", "09:00", null,
                new RaceResponse.RefItem("4", "Maraton"),
                new RaceResponse.RefItem("1", "Silnice"),
                List.of(), "1", false
        );
        PageResponse<RaceResponse> body = new PageResponse<>(
                List.of(race), 0, 10, 1, 1, true, true);

        when(raceService.search(eq("praz"), any(LocalDate.class), eq(1L), any(Pageable.class)))
                .thenReturn(body);

        mockMvc.perform(get("/api/races/search")
                        .param("q", "praz")
                        .param("from", "2026-01-01")
                        .param("trackTypeId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Pražský maraton"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
