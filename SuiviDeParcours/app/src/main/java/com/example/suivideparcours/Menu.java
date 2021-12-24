package com.example.suivideparcours;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Menu extends AppCompatActivity {

    private static final int PERMISSION_FOR_SMS = 0;
    AlertDialog dialog;
    private String numeroMarcheur = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void verifyPermissions() {
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog.dismiss();
            dialog.setOnShowListener(dialog1 -> ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true));
            dialog.show();
        }
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SEND_SMS}, PERMISSION_FOR_SMS);
        else {
            dialog.dismiss();
            dialog.setOnShowListener(dialog1 -> ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true));
            dialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String permissions[], int grantResults[]) {
        switch (code) {
            case PERMISSION_FOR_SMS: {
                boolean ok = true;
                if(grantResults.length > 0) {
                    for(int res=0; res<grantResults.length; res++) {
                        if(grantResults[res] != PackageManager.PERMISSION_GRANTED)
                            ok = false;
                    }
                } else ok = false;
                if(ok) {
                    Toast.makeText(this, "SMS autorisée", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    dialog.setOnShowListener(dialog1 -> ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true));
                }
                else {
                    Toast.makeText(this, "SMS non autorisée", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    dialog.setOnShowListener(dialog1 -> ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));
                }
                dialog.show();
            }
        }
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

            Intent smsIntent = new Intent(Intent.ACTION_VIEW);

            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address"  , numeroMarcheur);
            smsIntent.putExtra("sms_body"  , "Puis-je suivre ton parcours ? Oui | Non");

            try {
                startActivity(smsIntent);
                finish();
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(Menu.this,
                        "SMS failed, please try again later.", Toast.LENGTH_SHORT).show();
            }

            /*Intent intentSuiveur = new Intent(this,Suiveur.class);
            intentSuiveur.putExtra("numeroMarcheur",numeroMarcheur);
            startActivity(intentSuiveur);*/
        });

        dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));
        verifyPermissions();
        dialog.show();
    }

    public void historique(View v) {
        Intent intentHistorique = new Intent(this,Historique.class);
        startActivity(intentHistorique);
    }

    public void quit(View v) {
        this.finish();
    }
}