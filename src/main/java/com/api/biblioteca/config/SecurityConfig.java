package com.api.biblioteca.config;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        AuthenticationProvider authenticationProvider
    ) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // Rotas Públicas
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                .requestMatchers("/usuario/bem-vinda").permitAll()

                // Rotas de Admin
                .requestMatchers(HttpMethod.GET, "/admin/listarUsuarios").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.GET, "/admin/buscar").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.GET, "/admin/filtrar/{role}").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.POST, "/livros/cadatrar").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.PUT, "/livros/atualizar/{id}").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.DELETE, "/livros/remover/{id}").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.GET, "/reservas/listarTodas").hasRole("BIBLIOTECARIO")

                // Rotas de duplo acesso(Usuário/Admin)
                .requestMatchers(HttpMethod.GET, "/usuario/meuPerfil").authenticated()
                .requestMatchers(HttpMethod.PUT, "/usuario/atualizarDados").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/buscar/titulo/{titulo}").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/buscar/autor/{autor}").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/buscar/isbn/{isbn}").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/filtrar/categoria").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/filtrar/statusLivro").hasAnyRole("BIBLIOTECARIO", "LEITOR")

                // Rota de Usuário
                .requestMatchers(HttpMethod.POST, "/reservas/solicitar").hasRole("LEITOR")
                .requestMatchers(HttpMethod.GET, "/reservas/minhas").hasRole("LEITOR")

                // Todas as outras rotas exigem autenticação (LEITOR ou BIBLIOTECARIO)
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ADAPTAÇÃO: Coloque aqui a URL do seu frontend quando tiver um
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET","POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}
