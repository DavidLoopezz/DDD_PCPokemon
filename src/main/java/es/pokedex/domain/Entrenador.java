package es.pokedex.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.pokedex.exception.InvalidFormatException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Entrenador {
    private final String id;                  // DNI único del entrenador (8 dígitos + letra)
    private String nombre;                    // Nombre del entrenador
    private Region region;                    // Región a la que pertenece
    private List<String> pokedexNumbers = new ArrayList<>();  // Lista de IDs de Pokémon asociados

    /**
     * Constructor usado por Jackson y por la app.
     * Valida formato del DNI, que el nombre no esté vacío y que la región sea válida.
     */
    @JsonCreator
    public Entrenador(@JsonProperty("id") String id,
                      @JsonProperty("nombre") String nombre,
                      @JsonProperty("region") Region region,
                      @JsonProperty("pokedexNumbers") List<String> pokedexNumbers) {

        // Valida el formato de DNI (8 dígitos + una letra mayúscula)
        if (id == null || !id.matches("\\d{8}[A-Z]"))
            throw new InvalidFormatException("DNI tiene que tener 8 digitos + una mayúscula, eje: 65788344F");

        // Valida nombre no nulo/vacío
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("nombre requerido");

        // Valida región no nula
        if (region == null)
            throw new IllegalArgumentException("requiere region");

        this.id = id;
        this.nombre = nombre;
        this.region = region;

        // Si viene desde JSON, mantiene la lista; sino deja una lista vacía
        if (pokedexNumbers != null) this.pokedexNumbers = pokedexNumbers;
    }

    // Getters y setters con validación simple
    public String getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("nombre requerido");
        this.nombre = nombre;
    }

    public Region getRegion() { return region; }
    public void setRegion(Region region) {
        if (region == null)
            throw new IllegalArgumentException("region requerido");
        this.region = region;
    }

    public List<String> getPokedexNumbers() { return pokedexNumbers; }

    // Añade un Pokémon por ID si no estaba ya asociado
    public void addPokemon(String pokedexNumber) {
        if (!pokedexNumbers.contains(pokedexNumber)) pokedexNumbers.add(pokedexNumber);
    }

    // Elimina un Pokémon por ID si estaba asociado
    public void removePokemon(String pokedexNumber) {
        pokedexNumbers.remove(pokedexNumber);
    }

    // Dos entrenadores son iguales si tienen el mismo DNI
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrenador)) return false;
        Entrenador e = (Entrenador) o;
        return id.equals(e.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
