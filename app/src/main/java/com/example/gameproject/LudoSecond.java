package com.example.gameproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class LudoSecond extends AppCompatActivity {
    private TextView numberTextView, sumTextView, multiplyTextView, individualNumbersTextView;
    private EditText sidesInput, numberInput;
    private Button playButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ludo_second);

        numberTextView = findViewById(R.id.numberTextView);
        sumTextView = findViewById(R.id.sumTextView);
        multiplyTextView = findViewById(R.id.multiplyTextView);
        individualNumbersTextView = findViewById(R.id.individualNumbersTextView); // Added TextView
        sidesInput = findViewById(R.id.sidesInput);
        numberInput = findViewById(R.id.numberInput);
        playButton = findViewById(R.id.playButton);

        LinearLayout linearLayout = findViewById(R.id.yourLinearLayout);
        ImageView imageView = findViewById(R.id.yourImageView);
        TextView textView = findViewById(R.id.yourTextView);
        showWelcomePopup();

        // Set up the ObjectAnimator for translating (moving) the image along the x-axis
        ObjectAnimator translationXAnimator = ObjectAnimator.ofFloat(imageView, "translationX", -200f, 0f);

        // Set up the ObjectAnimator for rotating the image
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);

        // Set up the ObjectAnimator for scaling the TextView
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(textView, "scaleX", 0f, 1f);

        // Set up the ObjectAnimator for fading in the TextView
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);

        // Set up the AnimatorSet to play all animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translationXAnimator, rotationAnimator, scaleXAnimator, alphaAnimator);
        animatorSet.setDuration(1000); // Duration of the animation in milliseconds
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        // Start the animation
        animatorSet.start();

        // Add text watchers to validate input in real-time
        addTextWatcher(sidesInput);
        addTextWatcher(numberInput);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollDice();
            }
        });
    }
    private void rollDice() {
        String sidesText = sidesInput.getText().toString();
        String numberText = numberInput.getText().toString();

        if (isValidInput(sidesText) && isValidInput(numberText)) {
            int sides = Integer.parseInt(sidesText);
            int number = Integer.parseInt(numberText);

            if (isWithinRange(sides) && isWithinRange(number)) {
                StringBuilder numbersStringBuilder = new StringBuilder(); // To store individual numbers
                Random random = new Random();
                int sum = 0;
                int multiply = 1;

                for (int i = 0; i < number; i++) {
                    int randomNumber = random.nextInt(sides) + 1;
                    numbersStringBuilder.append(randomNumber);

                    // If it's not the last number, add a comma and a space
                    if (i < number - 1) {
                        numbersStringBuilder.append(", ");
                    }

                    sum += randomNumber;
                    multiply *= randomNumber;
                }

                updateUI(sum, multiply, numbersStringBuilder.toString());
            } else {
                showAlert("Please enter values between 1 and 6.");
            }
        } else {
            showAlert("Please insert value.");
        }
    }
    private void updateUI(int sum, int multiply, String individualNumbers) {
        sumTextView.setText("Sum: " + sum);
        multiplyTextView.setText("Multiplication: " + multiply);
//        numberTextView.setText("Individual Numbers: " + individualNumbers);
        individualNumbersTextView.setText("Individual Numbers: " + individualNumbers);
    }

    // Helper method to show an alert message
    private void showAlert(String message) {
        // Implement your alert logic here, such as using AlertDialog.Builder
        // or displaying a Toast message.
        // Example using Toast:
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidInput(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private boolean isWithinRange(int value) {
        return value >= 1 && value <= 6;
    }

    // Helper method to add a text watcher for input validation
    private void addTextWatcher(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (!isValidInput(input) || !isWithinRange(Integer.parseInt(input))) {
                    editText.setError("Please enter a valid value (1-6)");
                    blinkCursor(editText);
                } else {
                    editText.setError(null); // Clear error if input is valid
                }
            }
        });
    }

    // Helper method to make the cursor blink for better visibility
    private void blinkCursor(final EditText editText) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.setCursorVisible(!editText.isCursorVisible());
                handler.postDelayed(this, 500); // 500 milliseconds blink interval
            }
        }, 500);
    }
    private void showWelcomePopup() {
        // Create a dialog and set its content view
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_layout);

        // Set properties for the dialog window
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Match parent width
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Set initial height to WRAP_CONTENT
        layoutParams.gravity = Gravity.BOTTOM; // Start from the bottom

        // Apply the layout parameters to the dialog window
        dialog.getWindow().setAttributes(layoutParams);

        // Set up the ObjectAnimator for translating (moving) the dialog along the y-axis
        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(dialog.getWindow().getDecorView(), "translationY", getWindowManager().getDefaultDisplay().getHeight(), 0f);
        translationYAnimator.setDuration(1000); // Duration of the animation in milliseconds
        translationYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // Start the bottom-to-center animation
        translationYAnimator.start();

        // Show the dialog
        dialog.show();

        // Use a Handler to dismiss the dialog after 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    // Set up the ObjectAnimator for translating (moving) the dialog along the y-axis
                    ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(dialog.getWindow().getDecorView(), "translationY", 0f, getWindowManager().getDefaultDisplay().getHeight());
                    translationYAnimator.setDuration(1000); // Duration of the animation in milliseconds
                    translationYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                    // Start the center-to-bottom animation before dismissing
                    translationYAnimator.start();

                    // Dismiss the dialog after the animation
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 1000); // 1000 milliseconds (1 second)
                }
            }
        }, 4000); // 2000 milliseconds (2 seconds)
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Application?");
        builder.setMessage("Do you Want to Sure Exit This Application?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getApplicationContext(), SecondActivity.class));
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                builder.show();
            }
        });
    }
}
