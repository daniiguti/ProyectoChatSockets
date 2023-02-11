package com.example.proyectochat7_0.Controladores;

/**
 * Interfaz que usaremos de listener para escuchar cuando el patron delegado nos manda las peticiones
 */
public interface OnMessageReceivedListener {

    /**
     * metodo para cuando vamos a insertar un mensaje
     * @param auxnombre -> el nombre de quien nos lo envio
     * @param auxmensaje -> el mensaje
     * @param id -> el id del mensaje (para poder eliminarlo en un futuro)
     */
    public void onMessageReceived(String auxnombre, String auxmensaje, int id);

    /**
     * metodo para poder eliminar mensajes
     * @param id -> el id del mensaje que se va a eliminar
     */
    public void onEliminateMessage(int id);
}
