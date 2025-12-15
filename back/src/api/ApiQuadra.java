package api;

import static spark.Spark.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import spark.Request;
import spark.Response;
import spark.Route;

import dao.AluguelDAO;
import dao.ClienteDAO;
import model.Aluguel;
import model.Cliente;

import com.google.gson.Gson;
import util.GsonUtil;


public class ApiQuadra {

    // Instância do DAO.
    private static final ClienteDAO dao = new ClienteDAO();
    private static final AluguelDAO AluguelDAO = new AluguelDAO();
    
    // O GSON configurado é obtido de forma centralizada
    private static final Gson gson = GsonUtil.getGson();
    
    // constante para garantir que todas as respostas sejam JSON
    private static final String APPLICATION_JSON = GsonUtil.APPLICATION_JSON;

    // ======================================//
    // HABILITAÇÃO DO CORS 
    // (Esta função NÃO é alterada, apenas a sua chamada é movida)
    // ======================================//
    private static void configureCORS() {
        
        // Permite requisições pre-flight (OPTIONS)
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        // Adiciona os headers CORS em todas as respostas
        before((request, response) -> {
            // Especificamos a origem do React
            response.header("Access-Control-Allow-Origin", "http://localhost:3000"); 
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            // Garante que todas as respostas do Spark sejam JSON
            response.type(APPLICATION_JSON); 
        });
    }
    
    public static void main(String[] args) {

        // ===============================================
        // 1. CONFIGURAÇÃO DE PORTA (DEVE VIR PRIMEIRO!)
        // ===============================================
        port(4567); // Resolve o erro 'IllegalStateException'

        // ===============================================
        // 2. CONFIGURAÇÃO DO CORS (DEVE VIR ANTES DAS ROTAS)
        // ===============================================
        configureCORS(); 
        
        // ===============================================
        // 3. DEFINIÇÃO DAS ROTAS (A LÓGICA DE CADA ROTA É MANTIDA)
        // ===============================================

        // GET /cliente - Buscar todos
        get("/cliente", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                return gson.toJson(dao.buscarTodos());
            }
        });

        // GET /cliente/:id - Buscar por ID
        get("/cliente/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    Long id = Long.parseLong(request.params(":id"));
                    Cliente cliente = dao.buscarPorId(id);

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

        // POST /cliente - Criar novo cliente
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
                    return "{\"mensagem\": \"Erro ao criar cliente.\"}";
                }
            }
        });

        // PUT /cliente/:id - Atualizar cliente existente
        put("/cliente/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    Long id = Long.parseLong(request.params(":id"));

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
                    return "{\"mensagem\": \"Erro ao atualizar cliente.\"}";
                }
            }
        });

        // DELETE /cliente/:id - Deletar um produto
        delete("/cliente/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) throws SQLIntegrityConstraintViolationException {
                try {
                    Long id = Long.parseLong(request.params(":id"));

                    if (dao.buscarPorId(id) == null) {
                        response.status(404);
                        return "{\"mensagem\": \"cliente não encontrado para exclusão.\"}";
                    }

                    dao.deletar(id); 

                    response.status(204); // No Content
                    return ""; // Corpo vazio

                } catch (NumberFormatException e) {
                    response.status(400);
                    return "{\"mensagem\": \"Formato de ID inválido.\"}";
                } catch (SQLIntegrityConstraintViolationException e) {
                    // Adicionada para o erro 409
                    response.status(409);
                    return "{\"mensagem\": \"Não é possível excluir este cliente pois ele possui locações vinculadas.\"}";
                } catch (Exception e) {
                    response.status(500);
                    return "{\"mensagem\": \"Erro ao deletar cliente.\"}";
                }
            }
        });

        // SESSÃO DOS ALUGUEIS (Rotas /Aluguel com 'A' maiúsculo)

        // GET /Aluguel - Buscar todos
        get("/Aluguel", (request, response) -> gson.toJson(AluguelDAO.buscarTodos()));

        // GET /Aluguel/:id - Buscar por ID (presume-se que o ID é o id_locacao)
        get("/Aluguel/:id", (Request request, Response response) -> {
            try {
                Long idLocacao = Long.parseLong(request.params(":id"));

                List<Aluguel> aluguel = AluguelDAO.buscarPorId(idLocacao);

                if (!aluguel.isEmpty()) {
                    return gson.toJson(aluguel.get(0)); 
                } else {
                    response.status(404);
                    return "{\"mensagem\": \"Aluguel com ID " + idLocacao + " não encontrado\"}";
                }
            } catch (NumberFormatException e) {
                response.status(400);
                return "{\"mensagem\": \"Formato de ID inválido.\"}";
            }
        });

        // POST /Aluguel - Criar nova categoria
        post("/Aluguel", (request, response) -> {
            try {
                Aluguel novoAluguel = gson.fromJson(request.body(), Aluguel.class);
                AluguelDAO.inserir(novoAluguel);

                response.status(201); // Created
                return gson.toJson(novoAluguel);
            } catch (RuntimeException e) {
                 // Captura o erro customizado do DAO (ex: cliente já possui locação)
                response.status(400); 
                return "{\"mensagem\": \"Erro ao criar aluguel: " + e.getMessage() + "\"}";
            } catch (Exception e) {
                response.status(500);
                System.err.println("Erro ao processar requisição POST: " + e.getMessage());
                e.printStackTrace();
                return "{\"mensagem\": \"Erro interno ao criar aluguel.\"}";
            }
        });

        // PUT /Aluguel/:id - Atualizar produto existente
        put("/Aluguel/:id", (request, response) -> {
            try {
                Long id = Long.parseLong(request.params(":id")); 
                
                if (AluguelDAO.buscarPorId(id).isEmpty()) {
                    response.status(404);
                    return "{\"mensagem\": \"Aluguel não encontrado para atualização.\"}";
                }

                Aluguel aluguelParaAtualizar = gson.fromJson(request.body(), Aluguel.class);
                aluguelParaAtualizar.setId_locacao(id); // Usa o ID da URL

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
                return "{\"mensagem\": \"Erro ao atualizar Aluguel.\"}";
            }
        });

        // DELETE /Aluguel/:id - Deletar uma categoria
        delete("/Aluguel/:id", (request, response) -> {
            try {
                Long id = Long.parseLong(request.params(":id"));
                
                if (AluguelDAO.buscarPorId(id).isEmpty()) {
                    response.status(404);
                    return "{\"mensagem\": \"Aluguel não encontrado para exclusão.\"}";
                }

                AluguelDAO.deletar(id);

                response.status(204);
                return "";

            } catch (NumberFormatException e) {
                response.status(400);
                return "{\"mensagem\": \"Formato de ID inválido.\"}";
            } catch (SQLIntegrityConstraintViolationException e) {
                // Adicionada para o erro 409
                response.status(409);
                return "{\"mensagem\": \"Não é possível excluir este aluguel pois ele está vinculado a outros registros.\"}";
            } catch (Exception e) {
                response.status(500);
                return "{\"mensagem\": \"Erro ao deletar aluguel: " + e.getMessage() + "\"}";
            }
        });

        System.out.println("API de Quadras iniciada na porta 4567.");
    }
}