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

//@Component
//public class Filter extends OncePerRequestFilter {
//    @Autowired
//    TokenService tokenService;
//    @Autowired
//    @Qualifier("handlerExceptionResolver")
//    HandlerExceptionResolver resolver;
//    //những ai yêu cầu đến đường dẫn nay có thể truy cập
//    private final List<String> AUTH_PERMISSION = List.of(
//            "/swagger-ui/**",
//            "/v3/api-docs/**",
//            "/swagger-resources/**",
//            "/api/login",
//            "/api/register",
//            "/api/forgot-password"
////            "/api/breed/**",
////            "/api/koi/**"
//    );
//
//    public boolean checkIsPulbicAPI(String uri) {
//        //uri :/api/register
//        //nếu gặp những api trong list trên thì cho phép truy cập => return true
//        AntPathMatcher pathMatcher = new AntPathMatcher();
//        //nếu ko => check token => return false
//        return AUTH_PERMISSION.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        //check xem api người dùng yc có phải là public api ko?
//        boolean isPulbicAPI = checkIsPulbicAPI(request.getRequestURI());
//        if (isPulbicAPI) {
//            //cho pheps truy cap
//            filterChain.doFilter(request, response);
//        } else {
//            //kiem tra dinh danh = token
//            String token = getToken(request);
//            if (token == null) {
//                //ko dc phep truy cap
//                resolver.resolveException(request, response, null, new NotFoundException("You do not have sufficient access rights! (Empty token)"));
//                return;
//            }
//            // => co token => check xem token dung hay ko => lay thong tin tu token
//            Account account;
//            try {
//                account = tokenService.getAccountByToken(token);
//            } catch (ExpiredJwtException e) {
//                //response token het han
//                resolver.resolveException(request, response, null, new NotFoundException("Expired token"));
//                return;
//            } catch (MalformedJwtException malformedJwtException) {
//                //token sai
//                resolver.resolveException(request, response, null, new NotFoundException("Invalid token"));
//                return;
//            }
//            //=>token chuan => cho phep truy cap
//            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
//                    account
//                    , token
//                    , account.getAuthorities());
//            //=> luu lai thong tin token
//            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//            //token ok, cho login
//            filterChain.doFilter(request, response);
//        }
//    }
//
//    public String getToken(HttpServletRequest request) {
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null) return null;
//        return authHeader.substring(7);
//    }
//}
@Component
public class Filter extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    HandlerExceptionResolver resolver;

    // Public API paths for GET requests
    private final List<String> PUBLIC_GET_APIS = List.of(
            "/api/breed/**",
            "/api/koi/**"
    );

    // Public API paths regardless of the method (like Swagger and auth endpoints)
    private final List<String> AUTH_PERMISSION = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/login",
            "/api/register",
            "/api/forgot-password",
            "/api/login-google"
    );

    // Check if the request is a public GET API
    public boolean isPublicGetAPI(HttpServletRequest request) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        String uri = request.getRequestURI();

        // Allow public access only for GET requests to the defined paths
        return "GET".equalsIgnoreCase(request.getMethod()) &&
                PUBLIC_GET_APIS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    // Check if the request is a public API (like Swagger, login, or register)
    public boolean isPublicAPI(String uri) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return AUTH_PERMISSION.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // Check if the request is a public API (Swagger, login, register)
        if (isPublicAPI(uri) || isPublicGetAPI(request)) {
            // Allow public access
            filterChain.doFilter(request, response);
        } else {
            // Check token for non-public APIs
            String token = getToken(request);
            if (token == null) {
                // No token provided, return an error
                resolver.resolveException(request, response, null, new NotFoundException("You do not have sufficient access rights! (Empty token)"));
                return;
            }

            // Validate token and get the account
            Account account;
            try {
                account = tokenService.getAccountByToken(token);
            } catch (ExpiredJwtException e) {
                // Token has expired
                resolver.resolveException(request, response, null, new NotFoundException("Expired token"));
                return;
            } catch (MalformedJwtException malformedJwtException) {
                // Invalid token
                resolver.resolveException(request, response, null, new NotFoundException("Invalid token"));
                return;
            }

            // Set authentication if the token is valid
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    account, null, account.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Proceed with the filter chain
            filterChain.doFilter(request, response);
        }
    }

    // Helper to extract token from Authorization header
    public String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7); // Skip the "Bearer " prefix
    }
}

