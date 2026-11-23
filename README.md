# StayEg — India's #1 Smart PG Platform (Android App)

StayEg is India's most trusted paying guest (PG) ecosystem, rewritten as a modern, native Android application using **Kotlin** and **Jetpack Compose (Material Design 3)**.

---

## 🚀 Key Features Ported

### 🏠 Public & Tenant Discovery
- **Hero & City Search**: Search PGs by locality, landmark, or PG name across Bangalore, Mumbai, Delhi, Pune, Hyderabad, and Chennai.
- **Search Filters & Sorting**: Real-time filtering by Gender (Boys, Girls, Unisex), Budget Range Slider (₹5,000–₹30,000/mo), and Amenities (WiFi, AC, Meals, Laundry, Gym, Parking, CCTV, Power Backup).
- **PG Detail View**: High-resolution room galleries, verified badges, amenity grids, house rules, and detailed reviews breakdown.
- **Real-Time Bed Booking**: Interactive room & bed selector (Single, Double, Triple sharing), move-in date picker, coupon engine (`STAYEG500`, `FIRSTFREE`, `RENT10`), itemized price breakdown with Zero Brokerage guarantee, and instant booking confirmation.

### 👤 Tenant Self-Service Hub
- **Active Stay Management**: View room number, bed number, monthly rent, and stay overview.
- **Roommate Connect**: View roommates in the same room/PG with occupation and details.
- **Digital Wi-Fi Credentials**: Instant Wi-Fi SSID and password access with 1-tap copy.
- **Rent & Payments**: Upcoming rent due alerts, transaction history, and UPI/Card payment simulation with instant receipts.
- **Complaints & Support**: Raise maintenance, cleanliness, safety, or noise tickets with priority levels, track status (Open, In Progress, Resolved), and view caretaker resolution notes.

### 🏢 PG Owner & Host Portal
- **Executive Analytics Dashboard**: Real-time revenue overview (₹1,86,400/mo), 85% occupancy rate (19 of 24 beds occupied), pending dues tracker, and recent activity log.
- **Properties & Room Management**: Manage listed PGs, add rooms, and monitor bed availability.
- **Rent Roster & Reminders**: Tenant payment status tracking with 1-tap WhatsApp reminder triggers.
- **Complaint Triage**: Review tenant tickets, assign to staff/vendors (plumber, electrician), and submit resolution notes.
- **Digital QR Code Onboarding**: Fast tenant check-in at property entrance.

### 🤝 Community & Networking
- **Interest-Based Discussion Feed**: Post and read community threads across Roommates, Travel, Food, Local Tips, and Tech.
- **Interactive Likes & Comments**: Upvote tips and join discussions.
- **Local Groups**: Discover and join groups like *Bangalore PG Roommates*, *Bengaluru Foodies Club*, and *Weekend Trekkers*.

### 💳 Plans & Trust Framework
- **PG Owner Subscription Plans**: Starter (1 Year), Growth (2 Years), Enterprise (3 Years) with 15-day money-back guarantee.
- **First 1,000 Free Offer**: `FIRSTFREE` coupon support.
- **Digital KYC**: Aadhaar & PAN card verification with AES-256 encryption.
- **Policy & Safe Use Guidelines**: National emergency helplines (100, 108, 1091), tenant security checklist, transparent refund rules, and terms of service.

---

## 🛠️ Architecture & Tech Stack

- **Framework**: Native Android with Jetpack Compose (Material 3)
- **Language**: Kotlin 2.0.21
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Reactive State**: Kotlin Coroutines & `StateFlow`
- **Image Loading**: Coil 2.7.0 (`AsyncImage`)
- **Iconography**: Material Icons Extended
- **Design System**: StayEg Professional Blue (`#2563EB`, `#1E40AF`) + Gold Accent (`#C9A84C`)
- **Adaptive UI**: Edge-to-edge support with `statusBarsPadding()` and `navigationBarsPadding()`

---

## 📱 Navigation Structure

- `Landing`: Home discovery, trust metrics & featured PGs
- `Explore`: Multi-attribute search & filter sheet
- `PGDetail`: Property photos, bed picker, and reviews
- `Booking`: Checkout flow with coupon discount & payment
- `TenantHome`: Active stay dashboard & rent due alert
- `MyStay`: Room specs, Wi-Fi password & caretaker contact
- `Payments`: Dues & receipt history
- `Complaints`: Ticket triage & resolution notes
- `OwnerDashboard`: Revenue, occupancy & property operations
- `Community`: Discussion threads & local groups
- `Pricing`: PG owner subscription tiers
- `Profile`: KYC Aadhaar/PAN status & role switching
- `About`, `Contact`, `SafeUse`, `Terms`: Trust & emergency guidelines
