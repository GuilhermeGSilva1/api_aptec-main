
/* 
package tarefas02.api.models; // Declaração do pacote onde a classe está localizada

import java.util.ArrayList; // Importação da classe ArrayList para manipular listas dinâmicas
import java.util.HashSet; // Importação da classe HashSet para manipular conjuntos sem elementos duplicados
import java.util.List; // Importação da interface List
import java.util.Objects; // Importação da classe utilitária Objects para comparações e geração de hash
import java.util.Set; // Importação da interface Set
import java.util.stream.Collectors; // Importação da classe Collectors para recolher dados de Streams

import com.fasterxml.jackson.annotation.JsonProperty; // Importação da anotação JsonProperty para mapeamento JSON
import com.fasterxml.jackson.annotation.JsonProperty.Access; // Importação da enumeração Access para controlar acessos de leitura/escrita no JSON

import jakarta.persistence.CollectionTable; // Importação da anotação CollectionTable para configurar a tabela auxiliar do JPA
import jakarta.persistence.Column; // Importação da anotação Column para mapear colunas da base de dados
import jakarta.persistence.ElementCollection; // Importação da anotação ElementCollection para coleções de elementos simples
import jakarta.persistence.Entity; // Importação da anotação Entity para indicar uma entidade JPA
import jakarta.persistence.FetchType; // Importação da enumeração FetchType para definir a estratégia de carregamento
import jakarta.persistence.GeneratedValue; // Importação da anotação GeneratedValue para estratégia de geração de ID
import jakarta.persistence.GenerationType; // Importação da enumeração GenerationType para os tipos de geração de chave primária
import jakarta.persistence.Id; // Importação da anotação Id para identificar a chave primária
import jakarta.persistence.OneToMany; // Importação da anotação OneToMany para relacionamentos de um-para-muitos
import jakarta.persistence.Table; // Importação da anotação Table para especificar o nome da tabela
import jakarta.validation.constraints.NotBlank; // Importação da anotação NotBlank para validar campos não vazios
import jakarta.validation.constraints.Size; // Importação da anotação Size para validar limites de tamanho
import tarefas02.api.models.enums.ProfileEnums; // Importação do enumerador de perfis do projeto

@Entity // Define que esta classe é uma entidade JPA mapeada para a base de dados
@Table(name = User.TABLE_NAME) // Define o nome da tabela na base de dados utilizando a constante da classe
public class User { // Declaração da classe pública User

    public static final String TABLE_NAME = "user"; // Constante estática pública com o nome da tabela "user"

    public interface CreateUser {} // Interface de marcação para aplicar validações específicas na criação
    public interface UpdateUser {} // Interface de marcação para aplicar validações específicas na atualização

    @Id // Marca o atributo 'id' como chave primária da entidade
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura a geração do ID como auto-incremento da base de dados
    @Column(name = "id", unique = true) // Mapeia a coluna "id" e garante que seja única
    private Long id; // Declaração do campo de identificador único do utilizador

    @Column(name = "username", length = 100, nullable = false, unique = true) // Mapeia a coluna "username" com tamanho máximo de 100, obrigatória e única
    @NotBlank(groups = CreateUser.class) // Garante que o nome de utilizador não esteja em branco no grupo CreateUser
    @Size(groups = CreateUser.class, min = 2, max = 100) // Define o tamanho do nome de utilizador entre 2 e 100 caracteres no grupo CreateUser
    private String username; // Declaração do campo de nome de utilizador

    @JsonProperty(access = Access.WRITE_ONLY) // Define que a palavra-passe só pode ser escrita (não será devolvida na resposta JSON)
    @Column(name = "password", length = 60, nullable = false) // Mapeia a coluna "password" com tamanho máximo de 60 e não nula
    @NotBlank(groups = { CreateUser.class, UpdateUser.class }) // Garante que a palavra-passe não esteja em branco na criação e atualização
    @Size(groups = { CreateUser.class, UpdateUser.class }, min = 8, max = 60) // Define o tamanho da palavra-passe entre 8 e 60 caracteres
    private String password; // Declaração do campo da palavra-passe do utilizador

    @OneToMany(mappedBy = "user") // Define o relacionamento um-para-muitos mapeado pelo atributo "user" na classe Task
    @JsonProperty(access = Access.WRITE_ONLY) // Impede a serialização da lista de tarefas no JSON para evitar recursão infinita
    private List<Task> tasks = new ArrayList<>(); // Declaração e inicialização da lista de tarefas do utilizador

    @ElementCollection(fetch = FetchType.EAGER) // Mapeia a coleção de elementos simples e força o seu carregamento imediato (EAGER)
    @CollectionTable(name = "user_profile") // Especifica o nome da tabela auxiliar "user_profile" que armazenará os perfis
    @Column(name = "profile", nullable = false) // Define o nome da coluna que armazenará os códigos dos perfis na tabela auxiliar
    private Set<Integer> profiles = new HashSet<>(); // Conjunto de inteiros para armazenar os códigos numéricos dos perfis

    public User() { // Construtor padrão (sem argumentos) exigido pelo JPA
        addProfile(ProfileEnums.USER); // Adiciona automaticamente o perfil de utilizador básico (USER) ao criar o objeto
    }

    public User(Long id, String username, String password) { // Construtor parametrizado para instanciar utilizador com dados iniciais
        this(); // Executa o construtor padrão para garantir a atribuição do perfil básico USER
        this.id = id; // Atribui o ID recebido ao atributo da instância
        this.username = username; // Atribui o nome de utilizador recebido ao atributo da instância
        this.password = password; // Atribui a palavra-passe recebida ao atributo da instância
    }

    public Long getId() { // Método getter para aceder ao valor do ID
        return this.id; // Devolve o valor do ID
    }

    public void setId(Long id) { // Método setter para alterar o valor do ID
        this.id = id; // Atualiza o atributo id com o valor passado por parâmetro
    }

    public String getUsername() { // Método getter para aceder ao nome de utilizador
        return this.username; // Devolve o nome de utilizador
    }

    public void setUsername(String username) { // Método setter para alterar o nome de utilizador
        this.username = username; // Atualiza o atributo username com o valor passado por parâmetro
    }

    public String getPassword() { // Método getter para aceder à palavra-passe
        return this.password; // Devolve a palavra-passe
    }

    public void setPassword(String password) { // Método setter para alterar a palavra-passe
        this.password = password; // Atualiza o atributo password com o valor passado por parâmetro
    }

    public List<Task> getTasks() { // Método getter para aceder à lista de tarefas
        return this.tasks; // Devolve a lista de tarefas
    }

    public void setTasks(List<Task> tasks) { // Método setter para alterar a lista de tarefas
        this.tasks = tasks; // Atualiza o atributo tasks com a lista passada por parâmetro
    }

    public Set<ProfileEnums> getProfiles() { // Método getter para obter o conjunto de perfis convertidos para o enum ProfileEnums
        return this.profiles.stream() // Cria uma Stream a partir do conjunto de códigos inteiros
                .map(x -> ProfileEnums.toEnum(x)) // Converte cada código inteiro para o respetivo valor enum de ProfileEnums
                .collect(Collectors.toSet()); // Recolhe os enums convertidos e devolve um novo conjunto (Set)
    }

    public void addProfile(ProfileEnums profile) { // Método utilitário para adicionar um novo perfil ao utilizador
        this.profiles.add(profile.getCode()); // Extrai o código numérico do enum recebido e guarda-o no conjunto de perfis
    }

    @Override // Indica a sobrescrita do método equals da classe genérica Object
    public boolean equals(Object obj) { // Método para comparar se duas instâncias de User são iguais
        if (this == obj) // Verifica se as instâncias apontam para o mesmo endereço de memória
            return true; // Devolve verdadeiro se for a mesma referência
        if (obj == null || getClass() != obj.getClass()) // Verifica se o objeto comparado é nulo ou pertence a outra classe
            return false; // Devolve falso caso as classes sejam diferentes ou o objeto seja nulo
        User other = (User) obj; // Realiza o cast (conversão) do objeto para a classe User
        return Objects.equals(this.id, other.id) && // Compara a igualdade dos atributos id
               Objects.equals(this.username, other.username) && // Compara a igualdade dos atributos username
               Objects.equals(this.password, other.password); // Compara a igualdade dos atributos password
    }

    @Override // Indica a sobrescrita do método hashCode da classe genérica Object
    public int hashCode() { // Método para gerar um código hash para a instância
        return Objects.hash(this.id, this.username, this.password); // Gera e devolve o hash baseado nos atributos id, username e password
    }
}
    */

