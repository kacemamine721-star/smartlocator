# ZidCharge — Complete Design Strategy
### From Academic Prototype → App Store Tier-1 Product

> **Reference:** Tesla App (dark, premium, car-centric)  
> **Target:** A distinct North African EV identity — not a Tesla clone, not a generic map app  
> **Positioning:** The only EV app built for Tunisia. Raw infrastructure, real community, no compromise.

---

## 1. Brand Identity & Design DNA

### 1.1 The ZidCharge Concept

The name *Zid* (زد) means **"more" / "add" / "go further"** in Tunisian Arabic — this is the soul of every design decision. The app isn't just a locator; it's a statement that EV driving in Tunisia *is possible*, that the network *is real*, and that it *grows with you*.

The logo reinforces this: a **forward-slashing lightning bolt** with a **+** symbol inside a protective oval shield. The design language should inherit this: sharp geometry, energetic diagonals, a sense of forward momentum, and the warmth of the gold accent stopping the composition from feeling cold.

---

### 1.2 Brand Pillars

| Pillar | Meaning | Design Expression |
|---|---|---|
| **Forward** | Always moving, always further | Diagonal lines, forward-leaning typography, motion that exits right |
| **Grounded** | Built for Tunisia, not imported | Arabic numerals in subtle places, Maghreb-informed geometric ornament |
| **Precise** | Charging data must be trusted | Monospaced values, tight grid, never-rounded numbers |
| **Alive** | The network breathes in real time | Pulsing indicators, live status colors, animated availability |

---

### 1.3 Color System

Derived directly from the ZidCharge logo. Do not deviate.

```
/* ZidCharge Design Tokens */

/* Primary Palette */
--zid-black:       #0A0C0E   /* Near-black. App background. Deeper than Tesla's #000 */
--zid-surface:     #111418   /* Card and sheet surfaces */
--zid-surface-2:   #1A1F25   /* Elevated surfaces, modals */
--zid-border:      #252C34   /* Subtle borders */

/* Brand Colors — from logo */
--zid-teal:        #2EC4A0   /* Primary action, available status */
--zid-teal-dim:    #1A7A65   /* Teal at lower opacity for backgrounds */
--zid-gold:        #E8A930   /* Accent, warnings, premium elements */
--zid-gold-dim:    #7A5510   /* Gold background tint */

/* Status Colors */
--zid-available:   #2EC4A0   /* = zid-teal. Station available */
--zid-busy:        #E8A930   /* = zid-gold. Station occupied */
--zid-inactive:    #3D4550   /* Offline / unknown station */
--zid-error:       #E24B4A   /* Errors, alerts */

/* Typography Colors */
--zid-text-primary:    #F0F4F8
--zid-text-secondary:  #8A96A3
--zid-text-tertiary:   #4A5560
--zid-text-inverse:    #0A0C0E

/* Special */
--zid-glow-teal:   0 0 24px rgba(46, 196, 160, 0.35)
--zid-glow-gold:   0 0 16px rgba(232, 169, 48, 0.30)
```

**Color Usage Rules:**
- `--zid-black` is the canvas. Never use pure `#000000` — it feels fake on OLED.
- `--zid-teal` is reserved for **available**, **primary CTA**, and **active states only**. Not for decoration.
- `--zid-gold` is the surprise. Use it sparingly: warnings, premium badges, the SoC gauge needle, ratings stars. Its rarity is what makes it feel premium.
- Never use gradients between teal and gold — they muddy both. Separate them spatially.

---

### 1.4 Typography System

**Do not use Inter, Roboto, or any system font.**

```
Display / Headlines:  "Geist Mono"  (monospaced — kWh values, distances, speeds feel technical and precise)
Body / UI:            "DM Sans"     (warm, modern, highly legible at small sizes on dark backgrounds)
Arabic fallback:      "IBM Plex Arabic" (for bilingual support, Arabic numerals in subtle UI contexts)
Numbers (data):       "Geist Mono"  always — power values, SoC %, distances MUST be monospaced
```

**Type Scale:**

