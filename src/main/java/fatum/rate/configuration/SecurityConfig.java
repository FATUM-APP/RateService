package fatum.rate.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Validation of the Cognito token and the access rules.
 *
 * <p>Anybody with an account can read a score and write its own rating; the party of the order that
 * owes the rating is checked against the order itself, not against a group, because the token does
 * not know who hired whom.</p>
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    public static final String ADMIN = "ADMIN";
    public static final String PROFESSIONAL = "PROFESSIONAL";
    public static final String VERIFIED = "VERIFIED";

    private final String issuerUri;

    public SecurityConfig(@Value("${app.security.issuer-uri}") String issuerUri) {
        this.issuerUri = issuerUri;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/rating").hasAnyRole(VERIFIED, PROFESSIONAL, ADMIN)
                        // --- READ ---
                        .anyRequest().hasAnyRole(VERIFIED, PROFESSIONAL, ADMIN))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    /**
     * Cognito publishes the keys at {@code {issuer}/.well-known/jwks.json}.
     *
     * <p>The URL is built by hand so the start-up does not depend on the discovery document.</p>
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = issuerUri.replaceAll("/+$", "") + "/.well-known/jwks.json";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(CognitoAuthorities::from);
        return converter;
    }
}
