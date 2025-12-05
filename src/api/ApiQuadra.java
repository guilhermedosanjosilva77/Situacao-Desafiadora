package api;

import static spark.Spark.*;

import java.lang.ref.Cleaner.Cleanable;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Filter;
import dao.AluguelDAO;
import dao.ClienteDAO;
import model.Quadra;
import model.Cliente;

import com.google.gson.Gson;

public class ApiQuadra {

    // instancia do DAO e o GSON
    private static final ClienteDAO dao = new ClienteDAO();
    private static final AluguelDAO AluguelDAO = new AluguelDAO();
    private static final Gson gson = new Gson();

    // constante para garantir que todas as respostas sejam JSON
    private static final String APPLICATION_JSON = "application/json";

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

                    Cliente cliente = dao.buscarPorId(id); // Usa o Long ID

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
        put("/cliente:id", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    Long id = Long.parseLong(request.params(":id")); // Usa Long

                    if (dao.buscarPorId(id) == null) {
                        response.status(404);
                        return "{\"mensagem\": \"Cliente não encontrado para atualização.\"}";
                    }

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
            public Object handle(Request request, Response response) {
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





        //SESSÃO DOS ALUGUEIS

        // get categorias
        get("/categorias", (request, response) -> gson.toJson(categoriaDAO.buscarTodos()));

        // GET /categorias/:id - Buscar por ID
        get("/categorias/:id", (Request request, Response response) -> {
            try {
                Long id = Long.parseLong(request.params(":id"));

                Categoria categoria = categoriaDAO.buscarPorId(id);

                if (categoria != null) {
                    return gson.toJson(categoria);
                } else {
                    response.status(404);
                    return "{\"mensagem\": \"categoria com ID " + id + " não encontrado\"}";
                }
            } catch (NumberFormatException e) {
                response.status(400);
                return "{\"mensagem\": \"Formato de ID inválido.\"}";
            }
        });

        // POST /categorias - Criar nova categoria
        post("/categorias", (request, response) -> {
            try {
                Categoria novaCategoria = gson.fromJson(request.body(), Categoria.class);
                categoriaDAO.inserir(novaCategoria);

                response.status(201); // Created
                return gson.toJson(novaCategoria);
            } catch (Exception e) {
                response.status(500);
                System.err.println("Erro ao processar requisição POST: " + e.getMessage());
                e.printStackTrace();
                return "{\"mensagem\": \"Erro ao criar categoria.\"}";
            }
        });

        // PUT /categorias/:id - Atualizar produto existente
        put("/categorias/:id", (request, response) -> {
            try {
                Long id = Long.parseLong(request.params(":id")); // Usa Long

                if (categoriaDAO.buscarPorId(id) == null) {
                    response.status(404);
                    return "{\"mensagem\": \"Categoria não encontrada para atualização.\"}";
                }

                Categoria categoriaParaAtualizar = gson.fromJson(request.body(), Categoria.class);
                categoriaParaAtualizar.setId(id); // garante que o ID da URL seja usado

                categoriaDAO.atualizar(categoriaParaAtualizar);

                response.status(200); // OK
                return gson.toJson(categoriaParaAtualizar);

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
        delete("/categorias/:id", (request, response) -> {
            try {
                Long id = Long.parseLong(request.params(":id")); // Usa Long

                if (categoriaDAO.buscarPorId(id) == null) {
                    response.status(404);
                    return "{\"mensagem\": \"Categoria não encontrada para exclusão.\"}";
                }

                categoriaDAO.deletar(id);

                response.status(204);
                return "";

            } catch (NumberFormatException e) {
                response.status(400);
                return "{\"mensagem\": \"Formato de ID inválido.\"}";
            } catch (Exception e) {
                //Pega a causa da exceção, que no caso é violação da chave estrangeira
                if(e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException) {
                    response.status(409);
                    return "{\"mensagem\": \"Não é possível excluir uma categoria usada por mais produtos.\"}";
                }
            }

            response.status(500);
            return "{\"mensagem\": \"Erro ao deletar categoria.\"}";
        });

        System.out.println("API de Produtos iniciada na porta 4567. Acesse: http://localhost:4567/produtos");
    }
}