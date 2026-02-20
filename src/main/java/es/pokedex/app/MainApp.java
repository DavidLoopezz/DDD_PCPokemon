package es.pokedex.app;

import es.pokedex.domain.Entrenador;
import es.pokedex.domain.Movimiento;
import es.pokedex.domain.Pokemon;
import es.pokedex.domain.Region;
import es.pokedex.domain.TipoPokemon;
import es.pokedex.exception.EntityNotFoundException;
import es.pokedex.repository.EntrenadorRepository;
import es.pokedex.repository.MovimientoRepository;
import es.pokedex.repository.PokemonRepository;
import es.pokedex.service.EntrenadorService;
import es.pokedex.service.PokemonService;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import es.pokedex.repository.*;
import es.pokedex.service.EntrenadorService;
import es.pokedex.service.PokemonService;

// Clase principal del programa: gestiona menús y delega operaciones a servicios/repositorios.

public class MainApp {
    private static final String DATA_DIR = "data";

    private final PokemonRepository pokemonRepo = new PokemonRepository(DATA_DIR);
    private final MovimientoRepository movRepo = new MovimientoRepository(DATA_DIR);
    private final EntrenadorRepository entRepo = new EntrenadorRepository(DATA_DIR);

    private final EntrenadorService entrenadorService = new EntrenadorService(entRepo, pokemonRepo);
    private final PokemonService pokemonService = new PokemonService(pokemonRepo, entRepo, movRepo);

    private final Scanner sc = new Scanner(System.in);

    // Entrada principal, crea la app y lanza el bucle de menús.
    public static void main(String[] args) {
        new MainApp().run();
    }

