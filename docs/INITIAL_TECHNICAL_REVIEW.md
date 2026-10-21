# DELIVIO — Technical Hardening Report

Date: 2026-10-21
Base branch: `main`
Working branch: `security-fixes/initial-audit`

This is a source-level hardening pass. The latest CI run must pass before merging. A CI build is not proof that Firebase rules, real payments, live GPS, production data, or legal/privacy operations work in the real world.

## Changes made on the working branch

### Authentication and authorization
- Removed the hard-coded administrator login bypass and automatic administrator provisioning from the login flow.
- New local password hashes use PBKDF2-HMAC-SHA256 with a random salt per password; legacy salted SHA-256 hashes can be verified and upgraded. Plaintext passwords are rejected.
- Removed the default plaintext password from the user entity and removed locally seeded customer/staff accounts. Seeded demo accounts from existing version-8 databases are removed by a targeted migration.
- Disabled the predictable client-side OTP reset and direct password changes until a trusted reset-token service exists.
- Disabled Google and phone-number login paths that previously trusted a supplied email/name or phone number without verified Firebase identity/SMS OTP.
- Added role checks to sensitive local management actions and prevented role switching into another account's dashboard.
- These checks are defence-in-depth only; a mobile app is not a trusted authorization boundary. Production permissions must be enforced by a server/Firebase Security Rules.

### Catalog and AI shopping
- AI instructions no longer tell the model to invent marketplace items or realistic prices.
- Product selection accepts exact catalog IDs rather than partial IDs/name matches.
- Unknown requests no longer produce a fabricated purchasable item.
- Final price, stock, availability, seller identity, delivery fee and order totals still need trusted server validation.

### Orders and customer data
- Order queries are scoped by customer, driver or store owner where applicable; customer order history and active-order views no longer query every order indiscriminately.
- Added a customer ownership field to order records with a Room migration.
- Address deletion is scoped to the owning user, and address/profile state follows the authenticated local user rather than a fixed demo account.
- Removed fabricated local tickets, reviews, addresses, demo profiles and promotional notifications.
- Checkout is deliberately disabled until a trusted backend can verify identity, prices, stock, payment and fulfillment. The app must not call a local-only order "confirmed".

### Support and operational claims
- Removed the invented support hotline action/number and unverified claims about 24/7 staffing, 15–25 minute delivery, guaranteed immediate refunds/reshipments, card security, and active courier locations.
- The support assistant no longer sends a Gemini key directly from the app or presents invented operational facts. AI-backed support should be proxied through a trusted backend using verified order data.
- Wallet top-ups and local compensation are disabled because no verified payment/refund backend is configured.
- Promotions are disabled until validated promotion rules are provided by a trusted source.

### Storage, privacy and build configuration
- Removed Room's destructive migration fallback. Added targeted migrations from version 8 to 10; unsupported older versions now fail rather than silently wiping the database. If versions below 8 were ever distributed, their upgrade path must be explicitly implemented before rollout.
- Disabled Android backup/data transfer for local app data containing personal information.
- Replaced the dummy Maps key with a build-time `GOOGLE_MAPS_API_KEY` value. A valid, Android-restricted key is still required for maps to work.
- CI now runs `testDebugUnitTest` before `assembleDebug`.
- Added password-hashing tests. CI results for the latest commit must be checked; earlier successful runs do not certify later changes.

## Remaining release blockers

1. **Trusted backend and identity:** configure the real Firebase project, Firebase Authentication, account provisioning, and server-side role enforcement. Google and SMS login remain disabled until verified.
2. **Orders:** implement a trusted order-creation endpoint/Cloud Function that validates catalog IDs, price, availability, fees, promo eligibility, user ownership, payment status and order-state transitions.
3. **Payments and wallet:** connect an actual payment provider, server-side webhooks/verification, refunds and ledger-backed balances. Never accept a client-side balance/paid flag as proof.
4. **Firestore rules:** inspect and deploy least-privilege rules for every collection; test cross-user read/write attempts. A source-level manager is not proof of safe cloud sync.
5. **GPS:** test real driver-device location, background permissions, stale/offline behavior and customer-device updates. Do not simulate a courier as a real person.
6. **Maps:** configure a valid restricted key through GitHub Actions secrets/build environment.
7. **Database:** implement migrations for any actually distributed schema versions below 8 before rollout.
8. **Production data:** replace or clearly label sample catalog records and verify merchant consent, pricing, stock, hours and delivery zones.
9. **Privacy/compliance:** complete a privacy notice, consent, data retention/deletion, document access controls, and Moroccan data-protection review.
10. **Release:** configure protected release signing with secrets, test a release build on physical devices, and verify app-store/privacy declarations.

## Important operational consequence

This hardening pass intentionally disables flows that previously simulated secure login, promotions, wallet funding, support guarantees or order confirmation without the necessary trusted services. The app may build successfully while those production services remain unavailable.

Do not merge or market the app as production-ready until the latest CI run passes and the external release blockers above are completed and tested.
