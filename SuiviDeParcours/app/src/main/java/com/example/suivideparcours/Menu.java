package com.example.suivideparcours;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

public class Menu extends AppCompatActivity {

    private String numeroMarcheur = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void marcheur(View v) {
        Intent intentMarcheur = new Intent(this,Marcheur.class);
        startActivity(intentMarcheur);
    }

    public void suiveur(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Numéro du marcheur");

        EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.addTextChangedListener(new PhoneNumberFormattingTextWatcher("FR"));
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(input);

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.setPositiveButton("OK", (dialog, which) -> {
            numeroMarcheur = input.getText().toString();

            Intent intentSuiveur = new Intent(this,Suiveur.class);
            intentSuiveur.putExtra("numeroMarcheur",numeroMarcheur);
            startActivity(intentSuiveur);
        });

        builder.show();
    }

    public void quit(View v) {
        this.finish();
    }
}