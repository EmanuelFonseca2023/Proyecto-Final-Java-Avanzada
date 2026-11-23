package com.puj.ProyectoFinal.presentacion;

import com.puj.ProyectoFinal.excepciones.ParqueaderoException;
import com.puj.ProyectoFinal.modelo.Convenio;
import com.puj.ProyectoFinal.modelo.Tiquete;
import com.puj.ProyectoFinal.modelo.Vehiculo;
import com.puj.ProyectoFinal.negocio.IParqueadero;
import com.puj.ProyectoFinal.negocio.Parqueadero;
import com.puj.ProyectoFinal.persistencia.ManejoArchivos;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Consola {

    private static final String ARCHIVO_SISTEMA = "sistema.dat";

    private static IParqueadero sistema;
    private static Scanner lector;

    public static void main(String[] args) {
        sistema = new Parqueadero();
        lector = new Scanner(System.in);

        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            opcion = leerOpcion();
            ejecutarOpcion(opcion);
        }
        lector.close();
    }


    private static void mostrarMenu() {
        System.out.println();
        System.out.println("=== PARQUEADERO PLAZA CENTRAL ===");
        System.out.println("1. Cargar mensualidades");
        System.out.println("2. Procesar movimientos");
        System.out.println("3. Registrar una entrada");
        System.out.println("4. Registrar una salida");
        System.out.println("5. Ver los vehiculos adentro");
        System.out.println("6. Cierre de caja de un dia");
        System.out.println("7. Placas frecuentes");
        System.out.println("8. Guardar el sistema");
        System.out.println("9. Cargar el sistema");
        System.out.println("0. Salir");
    }

    private static int leerOpcion() {
        String texto = leerTexto("Opcion: ");
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1:
                opcionCargarMensualidades();
                break;
            case 2:
                opcionProcesarMovimientos();
                break;
            case 3:
                opcionRegistrarEntrada();
                break;
            case 4:
                opcionRegistrarSalida();
                break;
            case 5:
                opcionVehiculosAdentro();
                break;
            case 6:
                opcionCierreDeCaja();
                break;
            case 7:
                opcionPlacasFrecuentes();
                break;
            case 8:
                opcionGuardarSistema();
                break;
            case 9:
                opcionCargarSistema();
                break;
            case 0:
                System.out.println("Hasta luego.");
                break;
            default:
                System.out.println("Opcion no valida.");
        }
    }

    private static void opcionCargarMensualidades() {
        try {
            String carpeta = leerTexto("Carpeta de los archivos: ");
            String ruta = Paths.get(carpeta, "Mensualidades.txt").toString();
            List<String> errores = ManejoArchivos.cargarMensualidades(ruta, sistema);
            System.out.println("Carga terminada. Lineas rechazadas: " + errores.size());
            mostrarLineas(errores);
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    private static void opcionProcesarMovimientos() {
        try {
            String carpeta = leerTexto("Carpeta de los archivos: ");
            String ruta = Paths.get(carpeta, "Movimientos.txt").toString();
            List<String> errores = ManejoArchivos.procesarMovimientos(ruta, sistema);
            System.out.println("Proceso terminado. Lineas rechazadas: " + errores.size());
            mostrarLineas(errores);
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    private static void opcionRegistrarEntrada() {
        try {
            String placa = leerTexto("Placa: ");
            String tipo = leerTexto("Tipo (CARRO, MOTO o ELECTRICO): ").toUpperCase();
            LocalDateTime entrada = ManejoArchivos.parsearFechaHora(
                    leerTexto("Fecha y hora (aaaa-MM-dd HH:mm): "));

            Vehiculo vehiculo = ManejoArchivos.crearVehiculo(placa, tipo);
            Tiquete tiquete = sistema.registrarEntrada(vehiculo, entrada);
            System.out.println("Tiquete generado: " + tiquete);
        } catch (ParqueaderoException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void opcionRegistrarSalida() {
        try {
            String placa = leerTexto("Placa: ");
            LocalDateTime salida = ManejoArchivos.parsearFechaHora(
                    leerTexto("Fecha y hora (aaaa-MM-dd HH:mm): "));
            Convenio convenio = leerConvenio();

            Tiquete tiquete = sistema.registrarSalida(placa, salida, convenio);
            System.out.println("Tiquete cerrado: " + tiquete);
        } catch (ParqueaderoException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void opcionVehiculosAdentro() {
        List<String> lineas = sistema.reporteVehiculosAdentro();
        if (lineas.isEmpty()) {
            System.out.println("No hay vehiculos adentro.");
        } else {
            System.out.println("Vehiculos adentro");
            mostrarLineas(lineas);
        }
    }

    private static void opcionCierreDeCaja() {
        try {
            LocalDate fecha = ManejoArchivos.parsearFecha(leerTexto("Fecha (aaaa-MM-dd): "));
            List<String> lineas = sistema.reporteCierre(fecha);
            mostrarLineas(lineas);

            String ruta = "Cierre_" + fecha + ".txt";
            ManejoArchivos.exportarReporte(lineas, ruta);
            System.out.println("Cierre exportado en " + ruta);
        } catch (ParqueaderoException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("No se pudo exportar el cierre: " + e.getMessage());
        }
    }

    private static void opcionPlacasFrecuentes() {
        List<String> lineas = sistema.reportePlacasFrecuentes();
        if (lineas.isEmpty()) {
            System.out.println("Todavia no hay tiquetes cerrados.");
        } else {
            System.out.println("Placas frecuentes");
            mostrarLineas(lineas);
        }
    }

    private static void opcionGuardarSistema() {
        try {
            ManejoArchivos.guardarSistema(sistema, ARCHIVO_SISTEMA);
            System.out.println("Sistema guardado en " + ARCHIVO_SISTEMA);
        } catch (IOException e) {
            System.out.println("No se pudo guardar el sistema: " + e.getMessage());
        }
    }

    private static void opcionCargarSistema() {
        try {
            sistema = ManejoArchivos.cargarSistema(ARCHIVO_SISTEMA);
            System.out.println("Sistema recuperado de " + ARCHIVO_SISTEMA);
        } catch (IOException e) {
            System.out.println("No se pudo cargar el sistema: " + e.getMessage());
        }
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return lector.nextLine().trim();
    }

    private static Convenio leerConvenio() {
        String texto = leerTexto("Convenio (NINGUNO, ALMACEN, RESTAURANTE o CINE): ");
        try {
            return Convenio.valueOf(texto.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Convenio desconocido: " + texto);
        }
    }

    private static void mostrarLineas(List<String> lineas) {
        for (String linea : lineas) {
            System.out.println(linea);
        }
    }
}