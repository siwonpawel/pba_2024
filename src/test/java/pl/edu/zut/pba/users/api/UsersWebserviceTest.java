package pl.edu.zut.pba.users.api;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.edu.zut.pba.users.api.model.CreateRequest;
import pl.edu.zut.pba.users.api.model.RequestHeader;
import pl.edu.zut.pba.users.api.model.User;
import pl.edu.zut.pba.users.api.model.UserResponse;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsersWebserviceTest
{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "sp56708", roles = "USER")
    void shouldReturnAllUsers() throws Exception
    {
        // given
        MockHttpServletRequestBuilder request = get("/api/users");

        // when
        ResultActions result = mockMvc.perform(request);

        // then
        result.andExpect(MockMvcResultMatchers.content().json("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "sp56708", roles = "USER")
    void shouldSaveUser() throws Exception
    {
        // given
        RequestHeader requestHeader = new RequestHeader(
                UUID.randomUUID(),
                OffsetDateTime.now()
        );
        User user = new User(
                "Paweł",
                "Siwoń",
                99,
                "01234567890",
                User.CitizenshipEnum.PL
        );

        CreateRequest createRequest = new CreateRequest(requestHeader, user);

        MockHttpServletRequestBuilder request = post("/api/users")
                .header("X-HMAC-SIGNATURE", "mocked-value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest));

        // when
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        // then
        UserResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponse.class
        );

        assertThat(response.getUser().getId())
                .isNotNull();
        assertThat(response.getUser())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);
    }

    @Test
    void shouldGetUserById() throws Exception
    {
        // given
        RequestHeader requestHeader = new RequestHeader(
                UUID.randomUUID(),
                OffsetDateTime.now()
        );
        User user = new User(
                "Paweł",
                "Siwoń",
                99,
                "01234567890",
                User.CitizenshipEnum.PL
        );

        CreateRequest createRequest = new CreateRequest(requestHeader, user);

        MockHttpServletRequestBuilder createUserRequest = post("/api/users")
                .with(httpBasic("sp56708", "123456"))
                .header("X-HMAC-SIGNATURE", "mocked-value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest));

        MvcResult savedUserResult = mockMvc.perform(createUserRequest)
                .andExpect(status().isOk())
                .andReturn();

        UserResponse userSavedResponse = objectMapper.readValue(
                savedUserResult.getResponse().getContentAsString(),
                UserResponse.class
        );

        MockHttpServletRequestBuilder getUserByIdRequest = get("/api/users/{id}", userSavedResponse.getUser().getId())
                .with(jwt().authorities(new OAuth2UserAuthority("SCOPE_pba_resource", Map.of(
                        "sub", "sp56708"
                ))));

        // when
        MvcResult getUserResult = mockMvc.perform(getUserByIdRequest)
                .andExpect(status().isOk())
                .andReturn();

        // then
        UserResponse getUserResponse = objectMapper.readValue(
                getUserResult.getResponse().getContentAsString(),
                UserResponse.class
        );

        assertThat(getUserResponse.getUser().getId())
                .isEqualTo(userSavedResponse.getUser().getId());
        assertThat(getUserResponse.getUser())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);
    }

    @Test
    void shouldUpdateUser() throws Exception
    {
        // given
        CreateRequest createRequest = new CreateRequest(
                new RequestHeader(
                        UUID.randomUUID(),
                        OffsetDateTime.now()
                ),
                new User(
                        "Paweł",
                        "Siwoń",
                        99,
                        "01234567890",
                        User.CitizenshipEnum.PL
                )
        );

        MockHttpServletRequestBuilder createUserRequest = post("/api/users")
                .with(httpBasic("sp56708", "123456"))
                .header("X-HMAC-SIGNATURE", "mocked-value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest));

        MvcResult savedUserResult = mockMvc.perform(createUserRequest)
                .andExpect(status().isOk())
                .andReturn();

        UserResponse userSavedResponse = objectMapper.readValue(
                savedUserResult.getResponse().getContentAsString(),
                UserResponse.class
        );

        CreateRequest updateRequest = new CreateRequest(
                new RequestHeader(
                        UUID.randomUUID(),
                        OffsetDateTime.now()
                ),
                new User(
                        "Paweł",
                        "Nowak",
                        99,
                        "01234567890",
                        User.CitizenshipEnum.DE
                )
        );

        MockHttpServletRequestBuilder updateUserById = put("/api/users/{id}", userSavedResponse.getUser().getId())
                .header("X-JWS-SIGNATURE", "mocked-value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(jwt().authorities(new OAuth2UserAuthority("SCOPE_pba_resource", Map.of(
                        "sub", "sp56708"
                ))));

        mockMvc.perform(updateUserById)
                .andExpect(status().isOk());

        // when
        MockHttpServletRequestBuilder getUserByIdRequest = get("/api/users/{id}", userSavedResponse.getUser().getId())
                .with(jwt().authorities(new OAuth2UserAuthority("SCOPE_pba_resource", Map.of(
                        "sub", "sp56708"
                ))));

        MvcResult getUserResult = mockMvc.perform(getUserByIdRequest)
                .andExpect(status().isOk())
                .andReturn();

        // then
        UserResponse getUserResponse = objectMapper.readValue(
                getUserResult.getResponse().getContentAsString(),
                UserResponse.class
        );

        assertThat(getUserResponse.getUser())
                .returns(userSavedResponse.getUser().getId(), User::getId)
                .returns(User.CitizenshipEnum.DE, User::getCitizenship)
                .returns("Nowak", User::getSurname);

        assertThat(getUserResponse.getUser())
                .usingRecursiveComparison()
                .ignoringFields("id", "surname", "citizenship")
                .isEqualTo(getUserResponse.getUser());
    }

    @Test
    @WithMockUser(username = "sp56708", roles = "USER")
    void shouldDeleteUser() throws Exception
    {
        // given
        RequestHeader requestHeader = new RequestHeader(
                UUID.randomUUID(),
                OffsetDateTime.now()
        );
        User user = new User(
                "Paweł",
                "Siwoń",
                99,
                "01234567890",
                User.CitizenshipEnum.PL
        );

        CreateRequest createRequest = new CreateRequest(requestHeader, user);

        MockHttpServletRequestBuilder request = post("/api/users")
                .header("X-HMAC-SIGNATURE", "mocked-value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest));

        MvcResult createUserResponse = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        UserResponse createdUser = objectMapper.readValue(
                createUserResponse.getResponse().getContentAsString(),
                UserResponse.class
        );

        MockHttpServletRequestBuilder deleteUserById = delete("/api/users/{id}", createdUser.getUser().getId())
                .with(jwt().authorities(new OAuth2UserAuthority("SCOPE_pba_resource", Map.of(
                        "sub", "sp56708"
                ))));

        // when
        mockMvc.perform(deleteUserById).andExpect(status().isNoContent());

        // then
        MockHttpServletRequestBuilder getUserById = delete("/api/users/{id}", createdUser.getUser().getId())
                .with(jwt().authorities(new OAuth2UserAuthority("SCOPE_pba_resource", Map.of(
                        "sub", "sp56708"
                ))));
        mockMvc.perform(getUserById).andExpect(status().isNoContent());
    }

}