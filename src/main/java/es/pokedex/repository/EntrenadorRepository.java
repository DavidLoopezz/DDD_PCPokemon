package es.pokedex.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import es.pokedex.domain.Entrenador;
import es.pokedex.domain.Region;
import es.pokedex.util.JsonFileStore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class EntrenadorRepository implements IRepositorioExtend<Entrenador, String> {

    private final Path path;                 // Ruta del fichero entrenadores.json
    private final JsonFileStore store = new JsonFileStore();  // Utilidad para leer/escribir JSON

    /**
     * Constructor: recibe el directorio y fija la ruta al json de entrenadores.
     */
    public EntrenadorRepository(String dataDir) {
        this.path = Paths.get(dataDir, "entrenadores.json");
    }

    /**
     * Lee el fichero JSON y devuelve una lista nueva (copia para evitar modificar la interna).
     * synchronized evita conflictos si varios hilos acceden a la vez.
     */
    private synchronized List<Entrenador> load() {
        List<Entrenador> list = store.readList(path, new TypeReference<>(){});
        return new ArrayList<>(list);
    }

    /**
     * Guarda toda la lista sobrescribiendo el fichero.
     */
    private synchronized void saveAll(List<Entrenador> list) {
        store.writeList(path, list);
    }

    // ---------------- Implementación CRUD básica ----------------

    @Override
    public long count() { return load().size(); }

    /**
     * Borra un entrenador por ID. Si no existe no hace nada.
     */
    @Override
    public synchronized void deleteById(String id) {
        List<Entrenador> all = load();
        boolean removed = all.removeIf(e -> e.getId().equals(id));
        if (removed) saveAll(all);
    }

    /**
     * Elimina todos los entrenadores (deja fichero vacío).
     */
    @Override
    public synchronized void deleteAll() { saveAll(new ArrayList<>()); }

    /**
     * Comprueba si existe un entrenador con ese ID.
     */
    @Override
    public boolean existsById(String id) {
        return load().stream().anyMatch(e -> e.getId().equals(id));
    }

    /**
     * Devuelve el entrenador con ese ID o null si no existe.
     */
    @Override
    public Entrenador findById(String id) {
        return load().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Guarda o actualiza un entrenador.
     * Si existe lo actualiza; si no existe lo inserta.
     */
    @Override
    public synchronized <S extends Entrenador> S save(S entity) {
        List<Entrenador> all = load();

        Optional<Entrenador> existing = all.stream()
                .filter(e -> e.getId().equals(entity.getId()))
                .findFirst();

        if (existing.isPresent()) {
            // Actualiza campos del entrenador existente
            Entrenador ex = existing.get();
            ex.setNombre(entity.getNombre());
            ex.setRegion(entity.getRegion());

            ex.getPokedexNumbers().clear();
            ex.getPokedexNumbers().addAll(entity.getPokedexNumbers());
        } else {
            // Inserta nuevo entrenador
            all.add(entity);
        }

        saveAll(all);
        return entity;
    }



    @Override
    public Iterable<Entrenador> findAll() { return load(); }

    @Override
    public Optional<Entrenador> findByIdOptional(String id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<Entrenador> findAllToList() { return load(); }

    // ---------------- Métodos semánticos adicionales del repositorio ----------------

    /**
     * Devuelve entrenadores filtrados por región.
     */
    public List<Entrenador> findByRegion(Region region) {
        return load().stream()
                .filter(e -> e.getRegion() == region)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve entrenadores cuyo nombre empieza por un prefijo.
     */
    public List<Entrenador> findByNombrePrefix(String prefix) {
        String p = prefix.toLowerCase();
        return load().stream()
                .filter(e -> e.getNombre().toLowerCase().startsWith(p))
                .collect(Collectors.toList());
    }

    // ---------------- Métodos usados al borrar otras entidades ----------------

    /**
     * Elimina referencias a un Pokémon en TODOS los entrenadores.
     * Devuelve cuántos entrenadores fueron modificados.
     */
    public synchronized int removePokemonReferences(String pokedexNumber) {
        List<Entrenador> all = load();
        int count = 0;

        for (Entrenador e : all) {
            if (e.getPokedexNumbers().removeIf(id -> id.equals(pokedexNumber))) {
                count++;
            }
        }

        saveAll(all);
        return count;
    }

    /**
     * Eliminación de referencias a movimientos — aquí no aplica porque los entrenadores no tienen movimientos.
     * Se dejó por simetría pero realmente no se utiliza.
     */
    public synchronized int removeMovimientoReferences(String movimientoId) {
        List<Entrenador> all = load();
        int count = 0;

        for (Entrenador e : all) {
            if (e.getPokedexNumbers().removeIf(id -> id.equals(movimientoId))) {
                count++;
            }
        }

        saveAll(all);
        return count;
    }
}
