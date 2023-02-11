package com.example.proyectochat7_0.Controladores;

import android.os.Handler;
import android.os.Looper;

import com.example.proyectochat7_0.Modelos.Paquete;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Patron delegado, delegamos el trabajo de procesar los mensajes
 * en esta clase, independizando el manejo de las redes de la logica
 * de la aplicacion(MainActivity), y avisamos a esta con listeners
 */
public class DelegateChat {
    //tiene que haber puertos distintos puesto que si los utilizamos tanto como
    //para enviar/recibir mensajes como para enviar/recibir estados, nos va a saltar un error
    //de que ese puerto ya esta cogido
    private static int PORT_MENSAJES = 20001;
    private static int PORT_ESTADOS = 20002;

    //Listeners necesarios, para avisar a nuestro MainActivity
    private OnMessageReceivedListener listenerMensajes;
    private OnSignalReceivedListener listenerEstados;
    private OnConnectionErrorListener listenerExcepcion;

    //clase Handler, permite ejecutar codigo en un hilo, por eso le pasamos por parametro
    //una instancia del hilo principal(funciona como el runOnUiThread, pero este no necesita
    // que se extienda de AppComcapActivity)
    private final Handler handler = new Handler(Looper.getMainLooper());

    //atributos necesarios para crearnos el chatDelegado
    private String alias;
    private String ip;

    //semaforo para parar los hilos infinitos
    private boolean stop;

    //sockets servidores inicializados aqui para poderlos parar
    private ServerSocket servidorMensajes;
    private ServerSocket servidorSenales;

    /**
     * Constructor
     * @param ip -> hacia donde vamos a enviar cosass
     * @param alias -> nuestro alias para ser identificados a quien le enviemos
     */
    public DelegateChat(String ip, String alias) {
        this.alias = alias;
        this.ip = ip;
        this.stop = false;
    }

    //Getters y setters de los listeners
    public OnMessageReceivedListener getListenerMensajes() {
        return listenerMensajes;
    }
    public void setListenerMensajes(OnMessageReceivedListener listenerMensajes) {
        this.listenerMensajes = listenerMensajes;
    }

    public OnSignalReceivedListener getListenerEstados() {
        return listenerEstados;
    }
    public void setListenerEstados(OnSignalReceivedListener listenerEstados) {
        this.listenerEstados = listenerEstados;
    }

    public OnConnectionErrorListener getListenerExcepcion() {
        return listenerExcepcion;
    }
    public void setListenerExcepcion(OnConnectionErrorListener listenerExcepcion) {
        this.listenerExcepcion = listenerExcepcion;
    }