| Role | Size | Weight | Font | Usage |
|---|---|---|---|---|
| `--t-hero` | 48sp | 700 | Geist Mono | Onboarding headline |
| `--t-display` | 32sp | 600 | DM Sans | Screen titles |
| `--t-title` | 22sp | 600 | DM Sans | Card titles, station names |
| `--t-headline` | 17sp | 500 | DM Sans | Section headers |
| `--t-body` | 15sp | 400 | DM Sans | Primary body text |
| `--t-caption` | 13sp | 400 | DM Sans | Secondary info |
| `--t-label` | 11sp | 600 | DM Sans | Tags, chips, badges |
| `--t-data` | 28sp | 700 | Geist Mono | SoC%, kW values, timers |
| `--t-data-sm` | 17sp | 600 | Geist Mono | Distances, speeds |

**Typography Rules:**
- All power values (kW, kWh), percentages, distances (km), and timers → Geist Mono, always
- Station names → DM Sans 600, truncate with ellipsis at 1 line, never 2 lines in list items
- Never use ALL CAPS on Arabic text
- Letter-spacing: +0.5px on labels, 0 on everything else

---

## 2. Screen-by-Screen Design Specifications

### 2.1 Onboarding & Welcome

**Goal:** Communicate that ZidCharge is different in the first 3 seconds.

**Design Direction:** Full-bleed dark hero. No white background. The logo animates in — the lightning bolt draws itself along its diagonal path, then the oval traces around it. Below it: the tagline in DM Sans.

**Tagline options (choose one):**
- *"Charge. Explore. Go Further."*
- *"Tunisia's EV Network. In Your Hand."*
- *"زد شحنتك. زد مشوارك."* — (bilingual version, most memorable)

**Layout:**
```
┌─────────────────────────────┐
│                             │
│                             │  ← 40% black space
│       [ZidCharge Logo]      │  ← animated, centered, 96dp
│       with glow pulse       │
│                             │
│   "زد شحنتك. زد مشوارك."   │  ← DM Sans / IBM Plex Arabic
│   "Charge Further."         │     --zid-text-secondary
│                             │
│  ┌───────────────────────┐  │
│  │   SIGN IN             │  │  ← --zid-teal fill, DM Sans 600
│  └───────────────────────┘  │     16sp, 52dp height, 12dp radius
│                             │
│  ┌───────────────────────┐  │
│  │   CREATE ACCOUNT      │  │  ← border: --zid-border
│  └───────────────────────┘  │     text: --zid-text-primary
│                             │
│     Continue as Guest ›     │  ← --zid-text-tertiary, 13sp
│                             │
└─────────────────────────────┘
```

**Micro-interactions:**
- Logo entrance: bolt draws in 400ms (path animation), oval traces 300ms after, glow pulse every 4s
- Buttons: 80ms scale(0.97) on press, no ripple effect (ripple feels Android-generic; use scale instead)
- "Continue as Guest" has a `›` that slides 4px right on hover/focus

---

### 2.2 Registration Flow

**Reference the Tesla flow** (3-step, progressive disclosure) but make it feel native.

**Step Indicators:** 3 dots at top. Active dot = `--zid-teal` 8dp. Inactive = `--zid-border` 6dp. Completed = filled teal with a ✓ inside.

**Form Field Design:**
```
Label: DM Sans 11sp 600, --zid-text-secondary, letter-spacing +0.5px
Field: background --zid-surface-2, border 1px --zid-border
       border-radius 8dp, height 52dp, padding 16dp
       text: DM Sans 15sp, --zid-text-primary
Focus: border changes to --zid-teal (1.5px), subtle teal glow underneath
Error: border --zid-error, label turns red, shake animation 200ms
```

**Vehicle Profile Step (unique to ZidCharge):**
This is what Tesla doesn't have — and it's your killer onboarding differentiator. After email/password, the user picks their EV from a **scrollable horizontal card carousel**. Each card shows:
- Brand + model name (DM Sans 17sp 600)
- A clean side-view silhouette illustration (not a photo — SVG silhouette in `--zid-teal` tint)
- Range in km (Geist Mono, large)
- Connector badge: `CCS2` or `Type 2` pill

