package com.puj.ProyectoFinal.persistencia;

import com.puj.ProyectoFinal.excepciones.FechaInvalidaException;
import com.puj.ProyectoFinal.excepciones.ParqueaderoException;
import com.puj.ProyectoFinal.modelo.Carro;
import com.puj.ProyectoFinal.modelo.CarroElectrico;
import com.puj.ProyectoFinal.modelo.Convenio;
import com.puj.ProyectoFinal.modelo.Mensualidad;
import com.puj.ProyectoFinal.modelo.Moto;
import com.puj.ProyectoFinal.modelo.Vehiculo;
import com.puj.ProyectoFinal.negocio.IParqueadero;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class ManejoArchivos {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private ManejoArchivos() {
    }

    public static List<String> cargarMensualidades(String ruta, IParqueadero sistema)
            throws IOException {
        List<String> lineas = leerLineas(ruta);
        List<String> errores = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (esIgnorable(linea)) {
                continue;
            }
            try {
                procesarMensualidad(linea, sistema);
            } catch (ParqueaderoException | IllegalArgumentException e) {
                errores.add("Linea " + (i + 1) + ": " + e.getMessage());
            }
        }
        return errores;
    }

    public static List<String> procesarMovimientos(String ruta, IParqueadero sistema)
            throws IOException {
        List<String> lineas = leerLineas(ruta);
        List<String> errores = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (esIgnorable(linea)) {
                continue;
            }
            try {
                procesarMovimiento(linea, sistema);
            } catch (ParqueaderoException | IllegalArgumentException e) {
                errores.add("Linea " + (i + 1) + ": " + e.getMessage());
            }
        }
        return errores;
    }

    private static void procesarMensualidad(String linea, IParqueadero sistema)
            throws ParqueaderoException {
        String[] campos = separar(linea);
        if (campos.length != 3) {
            throw new IllegalArgumentException(
                    "Una mensualidad debe tener 3 campos y esta tiene " + campos.length);
        }
        if (campos[0].isEmpty()) {
            throw new IllegalArgumentException("La placa de la mensualidad esta vacia");
        }
        LocalDate inicio = parsearFecha(campos[1]);
        LocalDate fin = parsearFecha(campos[2]);
        sistema.agregarMensualidad(new Mensualidad(campos[0], inicio, fin));
    }

    private static void procesarMovimiento(String linea, IParqueadero sistema)
            throws ParqueaderoException {
        String[] campos = separar(linea);
        String movimiento = campos[0];

        if (movimiento.equals("E")) {
            if (campos.length != 4) {
                throw new IllegalArgumentException(
                        "Una entrada debe tener 4 campos y esta tiene " + campos.length);
            }
            Vehiculo vehiculo = crearVehiculo(campos[1], campos[2]);
            LocalDateTime entrada = parsearFechaHora(campos[3]);
            sistema.registrarEntrada(vehiculo, entrada);
        } else if (movimiento.equals("S")) {
            if (campos.length != 4) {
                throw new IllegalArgumentException(
                        "Una salida debe tener 4 campos y esta tiene " + campos.length);
            }
            LocalDateTime salida = parsearFechaHora(campos[2]);
            Convenio convenio = parsearConvenio(campos[3]);
            sistema.registrarSalida(campos[1], salida, convenio);
        } else {
            throw new IllegalArgumentException(
                    "Movimiento desconocido: " + movimiento + " (solo hay E y S)");
        }
    }
    public static Vehiculo crearVehiculo(String placa, String tipo) {
        switch (tipo) {
            case "CARRO":
                return new Carro(placa);
            case "MOTO":
                return new Moto(placa);
            case "ELECTRICO":
                return new CarroElectrico(placa);
            default:
                throw new IllegalArgumentException("Tipo de vehiculo desconocido: " + tipo);
        }
    }

    public static LocalDate parsearFecha(String texto) throws FechaInvalidaException {
        try {
            return LocalDate.parse(texto, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new FechaInvalidaException(
                    "Fecha mal escrita: '" + texto + "' (se espera aaaa-MM-dd)");
        }
    }

    public static LocalDateTime parsearFechaHora(String texto) throws FechaInvalidaException {
        try {
            return LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
        } catch (DateTimeParseException e) {
            throw new FechaInvalidaException(
                    "Fecha y hora mal escritas: '" + texto + "' (se espera aaaa-MM-dd HH:mm)");
        }
    }
    public static void exportarReporte(List<String> lineas, String ruta) throws IOException {
        Files.write(Paths.get(ruta), lineas, StandardCharsets.UTF_8);
    }

    public static void guardarSistema(IParqueadero sistema, String ruta) throws IOException {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ruta))) {
            salida.writeObject(sistema);
        }
    }

    public static IParqueadero cargarSistema(String ruta) throws IOException {
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ruta))) {
            return (IParqueadero) entrada.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("El archivo " + ruta + " no contiene un sistema valido", e);
        }
    }
    private static Convenio parsearConvenio(String texto) {
        try {
            return Convenio.valueOf(texto);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Convenio desconocido: " + texto);
        }
    }
    private static String[] separar(String linea) {
        String[] campos = linea.split("\\*", -1);
        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].trim();
        }
        return campos;
    }

    private static boolean esIgnorable(String linea) {
        String limpia = linea.trim();
        return limpia.isEmpty() || limpia.startsWith("#");
    }

    private static List<String> leerLineas(String ruta) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }
        }

        if (!lineas.isEmpty() && lineas.get(0).startsWith("\uFEFF")) {
            lineas.set(0, lineas.get(0).substring(1));
        }
        return lineas;
    }
}