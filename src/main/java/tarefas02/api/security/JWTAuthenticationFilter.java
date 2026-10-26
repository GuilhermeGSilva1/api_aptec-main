package tarefas02.api.security;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import  org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tarefas02.api.models.User;


public class JWTAuthenticationFilter  extends UsernamePasswordAuthenticationFilter{
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public JWTAuthenticationFilter (AuthenticationManager authenticationManager,JWTUtil jwtUtil){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/login");
    }

    @Override 
        public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException{
            try{
                User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
                 
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    user.getPassword(),
                    new ArrayList<>()
                );
                return this.authenticationManager.authenticate(authToken);
            } catch (IOException e){
                throw new RuntimeException("Erro ao processar as credencias de autenticação",e);
            }
        }

        @Override 
        protected  void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain, Authentication authResul) throws IOException, ServletException{
                
                UserSpringSecurity userSS = (UserSpringSecurity) authResul.getPrincipal();
                String token = this.jwtUtil.generateToken(userSS.getUsername());

                response.addHeader("Autorization", "Bearer"+ token);
                response.addHeader("access-control-expos-headers","Authorization");

            }
          
}
            

