package fatum.rate.configuration;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;

/**
 * The groups of the pool arrive in the {@code cognito:groups} claim.
 *
 * <p>Here they become Spring roles: {@code PROFESSIONAL} becomes {@code ROLE_PROFESSIONAL}, which is
 * what {@code hasRole} reads.</p>
 */
public final class CognitoAuthorities {

    public static final String GROUPS_CLAIM = "cognito:groups";

    private CognitoAuthorities() {
    }

    public static Collection<GrantedAuthority> from(Jwt jwt) {
        List<String> groups = jwt.getClaimAsStringList(GROUPS_CLAIM);
        if (groups == null) {
            return List.of();
        }
        return groups.stream()
                .map(group -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + group))
                .toList();
    }
}
