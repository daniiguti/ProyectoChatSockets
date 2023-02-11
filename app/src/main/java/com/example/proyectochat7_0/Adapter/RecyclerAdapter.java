package com.example.proyectochat7_0.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectochat7_0.Modelos.Mensaje;
import com.example.proyectochat7_0.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder>{
    //Atributos de nuestra clase
    private List<Mensaje> listMensajes;
    private View.OnLongClickListener longListener;

    //Constructor
    public RecyclerAdapter(List<Mensaje> listMensajes){
        this.listMensajes = listMensajes;
    }

    //Setter del Listener
    public void setOnLongClickListener(View.OnLongClickListener listener){ this.longListener = listener; }

    //Esto "infla" cada celda del recyclerView con nuestro diseño
    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mensajes,parent, false);
        RecyclerHolder recyclerHolder = new RecyclerHolder(view);
        view.setOnLongClickListener(longListener);
        return recyclerHolder;
    }

    //Esto junta cada Libro del arrayList con el diseño de cada celda
    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        Mensaje msg = this.listMensajes.get(position);
        String propietario = msg.getPropietario();
        if (propietario.equals("enviado")) {
                holder.txtMensajeEnviado.setText(msg.getMensaje());
                holder.txtMensajeRecibido.setVisibility(View.GONE);
        } else {
                holder.txtMensajeRecibido.setText(msg.getMensaje());
                holder.txtMensajeEnviado.setVisibility(View.GONE);
        }
        //Para decirle al holder que no recicle celdas, ya que al ser un RecyclerView, este "recicla"
        //las celdas para ser mas optimo, pero al ser un chat de whatsapp, nos interesa que no se pierdan
        //los mensajes, y por tanto que no los recicle
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return listMensajes.size();
    }

    //Enlazamos los elementos del diseño en relacion a nuestra clase
    public class RecyclerHolder extends RecyclerView.ViewHolder {
        TextView txtMensajeEnviado;
        TextView txtMensajeRecibido;

        public RecyclerHolder(@NonNull View itemView) {
            super(itemView);
            txtMensajeEnviado = (TextView) itemView.findViewById(R.id.tvEnviado);
            txtMensajeRecibido = (TextView) itemView.findViewById(R.id.tvRecibido);
        }
    }

    //Métodos auxiliares para modificar el array
    //Para borrar de nuestro arrayList
    public void delete(int id, String propietario){
        for(int i = 0; i < this.listMensajes.size(); i++) {
            if (this.listMensajes.get(i).getId() == id && this.listMensajes.get(i).getPropietario().equals(propietario)) {
                this.listMensajes.remove(i);
                this.notifyDataSetChanged();
            }
        }
    }
    //Para insertar en nuestro arrayList
    public void insertar(Mensaje msg){
        this.listMensajes.add(msg);
        this.notifyDataSetChanged();
    }

    //Para borrar nuestro arrayList
    public void clear(){
        listMensajes.clear();
    }
    //Para devolver de nuestro arrayList
    public Mensaje devolverMensaje(int posicion){
        return this.listMensajes.get(posicion);
    }

    /**
     * metodo que calcula un id nuevo aleatorio para el mensaje, este comprueba que ese id no se ha usado ya,
     * si ya esta en el list, se genera otro distinto
     * @return -> id del mensaje
     */
    public int calcularId(){
        int num = 0;
        boolean salir = false;
        boolean serepite = false;
        do {
            salir = false;
            serepite = false;
            num = (int) (Math.random() * 100000000);
            for(int i = 0; i < this.listMensajes.size() && salir == false; i++){
                int id = this.listMensajes.get(i).getId();
                if(id == num){
                    salir = true;
                    serepite = true;
                }
            }
        }while(serepite == true);

        return num;
    }
}
