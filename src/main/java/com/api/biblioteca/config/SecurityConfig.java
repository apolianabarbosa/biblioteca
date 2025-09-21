package com.api.biblioteca.config;

import java.util.Arrays; // <-- IMPORT NECESSÁRIO
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
// Adicione este import
import org.springframework.security.config.Customizer; 
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
        http.cors(Customizer.withDefaults()) // <-- MUDANÇA 1 AQUI: Ativa a configuração de CORS definida no bean abaixo
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize

                // Rotas Públicas
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                .requestMatchers("/usuario/bem-vinda").permitAll()
                
                // <-- MUDANÇA 2 AQUI: Tornamos a listagem de livros pública por enquanto
                .requestMatchers(HttpMethod.GET, "/livros").permitAll() 

                // Rotas de Admin
                .requestMatchers(HttpMethod.GET, "/admin/listarUsuarios").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.GET, "/admin/buscar").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.GET, "/admin/filtrar/{role}").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.POST, "/livros/cadastrar").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.PUT, "/livros/atualizar/{id}").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.DELETE, "/livros/remover/{id}").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.GET, "/reservas/listarTodas").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.POST, "/emprestimos/registrar").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.GET, "/emprestimos").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.PUT, "/emprestimos/devolver/{id}").hasRole("BIBLIOTECARIO")
                .requestMatchers(HttpMethod.GET, "/multas").hasRole("BIBLIOTECARIO")

                // Rotas de duplo acesso(Usuário/Admin) - A rota /livros foi movida para cima
                .requestMatchers(HttpMethod.GET, "/usuario/meuPerfil").authenticated()
                .requestMatchers(HttpMethod.PUT, "/usuario/atualizarDados").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/buscar/titulo/{titulo}").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/buscar/autor/{autor}").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/buscar/isbn/{isbn}").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/filtrar/categoria").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/livros/filtrar/statusLivro").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/emprestimos/buscar/{id}").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/multas/buscar/{id}").hasAnyRole("BIBLIOTECARIO", "LEITOR")
                .requestMatchers(HttpMethod.GET, "/multas/filtrar/statusMulta").hasAnyRole("BIBLIOTECARIO", "LEITOR")

                // Rota de Usuário
                .requestMatchers(HttpMethod.POST, "/reservas/solicitar").hasRole("LEITOR")
                .requestMatchers(HttpMethod.GET, "/reservas/minhas").hasRole("LEITOR")
                .requestMatchers(HttpMethod.GET, "/emprestimos/usuario/{idUsuario}").hasRole("LEITOR")
                .requestMatchers(HttpMethod.PUT, "/multas/pagar/{id}").hasRole("LEITOR")
                .requestMatchers(HttpMethod.GET, "/multas/usuario/{idUsuario}").hasRole("LEITOR")

                // Todas as outras rotas exigem autenticação
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // <-- MUDANÇA 3 AQUI: Corrigimos a URL para a do nosso projeto Vite
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // <-- MUDANÇA 4 AQUI: Adicionamos o método "OPTIONS", importante para o CORS
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Adicionei esta linha também, importante para o envio de tokens
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}