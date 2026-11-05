package tarefas02.api.security; // Pacote das classes de segurança e filtros JWT da aplicação

import java.io.IOException; // Importação da exceção de I/O para manipuladores do servlet
import java.util.Objects; // Importação da classe utilitária de checagem de objetos nulos

import org.springframework.security.authentication.AuthenticationManager; // Gerenciador de autenticação do Spring Security
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Objeto que representa o token de autenticação no contexto
import org.springframework.security.core.context.SecurityContextHolder; // Armazenador do contexto global de segurança do usuário na requisição
import org.springframework.security.core.userdetails.UserDetails; // Interface do Spring Security que contém as credenciais do usuário
import org.springframework.security.core.userdetails.UserDetailsService; // Serviço de carregamento de detalhes do usuário no banco
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter; // Filtro base para autenticação HTTP

import jakarta.servlet.FilterChain; // Interface para dar continuidade à cadeia de filtros da requisição
import jakarta.servlet.ServletException; // Exceção genérica de Servlet
import jakarta.servlet.http.HttpServletRequest; // Objeto de requisição HTTP recebida
import jakarta.servlet.http.HttpServletResponse; // Objeto de resposta HTTP enviada

// Filtro de autorização que intercepta requisições e valida o token JWT enviado no cabeçalho Authorization
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    // Instância da classe utilitária JWT para verificação e extração de dados do token
    private final JWTUtil jwtUtil;

    // Instância do serviço de busca de usuário no banco para carregar suas permissões
    private final UserDetailsService userDetailsService;

    // Construtor que recebe o gerenciador de autenticação, utilitário JWT e serviço de usuário
    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
            UserDetailsService userDetailsService) {
        super(authenticationManager); // Inicializa a classe pai com o AuthenticationManager
        this.jwtUtil = jwtUtil; // Injeta o utilitário JWT
        this.userDetailsService = userDetailsService; // Injeta o serviço de detalhes do usuário
    }

    // Método que intercepta internamente todas as requisições HTTP recebidas na API
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Extrai o conteúdo do cabeçalho "Authorization" recebido na requisição HTTP
        String header = request.getHeader("Authorization");

        // Verifica se o cabeçalho existe e se inicia com o prefixo padrão "Bearer "
        if (Objects.nonNull(header) && header.startsWith("Bearer ")) {

            // Remove o prefixo "Bearer " (7 caracteres) obtendo apenas a string pura do token JWT
            String token = header.substring(7);

            // Valida o token e cria o objeto de autenticação do Spring Security
            UsernamePasswordAuthenticationToken auth = getAuthentication(token);

            // Se o token for válido e o objeto de autenticação for criado
            if (Objects.nonNull(auth)) {

                // Armazena o usuário autenticado no contexto global de segurança para o ciclo desta requisição
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Repassa a requisição HTTP para ser processada pelos próximos filtros da cadeia
        chain.doFilter(request, response);
    }

    // Método auxiliar privado para checagem do token e montagem do UsernamePasswordAuthenticationToken
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {

        // Verifica se o token JWT é assinado corretamente e se não está expirado
        if (this.jwtUtil.isValidToken(token)) {

            // Lê o username (subject) armazenado dentro do payload do token JWT
            String username = this.jwtUtil.getUsername(token);

            // Carrega as informações e autorizações (roles) do usuário cadastradas na base de dados
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Cria e retorna o objeto de autenticação com o usuário e suas permissões
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }

        // Retorna nulo se o token for inválido, alterado ou expirado
        return null;
    }
}
    

