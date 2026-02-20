package es.pokedex.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import es.pokedex.domain.Movimiento;
import es.pokedex.domain.TipoPokemon;
import es.pokedex.util.JsonFileStore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MovimientoRepository implements IRepositorioExtend<Movimiento, String> {

    private final Path path;                  // Ruta del fichero movimientos.json
    private final JsonFileStore store = new JsonFileStore();   // Utilidad para leer/escribir JSON

    /**
     * Constructor: fija el path al archivo de movimientos dentro del directorio de datos.
     */
    public MovimientoRepository(String dataDir) {
        this.path = Paths.get(dataDir, "movimientos.json");
    }

    /**
     * Carga el listado de movimientos desde el JSON.
     * synchronized -> asegura que no haya lecturas/escrituras simultáneas.
     */
    private synchronized List<Movimiento> load() {
        List<Movimiento> list = store.readList(path, new TypeReference<>(){});
        return new ArrayList<>(list);  // copia defensiva
    }

    /**
     * Sobrescribe todo el fichero con la lista recibida.
     */
    private synchronized void saveAll(List<Movimiento> list) {
        store.writeList(path, list);
    }

    // ---------------- Implementación CRUD ----------------

    @Override
    public long count() { return load().size(); }

    /**
     * Borra un movimiento por ID si existe.
     */
    @Override
    public synchronized void deleteById(String id) {
        List<Movimiento> all = load();
        boolean removed = all.removeIf(m -> m.getId().equals(id));
        if (removed) saveAll(all);
    }

    /**
     * Borra todos los movimientos.
     */
    @Override
    public synchronized void deleteAll() {
        saveAll(new ArrayList<>());
    }

    public Map<TipoPokemon, Long> countByTipo() {
        return Map.of(); // o tu implementación SQL
    }


    /**
     * Comprueba si un movimiento existe por ID.
     */
    @Override
    public boolean existsById(String id) {
        return load().stream().anyMatch(m -> m.getId().equals(id));
    }

    /**
     * Devuelve un movimiento por ID o null si no existe.
     */
    @Override
    public Movimiento findById(String id) {
        return load().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Guarda o actualiza un movimiento.
     * Si existe -> actualiza nombre, tipo y potencia.
     * Si no existe -> inserta uno nuevo.
     */
    @Override
    public synchronized <S extends Movimiento> S save(S entity) {
        List<Movimiento> all = load();

        Optional<Movimiento> existing =
                all.stream().filter(m -> m.getId().equals(entity.getId())).findFirst();

        if (existing.isPresent()) {
            // Actualización
            Movimiento m = existing.get();
            m.setNombre(entity.getNombre());
            m.setTipo(entity.getTipo());
            m.setPotencia(entity.getPotencia());
        } else {
            // Inserción
            all.add(entity);
        }

        saveAll(all);
        return entity;
    }

    @Override
    public Iterable<Movimiento> findAll() { return load(); }

    @Override
    public Optional<Movimiento> findByIdOptional(String id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<Movimiento> findAllToList() { return load(); }

    // ---------------- Métodos semánticos extras ----------------

    /**
     * Devuelve movimientos filtrados por tipo Pokémon.
     */
    public List<Movimiento> findByTipo(TipoPokemon tipo) {
        return load().stream()
                .filter(m -> m.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve movimientos cuyo nombre empieza por un prefijo dado.
     */
    public List<Movimiento> findByNombrePrefix(String prefix) {
        String p = prefix.toLowerCase();
        return load().stream()
                .filter(m -> m.getNombre().toLowerCase().startsWith(p))
                .collect(Collectors.toList());
    }
}