    /**
     * @param ip -> nueva ip, para actualizar desde MainActivity
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * metodo para enviar mensaje a la direccion ip asociada en el setter
     * @param mensaje -> el mensaje que queremos enviar
     * @param id -> id asociado al mensaje, para poder eliminarlo en un futuro
     */
    public void enviarMensajes(String mensaje, int id) {
        Thread hiloEnviarMensajes = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket misocket = new Socket(ip, PORT_MENSAJES);
                    Paquete datos = new Paquete(alias, id, mensaje, 1);
                    ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
                    paquete_datos.writeObject(datos);
                    misocket.close();
                } catch (IOException ex) {
                    //si da un IOException significa que no se pudo conectar a esa ip, por lo que avisamos
                    //al mainActivity
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listenerExcepcion.onExcepcion(ex);
                        }
                    });
                }
            }
        });
        hiloEnviarMensajes.start();
    }

    /**
     * metodo para recibir peticiones, se pueden recibir peticiones de eliminar
     * o de añadir mensajes, estara siempre activo(bucle infinito) puesto
     * que no sabemos cuando nos pueden enviar mensajes
     */
    public void recibirMensajes() {
        Thread hiloRecibirMensajes = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    servidorMensajes = new ServerSocket(PORT_MENSAJES);
                    Paquete paquete_recibido;
                    while (!stop) {
                        //aceptamos el paquete
                        Socket misocket = servidorMensajes.accept();
                        ObjectInputStream paquete_datos = new ObjectInputStream(misocket.getInputStream());
                        paquete_recibido = (Paquete) paquete_datos.readObject();
                        //para obtener la ip de quien nos lo envio
                        InetAddress id = misocket.getInetAddress();
                        String auxnombre = paquete_recibido.getNick() + " - " + id.toString();
                        String auxmensaje = paquete_recibido.getMensaje();
                        int idmensaje = paquete_recibido.getIdmensaje();
                        //Podemos hacer 2 cosas: eliminar o añadir (en un futuro implementare modificar)
                        int accion = paquete_recibido.getAccion();
                        if (listenerMensajes != null) {
                            switch(accion){
                                //eliminar
                                case 0:
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            listenerMensajes.onEliminateMessage(idmensaje);
                                        }
                                    });
                                    break;
                                //añadir
                                case 1:
                                    //el metodo post permite ejecutar codigo en un hilo, como ya tenemos especificado
                                    //el hilo que es (el hilo principal) ejecutamos: listener.onMessageReceived(auxnombre, auxmensaje);
                                    //puesto que es un metodo que va a tocar la interfaz y por lo tanto tiene que ser ejecutado
                                    //en el hilo principal, (funciona igual que el metodo runOnUiThread)
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            listenerMensajes.onMessageReceived(auxnombre, auxmensaje, idmensaje);
                                        }
                                    });
                                    break;
                            }
                        }
                        misocket.close();
                    }
                    //servidor.close();
                    //System.out.println("SE HAN CERRADO LOS SERVIDORES DE MENSAJES");
                } catch (IOException ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                    System.out.println("port: " + PORT_MENSAJES);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        hiloRecibirMensajes.start();
    }

    /**
     * metodo para enviar estados
     * @param estado_enviado -> enviar un estado, en linea o escribiendo
     */
    public void enviarSenal(String estado_enviado){
        Thread hiloEnviarSenales = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    Socket misocket = new Socket(ip, PORT_ESTADOS);
                    DataOutputStream estado = new DataOutputStream(misocket.getOutputStream());
                    estado.writeUTF(estado_enviado);
                    misocket.close();
                } catch (IOException ex) {
                    //si da un IOException significa que no se pudo conectar a esa ip por lo que avisamos a nuestro main
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listenerExcepcion.onExcepcion(ex);
                        }
                    });
                }
            }
        });
        hiloEnviarSenales.start();
    }

    /**
     * metodo para recibir estados, es decir, en linea o escribiendo
     */
    public void recibirSenal(){
        Thread hiloRecibirSenales = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    servidorSenales = new ServerSocket(PORT_ESTADOS);
                    while (!stop) {
                        Socket misocket = servidorSenales.accept();
                        DataInputStream estado = new DataInputStream(misocket.getInputStream());
                        String estado_recibido = estado.readUTF();

                        if (listenerEstados != null) {
                            //el metodo post permite ejecutar codigo en un hilo, como ya tenemos especificado
                            //el hilo que es (el hilo principal) ejecutamos: listener.onMessageReceived(auxnombre, auxmensaje);
                            //puesto que es un metodo que va a tocar la interfaz y por lo tanto tiene que ser ejecutado
                            //en el hilo principal
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listenerEstados.onSignalReceived(estado_recibido);
                                }
                            });
                        }
                        misocket.close();
                    }
                    //Aqui se deberia de cerrar cuando para el bucle infinito, pero no lo hace(supongo que es por la gestion que hace
                    // el sistema de los hilos)
                    //servidor.close();
                    //System.out.println("SE HAN CERRADO LOS SERVIDORES DE SEÑAL");
                } catch (IOException ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                    System.out.println("port: "  + PORT_ESTADOS);
                }
            }
        });
        hiloRecibirSenales.start();
    }

    /**
     * metodo para eliminar mensajes
     */
    public void eliminarMensaje(String mensaje, int idmensaje) {
        Thread hiloEliminarMensajes = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket misocket = new Socket(ip, PORT_MENSAJES);
                    Paquete datos = new Paquete(alias, idmensaje, mensaje, 0);
                    ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
                    paquete_datos.writeObject(datos);
                    misocket.close();
                } catch (IOException ex) {
                    //si da un IOException significa que no se pudo conectar a esa ip
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listenerExcepcion.onExcepcion(ex);
                        }
                    });
                }
            }
        });
        hiloEliminarMensajes.start();
    }

    /**
     * metodo para parar los hilos infinitos y ademas los servidores
     * @throws -> IOException por si los servidores no se pudieron cerrar
     */
    public void stopServidores() throws IOException {
        stop = true;
        //por alguna razon que desconozco, al parar el bucle infinito no hace el .close() alli,
        //la mejor manera de pararlos seria, cuando activas el semaforo, y para de hacer el bucle infinito,
        //pero no lo hace, asiq hace falta declararlos de tal forma que sean visibles para pararlos aqui,
        //que aqui si que los para
        servidorMensajes.close();
        servidorSenales.close();
    }
}

