package com.example.proyectochat7_0.Controladores;

/**
 * interfaz para escuchar cuando nos llegan estados (En linea, Escribiendo...)
 */
public interface OnSignalReceivedListener {
    public void onSignalReceived(String estado);
}
