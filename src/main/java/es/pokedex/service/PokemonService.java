package es.pokedex.service;

import es.pokedex.domain.Entrenador;
import es.pokedex.domain.Movimiento;
import es.pokedex.domain.Pokemon;
import es.pokedex.exception.EntityNotFoundException;
import es.pokedex.repository.IRepositorioExtend;

import java.util.List;

public class PokemonService {

    private final IRepositorioExtend<Pokemon, String> pokemonRepo;
    private final IRepositorioExtend<Entrenador, String> entrenadorRepo;
    private final IRepositorioExtend<Movimiento, String> movimientoRepo;

    public PokemonService(
            IRepositorioExtend<Pokemon, String> pokemonRepo,
            IRepositorioExtend<Entrenador, String> entrenadorRepo,
            IRepositorioExtend<Movimiento, String> movimientoRepo
    ) {
        this.pokemonRepo = pokemonRepo;
        this.entrenadorRepo = entrenadorRepo;
        this.movimientoRepo = movimientoRepo;
    }

    public int deletePokemon(String pokedexNumber) {
        if (!pokemonRepo.existsById(pokedexNumber)) {
            throw new EntityNotFoundException("Pokemon no existe");
        }

        pokemonRepo.deleteById(pokedexNumber);

        int cleaned = 0;
        for (Entrenador e : entrenadorRepo.findAllToList()) {
            if (e.getPokedexNumbers().remove(pokedexNumber)) {
                entrenadorRepo.save(e);
                cleaned++;
            }
        }
        return cleaned;
    }

    // Logica de negocios 1: el movimiento debe coincidir con el tipo del pokemon
    public boolean addMovimientoToPokemon(String pokedexNumber, String movimientoId) {

        Pokemon p = pokemonRepo.findById(pokedexNumber);
        if (p == null) throw new EntityNotFoundException("Pokemon no existe");

        Movimiento m = movimientoRepo.findById(movimientoId);
        if (m == null) throw new EntityNotFoundException("Movimiento no existe");

        // Validación de tipo
        if (!p.getTipo().equals(m.getTipo())) {
            throw new IllegalStateException(
                    "Un Pokemon tipo " + p.getTipo() +
                            " no puede usar un movimiento tipo " + m.getTipo()
            );
        }

        if (p.getMovimientoIds().contains(movimientoId)) return false;

        p.addMovimiento(movimientoId);
        pokemonRepo.save(p);
        return true;
    }

    public boolean removeMovimientoFromPokemon(String pokedexNumber, String movimientoId) {
        Pokemon p = pokemonRepo.findById(pokedexNumber);
        if (p == null) throw new EntityNotFoundException("Pokemon no existe");

        boolean removed = p.getMovimientoIds().remove(movimientoId);
        pokemonRepo.save(p);
        return removed;
    }

    public int deleteMovimiento(String movimientoId) {
        if (!movimientoRepo.existsById(movimientoId)) {
            throw new EntityNotFoundException("Movimiento no existe");
        }

        int cleaned = 0;
        for (Pokemon p : pokemonRepo.findAllToList()) {
            if (p.getMovimientoIds().remove(movimientoId)) {
                pokemonRepo.save(p);
                cleaned++;
            }
        }

        movimientoRepo.deleteById(movimientoId);
        return cleaned;
    }
}
