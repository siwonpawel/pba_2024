package pl.edu.zut.pba.lab04.users;

import java.util.List;

import org.mapstruct.Mapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import pl.edu.zut.pba.lab04.users.api.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper
{

    UserDto toModel(@NotNull @Valid User user);

    User toApi(UserDto save);

    default List<User> toApi(List<UserDto> users)
    {
        return users.stream()
                .map(this::toApi)
                .toList();
    }
}
