package com.oibsip.calculator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * OIBSIP Android Task 3 - Calculator
 *
 * Builds an expression string as the user taps buttons, then evaluates it
 * on "=" using a small two-pass evaluator (no eval()): first resolve all
 * "*" and "/" left-to-right, then resolve "+" and "-" left-to-right. This
 * supports operator chaining such as "5+3*2" without needing to reset
 * after every operator.
 *
 * All button wiring uses setOnClickListener() in code - no inline
 * android:onclick attributes in the XML.
 */
public class MainActivity extends AppCompatActivity {

    private TextView textDisplay;

    // The expression as typed, using internal operator chars: + - * /
    private StringBuilder expression = new StringBuilder();
    // True right after "=" produced a result; the next digit press should
    // start a brand new expression instead of appending to the result.
    private boolean justEvaluated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashScreen.setOnExitAnimationListener(splashScreenView -> {
            View icon = splashScreenView.getIconView();
            AnimatorSet exit = new AnimatorSet();
            exit.playTogether(
                    ObjectAnimator.ofFloat(icon, View.SCALE_X, 1f, 0f),
                    ObjectAnimator.ofFloat(icon, View.SCALE_Y, 1f, 0f),
                    ObjectAnimator.ofFloat(icon, View.ALPHA, 1f, 0f));
            exit.setDuration(350);
            exit.setInterpolator(new AccelerateInterpolator());
            exit.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    splashScreenView.remove();
                }
            });
            exit.start();
        });

        textDisplay = findViewById(R.id.textDisplay);

        View.OnClickListener clickListener = this::onButtonClick;

        int[] allButtonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5,
                R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot,
                R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide,
                R.id.btnClear, R.id.btnBackspace, R.id.btnEquals
        };
        for (int id : allButtonIds) {
            findViewById(id).setOnClickListener(clickListener);
        }

        updateDisplay();
    }

    private void onButtonClick(View v) {
        animatePress(v);
        int id = v.getId();
        if (id == R.id.btnDot) {
            onDot();
        } else if (id == R.id.btnPlus) {
            onOperator('+');
        } else if (id == R.id.btnMinus) {
            onOperator('-');
        } else if (id == R.id.btnMultiply) {
            onOperator('*');
        } else if (id == R.id.btnDivide) {
            onOperator('/');
        } else if (id == R.id.btnClear) {
            onClear();
        } else if (id == R.id.btnBackspace) {
            onBackspace();
        } else if (id == R.id.btnEquals) {
            onEquals();
        } else {
            onDigit(((Button) v).getText().toString());
        }
    }

    /** Quick tactile "pop" feedback on every button press. */
    private void animatePress(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        v.animate().cancel();
        v.setScaleX(0.9f);
        v.setScaleY(0.9f);
        v.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(180)
                .setInterpolator(new OvershootInterpolator(3f))
                .start();
    }

    /** Subtle pop-in transition whenever the display text changes. */
    private void animateDisplayUpdate() {
        textDisplay.animate().cancel();
        textDisplay.setAlpha(0.5f);
        textDisplay.setTranslationY(14f);
        textDisplay.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(160)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /** Horizontal shake used to signal an error (e.g. divide by zero). */
    private void shakeDisplay() {
        textDisplay.animate().cancel();
        ObjectAnimator shake = ObjectAnimator.ofFloat(textDisplay, View.TRANSLATION_X,
                0f, -18f, 18f, -14f, 14f, -8f, 8f, 0f);
        shake.setDuration(400);
        shake.start();
    }

    private boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private void onDigit(String digit) {
        if (justEvaluated) {
            expression.setLength(0);
            justEvaluated = false;
        }
        expression.append(digit);
        updateDisplay();
    }

    private void onDot() {
        if (justEvaluated) {
            expression.setLength(0);
            justEvaluated = false;
        }
        // Find the current number segment (after the last operator) and
        // only allow one decimal point within it.
        int lastOpIndex = lastOperatorIndex();
        String currentSegment = expression.substring(lastOpIndex + 1);
        if (currentSegment.contains(".")) {
            return; // already has a decimal point in this number
        }
        if (currentSegment.isEmpty()) {
            expression.append("0");
        }
        expression.append(".");
        updateDisplay();
    }

    private void onOperator(char op) {
        if (expression.length() == 0) {
            // No leading operator support (keeps things simple/predictable).
            return;
        }
        justEvaluated = false;
        char last = expression.charAt(expression.length() - 1);
        if (isOperatorChar(last)) {
            // Replace the previous operator instead of stacking two in a row.
            expression.setCharAt(expression.length() - 1, op);
        } else {
            expression.append(op);
        }
        updateDisplay();
    }

    private void onClear() {
        expression.setLength(0);
        justEvaluated = false;
        updateDisplay();
    }

    private void onBackspace() {
        if (justEvaluated) {
            onClear();
            return;
        }
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
        }
        updateDisplay();
    }

    private void onEquals() {
        if (expression.length() == 0) {
            return;
        }
        char last = expression.charAt(expression.length() - 1);
        if (isOperatorChar(last)) {
            // Trailing operator with nothing after it - ignore the request.
            return;
        }

        try {
            double result = evaluate(expression.toString());
            String formatted = formatResult(result);
            textDisplay.setText(formatted);
            animateDisplayUpdate();
            expression.setLength(0);
            expression.append(formatted);
            justEvaluated = true;
        } catch (ArithmeticException e) {
            textDisplay.setText("Error");
            shakeDisplay();
            Toast.makeText(this, "Cannot divide by zero.", Toast.LENGTH_SHORT).show();
            expression.setLength(0);
            justEvaluated = true;
        } catch (Exception e) {
            textDisplay.setText("Error");
            shakeDisplay();
            expression.setLength(0);
            justEvaluated = true;
        }
    }

    private int lastOperatorIndex() {
        for (int i = expression.length() - 1; i >= 0; i--) {
            if (isOperatorChar(expression.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private void updateDisplay() {
        if (expression.length() == 0) {
            textDisplay.setText("0");
        } else {
            textDisplay.setText(expression.toString()
                    .replace('*', '×')
                    .replace('/', '÷'));
        }
        animateDisplayUpdate();
    }

    private String formatResult(double result) {
        if (result == Math.rint(result) && !Double.isInfinite(result)) {
            return String.valueOf((long) result);
        }
        return String.valueOf(result);
    }

    // ---- Expression evaluation (no eval()) ----

    /**
     * Tokenises the expression into numbers and operators, then evaluates
     * in two passes: * and / first (left to right), then + and - (left to
     * right). This correctly handles chained expressions like "5+3*2".
     */
    private double evaluate(String expr) throws ArithmeticException {
        List<String> tokens = tokenize(expr);

        // Pass 1: resolve * and /
        List<String> pass1 = new ArrayList<>();
        pass1.add(tokens.get(0));
        for (int i = 1; i < tokens.size(); i += 2) {
            String op = tokens.get(i);
            String nextNum = tokens.get(i + 1);
            if (op.equals("*") || op.equals("/")) {
                double left = Double.parseDouble(pass1.get(pass1.size() - 1));
                double right = Double.parseDouble(nextNum);
                double res;
                if (op.equals("/")) {
                    if (right == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    res = left / right;
                } else {
                    res = left * right;
                }
                pass1.set(pass1.size() - 1, String.valueOf(res));
            } else {
                pass1.add(op);
                pass1.add(nextNum);
            }
        }

        // Pass 2: resolve + and -
        double result = Double.parseDouble(pass1.get(0));
        for (int i = 1; i < pass1.size(); i += 2) {
            String op = pass1.get(i);
            double val = Double.parseDouble(pass1.get(i + 1));
            if (op.equals("+")) {
                result += val;
            } else if (op.equals("-")) {
                result -= val;
            }
        }
        return result;
    }

    private List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (isOperatorChar(c)) {
                tokens.add(num.toString());
                num.setLength(0);
                tokens.add(String.valueOf(c));
            } else {
                num.append(c);
            }
        }
        tokens.add(num.toString());
        return tokens;
    }
}
