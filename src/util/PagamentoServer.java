package util;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import connection.ConnectionDB;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import paginaweb.GerarSiteEstoque;

public class PagamentoServer {

    private static final Gson gson = new Gson();
    private static HttpServer server;

    // ==========================================
    // CONFIGURAÇÕES DO PIX
    // ==========================================
    private static final String CHAVE_PIX = "portobella.brecho@gmail.com";
    private static final String NOME_RECEBEDOR = "VANDERLEIA VIEI";
    private static final String CIDADE = "PORTO ALEGRE";

    // ==========================================
    // TOKEN DO MERCADO PAGO
    // ==========================================
    private static final String TOKEN_MP = "APP_USR-5504079628127234-061707-4f72faca8cd75c397d89abc34651960f-3480421128";

    // ==========================================
    // CLASSE NOTIFICACAO (AUXILIAR)
    // ==========================================
    private static class Notificacao {
        int id;
        String pedidoId;
        String codPeca;
        String cliente;
        String telefone;
        double valor;
        String meioPagamento;
        boolean retirarLoja;
        String endereco;
        String dataCriacao;
        String itens;

        Notificacao(int id, String pedidoId, String codPeca, String cliente, String telefone,
                    double valor, String meioPagamento, boolean retirarLoja,
                    String endereco, String dataCriacao, String itens) {
            this.id = id;
            this.pedidoId = pedidoId;
            this.codPeca = codPeca;
            this.cliente = cliente;
            this.telefone = telefone;
            this.valor = valor;
            this.meioPagamento = meioPagamento;
            this.retirarLoja = retirarLoja;
            this.endereco = endereco;
            this.dataCriacao = dataCriacao;
            this.itens = itens;
        }
    }

    // ==========================================
    // CORS HEADERS
    // ==========================================
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    // ==========================================
    // INICIAR SERVIDOR
    // ==========================================
    public static void iniciar() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        server.createContext("/api/pagamentos/criar", new CriarPagamentoHandler());
        server.createContext("/api/pagamentos/status", new StatusPagamentoHandler());
        server.createContext("/api/webhook", new WebhookHandler());
        server.createContext("/api/pagamentos/finalizar", new FinalizarCompraHandler());
        server.createContext("/api/frete/calcular", new CalcularFreteHandler());
        server.createContext("/api/pagamentos/notificar", new NotificarSistemaHandler());
        server.createContext("/api/pagamentos/consultar", new ConsultarNotificacoesHandler());
        server.createContext("/api/pagamentos/reservar", new ReservarItemHandler());
        server.createContext("/api/pagamentos/liberar-reserva", new LiberarReservaHandler());
        server.createContext("/api/pagamentos/responder", new ResponderNotificacaoHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ Servidor de pagamentos rodando em http://localhost:8080");
        System.out.println("   🔥 PIX: GERADO SEM MERCADO PAGO (chave: " + CHAVE_PIX + ")");
        System.out.println("   💳 MERCADO PAGO: SOMENTE PARA LINK DE PAGAMENTO");
        System.out.println("   📦 Frete: Cálculo por CEP (ViaCEP + Fallback)");
        System.out.println("   🔔 Notificações: /api/pagamentos/notificar");
        System.out.println("   🔍 Consultar: /api/pagamentos/consultar");
        System.out.println("   🔒 Reservar: /api/pagamentos/reservar");
        System.out.println("   🔓 Liberar: /api/pagamentos/liberar-reserva");
    }

    public static void parar() {
        if (server != null) {
            server.stop(0);
        }
    }

