# Smart Locator UI Presentation Notes

## 1. Project Context

For this stage of the Mobile Programming project, the objective is to design and implement the user interface of the application before developing the full functionality.

Our application is **Smart Locator**, a mobile app designed to help **EV drivers in Tunisia** quickly find nearby charging stations, check useful station information, and move easily between the main parts of the app.

At this stage, the focus is on:

- screen layout
- navigation flow
- readability
- consistency
- overall user experience

Full backend logic and live data integration are not the priority yet.

## 2. App Purpose

The goal of Smart Locator is to provide a simple and practical interface for electric vehicle users who need to:

- locate charging stations on a map
- view key station details
- save useful stations
- review history
- manage profile and preferences

Because this is a mobility app, the interface had to be **fast to read**, **easy to navigate**, and **clear while moving**.

## 3. Main Screens Included

The UI currently includes all major screens required for the first version of the app:

### Map Screen

This is the main screen of the app.

It includes:

- an interactive map
- EV charging station markers
- a floating search bar
- filter chips such as `Available`, `DC Fast`, `Type 2`, and `Open now`
- a station preview sheet at the bottom
- quick map action buttons

This screen is the center of the experience because the user’s primary action is discovering charging stations.

### Station Details Screen

This screen gives more information about a selected station.

It includes:

- station name
- availability status
- address
- power and connector information
- provider and pricing details
- quick action buttons
- a main call-to-action for navigation

This helps the user make a quick decision before going to a station.

### Favorites Screen

This screen allows users to access saved stations and organized collections.

It includes:

- a search area
- saved station cards
- curated categories such as favorite groups or charger collections

This improves convenience for users who often return to the same stations.

### History Screen

This screen is used to show recent activity and previous station interactions.

It helps the user quickly revisit previous destinations and gives continuity to the app experience.

### Profile Screen

This screen contains the personal area of the app.

It includes:

- user summary
- driver statistics
- settings and preferences

This screen supports personalization and account-related navigation.

### Alerts Screen

This screen is included as part of the app flow to support charging-related notifications and updates.

## 4. Navigation Flow

The navigation was designed to be simple and intuitive.

The app uses a **bottom navigation bar** to move between the main sections:

- Map
- Favorites
- History
- Profile

This choice was made because bottom navigation is familiar in mobile apps and gives fast access to the most important sections with minimal effort.

The user flow is:

1. Open the app on the **Map screen**
2. Explore nearby charging stations
3. Select a station to open its **details**
4. Save stations in **Favorites**
5. Review activity in **History**
6. Adjust personal settings in **Profile**

This flow matches the real needs of an EV user: discover, compare, decide, and return later.

## 5. Design Principles Applied

The UI was designed with basic and important design principles in mind.

### Alignment

Elements are aligned carefully to create order and visual clarity.

- titles and text blocks follow consistent alignment
- action buttons are grouped logically
- cards and sheets use structured spacing

### Spacing

Spacing was used to separate content and make the interface easier to scan.

- enough padding is used inside cards and sections
- margins separate important groups
- the layout avoids a crowded appearance

### Readability

Readability was important because users need to understand information quickly.

- clear typography hierarchy was used
- important information such as status, power, and distance is emphasized
- text sizes and contrast were chosen to remain legible on mobile screens

### Visual Consistency

Consistency was maintained across all screens by reusing the same design language.

- rounded cards and floating surfaces
- repeated color logic for status and actions
- similar button and chip styles
- consistent spacing and visual hierarchy

This makes the application feel unified instead of looking like separate unrelated pages.

## 6. UI Style Choices

The interface follows a modern map-first design.

The visual direction was chosen to feel:

- clean
- practical
- modern
- soft but professional

Some important choices include:

- floating surfaces over the map for a more immersive experience
- chip-based filters for quick recognition
- bottom sheets for contextual information without leaving the map
- clear call-to-action buttons for important actions like navigation

The map remains visible as much as possible because it is the most important visual element in this app.

## 7. Why These Design Choices Fit the App

The UI was designed specifically for an EV charging app, not as a generic interface.

These choices are useful because:

- EV users need quick access to location-based information
- station details must be easy to compare
- navigation between screens must be fast
- the interface should reduce confusion when the user is in transit

That is why the design gives priority to:

- map visibility
- short action paths
- simple navigation
- high information clarity

## 8. Current Scope of Implementation

At this stage, the project focuses mainly on the interface and experience.

What is already implemented:

- the main screens
- the layout structure
- navigation between sections
- a consistent visual design
- prototype station content for presentation

What is not yet the main focus:

- live charging data
- advanced filtering logic
- backend integration
- authentication
- full persistence

This is acceptable for the current milestone because the assignment focuses on UI design first.

## 9. Conclusion

The Smart Locator UI was designed to answer the needs of EV drivers with a clear and intuitive mobile experience.

The project already includes:

- all main screens
- a coherent navigation flow
- consistent visual styling
- basic mobile design principles

The design choices were made to support usability, clarity, and fast decision-making, which are essential in a charging-station locator app.

## 10. Short Oral Presentation Version

If a short explanation is needed during the presentation, the project can be introduced like this:

> Smart Locator is an EV charging station finder for Tunisia. For this first milestone, I focused on the user interface rather than full functionality. I designed the main screens of the app, including the map, station details, favorites, history, and profile. I used clear alignment, spacing, and consistent visual elements to make the app easy to use. My main design choice was to keep the map at the center of the experience, because users need fast access to nearby charging information. The result is a clean and intuitive UI that is ready for later functional development.
