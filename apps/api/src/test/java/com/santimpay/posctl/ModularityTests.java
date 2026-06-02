package com.santimpay.posctl;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * The architecture-fitness test (ADR-001). Fails the build if module boundaries are violated —
 * e.g. a module reaching into another module's {@code domain}/{@code infrastructure} instead of its
 * published events/interfaces. Also regenerates the C4 component docs + module canvas under
 * {@code target/spring-modulith-docs}.
 */
class ModularityTests {

    static final ApplicationModules MODULES = ApplicationModules.of(PosctlApplication.class);

    @Test
    void verifiesModuleBoundaries() {
        MODULES.verify();
    }

    @Test
    void writesDocumentation() {
        new Documenter(MODULES)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
