package model;

import java.util.Objects;

public class Ville {
    private String nom;
    private double x;
    private double y;

    // Constructeur vide nécessaire pour Gson
    public Ville() {}

    public Ville(String nom, double x, double y) {
        this.nom = nom;
        this.x = x;
        this.y = y;
    }

    // Getters (inchangés)
    public String getNom() { return nom; }
    public double getX() { return x; }
    public double getY() { return y; }

    public double distanceTo(Ville autre) {
        if (autre == null) return Double.POSITIVE_INFINITY;
        double dx = this.x - autre.x;
        double dy = this.y - autre.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return nom + " (" + String.format("%.2f", x) + ", " + String.format("%.2f", y) + ")";
    }

    // Important pour les comparaisons et les Map/Set
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ville ville = (Ville) o;
        return Objects.equals(nom, ville.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom);
    }
}