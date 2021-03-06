package com.example.project.userManagement;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.example.project.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView label = (TextView) findViewById(R.id.testo);

        mAuth = FirebaseAuth.getInstance();
        if(TextUtils.isEmpty(mAuth.getCurrentUser().getDisplayName()))
            label.setText("Benvenuto "+mAuth.getCurrentUser().getEmail());
        else
            label.setText("Benvenuto "+mAuth.getCurrentUser().getDisplayName()+"!");
        mAuth.signOut();
    }
}