**This step is optional** ("Skip — Add later"), but gamified: "Set your car to unlock range-accurate routing."

---

### 2.3 Map Screen (Core)

This is the screen users spend 80% of their time on. It must be **instantly scannable** and **tactile**.

**Map Style:**
Use a custom MapLibre style — do not use OSM's default (it's too colorful and fights your dark UI).

```
Custom tile style rules:
- Background: #0A0C0E (matches --zid-black exactly)
- Roads: #1E2530 (major), #171C22 (minor)
- Road labels: #4A5560 DM Sans 10sp
- Water: #0D1520 (very dark blue, barely visible)
- Parks/nature: #111A14 (very dark green)
- Buildings: #141920
- All POI icons: hidden (you control what shows)
```

This creates a map that feels like a **custom-built HUD**, not a web map with dark mode toggled on.

**Station Markers:**

Three states, three designs. All markers are the same shield shape from the logo (the oval/bolt form):

```
AVAILABLE  → --zid-teal fill, white bolt icon, --zid-glow-teal shadow
              Size: 36dp normal, 44dp when selected
              Pulse animation: ring expands from marker every 3s

BUSY       → --zid-gold fill, white bolt icon, no pulse
              Badge: number of occupied ports (small red pill top-right)

INACTIVE   → --zid-inactive fill, 60% opacity, no animation
              Tooltip on tap: "Last verified: [date]"
```

**Clustering:** Below zoom level 12, markers cluster into a circle showing count. Cluster color: `--zid-teal` if all available, `--zid-gold` if any busy, `--zid-inactive` if all offline.

**Floating Elements:**

```
TOP BAR (not a navigation bar — transparent, floating):
┌─────────────────────────────────┐
│ 🔍 Search stations, cities...   │  ← DM Sans 15sp, --zid-surface blur bg
│                                 │     backdrop-filter: blur(20px)
└─────────────────────────────────┘

FILTER CHIPS (below search, horizontal scroll):
[ ⚡ Fast (DC) ] [ 🔌 Type 2 ] [ 🆓 Free ] [ ✅ Available ] [ 🏢 My Car ]
  ← DM Sans 11sp 600, pill shape, selected = --zid-teal bg
  ← "My Car" chip auto-filters to user's connector type

BOTTOM RIGHT FAB STACK:
  [📍] Location           ← --zid-surface-2, 48dp circle
  [🔋] SoC Input          ← taps open "Set Battery Level" sheet
  [🗺️] Range Ring toggle  ← enables reachability overlay
```

**Range Ring Overlay:**
When SoC is set + range ring is on: a filled circle on map showing reachable radius, using `rgba(46, 196, 160, 0.08)` fill and `--zid-teal` 1px dashed border. Stations outside the ring dim to 40% opacity automatically.

---

### 2.4 Station Preview Sheet (Bottom Sheet — Peek State)

Triggered when user taps a marker. Rises to 40% screen height. Draggable to full detail.

```
┌─────────────────────────────────────┐
│  ▬  (drag handle, centered, 4dp)    │
│                                     │
│  ⬤ AVAILABLE          Shell ›       │  ← status pill left, operator right
│                                     │
│  Shell Ben Arous                    │  ← DM Sans 22sp 600
│  Route de Sfax, Ben Arous           │  ← DM Sans 13sp secondary
│                                     │
│  ┌──────────┬──────────┬──────────┐ │
│  │  50 kW   │  CCS2    │  Public  │ │  ← Geist Mono 17sp + DM Sans 11sp label
│  │  Fast DC │ Type 2   │  Paid    │ │     3 data pills in a row
│  └──────────┴──────────┴──────────┘ │
│                                     │
│  ┌────────────────┐  ┌────────────┐ │
│  │  NAVIGATE  ›   │  │  ♡ Save    │ │  ← teal CTA + outlined secondary
│  └────────────────┘  └────────────┘ │
└─────────────────────────────────────┘
```

