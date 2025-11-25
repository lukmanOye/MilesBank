# Miles Bank – The Future of Nigerian Digital Banking (2025)

**Live Backend:** https://milesbank-production.up.railway.app  
**Root Endpoint:** `GET /` → `{"message": "Miles Bank API is LIVE"}`

A complete, production-ready, full-stack digital banking backend built from scratch with Spring Boot 3 + Java 17.  
Inspired by Opay, Kuda & Moniepoint — but faster, cleaner, and ready for millions of users.

This is not a school project. This is a real bank backend.

---

### Core Features

| Feature                          | Status | Description |
|----------------------------------|--------|-----------|
| Registration + OTP Login         | Done   | Email + 6-digit OTP (real Gmail SMTP ready) → OTP printed clearly in Railway logs when `SKIP_EMAIL=true` (Railway blocks outbound SMTP on free tier, so we skip email in production to avoid crashes — industry-standard practice for demos & interviews) || Dual Currency Wallets (NGN + USD)| Done   | Separate balances, PIN protection |
| Virtual Debit Cards (NGN & USD)  | Done   | Create, view, top-up from wallet |
| Internal Transfers               | Done   | Instant Miles-to-Miles |
| Cross-Currency Transfers         | Done   | NGN ↔ USD with dynamic rates |
| External Transfers (All Nigerian Banks) | Done | Powered by Paystack |
| Name Enquiry & Bank Validation   | Done   | 219 Nigerian banks + Miles Bank internal |
| Bill Payments                    | Done   | Airtime, Data, Electricity, TV (DSTV, GOtv, Startimes) |
| Transaction History + PDF Receipts | Done | Professional downloadable receipts (iText7) |
| Profile Picture Upload           | Done   | Cloudinary integration |
| Admin Panel Endpoints            | Done   | Full control over users, wallets, rates |
| Exchange Rate Management         | Done   | Admin sets live USD/NGN rate |
| JWT + Role-Based Security        | Done   | USER & ADMIN roles |
| Full Swagger/OpenAPI Docs        | Done   | Interactive API playground |

---
### Live API Endpoints

#### Auth
POST   /api/auth/register                → Register + generate OTP
POST   /api/auth/login                   → Login with email + OTP
POST   /api/auth/resend-otp?email=...    → Resend OTP
POST   /api/auth/logout                  → Invalidate token

#### Wallets
POST   /api/wallets/create/ngn           → Create NGN wallet + set PIN
POST   /api/wallets/create/usd           → Create USD wallet + set PIN
GET    /api/wallets/my                   → Get both wallets
GET    /api/wallets/balances             → Current NGN & USD balances
PUT    /api/wallets/pin/ngn              → Change NGN PIN
PUT    /api/wallets/pin/usd              → Change USD PIN

#### Transactions
POST   /api/transactions/internal        → Miles → Miles (instant)
POST   /api/transactions/cross-currency  → NGN → USD or USD → NGN
POST   /api/transactions/external        → To any Nigerian bank (Paystack)
POST   /api/transactions/verify-name     → Name enquiry before transfer
GET    /api/transactions/my              → My transaction history
GET    /api/transactions/rates           → Current exchange rates
GET    /api/transactions/admin/all       → (Admin) All transactions

#### Virtual Cards
POST   /api/card/create/ngn              → Create NGN virtual card
POST   /api/card/create/usd              → Create USD virtual card
GET    /api/card/ngn                     → View NGN card details
GET    /api/card/usd                     → View USD card details
POST   /api/card/topup/miles/ngn        → Fund card from wallet

#### Bill Payments
POST   /api/bill/pay                     → Pay airtime/data/TV/electricity
GET    /api/bill/data-plans/MTN          → Get MTN data bundles
GET    /api/bill/tv-plans/DSTV           → Get DSTV packages
GET    /api/bill/history                 → Bill payment history

#### PDF Receipts & History
GET    /api/history/all                  → Full transaction history
GET    /api/history/pdf/transaction/123 → Download PDF receipt

#### Users & Profile
GET    /api/users/my-profile             → Get logged-in user details
PUT    /api/users/update-profile-picture → Upload photo (Cloudinary)
GET    /api/users/all                    → (Admin) List all users

#### Admin Only
GET    /api/admin/rates                  → View current rates
PUT    /api/admin/rates                  → Update USD/NGN rates
GET    /api/wallets/admin/all            → View all wallets

---

### Third-Party Services

| Service         | Purpose                          | Status       | Notes |
|-----------------|----------------------------------|--------------|-------|
| Paystack        | External transfers & bills       | Live (Test)  | Switched from NuBAPI (down) → Paystack is rock-solid |
| Gmail SMTP      | Email delivery                   | Optional     | Disabled on Railway (`SKIP_EMAIL=true`) |
| Cloudinary      | Profile picture storage          | Live         | Fast, secure, CDN |
| PostgreSQL      | Production database              | Live (Railway) | Auto-migration |
| iText7          | PDF receipt generation           | Done         | Bank-grade receipts |
| Luhn Algorithm  | Card number validation           | Done         | Industry standard |
| JWT + BCrypt    | Authentication & password hashing with 24-hour token expiration | Done | Military-grade security (BCrypt) + JWT tokens expire automatically after 24 hours |

> Paystack Test Mode: Maximum 3 real transactions/day (normal behavior). Switch to live key for unlimited.

---

### Tech Stack (Employer Magnet)

- Spring Boot 3.5.6 (Java 17)
- Spring Security + JWT
- Spring Data JPA + Hibernate
- PostgreSQL
- Maven
- Lombok
- Paystack SDK
- Cloudinary
- iText7 PDF
- Railway.app (Production hosting)

---

### Local Development

```bash
git clone https://github.com/lukmanOye/MilesBank.git
cd MilesBank

# Required environment variables
export JWT_SECRET=your_very_long_random_secret
export PAYSTACK_SECRET_KEY=sk_test_xxxxxxxxxx
export CLOUDINARY_CLOUD_NAME=yourname
export CLOUDINARY_API_KEY=123456789
export CLOUDINARY_API_SECRET=your_secret

./mvnw spring-boot:run



### Live API Endpoints

#### Auth
