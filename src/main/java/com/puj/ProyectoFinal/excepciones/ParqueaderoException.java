package com.puj.ProyectoFinal.excepciones;

public class ParqueaderoException extends Exception {

    private static final long serialVersionUID = 1L;

    public ParqueaderoException(String mensaje) {
        super(mensaje);
    }
}