**Details to nail:**
- The status pill (`AVAILABLE` / `BUSY` / `OFFLINE`) uses Geist Mono 11sp, all-caps, with a 4dp dot before it
- Operator name is a tappable link → operator info screen
- The 3-stat row uses thin 1px `--zid-border` dividers, not card backgrounds
- `NAVIGATE` button has a `›` that animates 4px right on press

---

### 2.5 Station Detail Screen (Full Sheet or New Screen)

**Layout:** Scroll view. Header is a full-width map snippet (non-interactive, just showing station pin), then content below.

```
SECTION 1 — Identity
  Name (large), address, operator, network badge

SECTION 2 — Charger Inventory (the most important section)
  For each charger port, a row:
  ┌─────────────────────────────────────┐
  │  ● CCS2  ·  50 kW          BUSY    │
  │  ● Type2 ·  22 kW       AVAILABLE  │
  └─────────────────────────────────────┘
  Dot color = status color. kW in Geist Mono.

SECTION 3 — For My Car (personalized, if vehicle set)
  ┌─────────────────────────────────────┐
  │  🚗 Kia EV3                         │
  │  Max speed here: 50 kW              │
  │  10% → 80%: ~31 minutes            │  ← Geist Mono for values
  │  Connector: ✅ Compatible (CCS2)    │
  └─────────────────────────────────────┘
  This is UNIQUE — no other Tunisian app shows this.

SECTION 4 — Community
  Ratings (5-star, --zid-gold stars)
  Last check-in: "Yassine checked in 2h ago" (avatar initial + time)
  [ + I'm here now ] button

SECTION 5 — Photos
  Horizontal scroll of community photos
  [ + Add a photo ] card at end (camera icon)

SECTION 6 — Amenities nearby (OSM Overpass query)
  Café ·  Restaurant ·  Supermarket  (pill tags, 150m radius)

SECTION 7 — Report
  "Report an issue" → bottom sheet with radio options
```

---

### 2.6 SoC Trip Planner

Access via: search bar → "Plan a trip" OR the battery FAB on map.

```
Screen layout:

  FROM  [📍 Your location          ]
  TO    [🔍 Search destination...  ]
  🔋    [Battery: ──●────── 65%    ]  ← slider, Geist Mono shows % live

  [ PLAN ROUTE ]

  ↓ Results:

  ┌─────────────────────────────────┐
  │  ⚠️ Range insufficient          │
  │  Your Kia EV3 at 65% reaches   │  ← personalized
  │  ~390 km — Destination is      │
  │  450 km away.                  │
  │                                │
  │  Suggested stop:               │
  │  ┌──────────────────────────┐  │
  │  │ ⚡ Shell Ben Arous        │  │  ← tap → station detail
  │  │ 50 kW DC · ~18 min stop  │  │
  │  │ Charge to 80%: continue  │  │
  │  └──────────────────────────┘  │
  │                                │
  │  [ NAVIGATE WITH STOPS ]       │
  └─────────────────────────────────┘
```

---

### 2.7 Favorites Screen

**Not a list.** A **grid of cards**, 2 columns.

Each card:
- Station name (DM Sans 15sp 600)
- Status dot + label
- Distance from current location (Geist Mono 13sp, `--zid-gold`)
- Quick-navigate tap (whole card is tappable)
- Long-press to reorder or remove

Empty state: illustrated — the ZidCharge bolt icon, dimmed, with text "Save stations you love. Tap ♡ on any station."

---

### 2.8 History Screen

Timeline layout. Each session:

```
┌─────────────────────────────────────┐
│  May 14, 2026                       │  ← date header, DM Sans 11sp, tertiary
│                                     │
│  ┌───────────────────────────────┐  │
│  │ Shell Ben Arous          22h  │  │
│  │ 50 kW · 45 min · +82 kWh     │  │  ← Geist Mono for values
│  │ 18% → 80%  ████████░░░       │  │  ← mini SoC bar, teal fill
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

The SoC bar is the memorable UI detail here — shows before/after visually.

---

### 2.9 Profile Screen

```
TOP: Avatar (initials circle, --zid-teal bg) · Name · "EV Driver since [year]"

MY CAR SECTION:
  [Car illustration — SVG silhouette]
  Kia EV3 · 78 kWh · CCS2 + Type 2
  [ Change Vehicle ]

