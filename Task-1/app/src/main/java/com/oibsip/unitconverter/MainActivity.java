package com.oibsip.unitconverter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

/**
 * OIBSIP Android Task 1 - Unit Converter
 *
 * Supports 3 measurement categories: Length, Weight, and Temperature.
 * Each category has its own set of convertible units. Conversion for
 * Length and Weight goes through a common "base unit" (metres / grams),
 * while Temperature uses dedicated formulas since it is not a simple
 * multiplicative conversion.
 */
public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView dropdownCategory;
    private AutoCompleteTextView dropdownFrom;
    private AutoCompleteTextView dropdownTo;
    private EditText editValue;
    private TextView textResult;
    private MaterialCardView cardForm;
    private MaterialCardView resultCard;
    private MaterialButton btnConvert;
    private MaterialButton btnSwap;

    private static final String CATEGORY_LENGTH = "Length";
    private static final String CATEGORY_WEIGHT = "Weight";
    private static final String CATEGORY_TEMPERATURE = "Temperature";

    private static final List<String> CATEGORIES = Arrays.asList(
            CATEGORY_LENGTH, CATEGORY_WEIGHT, CATEGORY_TEMPERATURE);

    private static final List<String> LENGTH_UNITS = Arrays.asList(
            "Meters", "Kilometers", "Centimeters", "Millimeters", "Miles", "Feet", "Inches");

    private static final List<String> WEIGHT_UNITS = Arrays.asList(
            "Kilograms", "Grams", "Milligrams", "Pounds", "Ounces");

    private static final List<String> TEMPERATURE_UNITS = Arrays.asList(
            "Celsius", "Fahrenheit", "Kelvin");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dropdownCategory = findViewById(R.id.dropdownCategory);
        dropdownFrom = findViewById(R.id.dropdownFrom);
        dropdownTo = findViewById(R.id.dropdownTo);
        editValue = findViewById(R.id.editValue);
        textResult = findViewById(R.id.textResult);
        cardForm = findViewById(R.id.cardForm);
        resultCard = findViewById(R.id.resultCard);
        btnConvert = findViewById(R.id.btnConvert);
        btnSwap = findViewById(R.id.btnSwap);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, R.layout.item_dropdown, CATEGORIES);
        dropdownCategory.setAdapter(categoryAdapter);
        dropdownCategory.setText(CATEGORY_LENGTH, false);

        dropdownCategory.setOnItemClickListener((parent, view, position, id) -> {
            String category = CATEGORIES.get(position);
            TransitionManager.beginDelayedTransition(cardForm, new AutoTransition());
            populateUnitDropdowns(category);
            hideResult();
        });

        // Populate initial state (Length is selected by default).
        populateUnitDropdowns(CATEGORY_LENGTH);

        btnSwap.setOnClickListener(v -> {
            String fromText = dropdownFrom.getText().toString();
            String toText = dropdownTo.getText().toString();
            dropdownFrom.setText(toText, false);
            dropdownTo.setText(fromText, false);
            btnSwap.animate()
                    .rotationBy(180f)
                    .setDuration(350)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
            hideResult();
        });

        btnConvert.setOnClickListener(v -> {
            animateButtonPress(btnConvert);
            performConversion();
        });

        cardForm.setAlpha(0f);
        cardForm.setTranslationY(dpToPx(40));
        cardForm.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(80)
                .setDuration(420)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void populateUnitDropdowns(String category) {
        List<String> units;
        switch (category) {
            case CATEGORY_WEIGHT:
                units = WEIGHT_UNITS;
                break;
            case CATEGORY_TEMPERATURE:
                units = TEMPERATURE_UNITS;
                break;
            case CATEGORY_LENGTH:
            default:
                units = LENGTH_UNITS;
                break;
        }

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown, units);
        dropdownFrom.setAdapter(fromAdapter);
        dropdownFrom.setText(units.get(0), false);

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown, units);
        dropdownTo.setAdapter(toAdapter);
        // Default "to" unit to the second entry when available, so a fresh
        // screen doesn't show identical from/to units.
        dropdownTo.setText(units.size() > 1 ? units.get(1) : units.get(0), false);
    }

    private void performConversion() {
        String rawValue = editValue.getText().toString().trim();
        if (rawValue.isEmpty()) {
            Toast.makeText(this, "Please enter a value to convert.", Toast.LENGTH_SHORT).show();
            return;
        }

        double value;
        try {
            value = Double.parseDouble(rawValue);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = dropdownCategory.getText().toString();
        String fromUnit = dropdownFrom.getText().toString();
        String toUnit = dropdownTo.getText().toString();

        if (fromUnit.isEmpty() || toUnit.isEmpty()) {
            Toast.makeText(this, "Please choose both units.", Toast.LENGTH_SHORT).show();
            return;
        }

        Double result;
        switch (category) {
            case CATEGORY_LENGTH:
                result = convertLength(value, fromUnit, toUnit);
                break;
            case CATEGORY_WEIGHT:
                result = convertWeight(value, fromUnit, toUnit);
                break;
            case CATEGORY_TEMPERATURE:
                result = convertTemperature(value, fromUnit, toUnit);
                break;
            default:
                result = null;
        }

        if (result == null) {
            Toast.makeText(this, "Could not convert those units.", Toast.LENGTH_SHORT).show();
            return;
        }

        textResult.setText(String.format("%.4f %s = %.4f %s", value, fromUnit, result, toUnit));
        revealResult();
    }

    /** Fades and slides the result card into view, replaying the animation on every conversion. */
    private void revealResult() {
        resultCard.animate().cancel();
        resultCard.setVisibility(View.VISIBLE);
        resultCard.setAlpha(0f);
        resultCard.setTranslationY(dpToPx(24));
        resultCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(320)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void hideResult() {
        if (resultCard.getVisibility() != View.VISIBLE) {
            return;
        }
        resultCard.animate().cancel();
        resultCard.animate()
                .alpha(0f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resultCard.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void animateButtonPress(View view) {
        view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    /** Converts a length value by first normalising to metres, the base unit. */
    private Double convertLength(double value, String from, String to) {
        Double meters = toMeters(value, from);
        if (meters == null) return null;
        return fromMeters(meters, to);
    }

    private Double toMeters(double value, String unit) {
        switch (unit) {
            case "Meters": return value;
            case "Kilometers": return value * 1000.0;
            case "Centimeters": return value / 100.0;
            case "Millimeters": return value / 1000.0;
            case "Miles": return value * 1609.344;
            case "Feet": return value * 0.3048;
            case "Inches": return value * 0.0254;
            default: return null;
        }
    }

    private Double fromMeters(double meters, String unit) {
        switch (unit) {
            case "Meters": return meters;
            case "Kilometers": return meters / 1000.0;
            case "Centimeters": return meters * 100.0;
            case "Millimeters": return meters * 1000.0;
            case "Miles": return meters / 1609.344;
            case "Feet": return meters / 0.3048;
            case "Inches": return meters / 0.0254;
            default: return null;
        }
    }

    /** Converts a weight value by first normalising to grams, the base unit. */
    private Double convertWeight(double value, String from, String to) {
        Double grams = toGrams(value, from);
        if (grams == null) return null;
        return fromGrams(grams, to);
    }

    private Double toGrams(double value, String unit) {
        switch (unit) {
            case "Kilograms": return value * 1000.0;
            case "Grams": return value;
            case "Milligrams": return value / 1000.0;
            case "Pounds": return value * 453.59237;
            case "Ounces": return value * 28.349523125;
            default: return null;
        }
    }

    private Double fromGrams(double grams, String unit) {
        switch (unit) {
            case "Kilograms": return grams / 1000.0;
            case "Grams": return grams;
            case "Milligrams": return grams * 1000.0;
            case "Pounds": return grams / 453.59237;
            case "Ounces": return grams / 28.349523125;
            default: return null;
        }
    }

    /** Temperature is not linear-through-zero, so it needs dedicated formulas. */
    private Double convertTemperature(double value, String from, String to) {
        double celsius;
        switch (from) {
            case "Celsius":
                celsius = value;
                break;
            case "Fahrenheit":
                celsius = (value - 32) * 5.0 / 9.0;
                break;
            case "Kelvin":
                celsius = value - 273.15;
                break;
            default:
                return null;
        }

        switch (to) {
            case "Celsius":
                return celsius;
            case "Fahrenheit":
                return celsius * 9.0 / 5.0 + 32;
            case "Kelvin":
                return celsius + 273.15;
            default:
                return null;
        }
    }
}
