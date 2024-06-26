package com.example.gameproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class SpinnerGame extends AppCompatActivity {
    private ImageView spinnerWheel, arrow;
    private Button btnPlay;
    private TextView tvCoins, tvResult, nameSP, nameSpinnerGameF;
    private RadioGroup numberOptionsGroup;
    private RadioButton rbOption1, rbOption2, rbOption3;
    private int coins = 3000;
    private SharedPreferences sharedPreferences;
    private String currentUsername = "";
    private boolean spinning = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner_game);

        spinnerWheel = findViewById(R.id.spinnerWheel);
        btnPlay = findViewById(R.id.btnPlay);
        tvCoins = findViewById(R.id.tvCoins);
        arrow = findViewById(R.id.arrow);
        tvResult = findViewById(R.id.tvResult);
        numberOptionsGroup = findViewById(R.id.numberOptionsGroup);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        nameSP = findViewById(R.id.nameSP);
        nameSpinnerGameF = findViewById(R.id.nameSpinnerGameF);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (currentUser.getProviderData().get(1).getProviderId().equals("google.com")) {
                nameSP.setVisibility(View.VISIBLE);
                nameSpinnerGameF.setVisibility(View.GONE);
                if (displayName != null && !displayName.isEmpty()) {
                    currentUsername = currentUser.getEmail(); // Use email as identifier for Google users
                    nameSP.setText(displayName);
                } else {
                    nameSP.setText("No Name");
                }
            } else {
                nameSP.setVisibility(View.GONE);
                nameSpinnerGameF.setVisibility(View.VISIBLE);
                if (displayName != null && !displayName.isEmpty()) {
                    currentUsername = displayName; // Use display name as identifier for Firebase users
                    nameSpinnerGameF.setText(displayName);
                } else {
                    currentUsername = "No Name";
                    nameSpinnerGameF.setText("No Name");
                }
            }
        }

        sharedPreferences = getSharedPreferences("spinnerGamePrefs", MODE_PRIVATE);

        loadSavedCoins();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!spinning) {
                    if (isOptionSelected()) {
                        playGame();
                    } else {
                        Toast.makeText(SpinnerGame.this, "Please select a number option", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private boolean isOptionSelected() {
        return rbOption1.isChecked() || rbOption2.isChecked() || rbOption3.isChecked();
    }
    private void playGame() {
        spinning = true;

        final RotateAnimation rotate = new RotateAnimation(0, 360 * 5, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(3000);
        rotate.setFillAfter(true);
        spinnerWheel.startAnimation(rotate);

        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                handleResult();
            }
        }.start();
    }
    private void handleResult() {
        spinning = false;

        Random random = new Random();
        int selectedOption = getSelectedOption();
        final int randomNumber = random.nextInt(12);

        String resultMessage = "Result: ";
        int coinsChange = 0;

        switch (selectedOption) {
            case 0: // 0-6
                if (randomNumber >= 0 && randomNumber <= 6) {
                    coinsChange = 10;
                } else {
                    coinsChange = -5;
                }
                break;
            case 1: // 7
                if (randomNumber == 7) {
                    coinsChange = 20;
                } else {
                    coinsChange = -10;
                }
                break;
            case 2: // 8-11
                if (randomNumber >= 8 && randomNumber <= 11) {
                    coinsChange = 6;
                } else {
                    coinsChange = -3;
                }
                break;
        }

        coins += coinsChange;
        resultMessage += "You " + (coinsChange > 0 ? "win " : "lose ") + Math.abs(coinsChange) + " coins!";

        float rotationAngle = calculateRotationAngle(randomNumber);

        spinnerWheel.setRotation(rotationAngle);
        arrow.setRotation(rotationAngle);

        tvResult.setText(resultMessage);
        String finalResultMessage = resultMessage;
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                tvResult.setText("");
                updateUI(finalResultMessage, String.valueOf(randomNumber));
            }
        }.start();
    }
    private int getSelectedOption() {
        int selectedOptionId = numberOptionsGroup.getCheckedRadioButtonId();
        if (selectedOptionId == rbOption1.getId()) {
            return 0;
        } else if (selectedOptionId == rbOption2.getId()) {
            return 1;
        } else if (selectedOptionId == rbOption3.getId()) {
            return 2;
        }
        return -1;
    }
    private void updateUI(String resultMessage, String randomNumber) {
        tvCoins.setText("Coins: " + coins);

        Toast.makeText(SpinnerGame.this, "Random Number: " + randomNumber, Toast.LENGTH_SHORT).show();

        if (coins <= 0) {
            btnPlay.setEnabled(false);
            Toast.makeText(this, "Game over! You ran out of coins.", Toast.LENGTH_LONG).show();
        }
    }
    private float calculateRotationAngle(int randomNumber) {
        float degreesPerOption = 360f / 12f;
        return 360 - degreesPerOption * randomNumber;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentCoins_" + currentUsername, coins);
        editor.apply();

        TextView textView = new TextView(this);
        textView.setText("\nUsername: " + currentUsername + "\nCurrent Score: " + coins);
        textView.setTextSize(16);
        textView.setTextColor(Color.GREEN);
        textView.setGravity(Gravity.CENTER);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                textView.setText("\nUsername: " + displayName + "\nCurrent Score: " + coins);
            } else {
                textView.setText("\nUsername: No Name\nCurrent Score: " + coins);
            }
        } else {
            textView.setText("\nUsername: " + currentUsername + "\nCurrent Score: " + coins);
        }

        builder.setView(textView);
        builder.show();
    }
    private void loadSavedCoins() {
        if (sharedPreferences.contains("currentCoins_" + currentUsername)) {
            coins = sharedPreferences.getInt("currentCoins_" + currentUsername, 3000);
        }
        tvCoins.setText("Coins: " + coins);
    }
}


