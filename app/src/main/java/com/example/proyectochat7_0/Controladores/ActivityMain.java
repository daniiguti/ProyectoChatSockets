package com.example.proyectochat7_0.Controladores;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectochat7_0.Adapter.RecyclerAdapter;
import com.example.proyectochat7_0.Modelos.Mensaje;
import com.example.proyectochat7_0.R;

import java.io.IOException;
import java.util.ArrayList;

public class ActivityMain extends AppCompatActivity{
    //Componentes de la interfaz
    private EditText txtMensaje;
    private TextView tvQuienNosEscribe;
    private TextView tvEstado;
    private TextView txtIP;
    private RecyclerView recyclerView;
    private Button btEnviar;

    //Clase delegado(encargada de gestionar las redes)
    private DelegateChat chatDelegado;

    //nuestro nombre para que los demas nos reconozcan
    private String alias;

    //Adapter para el recycler view
    private RecyclerAdapter recAdapter;
    private ArrayList<Mensaje> mensajes;

    //Nuestros listener necesarios
    private OnMessageReceivedListener listenerMensajes;
    private OnSignalReceivedListener listenerEstados;
    private OnConnectionErrorListener listenerExcepcion;

    private ActionMode actionMode;
    private int posicionPulsada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        Intent i = getIntent();
        alias = i.getStringExtra("alias");

        //creacion de nuestro objeto delegado
        chatDelegado = new DelegateChat("", alias);

        //Componentes de la interfaz
        txtMensaje = (EditText) findViewById(R.id.txtMensaje);
        tvEstado = (TextView) findViewById(R.id.tvEstado);
        tvQuienNosEscribe = (TextView) findViewById(R.id.tvNosEscriben);
        txtIP = (TextView) findViewById(R.id.txtIP2);
        recyclerView = (RecyclerView) findViewById(R.id.rvChat);
        btEnviar = (Button) findViewById(R.id.btEnviar);
        btEnviar.setEnabled(false);

        //Inicializacion de nuestro adapter
        mensajes = new ArrayList<>();
        recAdapter = new RecyclerAdapter(mensajes);

