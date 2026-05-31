package cz.bezcisobe.backend.controller;

import cz.bezcisobe.backend.dto.response.PageResponse;
import cz.bezcisobe.backend.dto.response.UserResponse;
import cz.bezcisobe.backend.service.AdminService;
import cz.bezcisobe.backend.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private RideService rideService;

    @Test
    void listUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listUsers_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_asAdmin_returns200() throws Exception {
        PageResponse<UserResponse> page = new PageResponse<>(
                List.<UserResponse>of(), 0, 20, 0, 0, true, true);
        when(adminService.listUsers(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void forceDeleteRide_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/admin/rides/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void forceDeleteRide_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(delete("/api/admin/rides/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