//  Updated Code Here


//    package com.example.gameproject;
//
//import android.annotation.SuppressLint;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.view.Gravity;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.RotateAnimation;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//import java.util.Random;
//
//public class SpinnerGame extends AppCompatActivity {
//    private ImageView spinnerWheel, arrow;
//    private Button btnPlay;
//    private TextView tvCoins, tvResult, nameSP, nameSpinnerGameF;
//    private RadioGroup numberOptionsGroup;
//    private RadioButton rbOption1, rbOption2, rbOption3;
//    private int coins = 3000;
//    private SharedPreferences sharedPreferences;
//    private String currentUsername = "";
//    private boolean spinning = false;
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_spinner_game);
//
//        spinnerWheel = findViewById(R.id.spinnerWheel);
//        btnPlay = findViewById(R.id.btnPlay);
//        tvCoins = findViewById(R.id.tvCoins);
//        arrow = findViewById(R.id.arrow);
//        tvResult = findViewById(R.id.tvResult);
//        numberOptionsGroup = findViewById(R.id.numberOptionsGroup);
//        rbOption1 = findViewById(R.id.rbOption1);
//        rbOption2 = findViewById(R.id.rbOption2);
//        rbOption3 = findViewById(R.id.rbOption3);
//        nameSP = findViewById(R.id.nameSP);
//        nameSpinnerGameF = findViewById(R.id.nameSpinnerGameF);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            String displayName = currentUser.getDisplayName();
//            if (currentUser.getProviderData().get(1).getProviderId().equals("google.com")) {
//                nameSP.setVisibility(View.VISIBLE);
//                nameSpinnerGameF.setVisibility(View.GONE);
//                if (displayName != null && !displayName.isEmpty()) {
//                    currentUsername = currentUser.getEmail(); // Use email as identifier for Google users
//                    nameSP.setText(displayName);
//                } else {
//                    nameSP.setText("No Name");
//                }
//            } else {
//                nameSP.setVisibility(View.GONE);
//                nameSpinnerGameF.setVisibility(View.VISIBLE);
//                if (displayName != null && !displayName.isEmpty()) {
//                    currentUsername = displayName; // Use display name as identifier for Firebase users
//                    nameSpinnerGameF.setText(displayName);
//                } else {
//                    currentUsername = "No Name";
//                    nameSpinnerGameF.setText("No Name");
//                }
//            }
//        }
//
//        sharedPreferences = getSharedPreferences("spinnerGamePrefs", MODE_PRIVATE);
//
//        loadSavedCoins();
//
//        btnPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!spinning) {
//                    if (isOptionSelected()) {
//                        playGame();
//                    } else {
//                        Toast.makeText(SpinnerGame.this, "Please select a number option", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });
//    }
//
//    private boolean isOptionSelected() {
//        return rbOption1.isChecked() || rbOption2.isChecked() || rbOption3.isChecked();
//    }
//
//    private void playGame() {
//        spinning = true;
//
//        final RotateAnimation rotate = new RotateAnimation(0, 360 * 5, Animation.RELATIVE_TO_SELF,
//                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        rotate.setDuration(3000);
//        rotate.setFillAfter(true);
//        spinnerWheel.startAnimation(rotate);
//
//        new CountDownTimer(3000, 1000) {
//            public void onTick(long millisUntilFinished) {}
//
//            public void onFinish() {
//                handleResult();
//            }
//        }.start();
//    }
//
//    private void handleResult() {
//        spinning = false;
//
//        Random random = new Random();
//        int selectedOption = getSelectedOption();
//        final int randomNumber = random.nextInt(12);
//
//        String resultMessage = "Result: ";
//        int coinsChange = 0;
//
//        switch (selectedOption) {
//            case 0: // 0-6
//                if (randomNumber >= 0 && randomNumber <= 6) {
//                    coinsChange = 10;
//                } else {
//                    coinsChange = -5;
//                }
//                break;
//            case 1: // 7
//                if (randomNumber == 7) {
//                    coinsChange = 20;
//                } else {
//                    coinsChange = -10;
//                }
//                break;
//            case 2: // 8-11
//                if (randomNumber >= 8 && randomNumber <= 11) {
//                    coinsChange = 6;
//                } else {
//                    coinsChange = -3;
//                }
//                break;
//        }
//
//        coins += coinsChange;
//        resultMessage += "You " + (coinsChange > 0 ? "win " : "lose ") + Math.abs(coinsChange) + " coins!";
//
//        float rotationAngle = calculateRotationAngle(randomNumber);
//
//        spinnerWheel.setRotation(rotationAngle);
//        arrow.setRotation(rotationAngle);
//
//        tvResult.setText(resultMessage);
//        String finalResultMessage = resultMessage;
//        new CountDownTimer(2000, 1000) {
//            public void onTick(long millisUntilFinished) {}
//
//            public void onFinish() {
//                tvResult.setText("");
//                updateUI(finalResultMessage, String.valueOf(randomNumber));
//            }
//        }.start();
//    }
//
//    private int getSelectedOption() {
//        int selectedOptionId = numberOptionsGroup.getCheckedRadioButtonId();
//        if (selectedOptionId == rbOption1.getId()) {
//            return 0;
//        } else if (selectedOptionId == rbOption2.getId()) {
//            return 1;
//        } else if (selectedOptionId == rbOption3.getId()) {
//            return 2;
//        }
//        return -1;
//    }
//
//    private void updateUI(String resultMessage, String randomNumber) {
//        tvCoins.setText("Coins: " + coins);
//
//        Toast.makeText(SpinnerGame.this, "Random Number: " + randomNumber, Toast.LENGTH_SHORT).show();
//
//        if (coins <= 0) {
//            btnPlay.setEnabled(false);
//            Toast.makeText(this, "Game over! You ran out of coins.", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private float calculateRotationAngle(int randomNumber) {
//        float degreesPerOption = 360f / 12f;
//        return 360 - degreesPerOption * randomNumber;
//    }
//
//    @SuppressLint("MissingSuperCall")
//    @Override
//    public void onBackPressed() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt("currentCoins_" + currentUsername, coins);
//        editor.apply();
//
//        TextView textView = new TextView(this);
//        textView.setText("\nUsername: " + currentUsername + "\nCurrent Score: " + coins);
//        textView.setTextSize(16);
//        textView.setTextColor(Color.GREEN);
//        textView.setGravity(Gravity.CENTER);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Exit Application");
//        builder.setMessage("Are you sure you want to exit?");
//        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            String displayName = currentUser.getDisplayName();
//            if (displayName != null && !displayName.isEmpty()) {
//                textView.setText("\nUsername: " + displayName + "\nCurrent Score: " + coins);
//            } else {
//                textView.setText("\nUsername: No Name\nCurrent Score: " + coins);
//            }
//        } else {
//            textView.setText("\nUsername: " + currentUsername + "\nCurrent Score: " + coins);
//        }
//
//        builder.setView(textView);
//        builder.show();
//    }
//
//    private void loadSavedCoins() {
//        if (sharedPreferences.contains("currentCoins_" + currentUsername)) {
//            coins = sharedPreferences.getInt("currentCoins_" + currentUsername, 3000);
//        }
//        tvCoins.setText("Coins: " + coins);
//    }
//}
