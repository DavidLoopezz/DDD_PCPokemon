package es.pokedex.repository;

public interface IRepositorio<T, ID> {
    long count();
    void deleteById(ID id);
    void deleteAll();
    boolean existsById(ID id);
    T findById(ID id);
    Iterable<T> findAll();
    <S extends T> S save(S entity);
}

