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
    // READ ALL
    // ======================================//
    public List<Cliente> buscarTodos() {

        List<Cliente> clientes = new ArrayList<>();

        String sql = "SELECT * FROM cliente";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();) {

            while (rs.next()) {
                Cliente cliente = new Cliente(
                        rs.getLong("id_cliente"),
                        rs.getString("Nome"),
                        rs.getString("Telefone"));
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

        // CORREÇÃO: Usar a instrução SELECT correta para buscar por ID
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?"; 

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // CORREÇÃO: Setar o ID no único parâmetro '?'
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cliente = new Cliente(
                            rs.getLong("id_cliente"),
                            rs.getString("Nome"),
                            rs.getString("Telefone"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cliente por ID: " + id + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
        }
        return cliente;
    }

    // ======================================//
    // CREATE
    // ======================================//
    public void inserir(Cliente cliente) {

        String sql = "INSERT INTO cliente (Nome, Telefone) VALUES (?, ?)"; 

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getTelefone());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    // Define o ID gerado de volta no objeto Cliente
                    cliente.setID(rs.getLong(1));

                }
            }

        } catch (SQLException e) {
            // Alterado o texto de erro para refletir Cliente (em vez de Categoria)
            System.err.println("Erro ao inserir cliente: " + cliente.getNome() + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------------------------
    // UPDATE
    // ------------------------------------
    public void atualizar(Cliente cliente) {

        String sql = "UPDATE cliente SET Nome = ?, Telefone = ? WHERE id_cliente = ?";

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

        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            // executa a exclusão
            int linhasAfetadas = stmt.executeUpdate();
            System.out.println("Tentativa de deletar Cliente ID " + id + ". Linhas afetadas: " + linhasAfetadas);

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLIntegrityConstraintViolationException();
        }

        catch (SQLException e) {
            // Alterado o texto de erro para refletir Cliente (em vez de Categoria)
            System.err.println("Erro ao deletar cliente ID: " + id + ". Detalhes: " + e.getMessage());
            e.printStackTrace();
            throw new SQLIntegrityConstraintViolationException();
        }
    }
}