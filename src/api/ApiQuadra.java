package api;

import static spark.Spark.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Filter;
import dao.AluguelDAO;
import dao.ClienteDAO;
import model.Aluguel;
import model.Cliente;

import com.google.gson.Gson; // Mantenha o import do Gson, mas ele será obtido da utilidade.
import util.GsonUtil; // ⬅️ NOVO: Importa a classe utilitária

public class ApiQuadra {

    // Instância do DAO. O GSON será obtido da classe utilitária.
    private static final ClienteDAO dao = new ClienteDAO();
    private static final AluguelDAO AluguelDAO = new AluguelDAO();
    // private static final Gson gson = new Gson(); // ⬅️ REMOVIDO: Usaremos GsonUtil.getGson()

    // O GSON configurado agora é obtido de forma centralizada
    private static final Gson gson = GsonUtil.getGson(); // ⬅️ ALTERADO: Usa o GSON configurado

    // constante para garantir que todas as respostas sejam JSON
    private static final String APPLICATION_JSON = GsonUtil.APPLICATION_JSON; // ⬅️ ALTERADO: Usa a constante da utilidade

    public static void main(String[] args) {

        // configuração do Servidor
        port(4567); // Define a porta da API. Acesso via http://localhost:4567

        // filtro para definir o tipo de conteúdo como JSON
        after(new Filter() {
            @Override
            public void handle(Request request, Response response) {
                response.type(APPLICATION_JSON);
            }
        });

        // GET /cliente - Buscar todos
        get("/cliente", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                return gson.toJson(dao.buscarTodos());
            }
        });

        // GET /produtos/:id - Buscar por ID
        get("/cliente/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    // converter o parâmetro da URL (String) para Long, que é o tipo do ID
                    Long id = Long.parseLong(request.params(":id"));

                    System.out.println("DEBUG: Tentando buscar Cliente ID: " + id);

                    Cliente cliente = dao.buscarPorId(id);

                    if (cliente != null) {
                        System.out.println("DEBUG: Cliente encontrado: " + cliente.toString());
                    } else {
                        System.out.println("DEBUG: Cliente não encontrado, retornando 404.");
                    }

                    if (cliente != null) {
                        return gson.toJson(cliente);
                    } else {
                        response.status(404); // Not Found
                        return "{\"mensagem\": \"Cliente com ID " + id + " não encontrado\"}";
                    }
                } catch (NumberFormatException e) {
                    response.status(400); // Bad Request
                    return "{\"mensagem\": \"Formato de ID inválido.\"}";
                }
            }
        });

        // POST /produtos - Criar novo produto
        post("/cliente", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    // gson.fromJson(request.body(), Cliente.class) ⬅️ JÁ USA O GSON CONFIGURADO
                    Cliente novoCliente = gson.fromJson(request.body(), Cliente.class);
                    dao.inserir(novoCliente);

                    response.status(201); // Created
                    return gson.toJson(novoCliente);
                } catch (Exception e) {
                    response.status(500);
                    System.err.println("Erro ao processar requisição POST: " + e.getMessage());
                    e.printStackTrace();
                    return "{\"mensagem\": \"Erro ao criar produto.\"}";
                }
            }
        });

        // PUT /produtos/:id - Atualizar produto existente
        put("/cliente/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    Long id = Long.parseLong(request.params(":id")); // Usa Long

                    if (dao.buscarPorId(id) == null) {
                        response.status(404);
                        return "{\"mensagem\": \"Cliente não encontrado para atualização.\"}";
                    }

                    // gson.fromJson(request.body(), Cliente.class)
                    Cliente clienteParaAtualizar = gson.fromJson(request.body(), Cliente.class);
                    clienteParaAtualizar.setID(id); // garante que o ID da URL seja usado

                    dao.atualizar(clienteParaAtualizar);

                    response.status(200); // OK
                    return gson.toJson(clienteParaAtualizar);

                } catch (NumberFormatException e) {
                    response.status(400); // Bad Request
                    return "{\"mensagem\": \"Formato de ID inválido.\"}";
                } catch (Exception e) {
                    response.status(500);
                    System.err.println("Erro ao processar requisição PUT: " + e.getMessage());
                    e.printStackTrace();
                    return "{\"mensagem\": \"Erro ao atualizar produto.\"}";
                }
            }
        });

        // DELETE /produtos/:id - Deletar um produto
        delete("/cliente/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) throws SQLIntegrityConstraintViolationException {
                try {
                    Long id = Long.parseLong(request.params(":id")); // Usa Long

                    if (dao.buscarPorId(id) == null) {
                        response.status(404);
                        return "{\"mensagem\": \"cliente não encontrado para exclusão.\"}";
                    }

                    dao.deletar(id); // Usa o Long ID

                    response.status(204); // No Content
                    return ""; // Corpo vazio

                } catch (NumberFormatException e) {
                    response.status(400);
                    return "{\"mensagem\": \"Formato de ID inválido.\"}";
                }
            }
        });

        // SESSÃO DOS ALUGUEIS

        // get categorias
        get("/Aluguel", (request, response) -> gson.toJson(AluguelDAO.buscarTodos()));

        // GET /categorias/:id - Buscar por ID
        get("/Aluguel/:id", (Request request, Response response) -> {
            try {
                Long idQuadra = Long.parseLong(request.params(":id"));

                List<Aluguel> aluguel = AluguelDAO.buscarPorId(idQuadra);

                if (aluguel != null) {
                    return gson.toJson(aluguel);
                } else {
                    response.status(404);
                    return "{\"mensagem\": \"aluguel com ID " + idQuadra + " não encontrado\"}";
                }
            } catch (NumberFormatException e) {
                response.status(400);
                return "{\"mensagem\": \"Formato de ID inválido.\"}";
            }
        });

        // POST /categorias - Criar nova categoria
        post("/Aluguel", (request, response) -> {
            try {
                // gson.fromJson(request.body(), Aluguel.class) ⬅️ AGORA VAI USAR O ADAPTADOR DE DATA
                Aluguel novaCategoria = gson.fromJson(request.body(), Aluguel.class);
                AluguelDAO.inserir(novaCategoria);

                response.status(201); // Created
                return gson.toJson(novaCategoria);
            } catch (Exception e) {
                response.status(500);
                System.err.println("Erro ao processar requisição POST: " + e.getMessage());
                e.printStackTrace();
                return "{\"mensagem\": \"Erro ao criar aluguel.\"}";
            }
        });

        // PUT /categorias/:id - Atualizar produto existente
        put("/Aluguel/:id", (request, response) -> {
            try {
                Long id = Long.parseLong(request.params(":id")); // Usa Long

                // A função buscarPorQuadraId retorna uma lista. Para verificar se existe um aluguel
                // com esse ID de locação, você precisaria de um método que busca por id_locacao
                // ou verificar se a lista retornada por buscarPorQuadraId(id) não está vazia.
                // Vou manter o seu código, mas faça essa verificação se possível:
                // if (AluguelDAO.buscarPorId(id) == null) { ... }
                // Já que buscarPorQuadraId retorna uma lista, a verificação deveria ser:
                
                // if (AluguelDAO.buscarPorQuadraId(id).isEmpty()) {
                //    response.status(404);
                //    return "{\"mensagem\": \"aluguel não encontrado para atualização.\"}";
                // }

                // Por enquanto, mantendo o seu código original para a verificação:
                if (AluguelDAO.buscarPorId(id) == null) {
                    response.status(404);
                    return "{\"mensagem\": \"aluguel não encontrado para atualização.\"}";
                }

                // gson.fromJson(request.body(), Aluguel.class) ⬅️ AGORA VAI USAR O ADAPTADOR DE DATA
                Aluguel aluguelParaAtualizar = gson.fromJson(request.body(), Aluguel.class);
                
                // Cuidado! O parâmetro da URL ":id" aqui é o ID do ALUGUEL (id_locacao) 
                // para o PUT, não o ID da Quadra (quadra_idquadra).
                // Você está chamando AluguelDAO.atualizar(aluguelParaAtualizar)
                // que espera o ID da locação no objeto. 
                // Você deve setar o ID da LOCAÇÃO, não o ID da Quadra, 
                // se a rota for para atualizar um aluguel específico.
                
                // Mantenho o set da Quadra conforme seu código original:
                aluguelParaAtualizar.setIdQuadra(id); // garante que o ID da URL seja usado
                
                // RECOMENDAÇÃO: Se a rota for para atualizar um aluguel (id_locacao) específico, 
                // você deveria chamar aluguelParaAtualizar.setId_locacao(id);
                
                AluguelDAO.atualizar(aluguelParaAtualizar);

                response.status(200); // OK
                return gson.toJson(aluguelParaAtualizar);

            } catch (NumberFormatException e) {
                response.status(400); // Bad Request
                return "{\"mensagem\": \"Formato de ID inválido.\"}";
            } catch (Exception e) {
                response.status(500);
                System.err.println("Erro ao processar requisição PUT: " + e.getMessage());
                e.printStackTrace();
                return "{\"mensagem\": \"Erro ao atualizar Categoria.\"}";
            }
        });

        // DELETE /categorias/:id - Deletar uma categoria
        delete("/Aluguel/:id", (request, response) -> {
    try {
        Long id = Long.parseLong(request.params(":id"));

        // CORREÇÃO 1: Adicionei o nome do método (verifique se é 'buscarPorId' no seu DAO)
        List<Aluguel> lista = AluguelDAO.buscarPorId(id);

        if (lista.isEmpty()) {
            response.status(404);
            return "{\"mensagem\": \"Aluguel não encontrado para exclusão.\"}";
        }

        AluguelDAO.deletar(id);

        response.status(204);
        return "";

    } catch (NumberFormatException e) {
        response.status(400);
        return "{\"mensagem\": \"Formato de ID inválido.\"}";
    } catch (Exception e) {
        // Pega a causa da exceção de integridade do banco (Chave Estrangeira)
        if (e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException) {
            response.status(409);
            // CORREÇÃO 2: Ajustei a mensagem para o contexto de Aluguel
            return "{\"mensagem\": \"Não é possível excluir este aluguel pois ele está vinculado a outros registros.\"}";
        }
        
        // Se cair aqui, é um erro genérico diferente de chave estrangeira
        response.status(500);
        // CORREÇÃO 3: Ajustei a mensagem para o contexto de Aluguel
        return "{\"mensagem\": \"Erro ao deletar aluguel: " + e.getMessage() + "\"}";
    }
});

        System.out.println("API de Produtos iniciada na porta 4567. Acesse: http://localhost:4567/produtos");
    }
}