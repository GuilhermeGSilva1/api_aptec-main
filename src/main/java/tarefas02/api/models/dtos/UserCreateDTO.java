package tarefas02.api.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor 
@NoArgsConstructor 
@Data 
public class UserCreateDTO {

    //Nome de usuario obrigatório na criação 
    @NotBlank 
    @Size (min = 2,max = 100)
    private String username;

    //Senha obrigatório na criação 
    @NotBlank 
    @Size (min = 8, max = 60)
    private  String password;
    
}
