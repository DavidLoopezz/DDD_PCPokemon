package es.pokedex.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import es.pokedex.domain.Pokemon;
import es.pokedex.domain.TipoPokemon;
import es.pokedex.util.JsonFileStore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PokemonRepository implements IRepositorioExtend<Pokemon, String> {

    private final Path path;                     // Ruta al fichero pokemons.json
    private final JsonFileStore store = new JsonFileStore(); // Utilidad para cargar/guardar JSON

    /**
     * Constructor: define dónde se almacenarán los datos del repositorio.
     */
    public PokemonRepository(String dataDir) {
        this.path = Paths.get(dataDir, "pokemons.json");
    }

    /**
     * Carga la lista completa de Pokemons desde el JSON.
     * synchronized evita condiciones de carrera al leer/escribir.
     */
    private synchronized List<Pokemon> load() {
        List<Pokemon> list = store.readList(path, new TypeReference<>(){});
        return new ArrayList<>(list); // se devuelve una copia
    }

    /**
     * Guarda toda la lista sobrescribiendo el fichero JSON.
     */
    private synchronized void saveAll(List<Pokemon> list) {
        store.writeList(path, list);
    }

    // ---------------- Implementación CRUD ----------------

    @Override
    public long count() { return load().size(); }

    /**
     * Borra un Pokémon por su número de Pokédex.
     */
    @Override
    public synchronized void deleteById(String id) {
        List<Pokemon> all = load();
        boolean removed = all.removeIf(p -> p.getPokedexNumber().equals(id));
        if (removed) saveAll(all);
    }

    /**
     * Borra todos los Pokémon (fichero vacío).
     */
    @Override
    public synchronized void deleteAll() {
        saveAll(new ArrayList<>());
    }

    /**
     * Comprueba si existe un Pokémon por ID.
     */
    @Override
    public boolean existsById(String id) {
        return load().stream().anyMatch(p -> p.getPokedexNumber().equals(id));
    }

    /**
     * Busca un Pokémon por ID. Devuelve null si no existe.
     */
    @Override
    public Pokemon findById(String id) {
        return load().stream()
                .filter(p -> p.getPokedexNumber().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Inserta o actualiza un Pokémon.
     * Si ya existe → actualiza nombre, tipo y movimientos.
     * Si no existe → lo añade nuevo.
     */
    @Override
    public synchronized <S extends Pokemon> S save(S entity) {
        List<Pokemon> all = load();

        Optional<Pokemon> existing =
                all.stream().filter(p -> p.getPokedexNumber().equals(entity.getPokedexNumber())).findFirst();

        if (existing.isPresent()) {
            // Actualización del Pokémon existente
            Pokemon p = existing.get();
            p.setNombre(entity.getNombre());
            p.setTipo(entity.getTipo());

            p.getMovimientoIds().clear();
            p.getMovimientoIds().addAll(entity.getMovimientoIds());

        } else {
            // Inserción
            all.add(entity);
        }

        saveAll(all);
        return entity;
    }

    @Override
    public Iterable<Pokemon> findAll() { return load(); }

    @Override
    public Optional<Pokemon> findByIdOptional(String id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<Pokemon> findAllToList() { return load(); }

    // ---------------- Métodos semánticos del repositorio ----------------

    /**
     * Devuelve los Pokémon que sean del tipo indicado.
     */
    public List<Pokemon> findByTipo(TipoPokemon tipo) {
        return load().stream()
                .filter(p -> p.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve un mapa Tipo → nº de Pokémon de ese tipo.
     */
    public Map<TipoPokemon, Long> countByTipo() {
        return load().stream()
                .collect(Collectors.groupingBy(Pokemon::getTipo, Collectors.counting()));
    }

    /**
     * Devuelve todos los Pokémon que tengan un movimiento concreto.
     */
    public List<Pokemon> findByMovimientoId(String movimientoId) {
        return load().stream()
                .filter(p -> p.getMovimientoIds().contains(movimientoId))
                .collect(Collectors.toList());
    }
}
