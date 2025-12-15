package dao;

    import java.sql.Connection;
    import java.sql.Date;
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
        // READ ALL
        // ======================================//
        public List<Aluguel> buscarTodos() {

            List<Aluguel> Locacao = new ArrayList<>();

            // Assumi que as colunas são: id_locacao, Quadra_id_quadra, Cliente_idCliente,
            // datalocacao
            String sql = "SELECT * FROM locacao";

            try (Connection conn = ConnectionFactory.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();) {

                while (rs.next()) {
                    Aluguel aluguel = new Aluguel(
                            rs.getLong("id_locacao"), // Corrigido para consistência
                            rs.getLong("Quadra_id_quadra"),
                            rs.getLong("Cliente_idCliente"),
                            rs.getDate("datalocacao"),
                            rs.getDouble("preco")
                        );
                    Locacao.add(aluguel);
                }
            } catch (Exception e) {
                System.err.println("Erro ao buscar locacoes: " + e.getMessage());
                e.printStackTrace();
            }
            return Locacao;
        }

        // ======================================//
        // NOVO: READ BY ALUGUEL ID (Necessário para o DELETE na API)
        // ======================================//
        public List<Aluguel> buscarPorId(Long id) {

            List<Aluguel> lista = new ArrayList<>();
            String sql = "SELECT * FROM locacao WHERE id_locacao = ?";

            try (Connection conn = ConnectionFactory.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, id);

                try (ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {

                        Aluguel aluguel = new Aluguel(
                                rs.getLong("id_locacao"), // Corrigido para consistência
                                rs.getLong("Quadra_id_quadra"),
                                rs.getLong("Cliente_idCliente"),
                                rs.getDate("datalocacao"),
                                rs.getDouble("preco")

                            );
                        lista.add(aluguel);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return lista;
        }

        // ======================================//
        // READ BY QUADRA ID
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
                                rs.getLong("id_locacao"), // Corrigido para consistência
                                rs.getLong("Quadra_id_quadra"),
                                rs.getLong("Cliente_idCliente"),
                                rs.getDate("datalocacao"),
                                rs.getDouble("preco")

                            );
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
            if (clienteJaPossuiLocacao(aluguel.getIdCliente())) {
                throw new RuntimeException("Cliente já possui uma locação ativa");

            }

            String sql = "INSERT INTO locacao (Quadra_id_quadra, Cliente_idCliente, datalocacao,preco) VALUES (?,?,?,?)";

            try (Connection conn = ConnectionFactory.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setLong(1, aluguel.getIdQuadra());
                stmt.setLong(2, aluguel.getIdCliente());
                stmt.setDate(3, aluguel.getDataLocacao());
                stmt.setDouble(4, aluguel.getPreco());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        // define o ID no objeto Aluguel que foi passado
                        aluguel.setId_locacao(rs.getLong(1));
                    }
                }

            } catch (SQLException e) {
                System.err
                        .println("Erro ao inserir aluguel: " + aluguel.getIdCliente() + ". Detalhes: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // ------------------------------------
        // UPDATE
        // ------------------------------------
        public void atualizar(Aluguel aluguel) {

            double precoFinal = buscarPrecoAtual(aluguel.getId_locacao());

            if (dataFoiAlterada(aluguel.getId_locacao(), aluguel.getDataLocacao())) {
            precoFinal += 50.0;
        }

            // CORREÇÃO: idlocacao -> id_locacao (Consistência)
            String sql = "UPDATE locacao SET Quadra_id_quadra = ?, Cliente_idCliente = ?, datalocacao = ?, preco=?  WHERE id_locacao = ?";

            try (
                    Connection conn = ConnectionFactory.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, aluguel.getIdQuadra());
                stmt.setLong(2, aluguel.getIdCliente());
                stmt.setObject(3, aluguel.getDataLocacao());
                stmt.setDouble(4, precoFinal);
                stmt.setLong(5, aluguel.getId_locacao());


                stmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // ------------------------------------
        // DELETE
        // ------------------------------------
        public void deletar(Long id) throws SQLIntegrityConstraintViolationException, SQLException {

            // CORREÇÃO: Adicionei SQLException à assinatura para lidar melhor com o catch
            String sql = "DELETE FROM locacao WHERE id_locacao = ?";

            try (Connection conn = ConnectionFactory.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, id);

                // executa a exclusão
                int linhasAfetadas = stmt.executeUpdate();
                System.out.println("Tentativa de deletar Locação ID " + id + ". Linhas afetadas: " + linhasAfetadas);

            } catch (SQLIntegrityConstraintViolationException e) {
                // A exceção de integridade é relançada para ser tratada pela API (status 409)
                throw e;
            }

            catch (SQLException e) {
                System.err.println("Erro ao deletar aluguel ID: " + id + ". Detalhes: " + e.getMessage());
                e.printStackTrace();
                // Lança a exceção para que o chamador (a API) possa tratá-la (status 500 ou
                // 409)
                throw e;
            }
        }

        // METODO DE PROIBIÇÃO DE UM USUÁRIO ALUGAR DUAS QUADRAS

        public boolean clienteJaPossuiLocacao(Long idCliente) {

            String sql = "SELECT COUNT(*) FROM locacao WHERE Cliente_idCliente = ?";

            try (Connection conn = ConnectionFactory.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, idCliente);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        }



        //Checar se a data da locacao foi alterada para que haja aplicação de multa
        public boolean dataFoiAlterada(Long idLocacao, Date novaData) {

        String sql = "SELECT datalocacao FROM locacao WHERE id_locacao = ?";

        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idLocacao);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Date dataAtual = rs.getDate("datalocacao");
                return !dataAtual.equals(novaData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    //Busca por preco atual no banco, usado para atualizar o valor da locacao quando tiver mudança de data no banco

    public double buscarPrecoAtual(Long id_locacao){
        String sql = "SELECT preco from locacao WHERE id_locacao = ?";

        try(Connection conn = ConnectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

                stmt.setLong(1,id_locacao);
                ResultSet rs = stmt.executeQuery();

                if(rs.next()){
                    return rs.getDouble("preco");
                }


                }catch (SQLException e) {
        e.printStackTrace();
    }

    return 0;
            } 

    }
    
