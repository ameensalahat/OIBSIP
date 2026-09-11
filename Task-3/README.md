# Android Task 3 — Calculator

**Track:** Android App Development (OIBSIP)
**Author:** Ameen Nader Salahat

## Objective
A calculator app that performs basic arithmetic operations (addition,
subtraction, multiplication, division) based on user input.

## Tech Stack
Android Studio, Java, XML, Material Components

## How to run
1. Open this folder (`Task-3`) in Android Studio as an existing project.
2. Let Gradle sync (the wrapper is included, so no manual Gradle setup is needed).
3. Run on an emulator or a physical device (minSdk 21+).

## Feature Checklist
- [x] Numeric keypad (0-9) plus decimal point
- [x] Basic operators: add, subtract, multiply, divide
- [x] Clear (C) and backspace (⌫) controls
- [x] Chained expressions (e.g. `5+3*2`) evaluated with correct operator precedence
- [x] Division-by-zero handled gracefully with an error message (no crash)
- [x] Custom adaptive app icon and branded splash screen
- [x] Polished dark theme with a distinct "screen" panel and auto-sizing result text
- [x] Tactile press animation + haptic feedback on every button
- [x] Animated display updates and an error shake on invalid input

## Implementation notes
- The expression is built as a string while typing and evaluated on `=`
  using a small two-pass evaluator (`*`/`/` first, then `+`/`-`) instead
  of `eval()`, so operator precedence is handled correctly without a
  full expression-parser dependency.
- UI is built with Material Components (`MaterialButton`, `MaterialCardView`)
  and uses `androidx.core:core-splashscreen` for the launch splash screen.
- The app icon and splash icon are plain vector drawables (adaptive icon
  for API 26+, a flattened vector fallback for older devices), so no
  binary image assets are needed.

## Screenshots / Demo
Add your screenshots and demo video link here before submitting.
