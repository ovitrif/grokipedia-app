# Wikipedia Mobile App Feature Specification

This document captures the comprehensive feature set of the official Wikipedia mobile apps for Android and iOS.

## Overview

- **Platforms**: Android (6.0+), iOS (16.6+)
- **Languages**: 300+ language editions, 60+ million articles
- **Distribution**: Play Store, App Store, F-Droid, Galaxy Store, Aptoide, Cafe Bazaar, GetJar
- **Ad-free**: Completely ad-free experience
- **Open Source**: Available on GitHub under Wikimedia

---

## 1. Navigation & Structure

### Bottom Navigation Bar
- **Explore**: Home feed with curated content
- **Places/Nearby**: Location-based article discovery
- **Saved**: Reading lists and saved articles
- **History**: Browsing history
- **Search**: Quick access to search

### Tabbed Browsing
- Open multiple articles in tabs
- Long-press links to open in new tab or background
- Tab overview for switching between articles
- Tabs remember scroll position
- Close all tabs at once
- Most frequently used feature after Search and Explore feed

### Table of Contents
- Persistent TOC for easy section navigation
- Shows current section while scrolling
- Quick jump to any section
- Collapsible on mobile for space

### Article Toolbar
- Table of contents access
- Language switcher
- Save to reading list
- Search within article
- Reading preferences (themes/text size)
- Share article

---

## 2. Explore Feed

### Content Cards
- **Featured Article**: Daily editor-selected article
- **Picture of the Day**: Daily featured image from Wikimedia Commons
- **Top Read/Trending**: Most-visited articles today
- **In the News**: Current events coverage
- **On This Day**: Historical events for today's date
- **Continue Reading**: Resume unfinished articles
- **Because You Read**: Personalized recommendations
- **Random Article (Randomizer)**: Discover random content

### Customization
- Hide specific cards
- Hide entire content types
- Reorder card types
- Filter by language
- Disable Explore feed entirely (makes Search home screen)
- Access via overflow menu (three dots)

---

## 3. Search

### Search Methods
- **Text Search**: Search bar at top of app
- **Voice Search**: Voice-enabled search on device
- **Emoji Search**: Search using emojis
- **Search Within Article**: Find text in current article

### Search Features
- **Personalized Results**: Incorporates open tabs, reading lists, search history
- **Recent Searches**: Quick access to previous searches
- **Search Suggestions**: Auto-complete as you type
- **Full-text Search**: Search article content, not just titles
- **Multi-language**: Search across different Wikipedia editions

---

## 4. Reading Experience

### Themes & Appearance
- **Light Theme**: Default white background
- **Sepia Theme**: Warm, paper-like background
- **Dark Theme**: Dark gray background
- **Black Theme (AMOLED)**: Pure black background for OLED screens
- **Image Dimming**: Reduce image brightness in dark modes
- **Match System Theme**: Automatically switch with device settings

### Text & Accessibility
- **Adjustable Text Size**: Multiple size options
- **Dynamic Type Support** (iOS): System-wide text size
- **VoiceOver Support** (iOS): Full screen reader support
- **Screen Brightness**: In-app brightness control

### Article Features
- **Link Previews**: Tap links to see preview cards
- **Image Gallery**: Full-screen image viewing with swipe
- **High-resolution Images**: Tap to view full resolution
- **References**: View citations and sources
- **External Links**: Access related websites
- **Categories**: Browse article categories
- **Talk Pages**: Access article discussions

---

## 5. Saved Content & Offline

### Reading Lists
- **Create Custom Lists**: Unlimited named lists with descriptions
- **Save Articles**: Bookmark any article to lists
- **Offline Access**: Downloaded articles available without internet
- **Cross-language**: Save articles from any Wikipedia edition
- **Sync Across Devices**: Logged-in users sync lists (privacy-preserving)
- **Used by 10.5%** of Android users (730K+ users)

### Syncing
- Enable in Settings > Article storage and syncing
- Requires login for sync
- Private to user (not publicly visible)
- Wikimedia doesn't profile users

### Browser Extension
- Save articles from desktop browsers (Chrome, Firefox, Safari)
- Syncs to mobile app reading lists

---

## 6. Places & Nearby

### Location Features
- **Nearby Articles**: Articles about locations near you
- **Distance Indicators**: Shows how far each place is
- **Compass Arrow**: Points direction to location
- **Map View**: Long-press to open location on map

### Map Integration
- **iOS**: Apple Maps
- **Android**: MapBox with WMF raster tiles

### Privacy
- Requires explicit location permission
- Can be disabled in device settings

---

## 7. History

### Browsing History
- Chronological list of viewed articles
- Search and filter history
- Clear all history option
- Delete individual entries
- Local to device (not synced)

---

## 8. User Account & Profile

### Profile Menu
- User Page access
- Talk Page access
- Notifications
- Watchlist
- Login/Logout
- Donate
- Settings

### Account Benefits
- Sync reading lists across devices
- Access notifications
- Edit articles
- View contribution history
- Hide fundraising banners

---

## 9. Watchlist

### Features
- Track changes to favorite articles
- Notifications for article edits
- Cross-platform (synced with web)
- Separate from Reading Lists
- Primarily for editing/monitoring

### Watchlist Expiry
- Watch articles temporarily (1 week, 1/3/6 months, 1 year)
- Auto-removes after period expires

---

## 10. Notifications

### Push Notifications
- **Echo Notifications**: Full notification system
- Off by default
- Enable in Settings > Push Notifications
- Supports Wikipedia, Wikidata, Commons

