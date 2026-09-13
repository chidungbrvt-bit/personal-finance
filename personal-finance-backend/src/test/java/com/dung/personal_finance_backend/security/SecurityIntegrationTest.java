package com.dung.personal_finance_backend.security;

import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.Role;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void welcomeEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/public/welcome"))
                .andExpect(status().isOk());
    }

    @Test
    void corsAllowsPreflightRequestFromConfiguredFrontend() throws Exception {
        mockMvc.perform(options("/api/users/1")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"));
    }

    @Test
    void corsRejectsPreflightRequestFromUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/users/1")
                        .header(HttpHeaders.ORIGIN, "https://malicious.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void protectedEndpointRequiresToken() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/users/1"))
                .andExpect(jsonPath("$.httpStatus").value("401 UNAUTHORIZED"));
    }

    @Test
    void protectedEndpointRejectsMalformedToken() throws Exception {
        mockMvc.perform(get("/api/users/1")
                        .header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void registeredUserCanAccessOwnDataButNotAnotherUsersData() throws Exception {
        MvcResult registerResult = registerUser();

        String body = registerResult.getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.data.accessToken");
        Number userId = JsonPath.read(body, "$.data.user.id");

        mockMvc.perform(get("/api/users/{userId}", userId.longValue())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@example.com"));

        mockMvc.perform(get("/api/users/{userId}", userId.longValue() + 1)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void loginReturnsTokenForCorrectCredentials() throws Exception {
        registerUser();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "  USER@EXAMPLE.COM  ",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("user@example.com"));
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        registerUser();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"));
    }

    @Test
    void regularUserCannotListAllUsers() throws Exception {
        RegisteredUser registeredUser = registerAndReadCredentials();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + registeredUser.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void adminCanListAllUsersAndAccessAnotherUsersResource() throws Exception {
        RegisteredUser admin = registerAndReadCredentials();
        User adminEntity = userRepository.findById(admin.userId()).orElseThrow();
        adminEntity.setRole(Role.ADMIN);
        userRepository.saveAndFlush(adminEntity);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/users/{userId}", admin.userId() + 100)
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotAccessAnotherUsersNestedResources() throws Exception {
        RegisteredUser registeredUser = registerAndReadCredentials();

        mockMvc.perform(get("/api/users/{userId}/accounts", registeredUser.userId() + 1)
                        .header("Authorization", "Bearer " + registeredUser.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    private MvcResult registerUser() throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dung",
                                  "email": "USER@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();
    }

    private RegisteredUser registerAndReadCredentials() throws Exception {
        MvcResult result = registerUser();
        String body = result.getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.data.accessToken");
        Number userId = JsonPath.read(body, "$.data.user.id");
        return new RegisteredUser(token, userId.longValue());
    }

    private record RegisteredUser(String token, Long userId) {
    }
}
