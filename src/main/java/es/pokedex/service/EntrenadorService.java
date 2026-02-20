package es.pokedex.service;

import es.pokedex.domain.Entrenador;
import es.pokedex.domain.Pokemon;
import es.pokedex.domain.Region;
import es.pokedex.exception.EntityNotFoundException;
import es.pokedex.repository.IRepositorioExtend;

import java.util.List;
import java.util.stream.Collectors;

public class EntrenadorService {

    private final IRepositorioExtend<Entrenador, String> entrenadorRepo;
    private final IRepositorioExtend<Pokemon, String> pokemonRepo;

    public EntrenadorService(
            IRepositorioExtend<Entrenador, String> entrenadorRepo,
            IRepositorioExtend<Pokemon, String> pokemonRepo
    ) {
        this.entrenadorRepo = entrenadorRepo;
        this.pokemonRepo = pokemonRepo;
    }

    // logica de negocio 2, Máximo 6 Pokémon y logica de negocio 3, el Pokémon debe pertenecer a la generación de su región
    public void assignPokemonToEntrenador(String entrenadorId, String pokedexNumber) {

        Pokemon pokemon = pokemonRepo.findById(pokedexNumber);
        if (pokemon == null) {
            throw new EntityNotFoundException("Pokemon " + pokedexNumber + " no existe");
        }

        Entrenador e = entrenadorRepo.findById(entrenadorId);
        if (e == null) {
            throw new EntityNotFoundException("Entrenador " + entrenadorId + " no existe");
        }

        // logica de negocio 2
        if (e.getPokedexNumbers().size() >= 6) {
            throw new IllegalStateException("Un entrenador no puede tener más de 6 Pokémon");
        }

        // Validación por generación (rango de Pokédex)
        if (!perteneceAGeneracion(e.getRegion(), pokedexNumber)) {
            throw new IllegalStateException(
                    "El Pokémon no pertenece a la región " + e.getRegion()
            );
        }

        e.addPokemon(pokedexNumber);
        entrenadorRepo.save(e);
    }
    //logica de negocio 3
    private boolean perteneceAGeneracion(Region region, String pokedexNumber) {

        int num = Integer.parseInt(pokedexNumber);

        return switch (region) {
            case KANTO  -> num >= 1   && num <= 151;
            case JOHTO  -> num >= 152 && num <= 251;
            case HOENN  -> num >= 252 && num <= 386;
            case SINNOH -> num >= 387 && num <= 493;
            case UNOVA  -> num >= 494 && num <= 649;
            case KALOS  -> num >= 650 && num <= 721;
            case ALOLA  -> num >= 722 && num <= 809;
            case GALAR  -> num >= 810 && num <= 905;
            case PALDEA -> num >= 906 && num <= 1010;
        };
    }

    public List<Pokemon> getPokemonsOfEntrenador(String entrenadorId) {

        Entrenador e = entrenadorRepo.findById(entrenadorId);
        if (e == null) {
            throw new EntityNotFoundException("Entrenador " + entrenadorId + " no existe");
        }

        return e.getPokedexNumbers().stream()
                .map(pokemonRepo::findById)
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }
}
