---
name: Lumio
colors:
  surface: '#fdf8f8'
  surface-dim: '#ddd9d8'
  surface-bright: '#fdf8f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f7f3f2'
  surface-container: '#f1edec'
  surface-container-high: '#ebe7e6'
  surface-container-highest: '#e5e2e1'
  on-surface: '#1c1b1b'
  on-surface-variant: '#444748'
  inverse-surface: '#313030'
  inverse-on-surface: '#f4f0ef'
  outline: '#747878'
  outline-variant: '#c4c7c7'
  surface-tint: '#5f5e5e'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#1c1b1b'
  on-primary-container: '#858383'
  inverse-primary: '#c8c6c5'
  secondary: '#615e58'
  on-secondary: '#ffffff'
  secondary-container: '#e7e2d9'
  on-secondary-container: '#67645d'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#1c1b1a'
  on-tertiary-container: '#868382'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e5e2e1'
  primary-fixed-dim: '#c8c6c5'
  on-primary-fixed: '#1c1b1b'
  on-primary-fixed-variant: '#474746'
  secondary-fixed: '#e7e2d9'
  secondary-fixed-dim: '#cbc6be'
  on-secondary-fixed: '#1d1b17'
  on-secondary-fixed-variant: '#494641'
  tertiary-fixed: '#e6e2df'
  tertiary-fixed-dim: '#cac6c4'
  on-tertiary-fixed: '#1c1b1a'
  on-tertiary-fixed-variant: '#484645'
  background: '#fdf8f8'
  on-background: '#1c1b1b'
  surface-variant: '#e5e2e1'
typography:
  display:
    fontFamily: Inter
    fontSize: 72px
    fontWeight: '600'
    lineHeight: '1.05'
    letterSpacing: -0.04em
  h1:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '600'
    lineHeight: '1.1'
    letterSpacing: -0.03em
  h2:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '500'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  h3:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '500'
    lineHeight: '1.3'
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: '0'
  body-md:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: '0'
  label:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: '1'
    letterSpacing: 0.05em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 4px
  xs: 8px
  sm: 16px
  md: 32px
  lg: 64px
  xl: 128px
  gutter: 24px
  margin: 40px
---

## Brand & Style

This design system is built on a foundation of **Editorial Minimalism**. It prioritizes clarity, whitespace, and a rhythmic verticality that mimics premium print publications. The target audience is the sophisticated professional who values focus and intentionality over feature density. 

The aesthetic is characterized by a "Quiet Luxury" approach: monochrome palettes, subtle tonal shifts instead of borders, and high-contrast typography. The UI should feel like a breathable canvas, evoking a sense of calm authority and effortless precision.

## Colors

The palette is a sophisticated range of warm neutrals designed to reduce eye strain while maintaining a premium feel. 

- **Background Strategy:** Use `--page-bg` as the primary canvas. `--soft-bg` and `--card-bg` are reserved for structural nesting and floating elements to create depth without relying on heavy shadows.
- **Monochrome Emphasis:** Primary actions and core branding utilize `--black` and `--deep-black`. 
- **Subtle Guidance:** Use `--muted` for secondary metadata and `--line` for delicate horizontal dividers that define sections without closing them off.

## Typography

The typographic system leverages **Inter** for its utilitarian precision, treated with an editorial eye. 

- **Headlines:** Must be high-contrast and tightly tracked. Large display sizes should feel architectural.
- **Body Text:** Generous line-height is essential for readability and to maintain the "spacious" brand promise.
- **Labels:** Use small, uppercase labels with slightly increased letter spacing to create a distinct visual hierarchy for metadata and small captions.

## Layout & Spacing

This design system uses a **Fixed Grid** with an emphasis on vertical rhythm. 

- **The Breathable Layout:** Layouts should favor large top and bottom margins (`--lg` or `--xl`) to isolate content blocks.
- **Grid Structure:** A 12-column grid is used for desktop, but content should rarely span the full width; centered, narrow columns (8-column width) are preferred for editorial readability.
- **Floating Logic:** Components should appear to float on the `--page-bg`, using padding instead of borders to define internal space.

## Elevation & Depth

Depth is achieved through **Tonal Layering** and **Soft Ambient Shadows**.

- **Layers:** Objects do not "pop" off the screen; they lift subtly. Use `--card-bg-light` for the highest elevation surfaces.
- **Shadows:** Shadows are nearly invisible—very large blur radii (30-60px) with extremely low opacity (2-4%) using the `--black` hue.
- **Outlines:** Use `--line` for ultra-thin (1px) borders only when necessary for accessibility in forms. Otherwise, rely on color-blocking against the warm background to define edges.

## Shapes

The shape language is disciplined and professional. 

- **Corner Radius:** Standard components (buttons, inputs) use a 7px radius. Larger containers (cards, modals) use a 12px or 16px radius to feel softer and more approachable.
- **Photography:** Soft, editorial photography should follow the same corner radius logic. Images should never be sharp-edged if they are floating components.

## Components

- **Buttons:** Primary buttons are solid `--black` with `--white` text. Secondary buttons use `--card-bg` with `--text`. No gradients.
- **Cards:** Use `--card-bg-light` with a 16px radius. Padding inside cards must be generous (minimum 32px) to maintain the spacious aesthetic.
- **Input Fields:** Minimalist design with a bottom border using `--line` or a subtle background fill of `--soft-bg`. Focus states should transition the border to `--black`.
- **Chips/Tags:** Small, pill-shaped elements with `--soft-bg` and `--muted` text. No borders.
- **Lists:** High vertical padding (16-24px per row) with thin `--line` separators. 
- **Modals:** Floating centered containers with the highest elevation (softest shadow) and a maximum width to ensure readability.