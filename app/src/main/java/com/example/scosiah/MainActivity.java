package com.example.scosiah;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Scosiah E-Card Verification");
    }

    // METHOD ZIMEWEKWA NDANI YA CLASS SAHIHI
    public void openSearch(View view) {
        Intent intent = new Intent(this, ActivityManualSearch.class);
        startActivity(intent);
    }

    public void openScanner(View view) {
        Intent intent = new Intent(this, ActivityQrScanner.class);
        startActivity(intent);
    }
}
