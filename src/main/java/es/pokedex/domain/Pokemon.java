package es.pokedex.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pokemon {
    private final String pokedexNumber;          // ID único del Pokémon (3 dígitos, ej: "025")
    private String nombre;                       // Nombre del Pokémon
    private TipoPokemon tipo;                    // Tipo elemental (enum)
    private List<String> movimientoIds = new ArrayList<>(); // IDs de movimientos asociados

    /**
     * Constructor principal usado por Jackson.
     * Valida formato del ID, nombre obligatorio, tipo obligatorio y carga movimientos si vienen en JSON.
     */
    @JsonCreator
    public Pokemon(@JsonProperty("pokedexNumber") String pokedexNumber,
                   @JsonProperty("nombre") String nombre,
                   @JsonProperty("tipo") TipoPokemon tipo,
                   @JsonProperty("movimientoIds") List<String> movimientoIds) {

        // Valida que el ID tenga exactamente 3 dígitos
        if (pokedexNumber == null || !pokedexNumber.matches("\\d{3}"))
            throw new IllegalArgumentException("pokedexNumber debe tener 3 dig, ej: 025");

        // Valida nombre no vacío
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("nombre requerido");

        // Valida tipo no nulo
        if (tipo == null)
            throw new IllegalArgumentException("tipo requerido");

        this.pokedexNumber = pokedexNumber;
        this.nombre = nombre;
        this.tipo = tipo;

        // Si desde JSON viene una lista, la usamos; si no, dejamos la lista vacía
        if (movimientoIds != null) this.movimientoIds = movimientoIds;
    }

    /**
     * Constructor simplificado usado al crear nuevos Pokémon desde la app.
     * Inicia la lista de movimientos vacía.
     */
    public Pokemon(String pokedexNumber, String nombre, TipoPokemon tipo) {
        this(pokedexNumber, nombre, tipo, new ArrayList<>());
    }

    // Getters y setters con validación
    public String getPokedexNumber() { return pokedexNumber; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("nombre requerido");
        this.nombre = nombre;
    }

    public TipoPokemon getTipo() { return tipo; }
    public void setTipo(TipoPokemon tipo) {
        if (tipo == null)
            throw new IllegalArgumentException("tipo requerido");
        this.tipo = tipo;
    }

    public List<String> getMovimientoIds() { return movimientoIds; }

    // Asocia un movimiento si no estaba ya
    public void addMovimiento(String movId) {
        if (!movimientoIds.contains(movId)) movimientoIds.add(movId);
    }

    // Elimina un movimiento si estaba asociado
    public void removeMovimiento(String movId) {
        movimientoIds.remove(movId);
    }

    // Igualdad basada únicamente en el número de Pokédex
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pokemon)) return false;
        Pokemon p = (Pokemon) o;
        return pokedexNumber.equals(p.pokedexNumber);
    }

    @Override
    public int hashCode() { return Objects.hash(pokedexNumber); }
}
