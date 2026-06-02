/**
 * Shared kernel — the only module every other module is allowed to depend on.
 *
 * <p>Declared {@code OPEN} so that its types (base domain classes, outbox, security, web error
 * handling, configuration) are usable across module boundaries without each sub-package needing an
 * explicit named interface. Keep this module small and stable: it is the platform's common language,
 * not a junk drawer. Business logic does not belong here.
 */
@org.springframework.modulith.ApplicationModule(
        type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.santimpay.posctl.shared;
