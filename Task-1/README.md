# Android Task 1 — Unit Converter

**Track:** Android App Development (OIBSIP)
**Author:** Ameen Nader Salahat

## Objective
An Android app that converts values between common units of measurement
(length, weight, and temperature) based on user input.

## Tech Stack
Android Studio, Java, XML, Material Components 3

## How to run
1. Open this folder (`Android-Task1-UnitConverter`) in Android Studio as an existing project.
2. Let Gradle sync (Android Studio will offer to generate the Gradle wrapper automatically — accept it).
3. Run on an emulator or a physical device (minSdk 21+).

## Feature Checklist
- [x] Input field for the numeric value to be converted
- [x] Material 3 exposed dropdown menu to select the source unit
- [x] Material 3 exposed dropdown menu to select the target unit
- [x] One-tap swap button to flip the source/target units, with a rotation animation
- [x] Convert button that computes and displays the result
- [x] Animated result card that fades and slides into view on every conversion
- [x] Support for 3 measurement categories: Length, Weight, Temperature
- [x] Input validation: Toast message if the input field is empty or non-numeric
- [x] Category selector that animates the form and resets the unit dropdowns when switched
- [x] Light and dark theme support (follows system setting)
- [x] Custom adaptive app icon and branded splash screen

## Implementation notes
- Length and Weight conversions normalise through a common base unit
  (meters / grams respectively) so adding a new unit only requires two
  small conversion functions.
- Temperature uses dedicated formulas (not a simple multiplier) since
  Celsius/Fahrenheit/Kelvin scales don't share a zero point.
- Non-numeric or empty input is rejected with a `Toast` instead of crashing.
- UI is built with Material 3 components (`TextInputLayout` exposed dropdowns,
  `MaterialCardView`, `MaterialButton`) and uses `androidx.core:core-splashscreen`
  for the launch splash screen.

## Screenshots / Demo
Add your screenshots and demo video link here before submitting.
