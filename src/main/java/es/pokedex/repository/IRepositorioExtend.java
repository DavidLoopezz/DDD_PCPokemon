package es.pokedex.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IRepositorioExtend<T, ID> {

    T findById(ID id);

    Optional<T> findByIdOptional(ID id);

    List<T> findAllToList();

    Iterable<T> findAll();

    <S extends T> S save(S entity);

    void deleteById(ID id);

    void deleteAll();

    long count();

    boolean existsById(ID id);
}
