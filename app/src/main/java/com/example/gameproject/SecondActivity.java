    package com.example.gameproject;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.ActionBarDrawerToggle;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.AppCompatButton;
    import androidx.appcompat.widget.Toolbar;
    import androidx.cardview.widget.CardView;
    import androidx.core.view.GravityCompat;
    import androidx.drawerlayout.widget.DrawerLayout;

    import android.annotation.SuppressLint;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.bumptech.glide.Glide;
    import com.google.android.gms.auth.api.signin.GoogleSignIn;
    import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
    import com.google.android.gms.auth.api.signin.GoogleSignInClient;
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.android.material.navigation.NavigationView;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.util.Currency;

    public class SecondActivity extends AppCompatActivity {
        TextView nametext, fName;
        ImageView pimage;
        GoogleSignInClient mGoogleSignInClient;
        CardView cardFirst, cardSecond, cardThird, cardFourth;
        private FirebaseAuth mAuth;
        private FirebaseFirestore db;

        @SuppressLint("MissingInflatedId")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_second);

            nametext = findViewById(R.id.nametext);
            pimage = findViewById(R.id.pimage);
            cardFirst = findViewById(R.id.cardFirst);
            cardSecond = findViewById(R.id.cardSecond);
            cardThird = findViewById(R.id.cardThird);
            cardFourth = findViewById(R.id.cardFourth);
            fName = findViewById(R.id.fName);

            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            fName.setVisibility(View.VISIBLE);

            String usernameF = getIntent().getStringExtra("username");

            if (usernameF != null && !usernameF.isEmpty()) {
                fName.setText(usernameF);
                nametext.setVisibility(View.GONE);
                fName.setVisibility(View.VISIBLE);
            } else {
                // If username is not passed, retrieve it from Firestore
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    DocumentReference docRef = db.collection("users").document(user.getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                String retrievedUsername = task.getResult().getString("username");
                                fName.setText(retrievedUsername);
                                nametext.setVisibility(View.GONE);
                                fName.setVisibility(View.VISIBLE);

                            } else {
                                Toast.makeText(SecondActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SecondActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            pimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("Logout?");
                    builder.setMessage("Are you sure you want to logout?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            mGoogleSignInClient.signOut().addOnCompleteListener(SecondActivity.this, task -> {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            });
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.show();
                }
            });

            cardFirst.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), LudoSplash.class);
                    intent.putExtra("userName", nametext.getText().toString());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            });

            cardSecond.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SecondActivity.this, "Not Available in This Time..", Toast.LENGTH_SHORT).show();
                }
            });

            cardThird.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SplashSpin.class);
                intent.putExtra("username", nametext.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            });

            cardFourth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(SecondActivity.this, "", Toast.LENGTH_SHORT).show();
                }
            });

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                String username = account.getDisplayName(); // Retrieve the username
                // Save the username to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.apply();

                SharedPreferences sharedPreferences1 = getSharedPreferences("myGame", MODE_PRIVATE);
                editor.putString("usernameS", username);
                fName.setText(account.getDisplayName());

                // Navigate to LudoFirstGame activity
                nametext.setText(account.getDisplayName());
                Glide.with(this).load(account.getPhotoUrl()).into(pimage);
                nametext.setVisibility(View.VISIBLE);
                fName.setVisibility(View.GONE);
            }

        }
        // Your existing code

        @SuppressLint("MissingSuperCall")
        @Override
        public void onBackPressed() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Exit Application?");
            builder.setMessage("Do You Want to Exit This Application?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();

        }

    }