    // Muestra el menú principal y redirige a submenús.
    private void run() {
        while (true) {
            System.out.println("\n--- POKEDEX MANAGER ---");
            System.out.println("1) Gestionar Pokemons");
            System.out.println("2) Gestionar Entrenadores");
            System.out.println("3) Gestionar Movimientos");
            System.out.println("4) Reportes");
            System.out.println("0) Salir");
            System.out.print("Elige opción: ");
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1" -> menuPokemons();
                case "2" -> menuEntrenadores();
                case "3" -> menuMovimientos();
                case "4" -> menuReportes();
                case "0" -> { System.out.println("Adiós"); return; }
                default -> System.out.println("Opción no válida");
            }
        }
    }

    // ---------- POKEMONS ---------
    // Muestra menú de Pokemons.
    private void menuPokemons() {
        while (true) {
            System.out.println("\n--- POKEMONS ---");
            System.out.println("1) Listar todos");
            System.out.println("2) Buscar por PokedexNumber");
            System.out.println("3) Buscar por tipo");
            System.out.println("4) Crear");
            System.out.println("5) Editar");
            System.out.println("6) Borrar");
            System.out.println("7) Asociar movimiento a pokemon");
            System.out.println("8) Quitar movimiento de pokemon");
            System.out.println("9) Volver");
            System.out.print("Opción: ");
            String o = sc.nextLine().trim();
            switch (o) {
                case "1" -> printPokemons(pokemonRepo.findAllToList());
                case "2" -> { System.out.print("PokedexNumber: "); String id = sc.nextLine().trim(); printPokemon(pokemonRepo.findById(id)); }
                case "3" -> { TipoPokemon t = chooseTipo(); if (t != null) printPokemons(pokemonRepo.findByTipo(t)); }
                case "4" -> createPokemon();
                case "5" -> editPokemon();
                case "6" -> deletePokemon();
                case "7" -> assignMovimientoToPokemon();
                case "8" -> removeMovimientoFromPokemon();
                case "9" -> { return; }
                default -> System.out.println("No válido");
            }
        }
    }

    // Pide al usuario seleccionar un tipo y devuelve el enum correspondiente.
    private TipoPokemon chooseTipo() {
        TipoPokemon[] vals = TipoPokemon.values();
        for (int i = 0; i < vals.length; i++) System.out.printf("%d) %s%n", i+1, vals[i]);
        System.out.print("Elige tipo (número): ");
        try {
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (idx < 0 || idx >= vals.length) { System.out.println("Tipo no válido"); return null; }
            return vals[idx];
        } catch (NumberFormatException e) { System.out.println("Entrada inválida"); return null; }
    }

    // Crea un nuevo Pokemon y lo guarda.
    private void createPokemon() {
        try {
            System.out.print("PokedexNumber (3 dígitos): ");
            String id = sc.nextLine().trim();
            System.out.print("Nombre: ");
            String nombre = sc.nextLine().trim();
            TipoPokemon tipo = chooseTipo();
            if (tipo == null) return;
            Pokemon p = new Pokemon(id, nombre, tipo);
            pokemonRepo.save(p);
            System.out.println("Pokemon creado.");
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // Edita nombre y/o tipo de un Pokemon existente.
    private void editPokemon() {
        System.out.print("PokedexNumber a editar: ");
        String id = sc.nextLine().trim();
        Pokemon p = pokemonRepo.findById(id);
        if (p == null) { System.out.println("No existe"); return; }
        System.out.print("Nuevo nombre (ENTER para mantener '" + p.getNombre() + "'): ");
        String n = sc.nextLine();
        if (!n.isBlank()) p.setNombre(n.trim());
        System.out.println("Tipo actual: " + p.getTipo());
        System.out.print("¿Cambiar tipo? (S/N): ");
        if ("S".equalsIgnoreCase(sc.nextLine().trim())) {
            TipoPokemon t = chooseTipo(); if (t != null) p.setTipo(t);
        }
        pokemonRepo.save(p);
        System.out.println("Actualizado.");
    }

    // Borra un Pokemon y limpia referencias usando PokemonService.
    private void deletePokemon() {
        System.out.print("PokedexNumber a borrar: ");
        String id = sc.nextLine().trim();
        Pokemon p = pokemonRepo.findById(id);
        if (p == null) { System.out.println("No existe"); return; }
        System.out.printf("Eliminar %s (%s)? (S/N): ", p.getNombre(), p.getPokedexNumber());
        if ("S".equalsIgnoreCase(sc.nextLine().trim())) {
            try {
                int cleaned = pokemonService.deletePokemon(id);
                System.out.printf("Eliminado. Referencias limpiadas en %d entrenadores.%n", cleaned);
            } catch (EntityNotFoundException ex) {
                System.out.println("No existe: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Error inesperado: " + ex.getMessage());
            }
        } else System.out.println("Cancelado.");
    }

    // Asocia un movimiento a un pokemon ..
    private void assignMovimientoToPokemon() {
        System.out.print("PokedexNumber: ");
        String pid = sc.nextLine().trim();
        System.out.print("Movimiento ID (2 letras + 4 dígitos): ");
        String mid = sc.nextLine().trim().toUpperCase();
        try {
            boolean added = pokemonService.addMovimientoToPokemon(pid, mid);
            if (added) System.out.println("Movimiento añadido al pokemon.");
            else System.out.println("El pokemon ya tenía ese movimiento.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Quita la asociación movimiento <----> pokemon usando el servicio.
    private void removeMovimientoFromPokemon() {
        System.out.print("PokedexNumber: ");
        String pid = sc.nextLine().trim();
        System.out.print("Movimiento ID: ");
        String mid = sc.nextLine().trim().toUpperCase();
        try {
            boolean removed = pokemonService.removeMovimientoFromPokemon(pid, mid);
            if (removed) System.out.println("Movimiento quitado del pokemon.");
            else System.out.println("El pokemon no tenía ese movimiento.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Imprime resumen simple de Pokemons y quiénes los usan.
    private void printPokemons(List<Pokemon> list) {
        System.out.println("Pokedex | Nombre | Tipo | #Movimientos | Usado por");
        for (Pokemon p : list) {
            List<String> users = entRepo.findAllToList().stream()
                    .filter(e -> e.getPokedexNumbers().contains(p.getPokedexNumber()))
                    .map(e -> e.getNombre())
                    .collect(Collectors.toList());
            System.out.printf("%s | %s | %s | %d | %s%n", p.getPokedexNumber(), p.getNombre(), p.getTipo(), p.getMovimientoIds().size(), String.join(", ", users));
        }
    }

    // Imprime un único Pokemon (usa printPokemons).
    private void printPokemon(Pokemon p) {
        if (p == null) { System.out.println("No encontrado"); return; }
        printPokemons(List.of(p));
    }

    // ---------- ENTRENADORES ---------
    // Menu simple para entrenadores.
    private void menuEntrenadores() {
        while (true) {
            System.out.println("\n--- ENTRENADORES ---");
            System.out.println("1) Listar todos");
            System.out.println("2) Crear");
            System.out.println("3) Asignar pokemon");
            System.out.println("4) Mostrar pokemons de entrenador");
            System.out.println("5) Borrar entrenador");
            System.out.println("6) Volver");
            System.out.print("Opción: ");
            String o = sc.nextLine().trim();
            switch (o) {
                case "1" -> printEntrenadores(entRepo.findAllToList());
                case "2" -> createEntrenador();
                case "3" -> assignPokemonToEntrenador();
                case "4" -> showPokemonsOfEntrenador();
                case "5" -> deleteEntrenador();
                case "6" -> { return; }
                default -> System.out.println("No válido");
            }
        }
    }

    // Crea un entrenador leyendo DNI, nombre y región.
    private void createEntrenador() {
        try {
            System.out.print("DNI (8 dígitos + letra, e.g. 65788344F): ");
            String id = sc.nextLine().trim().toUpperCase();
            System.out.print("Nombre: ");
            String nombre = sc.nextLine().trim();
            System.out.println("Selecciona región:");
            Region[] regs = Region.values();
            for (int i=0;i<regs.length;i++) System.out.printf("%d) %s%n", i+1, regs[i]);
            System.out.print("Opción: ");
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            Region r = regs[idx];
            var e = new Entrenador(id, nombre, r, null);
            entRepo.save(e);
            System.out.println("Entrenador creado.");
        } catch (Exception ex) { System.out.println("Error: " + ex.getMessage()); }
    }

    // Pide DNI y pokemon, y delega la asignación al servicio de entrenadores.
    private void assignPokemonToEntrenador() {
        try {
            System.out.print("Entrenador DNI: ");
            String id = sc.nextLine().trim().toUpperCase();
            System.out.print("PokedexNumber: ");
            String pid = sc.nextLine().trim();
            entrenadorService.assignPokemonToEntrenador(id, pid);
            System.out.println("Pokemon asignado.");
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // Muestra los pokemons asociados a un entrenador (usa EntrenadorService).
    private void showPokemonsOfEntrenador() {
        System.out.print("Entrenador DNI: ");
        String id = sc.nextLine().trim().toUpperCase();
        try {
            List<Pokemon> ps = entrenadorService.getPokemonsOfEntrenador(id);
            printPokemons(ps);
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // Borra un entrenador tras confirmación.
    private void deleteEntrenador() {
        System.out.print("Entrenador DNI a borrar: ");
        String id = sc.nextLine().trim().toUpperCase();
        var e = entRepo.findById(id);
        if (e == null) { System.out.println("No existe"); return; }
        System.out.printf("Eliminar %s (%s)? (S/N): ", e.getNombre(), e.getId());
        if ("S".equalsIgnoreCase(sc.nextLine().trim())) {
            entRepo.deleteById(id);
            System.out.println("Entrenador eliminado.");
        } else System.out.println("Cancelado.");
    }

    // Imprime lista simple de entrenadores.
    private void printEntrenadores(List<Entrenador> list) {
        System.out.println("ID | Nombre | Region | Pokemons");
        for (var e : list) {
            System.out.printf("%s | %s | %s | %s%n", e.getId(), e.getNombre(), e.getRegion(), String.join(", ", e.getPokedexNumbers()));
        }
    }

    // ---------- MOVIMIENTOS ---------
    // Muestra menú de movimientos.
    private void menuMovimientos() {
        while (true) {
            System.out.println("\n--- MOVIMIENTOS ---");
            System.out.println("1) Listar todos");
            System.out.println("2) Crear");
            System.out.println("3) Borrar");
            System.out.println("4) Volver");
            System.out.print("Opción: ");
            String o = sc.nextLine().trim();
            switch (o) {
                case "1" -> printMovimientos(movRepo.findAllToList());
                case "2" -> createMovimiento();
                case "3" -> deleteMovimiento();
                case "4" -> { return; }
                default -> System.out.println("No válido");
            }
        }
    }

    // Crea un movimiento y lo guarda.
    private void createMovimiento() {
        try {
            System.out.print("ID (2 letras + 4 dígitos, e.g. TH1023): ");
            String id = sc.nextLine().trim().toUpperCase();
            System.out.print("Nombre: ");
            String nombre = sc.nextLine().trim();
            TipoPokemon tipo = chooseTipo();
            if (tipo == null) return;
            System.out.print("Potencia (int): ");
            int pot = Integer.parseInt(sc.nextLine().trim());
            var m = new Movimiento(id, nombre, tipo, pot);
            movRepo.save(m);
            System.out.println("Movimiento creado.");
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // Borra un movimiento y limpia su uso en pokemons usando PokemonService.
    private void deleteMovimiento() {
        System.out.print("ID movimiento a borrar: ");
        String id = sc.nextLine().trim().toUpperCase();
        var mov = movRepo.findById(id);
        if (mov == null) { System.out.println("No existe"); return; }
        System.out.printf("Eliminar %s (%s)? (S/N): ", mov.getNombre(), mov.getId());
        if ("S".equalsIgnoreCase(sc.nextLine().trim())) {
            try {
                int cleaned = pokemonService.deleteMovimiento(id);
                System.out.printf("Movimiento eliminado. Referencias limpiadas en %d pokemons.%n", cleaned);
            } catch (EntityNotFoundException ex) {
                System.out.println("No existe: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        } else System.out.println("Cancelado.");
    }

    // Imprime resumen simple de movimientos y qué pokemons lo usan.
    private void printMovimientos(List<Movimiento> list) {
        System.out.println("ID | Nombre | Tipo | Potencia | Usado por");
        for (var m : list) {
            var pokes = pokemonRepo.findByMovimientoId(m.getId()).stream().map(Pokemon::getNombre).collect(Collectors.toList());
            System.out.printf("%s | %s | %s | %d | %s%n", m.getId(), m.getNombre(), m.getTipo(), m.getPotencia(), String.join(", ", pokes));
        }
    }

    // ---------- REPORTES ---------
    // Muestra un conteopor orden, por tipo y top entrenadores.
    private void menuReportes() {
        System.out.println("\n--- REPORTES ---");
        System.out.println("Pokemons por tipo:");
        pokemonRepo.countByTipo().forEach((k,v)-> System.out.printf("%s: %d%n", k, v));
        System.out.println("Top entrenadores por nº pokemons:");
        entRepo.findAllToList().stream()
                .sorted((a,b) -> Integer.compare(b.getPokedexNumbers().size(), a.getPokedexNumbers().size()))
                .limit(10)
                .forEach(e -> System.out.printf("%s (%s): %d%n", e.getNombre(), e.getId(), e.getPokedexNumbers().size()));
    }
}


