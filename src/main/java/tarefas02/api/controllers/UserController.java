package tarefas02.api.controllers; // Pacote dos controladores REST da aplicação

import java.net.URI; // Importação da classe URI

import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependências do Spring
import org.springframework.http.ResponseEntity; // Encapsulador de resposta HTTP
import org.springframework.validation.annotation.Validated; // Ativa a validação na controller
import org.springframework.web.bind.annotation.DeleteMapping; // Mapeia o método HTTP DELETE
import org.springframework.web.bind.annotation.GetMapping; // Mapeia o método HTTP GET
import org.springframework.web.bind.annotation.PathVariable; // Extração de variável da URL
import org.springframework.web.bind.annotation.PostMapping; // Mapeia o método HTTP POST
import org.springframework.web.bind.annotation.PutMapping; // Mapeia o método HTTP PUT
import org.springframework.web.bind.annotation.RequestBody; // Extrai o corpo em JSON da requisição
import org.springframework.web.bind.annotation.RequestMapping; // Mapeia o caminho base da rota
import org.springframework.web.bind.annotation.RestController; // Marca a classe como Controller REST
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // Utilitário de construção de URIs

import jakarta.validation.Valid; // Dispara o Bean Validation no parâmetro recebido
import tarefas02.api.models.User; // Entidade User
import tarefas02.api.models.dtos.UserCreateDTO; // DTO de criação de usuário
import tarefas02.api.models.dtos.UserUpdateDTO; // DTO de atualização de usuário
import tarefas02.api.services.UserService; // Serviço de usuários

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    // Injeta o serviço de usuários
    @Autowired
    private UserService userService;

    // Busca um usuário por ID: GET /user/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        User obj = this.userService.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    // Endpoint de criação atualizado para receber o UserCreateDTO em vez da Entidade pura
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody UserCreateDTO objDto) {
        User obj = this.userService.fromDTO(objDto);
        User newObj = this.userService.create(obj);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newObj.getId()).toUri();

        return ResponseEntity.created(uri).build();
    }

    // Endpoint de atualização de senha atualizado para receber o UserUpdateDTO
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@Valid @RequestBody UserUpdateDTO objDto, @PathVariable Long id) {
        objDto.setId(id);
        User obj = this.userService.fromDTO(objDto);
        this.userService.update(obj);
        return ResponseEntity.noContent().build();
    }

    // Exclui um usuário existente: DELETE /user/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}