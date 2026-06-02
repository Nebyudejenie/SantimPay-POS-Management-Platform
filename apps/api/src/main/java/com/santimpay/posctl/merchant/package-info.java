/**
 * Merchant bounded context — merchants and (as separate aggregates) their owners, branches and
 * settlement accounts. Reference implementation of the standard module layering
 * ({@code domain → application → infrastructure → web}); copy this shape for new modules.
 *
 * <p>Allowed dependencies: {@code shared} (OPEN). Other modules integrate with merchant ONLY via the
 * events published from {@code merchant.events} (e.g. {@code MerchantActivated}) — never by importing
 * {@code merchant.domain} or {@code merchant.infrastructure}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Merchant")
package com.santimpay.posctl.merchant;
