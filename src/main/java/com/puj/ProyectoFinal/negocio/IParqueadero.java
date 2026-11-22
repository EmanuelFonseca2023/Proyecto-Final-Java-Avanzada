package com.puj.ProyectoFinal.negocio;

import com.puj.ProyectoFinal.excepciones.ParqueaderoException;
import com.puj.ProyectoFinal.modelo.Convenio;
import com.puj.ProyectoFinal.modelo.Mensualidad;
import com.puj.ProyectoFinal.modelo.Tiquete;
import com.puj.ProyectoFinal.modelo.Vehiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IParqueadero {

    void agregarMensualidad(Mensualidad m) throws ParqueaderoException;

    Tiquete registrarEntrada(Vehiculo v, LocalDateTime entrada) throws ParqueaderoException;

    Tiquete registrarSalida(String placa, LocalDateTime salida, Convenio convenio)
            throws ParqueaderoException;

    List<String> reporteVehiculosAdentro();

    List<String> reporteCierre(LocalDate fecha);

    List<String> reportePlacasFrecuentes();

    List<Tiquete> getTiquetes();
}