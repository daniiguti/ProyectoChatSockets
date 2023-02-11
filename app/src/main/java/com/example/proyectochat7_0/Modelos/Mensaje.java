package com.example.proyectochat7_0.Modelos;

public class Mensaje {
    //id -> un id unico para poder eliminar este mensaje
    private int id;
    private String mensaje;
    //propietario -> enviado o recibido, para identificarlo
    //este atributo es necesario en muchos sitios, a la hora de eliminar,
    //a la hora de añadirlo (ya que tenemos dos diseños, podemos eliminar mensajes, etc)
    private String propietario;

    public Mensaje(String mensaje, String propietario, int id) {
        this.mensaje = mensaje;
        this.propietario = propietario;
        this.id = id;
    }

    //getters y setters
    public String getMensaje() {
        return mensaje;
    }
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPropietario() {
        return propietario;
    }
    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
