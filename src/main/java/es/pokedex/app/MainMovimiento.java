package es.pokedex.app;

import es.pokedex.domain.Movimiento;
import es.pokedex.domain.TipoPokemon;
import es.pokedex.repository.MovimientoRepository;
import es.pokedex.repository.PokemonRepository;
import es.pokedex.repository.EntrenadorRepository;
import es.pokedex.service.PokemonService;

import java.util.Scanner;

public class MainMovimiento {

    private static final String DATA_DIR = "data";

    public static void main(String[] args) {
        MovimientoRepository repo = new MovimientoRepository(DATA_DIR);
        PokemonRepository pokeRepo = new PokemonRepository(DATA_DIR);
        EntrenadorRepository entRepo = new EntrenadorRepository(DATA_DIR);
        PokemonService service = new PokemonService(pokeRepo, entRepo, repo);

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- TEST REPO: MOVIMIENTO ---");
            System.out.println("1) Listar movimientos");
            System.out.println("2) Crear movimiento");
            System.out.println("3) Buscar por ID");
            System.out.println("4) Borrar movimiento (usa PokemonService)");
            System.out.println("5) Asociar movimiento a pokemon");
            System.out.println("0) Salir");
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();

            switch (op) {
                case "1" -> repo.findAllToList().forEach(m ->
                        System.out.println(m.getId() + " - " + m.getNombre() +
                                " (" + m.getTipo() + ", " + m.getPotencia() + ")")
                );

                case "2" -> {
                    try {
                        System.out.print("ID (2 letras + 4 números): ");
                        String id = sc.nextLine().trim().toUpperCase();

                        System.out.print("Nombre: ");
                        String nombre = sc.nextLine().trim();

                        System.out.println("Tipo:");
                        for (int i = 0; i < TipoPokemon.values().length; i++)
                            System.out.println((i + 1) + ") " + TipoPokemon.values()[i]);
                        System.out.print("Elige tipo: ");
                        int t = Integer.parseInt(sc.nextLine()) - 1;

                        System.out.print("Potencia: ");
                        int potencia = Integer.parseInt(sc.nextLine());

                        Movimiento mov = new Movimiento(id, nombre, TipoPokemon.values()[t], potencia);
                        repo.save(mov);
                        System.out.println("Guardado correctamente.");

                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                case "3" -> {
                    System.out.print("ID: ");
                    String id = sc.nextLine().trim().toUpperCase();
                    Movimiento m = repo.findById(id);
                    if (m == null) System.out.println("No existe");
                    else System.out.println(m.getId() + " - " + m.getNombre() +
                            " (" + m.getTipo() + ", " + m.getPotencia() + ")");
                }

                case "4" -> {
                    System.out.print("ID: ");
                    String id = sc.nextLine().trim().toUpperCase();
                    try {
                        int cleaned = service.deleteMovimiento(id);
                        System.out.println("Eliminado. Referencias limpiadas en " + cleaned + " pokemons.");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                case "5" -> {
                    System.out.print("PokedexNumber: ");
                    String pid = sc.nextLine().trim();
                    System.out.print("Movimiento ID (2 letras + 4 dígitos): ");
                    String mid = sc.nextLine().trim().toUpperCase();
                    try {
                        boolean added = service.addMovimientoToPokemon(pid, mid);
                        if (added) System.out.println("Movimiento añadido al pokemon.");
                        else System.out.println("El pokemon ya tenía ese movimiento.");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                case "0" -> {
                    System.out.println("Adiós.");
                    return;
                }

                default -> System.out.println("Opción inválida.");
            }
        }
    }
}


