package es.pokedex.repository;

import es.pokedex.domain.Entrenador;
import es.pokedex.domain.Region;
import es.pokedex.domain.TipoPokemon;
import es.pokedex.util.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntrenadorRepositoryMySQL implements IRepositorioExtend<Entrenador, String> {

    @Override
    public Entrenador findById(String id) {

        String sql = "SELECT * FROM entrenador WHERE id = ?";

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Entrenador(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        Region.valueOf(rs.getString("region")),
                        new ArrayList<>()
                );
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando entrenador", e);
        }
    }

    @Override
    public Optional<Entrenador> findByIdOptional(String id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<Entrenador> findAllToList() {

        List<Entrenador> list = new ArrayList<>();
        String sql = "SELECT * FROM entrenador";

        try (Connection con = DataBaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Entrenador(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        Region.valueOf(rs.getString("region")),
                        new ArrayList<>()
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando entrenadores", e);
        }

        return list;
    }

    public List<Entrenador> findAll() {
        return findAllToList();
    }

    @Override
    public <S extends Entrenador> S save(S entity) {

        String sql = """
            INSERT INTO entrenador (id, nombre, region)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                nombre = VALUES(nombre),
                region = VALUES(region)
            """;

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entity.getId());
            ps.setString(2, entity.getNombre());
            ps.setString(3, entity.getRegion().name());
            ps.executeUpdate();

            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando entrenador", e);
        }
    }

    @Override
    public void deleteById(String id) {

        String sql = "DELETE FROM entrenador WHERE id = ?";

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error borrando entrenador", e);
        }
    }

    @Override
    public void deleteAll() {

        String sql = "DELETE FROM entrenador";

        try (Connection con = DataBaseConnection.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(sql);

        } catch (SQLException e) {
            throw new RuntimeException("Error borrando entrenadores", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        return findById(id) != null;
    }

    @Override
    public long count() {
        return findAllToList().size();
    }

    // ===== OBLIGATORIO POR IRepositorioExtend =====

    public Map<TipoPokemon, Long> countByTipo() {
        return Map.of(); // NO APLICA
    }
}
