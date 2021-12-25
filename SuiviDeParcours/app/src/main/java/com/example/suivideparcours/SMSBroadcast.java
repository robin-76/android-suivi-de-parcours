package com.example.suivideparcours;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSBroadcast extends BroadcastReceiver {
    private static String SMS = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(SMS)) {
            Bundle bundle = intent.getExtras();

            Object[] objects = (Object[]) bundle.get("pdus");

            SmsMessage messages = null;

            for(int i=0; i<objects.length; i++)
                messages = SmsMessage.createFromPdu((byte[]) objects[i]);

            if(messages.getMessageBody().equals("Oui")){
                Toast.makeText(context, "Vous pouvez suivre mon parcours",
                        Toast.LENGTH_SHORT).show();

                Intent intentSuiveur = new Intent(context,Suiveur.class);
                intentSuiveur.putExtra("numeroMarcheur",messages.getDisplayOriginatingAddress());
                intentSuiveur.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentSuiveur);
            }

            else if(messages.getMessageBody().equals("Non"))
                Toast.makeText(context, "Vous ne pouvez pas suivre mon parcours",
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Réponse incorrecte",
                        Toast.LENGTH_SHORT).show();
        }
    }
}
