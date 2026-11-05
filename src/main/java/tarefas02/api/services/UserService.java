package tarefas02.api.services; // Pacote das classes de serviço da aplicação

import java.util.Objects; // Utilitário para checagens de nulos
import java.util.Optional; // Encapsulador para objetos que podem ser nulos

import org.springframework.beans.factory.annotation.Autowired; // Anotação para injeção de dependências
import org.springframework.security.core.context.SecurityContextHolder; // Acesso ao contexto global de segurança
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Utilitário de hash de senhas BCrypt
import org.springframework.stereotype.Service; // Marca a classe como um serviço Spring
import org.springframework.transaction.annotation.Transactional; // Gerencia o contexto transacional do banco

import jakarta.validation.Valid;
import tarefas02.api.models.User; // Modelo da entidade User
import tarefas02.api.models.dtos.UserCreateDTO;
import tarefas02.api.models.dtos.UserUpdateDTO;
import tarefas02.api.models.enums.ProfileEnums; // Enumerador com os perfis de acesso (ADMIN, USER)
import tarefas02.api.repositories.UserRepository; // Repositório JPA para a entidade User
import tarefas02.api.security.UserSpringSecurity; // Objeto de detalhes do usuário autenticado no Spring Security
import tarefas02.api.exceptions.AuthorizationException; // Exceção de acesso negado
import tarefas02.api.services.exceptions.DataBindingViolationException; // Exceção de violação de vínculo de dados
import tarefas02.api.services.exceptions.ObjectNotFoundException; // Exceção para objetos não encontrados


// Serviço responsável pelas regras de negócio da entidade User e autorização de perfis
@Service
public class UserService {

    // Injeta o repositório JPA do usuário
    @Autowired
    private UserRepository userRepository;

    // Injeta o codificador de senhas BCrypt
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // Método estático utilitário que recupera o usuário autenticado na requisição atual
    public static UserSpringSecurity authenticated() {
        try {
            // Lê o usuário ativo armazenado no SecurityContextHolder pelo JWTAuthorizationFilter
            return (UserSpringSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            // Retorna nulo se a requisição for anônima ou sem token válido
            return null;
        }
    }

    // Busca um usuário por ID aplicando a regra de autorização
    public User findById(Long id) {
        // Recupera o usuário logado
        UserSpringSecurity  userSpringSecurity = authenticated();

        // Se não houver usuário logado, ou se ele NÃO for ADMIN e tentar buscar um ID diferente do seu próprio ID
        if (Objects.isNull(userSpringSecurity) || !userSpringSecurity.hasRole(ProfileEnums.ADMIN) && !id.equals(userSpringSecurity.getId())) {
            // Lança a exceção de autorização negando a consulta com o código HTTP 403 Forbidden
            throw new AuthorizationException("Acesso negado! Você não possui permissão para visualizar este perfil.");
        }

        // Busca o usuário no banco MySQL
        Optional<User> user = this.userRepository.findById(id);

        // Retorna a entidade encontrada ou lança 404 Not Found
        return user.orElseThrow(() -> new ObjectNotFoundException(
                "Usuário não encontrado! Id: " + id + ", Tipo: " + User.class.getName()));
    }

    // Cria um novo usuário comum no banco
    @Transactional
    public User create(User obj) {
        // Garante que o ID seja nulo para ser um INSERT
        obj.setId(null);
        // Aplica o hash BCrypt na senha informada
        obj.setPassword(this.bCryptPasswordEncoder.encode(obj.getPassword()));
        // Adiciona o perfil de usuário padrão (ROLE_USER)
        obj.addProfile(ProfileEnums.USER);
        // Persiste o novo usuário no MySQL
        obj = this.userRepository.save(obj);
        return obj;
    }

    // Atualiza a senha de um usuário existente
    @Transactional
    public User update(User obj) {
        // Valida se o usuário existe e se o usuário logado tem autorização para alterá-lo
        User newObj = findById(obj.getId());
        // Criptografa a nova senha
        newObj.setPassword(this.bCryptPasswordEncoder.encode(obj.getPassword()));
        // Salva as alterações
        return this.userRepository.save(newObj);
    }

    // Exclui um usuário por ID
    public void delete(Long id) {
        // Valida autorização antes de excluir
        findById(id);
        try {
            // Remove a entidade da base de dados
            this.userRepository.deleteById(id);
            // Executa o flush da transação
            this.userRepository.flush();
        } catch (Exception e) {
            // Lança exceção de conflito caso o usuário possua tarefas vinculadas
            throw new DataBindingViolationException("Não é possível excluir pois há entidades relacionadas!");
        }
    }

 // Converte o DTO de criação (UserCreateDTO) em uma Entidade User
    public User fromDTO(@Valid UserCreateDTO obj) {
        User user = new User();
        user.setUsername(obj.getUsername());
        user.setPassword(obj.getPassword());
        return user;
    }

    // Converte o DTO de atualização (UserUpdateDTO) em uma Entidade User
    public User fromDTO(@Valid UserUpdateDTO obj) {
        User user = new User();
        user.setId(obj.getId());
        user.setPassword(obj.getPassword());
        return user;
    }
}
