---
name: Krono Automation System
colors:
  surface: '#101319'
  surface-dim: '#101319'
  surface-bright: '#363940'
  surface-container-lowest: '#0b0e14'
  surface-container-low: '#191c22'
  surface-container: '#1d2026'
  surface-container-high: '#272a31'
  surface-container-highest: '#32353c'
  on-surface: '#e1e2eb'
  on-surface-variant: '#cbc3d7'
  inverse-surface: '#e1e2eb'
  inverse-on-surface: '#2e3037'
  outline: '#958ea0'
  outline-variant: '#494454'
  surface-tint: '#d0bcff'
  primary: '#d0bcff'
  on-primary: '#3c0091'
  primary-container: '#a078ff'
  on-primary-container: '#340080'
  inverse-primary: '#6d3bd7'
  secondary: '#4cd7f6'
  on-secondary: '#003640'
  secondary-container: '#03b5d3'
  on-secondary-container: '#00424e'
  tertiary: '#4edea3'
  on-tertiary: '#003824'
  tertiary-container: '#00a572'
  on-tertiary-container: '#00311f'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#d0bcff'
  on-primary-fixed: '#23005c'
  on-primary-fixed-variant: '#5516be'
  secondary-fixed: '#acedff'
  secondary-fixed-dim: '#4cd7f6'
  on-secondary-fixed: '#001f26'
  on-secondary-fixed-variant: '#004e5c'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#101319'
  on-background: '#e1e2eb'
  surface-variant: '#32353c'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 34px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.02em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system expresses disciplined technical power, procedural clarity, and cognitive calm for an advanced personal automation platform on Android. Blending Material 3 structural foundations with dark tactical minimalism, the visual environment treats logic rules, trigger conditions, and background execution routines as tactile, high-precision entities. 

The aesthetic is low-glare, dark-engineered, and intentional. Content hierarchy is established through surface tonal shifts and dynamic spectral accents rather than aggressive fills. Every component—from pipeline nodes to status pills—communicates reliability, deterministic execution, and state awareness.

## Colors

The palette is engineered specifically for deep-dark, OLED-friendly ambient environments:

- **Canvas & Surfaces:** Pure background starts at deep void `#0E1117`, stepped up to primary canvas `#161B22`. Structural container tiers use deliberate tonal steps: `#1C2128` (Container Low), `#22272E` (Container Base), and `#2D333B` (Container High).
- **Primary (Electric Violet / Indigo `#8B5CF6`):** Reserved for orchestrator logic, app-level controls, primary interactive states, and rule flow initiation.
- **Secondary (Cyan `#06B6D4`):** Designates runtime triggers, input sensors, event listeners, and incoming parameters.
- **Tertiary (Emerald Green `#10B981`):** Represents successful rule passes, execution actions, outputs, and active background daemons.
- **Outlines & Delimiters:** Use a constant structural boundary of `rgba(255, 255, 255, 0.08)` to `rgba(255, 255, 255, 0.12)`, preventing light bleed while maintaining edge definition between stacked containers.

## Typography

Typography balances ergonomic Android readability with technical precision. Primary body text and headlines leverage **Inter** for its neutral geometry, optical clarity, and robust rendering at small pixel grids. 

For syntax tokens, execution arguments, node metadata, status metrics, and rule logs, **JetBrains Mono** is utilized. This introduces an authentic IDE and scriptor feel without degrading readability. Numeric figures, timestamps, and parameters must always use tabular figures (`tnum`) to maintain clean scan lines across operational views.

## Layout & Spacing

The layout is built around a dynamic fluid column system optimized for Android handhelds and foldables:

- **Mobile Viewports (<600dp):** Single column fluid canvas, 16dp outer screen margin, 16dp gutters between card blocks, and 8dp vertical rhythm between consecutive logic steps.
- **Foldables & Tablets (>=600dp):** 2 to 4 fluid columns or master-detail pane orientation with 24dp screen margins and 16dp gutters. The node canvas transitions from vertical stacking to freeform horizontal workflow charting.
- **Vertical Rhythm:** Dense operational layout. Tighter internal card padding (`space-sm` to `space-md`) preserves screen real estate for complex rule trees and multi-step condition chains.

## Elevation & Depth

Visual hierarchy does not use blur shadows or heavy drop shadows, which cause muddy rendering against dark OLED backgrounds. Depth is achieved strictly through:

1. **Material Surface Tiers:** Content elevation steps up incrementally (`#0E1117` → `#161B22` → `#1C2128` → `#22272E` → `#2D333B`). Floating action sheets and modals sit at the highest layer (`#2D333B`).
2. **Subtle Hairline Borders:** All elevated cards, logic blocks, and dialogs carry a 1px border at `rgba(255, 255, 255, 0.08)`. Hover or active execution states step up to `rgba(255, 255, 255, 0.20)` or tint with the operational accent color.
3. **Execution Glow:** When a rule triggers or an action fires, an ambient 8px–12px glow using the category accent (`#06B6D4` or `#10B981`) at 15% opacity highlights the card perimeter without obscuring text.

## Shapes

The design system employs a structured geometry:
- Standard surface containers, workflow cards, code inputs, and modal sheets use balanced `0.5rem` (8px) corners to preserve a crisp architectural profile.
- Micro-elements such as category filters, state indicators, trigger/action badges, and interactive chips use full pill shapes (`9999px`) to create distinction between container boxes and actionable tokens.

## Components

### Buttons
- **Primary Action:** Solid electric violet (`#8B5CF6`) background with white text, 8px corner radius, 40dp minimum touch height. Press state dims to 85% opacity.
- **Tonal / Secondary:** Elevated surface (`#2D333B`) with 1px border `rgba(255, 255, 255, 0.12)` and high-emphasis body text.
- **Node Connection Trigger:** Ghost buttons framed with dashed border tokens for appending triggers or actions inline.

### Chips & Badges
- **Status Pills:** Compact pill badge (`rounded-full`, 24dp height) with muted surface background and a 6px glowing indicator dot.
  - Cyan (`#06B6D4`): Trigger state (e.g., `GEOFENCE_ENTER`).
  - Violet (`#8B5CF6`): Logic operator (e.g., `AND`, `OR`, `DELAY`).
  - Emerald (`#10B981`): Action event (e.g., `SET_VOLUME_0`, `SEND_WEBHOOK`).
  - Muted Slate: Disabled or inactive condition.

### Node Connector Lines
- Vertical and branching SVG connector paths running between sequence steps.
- 2px line width in `rgba(255, 255, 255, 0.12)`. When a sequence is actively processing, the line animates with an emerald or cyan pulse gradient flowing downwards.

### Toggle Switches
- Track: 48dp width, 24dp height, fully rounded pill. Inactive state: `#1C2128` with 1px border. Active state: `#8B5CF6`.
- Thumb: Crisp 18dp circle in `#FFFFFF`, shifting smoothly along the track without oversized shadows.

### Input Fields & Parameters
- Base surface in `#161B22` inset into `#22272E` containers.
- Monospace font (`JetBrains Mono`) for parameter inputs (threshold values, regex, URLs).
- Focus state: border shifts from hairline white to `#8B5CF6` with zero outer blur.

### Cards & Logic Containers
- Structured blocks with 8px radius, `#1C2128` or `#22272E` background, and `rgba(255, 255, 255, 0.08)` outline.
- Header row contains the action icon, category pill, and reorder handle; body provides condition configurations; footer displays execution statistics and test triggers.