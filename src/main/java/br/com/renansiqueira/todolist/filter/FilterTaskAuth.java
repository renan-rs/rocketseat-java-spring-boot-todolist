package br.com.renansiqueira.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.renansiqueira.todolist.user.IUserRepository;
//import jakarta.servlet.Filter;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository UserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();
        //if(servletPath.equals("/tasks/")){
        if(servletPath.startsWith("/tasks/")){
            // Pegar a antenticação (usuario e senha)
            var authorization = request.getHeader("Authorization");
            // Remove "Basic " do base64(usuario e senha)
            var authEncoded = authorization.substring("Basic".length()).trim();
            //System.out.println("Authorization");
            //System.out.println(authEncoded);
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);
            var authString = new String(authDecode);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            //System.out.println("Authorization");
            //System.out.println(username);
            //System.out.println(password);

            // Validar usuário
            var user = this.UserRepository.findByUsername(username);
            if(user == null){
                //response.sendError(401, "Usuário sem autorização");
                response.sendError(401);
            } else {
                // Validar senha
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if(passwordVerify.verified){
                    // Segue viagem
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }
    
}

/*@Component
public class FilterTaskAuth implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // Executar alguma ação
        System.out.println("Chegou no filtro");
        chain.doFilter(request, response);
    }
    
}*/
