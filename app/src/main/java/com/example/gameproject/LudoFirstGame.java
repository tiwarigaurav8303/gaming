package com.example.gameproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class LudoFirstGame extends AppCompatActivity {
    Button rollButton;
    ImageView dice1, dice2;
    TextView sumResult, multiplicationResult, totalCoinsText, name, nameFirstGameF;
    EditText guessSum, guessMultiplication;
    int coinCount = 2000; // Initial coin count
    SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ludo_first_game);

        rollButton = findViewById(R.id.rollButton);
        dice1 = findViewById(R.id.dice1);
        dice2 = findViewById(R.id.dice2);
        sumResult = findViewById(R.id.sumResult);
        multiplicationResult = findViewById(R.id.multiplicationResult);
        guessSum = findViewById(R.id.guessSum);
        guessMultiplication = findViewById(R.id.guessMultiplication);
        totalCoinsText = findViewById(R.id.totalCoins);
        name = findViewById(R.id.name);
        nameFirstGameF = findViewById(R.id.nameFirstGameF);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);

        // Retrieve current user from Firebase Authentication
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Fetch username from Firestore
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String retrievedUsername = task.getResult().getString("username");
                        nameFirstGameF.setText(retrievedUsername);
                        name.setVisibility(View.GONE);
                        nameFirstGameF.setVisibility(View.VISIBLE);

                        // Retrieve the saved current coins from SharedPreferences
                        coinCount = sharedPreferences.getInt("currentCoins_" + retrievedUsername, 2000);
                        updateTotalCoins();
                    } else {
                        Toast.makeText(LudoFirstGame.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LudoFirstGame.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Retrieve the username from SharedPreferences
            String username = sharedPreferences.getString("username", "");

            // Use the retrieved username as needed
            // For example, set it to a TextView
            name.setText(username);
            name.setVisibility(View.VISIBLE);
            nameFirstGameF.setVisibility(View.GONE);

            // Retrieve the saved current coins from SharedPreferences
            coinCount = sharedPreferences.getInt("currentCoins_" + username, 2000);

            // Update the total coins text view
            updateTotalCoins();
        }

        rollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sumGuess = guessSum.getText().toString();
                String multiplicationGuess = guessMultiplication.getText().toString();

                if (sumGuess.isEmpty() || multiplicationGuess.isEmpty()) {
                    Toast.makeText(LudoFirstGame.this, "Please enter numbers for both sum and multiplication", Toast.LENGTH_SHORT).show();
                    return;
                }

                int userGuessSum = Integer.parseInt(sumGuess);
                int userGuessMultiplication = Integer.parseInt(multiplicationGuess);

                if (userGuessSum > 12 || userGuessMultiplication > 36) {
                    Toast.makeText(LudoFirstGame.this, "Invalid number! Please enter a number less than or equal to 12 for sum and less than or equal to 36 for multiplication", Toast.LENGTH_SHORT).show();
                    return;
                }

                rollDice();
            }
        });
    }
    private void initializeCoins(String userId) {
        // Initialize coins to 2000 for new users in Firestore
        db.collection("users").document(userId)
                .update("coins", 2000)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        coinCount = 2000; // Set local coin count to 2000
                        updateTotalCoins();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LudoFirstGame.this, "Failed to initialize coins", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateTotalCoins() {
        totalCoinsText.setText("Total Coins: " + coinCount);
    }
    public void rollDice() {
        Random random = new Random();
        int value1 = random.nextInt(6) + 1;
        int value2 = random.nextInt(6) + 1;

        setDiceImage(dice1, value1);
        setDiceImage(dice2, value2);

        int sum = value1 + value2;
        int multiplication = value1 * value2;

        sumResult.setText("Sum Result: " + sum);
        multiplicationResult.setText("Multiplication Result: " + multiplication);

        checkGuesses(sum, multiplication);
    }
    private void checkGuesses(int actualSum, int actualMultiplication) {
        try {
            int userGuessSum = Integer.parseInt(guessSum.getText().toString());
            int userGuessMultiplication = Integer.parseInt(guessMultiplication.getText().toString());

            if (userGuessSum == actualSum && userGuessMultiplication == actualMultiplication) {
                showCongratulationsPopup();
                coinCount += 100; // Award 100 coins for correct guesses
            } else {
                showSorryPopup();
                coinCount -= 50; // Deduct 50 coins for incorrect guesses
            }

            updateTotalCoins();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid guesses", Toast.LENGTH_SHORT).show();
        }
    }
    private void showCongratulationsPopup() {
        showPopup("Congratulations!", "You win 100 coins!");
    }
    private void showSorryPopup() {
        showPopup("Sorry!", "You lose 50 coins. Try again.");
    }
    private void showPopup(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog after 2 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 2000);
            }
        });
        builder.show();
    }

    private void setDiceImage(ImageView imageView, int value) {
        switch (value) {
            case 1:
                imageView.setImageResource(R.drawable.dice1);
                break;
            case 2:
                imageView.setImageResource(R.drawable.dice2);
                break;
            case 3:
                imageView.setImageResource(R.drawable.dice3);
                break;
            case 4:
                imageView.setImageResource(R.drawable.dice4);
                break;
            case 5:
                imageView.setImageResource(R.drawable.dice5);
                break;
            case 6:
                imageView.setImageResource(R.drawable.dice6);
                break;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Save the current coin count to SharedPreferences
        String username = nameFirstGameF.getText().toString().isEmpty() ? name.getText().toString() : nameFirstGameF.getText().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentCoins_" + username, coinCount);
        editor.apply();

        // Retrieve the current user from Firebase Authentication
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Fetch username from Firestore
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String retrievedUsername = task.getResult().getString("username");

                        // Create a TextView with the message
                        TextView messageTextView = new TextView(LudoFirstGame.this);
                        messageTextView.setText("Username: " + retrievedUsername + "\nCurrent Coins: " + coinCount);
                        messageTextView.setGravity(Gravity.CENTER);
                        messageTextView.setTextSize(16); // Adjust the text size as needed
                        messageTextView.setTextColor(Color.GREEN); // Set the text color

                        // Show the popup with the username and current coins
                        new AlertDialog.Builder(LudoFirstGame.this)
                                .setTitle("Exit Application")
                                .setView(messageTextView)
                                .setMessage("Are you sure you want to exit?")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    // After the user dismisses the dialog, finish the activity
                                    LudoFirstGame.super.onBackPressed();
                                }) .setNegativeButton("Cancel", (dialog, which) -> {
                                    // Dismiss the dialog without exiting
                                    dialog.dismiss();
                                })
                                .show();
                    } else {
                        Toast.makeText(LudoFirstGame.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LudoFirstGame.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String retrievedUsername = sharedPreferences.getString("username", "");
            // Create a TextView with the message
            TextView messageTextView = new TextView(LudoFirstGame.this);
            messageTextView.setText("Username: " + retrievedUsername + "\nCurrent Coins: " + coinCount);
            messageTextView.setGravity(Gravity.CENTER);
            messageTextView.setTextSize(16); // Adjust the text size as needed
            messageTextView.setTextColor(Color.GREEN); // Set the text color

            // Show the popup with the username and current coins
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Exit Application");
            builder.setMessage("Are you sure you want to exit?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });


        }
    }
}
