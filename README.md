# WASLIXA - Food Delivery & Local Services Super-App Platform

## 📌 Overview
**WASLIXA (وصلـيكسا)** is a comprehensive, scalable food delivery and local e-commerce super-app platform designed to streamline operations between customers, couriers, and partner merchants across Morocco and globally. Featuring a modern Material 3 architecture, real-time GPS tracking, conversational AI shopping assistant, and dedicated multi-role portals (Customer, Driver Partner, Restaurant Merchant, and Super Admin).

## 🌟 Unique Selling Proposition (USP)
### 🤖 AI-Powered Shopping Assistant (Waslixa AI)
* **Conversational Voice & Text Ordering:** Users can speak or chat naturally in Moroccan Darija, Arabic, French, or English.
* **Authoritative Catalog Matching:** Real-time semantic matching strictly against verified merchant menus and inventory—no hallucinated or invented products/prices.
* **Smart Cart Preparation:** The AI extracts exact quantities and preparation notes, calculates real subtotals, and prepares the cart for user confirmation.
* **One-Tap Checkout:** Seamless review and confirmation with multiple payment options (Cash on Delivery, WASLIXA Pay Wallet, Bank Card).

## 🚀 Key Features
* **Multi-Role RBAC:** Secure access separation for Customers, Drivers, Store Owners, Support Agents, and Super Admin.
* **Real-Time GPS Tracking:** Live map and telemetry tracking for customers to follow couriers from kitchen to doorstep.
* **Store & Driver Portals:** Dedicated dashboards to accept orders, manage culinary menus, toggle product availability, and monitor earnings.
* **Cloud & Local Synchronization:** Seamless two-way sync between SQLite Room Database and Google Cloud Firestore.
* **Localized Moroccan Experience:** Full RTL support, multi-language switching (Arabic, French, English), and local payment methods.

## 🛠️ Tech Stack
* **Language & UI:** Kotlin, Jetpack Compose, Material Design 3.
* **Architecture:** MVVM, Coroutines, StateFlow, Room Database, Cloud Firestore, Firebase Authentication.
* **AI & Voice:** Google Gemini API with fallback NLP matching and Android Text-to-Speech / Speech Recognizer.
* **Maps & Geo:** Google Maps SDK, Android FusedLocationProviderClient.

## 📄 License & Copyrights
Copyright © 2026 WASLIXA Technologies. All rights reserved.
Protected under applicable intellectual property laws.
