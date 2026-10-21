# DELIVIO — AI-Assisted Delivery App Prototype

DELIVIO is an Android/Kotlin prototype for a delivery marketplace with Arabic, French and English UI and an AI-assisted shopping flow.

## Current verified scope

- Android app built with Kotlin and Jetpack Compose.
- Local catalog browsing and rule-based natural-language matching/cart preparation based on exact catalog product IDs. Remote Gemini and external voice APIs are disabled until a trusted backend proxy is configured.
- Local Room persistence and user-scoped customer order/address/profile queries.
- GitHub Actions workflow that runs unit tests before assembling a debug APK.
- Build-time configuration for a restricted Google Maps API key.

## Important status: not production-ready

The current repository does not yet provide a complete trusted backend. To avoid presenting simulated behavior as real, the working hardening branch intentionally disables:
- Order confirmation/checkout until a trusted server validates identity, prices, stock, payment and fulfillment.
- Wallet top-ups, refunds/compensation, and promotional discounts.
- Google login and phone login until Firebase identity/SMS OTP is verified.
- Password reset until a secure server-issued, expiring reset token is available.
- Courier simulation, live order-state transitions and GPS sharing until authenticated backend synchronization is configured.
- Partner/driver onboarding until KYC documents can be handled securely on a backend.

The catalog data still needs to be verified against actual contracted merchants, prices, stock, hours and delivery zones. Do not represent sample catalog records or ratings as real partner data.

## Security notes

- New local password hashes use PBKDF2-HMAC-SHA256 with random per-password salts. This local helper is not a substitute for server-managed production authentication.
- Role checks in the Android client are defence-in-depth only. Admin, driver, merchant and customer permissions must be enforced by backend authorization and Firebase Security Rules.
- Android backup is disabled because the app models personal addresses, identity information and delivery locations.
- Room migrations preserve unsupported old data rather than silently wiping it. The supported migration path currently starts at schema version 8; earlier distributed versions need explicit migration work before rollout.
- A real Android-restricted `GOOGLE_MAPS_API_KEY`, Firebase project/configuration, reviewed collection-specific Firestore rules, server-side AI proxy, payment integration, and protected release-signing secrets must be configured before a production release. The checked-in `firestore.rules` is deny-all by design and must not be treated as a complete production ruleset.

## Build

Use the repository's GitHub Actions workflow to run unit tests and assemble a **debug** APK. A successful debug build is not proof of production readiness or external-service integration.

## Project branding

Android app display name: **DELIVIO**. The repository was previously named ALLO AZIZ.

## License

Copyright © 2026 Abdelaziz Chefnaj. All rights reserved. Commercial use or resale requires permission from the rights holder.
