package es.pokedex.repository;

import es.pokedex.domain.Movimiento;
import es.pokedex.domain.TipoPokemon;
import es.pokedex.util.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MovimientoRepositoryMySQL implements IRepositorioExtend<Movimiento, String> {

    @Override
    public Movimiento findById(String id) {

        String sql = "SELECT * FROM movimiento WHERE id = ?";

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Movimiento(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        TipoPokemon.valueOf(rs.getString("tipo")),
                        rs.getInt("potencia")
                );
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando movimiento", e);
        }
    }

    @Override
    public Optional<Movimiento> findByIdOptional(String id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<Movimiento> findAllToList() {

        List<Movimiento> list = new ArrayList<>();
        String sql = "SELECT * FROM movimiento";

        try (Connection con = DataBaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Movimiento(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        TipoPokemon.valueOf(rs.getString("tipo")),
                        rs.getInt("potencia")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando movimientos", e);
        }

        return list;
    }

    public List<Movimiento> findAll() {
        return findAllToList();
    }

    @Override
    public <S extends Movimiento> S save(S entity) {

        String sql = """
            INSERT INTO movimiento (id, nombre, tipo, potencia)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                nombre = VALUES(nombre),
                tipo = VALUES(tipo),
                potencia = VALUES(potencia)
            """;

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entity.getId());
            ps.setString(2, entity.getNombre());
            ps.setString(3, entity.getTipo().name());
            ps.setInt(4, entity.getPotencia());
            ps.executeUpdate();

            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando movimiento", e);
        }
    }

    @Override
    public void deleteById(String id) {

        String sql = "DELETE FROM movimiento WHERE id = ?";

        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error borrando movimiento", e);
        }
    }

    @Override
    public void deleteAll() {

        String sql = "DELETE FROM movimiento";

        try (Connection con = DataBaseConnection.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(sql);

        } catch (SQLException e) {
            throw new RuntimeException("Error borrando movimientos", e);
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
        return Map.of(); // o tu implementación SQL
    }

}
