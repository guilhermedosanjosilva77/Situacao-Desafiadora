package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Aluguel;
import util.ConnectionFactory;



public class AluguelDAO {

    // ======================================//
    // READ
    // ======================================//
    public List<Aluguel> buscarTodos() {

        List<Aluguel> Locacao = new ArrayList<>();

        String sql = "SELECT * FROM locacao";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();) {

            while (rs.next()) {
                Aluguel aluguel = new Aluguel(
                        rs.getLong("idlocacao"),
                        rs.getLong("quadra_idquadra"),
                        rs.getLong("cliente_idcliente"),
                       rs.getDate("datalocacao"));
                Locacao.add(aluguel);
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar locacoes: " + e.getMessage());
            e.printStackTrace();
        }
        return Locacao;
    }

    // ======================================//
    // READ BY ID
    // ======================================//
    public List<Aluguel> buscarPorQuadraId(Long idQuadra) {

        List<Aluguel> lista = new ArrayList<>();
        String sql = "SELECT * FROM locacao WHERE quadra_idquadra = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idQuadra);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Aluguel aluguel = new Aluguel(
                            rs.getLong("idlocacao"),
                            rs.getLong("quadra_idquadra"),
                            rs.getLong("cliente_idcliente"),
                            rs.getDate("datalocacao"));
                    lista.add(aluguel);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ======================================//
    // CREATE
    // ======================================//
    public void inserir(Aluguel aluguel) {

        // usa Statement.RETURN_GENERATED_KEYS para solicitar o ID gerado
        String sql = "INSERT INTO locacao (quadra_idquadra, cliente_idcliente, datalocacao) VALUES (?,?,?)";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, aluguel.getIdQuadra());
            stmt.setLong(2, aluguel.getIdCliente());
            stmt.setObject(3, aluguel.getDataLocacao());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    // define o ID no objeto Produto que foi passado (importante para a API)
                   aluguel.setId_locacao(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            System.err
                    .println("Erro ao inserir categoria: " + aluguel.getIdCliente() + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------------------------
    // UPDATE
    // ------------------------------------
    public void atualizar(Aluguel aluguel) {

        String sql = "UPDATE locacao SET quadra_idquadra = ?, cliente_idcliente = ?, data_locacao = ? WHERE idlocacao = ?";

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, aluguel.getIdQuadra());
            stmt.setLong(2, aluguel.getIdCliente());
            stmt.setObject(3, aluguel.getDataLocacao());
            stmt.setLong(4, aluguel.getId_locacao());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------
    public void deletar(Long id) throws SQLIntegrityConstraintViolationException {

        String sql = "DELETE FROM locacao WHERE id_locacao = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            // executa a exclusão
            int linhasAfetadas = stmt.executeUpdate();
            System.out.println("Tentativa de deletar Locação ID " + id + ". Linhas afetadas: " + linhasAfetadas);

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLIntegrityConstraintViolationException();
        }

        catch (SQLException e) {
            System.err.println("Erro ao deletar categoria ID: " + id + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
            throw new SQLIntegrityConstraintViolationException();
        }
    }
}
