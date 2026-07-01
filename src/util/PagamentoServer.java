package util;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import connection.ConnectionDB;

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

public class PagamentoServer {
    
    private static final Gson gson = new Gson();
    private static HttpServer server;
    
    // ==========================================
    // 🔥 CONFIGURAÇÕES DO PIX (SEM MERCADO PAGO)
    // ==========================================
    private static final String CHAVE_PIX = "portobella.brecho@gmail.com"; // ⚠️ Coloque sua chave Pix aqui
    private static final String NOME_RECEBEDOR = "VANDERLEIA VIEI"; // ⚠️ Coloque seu nome aqui
    private static final String CIDADE = "PORTO ALEGRE";
    
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
        server.createContext("/api/pagamentos/notificar", new NotificarSistemaHandler());
        server.createContext("/api/pagamentos/consultar", new ConsultarNotificacoesHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("✅ Servidor de pagamentos rodando em http://localhost:8080");
        System.out.println("   🔥 PIX: GERADO SEM MERCADO PAGO (usando chave: " + CHAVE_PIX + ")");
        System.out.println("   💳 MERCADO PAGO: SOMENTE PARA LINK DE PAGAMENTO");
        System.out.println("   📦 Frete: Cálculo por CEP (ViaCEP + Fallback)");
        System.out.println("   🔔 Notificações: /api/pagamentos/notificar");
        System.out.println("   🔍 Consultar: /api/pagamentos/consultar");
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
                    String payloadPix = gerarPayloadPix(total, "Pedido PORTOBERLLA");

                    response.put("success", true);
                    response.put("meio", "PIX");
                    response.put("payload", payloadPix);
                    response.put("total", total);
                    response.put("pedidoId", System.currentTimeMillis());

                    System.out.println("   ✅ Pix gerado com sucesso!");

                } else {
                    // Gera link do Mercado Pago
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
            } catch (JsonSyntaxException | IOException e) {
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
            } catch (JsonSyntaxException | IOException e) {
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
                    
                    String payloadPix = gerarPayloadPix(valorTotal, "Pedido PORTOBELLA");
                    
                    response.put("success", true);
                    response.put("meio", "PIX");
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
            } catch (IOException | NumberFormatException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }
    }
    
    // ==========================================
    // GERAR PAYLOAD PIX COMPLETO
    // ==========================================
    public static String gerarPayloadPix(double valor, String descricao) {
        try {
            System.out.println("🔧 Gerando payload Pix...");
            System.out.println("💰 Valor: R$ " + valor);
            
            StringBuilder payload = new StringBuilder();
            
            // 1. Payload Format Indicator (00)
            payload.append(emvField("00", "01"));
            
            // 2. Point of Initiation Method (01)
            payload.append(emvField("01", "11"));
            
            // 3. Merchant Account Information - PIX (26)
            String gui = "br.gov.bcb.pix";
            String chavePix = CHAVE_PIX;
            
            // Subcampos do 26
            String sub00 = emvField("00", gui);
            String sub01 = emvField("01", chavePix);
            String valor26 = sub00 + sub01;
            
            payload.append(emvField("26", valor26));
            
            // 4. Merchant Category Code (52)
            payload.append(emvField("52", "0000"));
            
            // 5. Transaction Currency (53)
            payload.append(emvField("53", "986"));
            
            // 6. Transaction Amount (54) - SÓ SE TIVER VALOR
            if (valor > 0) {
                String valorFormatado = String.format("%.2f", valor);
                payload.append(emvField("54", valorFormatado));
            }
            
            // 7. Country Code (58)
            payload.append(emvField("58", "BR"));
            
            // 8. Merchant Name (59)
            payload.append(emvField("59", NOME_RECEBEDOR));
            
            // 9. Merchant City (60)
            payload.append(emvField("60", CIDADE));
            
            // 10. Additional Data Field Template (62)
            String txid = "***";
            String sub05 = emvField("05", txid);
            payload.append(emvField("62", sub05));
            
            // 11. CRC (63) - Calculado no final
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
    // CRIAR CAMPO EMV PADRÃO (ID + TAMANHO + VALOR)
    // ==========================================
    private static String emvField(String id, String valor) {
        if (valor == null) {
            valor = "";
        }
        int tamanho = valor.length();
        return id + String.format("%02d", tamanho) + valor;
    }
    
    // ==========================================
    // CALCULAR CRC16 (PADRÃO PIX)
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
        } catch (IOException e) {
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
                
            } catch (IOException e) {
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
                
            } catch (IOException e) {
                sendResponse(exchange, 200, "{\"status\":\"ok\"}");
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

                // ==========================================
                // 🔥 SALVAR NO BANCO DE DADOS
                // ==========================================
                salvarNotificacaoNoBanco(codPeca, nomeCliente, valorTotal, meioPagamento, 
                                      retirarLoja, endereco, pedidoId, telefone, itens);

                // ==========================================
                // RETORNA SUCESSO IMEDIATO PARA O CLIENTE
                // ==========================================
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

        // ==========================================
        // SALVAR NOTIFICAÇÃO NO BANCO DE DADOS
        // ==========================================
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

                    // Busca notificações PENDENTES e NÃO LIDAS
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
    // REGISTRAR VENDA NAS TABELAS
    // ==========================================
    private static void registrarVenda(String codPeca, String cliente, double valor, 
                                        String meioPagamento, String pedidoId, 
                                        String endereco, boolean retirarLoja, String telefone) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();

            // ==========================================
            // 1. REGISTRAR NA TABELA VENDAS
            // ==========================================
            String sqlVenda = "INSERT INTO vendas (datavenda, codpeca, cliente, valor_total, meio_pagamento, " +
                             "pedido_id, endereco_entrega, retirar_loja, telefone, status_pagamento) " +
                             "VALUES (CURDATE(), ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMADO')";

            stmt = con.prepareStatement(sqlVenda);
            stmt.setString(1, codPeca);
            stmt.setString(2, cliente);
            stmt.setDouble(3, valor);
            stmt.setString(4, meioPagamento);
            stmt.setString(5, pedidoId);
            stmt.setString(6, endereco);
            stmt.setBoolean(7, retirarLoja);
            stmt.setString(8, telefone);
            stmt.executeUpdate();
            System.out.println("   ✅ Venda registrada: " + pedidoId);
            stmt.close();

            // ==========================================
            // 2. REGISTRAR NA TABELA SACOLA
            // ==========================================
            String sqlSacola = "INSERT INTO sacola (codpeca, cliente, valor, meio_pagamento, pedido_id, telefone, data, status) " +
                              "VALUES (?, ?, ?, ?, ?, ?, NOW(), 'CONFIRMADO')";

            stmt = con.prepareStatement(sqlSacola);
            stmt.setString(1, codPeca);
            stmt.setString(2, cliente);
            stmt.setDouble(3, valor);
            stmt.setString(4, meioPagamento);
            stmt.setString(5, pedidoId);
            stmt.setString(6, telefone);
            stmt.executeUpdate();
            System.out.println("   ✅ Sacola registrada: " + pedidoId);
            stmt.close();

            // ==========================================
            // 3. REGISTRAR NA TABELA ENTREGA
            // ==========================================
            String tipoEntrega = retirarLoja ? "RETIRE_LOJA" : "ENTREGA_ENDERECO";
            String statusEntrega = retirarLoja ? "AGUARDANDO_RETIRADA" : "AGUARDANDO_ENVIO";

            String sqlEntrega = "INSERT INTO entrega (pedido_id, cliente, endereco, tipo_entrega, status, data) " +
                               "VALUES (?, ?, ?, ?, ?, NOW())";

            stmt = con.prepareStatement(sqlEntrega);
            stmt.setString(1, pedidoId);
            stmt.setString(2, cliente);
            stmt.setString(3, endereco);
            stmt.setString(4, tipoEntrega);
            stmt.setString(5, statusEntrega);
            stmt.executeUpdate();
            System.out.println("   ✅ Entrega registrada: " + tipoEntrega);
            stmt.close();

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("   ❌ Erro ao registrar venda: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // ATUALIZAR ESTOQUE (BAIXA DO PRODUTO)
    // ==========================================
    private static void atualizarEstoque(String codPeca) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();

            // ==========================================
            // 1. ATUALIZAR STATUS DA PEÇA PARA VENDIDO
            // ==========================================
            String sqlUpdate = "UPDATE estoque SET status = 'VENDIDO' WHERE codpeca = ? AND status = 'DISPONIVEL'";

            stmt = con.prepareStatement(sqlUpdate);
            stmt.setString(1, codPeca);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("   ✅ Estoque atualizado: " + codPeca + " -> VENDIDO");
            } else {
                System.out.println("   ⚠️ Produto já vendido ou não encontrado: " + codPeca);
            }
            stmt.close();

            // ==========================================
            // 2. REGISTRAR MOVIMENTAÇÃO DE ESTOQUE
            // ==========================================
            String sqlMov = "INSERT INTO movimentacao_estoque (codpeca, data_movimento, status_anterior, status_novo, observacao) " +
                           "SELECT ?, NOW(), status, 'VENDIDO', 'Venda confirmada - Pedido #' || ? FROM estoque WHERE codpeca = ?";

            stmt = con.prepareStatement(sqlMov);
            stmt.setString(1, codPeca);
            stmt.setString(2, codPeca);
            stmt.setString(3, codPeca);
            stmt.executeUpdate();
            System.out.println("   ✅ Movimentação registrada: " + codPeca);
            stmt.close();

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("   ❌ Erro ao atualizar estoque: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
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