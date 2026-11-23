package com.puj.ProyectoFinal.negocio;

import com.puj.ProyectoFinal.excepciones.FechaInvalidaException;
import com.puj.ProyectoFinal.excepciones.ParqueaderoException;
import com.puj.ProyectoFinal.excepciones.PlacaInvalidaException;
import com.puj.ProyectoFinal.excepciones.VehiculoNoEncontradoException;
import com.puj.ProyectoFinal.excepciones.VehiculoYaAdentroException;
import com.puj.ProyectoFinal.modelo.Convenio;
import com.puj.ProyectoFinal.modelo.Mensualidad;
import com.puj.ProyectoFinal.modelo.Tiquete;
import com.puj.ProyectoFinal.modelo.Vehiculo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parqueadero implements IParqueadero, Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] TIPOS = {"CARRO", "MOTO", "ELECTRICO"};

    // Es un atributo (no static) para que se guarde con el sistema.
    private int siguienteNumero = 1;
    private ArrayList<Mensualidad> mensualidades = new ArrayList<>();
    private ArrayList<Tiquete> tiquetes = new ArrayList<>();

    @Override
    public void agregarMensualidad(Mensualidad m) throws ParqueaderoException {
        if (m.getFin().isBefore(m.getInicio())) {
            throw new FechaInvalidaException("La mensualidad de " + m.getPlaca()
                    + " termina el " + m.getFin() + ", antes de su inicio el " + m.getInicio());
        }
        mensualidades.add(m);
    }

    @Override
    public Tiquete registrarEntrada(Vehiculo v, LocalDateTime entrada)
            throws ParqueaderoException {
        if (!v.placaValida()) {
            throw new PlacaInvalidaException("La placa " + v.getPlaca()
                    + " no es valida para un vehiculo " + v.getTipo());
        }
        if (buscarAbierto(v.getPlaca()) != null) {
            throw new VehiculoYaAdentroException("El vehiculo " + v.getPlaca()
                    + " ya esta dentro del parqueadero");
        }
        // El numero solo se consume cuando ya paso las dos validaciones.
        Tiquete tiquete = new Tiquete(siguienteNumero, v, entrada);
        siguienteNumero++;
        tiquetes.add(tiquete);
        return tiquete;
    }

    @Override
    public Tiquete registrarSalida(String placa, LocalDateTime salida, Convenio convenio)
            throws ParqueaderoException {
        Tiquete tiquete = buscarAbierto(placa);
        if (tiquete == null) {
            throw new VehiculoNoEncontradoException(
                    "No hay un tiquete abierto para la placa " + placa);
        }
        if (salida.isBefore(tiquete.getEntrada())) {
            throw new FechaInvalidaException("La salida de " + placa + " ("
                    + salida.format(FORMATO) + ") es anterior a su entrada ("
                    + tiquete.getEntrada().format(FORMATO) + ")");
        }
        boolean mensualidad = tieneMensualidad(placa, tiquete.getEntrada().toLocalDate());
        tiquete.cerrar(salida, convenio, mensualidad);
        return tiquete;
    }

    @Override
    public List<String> reporteVehiculosAdentro() {
        List<Tiquete> abiertos = new ArrayList<>();
        for (Tiquete t : tiquetes) {
            if (t.estaAbierto()) {
                abiertos.add(t);
            }
        }
        abiertos.sort((a, b) -> {
            int comparacion = a.getEntrada().compareTo(b.getEntrada());
            if (comparacion != 0) {
                return comparacion;
            }
            return a.getVehiculo().getPlaca().compareTo(b.getVehiculo().getPlaca());
        });
        List<String> lineas = new ArrayList<>();
        for (Tiquete t : abiertos) {
            lineas.add(t.toString());
        }
        return lineas;
    }

    @Override
    public List<String> reporteCierre(LocalDate fecha) {
        List<Tiquete> delDia = new ArrayList<>();
        for (Tiquete t : tiquetes) {
            if (!t.estaAbierto() && t.getSalida().toLocalDate().equals(fecha)) {
                delDia.add(t);
            }
        }
        long parqueo = 0;
        long recarga = 0;
        for (Tiquete t : delDia) {
            parqueo += t.getValorParqueo();
            recarga += t.getValorRecarga();
        }

        List<String> lineas = new ArrayList<>();
        lineas.add("CIERRE " + fecha);
        lineas.add("tiquetes=" + delDia.size());
        lineas.add("parqueo=" + parqueo);
        lineas.add("recarga=" + recarga);
        lineas.add("total=" + (parqueo + recarga));

        for (String tipo : TIPOS) {
            int cantidad = 0;
            long total = 0;
            for (Tiquete t : delDia) {
                if (t.getVehiculo().getTipo().equals(tipo)) {
                    cantidad++;
                    total += t.getTotal();
                }
            }
            lineas.add(tipo + " tiquetes=" + cantidad + " total=" + total);
        }
        return lineas;
    }

    @Override
    public List<String> reportePlacasFrecuentes() {
        HashMap<String, Integer> visitas = new HashMap<>();
        HashMap<String, Long> pagado = new HashMap<>();
        for (Tiquete t : tiquetes) {
            if (!t.estaAbierto()) {
                String placa = t.getVehiculo().getPlaca();
                visitas.put(placa, visitas.getOrDefault(placa, 0) + 1);
                pagado.put(placa, pagado.getOrDefault(placa, 0L) + t.getTotal());
            }
        }

        List<String> placas = new ArrayList<>(visitas.keySet());
        placas.sort((a, b) -> {
            int comparacion = Integer.compare(visitas.get(b), visitas.get(a));
            if (comparacion != 0) {
                return comparacion;
            }
            comparacion = Long.compare(pagado.get(b), pagado.get(a));
            if (comparacion != 0) {
                return comparacion;
            }
            return a.compareTo(b);
        });

        List<String> lineas = new ArrayList<>();
        int limite = Math.min(3, placas.size());
        for (int i = 0; i < limite; i++) {
            String placa = placas.get(i);
            lineas.add((i + 1) + ". " + placa + " visitas=" + visitas.get(placa)
                    + " pagado=" + pagado.get(placa));
        }
        return lineas;
    }

    @Override
    public List<Tiquete> getTiquetes() {
        return new ArrayList<>(tiquetes);
    }

    private Tiquete buscarAbierto(String placa) {
        for (Tiquete t : tiquetes) {
            if (t.estaAbierto() && t.getVehiculo().getPlaca().equals(placa)) {
                return t;
            }
        }
        return null;
    }

    private boolean tieneMensualidad(String placa, LocalDate fecha) {
        for (Mensualidad m : mensualidades) {
            if (m.getPlaca().equals(placa) && m.cubre(fecha)) {
                return true;
            }
        }
        return false;
    }
}