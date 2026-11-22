package com.puj.ProyectoFinal.modelo;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tiquete implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int numero;
    private Vehiculo vehiculo;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private Convenio convenio;
    private long valorParqueo;
    private long valorRecarga;

    public Tiquete(int numero, Vehiculo vehiculo, LocalDateTime entrada) {
        this.numero = numero;
        this.vehiculo = vehiculo;
        this.entrada = entrada;
    }

    public int getNumero() {
        return numero;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public LocalDateTime getSalida() {
        return salida;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public long getValorParqueo() {
        return valorParqueo;
    }

    public long getValorRecarga() {
        return valorRecarga;
    }

    public boolean estaAbierto() {
        return salida == null;
    }

    public long getMinutos() {
        if (salida == null) {
            return 0;
        }
        return Duration.between(entrada, salida).toMinutes();
    }

    public void cerrar(LocalDateTime salida, Convenio convenio, boolean mensualidad) {
        this.salida = salida;
        this.convenio = convenio;
        long minutos = getMinutos();
        long cobrables = Math.max(0, minutos - convenio.getMinutosGratis());
        this.valorParqueo = mensualidad ? 0 : vehiculo.calcularTarifa(cobrables);
        this.valorRecarga = vehiculo.cargoAdicional(minutos);
    }

    public long getTotal() {
        return valorParqueo + valorRecarga;
    }

    @Override
    public String toString() {
        String texto = numero + " " + vehiculo.getPlaca() + " " + vehiculo.getTipo()
                + " " + entrada.format(FORMATO);
        if (estaAbierto()) {
            return texto;
        }
        return texto + " -> " + salida.format(FORMATO)
                + " | minutos=" + getMinutos()
                + " convenio=" + convenio
                + " parqueo=" + valorParqueo
                + " recarga=" + valorRecarga
                + " total=" + getTotal();
    }
}
