package es.pokedex.app;

import es.pokedex.domain.Pokemon;
import es.pokedex.domain.TipoPokemon;
import es.pokedex.repository.MovimientoRepository;
import es.pokedex.repository.PokemonRepository;
import es.pokedex.repository.EntrenadorRepository;
import es.pokedex.service.PokemonService;

import java.util.Scanner;

public class MainPokemon {

    private static final String DATA_DIR = "data";

    // Main de pruebas para el repositorio de Pokemons.
    public static void main(String[] args) {
        // Repositorios usados: pokemons, movimientos y entrenadores.
        PokemonRepository repo = new PokemonRepository(DATA_DIR);
        MovimientoRepository movRepo = new MovimientoRepository(DATA_DIR);
        EntrenadorRepository entRepo = new EntrenadorRepository(DATA_DIR);
        PokemonService service = new PokemonService(repo, entRepo, movRepo);

        Scanner sc = new Scanner(System.in);

        // Bucle principal del menú de prueba.
        while (true) {
            System.out.println("\n--- TEST REPO: POKEMON ---");
            System.out.println("1) Listar Pokemons");
            System.out.println("2) Crear Pokemon");
            System.out.println("3) Buscar por ID");
            System.out.println("4) Borrar Pokemon (usa PokemonService)");
            System.out.println("5) Buscar por tipo");
            System.out.println("6) Asociar movimiento a pokemon");
            System.out.println("7) Quitar movimiento de pokemon");
            System.out.println("0) Salir");
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();

            switch (op) {

                // Lista todos los Pokemons y muestra id, nombre y tipo.
                case "1" -> repo.findAllToList().forEach(p ->
                        System.out.println(p.getPokedexNumber() + " - " + p.getNombre() + " - " + p.getTipo())
                );

                // Crea un Pokemon, lee PokedexNumber, nombre y tipo (elige del enum) y lo guarda.
                case "2" -> {
                    try {
                        System.out.print("Pokedex Number (3 dígitos): ");
                        String id = sc.nextLine().trim();
                        System.out.print("Nombre: ");
                        String nombre = sc.nextLine().trim();
                        System.out.println("Tipo:");
                        for (int i = 0; i < TipoPokemon.values().length; i++)
                            System.out.println((i + 1) + ") " + TipoPokemon.values()[i]);
                        System.out.print("Elige tipo: ");
                        int t = Integer.parseInt(sc.nextLine()) - 1;

                        Pokemon p = new Pokemon(id, nombre, TipoPokemon.values()[t]);
                        repo.save(p);

                        System.out.println("Guardado correctamente.");
                    } catch (Exception e) {
                        // Muestra error si el formato o guardado fallas.
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                // Busca un Pokemon por su PokedexNumber y lo muestra si existe.
                case "3" -> {
                    System.out.print("ID: ");
                    String id = sc.nextLine().trim();
                    Pokemon p = repo.findById(id);
                    System.out.println(p == null ? "No existe" :
                            p.getPokedexNumber() + " - " + p.getNombre() + " - " + p.getTipo());
                }

                // Borra un Pokemon usando PokemonService para que limpie referencias en entrenadores.
                case "4" -> {
                    System.out.print("ID: ");
                    String id = sc.nextLine().trim();
                    try {
                        int cleaned = service.deletePokemon(id);
                        System.out.println("Eliminado. Referencias limpiadas en " + cleaned + " entrenadores.");
                    } catch (Exception e) {
                        // Maneja errores
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                // Lista Pokemons filtrando por tipo (selección del enum).
                case "5" -> {
                    System.out.println("Tipo:");
                    for (int i = 0; i < TipoPokemon.values().length; i++)
                        System.out.println((i + 1) + ") " + TipoPokemon.values()[i]);
                    System.out.print("Elige: ");
                    int t = Integer.parseInt(sc.nextLine()) - 1;
                    repo.findByTipo(TipoPokemon.values()[t])
                            .forEach(p -> System.out.println(p.getPokedexNumber() + " - " + p.getNombre()));
                }

                // Asocia un movimiento existente a un Pokemon (valida existencia de ambos).
                case "6" -> {
                    System.out.print("PokedexNumber: ");
                    String pid = sc.nextLine().trim();
                    System.out.print("Movimiento ID (2 letras + 4 dígitos): ");
                    String mid = sc.nextLine().trim().toUpperCase();
                    try {
                        boolean added = service.addMovimientoToPokemon(pid, mid);
                        if (added) System.out.println("Movimiento añadido al pokemon.");
                        else System.out.println("El pokemon ya tenía ese movimiento.");
                    } catch (Exception e) {
                        // Puede lanzar IllegalArgumentException o EntityNotFoundException.
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                // Quita la asociación movimiento <---> pokemon y persiste el cambio.
                case "7" -> {
                    System.out.print("PokedexNumber: ");
                    String pid = sc.nextLine().trim();
                    System.out.print("Movimiento ID: ");
                    String mid = sc.nextLine().trim().toUpperCase();
                    try {
                        boolean removed = service.removeMovimientoFromPokemon(pid, mid);
                        if (removed) System.out.println("Movimiento quitado del pokemon.");
                        else System.out.println("El pokemon no tenía ese movimiento.");
                    } catch (Exception e) {
                        // Maneja errores
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                // Sale del programa de pruebas.
                case "0" -> {
                    System.out.println("Adiós.");
                    return;
                }

                // Opción inválida.
                default -> System.out.println("Opción inválida.");
            }
        }
    }
}
