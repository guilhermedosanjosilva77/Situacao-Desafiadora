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
                        rs.getLong("id_locacao"),
                        rs.getLong("Quadra_id_quadra"),
                        rs.getLong("Cliente_idCliente"),
                        rs.getDate("datalocacao"));
                Locacao.add(aluguel);
                // Adicionar uma linha de log para verificar se os dados estão sendo lidos
                // System.out.println("Aluguel lido do DB: " + rs.getLong("idlocacao")); 
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
        String sql = "SELECT * FROM locacao WHERE Quadra_id_quadra = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idQuadra);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Aluguel aluguel = new Aluguel(
                           rs.getLong("idlocacao"),
                        rs.getLong("Quadra_id_quadra"),
                        rs.getLong("Cliente_idCliente"),
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
    public void inserir(Aluguel aluguel) throws SQLException {

    // REGRA DE NEGÓCIO: cliente só pode ter 1 aluguel
    if (clienteJaPossuiAluguel(aluguel.getIdCliente())) {
        throw new SQLException("O cliente já possui uma quadra alugada!");
    }

    String sql = "INSERT INTO locacao (Quadra_id_quadra, Cliente_idCliente, datalocacao) VALUES (?,?,?)";

    try (Connection conn = ConnectionFactory.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        stmt.setLong(1, aluguel.getIdQuadra());
        stmt.setLong(2, aluguel.getIdCliente());
        stmt.setObject(3, aluguel.getDataLocacao());
        stmt.executeUpdate();

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                aluguel.setId_locacao(rs.getLong(1));
            }
        }

    } catch (SQLException e) {
        System.err.println("Erro ao inserir aluguel: " + aluguel.getIdCliente() + ". " + e.getMessage());
        throw e;
    }
}

    // ------------------------------------
    // UPDATE
    // ------------------------------------
    public void atualizar(Aluguel aluguel) {
        
        // CORREÇÃO: "data_locacao" foi trocado por "datalocacao"
        String sql = "UPDATE locacao SET Quadra_id_quadra = ?, Cliente_idCliente = ?, datalocacao = ? WHERE idlocacao = ?"; 

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

        // CUIDADO: Seu código usa id_locacao, mas em outros pontos usa idlocacao.
        // Vou manter id_locacao aqui, assumindo que DELETE usa o ID primário do aluguel.
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
            System.err.println("Erro ao deletar aluguel ID: " + id + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
            throw new SQLIntegrityConstraintViolationException();
        }
    }


    // Cliente já possui aluguel cadastrado
public boolean clienteJaPossuiAluguel(Long idCliente) throws SQLException {

    String sql = "SELECT COUNT(*) FROM locacao WHERE Cliente_idCliente = ?";

    try (Connection conn = ConnectionFactory.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setLong(1, idCliente);

        try (ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int qtd = rs.getInt(1);

                if (qtd > 0) {
                    // ERRO: cliente já possui um aluguel cadastrado
                    throw new SQLException(
                        "Erro: O cliente ID " + idCliente + " já possui um aluguel cadastrado."
                    );
                }

                return false; 
            }
        }
    } catch (SQLException e) {
        throw e; 
    }

    return false;
}

}