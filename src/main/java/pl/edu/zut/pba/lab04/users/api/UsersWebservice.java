package pl.edu.zut.pba.lab04.users.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pl.edu.zut.pba.lab04.users.UserMapper;
import pl.edu.zut.pba.lab04.users.UserService;
import pl.edu.zut.pba.lab04.users.api.model.CreateRequest;
import pl.edu.zut.pba.lab04.users.api.model.UpdateRequest;
import pl.edu.zut.pba.lab04.users.api.model.User;
import pl.edu.zut.pba.lab04.users.api.model.UserListResponse;
import pl.edu.zut.pba.lab04.users.api.model.UserResponse;

@RestController
@RequiredArgsConstructor
public class UsersWebservice implements UsersApi
{

    private final UserService userService;
    private final UserMapper mapper;

    @Override
    @PostMapping("/api/users")
    public ResponseEntity<UserResponse> createUser(@Parameter(name = "body", description = "User object that has to be added", required = true) @Valid @RequestBody CreateRequest body)
    {
        User userResponse = mapper.toApi(userService.save(mapper.toModel(body.getUser())));

        return ResponseEntity.ok(new UserResponse(body.getRequestHeader(), userResponse));
    }

    @Override
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> deleteUser(@Parameter(name = "id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id)
    {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/api/users")
    public ResponseEntity<UserListResponse> getAllUsers()
    {
        List<User> api = mapper.toApi(userService.findAll());
        return ResponseEntity.ok(new UserListResponse(null, api));
    }

    @Override
    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@Parameter(name = "id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id)
    {
        return ResponseEntity.ok(new UserResponse(null, mapper.toApi(userService.getById(id))));
    }

    @Override
    @PutMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(name = "id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id,
            @Parameter(name = "body", description = "", required = true) @Valid @RequestBody UpdateRequest body)
    {
        return ResponseEntity.ok(new UserResponse(body.getRequestHeader(), mapper.toApi(userService.save(id, mapper.toModel(body.getUser())))));
    }
}
