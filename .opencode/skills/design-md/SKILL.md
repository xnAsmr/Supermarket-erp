---
name: design-md
description: Use when the user asks to style UI to match a specific brand's design language, or says "make it look like Stripe / Linear / Notion / Apple / Vercel / ...", or wants a consistent brand design system for the frontend. Reads a brand's DESIGN.md from the local awesome-design-md clone and applies its color/typography/component rules. Trigger keywords: 设计风格, 品牌风格, DESIGN.md, design system, 配色, make it look like.
---

# Design System (DESIGN.md)

This project has a local clone of the `awesome-design-md` collection at:

```
D:\SoftWareData\CodeSpace\OpenCode\awesome-design-md
```

Each brand lives in `design-md/<brand>/` and contains a `DESIGN.md` (the design system: colors, typography, components, layout, depth, do's/don'ts, agent prompt guide) plus a `README.md`. `DESIGN.md` has YAML frontmatter with structured tokens and a Markdown body.

## When to use

- The user explicitly asks to style/match a brand: "make it look like Stripe", "用 Linear 的风格", "按某个设计系统来配色".
- The user mentions "设计风格" / "品牌风格" / "design system" / "配色方案" for frontend work.

## How to use

1. **Pick the brand.** If the user names one, use it. Otherwise ask which brand, or recommend based on their app type (e.g. SaaS dashboard → Linear/Stripe; docs → Notion/Mintlify; dark dev tool → Vercel/Raycast).
2. **Read the DESIGN.md** at `D:\SoftWareData\CodeSpace\OpenCode\awesome-design-md\design-md\<brand>\DESIGN.md` (not the README — it is just a stub pointing to getdesign.md).
3. **Extract the tokens** from the frontmatter: `colors`, `typography`, and any component/layout rules in the body.
4. **Apply consistently** when creating or modifying Vue components / styles:
   - Map the brand's color roles onto the app's CSS variables or Naive UI theme overrides.
   - Follow the typography hierarchy table (display/body/labels) with the same families, sizes, weights, and letter-spacing.
   - Respect component stylings (buttons, cards, inputs, states), layout principles, spacing scale, and do's / don'ts.
5. **Preview files** (`preview.html`, `preview-dark.html`) may exist for some brands; the DESIGN.md body is the source of truth for tokens.

## Notes

- All brand DESIGN.md files are "inspired interpretations", not exact brands — treat them as a style guide.
- If the user wants a different brand later, re-read that brand's DESIGN.md and re-apply.
- Do NOT commit or copy the whole awesome-design-md repo into this project; reference the local clone only.