# Pack Your Bag

## Travel Organizer App

### 1. Project Introduction

The "Pack Your Bag" application is a comprehensive travel organizer designed to assist users in efficiently preparing for trips outside their home. Developed for Android using Java in Android Studio, the application provides users with a systematic way to create, manage, and track their packing lists. It aims to simplify the packing process and enhance the overall travel experience by ensuring users have all essentials and minimizing the likelihood of leaving items behind. 

It now operates on a premium subscription model powered by BdApps.

### 2. Justification

Traveling involves meticulous planning, and packing is a crucial aspect that can significantly impact the travel experience. The "Pack Your Bag" app addresses common challenges faced by travelers, such as forgetting essential items or overpacking. By offering a user-friendly platform to create and manage packing lists, the app aims to streamline the packing process, reduce stress, and ensure users are well-prepared for their journeys.

### 3. Features

#### Key Features

- **BdApps Premium Subscription:**
  - Mandatory subscription flow (2Tk/Day) with OTP verification.
  - Seamless onboarding via an animated landing page.
  - Secure state management using SharedPreferences.

- **Multi-Trip Management:**
  - Create and manage multiple trips simultaneously.
  - Track trip dates and dynamic countdowns for upcoming trips.

- **Smart Packing Lists:**
  - Interactive checklists spanning 12 distinct categories (Clothing, Electronics, Health, etc.).
  - Over 100+ pre-loaded items with progress tracking (circular progress indicators).
  - Add custom items, edit, or delete items as needed.

- **Weather-Based Recommendations:**
  - Automated packing suggestions based on the destination's real-time weather forecast.
  - Integrates seamlessly with Open-Meteo API.

- **Daily Smart Reminders:**
  - Set specific times for daily packing reminders leading up to your trip.
  - Notifies you of how many items are still pending.

### 4. Software Architecture & Toolchain

The development of the "Pack Your Bag" app leverages the following software toolchain:

- **Programming Language:** Java / Android SDK
- **Integrated Development Environment (IDE):** Android Studio
- **Database:** Room Database for local, offline-first storage of user trips and checklist items.
- **User Interface (UI/UX):** Modern Material Design components, immersive gradients, and responsive layouts.
- **Network / API Integration:** 
  - **Retrofit & Gson** for API communication.
  - **Open-Meteo API** for weather-based recommendations.
  - **Custom PHP Backend** for BdApps OTP and Subscription management.
- **Version Control:** Git for collaborative development.

This architecture ensures a robust, efficient, and highly scalable application that provides a premium user experience.

### 5. Backend Requirements

To run this application with the BdApps subscription integration, you must have a PHP backend server configured. 
The app points to a specific endpoint (e.g., `https://ruetandroiddevelopers.com/Mahadi(PYB)/`) and expects the following scripts:
- `send_otp.php`: To handle mobile number submissions and send the OTP via BdApps API.
- `verify_otp.php`: To verify the 6-digit OTP and complete the subscription.
- `unsubscribe.php`: To handle user unsubscription requests.

### 6. Installation & Setup

To clone and run this application locally:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/tem-mahadi/Pack_Your_Bag.git
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select `Open an existing Android Studio project`.
   - Navigate to the cloned directory and select it.
3. **Configure the Backend URL (Optional):**
   - If you have your own backend for BdApps integration, update the `BASE_URL` inside `RetrofitClient.java`.
4. **Build and Run:**
   - Sync the project with Gradle files.
   - Click the "Run" button or use `Shift + F10` to install it on your emulator or physical device.
     
### 8. License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
