package com.bazaar.catalogo;

public class Catalogo {

    //Definición del Record
    record Punto(int x, int y) {}

    public static void main(String[] args) {
        Object obj = new Punto(3, 4);

        // Uso de Pattern Matching para switch y Record Patterns (Java 21)
        String resultado = switch (obj) {
            case Punto(int x, int y) when x == y -> "Punto diagonal";
            case Punto(int x, int y)             -> "Punto en (" + x + ", " + y + ")";
            default                              -> "Desconocido";
        };

        // Impresión de resultados
        System.out.println(resultado);
        System.out.println("JDK version: " + System.getProperty("java.version"));
    }
}
