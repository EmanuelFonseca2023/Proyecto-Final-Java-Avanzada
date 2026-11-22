package com.puj.ProyectoFinal.modelo;

public class Carro extends Vehiculo {

    private static final long serialVersionUID = 1L;

    private static final long TARIFA_MINUTO = 120;
    private static final String PATRON_PLACA = "[A-Z]{3}[0-9]{3}";

    public Carro(String placa) {
        super(placa);
    }

    @Override
    public String getTipo() {
        return "CARRO";
    }

    @Override
    public boolean placaValida() {
        return getPlaca() != null && getPlaca().matches(PATRON_PLACA);
    }

    @Override
    public long calcularTarifa(long minutos) {
        return minutos * TARIFA_MINUTO;
    }
}
