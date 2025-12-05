package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Cliente;
import util.ConnectionFactory;

public class ClienteDAO {

    // ======================================//
    // READ
    // ======================================//
    public List<Cliente> buscarTodos() {

        List<Cliente> clientes = new ArrayList<>();

        String sql = "SELECT * FROM cliente";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();) {

            while (rs.next()) {
                Cliente cliente = new Cliente(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("telefone"));
                clientes.add(cliente);
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return clientes;
    }

    // ======================================//
    // READ BY ID
    // ======================================//
    public Cliente buscarPorId(Long id) {

        Cliente cliente = null;

        String sql = "SELECT id, nome, telefone FROM cliente WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cliente = new Cliente(
                            rs.getLong("id"),
                            rs.getString("nome"),
                            rs.getString("telefone"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar categoria por ID: " + id + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
        }
        return cliente;
    }

    // ======================================//
    // CREATE
    // ======================================//
    public void inserir(Cliente cliente) {

        // usa Statement.RETURN_GENERATED_KEYS para solicitar o ID gerado
        String sql = "INSERT INTO cliente (nome,telefone) VALUES (?,?)";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getTelefone());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    // define o ID no objeto Produto que foi passado (importante para a API)
                     cliente.setID(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir categoria: " + cliente.getNome() + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------------------------
    // UPDATE
    // ------------------------------------
    public void atualizar(Cliente cliente) {

        String sql = "UPDATE produtos SET nome = ?, telefone = ?  WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // define os parâmetros (os novos valores)
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getTelefone());
            // define o ID no WHERE (o último '?')
            stmt.setLong(3, cliente.getID());

            // executa a atualização
            int linhasAfetadas = stmt.executeUpdate();
            System.out.println("Cliente ID " + cliente.getID() + " atualizado. Linhas afetadas: " + linhasAfetadas);

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar cliente ID: " + cliente.getID() + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------
    public void deletar(Long id) throws SQLIntegrityConstraintViolationException {

        String sql = "DELETE FROM cliente WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            // executa a exclusão
            int linhasAfetadas = stmt.executeUpdate();
            System.out.println("Tentativa de deletar Categoria ID " + id + ". Linhas afetadas: " + linhasAfetadas);

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
