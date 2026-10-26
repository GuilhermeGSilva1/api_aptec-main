package tarefas02.api.configs; // Pacote de configurações da aplicação

import org.springframework.beans.factory.annotation.Autowired; // Anotação para injeção automática de dependências
import org.springframework.context.annotation.Bean; // Indica que o método retorna um Bean gerenciado pelo Spring
import org.springframework.context.annotation.Configuration; // Marca a classe como uma classe de configuração Spring
import org.springframework.http.HttpMethod; // Enumeração com os métodos HTTP
import org.springframework.security.authentication.AuthenticationManager; // Interface do gerenciador de autenticação
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // Construtor de autenticação
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Habilita autorizações por anotações em métodos
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Construtor das regras de segurança HTTP
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Habilita o suporte de segurança Web do Spring
import org.springframework.security.config.http.SessionCreationPolicy; // Política de criação de sessões HTTP
import org.springframework.security.core.userdetails.UserDetailsService; // Interface do serviço de dados de usuário
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Encodificador de senhas BCrypt
import org.springframework.security.web.SecurityFilterChain; // Componente que encadeia os filtros de segurança

import tarefas02.api.security.JWTAuthenticationFilter; // Filtro customizado de autenticação no login
import tarefas02.api.security.JWTAuthorizationFilter; // Filtro customizado de autorização por token
import tarefas02.api.security.JWTUtil; // Utilitário para manipular tokens JWT

// Classe central de configuração da segurança do Spring Security
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // Guarda a referência do AuthenticationManager configurado
    private AuthenticationManager authenticationManager;

    // Injeta o serviço customizado de busca de usuários no MySQL
    @Autowired
    private UserDetailsService userDetailsService;

    // Injeta o utilitário JWT
    @Autowired
    private JWTUtil jwtUtil;

    // Define as URLs públicas para requisições com qualquer método HTTP
    private static final String[] PUBLIC_MATCHERS = {
        "/"
    };

    // Define as URLs públicas exclusivas para o método HTTP POST
    private static final String[] PUBLIC_MATCHERS_POST = {
        "/user",
        "/login"
    };

    // Configura a cadeia de filtros de segurança HTTP da aplicação
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Obtém o construtor do gerenciador de autenticação compartilhado pelo Spring
        AuthenticationManagerBuilder authenticationManagerBuilder = 
                http.getSharedObject(AuthenticationManagerBuilder.class);

        // Define o UserDetailsService e o codificador BCrypt para validar o login
        authenticationManagerBuilder
                .userDetailsService(this.userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder());

        // Constrói o AuthenticationManager e armazena na variável
        this.authenticationManager = authenticationManagerBuilder.build();

        // Desabilita a proteção CSRF, desnecessária em APIs REST Stateless que utilizam JWT
        http.csrf(csrf -> csrf.disable());

        // Configura a gestão de sessão para STATELESS (não cria sessão HTTP no servidor)
        http.sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // Define as regras de autorização de requisições HTTP
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll() // Libera criação de usuário e login
            .requestMatchers(PUBLIC_MATCHERS).permitAll() // Libera a rota raiz
            .anyRequest().authenticated() // Exige token JWT válido para todas as outras rotas
        );

        // Seta o gerenciador de autenticação construído
        http.authenticationManager(this.authenticationManager);

        // Adiciona o filtro de login (JWTAuthenticationFilter) para escutar /login
        http.addFilter(new JWTAuthenticationFilter(this.authenticationManager, this.jwtUtil));

        // Adiciona o filtro de autorização (JWTAuthorizationFilter) para interceptar todas as requisições com token
        http.addFilter(new JWTAuthorizationFilter(this.authenticationManager, this.jwtUtil, this.userDetailsService));

        // Retorna a cadeia de filtros compilada
        return http.build();
    }

    // Registra o Bean do codificador BCryptPasswordEncoder para ser usado no projeto
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}