package pl.edu.zut.pba.lab04.users;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService
{

    private final UserRepository userRepository;

    public UserDto save(UserDto userDto)
    {
        return userRepository.save(userDto);
    }

    public UserDto save(UUID id, UserDto userDto)
    {
        userDto.setId(id);
        return userRepository.save(userDto);
    }

    public void delete(UUID id)
    {
        userRepository.deleteById(id);
    }

    public List<UserDto> findAll()
    {
        return userRepository.findAll();
    }

    public UserDto getById(UUID id)
    {
        return userRepository.getReferenceById(id);
    }
}
