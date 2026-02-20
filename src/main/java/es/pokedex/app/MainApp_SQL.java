package es.pokedex.app;

import es.pokedex.domain.*;
import es.pokedex.exception.EntityNotFoundException;
import es.pokedex.repository.*;
import es.pokedex.service.EntrenadorService;
import es.pokedex.service.PokemonService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

// todo entrenador case 4 no enseña pokemon

// Main principal usando MySQL.
public class MainApp_SQL {

    private final PokemonRepositoryMySQL pokemonRepo = new PokemonRepositoryMySQL();
    private final MovimientoRepositoryMySQL movRepo = new MovimientoRepositoryMySQL();
    private final EntrenadorRepositoryMySQL entRepo = new EntrenadorRepositoryMySQL();

    private final EntrenadorService entrenadorService =
            new EntrenadorService(entRepo, pokemonRepo);

    private final PokemonService pokemonService =
            new PokemonService(pokemonRepo, entRepo, movRepo);

    private final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        new MainApp_SQL().run();
    }

    private void run() {
        while (true) {
            System.out.println("\n--- POKEDEX MANAGER (MySQL) ---");
            System.out.println("1) Gestionar Pokemons");
            System.out.println("2) Gestionar Entrenadores");
            System.out.println("3) Gestionar Movimientos");
            System.out.println("4) Reportes");
            System.out.println("0) Salir");
            System.out.print("Seleccione una opción: ");

            switch (sc.nextLine().trim()) {
                case "1" -> menuPokemons();
                case "2" -> menuEntrenadores();
                case "3" -> menuMovimientos();
                case "4" -> menuReportes();
                case "0" -> { return; }
                default -> System.out.println("Opción no válida");
            }
        }
    }

    // ================= POKEMONS =================

    private void menuPokemons() {
        while (true) {
            System.out.println("\n--- POKEMONS ---");
            System.out.println("1) Listar");
            System.out.println("2) Buscar por ID");
            System.out.println("3) Buscar por tipo");
            System.out.println("4) Crear");
            System.out.println("5) Borrar");
            System.out.println("6) Asociar movimiento");
            System.out.println("7) Quitar movimiento");
            System.out.println("8) Volver");
            System.out.print("Seleccione una opción: ");

            switch (sc.nextLine().trim()) {

                case "1" -> printPokemons(pokemonRepo.findAllToList());

                case "2" -> {
                    System.out.print("Introduzca el ID del Pokémon (3 dígitos): ");
                    String id = sc.nextLine();
                    pokemonRepo.findByIdOptional(id.trim());
                    Optional<Pokemon> pokemon = pokemonRepo.findByIdOptional(id);
                    System.out.println(pokemon);
                }

                case "3" -> {
                    System.out.println("Seleccione el tipo:");
                    TipoPokemon t = chooseTipo();
                    if (t != null)
                        printPokemons(pokemonRepo.findByTipo(t));
                }

                case "4" -> createPokemon();

                case "5" -> {
                    System.out.print("Introduzca el ID del Pokémon a borrar: ");
                    deletePokemon();
                }

                case "6" -> {
                    System.out.print("Introduzca el ID del Pokémon: ");
                    String pid = sc.nextLine().trim();
                    System.out.print("Introduzca el ID del Movimiento (2 letras + 4 números): ");
                    String mid = sc.nextLine().trim().toUpperCase();
                    try {
                        pokemonService.addMovimientoToPokemon(pid, mid);
                        System.out.println("Movimiento asociado correctamente.");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

                case "7" -> {
                    System.out.print("Introduzca el ID del Pokémon: ");
                    String pid = sc.nextLine().trim();
                    System.out.print("Introduzca el ID del Movimiento: ");
                    String mid = sc.nextLine().trim().toUpperCase();
                    try {
                        pokemonService.removeMovimientoFromPokemon(pid, mid);
                        System.out.println("Movimiento eliminado correctamente.");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

                case "8" -> { return; }
                default -> System.out.println("Opción no válida");
            }
        }
    }

    private TipoPokemon chooseTipo() {
        TipoPokemon[] tipos = TipoPokemon.values();
        for (int i = 0; i < tipos.length; i++)
            System.out.println((i + 1) + ") " + tipos[i]);

        System.out.print("Seleccione número: ");
        try {
            return tipos[Integer.parseInt(sc.nextLine()) - 1];
        } catch (Exception e) {
            return null;
        }
    }

    private void createPokemon() {
        System.out.print("ID del Pokémon (3 dígitos): ");
        String id = sc.nextLine().trim();
        System.out.print("Nombre del Pokémon: ");
        String nombre = sc.nextLine().trim();
        System.out.println("Seleccione tipo:");
        TipoPokemon tipo = chooseTipo();
        if (tipo == null) return;

        pokemonRepo.save(new Pokemon(id, nombre, tipo));
        System.out.println("Pokémon creado correctamente.");
    }

    private void deletePokemon() {
        try {
            pokemonService.deletePokemon(sc.nextLine().trim());
            System.out.println("Pokémon eliminado correctamente.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void printPokemons(List<Pokemon> list) {
        list.forEach(p ->
                System.out.println(p.getPokedexNumber() + " - " + p.getNombre())
        );
    }

    private void printPokemon(Pokemon p) {
        if (p != null)
            System.out.println(p.getPokedexNumber() + " - " + p.getNombre());
        else
            System.out.println("Pokémon no encontrado.");
    }

    // ================= ENTRENADORES =================

    private void menuEntrenadores() {
        while (true) {
            System.out.println("\n--- ENTRENADORES ---");
            System.out.println("1) Listar");
            System.out.println("2) Crear");
            System.out.println("3) Asignar pokemon");
            System.out.println("4) Ver pokemons");
            System.out.println("5) Borrar");
            System.out.println("6) Volver");
            System.out.print("Seleccione una opción: ");

            switch (sc.nextLine().trim()) {

                case "1" -> entRepo.findAllToList()
                        .forEach(e -> System.out.println(e.getId() + " - " + e.getNombre()));

                case "2" -> createEntrenador();

                case "3" -> {
                    System.out.print("Introduzca el DNI del entrenador: ");
                    String id = sc.nextLine().trim();
                    System.out.print("Introduzca el ID del Pokémon: ");
                    String pid = sc.nextLine().trim();
                    try {
                        entrenadorService.assignPokemonToEntrenador(id, pid);
                        System.out.println("Pokémon asignado correctamente.");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

                case "4" -> {
                    System.out.print("Introduzca el DNI del entrenador: ");
                    try {
                        entrenadorService.getPokemonsOfEntrenador(sc.nextLine().trim())
                                .forEach(p -> System.out.println(p.getNombre()));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

                case "5" -> {
                    System.out.print("Introduzca el DNI del entrenador a borrar: ");
                    entRepo.deleteById(sc.nextLine().trim());
                    System.out.println("Entrenador eliminado (si existía).");
                }

                case "6" -> { return; }
            }
        }
    }

    private void createEntrenador() {
        System.out.print("DNI del entrenador (8 dígitos + letra): ");
        String id = sc.nextLine().trim();
        System.out.print("Nombre del entrenador: ");
        String nombre = sc.nextLine().trim();

        System.out.println("Seleccione región:");
        Region[] regiones = Region.values();
        for (int i = 0; i < regiones.length; i++)
            System.out.println((i + 1) + ") " + regiones[i]);

        System.out.print("Seleccione número: ");
        Region r = regiones[Integer.parseInt(sc.nextLine()) - 1];

        entRepo.save(new Entrenador(id, nombre, r, null));
        System.out.println("Entrenador creado correctamente.");
    }

    // ================= MOVIMIENTOS =================

    private void menuMovimientos() {
        while (true) {
            System.out.println("\n--- MOVIMIENTOS ---");
            System.out.println("1) Listar");
            System.out.println("2) Crear");
            System.out.println("3) Borrar");
            System.out.println("4) Volver");
            System.out.print("Seleccione una opción: ");

            switch (sc.nextLine().trim()) {

                case "1" -> movRepo.findAllToList()
                        .forEach(m -> System.out.println(m.getId() + " - " + m.getNombre()));

                case "2" -> createMovimiento();

                case "3" -> {
                    System.out.print("Introduzca el ID del movimiento a borrar: ");
                    pokemonService.deleteMovimiento(sc.nextLine().trim());
                    System.out.println("Movimiento eliminado correctamente.");
                }

                case "4" -> { return; }
            }
        }
    }

    private void createMovimiento() {
        System.out.print("ID del movimiento (2 letras + 4 números): ");
        String id = sc.nextLine().trim().toUpperCase();
        System.out.print("Nombre del movimiento: ");
        String nombre = sc.nextLine().trim();

        System.out.println("Seleccione tipo:");
        TipoPokemon tipo = chooseTipo();

        System.out.print("Potencia del movimiento: ");
        int potencia = Integer.parseInt(sc.nextLine());

        movRepo.save(new Movimiento(id, nombre, tipo, potencia));
        System.out.println("Movimiento creado correctamente.");
    }

    // ================= REPORTES =================

    private void menuReportes() {
        pokemonRepo.countByTipo()
                .forEach((k, v) -> System.out.println(k + ": " + v));
    }
}