        //Listener del recAdapter para cuando dejemos pulsado se nos habra un menú de acción
        //para eliminar un mensaje
        recAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                posicionPulsada = recyclerView.getChildAdapterPosition(view);
                //Hay que pasarle la interfaz implementada mas abajo
                actionMode = startActionMode(actionModeCallback);
                view.setSelected(true);
                return true;
            }
        });
        //Layout para que salga en forma de lista el recycler View
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Le ponemos al recyclerView el adapter que contiene el arraylist
        recyclerView.setAdapter(recAdapter);

        //Listeners necesarios para el manejo de la aplicacion
        //MENSAJES
        listenerMensajes = new OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String auxnombre, String auxmensaje, int idmensaje) {
                //actualizamos los datos de quien nos escribio y ademas añadimos su mensaje a nuestro recView
                tvQuienNosEscribe.setText(auxnombre);
                recAdapter.insertar(new Mensaje(auxmensaje, "recibido", idmensaje));
                //para mover automaticamente el recycler view a la ultima posicion
                recyclerView.smoothScrollToPosition(recAdapter.getItemCount() - 1);
            }

            @Override
            public void onEliminateMessage(int id){
                //eliminamos el mensaje de nuestro recView, con su id y con el atrb recibido,
                //ya que si por ej nos hablamos a nosotros mismos (localhost) daria fallo ya que eliminaria
                //el que no tiene que eliminar
                recAdapter.delete(id, "recibido");
            }
        };
        //ESTADOS
        listenerEstados = new OnSignalReceivedListener() {
            @Override
            public void onSignalReceived(String estado) {
                //para recibir los estados(Escribiend..., En linea)
                tvEstado.setText(estado);
            }
        };
        //EXCEPCIONES
        listenerExcepcion = new OnConnectionErrorListener() {
            @Override
            public void onExcepcion(IOException ex) {
                //si da una exepcion significa que esa ip es invalida, por lo que avisamos,
                //y hacemos otros ajustes como que no pueda escribir, etc
                txtIP.setText("");
                txtMensaje.setText("");
                btEnviar.setEnabled(false);
                showToast("Dirección inválida");
            }
        };

        //Poner al chatDelegado esos listeners
        chatDelegado.setListenerEstados(listenerEstados);
        chatDelegado.setListenerMensajes(listenerMensajes);
        chatDelegado.setListenerExcepcion(listenerExcepcion);

        //ponemos a escuchar tanto mensajes como señales
        chatDelegado.recibirSenal();
        chatDelegado.recibirMensajes();

        //cuando pulsemos el boton enviar, se insertara en nuestro adapter y ademas llamaremos al metodo
        //del delegado para enviar ese mensaje
        btEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //metodo para calcular un id, que va asociado al mensaje
                int id = recAdapter.calcularId();
                recAdapter.insertar(new Mensaje(txtMensaje.getText().toString(), "enviado", id));
                chatDelegado.enviarMensajes(txtMensaje.getText().toString(), id);
                txtMensaje.setText("");

                //para mover automaticamente el recyclre view a la ultima posicion
                recyclerView.smoothScrollToPosition(recAdapter.getItemCount() - 1);
            }
        });

        //listener para activar o desactivar el boton de enviar, en el caso de que el edittext este vacio,
        //y ademas acualizar el estado
        txtMensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Para cuando se cambie el texto, si escribio algo se le activa el boton de enviar
                if (txtMensaje.getText().toString().equals("") == false && txtIP.getText().toString().equals("") == false){
                    chatDelegado.enviarSenal("Escribiendo...");
                    btEnviar.setEnabled(true);
                }
                //sino hay nada escrito se desactiva y se pone en linea
                else{
                    if (txtIP.getText().toString().equals("") == false) {
                        btEnviar.setEnabled(false);
                        chatDelegado.enviarSenal("En linea");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Listener para que cada vez que se cambie el texto de la IP, actualizar nuestra ip en nuestro chatDelegado
        txtIP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                chatDelegado.setIp(txtIP.getText().toString());
            }
        });

        //cargamos las preferencias
        loadPreferences();
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    //Interfaz para el MENU DE ACCION
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_accion, menu);
            return true;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.btEliminar:
                    //obtenemos el mensaje que se pulso, para pasarselo al alert dialog
                    Mensaje mensaje = recAdapter.devolverMensaje(posicionPulsada);
                    AlertDialog alertDialog = createAlertDialogEliminar(mensaje);
                    alertDialog.show();
                    mode.finish();
                    break;
            }
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };

    //alertdialog para preguntar cuando este quiera eliminar el mensaje
    public AlertDialog createAlertDialogEliminar(Mensaje mensaje){
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);

        builder.setMessage("¿Seguro que desea eliminar?");
        builder.setTitle("Eliminar");

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //si el mensaje es enviado, es decir, es nuestro lo eliminamos del adapter y ademas a la otra persona
                if(mensaje.getPropietario().equals("enviado")) {
                    //mandamos la peticion al server para que lo elimine en el otro dispositivo
                    chatDelegado.eliminarMensaje(mensaje.getMensaje(), recAdapter.devolverMensaje(posicionPulsada).getId());
                    //lo eliminamos de nuestro adapter
                    recAdapter.delete(mensaje.getId(), "enviado");
                }else{
                    //si el mensaje NO es nuestro solo lo eliminamos del adapter
                    recAdapter.delete(mensaje.getId(), "recibido");
                }
                showToast("Mensaje eliminado");
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showToast("Mensaje no eliminado");
            }
        });

        return builder.create();
    }

    /**
     * Cargamos las preferencias, usamos un fondo y unos ajustes de diseño
     * que ya tenemos hechos en values/themes
     */
    public void loadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String colorFondo = sharedPreferences.getString("preferences_tema", "Light");
        View view = this.getWindow().getDecorView();
        switch (colorFondo) {
            case "Light":
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                view.setBackgroundResource(R.drawable.fondo_claro);
                break;
            case "Night":
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                view.setBackgroundResource(R.drawable.fondo_oscuro);
                break;
        }
    }

    //en el onDestroy paramos los hilos que estan en bucles infinitos y cerramos los servidores
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.chatDelegado.stopServidores();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}