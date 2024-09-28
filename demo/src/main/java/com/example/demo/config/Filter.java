package com.example.demo.config;

import com.example.demo.entity.Account;
import com.example.demo.exception.NotFoundException;
import com.example.demo.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class Filter extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    HandlerExceptionResolver resolver;
    //những ai yêu cầu đến đường dẫn nay có thể truy cập
    private final List<String> AUTH_PERMISSION = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/login",
            "/api/register"
    );

    public boolean checkIsPulbicAPI(String uri) {
        //uri :/api/register
        //nếu gặp những api trong list trên thì cho phép truy cập => return true
        AntPathMatcher pathMatcher = new AntPathMatcher();
        //nếu ko => check token => return false
        return AUTH_PERMISSION.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //check xem api người dùng yc có phải là public api ko?
        boolean isPulbicAPI = checkIsPulbicAPI(request.getRequestURI());
        if (isPulbicAPI) {
            //cho pheps truy cap
            filterChain.doFilter(request, response);
        } else {
            //kiem tra dinh danh = token
            String token = getToken(request);
            if (token == null) {
                //ko dc phep truy cap
                resolver.resolveException(request, response, null, new NotFoundException("You do not have sufficient access rights! (Empty token)"));
                return;
            }
            // => co token => check xem token dung hay ko => lay thong tin tu token
            Account account;
            try {
                account = tokenService.getAccountByToken(token);
            } catch (ExpiredJwtException e) {
                //response token het han
                resolver.resolveException(request, response, null, new NotFoundException("Expired token"));
                return;
            } catch (MalformedJwtException malformedJwtException) {
                //token sai
                resolver.resolveException(request, response, null, new NotFoundException("Invalid token"));
                return;
            }
            //=>token chuan => cho phep truy cap
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    account
                    , token
                    , account.getAuthorities());
            //=> luu lai thong tin token
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            //token ok, cho login
            filterChain.doFilter(request, response);
        }
    }

    public String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.substring(7);
    }
}
