package tarefas02.api.services; // Pacote onde esta classe de serviço está localizada no projeto

import java.util.List; // Importa List para manipular coleções de tarefas
import java.util.Objects; // Importa Objects para verificações de nulidade
import java.util.Optional; // Importa Optional para tratar retornos que podem ser nulos

import org.springframework.beans.factory.annotation.Autowired; // Anotação para injeção automática de dependências
import org.springframework.stereotype.Service; // Anotação que define esta classe como um Serviço do Spring
import org.springframework.transaction.annotation.Transactional; // Anotação para gerenciamento de transações no banco de dados

import tarefas02.api.models.Task; // Importa o modelo da entidade Task
import tarefas02.api.models.User; // Importa o modelo da entidade User
import tarefas02.api.models.enums.ProfileEnums; // Importa o enum de perfis de acesso (ADMIN, USER)
import tarefas02.api.repositories.TaskRepository; // Importa a interface do repositório de tarefas
import tarefas02.api.security.UserSpringSecurity; // Importa a classe de detalhes do usuário autenticado no Spring Security
import tarefas02.api.exceptions.AuthorizationException; // Importa a exceção customizada para acesso negado (HTTP 403)
import tarefas02.api.services.exceptions.DataBindingViolationException; // Importa a exceção customizada para erros de vínculo (HTTP 409)
import tarefas02.api.services.exceptions.ObjectNotFoundException; // Importa a exceção customizada para recursos não encontrados (HTTP 404)

// Classe de serviço responsável pelas regras de negócio, persistência e autorização de tarefas
@Service
public class TaskService {

    // Injeta automaticamente a instância do TaskRepository gerenciada pelo Spring
    @Autowired
    private TaskRepository taskRepository;

    // Injeta automaticamente a instância do UserService para buscas de usuário
    @Autowired
    private UserService userService;

    // Busca uma tarefa por ID validando se ela existe e se o usuário logado tem autorização para visualizá-la
    @Transactional(readOnly = true)
    public Task findById(Long id) {
        // Executa a busca no banco de dados, retornando um Optional
        Optional<Task> task = this.taskRepository.findById(id);

        // Se a tarefa não for encontrada, lança a exceção customizada ObjectNotFoundException (HTTP 404)
        Task obj = task.orElseThrow(() -> new ObjectNotFoundException(
                "Tarefa não encontrada! Id: " + id + ", Tipo: " + Task.class.getName()));

        // Recupera o usuário autenticado que está realizando a requisição
        UserSpringSecurity userSS = UserService.authenticated();

        // Se não houver usuário logado ou se o usuário logado NÃO for ADMIN e a tarefa não pertencer a ele
        if (Objects.isNull(userSS) || !userSS.hasRole(ProfileEnums.ADMIN) && !obj.getUser().getId().equals(userSS.getId())) {
            // Lança exceção de autorização negando o acesso (HTTP 403 Forbidden)
            throw new AuthorizationException("Acesso negado! Você não possui permissão para visualizar esta tarefa.");
        }

        // Retorna a tarefa encontrada e autorizada
        return obj;
    }

    // Busca todas as tarefas associadas ao usuário atualmente logado (extraído do token JWT)
    @Transactional(readOnly = true)
    public List<Task> findAllByLoggedUser() {
        // Recupera o usuário ativo no contexto de segurança da aplicação
        UserSpringSecurity userSS = UserService.authenticated();

        // Se a requisição não possuir um usuário autenticado
        if (Objects.isNull(userSS)) {
            // Lança exceção de autorização negada
            throw new AuthorizationException("Acesso negado! Usuário não autenticado.");
        }

        // Executa a consulta no repositório filtrando pelo ID do usuário autenticado
        return this.taskRepository.findByUser_Id(userSS.getId());
    }

    // Cria uma nova tarefa vinculando-a automaticamente ao usuário que está logado
    @Transactional
    public Task create(Task obj) {
        // Recupera o usuário logado no sistema
        UserSpringSecurity userSS = UserService.authenticated();

        // Verifica se existe um usuário autenticado na requisição
        if (Objects.isNull(userSS)) {
            throw new AuthorizationException("Acesso negado! Usuário não autenticado.");
        }

        // Busca a entidade User completa no banco de dados a partir do ID do usuário logado
        User user = this.userService.findById(userSS.getId());

        // Garante que o ID seja nulo para forçar um INSERT no banco de dados
        obj.setId(null);

        // Associa a entidade User validada à nova tarefa
        obj.setUser(user);

        // Salva a tarefa no banco de dados e retorna o objeto persistido
        return this.taskRepository.save(obj);
    }

    // Atualiza a descrição de uma tarefa existente após validar a autorização de propriedade
    @Transactional
    public Task update(Task obj) {
        // Busca a tarefa existente pelo ID (o método findById já valida a existência e a permissão do usuário)
        Task newObj = findById(obj.getId());

        // Atualiza apenas a descrição do objeto persistido com o novo valor informado
        newObj.setDescription(obj.getDescription());

        // Salva as alterações no banco de dados e retorna o objeto atualizado
        return this.taskRepository.save(newObj);
    }

    // Exclui uma tarefa do banco de dados pelo ID após validar a autorização
    public void delete(Long id) {
        // Valida se a tarefa existe e se o usuário logado tem permissão para excluí-la
        findById(id);

        try {
            // Solicita a remoção da tarefa no banco de dados pelo ID
            this.taskRepository.deleteById(id);
        } catch (Exception e) {
            // Captura exceções de violação de integridade e lança a exceção customizada (HTTP 409)
            throw new DataBindingViolationException("Não é possível excluir pois há entidades relacionadas!");
        }
    }
}