package tarefas02.api.models;

import java.util.ArrayList; // Importação da classe ArrayList para manipular listas dinâmicas
import java.util.HashSet; // Importação da classe HashSet para manipular conjuntos sem elementos duplicados
import java.util.List; // Importação da interface List
import java.util.Objects; // Importação da classe utilitária Objects para comparações e geração de hash
import java.util.Set; // Importação da interface Set
import java.util.stream.Collectors; // Importação da classe Collectors para recolher dados de Streams

import com.fasterxml.jackson.annotation.JsonProperty; // Importação da anotação JsonProperty para mapeamento JSON
import com.fasterxml.jackson.annotation.JsonProperty.Access; // Importação da enumeração Access para controlar acessos de leitura/escrita no JSON

import jakarta.persistence.CollectionTable; // Importação da anotação CollectionTable para configurar a tabela auxiliar do JPA
import jakarta.persistence.Column; // Importação da anotação Column para mapear colunas da base de dados
import jakarta.persistence.ElementCollection; // Importação da anotação ElementCollection para coleções de elementos simples
import jakarta.persistence.Entity; // Importação da anotação Entity para indicar uma entidade JPA
import jakarta.persistence.FetchType; // Importação da enumeração FetchType para definir a estratégia de carregamento
import jakarta.persistence.GeneratedValue; // Importação da anotação GeneratedValue para estratégia de geração de ID
import jakarta.persistence.GenerationType; // Importação da enumeração GenerationType para os tipos de geração de chave primária
import jakarta.persistence.Id; // Importação da anotação Id para identificar a chave primária
import jakarta.persistence.OneToMany; // Importação da anotação OneToMany para relacionamentos de um-para-muitos
import jakarta.persistence.Table; // Importação da anotação Table para especificar o nome da tabela
import jakarta.validation.constraints.NotBlank; // Importação da anotação NotBlank para validar campos não vazios
import jakarta.validation.constraints.Size; // Importação da anotação Size para validar limites de tamanho
import tarefas02.api.models.enums.ProfileEnums; // Importação do enumerador de perfis do projeto
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = User.TABLE_NAME)
@AllArgsConstructor 
@NoArgsConstructor 
@Data 

public class User {

    public  static final String TABLE_NAME = "user";

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column (name = "username", length = 100, nullable = false, unique = true)
    @NotBlank 
    @Size(min = 2, max = 100)
    private String username;

    @JsonProperty(access = Access.WRITE_ONLY)
    @Column(name = "password", length = 60, nullable = false)
    @NotBlank 
    @Size (min = 8, max = 60)
    private String password;

    @OneToMany(mappedBy = "user")
    @JsonProperty(access = Access.WRITE_ONLY)
    private List<Task> tasks = new ArrayList<>();

    @ElementCollection(fetch =  FetchType.EAGER)
    @CollectionTable(name = "user_profile")
    @Column(name = "profile", nullable = false)
    private Set<Integer> profiles = new HashSet<>();

    public User(Long id, String username, String password){
        this.id = id;
        this.username = username;
        this.password = password;
        addProfile(ProfileEnums.USER);
    }

    public Set<ProfileEnums> getProfiles(){
        return this.profiles.stream()
            .map(x-> ProfileEnums.toEnum(x))
            .collect(Collectors.toSet());
    }

    public void addProfile(ProfileEnums profile){
        this.profiles.add(profile.getCode());
    }  
}