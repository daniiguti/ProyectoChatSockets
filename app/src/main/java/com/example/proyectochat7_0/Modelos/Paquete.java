package com.example.proyectochat7_0.Modelos;

import java.io.Serializable;

public class Paquete implements Serializable{
    //lo que vamos a enviar en el paquete
    private String nick;
    private int idmensaje;
    private String mensaje;
    //atributo para saber que se va a hacer con este paquete
    // 0-> elimina, 1->añade, en un futuro se añadira 2-> modifica
    private int accion;

    public Paquete(String nick, int idmensaje, String mensaje, int accion) {
        this.nick = nick;
        this.idmensaje = idmensaje;
        this.mensaje = mensaje;
        this.accion = accion;
    }

    //Getters y setter
    public String getNick() {
        return nick;
    }
    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getMensaje() {
        return mensaje;
    }
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getAccion() {
        return accion;
    }
    public void setAccion(int accion) {
        this.accion = accion;
    }

    public int getIdmensaje() {
        return idmensaje;
    }
    public void setIdmensaje(int idmensaje) {
        this.idmensaje = idmensaje;
    }
}
