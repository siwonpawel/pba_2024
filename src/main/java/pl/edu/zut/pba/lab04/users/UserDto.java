package pl.edu.zut.pba.lab04.users;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto
{
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String surname;
    private Integer age;
    private String personalId;
    private String citizenship;
    private String email;

}
