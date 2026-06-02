package com.santimpay.posctl;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.santimpay.posctl.support.IntegrationTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Contract parity gate. Boots the real app, fetches springdoc's runtime spec at {@code /v3/api-docs},
 * and asserts that every {@code operationId} declared in the hand-curated
 * {@code packages/contracts/openapi.yaml} is actually served by a controller. This catches the
 * classic drift: a controller renamed/removed while the published contract (and the generated TS/Dart
 * clients) still advertise the old operation. It does NOT fail on extra runtime operations not yet in
 * the curated spec — that direction is a softer warning (printed), since the curated file is the
 * client-facing subset.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContractParityTest extends IntegrationTest {

    @Autowired TestRestTemplate rest;

    private static final Path CURATED =
            Path.of("../../packages/contracts/openapi.yaml");

    @Test
    void everyCuratedOperationIsServedByTheRuntimeSpec() throws Exception {
        // Curated operationIds (source of truth for the clients).
        JsonNode curated = new YAMLMapper().readTree(Files.readString(CURATED));
        Set<String> curatedOps = operationIds(curated);
        assertThat(curatedOps).as("curated spec should declare operations").isNotEmpty();

        // Runtime spec served by springdoc.
        String runtimeJson = rest.getForObject("/v3/api-docs", String.class);
        JsonNode runtime = new ObjectMapper().readTree(runtimeJson);
        Set<String> runtimeOps = operationIds(runtime);

        Set<String> missing = new TreeSet<>(curatedOps);
        missing.removeAll(runtimeOps);

        Set<String> undocumented = new TreeSet<>(runtimeOps);
        undocumented.removeAll(curatedOps);
        if (!undocumented.isEmpty()) {
            System.out.println("[contract] runtime operations not in curated spec (add when stable): "
                    + undocumented);
        }

        assertThat(missing)
                .as("curated operationIds with no matching controller — the contract/clients are stale")
                .isEmpty();
    }

    private static Set<String> operationIds(JsonNode spec) {
        Set<String> ids = new TreeSet<>();
        JsonNode paths = spec.get("paths");
        if (paths == null) return ids;
        paths.forEach(path -> path.forEach(op -> {
            JsonNode id = op.get("operationId");
            if (id != null) ids.add(id.asText());
        }));
        return ids;
    }
}
