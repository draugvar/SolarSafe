package com.steve.solarsafe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Settings extends Activity {

    private EditText text_a;
    private EditText text_b;
    private Button button_a;
    private Button button_b;
    private Button button_reset;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
    }

    private void init() {
        text_a = (EditText) findViewById(R.id.first_param);
        text_b = (EditText) findViewById(R.id.second_param);
        button_a = (Button) findViewById(R.id.button1);
        button_b = (Button) findViewById(R.id.button2);
        button_reset = (Button) findViewById(R.id.button_reset);
    }

    private void reset_text() {
        text_a.setHint(
                "reference parameter: " + settings.getFloat(
                        "param_a",Float.parseFloat(Bluetooth.paramA)));
        text_b.setHint(
                "reference parameter: " + settings.getFloat(
                        "param_b",Float.parseFloat(Bluetooth.paramB)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = getSharedPreferences(Bluetooth.deviceConnected.getAddress(), 0);
        if(Bluetooth.connectedThread != null){
            reset_text();
            button_a.setEnabled(true);
            button_b.setEnabled(true);
            button_reset.setEnabled(true);
        }
    }

    public void saveA(View view) {
        String parameter = text_a.getText().toString().trim();
        if(!parameter.equals("")) {
            if (isFloatNumber(parameter)) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putFloat("param_a", Float.parseFloat(parameter));
                // Commit the edits!
                editor.apply();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Not a Number!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Empty Field!", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveB(View view) {
        String parameter = text_b.getText().toString().trim();
        if(!parameter.equals("")) {
            if (isFloatNumber(parameter)) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putFloat("param_b", Float.parseFloat(parameter));
                // Commit the edits!
                editor.apply();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Not a Number!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Empty Field!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isFloatNumber(String num){
        try{
            Double.parseDouble(num);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public void reset(View view) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("param_a");
        editor.remove("param_b");
        editor.apply();
        reset_text();
        Toast.makeText(this, "Reset to default values", Toast.LENGTH_SHORT).show();
    }
}