    // ==========================================
    // HANDLER: FINALIZAR COMPRA (CARRINHO)
    // ==========================================
    static class FinalizarCompraHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                return;
            }

            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);

                System.out.println("📥 Body recebido: " + body);

                JsonObject json = gson.fromJson(body, JsonObject.class);
                String meio = json.get("meio").getAsString();
                double subtotal = json.get("subtotal").getAsDouble();
                double frete = json.get("frete").getAsDouble();
                double total = json.get("total").getAsDouble();
                String cep = json.get("cep").getAsString();
                String endereco = json.get("endereco").getAsString();

                com.google.gson.JsonArray itensArray = json.getAsJsonArray("itens");
                String codPeca = itensArray.get(0).getAsJsonObject().get("id").getAsString();
                String nomeProduto = itensArray.get(0).getAsJsonObject().get("nome").getAsString();

                System.out.println("📝 Finalizando compra:");
                System.out.println("   Meio: " + meio);
                System.out.println("   Subtotal: R$ " + subtotal);
                System.out.println("   Frete: R$ " + frete);
                System.out.println("   Total: R$ " + total);
                System.out.println("   CEP: " + cep);
                System.out.println("   Endereço: " + endereco);
                System.out.println("   Produto: " + nomeProduto);

                registrarVendaCarrinho(codPeca, subtotal, frete, total, endereco, cep, meio);

                Map<String, Object> response = new HashMap<>();

                if ("pix".equalsIgnoreCase(meio)) {
                    String payloadPix = gerarPayloadPix(total, "Pedido PORTOBERLLA");
                    response.put("success", true);
                    response.put("meio", "PIX");
                    response.put("payload", payloadPix);
                    response.put("total", total);
                    response.put("pedidoId", System.currentTimeMillis());
                    System.out.println("   ✅ Pix gerado com sucesso!");
                } else {
                    String link = criarLinkMercadoPago(codPeca, "Pedido PORTOBELLA", total);
                    if (link != null && !link.isEmpty()) {
                        response.put("success", true);
                        response.put("meio", "CREDITO");
                        response.put("paymentUrl", link);
                        System.out.println("   ✅ Link MP gerado: " + link);
                    } else {
                        response.put("success", false);
                        response.put("error", "Erro ao gerar link do Mercado Pago");
                    }
                }

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        private void registrarVendaCarrinho(String codPeca, double subtotal, double frete, double total,
                                            String endereco, String cep, String meio) {
            Connection con = null;
            PreparedStatement stmt = null;

            try {
                con = ConnectionDB.getConnectionCloud();
                String sql = "INSERT INTO vendas (datavenda, codpeca, valor_subtotal, valor_frete, valor_total, endereco_entrega, cep, meio_pagamento, status_pagamento) VALUES (CURDATE(), ?, ?, ?, ?, ?, ?, ?, 'PENDENTE')";
                stmt = con.prepareStatement(sql);
                stmt.setString(1, codPeca);
                stmt.setDouble(2, subtotal);
                stmt.setDouble(3, frete);
                stmt.setDouble(4, total);
                stmt.setString(5, endereco);
                stmt.setString(6, cep);
                stmt.setString(7, meio);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("   ✅ Venda registrada no banco!");
                }

            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("   ❌ Erro ao registrar venda: " + e.getMessage());
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (con != null) con.close(); } catch (SQLException e) {}
            }
        }
    }

    // ==========================================
    // HANDLER: CALCULAR FRETE POR CEP
    // ==========================================
    static class CalcularFreteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQueryParams(query);
                String cep = params.get("cep");

                if (cep == null || cep.isEmpty()) {
                    sendResponse(exchange, 400, "{\"error\":\"CEP não informado\"}");
                    return;
                }

                cep = cep.replaceAll("\\D", "");

                if (cep.length() != 8) {
                    sendResponse(exchange, 400, "{\"error\":\"CEP inválido. Deve ter 8 dígitos.\"}");
                    return;
                }

                System.out.println("📦 Calculando frete para CEP: " + cep);

                String uf = buscarUFViaCEP(cep);
                double valorFrete = calcularFretePorUF(uf);
                String prazo = estimarPrazoPorUF(uf);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("cep", cep);
                response.put("uf", uf);
                response.put("frete", valorFrete);
                response.put("prazo", prazo);
                response.put("cidade", buscarCidadeViaCEP(cep));

                System.out.println("   UF: " + uf);
                System.out.println("   Frete: R$ " + valorFrete);
                System.out.println("   Prazo: " + prazo);

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        private String buscarUFViaCEP(String cep) {
            try {
                String url = "https://viacep.com.br/ws/" + cep + "/json/";
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String responseBody = lerResposta(conn);
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                    if (json.has("uf") && !json.get("uf").isJsonNull()) {
                        return json.get("uf").getAsString();
                    }
                }
            } catch (JsonSyntaxException | IOException e) {
                System.out.println("   ⚠️ ViaCEP indisponível: " + e.getMessage());
            }
            return estimarUFporCEP(cep);
        }

        private String buscarCidadeViaCEP(String cep) {
            try {
                String url = "https://viacep.com.br/ws/" + cep + "/json/";
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String responseBody = lerResposta(conn);
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                    if (json.has("localidade") && !json.get("localidade").isJsonNull()) {
                        return json.get("localidade").getAsString();
                    }
                }
            } catch (JsonSyntaxException | IOException e) {
                // Ignora erro
            }
            return "Não informado";
        }

        private String estimarUFporCEP(String cep) {
            String prefixo = cep.substring(0, 1);
            switch (prefixo) {
                case "0": return "SP";
                case "1": return "SP";
                case "2": return "RJ";
                case "3": return "MG";
                case "4": return "BA";
                case "5": return "PE";
                case "6": return "CE";
                case "7": return "DF";
                case "8": return "PR";
                case "9": return "RS";
                default: return "SP";
            }
        }

        private double calcularFretePorUF(String uf) {
            if (uf == null || uf.isEmpty()) return 35.90;
            switch (uf.toUpperCase()) {
                case "SP": case "RJ": case "MG": case "ES": return 25.90;
                case "PR": case "SC": case "RS": return 35.90;
                case "DF": case "GO": case "MT": case "MS": return 40.90;
                case "BA": case "SE": case "AL": case "PE": case "PB": case "RN": case "CE": case "PI": case "MA": return 45.90;
                case "PA": case "AM": case "AC": case "RR": case "RO": case "AP": case "TO": return 55.90;
                default: return 35.90;
            }
        }

        private String estimarPrazoPorUF(String uf) {
            if (uf == null || uf.isEmpty()) return "5 a 7 dias úteis";
            switch (uf.toUpperCase()) {
                case "SP": case "RJ": return "2 a 4 dias úteis";
                case "MG": case "ES": return "3 a 5 dias úteis";
                case "PR": case "SC": return "4 a 6 dias úteis";
                case "RS": return "5 a 7 dias úteis";
                case "DF": case "GO": return "5 a 7 dias úteis";
                case "BA": case "SE": return "5 a 8 dias úteis";
                case "PE": case "PB": case "RN": case "CE": return "6 a 9 dias úteis";
                case "AM": case "PA": case "AC": case "RR": case "RO": case "AP": case "TO": return "8 a 12 dias úteis";
                default: return "5 a 7 dias úteis";
            }
        }

        private String lerResposta(java.net.HttpURLConnection conn) throws IOException {
            java.io.InputStream is = conn.getInputStream();
            try (java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A")) {
                return s.hasNext() ? s.next() : "";
            }
        }
    }

    // ==========================================
    // HANDLER: RESERVAR ITEM
    // ==========================================
    static class ReservarItemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);

                JsonObject json = gson.fromJson(body, JsonObject.class);
                String codPeca = json.get("codPeca").getAsString();
                String pedidoId = json.get("pedidoId").getAsString();
                int quantidade = json.has("quantidade") ? json.get("quantidade").getAsInt() : 1;

                boolean reservado = reservarItem(codPeca, pedidoId, quantidade);

                Map<String, Object> response = new HashMap<>();
                response.put("success", reservado);
                response.put("codPeca", codPeca);
                response.put("pedidoId", pedidoId);
                response.put("mensagem", reservado ? "Item reservado com sucesso!" : "Item indisponível!");

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        private boolean reservarItem(String codPeca, String pedidoId, int quantidade) {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            boolean reservado = false;

            try {
                con = ConnectionDB.getConnectionCloud();
                con.setAutoCommit(false);

                String sqlCheck = "SELECT status, quantidade FROM estoque WHERE codpeca = ? AND status = 'DISPONIVEL' FOR UPDATE";
                stmt = con.prepareStatement(sqlCheck);
                stmt.setString(1, codPeca);
                stmt.setQueryTimeout(10);
                rs = stmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ [RESERVA] Item não disponível: " + codPeca);
                    con.rollback();
                    return false;
                }

                int qtdDisponivel = rs.getInt("quantidade");
                if (qtdDisponivel < quantidade) {
                    System.out.println("❌ [RESERVA] Estoque insuficiente: " + codPeca);
                    con.rollback();
                    return false;
                }

                String sqlUpdate = "UPDATE estoque SET status = 'RESERVADO', quantidade = quantidade - ? WHERE codpeca = ?";
                stmt = con.prepareStatement(sqlUpdate);
                stmt.setInt(1, quantidade);
                stmt.setString(2, codPeca);
                stmt.executeUpdate();

                String sqlReserva = "INSERT INTO reservas (cod_peca, pedido_id, quantidade, data_reserva, status) VALUES (?, ?, ?, NOW(), 'RESERVADO')";
                stmt = con.prepareStatement(sqlReserva);
                stmt.setString(1, codPeca);
                stmt.setString(2, pedidoId);
                stmt.setInt(3, quantidade);
                stmt.executeUpdate();

                con.commit();
                reservado = true;
                System.out.println("✅ [RESERVA] Item reservado: " + codPeca + " (Pedido: " + pedidoId + ") → RESERVADO");

            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("❌ [RESERVA] Erro: " + e.getMessage());
                try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
            }

            return reservado;
        }
    }

    // ==========================================
    // HANDLER: LIBERAR RESERVA (REJEIÇÃO)
    // ==========================================
    static class LiberarReservaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);

                JsonObject json = gson.fromJson(body, JsonObject.class);
                String codPeca = json.get("codPeca").getAsString();
                String pedidoId = json.get("pedidoId").getAsString();

                boolean liberado = liberarReserva(codPeca, pedidoId);

                Map<String, Object> response = new HashMap<>();
                response.put("success", liberado);
                response.put("mensagem", liberado ? "Reserva liberada! Item disponível novamente." : "Erro ao liberar reserva!");

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        private boolean liberarReserva(String codPeca, String pedidoId) {
            Connection con = null;
            PreparedStatement stmt = null;

            try {
                con = ConnectionDB.getConnectionCloud();
                con.setAutoCommit(false);

                String sqlCheck = "SELECT quantidade FROM reservas WHERE cod_peca = ? AND pedido_id = ? AND status = 'RESERVADO' FOR UPDATE";
                stmt = con.prepareStatement(sqlCheck);
                stmt.setString(1, codPeca);
                stmt.setString(2, pedidoId);
                stmt.setQueryTimeout(10);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    con.rollback();
                    return false;
                }

                int quantidade = rs.getInt("quantidade");

                String sqlUpdate = "UPDATE estoque SET quantidade = quantidade + ?, status = 'DISPONIVEL' WHERE codpeca = ?";
                stmt = con.prepareStatement(sqlUpdate);
                stmt.setInt(1, quantidade);
                stmt.setString(2, codPeca);
                stmt.executeUpdate();

                String sqlReserva = "UPDATE reservas SET status = 'CANCELADO', data_cancelamento = NOW() WHERE cod_peca = ? AND pedido_id = ?";
                stmt = con.prepareStatement(sqlReserva);
                stmt.setString(1, codPeca);
                stmt.setString(2, pedidoId);
                stmt.executeUpdate();

                con.commit();
                System.out.println("✅ [RESERVA] Reserva liberada: " + codPeca + " (Pedido: " + pedidoId + ") → DISPONIVEL");
                return true;

            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("❌ [RESERVA] Erro ao liberar: " + e.getMessage());
                try { if (con != null) con.rollback(); } catch (SQLException ex) {}
                return false;
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
            }
        }
    }

    // ==========================================
    // HANDLER: NOTIFICAR SISTEMA DESKTOP
    // ==========================================
    static class NotificarSistemaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                return;
            }

            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);

                System.out.println("📥 Notificação recebida: " + body);

                JsonObject json = gson.fromJson(body, JsonObject.class);

                String codPeca = json.get("codPeca").getAsString();
                String nomeCliente = json.get("destinatario").getAsString();
                double valorTotal = json.get("total").getAsDouble();
                String meioPagamento = json.get("meio").getAsString();
                String endereco = json.get("endereco").getAsString();
                boolean retirarLoja = json.has("retirarLoja") && json.get("retirarLoja").getAsBoolean();
                String pedidoId = json.get("pedidoId").getAsString();
                String telefone = json.has("telefone") ? json.get("telefone").getAsString() : "Não informado";
                String itens = json.get("itens").toString();

                System.out.println("📝 Dados da venda:");
                System.out.println("   Cliente: " + nomeCliente);
                System.out.println("   Telefone: " + telefone);
                System.out.println("   Valor: R$ " + valorTotal);
                System.out.println("   Peça: " + codPeca);
                System.out.println("   Retirar na loja: " + (retirarLoja ? "SIM" : "NÃO"));

                salvarNotificacaoNoBanco(codPeca, nomeCliente, valorTotal, meioPagamento,
                        retirarLoja, endereco, pedidoId, telefone, itens);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("pedidoId", pedidoId);
                response.put("mensagem", "Notificação enviada para a loja!");

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        private void salvarNotificacaoNoBanco(String codPeca, String cliente, double valor,
                                              String meioPagamento, boolean retirarLoja,
                                              String endereco, String pedidoId, String telefone,
                                              String itens) {
            Connection con = null;
            PreparedStatement stmt = null;

            try {
                con = ConnectionDB.getConnectionCloud();

                String sql = "INSERT INTO notificacoes_pendentes " +
                        "(pedido_id, cod_peca, cliente, telefone, valor, meio_pagamento, " +
                        "endereco, retirar_loja, itens, data_criacao, status, lida) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'PENDENTE', 0)";

                stmt = con.prepareStatement(sql);
                stmt.setString(1, pedidoId);
                stmt.setString(2, codPeca);
                stmt.setString(3, cliente);
                stmt.setString(4, telefone);
                stmt.setDouble(5, valor);
                stmt.setString(6, meioPagamento);
                stmt.setString(7, endereco);
                stmt.setBoolean(8, retirarLoja);
                stmt.setString(9, itens);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("   ✅ Notificação salva no banco! ID: " + pedidoId);
                }

            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("   ❌ Erro ao salvar notificação: " + e.getMessage());
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (con != null) con.close(); } catch (SQLException e) {}
            }
        }
    }

    // ==========================================
    // HANDLER: CONSULTAR NOTIFICAÇÕES
    // ==========================================
    static class ConsultarNotificacoesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                List<Map<String, Object>> notificacoes = new ArrayList<>();
                Connection con = null;
                PreparedStatement stmt = null;
                ResultSet rs = null;

                try {
                    con = ConnectionDB.getConnectionCloud();

                    String sql = "SELECT id, pedido_id, cod_peca, cliente, telefone, valor, " +
                            "meio_pagamento, endereco, retirar_loja, itens, data_criacao " +
                            "FROM notificacoes_pendentes " +
                            "WHERE status = 'PENDENTE' AND lida = 0 " +
                            "ORDER BY data_criacao ASC";

                    stmt = con.prepareStatement(sql);
                    rs = stmt.executeQuery();

                    while (rs.next()) {
                        Map<String, Object> notif = new HashMap<>();
                        notif.put("id", rs.getInt("id"));
                        notif.put("pedidoId", rs.getString("pedido_id"));
                        notif.put("codPeca", rs.getString("cod_peca"));
                        notif.put("cliente", rs.getString("cliente"));
                        notif.put("telefone", rs.getString("telefone"));
                        notif.put("valor", rs.getDouble("valor"));
                        notif.put("meioPagamento", rs.getString("meio_pagamento"));
                        notif.put("endereco", rs.getString("endereco"));
                        notif.put("retirarLoja", rs.getBoolean("retirar_loja"));
                        notif.put("itens", rs.getString("itens"));
                        notif.put("dataCriacao", rs.getTimestamp("data_criacao").toString());
                        notificacoes.add(notif);
                    }

                } finally {
                    try { if (rs != null) rs.close(); } catch (SQLException e) {}
                    try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                    try { if (con != null) con.close(); } catch (SQLException e) {}
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("notificacoes", notificacoes);
                response.put("total", notificacoes.size());

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (IOException | ClassNotFoundException | SQLException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }
    }

    // ==========================================
    // HANDLER: RESPONDER NOTIFICAÇÃO
    // ==========================================
    static class ResponderNotificacaoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);

                System.out.println("📥 Resposta recebida: " + body);

                JsonObject json = gson.fromJson(body, JsonObject.class);
                int id = json.get("id").getAsInt();
                String pedidoId = json.get("pedidoId").getAsString();
                boolean aprovado = json.get("aprovado").getAsBoolean();

                Notificacao notif = buscarNotificacaoPorId(id);

                if (notif != null) {
                    responderNotificacao(notif, aprovado);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("mensagem", "Resposta registrada com sucesso!");

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        private Notificacao buscarNotificacaoPorId(int id) {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                con = ConnectionDB.getConnectionCloud();

                String sql = "SELECT id, pedido_id, cod_peca, cliente, telefone, valor, " +
                        "meio_pagamento, endereco, retirar_loja, itens, data_criacao " +
                        "FROM notificacoes_pendentes WHERE id = ?";

                stmt = con.prepareStatement(sql);
                stmt.setInt(1, id);
                stmt.setQueryTimeout(10);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return new Notificacao(
                            rs.getInt("id"),
                            rs.getString("pedido_id"),
                            rs.getString("cod_peca"),
                            rs.getString("cliente"),
                            rs.getString("telefone"),
                            rs.getDouble("valor"),
                            rs.getString("meio_pagamento"),
                            rs.getBoolean("retirar_loja"),
                            rs.getString("endereco"),
                            rs.getString("data_criacao"),
                            rs.getString("itens")
                    );
                }

            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("❌ Erro ao buscar notificação: " + e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (con != null) con.close(); } catch (SQLException e) {}
            }

            return null;
        }
    }

    // ==========================================
    // HANDLER: CRIAR PAGAMENTO
    // ==========================================
    static class CriarPagamentoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQueryParams(query);

                String meio = params.get("meio");
                String produtoId = params.get("id");
                double preco = Double.parseDouble(params.getOrDefault("preco", "0"));
                String nome = params.get("nome");
                double frete = Double.parseDouble(params.getOrDefault("frete", "0"));
                double valorTotal = preco + frete;

                System.out.println("📝 Criando pagamento para: " + nome + " - R$ " + valorTotal);
                System.out.println("   Meio: " + meio);

                Map<String, Object> response = new HashMap<>();

                if ("pix".equalsIgnoreCase(meio)) {
                    String payloadPix = gerarPayloadPix(valorTotal, "Pedido PORTOBELLA");
                    response.put("success", true);
                    response.put("meio", "PIX");
                    response.put("payload", payloadPix);
                    response.put("valor", valorTotal);
                    response.put("produto", nome);
                    System.out.println("   ✅ Payload Pix gerado com sucesso!");
                } else if ("mercado_pago".equalsIgnoreCase(meio)) {
                    String linkPagamento = criarLinkMercadoPago(produtoId, nome, valorTotal);
                    if (linkPagamento != null && !linkPagamento.isEmpty()) {
                        response.put("success", true);
                        response.put("meio", "mercado_pago");
                        response.put("paymentUrl", linkPagamento);
                        System.out.println("   ✅ Link gerado: " + linkPagamento);
                    } else {
                        response.put("success", false);
                        response.put("error", "Erro ao gerar link do Mercado Pago");
                    }
                } else {
                    response.put("success", false);
                    response.put("error", "Meio de pagamento inválido: " + meio);
                }

                sendResponse(exchange, 200, gson.toJson(response));
                addCorsHeaders(exchange);
            } catch (IOException | NumberFormatException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }
    }

    // ==========================================
    // HANDLER: STATUS PAGAMENTO
    // ==========================================
    static class StatusPagamentoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            try {
                String path = exchange.getRequestURI().getPath();
                String paymentIdStr = path.substring(path.lastIndexOf("/") + 1);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("paymentId", paymentIdStr);
                response.put("status", "pending");

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }
    }

    // ==========================================
    // HANDLER: WEBHOOK
    // ==========================================
    static class WebhookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);

                System.out.println("📢 Webhook: " + body);
                sendResponse(exchange, 200, "{\"status\":\"ok\"}");

            } catch (IOException e) {
                sendResponse(exchange, 200, "{\"status\":\"ok\"}");
            }
        }
    }

    // ==========================================
    // GERAR PAYLOAD PIX
    // ==========================================
    public static String gerarPayloadPix(double valor, String descricao) {
        try {
            System.out.println("🔧 Gerando payload Pix...");
            System.out.println("💰 Valor: R$ " + valor);

            StringBuilder payload = new StringBuilder();

            payload.append(emvField("00", "01"));
            payload.append(emvField("01", "11"));

            String gui = "br.gov.bcb.pix";
            String chavePix = CHAVE_PIX;

            String sub00 = emvField("00", gui);
            String sub01 = emvField("01", chavePix);
            String valor26 = sub00 + sub01;

            payload.append(emvField("26", valor26));
            payload.append(emvField("52", "0000"));
            payload.append(emvField("53", "986"));

            if (valor > 0) {
                String valorFormatado = String.format("%.2f", valor);
                payload.append(emvField("54", valorFormatado));
            }

            payload.append(emvField("58", "BR"));
            payload.append(emvField("59", NOME_RECEBEDOR));
            payload.append(emvField("60", CIDADE));

            String txid = "***";
            String sub05 = emvField("05", txid);
            payload.append(emvField("62", sub05));

            String payloadSemCRC = payload.toString();
            String crc = calcularCRC16(payloadSemCRC);
            payload.append(emvField("63", crc));

            String payloadFinal = payload.toString();

            System.out.println("✅ Payload gerado com sucesso!");
            System.out.println("📋 Payload: " + payloadFinal);
            System.out.println("📏 Tamanho: " + payloadFinal.length());

            return payloadFinal;

        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar payload: " + e.getMessage());
            return null;
        }
    }

    // ==========================================
    // EMV FIELD
    // ==========================================
    private static String emvField(String id, String valor) {
        if (valor == null) valor = "";
        return id + String.format("%02d", valor.length()) + valor;
    }

    // ==========================================
    // CALCULAR CRC16
    // ==========================================
    private static String calcularCRC16(String input) {
        try {
            int crc = 0xFFFF;
            byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);

            for (byte b : bytes) {
                crc ^= (b & 0xFF) << 8;
                for (int i = 0; i < 8; i++) {
                    if ((crc & 0x8000) != 0) {
                        crc = (crc << 1) ^ 0x1021;
                    } else {
                        crc = crc << 1;
                    }
                }
            }

            return String.format("%04X", crc & 0xFFFF);

        } catch (Exception e) {
            System.err.println("❌ Erro no CRC16: " + e.getMessage());
            return "0000";
        }
    }

    // ==========================================
    // MERCADO PAGO
    // ==========================================
    private static String criarLinkMercadoPago(String codPeca, String titulo, double valor) {
        try {
            String precoFormatado = String.format(java.util.Locale.US, "%.2f", valor);

            String jsonPayload = "{"
                    + "\"items\": [{"
                    + "\"id\": \"" + codPeca + "\","
                    + "\"title\": \"" + titulo + "\","
                    + "\"quantity\": 1,"
                    + "\"currency_id\": \"BRL\","
                    + "\"unit_price\": " + precoFormatado
                    + "}],"
                    + "\"back_urls\": {"
                    + "\"success\": \"https://srsteinmetz12.github.io/sucesso.html\","
                    + "\"failure\": \"https://srsteinmetz12.github.io/falha.html\","
                    + "\"pending\": \"https://srsteinmetz12.github.io/pendente.html\""
                    + "},"
                    + "\"auto_return\": \"approved\""
                    + "}";

            System.out.println("   📤 JSON MP: " + jsonPayload);

            java.net.URL url = new java.net.URL("https://api.mercadopago.com/checkout/preferences");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + TOKEN_MP);
            conn.setDoOutput(true);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes("utf-8"));
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 201) {
                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder res = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    res.append(line.trim());
                }

                String txt = res.toString();
                int inicio = txt.indexOf("\"init_point\":\"") + 14;
                int fim = txt.indexOf("\"", inicio);
                String link = txt.substring(inicio, fim).replace("\\/", "/");

                return link;
            } else {
                System.err.println("   ❌ MP rejeitou. Código: " + responseCode);

                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    StringBuilder erro = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        erro.append(line);
                    }
                    System.err.println("   Erro: " + erro.toString());
                }
            }
        } catch (IOException e) {
            System.err.println("   ❌ Erro MP: " + e.getMessage());
        }

        return null;
    }

    // ==========================================
    // RESPONDER NOTIFICAÇÃO
    // ==========================================
    private static void responderNotificacao(Notificacao notif, boolean aprovado) {
        System.out.println("📤 [RESPONDER] Iniciando: " + notif.pedidoId + " (aprovado=" + aprovado + ")");

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();

            if (con == null || con.isClosed()) {
                Thread.sleep(2000);
                con = ConnectionDB.getConnectionCloud();
                if (con == null) {
                    throw new SQLException("Não foi possível reconectar ao banco");
                }
            }

            String status = aprovado ? "CONFIRMADO" : "REJEITADO";

            String sql = "UPDATE notificacoes_pendentes SET status = ?, lida = 1, data_confirmacao = NOW() WHERE id = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, notif.id);
            stmt.setQueryTimeout(10);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ [RESPONDER] Notificação #" + notif.id + " -> " + status);

                if (aprovado) {
                    moverParaHistorico(con, notif);
                    int idVenda = registrarVenda(con, notif);

                    if (idVenda > 0) {
                        registrarSacola(con, notif, idVenda);
                        registrarEntrega(con, notif, idVenda);
                    }

                    confirmarReserva(notif.codPeca, notif.pedidoId);
                    atualizarSiteAsync();
                    mostrarMensagemTray("✅ Venda CONFIRMADA!", "Pedido: " + notif.pedidoId);

                } else {
                    liberarReserva(notif.codPeca, notif.pedidoId);
                    moverParaHistoricoRejeitado(con, notif);
                    atualizarSiteAsync();
                    mostrarMensagemTray("❌ Venda REJEITADA!", "Pedido: " + notif.pedidoId);
                }
            }

        } catch (ClassNotFoundException | InterruptedException | SQLException e) {
            System.err.println("❌ [RESPONDER] Erro: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }

        System.out.println("📤 [RESPONDER] Finalizado: " + notif.pedidoId);
    }

    // ==========================================
    // CONFIRMAR RESERVA
    // ==========================================
    private static void confirmarReserva(String codPeca, String pedidoId) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            con.setAutoCommit(false);

            String sqlReserva = "UPDATE reservas SET status = 'CONFIRMADO', data_confirmacao = NOW() WHERE cod_peca = ? AND pedido_id = ?";
            stmt = con.prepareStatement(sqlReserva);
            stmt.setString(1, codPeca);
            stmt.setString(2, pedidoId);
            stmt.executeUpdate();

            String sqlEstoque = "UPDATE estoque SET status = 'VENDIDO', datavenda = CURDATE() WHERE codpeca = ? AND status = 'RESERVADO'";
            stmt = con.prepareStatement(sqlEstoque);
            stmt.setString(1, codPeca);
            stmt.executeUpdate();

            con.commit();
            System.out.println("✅ [RESERVA] Venda confirmada: " + codPeca + " → VENDIDO");

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ [RESERVA] Erro ao confirmar: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    // ==========================================
    // LIBERAR RESERVA
    // ==========================================
    private static void liberarReserva(String codPeca, String pedidoId) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            con.setAutoCommit(false);

            String sqlCheck = "SELECT quantidade FROM reservas WHERE cod_peca = ? AND pedido_id = ? AND status = 'RESERVADO' FOR UPDATE";
            stmt = con.prepareStatement(sqlCheck);
            stmt.setString(1, codPeca);
            stmt.setString(2, pedidoId);
            stmt.setQueryTimeout(10);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                con.rollback();
                return;
            }

            int quantidade = rs.getInt("quantidade");

            String sqlUpdate = "UPDATE estoque SET quantidade = quantidade + ?, status = 'DISPONIVEL' WHERE codpeca = ?";
            stmt = con.prepareStatement(sqlUpdate);
            stmt.setInt(1, quantidade);
            stmt.setString(2, codPeca);
            stmt.executeUpdate();

            String sqlReserva = "UPDATE reservas SET status = 'CANCELADO', data_cancelamento = NOW() WHERE cod_peca = ? AND pedido_id = ?";
            stmt = con.prepareStatement(sqlReserva);
            stmt.setString(1, codPeca);
            stmt.setString(2, pedidoId);
            stmt.executeUpdate();

            con.commit();
            System.out.println("✅ [RESERVA] Reserva liberada: " + codPeca + " → DISPONIVEL");

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ [RESERVA] Erro ao liberar: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    // ==========================================
    // MOVER PARA HISTÓRICO (CONFIRMADO)
    // ==========================================
    private static void moverParaHistorico(Connection con, Notificacao notif) {
        PreparedStatement stmt = null;

        try {
            String sql = "INSERT INTO notificacoes_historico " +
                    "(notificacao_id, pedido_id, cod_peca, cliente, telefone, valor, " +
                    "meio_pagamento, endereco, retirar_loja, itens, status, data_criacao, data_confirmacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, notif.id);
            stmt.setString(2, notif.pedidoId);
            stmt.setString(3, notif.codPeca);
            stmt.setString(4, notif.cliente);
            stmt.setString(5, notif.telefone);
            stmt.setDouble(6, notif.valor);
            stmt.setString(7, notif.meioPagamento);
            stmt.setString(8, notif.endereco);
            stmt.setBoolean(9, notif.retirarLoja);
            stmt.setString(10, notif.itens);
            stmt.setString(11, "CONFIRMADO");
            stmt.setTimestamp(12, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(13, new java.sql.Timestamp(System.currentTimeMillis()));

            stmt.setQueryTimeout(10);
            stmt.executeUpdate();

            System.out.println("   ✅ Movido para histórico (CONFIRMADO): " + notif.pedidoId);

        } catch (SQLException e) {
            System.err.println("   ❌ Erro ao mover para histórico: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // MOVER PARA HISTÓRICO (REJEITADO)
    // ==========================================
    private static void moverParaHistoricoRejeitado(Connection con, Notificacao notif) {
        PreparedStatement stmt = null;

        try {
            String sql = "INSERT INTO notificacoes_historico " +
                    "(notificacao_id, pedido_id, cod_peca, cliente, telefone, valor, " +
                    "meio_pagamento, endereco, retirar_loja, itens, status, data_criacao, data_confirmacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, notif.id);
            stmt.setString(2, notif.pedidoId);
            stmt.setString(3, notif.codPeca);
            stmt.setString(4, notif.cliente);
            stmt.setString(5, notif.telefone);
            stmt.setDouble(6, notif.valor);
            stmt.setString(7, notif.meioPagamento);
            stmt.setString(8, notif.endereco);
            stmt.setBoolean(9, notif.retirarLoja);
            stmt.setString(10, notif.itens);
            stmt.setString(11, "REJEITADO");
            stmt.setTimestamp(12, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(13, new java.sql.Timestamp(System.currentTimeMillis()));

            stmt.setQueryTimeout(10);
            stmt.executeUpdate();

            System.out.println("   ✅ Movido para histórico (REJEITADO): " + notif.pedidoId);

        } catch (SQLException e) {
            System.err.println("   ❌ Erro ao mover para histórico (rejeitado): " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // REGISTRAR VENDA
    // ==========================================
    private static int registrarVenda(Connection con, Notificacao notif) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int idVenda = 0;

        try {
            int proximoId = getProximoIdVenda(con);

            String obsVendas = "Pedido: " + notif.pedidoId;
            if (notif.endereco != null && !notif.endereco.isEmpty()) {
                String enderecoResumido = notif.endereco;
                int espacoRestante = 50 - obsVendas.length() - 3;
                if (espacoRestante > 0 && enderecoResumido.length() > espacoRestante) {
                    enderecoResumido = enderecoResumido.substring(0, espacoRestante) + "...";
                } else if (espacoRestante <= 0) {
                    enderecoResumido = "";
                }
                if (!enderecoResumido.isEmpty()) {
                    obsVendas += " | " + enderecoResumido;
                }
            }
            if (obsVendas.length() > 50) {
                obsVendas = obsVendas.substring(0, 47) + "...";
            }

            String valorFormatado = String.format("%.2f", notif.valor).replace(".", ",");

            String sql = "INSERT INTO vendas (id, pedido_id, datavenda, origemvenda, tipopag, valorvenda, codpecas, nomedi, obsvendas, entrega, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, proximoId);
            stmt.setString(2, notif.pedidoId);
            stmt.setDate(3, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.setString(4, "SITE");
            stmt.setString(5, notif.meioPagamento);
            stmt.setString(6, valorFormatado);
            stmt.setString(7, notif.codPeca);
            stmt.setString(8, notif.cliente);
            stmt.setString(9, obsVendas);
            stmt.setString(10, notif.retirarLoja ? "RETIRADA NA LOJA" : "ENTREGA");
            stmt.setString(11, "EM_SEPARACAO");
            stmt.setQueryTimeout(10);
            stmt.executeUpdate();

            idVenda = proximoId;

            System.out.println("   ✅ Venda registrada (ID: " + idVenda + ", Pedido: " + notif.pedidoId + ")");

        } catch (SQLException e) {
            System.err.println("   ❌ Erro ao registrar venda: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }

        return idVenda;
    }

    // ==========================================
    // BUSCAR PRÓXIMO ID DA VENDA
    // ==========================================
    private static int getProximoIdVenda(Connection con) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int proximoId = 1;

        try {
            String sql = "SELECT MAX(id) FROM vendas";
            stmt = con.prepareStatement(sql);
            stmt.setQueryTimeout(10);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int maxId = rs.getInt(1);
                proximoId = maxId + 1;
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Erro ao buscar próximo ID: " + e.getMessage());
            proximoId = (int) (System.currentTimeMillis() / 1000);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }

        return proximoId;
    }

    // ==========================================
    // REGISTRAR SACOLA
    // ==========================================
    private static void registrarSacola(Connection con, Notificacao notif, int idVenda) {
        PreparedStatement stmt = null;

        try {
            String valorFormatado = String.format("%.2f", notif.valor).replace(".", ",");

            String sql = "INSERT INTO sacola (id, pedido_id, datavenda, valorvenda, status, codepcas, nomecli, tipoentrega) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, idVenda);
            stmt.setString(2, notif.pedidoId);
            stmt.setDate(3, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.setString(4, valorFormatado);
            stmt.setString(5, "EM_SEPARACAO");
            stmt.setString(6, notif.codPeca);
            stmt.setString(7, notif.cliente);
            stmt.setString(8, notif.retirarLoja ? "RETIRE_LOJA" : "ENTREGA");
            stmt.setQueryTimeout(10);
            stmt.executeUpdate();

            System.out.println("   ✅ Sacola registrada (ID_Venda: " + idVenda + ")");

        } catch (SQLException e) {
            System.err.println("   ❌ Erro ao registrar sacola: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // REGISTRAR ENTREGA
    // ==========================================
    private static void registrarEntrega(Connection con, Notificacao notif, int idVenda) {
        PreparedStatement stmt = null;

        try {
            String tipoEntrega = notif.retirarLoja ? "RETIRE_LOJA" : "ENTREGA_ENDERECO";

            String sql = "INSERT INTO entregas (idvenda, pedido_id, datavenda, nomecli, codpeca, valorfrete, fretepago, entregue, dataentrega, status, tipoentrega, canal) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, idVenda);
            stmt.setString(2, notif.pedidoId);
            stmt.setDate(3, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.setString(4, notif.cliente);
            stmt.setString(5, notif.codPeca);
            stmt.setDouble(6, 0.0);
            stmt.setBoolean(7, false);
            stmt.setBoolean(8, false);
            stmt.setNull(9, java.sql.Types.DATE);
            stmt.setString(10, "EM_SEPARACAO");
            stmt.setString(11, tipoEntrega);
            stmt.setString(12, "SITE");
            stmt.setQueryTimeout(10);
            stmt.executeUpdate();

            System.out.println("   ✅ Entrega registrada (ID_Venda: " + idVenda + ")");

        } catch (SQLException e) {
            System.err.println("   ❌ Erro ao registrar entrega: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // ATUALIZAR SITE (ASSÍNCRONO)
    // ==========================================
    private static void atualizarSiteAsync() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("🔄 [SITE] Atualizando vitrine virtual...");

                GerarSiteEstoque gerador = new GerarSiteEstoque();
                gerador.gerarSiteEstoque();

                System.out.println("✅ [SITE] Vitrine virtual atualizada!");
            } catch (ClassNotFoundException | InterruptedException | SQLException e) {
                System.err.println("❌ [SITE] Erro ao atualizar: " + e.getMessage());
            }
        }).start();
    }

    // ==========================================
    // MOSTRAR MENSAGEM NA BANDEJA
    // ==========================================
    private static void mostrarMensagemTray(String titulo, String mensagem) {
        System.out.println("🔔 [" + titulo + "] " + mensagem);
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("");
                TrayIcon trayIcon = new TrayIcon(image, "PORTOBELLA");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                trayIcon.displayMessage(titulo, mensagem, TrayIcon.MessageType.INFO);
                new Timer(3000, e -> {
                    tray.remove(trayIcon);
                }).start();
            }
        } catch (AWTException e) {
            System.err.println("⚠️ Erro ao mostrar mensagem na bandeja: " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================
    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    // ==========================================
    // MAIN
    // ==========================================
    public static void main(String[] args) {
        try {
            iniciar();
        } catch (IOException ex) {
            System.err.println("Erro: " + ex);
        }
    }
}