### Notification Types
- Messages on Talk page
- Mentions by other users
- Thanks from editors
- Edits to watched articles
- Edit reversions
- User rights changes
- Login attempts
- Reviews on created pages

### Email Notifications
- Optional email for talk page messages
- Configurable in preferences

---

## 11. Editing & Contributions

### Article Editing
- Edit articles directly from app
- Edit without account (IP or temporary account)
- View article edit history
- Access Talk pages

### Suggested Edits
- **Article Descriptions**: Add Wikidata descriptions
- **Article Images**: Add images to articles lacking them
- **Image Captions**: Caption uncaptioned Commons images
- **Image Tags**: Tag images for searchability
- **Edit Patrol**: Review recent edits, undo vandalism

### New Editor Features
- Homepage for new editors
- Assigned mentor (experienced Wikipedian)
- Impact module (shows view counts)
- Growth features for onboarding

### Contribution Tracking
- View personal edit history
- Edits tab in navigation
- Year in Review (iOS): Annual contribution summary

---

## 12. Sharing & Social

### Share Options
- Share article link
- Share to social media apps
- Share images from articles
- Copy URL to clipboard
- Open in external browser (Safari/Chrome)

### Facebook Integration
- Direct sharing to Facebook
- Share with other social platforms

---

## 13. Widgets (iOS)

### Home Screen Widgets
- **On This Day**: Historical event widget
- **Picture of the Day**: Daily featured image
- **Top Read**: Trending articles widget
- Updates daily
- Tap to open article

### Widget Sizes
- Multiple sizes available
- Add via long-press on home screen

---

## 14. Language Support

### Multi-language Features
- Search in 300+ languages
- Switch article language (interlanguage links)
- Add multiple app languages
- Customize Explore feed languages
- Reading lists across languages

### Content Translation
- Unified translation dashboard
- Section Translation for mobile
- Machine translation support (Google Translate, MinT, MADLAD-400)
- Supports 200+ languages

---

## 15. Games & Interactive Features

### Which Came First? Quiz (2025)
- Daily quiz game
- 5 questions per day
- Guess which historical event came first
- Available in 8+ languages
- Archive for previous days' games

### Discover Feature (2025)
- Weekly reading list recommendations
- A/B tested in Saved tab
- Personalized article suggestions

---

## 16. Donation & Support

### In-App Donation
- Access via Profile > Donate
- Multiple payment methods:
  - Credit/Debit cards (Visa, MC, Amex, Discover)
  - Apple Pay
  - Google Pay
  - PayPal
  - Venmo
  - Bank transfer
- Secure processing (no card storage)

### Fundraising Banners
- Appear periodically
- Hide for 1 week by closing
- Hidden for logged-in users
- "Already donated" option

### Donor Badge
- Limited trial feature
- Can be hidden if preferred

---

## 17. Settings & Configuration

### Reading Preferences
- Theme selection
- Text size
- Image dimming
- Show images setting

### Privacy Settings
- Clear search history
- Clear browsing history
- Crash reporting toggle
- Usage statistics toggle

### Article Storage
- Download settings
- Sync toggle
- Storage usage view
- Clear cached content

### Feed Customization
- Content type toggles
- Language preferences
- Card order

### Notifications
- Push notification toggles
- Notification types

### About & Support
- App version
- Terms of use
- Privacy policy
- Licenses
- Send feedback

---

## 18. Technical Features

### Performance
- Efficient caching
- Lazy image loading
- Offline-first architecture
- Background article download

### Device Integration
- **Handoff (iOS)**: Continue reading on other Apple devices
- **3D Touch/Haptic Touch**: Preview links
- **Deep Links**: Open articles from URLs
- **Share Extensions**: Save from other apps

### Debugging (Android)
- WebView debugging via Chrome DevTools (`chrome://inspect`)
- JavaScript console logging

---

## 19. Platform-Specific Features

### iOS-Only Features
- VoiceOver full support
- Dynamic Type support
- Handoff to other devices
- iMessage stickers
- Apple Maps integration
- Lock Screen widgets (potentially planned)
- Year in Review feature

### Android-Only Features
- MapBox maps (for non-Google devices)
- Multiple app stores availability
- Home screen shortcuts
- Edit patrol features
- More extensive suggested edits

---

## 20. API & Integration

### Wikimedia APIs Used
- Geosearch API (Nearby)
- Page content API
- Search API
- Feed API (daily content)
- Reading lists API
- Echo (notifications) API
- Action API (editing)

### External Integration
- Chrome/Firefox/Safari extensions for reading lists
- Wikipedia Preview (embeddable JS component)

---

## Statistics & Usage

- **63%** of English Wikipedia page views are from mobile (as of Sept 2025)
- **Reading Lists**: Used by ~10.5% of Android users
- **Tabs**: Most used feature after Search and Explore
- **Link Previews**: Led to 20% increase in links clicked

---

## Sources

- [Wikipedia App Store Listing](https://apps.apple.com/us/app/wikipedia/id324715238)
- [Wikipedia Play Store Listing](https://play.google.com/store/apps/details?id=org.wikipedia)
- [Wikimedia Apps - MediaWiki](https://www.mediawiki.org/wiki/Wikimedia_Apps)
- [Wikimedia Apps Android FAQ](https://www.mediawiki.org/wiki/Wikimedia_Apps/Android_FAQ)
- [Wikimedia Apps iOS FAQ](https://www.mediawiki.org/wiki/Wikimedia_Apps/iOS_FAQ)
- [Wikimedia Foundation News](https://wikimediafoundation.org/news/)
- [Diff Blog - Wikimedia](https://diff.wikimedia.org/)
- [Wikimedia Design Blog](https://design.wikimedia.org/blog/)
