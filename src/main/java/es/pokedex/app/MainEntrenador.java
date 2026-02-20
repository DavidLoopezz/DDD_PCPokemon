package es.pokedex.app;

import es.pokedex.domain.Entrenador;
import es.pokedex.domain.Region;
import es.pokedex.repository.EntrenadorRepository;
import es.pokedex.repository.PokemonRepository;
import es.pokedex.service.EntrenadorService;

import java.util.Scanner;

public class MainEntrenador {

    private static final String DATA_DIR = "data";

    public static void main(String[] args) {

        EntrenadorRepository entRepo = new EntrenadorRepository(DATA_DIR);
        PokemonRepository pokeRepo = new PokemonRepository(DATA_DIR);
        EntrenadorService service = new EntrenadorService(entRepo, pokeRepo);

        Scanner sc = new Scanner(System.in);

        // Bucle principal del main de pruebas para Entrenador
        while (true) {

            System.out.println("\n--- TEST REPO: ENTRENADOR ---");
            System.out.println("1) Listar entrenadores");
            System.out.println("2) Crear entrenador");
            System.out.println("3) Buscar por ID");
            System.out.println("4) Asignar Pokémon");
            System.out.println("5) Mostrar Pokémon de un entrenador");
            System.out.println("6) Borrar entrenador");
            System.out.println("0) Salir");
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();

            switch (op) {

                // Lista todos los entrenadores usa entRepo.findAllToList().
                case "1" -> entRepo.findAllToList().forEach(e ->
                        System.out.println(e.getId() + " - " + e.getNombre() +
                                " (" + e.getRegion() + ") Pokemons: " + e.getPokedexNumbers())
                );

                // Crea un entrenador: lee DNI, nombre y región
                case "2" -> {
                    try {
                        System.out.print("DNI (8 dígitos + letra): ");
                        String id = sc.nextLine().trim().toUpperCase();

                        System.out.print("Nombre: ");
                        String nombre = sc.nextLine().trim();

                        System.out.println("Región:");
                        for (int i = 0; i < Region.values().length; i++)
                            System.out.println((i + 1) + ") " + Region.values()[i]);
                        System.out.print("Elige: ");
                        int r = Integer.parseInt(sc.nextLine()) - 1;

                        Entrenador e = new Entrenador(id, nombre, Region.values()[r], null);
                        entRepo.save(e);

                        System.out.println("Entrenador creado.");

                    } catch (Exception e) {
                        // Muestra error por consola si hay formato erroneo
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                // Busca un entrenador por DNI y lo muestra por consola.
                case "3" -> {
                    System.out.print("DNI: ");
                    String id = sc.nextLine().trim().toUpperCase();
                    Entrenador e = entRepo.findById(id);
                    if (e == null) System.out.println("No existe");
                    else System.out.println(e.getId() + " - " + e.getNombre() +
                            " (" + e.getRegion() + ") Pokemons: " + e.getPokedexNumbers());
                }

                // Asigna un Pokémon a un entrenador.
                case "4" -> {
                    System.out.print("DNI Entrenador: ");
                    String id = sc.nextLine().trim().toUpperCase();

                    System.out.print("PokedexNumber: ");
                    String pid = sc.nextLine().trim();

                    try {
                        service.assignPokemonToEntrenador(id, pid);
                        System.out.println("Asignado.");
                    } catch (Exception e) {
                        // Captura errores
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                // Muestra los Pokemons asociados a un entrenador usando el servicio.
                case "5" -> {
                    System.out.print("DNI: ");
                    String id = sc.nextLine().trim().toUpperCase();
                    try {
                        service.getPokemonsOfEntrenador(id)
                                .forEach(p -> System.out.println(p.getPokedexNumber() + " - " + p.getNombre()));
                    } catch (Exception e) {
                        // Muestra error si el entrenador no existe
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                // Borra un entrenador por DNI, llama entRepo.deleteById.
                case "6" -> {
                    System.out.print("DNI: ");
                    String id = sc.nextLine().trim().toUpperCase();
                    entRepo.deleteById(id);
                    System.out.println("Eliminado (si existía).");
                }

                // Salir del main de pruebas.
                case "0" -> {
                    System.out.println("Adiós.");
                    return;
                }

                // Opción invlida
                default -> System.out.println("Opción inválida.");
            }
        }
    }
}
