package com.example.proyectochat7_0.Controladores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.captaindroid.tvg.Tvg;
import com.example.proyectochat7_0.R;


public class ActivityLogin extends AppCompatActivity {

    private Button btSiguiente;
    private Button btPreferencias;
    private EditText txtAlias;
    private TextView tvSocketsChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        btSiguiente = (Button) findViewById(R.id.btSiguiente);
        btPreferencias = (Button) findViewById(R.id.btPreferencias);
        txtAlias = (EditText) findViewById(R.id.txtAlias);
        tvSocketsChat = (TextView) findViewById(R.id.tvSocketsChat);

        //para pasar a la actividad del chat
        btSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //comprobamos que no deje vacio el area de texto del alias
                String auxAlias = txtAlias.getText().toString();
                if(auxAlias != null && auxAlias.equals("") == false){
                    Intent i = new Intent(getApplicationContext(), ActivityMain.class);
                    i.putExtra("alias", auxAlias);
                    startActivity(i);
                }
                else{
                    showToast("No deje el alias vacío");
                }
            }
        });

        //para abrir la ventana de preferencias
        btPreferencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i2 = new Intent(getApplicationContext(), ActivityPreferences.class);
                startActivity(i2);
            }
        });

        //Cambio de color gradiente del text view(SocketsChat) (con una biblioteca externa)
        Tvg.change(tvSocketsChat, new int[]{
                Color.parseColor("#228B22"),
                Color.parseColor("#32CD32"),
                Color.parseColor("#90EE90"),
        });
    }

    //metodo on resume por si actualizamos las preferencias
    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
    }

    /**
     * metodo que carga las preferencias, con el diseño ya realizado en values/themes
     */
    public void loadPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String colorFondo = sharedPreferences.getString("preferences_tema","Light");
        switch (colorFondo){
            case "Light":
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Night":
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    private void showToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
