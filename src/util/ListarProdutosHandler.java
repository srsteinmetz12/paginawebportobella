package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import connection.ConnectionDB;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;

public class ListarProdutosHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"erro\":\"Método não permitido. Use GET.\"}");
            return;
        }

        try {
            JsonArray produtos = listarProdutosDisponiveis();
            sendResponse(exchange, 200, gson.toJson(produtos));
        } catch (ClassNotFoundException | SQLException e) {
            JsonObject erro = new JsonObject();
            erro.addProperty("erro", "Erro ao buscar produtos: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(erro));
        }
    }

    private JsonArray listarProdutosDisponiveis() throws ClassNotFoundException, SQLException {
        JsonArray lista = new JsonArray();

        String sql = "SELECT codpeca, nome, valor, foto, descricao, categoria, status " +
                     "FROM estoque WHERE status = 'DISPONIVEL' ORDER BY nome ASC";

        try (Connection con = ConnectionDB.getConnectionCloud();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                JsonObject produto = new JsonObject();
                produto.addProperty("id", rs.getString("codpeca"));
                produto.addProperty("nome", rs.getString("nome"));
                produto.addProperty("preco", rs.getDouble("valor"));
                produto.addProperty("foto", rs.getString("foto") != null ? rs.getString("foto") : "default.jpg");
                produto.addProperty("descricao", rs.getString("descricao") != null ? rs.getString("descricao") : "");
                produto.addProperty("categoria", rs.getString("categoria") != null ? rs.getString("categoria") : "Geral");
                produto.addProperty("status", rs.getString("status"));
                lista.add(produto);
            }
        }

        return lista;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}