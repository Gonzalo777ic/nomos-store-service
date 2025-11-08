package com.nomos.store.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 🛑 CLAIM PERSONALIZADO DE AUTH0: Debe coincidir con el claim de roles que usa Auth0
    private static final String ROLES_CLAIM = "https://nomosstore.com/roles";

    /**
     * Define la cadena de filtros de seguridad para el servicio de Ventas (Store).
     * Convierte el servicio en un Resource Server.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configuración de CORS y CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Establecer la política de sesión sin estado (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 🛑 RESTRICCIONES DE ACCESO BASADAS EN ROLES
                .authorizeHttpRequests(auth -> auth
                        // Permite a Vendedores y Administradores acceder a las funciones principales de Ventas (Crear/Editar)
                        .requestMatchers("/api/store/sales/**", "/api/store/promotions/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDOR")

                        // Permite a Clientes y Roles Internos consultar información
                        .requestMatchers("/api/store/quotations/client/**", "/api/store/sales/client/**").hasAnyAuthority("ROLE_CLIENT", "ROLE_ADMIN", "ROLE_VENDOR")

                        // El resto de peticiones debe estar autenticado
                        .anyRequest().authenticated()
                )

                // 4. 🛑 CONFIGURAR COMO RESOURCE SERVER CON JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Aplicar el conversor personalizado para extraer los roles
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /**
     * Crea un conversor personalizado para extraer los roles de los claims de Auth0.
     * **FIX:** Utiliza jwt.getClaim() para asegurar compatibilidad al obtener el List<String> de roles.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Define cómo extraer las autoridades (roles) del token
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Obtenemos el claim. Usamos getClaim() y casteamos a List<String>.
            Object rolesObject = jwt.getClaim(ROLES_CLAIM);

            if (rolesObject instanceof List) {
                // Casteo seguro después de la verificación.
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesObject;

                // Mapear cada rol a un SimpleGrantedAuthority (Spring Security espera este formato)
                return roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role))
                        .collect(Collectors.toList());
            }

            // Si el claim no existe o no es una lista, devuelve una lista vacía de autoridades.
            return Collections.emptyList();
        });
        return converter;
    }

    /**
     * Configuración básica de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Incluir todos los orígenes que pueden consumir este API.
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4000", "http://localhost:8081", "http://localhost:8080", "http://localhost:8082"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}