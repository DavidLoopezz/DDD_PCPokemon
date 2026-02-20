package es.pokedex.repository;

import es.pokedex.domain.Movimiento;
import es.pokedex.domain.Pokemon;
import es.pokedex.domain.TipoPokemon;
import es.pokedex.util.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PokemonRepositoryMySQL implements IRepositorioExtend<Pokemon, String> {

    @Override
    public long count() {

        String sql = "SELECT COUNT(*) FROM pokemon";

        try (Connection con = DataBaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Error contando pokemons", e);
        }
    }

    @Override
    public void deleteById(String id) {

        String sql = "DELETE FROM pokemon WHERE pokedex_number = ?";

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error borrando pokemon", e);
        }
    }

    @Override
    public void deleteAll() {

        String sql = "DELETE FROM pokemon";

        try (Connection con = DataBaseConnection.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(sql);

        } catch (SQLException e) {
            throw new RuntimeException("Error borrando todos los pokemons", e);
        }
    }


    public Map<TipoPokemon, Long> countByTipo() {
        return Map.of();
    }

    @Override
    public boolean existsById(String id) {

        String sql = "SELECT 1 FROM pokemon WHERE pokedex_number = ?";

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Error comprobando pokemon", e);
        }
    }

    @Override
    public Pokemon findById(String id) {

        String sql = "SELECT * FROM pokemon WHERE pokedex_number = ?";

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Pokemon(
                        rs.getString("pokedex_number"),
                        rs.getString("nombre"),
                        TipoPokemon.valueOf(rs.getString("tipo"))
                );
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando pokemon", e);
        }
    }

    @Override
    public Optional<Pokemon> findByIdOptional(String id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public Iterable<Pokemon> findAll() {
        return findAllToList();
    }


    @Override
    public List<Pokemon> findAllToList() {

        List<Pokemon> list = new ArrayList<>();
        String sql = "SELECT * FROM pokemon";

        try (Connection con = DataBaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Pokemon(
                        rs.getString("pokedex_number"),
                        rs.getString("nombre"),
                        TipoPokemon.valueOf(rs.getString("tipo"))
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando pokemons", e);
        }

        return list;
    }

    @Override
    public <S extends Pokemon> S save(S entity) {

        String sql = """
            INSERT INTO pokemon (pokedex_number, nombre, tipo)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                nombre = VALUES(nombre),
                tipo = VALUES(tipo)
            """;

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entity.getPokedexNumber());
            ps.setString(2, entity.getNombre());
            ps.setString(3, entity.getTipo().name());
            ps.executeUpdate();

            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando pokemon", e);
        }
    }


    public List<Pokemon> findByTipo(TipoPokemon tipo) {

        List<Pokemon> resultado = new ArrayList<>();

        String sql = "SELECT * FROM pokemon WHERE tipo = ?";

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tipo.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                resultado.add(new Pokemon(
                        rs.getString("pokedex_number"),
                        rs.getString("nombre"),
                        TipoPokemon.valueOf(rs.getString("tipo"))
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando pokemons por tipo", e);
        }

        return resultado;
    }
}

