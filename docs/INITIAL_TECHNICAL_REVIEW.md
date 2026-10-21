# Initial Technical Review — ALLO-AZIZ

Date: 2026-10-21
Base branch reviewed: `main`
Working branch: `security-fixes/initial-audit`

This review is based on source inspection. A successful build, device run, Firebase rules review, payment-provider verification, and production deployment have not yet been confirmed.

## Findings

### Critical — Authentication and authorization
- Removed the hard-coded administrator email/password bypass from `AlloAzizViewModel.loginStaff` in this patch; staff must pass normal password verification.
- `AlloAzizRepository` seeds a privileged SUPER_ADMIN account and other staff accounts using a known default password hashed with a fixed salt.
- Replaced new local password hashes with PBKDF2-HMAC-SHA256 and random per-password salts; legacy salted SHA-256 hashes can be verified and upgraded after successful login. This remains a local helper, not a substitute for server-managed production authentication.
- Role switching and local UI state are not a security boundary. Sensitive admin, support, merchant approval, driver activation, and order actions require server-side authorization.
- Disabled the fixed OTP (`842910`) reset flow and direct password change until a trusted backend can issue and verify expiring, single-use reset tokens. Password reset is temporarily unavailable.
- Some management actions modify local state without proving authorization at a trusted backend.

**Status:** Improved but not production-secure. The repository still seeds demo accounts with a known shared default password, and all roles/data remain locally controlled without a trusted server authorization boundary. Do not use the current build with real customer or staff accounts.

### Critical — AI product integrity
- The Gemini prompt instructed the model to invent a concierge product and a realistic price for requests from external marketplaces.
- The response parser previously matched products by partial names and partial IDs, which could select the wrong item.
- The offline fallback previously created a dynamic purchasable item at a fixed price for unrecognized requests.

**Initial patch:** prompt now forbids invented products/prices, model results are accepted only for exact catalog IDs, and unknown local requests no longer create a fabricated product. Server-side validation of current price, availability, stock, and final order total remains required.

### High — Maps configuration
- AndroidManifest contained a hard-coded dummy Google Maps key.
- Initial patch changes the manifest to read a build-time `GOOGLE_MAPS_API_KEY` environment variable. Builds without that variable will not have a functioning Google Maps key; configure a real, restricted key in GitHub Actions secrets or the build environment.

### High — Database migration
- Room uses `fallbackToDestructiveMigration()`, which can delete local data on an unsupported schema upgrade.
- Replace this with explicit migrations and test upgrade paths before production.

### High — Firebase/cloud synchronization
- A Firestore synchronization manager exists, but repository source alone does not prove that Firebase project configuration, authentication, security rules, and cross-device synchronization work correctly.
- Review and test Firestore rules with least-privilege access for customers, drivers, restaurants, support, and administrators.

### High — Payments and order integrity
- A payment-provider key is empty by default; the presence of a payment-method field does not prove payment processing works.
- Payment confirmation, order totals, discounts, refunds, and wallet balances must be verified on a trusted backend. Do not trust totals or role values sent by a mobile client.

### High — Tracking
- Location update code exists, but live GPS updates between separate customer and driver devices have not been tested here.
- Test permission denial, background restrictions, connectivity loss, stale coordinates, and distinguish simulated positions from real GPS.

### High — Privacy and backup
- User, address, identity/document, and delivery-location data are represented in local models.
- Backup is enabled and the backup-rule files are largely templates. Review what sensitive data can be backed up or transferred, obtain location consent, define retention/deletion, and publish an appropriate privacy notice.

### Medium — CI and release
- Workflow previously built a debug APK without running unit tests.
- Initial patch adds `testDebugUnitTest` before assembling the debug APK.
- CI result is still pending; this patch has not yet been built or tested by GitHub Actions.
- Release signing and release-mode verification remain separate work.

### Medium — Branding and demo data
- Repository naming and README use ALLO AZIZ while the Android app resource/build code uses DELIVIO.
- Seeded stores, sample reviews, promotions, driver profiles, service promises, and support answers may look real. Mark demo data clearly or replace it with verified operational data before launch.

## Changed in this initial patch
- Harden AI shopping against invented products and partial-ID matching.
- Remove fabricated local concierge products for unmatched requests.
- Replace the dummy Maps key with build-time configuration.
- Change the admin password test to use a hash and add unit tests to CI.
- Remove the hard-coded staff administrator login bypass, use PBKDF2 hashes with per-password salts for new local hashes, upgrade verified legacy hashes, and disable the predictable client-side password reset.

## Remaining release blockers
1. Replace local/hard-coded staff authentication and default privileged accounts with secure account provisioning and server-side authorization.
2. Replace fixed OTP password reset with a secure verified flow.
3. Replace destructive Room migration with explicit migrations.
4. Validate Firebase rules, payment processing, and GPS tracking end-to-end.
5. Add tests for authorization, unknown product requests, invalid model output, prices/stock, and order state transitions.
6. Configure a restricted Maps key and secure release signing in GitHub Actions.
7. Review privacy and production data, and verify the app's legal/operational claims.

No production-readiness claim should be made until these blockers are resolved and verified.