package com.example.proyectochat7_0.Controladores;

import java.io.IOException;

/**
 * interfaz para controlar cuando el usuario meta direcciones ips invalidas
 */
public interface OnConnectionErrorListener {
    /**
     * para avisar de que NO se conecto
     * @param ex -> excepcion que nos devuelve
     */
    public void onExcepcion(IOException ex);

}
