package util;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import connection.ConnectionDB;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PagamentoServer {
    
    private static final Gson gson = new Gson();
    private static HttpServer server;
    
    // ==========================================
    // 🔥 CONFIGURAÇÕES DO PIX (SEM MERCADO PAGO)
    // ==========================================
    private static final String CHAVE_PIX = "portobella.brecho@gmail.com"; // ⚠️ Coloque sua chave Pix aqui
    private static final String NOME_RECEBEDOR = "Vanderleia Vieira Moraes Lemos Steinmetz"; // ⚠️ Coloque seu nome aqui
    
    // ==========================================
    // 🔥 TOKEN DO MERCADO PAGO (SOMENTE PARA LINK)
    // ==========================================
    private static final String TOKEN_MP = "APP_USR-5504079628127234-061707-4f72faca8cd75c397d89abc34651960f-3480421128";
    
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }
    
    public static void iniciar() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        
        server.createContext("/api/pagamentos/criar", new CriarPagamentoHandler());
        server.createContext("/api/pagamentos/status", new StatusPagamentoHandler());
        server.createContext("/api/webhook", new WebhookHandler());
        server.createContext("/api/pagamentos/finalizar", new FinalizarCompraHandler());
        server.createContext("/api/frete/calcular", new CalcularFreteHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("✅ Servidor de pagamentos rodando em http://localhost:8080");
        System.out.println("   🔥 PIX: GERADO SEM MERCADO PAGO (usando chave: " + CHAVE_PIX + ")");
        System.out.println("   💳 MERCADO PAGO: SOMENTE PARA LINK DE PAGAMENTO");
        System.out.println("   📦 Frete: Cálculo por CEP (ViaCEP + Fallback)");
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
                // ==========================================
                // 🔥 CORRIGIDO: BufferedReader (NÃO BufferedWriter)
                // ==========================================
                String body = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().reduce("", (a, b) -> a + b);

                System.out.println("📥 Body recebido: " + body);

                // ==========================================
                // 🔥 CORRIGIDO: SEM "var" (Java 8)
                // ==========================================
                JsonObject json = gson.fromJson(body, JsonObject.class);
                String meio = json.get("meio").getAsString();
                double subtotal = json.get("subtotal").getAsDouble();
                double frete = json.get("frete").getAsDouble();
                double total = json.get("total").getAsDouble();
                String cep = json.get("cep").getAsString();
                String endereco = json.get("endereco").getAsString();

                // ==========================================
                // 🔥 CORRIGIDO: JsonArray (NÃO "var")
                // ==========================================
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

                // ==========================================
                // REGISTRA A VENDA NO BANCO
                // ==========================================
                registrarVendaCarrinho(codPeca, subtotal, frete, total, endereco, cep, meio);

                // ==========================================
                // PREPARA RESPOSTA
                // ==========================================
                Map<String, Object> response = new HashMap<>();

                if ("pix".equalsIgnoreCase(meio)) {
                    // Gera payload Pix com o valor TOTAL
                    String payloadPix = gerarPayloadPix(total, "Pedido PORTOBELLA");

                    response.put("success", true);
                    response.put("meio", "pix");
                    response.put("payload", payloadPix);
                    response.put("total", total);
                    response.put("pedidoId", System.currentTimeMillis());

                    System.out.println("   ✅ Pix gerado com sucesso!");

                } else {
                    // Gera link do Mercado Pago
                    String link = criarLinkMercadoPago(codPeca, "Pedido PORTOBELLA", total);

                    if (link != null && !link.isEmpty()) {
                        response.put("success", true);
                        response.put("meio", "mercado_pago");
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

        // ==========================================
        // REGISTRAR VENDA NO BANCO
        // ==========================================
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

                // Remove caracteres não numéricos
                cep = cep.replaceAll("\\D", "");

                if (cep.length() != 8) {
                    sendResponse(exchange, 400, "{\"error\":\"CEP inválido. Deve ter 8 dígitos.\"}");
                    return;
                }

                System.out.println("📦 Calculando frete para CEP: " + cep);

                // ==========================================
                // 1. TENTA BUSCAR ENDEREÇO PELO ViaCEP
                // ==========================================
                String uf = buscarUFViaCEP(cep);

                // ==========================================
                // 2. CALCULA FRETE BASEADO NA UF
                // ==========================================
                double valorFrete = calcularFretePorUF(uf);
                String prazo = estimarPrazoPorUF(uf);

                // ==========================================
                // 3. MONTAR RESPOSTA
                // ==========================================
                Map<String, Object> response = new HashMap<String, Object>();
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

            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> error = new HashMap<String, Object>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        // ==========================================
        // BUSCAR UF PELO CEP (ViaCEP)
        // ==========================================
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
            } catch (Exception e) {
                System.out.println("   ⚠️ ViaCEP indisponível: " + e.getMessage());
            }

            // Fallback: estimar UF pelo prefixo do CEP
            return estimarUFporCEP(cep);
        }

        // ==========================================
        // BUSCAR CIDADE PELO CEP (ViaCEP)
        // ==========================================
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
            } catch (Exception e) {
                // Ignora erro
            }
            return "Não informado";
        }

        // ==========================================
        // ESTIMAR UF PELO PREFIXO DO CEP (FALLBACK)
        // ==========================================
        private String estimarUFporCEP(String cep) {
            String prefixo = cep.substring(0, 1);

            switch (prefixo) {
                case "0": return "SP";  // São Paulo
                case "1": return "SP";  // São Paulo
                case "2": return "RJ";  // Rio de Janeiro
                case "3": return "MG";  // Minas Gerais
                case "4": return "BA";  // Bahia
                case "5": return "PE";  // Pernambuco
                case "6": return "CE";  // Ceará
                case "7": return "DF";  // Distrito Federal
                case "8": return "PR";  // Paraná
                case "9": return "RS";  // Rio Grande do Sul
                default: return "SP";
            }
        }

        // ==========================================
        // CALCULAR FRETE POR UF
        // ==========================================
        private double calcularFretePorUF(String uf) {
            if (uf == null || uf.isEmpty()) {
                return 35.90;
            }

            switch (uf.toUpperCase()) {
                // Sudeste (mais próximo)
                case "SP":
                case "RJ":
                case "MG":
                case "ES":
                    return 25.90;

                // Sul
                case "PR":
                case "SC":
                case "RS":
                    return 35.90;

                // Centro-Oeste
                case "DF":
                case "GO":
                case "MT":
                case "MS":
                    return 40.90;

                // Nordeste
                case "BA":
                case "SE":
                case "AL":
                case "PE":
                case "PB":
                case "RN":
                case "CE":
                case "PI":
                case "MA":
                    return 45.90;

                // Norte
                case "PA":
                case "AM":
                case "AC":
                case "RR":
                case "RO":
                case "AP":
                case "TO":
                    return 55.90;

                default:
                    return 35.90;
            }
        }

        // ==========================================
        // ESTIMAR PRAZO POR UF
        // ==========================================
        private String estimarPrazoPorUF(String uf) {
            if (uf == null || uf.isEmpty()) {
                return "5 a 7 dias úteis";
            }

            switch (uf.toUpperCase()) {
                case "SP":
                case "RJ":
                    return "2 a 4 dias úteis";
                case "MG":
                case "ES":
                    return "3 a 5 dias úteis";
                case "PR":
                case "SC":
                    return "4 a 6 dias úteis";
                case "RS":
                    return "5 a 7 dias úteis";
                case "DF":
                case "GO":
                    return "5 a 7 dias úteis";
                case "BA":
                case "SE":
                    return "5 a 8 dias úteis";
                case "PE":
                case "PB":
                case "RN":
                case "CE":
                    return "6 a 9 dias úteis";
                case "AM":
                case "PA":
                case "AC":
                case "RR":
                case "RO":
                case "AP":
                case "TO":
                    return "8 a 12 dias úteis";
                default:
                    return "5 a 7 dias úteis";
            }
        }

        // ==========================================
        // LER RESPOSTA DA API
        // ==========================================
        private String lerResposta(java.net.HttpURLConnection conn) throws IOException {
            java.io.InputStream is = conn.getInputStream();
            try (java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A")) {
                return s.hasNext() ? s.next() : "";
            }
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
                    // ==========================================
                    // 🔥 PIX: GERAR QR CODE SEM MERCADO PAGO
                    // ==========================================
                    System.out.println("   🔥 Gerando QR Code Pix SEM Mercado Pago...");
                    
                    String payloadPix = gerarPayloadPix(valorTotal, nome);
                    
                    response.put("success", true);
                    response.put("meio", "pix");
                    response.put("payload", payloadPix);
                    response.put("valor", valorTotal);
                    response.put("produto", nome);
                    
                    System.out.println("   ✅ Payload Pix gerado com sucesso!");
                    System.out.println("   Chave: " + CHAVE_PIX);
                    
                } else if ("mercado_pago".equalsIgnoreCase(meio)) {
                    // ==========================================
                    // 💳 MERCADO PAGO: GERAR LINK DE PAGAMENTO
                    // ==========================================
                    System.out.println("   💳 Gerando link do Mercado Pago...");
                    
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
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }
    }
    
    // ==========================================
    // 🔥 GERAR PAYLOAD PIX (SEM MERCADO PAGO)
    // ==========================================
    private static String gerarPayloadPix(double valor, String descricao) {
        // Formata o valor (ex: 89.90 -> 0000000008990)
        String valorStr = String.format("%.0f", valor * 100);
        String valorFormatado = valorStr.length() + valorStr;
        
        // Monta o payload Pix (BR Code)
        StringBuilder payload = new StringBuilder();
        payload.append("000201");
        payload.append("26330014BR.GOV.BCB.PIX0111").append(CHAVE_PIX);
        payload.append("52040000");
        payload.append("5303986");
        payload.append("54").append(valorFormatado);
        payload.append("5802BR");
        payload.append("59").append(String.format("%02d", NOME_RECEBEDOR.length())).append(NOME_RECEBEDOR);
        payload.append("6008BRASIL");
        
        // Descrição (opcional)
        if (descricao != null && !descricao.isEmpty()) {
            String desc = descricao.length() > 20 ? descricao.substring(0, 20) : descricao;
            payload.append("62070503***");
        }
        
        // Calcula CRC16
        String crc = calcularCRC16(payload.toString());
        payload.append("6304").append(crc);
        
        return payload.toString();
    }
    
    // ==========================================
    // CALCULAR CRC16 (para o Pix)
    // ==========================================
    private static String calcularCRC16(String payload) {
        int crc = 0xFFFF;
        byte[] bytes = payload.getBytes(StandardCharsets.ISO_8859_1);
        
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
    }
    
    // ==========================================
    // 💳 MERCADO PAGO: Criar link de pagamento
    // ==========================================
    private static String criarLinkMercadoPago(String codPeca, String titulo, double valor) {
        try {
            String precoFormatado = String.format(java.util.Locale.US, "%.2f", valor);
            
            // Monta o JSON da preferência
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

            // Faz a requisição para o Mercado Pago
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
                
                // Lê o erro
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
        } catch (Exception e) {
            System.err.println("   ❌ Erro MP: " + e.getMessage());
        }
        
        return null;
    }
    
    // ==========================================
    // STATUS PAGAMENTO (para o frontend)
    // ==========================================
    static class StatusPagamentoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            try {
                String path = exchange.getRequestURI().getPath();
                String paymentIdStr = path.substring(path.lastIndexOf("/") + 1);
                
                // Para Pix (pagamento direto), sempre retorna como pendente
                // O cliente confirma manualmente
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("paymentId", paymentIdStr);
                response.put("status", "pending");
                
                sendResponse(exchange, 200, gson.toJson(response));
                
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }
    }
    
    // ==========================================
    // WEBHOOK (para Mercado Pago)
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
                
            } catch (Exception e) {
                sendResponse(exchange, 200, "{\"status\":\"ok\"}");
            }
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
    
    public static void main(String[] args) {
        try {
            iniciar();
        } catch (IOException ex) {
            System.err.println("Erro: "+ex);
        }
    }
}