STATS (Geist Mono values):
  ┌──────────┬──────────┬──────────┐
  │  12      │  847 km  │  3       │
  │ Sessions │ Charged  │ Stations │
  └──────────┴──────────┴──────────┘

CONTRIBUTION BADGE:
  If user has contributed stations → gold badge "Network Builder 🏅"

SETTINGS LIST:
  Standard list rows with chevrons.
  Sections: Account · Notifications · Map Preferences · Language (AR/FR/EN) · About

SIGN OUT: Red text at bottom, no button — just text link. Destructive actions shouldn't look like actions.
```

---

### 2.10 Contribution Screen ("Add a Station")

**Multi-step form with a progress bar** (not dots — a continuous teal bar that fills).

```
Step 1: Location
  Full-screen map, user drags a pin to exact position
  Pin style: the ZidCharge shield marker in --zid-gold (pending state)
  "Hold and drag to adjust" tooltip fades after 2s

Step 2: Basic Info
  Station name, operator (typeahead), address confirmation

Step 3: Charger Details
  Connector type selector: visual grid of connector icons (CCS2, Type2, Schuko)
  Each connector: tap-to-select, teal border when selected
  Power input: number keyboard, Geist Mono, suffix "kW"
  Number of ports: +/- stepper

Step 4: Access
  Segmented control: Public / Semi-public / Private
  Paid / Free toggle
  Opening hours (optional)

Step 5: Photo
  Camera launch + existing photo picker
  Not required, but encouraged: "Stations with photos get 3× more check-ins"

Submit: [ SUBMIT FOR REVIEW ] → success animation (bolt icon shoots up, "Thank you! We'll verify and publish soon.")
```

---

## 3. Motion & Animation System

### 3.1 Core Principles

- **Physics-based:** Spring curves (stiffness 300, damping 28) for all position changes
- **Never decorative:** Every animation communicates state or direction
- **Duration budget:** Micro = 80–150ms, Transition = 250–350ms, Hero = 400–600ms

### 3.2 Animation Library

| Element | Animation | Duration | Easing |
|---|---|---|---|
| Screen enter | Slide up 20dp + fade in | 300ms | Spring |
| Screen exit | Slide down 10dp + fade out | 200ms | Ease-in |
| Bottom sheet rise | Slide up from 0 | 350ms | Spring (stiff) |
| Map marker appear | Scale 0→1 + fade | 200ms | Spring |
| Available marker pulse | Ring scale 1→1.8 + fade | 2000ms | Ease-out, infinite |
| Button press | Scale 0.97 | 80ms | Ease-out |
| Card press | Scale 0.98 + shadow shrink | 100ms | Ease-out |
| SoC bar fill | Width from 0 to value | 600ms | Ease-out with spring at end |
| Status pill change | Cross-fade | 200ms | Ease |
| Logo on onboarding | Bolt path draw + ring trace | 700ms | Custom bezier |

### 3.3 The Signature Moment

When a user sets their battery level and the range ring appears on map: the ring **expands from the user's dot** outward (from 0 to final radius, 500ms ease-out), while stations outside the ring **fade to 40% opacity simultaneously** (300ms ease-in, delayed 200ms). This is the moment users share on screenshots. Make it perfect.

---

## 4. Navigation Architecture

**Bottom Navigation Bar** — 4 tabs only. Never 5.

```
[ Map ]  [ Trip ]  [ Saved ]  [ Profile ]
  ↑        ↑          ↑           ↑
 Home    Planner   Favorites   Account
```

**Tab Bar Design:**
- Background: `--zid-surface` with `backdrop-filter: blur(24px)` + top border 1px `--zid-border`
- Active tab: icon in `--zid-teal` + label in `--zid-teal` DM Sans 11sp 600
- Inactive: icon + label in `--zid-text-tertiary`
- Active indicator: 2dp × 20dp `--zid-teal` pill above the icon (not the common underline — above is unusual and memorable)
- No badge numbers unless there's a notification

**Gesture Navigation:**
- All bottom sheets are draggable, snap to 40% / 90% / dismissed
- Swipe right on any detail screen → back
- Long-press the Map tab → opens SoC input directly (power-user shortcut)

---

## 5. Component Design Library

### 5.1 Buttons

```
PRIMARY:   --zid-teal bg, --zid-text-inverse text, DM Sans 15sp 600
           Height 52dp, border-radius 10dp, full-width or min 160dp
           Shadow: --zid-glow-teal on focus/hover

