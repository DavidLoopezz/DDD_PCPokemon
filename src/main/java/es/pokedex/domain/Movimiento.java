package es.pokedex.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Movimiento {
    private final String id;      // Identificador único: 2 letras + 4 dígitos (ej. TH1023)
    private String nombre;        // Nombre del movimiento
    private TipoPokemon tipo;     // Tipo elemental del movimiento
    private int potencia;         // Potencia base del ataque

    /**
     * Constructor usado por Jackson y por la aplicación.
     * Valida formato del ID, nombre, tipo y potencia antes de crear el objeto.
     */
    @JsonCreator
    public Movimiento(@JsonProperty("id") String id,
                      @JsonProperty("nombre") String nombre,
                      @JsonProperty("tipo") TipoPokemon tipo,
                      @JsonProperty("potencia") int potencia) {

        // Valida ID del movimiento (formato AA0000)
        if (id == null || !id.matches("[A-Z]{2}\\d{4}"))
            throw new IllegalArgumentException("id movimiento debe ser 2 MAYUSC + 4 DIGIT, ej: TH1023");

        // Valida nombre no vacío
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("nombre requerido");

        // Valida tipo
        if (tipo == null)
            throw new IllegalArgumentException("tipo requerido");

        // Potencia debe ser >= 0
        if (potencia < 0)
            throw new IllegalArgumentException("potencia >= 0");

        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.potencia = potencia;
    }

    // Getters y setters básicos
    public String getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoPokemon getTipo() { return tipo; }
    public void setTipo(TipoPokemon tipo) { this.tipo = tipo; }

    public int getPotencia() { return potencia; }
    public void setPotencia(int potencia) { this.potencia = potencia; }

    // Igualdad basada únicamente en ID del movimiento
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movimiento)) return false;
        Movimiento m = (Movimiento) o;
        return id.equals(m.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
