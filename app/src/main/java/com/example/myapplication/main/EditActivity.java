package com.example.myapplication.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.LoginActivity;
import com.example.myapplication.OwnerHomeActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private Button editButton;
    private String userIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        editButton = findViewById(R.id.edit);

        Intent intent = getIntent();
        userIndex = intent.getStringExtra("userIndex");
        Log.i("userIndex", userIndex);

        editButton.setOnClickListener(view -> {
            ref = FirebaseDatabase.getInstance().getReference();
            Query lastQuery = ref.child("user");
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot){
                    updateData(userIndex, dataSnapshot.child(userIndex).child("password").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });
        });
    }

    private void updateData(String userIndex, String passwordAuth) {
        EditText username = (EditText) findViewById(R.id.et_username);
        EditText email = (EditText) findViewById(R.id.et_email);
        EditText password = (EditText) findViewById(R.id.et_password);
        EditText password2 = (EditText) findViewById(R.id.et_confirmpass);

        if(!password.getText().toString().equals(password2.getText().toString())){
            Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            password.setText(null);
            password2.setText(null);
        }
        else if(!isValidEmailAddress(email.getText().toString())){
            Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
        }
        else if(email.getText().toString().equals("") ||
                username.getText().toString().equals("") ||
                password.getText().toString().equals("") ||
                password2.getText().toString().equals("")){
            Toast.makeText(EditActivity.this, "Fill all cells!", Toast.LENGTH_SHORT).show();
        }
        else {
            ref = FirebaseDatabase.getInstance().getReference();
            String username_str = username.getText().toString();
            String mail = email.getText().toString();
            String pass = password.getText().toString();
            ref.child("user").child(userIndex).child("username").setValue(username_str);
            ref.child("user").child(userIndex).child("email").setValue(mail);
            ref.child("user").child(userIndex).child("password").setValue(pass);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), passwordAuth);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            updateEmail(mail);
                            Toast.makeText(EditActivity.this, "All changes were applied!", Toast.LENGTH_SHORT).show();
                            //updatePassword();
                        }
                    });
            Intent gotoHome = new Intent(EditActivity.this, OwnerHomeActivity.class);
            gotoHome.putExtra("userIndex", userIndex);
            startActivity(gotoHome);
        }
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^.+@.+\\..+$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    /*public void updatePassword() {
        // [START update_password]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String newPassword = "kynodontas4";

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Hola", "User password updated.");
                        }
                    }
                });
        // [END update_password]
    }*/

    public void updateEmail(String email){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditActivity.this, "Account was successfully deleted!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