SECONDARY: transparent bg, 1.5px --zid-teal border, --zid-teal text
           Same sizing

GHOST:     No border, --zid-text-secondary text → teal on hover
           Used for: "Cancel", "Skip", text-only actions

DANGER:    --zid-error text, no background (ghost)
           Never a full red button — too alarming

ICON FAB:  48dp circle, --zid-surface-2 bg, 1px --zid-border
           Icon 20dp, --zid-text-secondary → teal when active
```

### 5.2 Cards

```
BASE CARD:
  background: --zid-surface
  border: 1px --zid-border
  border-radius: 14dp
  padding: 16dp
  shadow: 0 2px 12px rgba(0,0,0,0.4)

ELEVATED CARD (modals, sheets):
  background: --zid-surface-2
  Same border + radius

INTERACTIVE CARD (tap targets):
  Press state: background shifts to --zid-surface-2
  Scale: 0.98 on press
```

### 5.3 Status Pills

```
Structure: [dot 6dp] [label DM Sans 11sp 600] — no background, just dot + text
Available: --zid-teal dot
Busy:      --zid-gold dot
Inactive:  --zid-inactive dot

When used on cards with background: add matching 10% opacity bg behind the pill
```

### 5.4 Connector Badges

```
Pill shape, DM Sans 11sp 600, letter-spacing +0.5px
CCS2:  --zid-teal text, --zid-teal-dim background
Type2: #5B9BD5 text, rgba(91,155,213,0.15) background
Schuko: --zid-text-secondary, --zid-border background
```

### 5.5 Data Display (kW / km / %)

```
Always Geist Mono.
Large values (SoC gauge): 48sp 700
Medium values (station power): 22sp 700 + 13sp unit label
Small values (list distances): 15sp 600

Value/unit pairing rule:
  VALUE → Geist Mono, primary color
  unit  → DM Sans, secondary color, 60% the size
  e.g.: "50" [kW], "82" [%], "14.2" [km]
```

---

## 6. Iconography

**Do not use Material Icons or SF Symbols as-is.** Design or customize a small set of 20 icons in the ZidCharge style:

**Icon Style Rules:**
- 1.5px stroke, rounded caps and joins
- 24dp grid, 20dp content area
- Never filled — always outlined, except for status indicators
- Teal for active, `--zid-text-secondary` for inactive

**Required Custom Icons:**
- `zid-bolt` — the logo bolt, used as loading indicator
- `zid-station` — charging station silhouette
- `zid-ccs2` — CCS2 connector shape
- `zid-type2` — Type 2 connector shape
- `zid-range` — circle with car inside (range ring)
- `zid-battery` — battery with bolt, for SoC input
- `zid-checkin` — person + bolt (community check-in)

---

## 7. Empty & Error States

Never show a blank screen. Every empty state has a purpose.

| State | Illustration | Message | Action |
|---|---|---|---|
| No stations in view | Dimmed bolt icon, "zoomed out" visual | "Zoom in or search a city to see stations" | — |
| No favorites | Shield icon + heart | "Save stations you love — tap ♡ on any station" | — |
| No history | Bolt icon with clock | "Your charge sessions will appear here" | — |
| Trip impossible | Warning triangle | "No station found on this route. Contribute one?" | "Add Station" CTA |
| Network error | Disconnected bolt | "Couldn't load stations. Your cached map is shown." | "Retry" ghost button |
| GPS denied | Location pin with X | "Enable location to find stations near you" | "Open Settings" |

---

## 8. Accessibility Standards

- **Minimum touch target:** 44×44dp, no exceptions
- **Contrast ratios:** All text ≥ 4.5:1 against backgrounds. `--zid-teal` on `--zid-black`: 5.8:1 ✅
- **Font scaling:** All text uses `sp` units. Layouts tested at 85% and 150% font scale
- **Screen reader:** All icon buttons have `contentDescription`. Station status announced as "Shell Ben Arous, available, 50 kilowatts"
- **Motion sensitivity:** Check `reduceMotion` system setting — disable all looping animations, keep transitions under 150ms
- **Color independence:** Status is never communicated by color alone — always + icon or text label (critical for colorblind users)

---

## 9. App Store Presence

### 9.1 App Icon

The ZidCharge logo mark on `--zid-black` background. No text in the icon.

Variants:
- Default: teal bolt + gold accent on black (use the existing logo)
- Dark: same (already dark)
- Light (iOS requirement): teal bolt + gold accent on `#1A1F25`

