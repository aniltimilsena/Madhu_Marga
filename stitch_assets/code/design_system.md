# Design System: Beekeeping Management System

## Style Guidelines
## Brand & Style

This design system is built on the principles of **Organic Professionalism**. It bridges the gap between the raw, tactile world of apiculture and the precision required for modern agricultural data management. The aesthetic is clean and grounded, emphasizing clarity and ease of use in high-glare outdoor environments. 

The target audience ranges from hobbyist beekeepers to commercial apiary managers. Consequently, the UI must feel both approachable and authoritative. By utilizing a minimalist layout with subtle tactile cues—such as soft shadows and organic curves—the system evokes a sense of calm and reliability, mirroring the steady patience required for hive maintenance.

## Layout & Spacing

The layout philosophy follows a **Fluid Grid** model with a heavy emphasis on "Safe Touch Zones." Given that beekeepers may use the app while wearing thin gloves or in erratic outdoor conditions, the spacing rhythm is intentionally spacious.

A 12-column grid is used for desktop views, collapsing to a single column for mobile. Gutters are fixed at 16px to maintain a tight relationship between related data, while external margins are kept wide (minimum 20px) to prevent interactive elements from hugging the bezel of the device. All component heights are multiples of the 8px base unit to ensure a consistent vertical rhythm.

## Elevation & Depth

To maintain a "Professional Nature" feel, elevation is conveyed through **Tonal Layers** and **Ambient Shadows** rather than harsh outlines. Surfaces use a "stacked" logic: the base apiary map or dashboard is the lowest level (Level 0), while data entry cards sit on Level 1 with a very soft, diffused amber-tinted shadow.

We avoid heavy blacks in shadows to prevent a "dirty" look; instead, shadows use a low-opacity Deep Forest Green tint (#064E3B at 8% opacity). This creates an organic depth that feels like a leaf casting a shadow on the ground. Modal overlays use a subtle backdrop blur to keep the user focused on the task without losing the context of the hive they are inspecting.

## Components

### Buttons
Primary action buttons are high-contrast: Amber background with Charcoal text. They feature a minimum height of 48px to accommodate outdoor use. Secondary buttons use a Forest Green outline with a clear background, maintaining a professional profile without competing for visual dominance.

### Status Badges (Hive Health)
Badges are pill-shaped and utilize a "Light Fill + Dark Text" combination. For example, a "Healthy" hive displays a soft green background with a dark green label. These must always include an accompanying icon (e.g., a checkmark or alert triangle) to ensure accessibility for color-blind users.

### Data Entry Cards
Cards are the primary container for hive inspections. They feature a 1px soft border in a light sage-gray and a Level 1 shadow. Headers within cards are always Forest Green to provide a clear anchor. Input fields within these cards feature large hit areas and prominent labels that never disappear (floating labels), ensuring beekeepers always know what data point they are recording.

### Progress Indicators
Used for honey production or queen cell development, these indicators use a thick, rounded "honey-fill" track. The track is a pale amber, and the progress is a saturated amber, creating a tactile, "filling" sensation.

### Tactile Toggles
Switches and checkboxes are oversized. Toggles use a physical sliding metaphor with a distinct Amber "on" state, providing clear visual feedback even in low-light or high-glare environments.

## Theme Tokens

### Colors
- **Primary**: #8d4b00
- **Secondary**: #2b6954
- **Tertiary**: #665f3d
- **Background**: #f9f9ff
- **Surface**: #f9f9ff
- **Error**: #ba1a1a

### Typography (Lexend)
- **Headline Large**: 32px, 700 weight, 40px line height
- **Headline Medium**: 24px, 600 weight, 32px line height
- **Body Large**: 18px, 400 weight, 28px line height
- **Body Medium**: 16px, 400 weight, 24px line height

### Shapes
- **Corner Roundness**: Round Eight (8px base)
