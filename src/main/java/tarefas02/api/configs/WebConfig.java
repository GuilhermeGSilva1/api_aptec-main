package tarefas02.api.configs; // Pacote de configurações da aplicação

import org.springframework.context.annotation.Configuration; // Marca a classe como configuração do Spring
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Registro de mapeamento de políticas CORS
import org.springframework.web.servlet.config.annotation.EnableWebMvc; // Habilita o suporte ao Spring Web MVC
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Interface para customização do Spring MVC

@Configuration // Define que a classe é um componente de configuração gerenciado pelo Spring
@EnableWebMvc // Ativa as configurações padrão do Spring MVC
public class WebConfig implements WebMvcConfigurer { // Classe que define regras de CORS para a API

    // Método que configura as permissões de acesso entre origens cruzadas (CORS)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a regra de permissão para todas as rotas e endpoints da API
                .allowedOrigins("*") // Permite requisições vindas de qualquer origem (qualquer porta/domínio)
                .allowedHeaders("*") // Aceita qualquer cabeçalho HTTP enviado no request (incluindo Authorization)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT"); // Permite todos os métodos HTTP
    }
}