Icon must not have rounded corners applied in artwork — the OS handles this.

### 9.2 Screenshots (Google Play & App Store)

**6 screenshots, each with device frame + caption:**

1. **Map** — "Every charging station in Tunisia" — shows the dark map with teal markers glowing
2. **Station Detail** — "Know before you go" — shows the "For My Car" personalized section
3. **Trip Planner** — "Go further, confidently" — shows route with charging stop
4. **Range Ring** — "See exactly how far you can go" — shows the overlay on map
5. **Community** — "Real drivers, real reports" — shows check-ins and photos
6. **Onboarding** — "Built for Tunisia's EV drivers" — shows the bilingual welcome

**Screenshot style:**
- Dark background, `--zid-black`
- Caption: DM Sans 28sp 600, white
- Sub-caption: DM Sans 17sp, `--zid-text-secondary`
- Teal accent element (underline, dot, or bracket) under the main caption word

### 9.3 App Store Copy

**Short description (80 chars):**
> Find, navigate & charge smarter — Tunisia's #1 EV companion.

**Long description (highlights):**
- Open with the pain: "Tunisia's EV charging network is real — but hard to find."
- Lead with the range ring and trip planner (unique features)
- Mention the vehicle-matched filtering
- Community: "Powered by EV drivers across Tunisia"
- Close: Arabic line — "زد شحنتك. زد مشوارك."

---

## 10. Development Handoff Checklist

Before moving to implementation, this document covers:

- [ ] Color tokens defined as XML `<resources>` in `colors.xml`
- [ ] Typography styles defined in `type.xml` with Geist Mono + DM Sans imported
- [ ] Custom MapLibre JSON style for dark map (derive from the token values above)
- [ ] Custom station marker SVG in 3 states (available, busy, inactive)
- [ ] Spring animation constants defined in a shared `AnimationConfig.kt`
- [ ] All empty states illustrated (can use simple SVG/Lottie)
- [ ] Bottom sheet behavior standardized across all screens (one BottomSheetBehavior config)
- [ ] Accessibility contentDescriptions on all interactive elements
- [ ] Icon set (20 icons) exported at 24dp in `drawable/` as vector XML
- [ ] `reduceMotion` system setting check implemented globally
- [ ] App icon exported at all required sizes (Play Store: 512×512, feature graphic 1024×500)

---

## 11. What Makes ZidCharge Unforgettable

A checklist of the design decisions that no other EV app in the region has:

1. **Vehicle-matched station filtering** — stations that don't fit your car are grayed out automatically
2. **"For My Car" panel** — exact charge time computed from your vehicle's DC max power
3. **The range ring** — a reachability circle that dims unreachable stations simultaneously
4. **Bilingual soul** — the tagline "زد شحنتك" roots the app in its geography, not imported design
5. **Geist Mono for all data** — makes kW values feel like engineering instruments, not just numbers
6. **The "pending" gold marker** — contributed (unverified) stations appear in gold, not teal, making verification visible
7. **SoC bar in history** — the before/after battery bar in session history is the screenshot-worthy detail
8. **Signature motion** — the range ring expanding from user position while stations dim is the one moment users will share

---

*ZidCharge Design Strategy v1.0 — May 2026*  
*Built for Tunisia. Designed to go further.*
