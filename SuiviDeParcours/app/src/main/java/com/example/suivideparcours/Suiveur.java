package com.example.suivideparcours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Suiveur extends AppCompatActivity {

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suiveur);

        Intent intentAppelant = getIntent();
        String numeroMarcheur = intentAppelant.getStringExtra("numeroMarcheur");

        tv = findViewById(R.id.numero);
        tv.setText("Suivi de : "+numeroMarcheur);
    }
}

