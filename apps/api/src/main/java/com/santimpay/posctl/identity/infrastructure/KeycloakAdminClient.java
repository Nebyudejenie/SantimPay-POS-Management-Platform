package com.santimpay.posctl.identity.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin adapter over the Keycloak Admin REST API, used so a Super Admin can provision users from
 * inside the app (the platform's system of record for humans is Keycloak, not the local DB).
 *
 * <p>Authenticates as the {@code posctl-admin-cli} confidential service account via the
 * {@code client_credentials} grant (its secret comes from Vault in prod). All calls require the
 * caller to already hold {@code user:manage} — enforced at the controller.
 *
 * <p>Production behaviour: created users get a <b>temporary</b> password and must reset it +
 * (optionally) enrol MFA on first login. No plaintext passwords are stored anywhere.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private final ObjectMapper mapper;

    @Value("${posctl.keycloak.base-url:http://keycloak:8081/auth}")
    private String baseUrl;
    @Value("${posctl.keycloak.realm:posctl}")
    private String realm;
    @Value("${posctl.keycloak.admin-client-id:posctl-admin-cli}")
    private String adminClientId;
    @Value("${posctl.keycloak.admin-client-secret:posctl-admin-secret}")
    private String adminClientSecret;
    /** The resource-server client whose roles are the fine-grained permissions. */
    @Value("${posctl.keycloak.api-client-id:posctl-api}")
    private String apiClientId;

    private RestClient http() {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    private String adminToken() {
        JsonNode r = http().post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials&client_id=" + adminClientId
                        + "&client_secret=" + adminClientSecret)
                .retrieve().body(JsonNode.class);
        return r == null ? null : r.get("access_token").asText();
    }

    /** Create a user with a realm role + the given permission client-roles + a temporary password. */
    public String createUser(String username, String email, String firstName, String lastName,
                             String realmRole, List<String> permissions, String tempPassword) {
        String token = adminToken();
        var admin = RestClient.builder().baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();

        // 1) create the user (enabled, temp password, must reset on first login)
        Map<String, Object> body = Map.of(
                "username", username, "email", email, "firstName", firstName, "lastName", lastName,
                "enabled", true, "emailVerified", true,
                "credentials", List.of(Map.of("type", "password", "value", tempPassword, "temporary", true)));
        admin.post().uri("/admin/realms/{realm}/users", realm)
                .contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().toBodilessEntity();

        // 2) look the user up to get its id
        JsonNode found = admin.get()
                .uri("/admin/realms/{realm}/users?username={u}&exact=true", realm, username)
                .retrieve().body(JsonNode.class);
        if (found == null || !found.isArray() || found.isEmpty()) {
            throw new IllegalStateException("User not found after create: " + username);
        }
        String userId = found.get(0).get("id").asText();

        // 3) assign the realm role
        JsonNode role = admin.get()
                .uri("/admin/realms/{realm}/roles/{role}", realm, realmRole)
                .retrieve().body(JsonNode.class);
        admin.post().uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", realm, userId)
                .contentType(MediaType.APPLICATION_JSON).body(List.of(role))
                .retrieve().toBodilessEntity();

        // 4) assign the permission client-roles (of posctl-api)
        String apiCid = clientUuid(admin, apiClientId);
        var roleReps = permissions.stream().map(p -> {
            JsonNode cr = admin.get()
                    .uri("/admin/realms/{realm}/clients/{cid}/roles/{r}", realm, apiCid, p)
                    .retrieve().body(JsonNode.class);
            return cr;
        }).toList();
        if (!roleReps.isEmpty()) {
            admin.post().uri("/admin/realms/{realm}/users/{id}/role-mappings/clients/{cid}", realm, userId, apiCid)
                    .contentType(MediaType.APPLICATION_JSON).body(roleReps)
                    .retrieve().toBodilessEntity();
        }
        log.info("Provisioned Keycloak user {} (role {}, {} perms)", username, realmRole, permissions.size());
        return userId;
    }

    private String clientUuid(RestClient admin, String clientId) {
        JsonNode arr = admin.get()
                .uri("/admin/realms/{realm}/clients?clientId={cid}", realm, clientId)
                .retrieve().body(JsonNode.class);
        return arr.get(0).get("id").asText();
    }
}
