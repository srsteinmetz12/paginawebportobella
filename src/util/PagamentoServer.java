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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import paginaweb.EmailServiceSendGrid;
import paginaweb.GerarSiteEstoque;
import paginaweb.ListarProdutosHandler;

public class PagamentoServer {

    // ==========================================
    // CONSTANTES
    // ==========================================
    private static final Gson gson = new Gson();
    private static HttpServer server;
    private static final Map<String, DadosPedido> pedidosPendentes = new ConcurrentHashMap<>();

    private static final String CHAVE_PIX = "portobella.brecho@gmail.com";
    private static final String NOME_RECEBEDOR = "VANDERLEIA VIEI";
    private static final String CIDADE = "PORTO ALEGRE";
    private static String TOKEN_MP = System.getenv("MP_ACCESS_TOKEN");

    // ==========================================
    // CLASSE AUXILIAR NOTIFICACAO
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
        
        server.createContext("/", new RootHandler());
        server.createContext("/api/pagamentos/criar", new CriarPagamentoHandler());
        server.createContext("/api/pagamentos/status", new StatusPagamentoHandler());
        server.createContext("/api/webhook", new WebhookHandler());
        server.createContext("/api/pagamentos/finalizar", new FinalizarCompraHandler());
        server.createContext("/api/frete/calcular", new CalcularFreteHandler());
        server.createContext("/api/pagamentos/notificar", new NotificarSistemaHandler());
        server.createContext("/api/pagamentos/consultar", new ConsultarNotificacoesHandler());
        server.createContext("/api/pagamentos/reservar-lote", new ReservarLoteHandler());
        server.createContext("/api/pedidos/confirmar", new ConfirmarPedidoHandler());
        server.createContext("/api/produtos", new ListarProdutosHandler());
        server.createContext("/api/pagamentos/verificar-disponibilidade", new VerificarDisponibilidadeHandler());
        server.createContext("/api/pagamentos/finalizar-mp", new FinalizarMercadoPagoHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ Servidor de pagamentos rodando em http://localhost:8080");
        System.out.println("   🔥 PIX: " + CHAVE_PIX);
        System.out.println("   💳 MERCADO PAGO: Link de pagamento");
        System.out.println("   📦 Frete: Cálculo por CEP");
        System.out.println("   🔔 Notificações: /api/pagamentos/notificar");
        System.out.println("   🔍 Consultar: /api/pagamentos/consultar");
        System.out.println("   🔒 Reservar: /api/pagamentos/reservar-lote");
        System.out.println("   🔓 Liberar: /api/pagamentos/liberar-reserva");
        System.out.println("   📦 Produtos: /api/produtos");
    }

    public static void parar() {
        if (server != null) server.stop(0);
    }
    
    private static class DadosPedido {
        List<Map<String, Object>> itens;
        String email;
        String destinatario;
        String telefone;
        String endereco;
        boolean retirarLoja;
        double subtotal;
        double frete;
        double total;
    }
    
    // ==========================================
    // HANDLER: ROOT HANDLER
    // ==========================================    
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 🔥 DIRETÓRIO ONDE O index.html DEVE FICAR
            String baseDir = "/app/estoqueVitrineWeb";
            File dir = new File(baseDir);

            // 🔥 CRIA O DIRETÓRIO SE NÃO EXISTIR
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("📁 Diretório criado: " + baseDir);
            }

            File htmlFile = new File(dir, "index.html");

            // 🔥 SE O ARQUIVO NÃO EXISTIR, GERA
            if (!htmlFile.exists()) {
                try {
                    System.out.println("📄 Gerando index.html...");
                    GerarSiteEstoque.main(new String[0]);
                } catch (Exception e) {
                    System.err.println("❌ Erro ao gerar: " + e.getMessage());
                }
            }

            // 🔥 SERVE O ARQUIVO
            if (htmlFile.exists()) {
                byte[] response = java.nio.file.Files.readAllBytes(htmlFile.toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } else {
                String msg = "Erro: index.html não foi gerado";
                byte[] response = msg.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        }
    }

    // ==========================================
    // HANDLER: FINALIZAR COMPRA (CARRINHO)
    // ==========================================
    static class FinalizarCompraHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
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

                // 🔥 CAMPO EMAIL OBRIGATÓRIO – VALIDAÇÃO
                if (!json.has("email") || json.get("email").getAsString().trim().isEmpty() || !json.get("email").getAsString().contains("@")) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", "E-mail é obrigatório e deve ser válido");
                    sendResponse(exchange, 400, gson.toJson(error));
                    return;
                }

                String meio = json.get("meio").getAsString();
                double subtotal = json.get("subtotal").getAsDouble();
                double frete = json.get("frete").getAsDouble();
                double total = json.get("total").getAsDouble();
                String cep = json.get("cep").getAsString();
                String endereco = json.get("endereco").getAsString();
                String email = json.get("email").getAsString();
                String destinatario = json.get("destinatario").getAsString();
                String telefone = json.get("telefone").getAsString();
                boolean retirarLoja = json.has("retirarLoja") && json.get("retirarLoja").getAsBoolean();

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
                    String pedidoId = String.valueOf(System.currentTimeMillis());

                    // Salva dados para fallback
                    DadosPedido dados = new DadosPedido();
                    List<Map<String, Object>> itensConvertidos = new ArrayList<>();
                    if (itensArray != null) {
                        for (int i = 0; i < itensArray.size(); i++) {
                            JsonObject item = itensArray.get(i).getAsJsonObject();
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", item.get("id").getAsString());
                            map.put("nome", item.get("nome").getAsString());
                            map.put("preco", item.get("preco").getAsDouble());
                            map.put("quantidade", item.get("quantidade").getAsInt());
                            itensConvertidos.add(map);
                        }
                    }
                    dados.itens = itensConvertidos;
                    dados.email = email;
                    dados.destinatario = destinatario;
                    dados.telefone = telefone;
                    dados.endereco = endereco;
                    dados.retirarLoja = retirarLoja;
                    dados.subtotal = subtotal;
                    dados.frete = frete;
                    dados.total = total;
                    pedidosPendentes.put(pedidoId, dados);

                    // Gera link do Mercado Pago
                    String link = criarLinkMercadoPago(codPeca, "Pedido PORTOBELLA", total, pedidoId, email, destinatario, telefone);

                    if (link != null && !link.isEmpty()) {
                        response.put("success", true);
                        response.put("meio", "CREDITO");
                        response.put("paymentUrl", link);
                        response.put("pedidoId", pedidoId);
                        System.out.println("   ✅ Link MP gerado: " + link);
                    } else {
                        response.put("success", false);
                        response.put("error", "Erro ao gerar link do Mercado Pago");
                        System.err.println("   ❌ Falha ao gerar link MP para o pedido " + pedidoId);
                    }
                }

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "JSON inválido: " + e.getMessage());
                sendResponse(exchange, 400, gson.toJson(error));
            } catch (IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Erro interno: " + e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }
    }
    private List<Map<String, Object>> converterItensArray(com.google.gson.JsonArray itensArray) {
        List<Map<String, Object>> lista = new ArrayList<>();
        if (itensArray == null) return lista;
        for (int i = 0; i < itensArray.size(); i++) {
            JsonObject item = itensArray.get(i).getAsJsonObject();
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.get("id").getAsString());
            map.put("nome", item.get("nome").getAsString());
            map.put("preco", item.get("preco").getAsDouble());
            map.put("quantidade", item.get("quantidade").getAsInt());
            lista.add(map);
        }
        return lista;
    }

    // ==========================================
    // HANDLER: CALCULAR FRETE POR CEP
    // ==========================================
    static class CalcularFreteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange); // método addCorsHeaders deve estar definido

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

                // Busca UF e cidade (para informação)
                String uf = buscarUFViaCEP(cep);
                String cidade = buscarCidadeViaCEP(cep);

                // 🔥 CALCULA FRETE PELA TABELA DETALHADA POR CEP
                double valorFrete = calcularFretePorCEP(cep);

                // 🔥 CALCULA PRAZO PELA TABELA DETALHADA POR CEP
                String prazo = estimarPrazoPorCEP(cep);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("cep", cep);
                response.put("uf", uf);
                response.put("frete", valorFrete);
                response.put("prazo", prazo);
                response.put("cidade", cidade);

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
        // MÉTODOS AUXILIARES (BUSCA VIA CEP, ETC.)
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
            if (cep == null || cep.isEmpty()) return "SP";
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

        // ==========================================
        // 🔥 NOVA TABELA DE FRETE DETALHADA POR CEP
        // ==========================================
        private double calcularFretePorCEP(String cep) {
            if (cep == null || cep.length() < 3) return 35.90;
            int faixa;
            try {
                faixa = Integer.parseInt(cep.substring(0, 3));
            } catch (NumberFormatException e) {
                return 35.90;
            }

            // ==================== SUL ====================
            // RS (90000-99999)
            if (faixa >= 900 && faixa <= 919) return 15.90;
            if (faixa >= 920 && faixa <= 939) return 16.90;
            if (faixa >= 940 && faixa <= 969) return 22.90;
            if (faixa >= 970 && faixa <= 989) return 22.90;
            if (faixa >= 990 && faixa <= 999) return 27.90;

            // SC (88000-89999)
            if (faixa >= 880 && faixa <= 889) return 29.90;
            if (faixa >= 890 && faixa <= 899) return 34.90;

            // PR (80000-87999)
            if (faixa >= 800 && faixa <= 809) return 32.90;
            if (faixa >= 810 && faixa <= 839) return 39.90;
            if (faixa >= 840 && faixa <= 859) return 42.90;
            if (faixa >= 860 && faixa <= 879) return 36.90;

            // ==================== SUDESTE ====================
            // SP (01000-19999)
            if (faixa >= 100 && faixa <= 599) return 46.90;
            if (faixa >= 600 && faixa <= 999) return 46.90;
            if (faixa >= 110 && faixa <= 139) return 51.90;
            if (faixa >= 140 && faixa <= 159) return 44.90;
            if (faixa >= 160 && faixa <= 199) return 41.90;

            // RJ (20000-28999)
            if (faixa >= 200 && faixa <= 289) return 55.90;

            // MG (30000-39999)
            if (faixa >= 300 && faixa <= 319) return 58.90;
            if (faixa >= 320 && faixa <= 349) return 57.90;
            if (faixa >= 350 && faixa <= 359) return 51.90;
            if (faixa >= 360 && faixa <= 399) return 53.90;

            // ES (29000-29999)
            if (faixa >= 290 && faixa <= 299) return 63.90;

            // ==================== CENTRO-OESTE ====================
            // DF (70000-72999)
            if (faixa >= 700 && faixa <= 729) return 61.90;

            // GO (73000-76999)
            if (faixa >= 730 && faixa <= 749) return 58.90;
            if (faixa >= 750 && faixa <= 769) return 58.90;

            // MT (78000-79999)
            if (faixa >= 780 && faixa <= 789) return 70.90;
            if (faixa >= 790 && faixa <= 799) return 67.90;

            // MS (79000-79999) – já incluso, mas mantido
            if (faixa >= 790 && faixa <= 799) return 56.90;

            // ==================== NORDESTE ====================
            // BA (40000-48999)
            if (faixa >= 400 && faixa <= 419) return 70.90;
            if (faixa >= 420 && faixa <= 449) return 68.90;
            if (faixa >= 450 && faixa <= 489) return 65.90;

            // PE (50000-56999)
            if (faixa >= 500 && faixa <= 509) return 79.90;
            if (faixa >= 510 && faixa <= 549) return 76.90;
            if (faixa >= 550 && faixa <= 569) return 72.90;

            // CE (60000-63999)
            if (faixa >= 600 && faixa <= 619) return 82.90;
            if (faixa >= 620 && faixa <= 639) return 79.90;

            // PB (58000-58999)
            if (faixa >= 580 && faixa <= 589) return 77.90;

            // RN (59000-59999)
            if (faixa >= 590 && faixa <= 599) return 85.90;

            // AL (57000-57999)
            if (faixa >= 570 && faixa <= 579) return 77.90;

            // SE (49000-49999)
            if (faixa >= 490 && faixa <= 499) return 72.90;

            // PI (64000-64999)
            if (faixa >= 640 && faixa <= 649) return 77.90;

            // MA (65000-65999)
            if (faixa >= 650 && faixa <= 659) return 82.90;

            // ==================== NORTE ====================
            // PA (66000-68999)
            if (faixa >= 660 && faixa <= 669) return 94.90;
            if (faixa >= 670 && faixa <= 689) return 90.90;

            // AM (69000-69999)
            if (faixa >= 690 && faixa <= 699) return 102.90;

            // RR (69300-69399)
            if (faixa >= 693 && faixa <= 693) return 110.90;

            // AP (68900-68999)
            if (faixa >= 689 && faixa <= 689) return 102.90;

            // AC (69900-69999)
            if (faixa >= 699 && faixa <= 699) return 110.90;

            // RO (76800-76999)
            if (faixa >= 768 && faixa <= 769) return 82.90;

            // TO (77000-77999)
            if (faixa >= 770 && faixa <= 779) return 70.90;

            // ==================== FALLBACK ====================
            // Se não mapeado, usa a UF estimada
            String uf = estimarUFporCEP(cep);
            return calcularFretePorUF(uf);
        }

        // ==========================================
        // 🔥 NOVA TABELA DE PRAZO DETALHADA POR CEP
        // ==========================================
        private String estimarPrazoPorCEP(String cep) {
            if (cep == null || cep.length() < 3) return "5 a 7 dias úteis";
            int faixa;
            try {
                faixa = Integer.parseInt(cep.substring(0, 3));
            } catch (NumberFormatException e) {
                return "5 a 7 dias úteis";
            }

            // RS
            if (faixa >= 900 && faixa <= 919) return "1 a 2 dias úteis";
            if (faixa >= 920 && faixa <= 939) return "2 a 3 dias úteis";
            if (faixa >= 940 && faixa <= 969) return "3 a 5 dias úteis";
            if (faixa >= 970 && faixa <= 989) return "3 a 5 dias úteis";
            if (faixa >= 990 && faixa <= 999) return "4 a 6 dias úteis";

            // SC
            if (faixa >= 880 && faixa <= 889) return "4 a 6 dias úteis";
            if (faixa >= 890 && faixa <= 899) return "5 a 7 dias úteis";

            // PR
            if (faixa >= 800 && faixa <= 809) return "5 a 7 dias úteis";
            if (faixa >= 810 && faixa <= 839) return "6 a 8 dias úteis";
            if (faixa >= 840 && faixa <= 859) return "6 a 8 dias úteis";
            if (faixa >= 860 && faixa <= 879) return "5 a 7 dias úteis";

            // SP
            if (faixa >= 100 && faixa <= 599) return "6 a 8 dias úteis";
            if (faixa >= 600 && faixa <= 999) return "6 a 8 dias úteis";
            if (faixa >= 110 && faixa <= 139) return "7 a 9 dias úteis";
            if (faixa >= 140 && faixa <= 159) return "6 a 8 dias úteis";
            if (faixa >= 160 && faixa <= 199) return "5 a 7 dias úteis";

            // RJ
            if (faixa >= 200 && faixa <= 289) return "7 a 9 dias úteis";

            // MG
            if (faixa >= 300 && faixa <= 319) return "8 a 10 dias úteis";
            if (faixa >= 320 && faixa <= 349) return "7 a 9 dias úteis";
            if (faixa >= 350 && faixa <= 359) return "7 a 9 dias úteis";
            if (faixa >= 360 && faixa <= 399) return "7 a 9 dias úteis";

            // ES
            if (faixa >= 290 && faixa <= 299) return "8 a 10 dias úteis";

            // DF, GO
            if (faixa >= 700 && faixa <= 769) return "8 a 10 dias úteis";

            // MT, MS
            if (faixa >= 780 && faixa <= 799) return "9 a 11 dias úteis";

            // BA
            if (faixa >= 400 && faixa <= 489) return "9 a 11 dias úteis";

            // PE, PB, RN, AL, SE, PI, MA
            if (faixa >= 490 && faixa <= 659) return "10 a 12 dias úteis";

            // CE
            if (faixa >= 600 && faixa <= 639) return "10 a 12 dias úteis";

            // Norte
            if (faixa >= 660 && faixa <= 699) return "12 a 15 dias úteis";

            // TO
            if (faixa >= 770 && faixa <= 779) return "9 a 11 dias úteis";

            // RO
            if (faixa >= 768 && faixa <= 769) return "11 a 13 dias úteis";

            // Fallback
            return "5 a 7 dias úteis";
        }

        // ==========================================
        // TABELA DE FRETE POR UF (FALLBACK)
        // ==========================================
        private double calcularFretePorUF(String uf) {
            if (uf == null || uf.isEmpty()) return 35.90;
            switch (uf.toUpperCase()) {
                case "RS": return 15.90;
                case "PR": case "SC": case "SP": case "RJ": case "MG": case "ES":
                    return 25.90;
                case "DF": case "GO": case "MT": case "MS":
                case "BA": case "SE": case "AL": case "PE": case "PB": case "RN": case "CE": case "PI": case "MA":
                    return 40.90;
                case "PA": case "AM": case "AC": case "RR": case "RO": case "AP": case "TO":
                    return 55.90;
                default: return 35.90;
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
    // HANDLER: VERIFICAR DISPONIBILIDADE
    // ==========================================
    static class VerificarDisponibilidadeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // ✅ CORS garantido
                return;
            }

            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);

                JsonObject json = gson.fromJson(body, JsonObject.class);
                String codPeca = json.get("codPeca").getAsString();

                boolean disponivel = verificarDisponibilidade(codPeca);

                Map<String, Object> response = new HashMap<>();
                response.put("disponivel", disponivel);
                response.put("codPeca", codPeca);

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        private boolean verificarDisponibilidade(String codPeca) {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                con = ConnectionDB.getConnectionCloud();
                String sql = "SELECT status, quantidade FROM estoque WHERE codpeca = ?";
                stmt = con.prepareStatement(sql);
                stmt.setString(1, codPeca);
                stmt.setQueryTimeout(10);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    int quantidade = rs.getInt("quantidade");
                    return "DISPONIVEL".equals(status) && quantidade > 0;
                }

            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("❌ Erro ao verificar disponibilidade: " + e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (con != null) con.close(); } catch (SQLException e) {}
            }

            return false;
        }
    }

    // ==========================================
    // HANDLER: LISTAR PRODUTOS (API PARA VERCEL)
    // ==========================================
    static class ListarProdutosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // ✅ CORS garantido
                return;
            }

            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                return;
            }

            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                con = ConnectionDB.getConnectionCloud();
                String sql = "SELECT codpeca, itemdesc, marca, tamanho, precosug, imagem, status, quantidade, data " +
                     "FROM estoque WHERE status = 'DISPONIVEL' AND data >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) ORDER BY itemdesc ASC";
                stmt = con.prepareStatement(sql);
                stmt.setQueryTimeout(10);
                rs = stmt.executeQuery();

                List<Map<String, Object>> produtos = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> p = new HashMap<>();
                    p.put("codpeca", rs.getString("codpeca"));
                    p.put("itemdesc", rs.getString("itemdesc"));
                    p.put("tamanho", rs.getString("tamanho"));
                    p.put("precosug", rs.getDouble("precosug"));
                    p.put("imagem", rs.getString("imagem"));
                    p.put("marca", rs.getString("marca"));
                    p.put("quantidade", rs.getInt("quantidade"));
                    p.put("status", rs.getString("status"));
                    p.put("dataCriacao", rs.getDate("data"));
                    produtos.add(p);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("produtos", produtos);
                response.put("total", produtos.size());

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (ClassNotFoundException | SQLException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (con != null) con.close(); } catch (SQLException e) {}
            }
        }
    }

    // ==========================================
    // HANDLER: RESERVAR LOTE (MÚLTIPLOS ITENS)
    // ==========================================
    static class ReservarLoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // ✅ CORS garantido
                return;
            }

            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().reduce("", (a, b) -> a + b);

                System.out.println("📥 [RESERVA-LOTE] Body recebido: " + body);

                JsonObject json = gson.fromJson(body, JsonObject.class);
                String pedidoId = json.get("pedidoId").getAsString();
                com.google.gson.JsonArray itensArray = json.getAsJsonArray("itens");
                
                String emailCliente = json.has("email") ? json.get("email").getAsString() : "";

                List<String> codPecas = new ArrayList<>();
                for (int i = 0; i < itensArray.size(); i++) {
                    codPecas.add(itensArray.get(i).getAsString());
                }

                System.out.println("📦 [RESERVA-LOTE] Pedido: " + pedidoId);
                System.out.println("📦 [RESERVA-LOTE] Itens: " + codPecas);
                System.out.println("📦 [RESERVA-LOTE] Email: " + emailCliente);

                boolean reservado = reservarLote(codPecas, pedidoId, emailCliente);
                
                if (reservado) {
                    // 🔥 GERA O NOVO HTML AUTOMATICAMENTE
//                    new Thread(() -> {
//                        try {
//                            System.out.println("   🌐 Gerando novo site...");
//                            GerarEEnviar.main(new String[0]);
//                            System.out.println("   ✅ Site gerado com sucesso!");
//                        } catch (Exception e) {
//                            System.err.println("   ❌ Erro ao gerar site: " + e.getMessage());
//                        }
//                    }).start();
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", reservado);
                response.put("pedidoId", pedidoId);
                response.put("mensagem", reservado ? "Itens reservados com sucesso!" : "Falha ao reservar itens!");
                response.put("itens", codPecas);

                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

//        private static boolean reservarLote(List<String> codPecas, String pedidoId, String emailCliente) {
//            System.out.println("📥 [RESERVA-LOTE] INICIANDO RESERVA..."); // 🔥 LOG 1
//            Connection con = null;
//            PreparedStatement stmt = null;
//            ResultSet rs = null;
//
//            try {
//                System.out.println("📥 [RESERVA-LOTE] Tentando conectar ao banco..."); // 🔥 LOG 2
//                con = ConnectionDB.getConnectionCloud();
//                System.out.println("✅ [RESERVA-LOTE] Conectado com sucesso!"); // 🔥 LOG 3
//
//                con.setAutoCommit(false);
//                System.out.println("✅ [RESERVA-LOTE] AutoCommit desabilitado!"); // 🔥 LOG 4
//
//                // 🔥 1. Verifica se todos os itens estão disponíveis
//                for (String codPeca : codPecas) {
//                    System.out.println("🔍 [RESERVA-LOTE] Verificando item: " + codPeca); // 🔥 LOG 5
//                    String sqlCheck = "SELECT status, quantidade FROM estoque WHERE codpeca = ? AND status = 'DISPONIVEL'";
//                    stmt = con.prepareStatement(sqlCheck);
//                    stmt.setString(1, codPeca);
//                    rs = stmt.executeQuery();
//
//                    if (!rs.next()) {
//                        System.out.println("❌ [RESERVA-LOTE] Item NÃO DISPONÍVEL: " + codPeca); // 🔥 LOG 6
//                        con.rollback();
//                        return false;
//                    }
//
//                    int qtd = rs.getInt("quantidade");
//                    System.out.println("   📝 Quantidade: " + qtd); // 🔥 LOG 7
//                    if (qtd < 1) {
//                        System.out.println("❌ [RESERVA-LOTE] Estoque insuficiente: " + codPeca); // 🔥 LOG 8
//                        con.rollback();
//                        return false;
//                    }
//                }
//
//                // 🔥 2. Atualiza estoque para RESERVADO
//                for (String codPeca : codPecas) {
//                    System.out.println("🔄 [RESERVA-LOTE] Atualizando estoque: " + codPeca); // 🔥 LOG 9
//                    String sqlUpdate = "UPDATE estoque SET status = 'RESERVADO', quantidade = quantidade - 1 WHERE codpeca = ?";
//                    stmt = con.prepareStatement(sqlUpdate);
//                    stmt.setString(1, codPeca);
//                    int rows = stmt.executeUpdate();
//                    System.out.println("   📝 Atualizadas " + rows + " linha(s)"); // 🔥 LOG 10
//                }
//
//                // 🔥 3. Insere na tabela reservas
//                String codPecaStr = String.join(",", codPecas);
//                System.out.println("📝 [RESERVA-LOTE] Inserindo reserva: " + codPecaStr); // 🔥 LOG 11
//
//                String sqlReserva = "INSERT INTO reservas (cod_peca, pedido_id, email, quantidade, data_reserva, status) VALUES (?, ?, ?, ?, NOW(), 'RESERVADO')";
//                stmt = con.prepareStatement(sqlReserva);
//                stmt.setString(1, codPecaStr);
//                stmt.setString(2, pedidoId);
//                stmt.setString(3, emailCliente);   // 🔥 NOVO
//                stmt.setInt(4, codPecas.size());
//                int rows = stmt.executeUpdate();
//                System.out.println("   📝 Inseridas " + rows + " linha(s) na reserva"); // 🔥 LOG 12
//
//                con.commit();
//                System.out.println("✅ [RESERVA-LOTE] Reserva finalizada com sucesso!"); // 🔥 LOG 13
//                return true;
//
//            } catch (ClassNotFoundException | SQLException e) {
//                System.err.println("❌ [RESERVA-LOTE] ERRO: " + e.getMessage());
//                // 🔥 IMPRIME O ERRO COMPLETO
//                try { if (con != null) con.rollback(); } catch (SQLException ex) {System.out.println("❌ Erro: "+ex);}
//                return false;
//            } finally {
//                try { if (rs != null) rs.close(); } catch (SQLException ex) {System.err.println("❌ Erro: "+ex);}
//                try { if (stmt != null) stmt.close(); } catch (SQLException ex) {System.err.println("❌ Erro: "+ex);}
//                try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException ex) {System.err.println("❌ Erro: "+ex);}
//            }
//        }
    }
    
    private static boolean reservarLote(List<String> codPecas, String pedidoId, String emailCliente) {
        System.out.println("📥 [RESERVA-LOTE] INICIANDO RESERVA..."); // 🔥 LOG 1
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            System.out.println("📥 [RESERVA-LOTE] Tentando conectar ao banco..."); // 🔥 LOG 2
            con = ConnectionDB.getConnectionCloud();
            System.out.println("✅ [RESERVA-LOTE] Conectado com sucesso!"); // 🔥 LOG 3

            con.setAutoCommit(false);
            System.out.println("✅ [RESERVA-LOTE] AutoCommit desabilitado!"); // 🔥 LOG 4

            // 🔥 1. Verifica se todos os itens estão disponíveis
            for (String codPeca : codPecas) {
                System.out.println("🔍 [RESERVA-LOTE] Verificando item: " + codPeca); // 🔥 LOG 5
                String sqlCheck = "SELECT status, quantidade FROM estoque WHERE codpeca = ? AND status = 'DISPONIVEL'";
                stmt = con.prepareStatement(sqlCheck);
                stmt.setString(1, codPeca);
                rs = stmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ [RESERVA-LOTE] Item NÃO DISPONÍVEL: " + codPeca); // 🔥 LOG 6
                    con.rollback();
                    return false;
                }

                int qtd = rs.getInt("quantidade");
                System.out.println("   📝 Quantidade: " + qtd); // 🔥 LOG 7
                if (qtd < 1) {
                    System.out.println("❌ [RESERVA-LOTE] Estoque insuficiente: " + codPeca); // 🔥 LOG 8
                    con.rollback();
                    return false;
                }
            }

            // 🔥 2. Atualiza estoque para RESERVADO
            for (String codPeca : codPecas) {
                System.out.println("🔄 [RESERVA-LOTE] Atualizando estoque: " + codPeca); // 🔥 LOG 9
                String sqlUpdate = "UPDATE estoque SET status = 'RESERVADO', quantidade = quantidade - 1 WHERE codpeca = ?";
                stmt = con.prepareStatement(sqlUpdate);
                stmt.setString(1, codPeca);
                int rows = stmt.executeUpdate();
                System.out.println("   📝 Atualizadas " + rows + " linha(s)"); // 🔥 LOG 10
            }

            // 🔥 3. Insere na tabela reservas
            String codPecaStr = String.join(",", codPecas);
            System.out.println("📝 [RESERVA-LOTE] Inserindo reserva: " + codPecaStr); // 🔥 LOG 11

            String sqlReserva = "INSERT INTO reservas (cod_peca, pedido_id, email, quantidade, data_reserva, status) VALUES (?, ?, ?, ?, NOW(), 'RESERVADO')";
            stmt = con.prepareStatement(sqlReserva);
            stmt.setString(1, codPecaStr);
            stmt.setString(2, pedidoId);
            stmt.setString(3, emailCliente);   // 🔥 NOVO
            stmt.setInt(4, codPecas.size());
            int rows = stmt.executeUpdate();
            System.out.println("   📝 Inseridas " + rows + " linha(s) na reserva"); // 🔥 LOG 12

            con.commit();
            System.out.println("✅ [RESERVA-LOTE] Reserva finalizada com sucesso!"); // 🔥 LOG 13
            return true;

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ [RESERVA-LOTE] ERRO: " + e.getMessage());
            // 🔥 IMPRIME O ERRO COMPLETO
            try { if (con != null) con.rollback(); } catch (SQLException ex) {System.out.println("❌ Erro: "+ex);}
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ex) {System.err.println("❌ Erro: "+ex);}
            try { if (stmt != null) stmt.close(); } catch (SQLException ex) {System.err.println("❌ Erro: "+ex);}
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException ex) {System.err.println("❌ Erro: "+ex);}
        }
    }

    // ==========================================
    // HANDLER: NOTIFICAR SISTEMA DESKTOP
    // ==========================================
    static class NotificarSistemaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // ✅ CORS garantido
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

                // Extrai os campos básicos
                String codPeca = json.get("codPeca").getAsString();
                String nomeCliente = json.get("destinatario").getAsString();
                String meioPagamento = json.get("meio").getAsString();
                String endereco = json.get("endereco").getAsString();
                boolean retirarLoja = json.has("retirarLoja") && json.get("retirarLoja").getAsBoolean();
                String pedidoId = json.get("pedidoId").getAsString();
                String telefone = json.has("telefone") ? json.get("telefone").getAsString() : "Não informado";
                String itens = json.get("itens").toString();
                String emailCliente = json.has("email") ? json.get("email").getAsString() : "Não informado";

                // 🔥 Valores monetários – sempre lidos do JSON, com fallback seguro
                double total = json.get("total").getAsDouble();
                double frete = json.has("frete") ? json.get("frete").getAsDouble() : 0.0;
                double subtotal = json.has("subtotal") ? json.get("subtotal").getAsDouble() : (total - frete);

                // 🔥 Logs claros e diretos
                System.out.println("📝 Dados da venda:");
                System.out.println("   Cliente: " + nomeCliente);
                System.out.println("   Telefone: " + telefone);
                System.out.println("   Email: " + emailCliente);
                System.out.println("   Subtotal (itens): R$ " + String.format("%.2f", subtotal));
                System.out.println("   Frete: R$ " + String.format("%.2f", frete));
                System.out.println("   Total: R$ " + String.format("%.2f", total));
                System.out.println("   Peça: " + codPeca);
                System.out.println("   Retirar na loja: " + (retirarLoja ? "SIM" : "NÃO"));

                // 🔥 PASSA O EMAIL PARA O MÉTODO DE SALVAR
                salvarNotificacaoNoBanco(codPeca, nomeCliente, emailCliente, subtotal, frete, meioPagamento,
                        retirarLoja, endereco, pedidoId, telefone, itens);
                
                // 🔥 Envia e-mail para o cliente avisando que o pedido foi recebido
                try {
                    EmailServiceSendGrid.enviarPedidoRecebidoCliente(emailCliente, nomeCliente, pedidoId, subtotal, frete, total, itens);
                    System.out.println("   ✅ E-mail de pedido recebido enviado para o cliente: " + emailCliente);
                } catch (Exception e) {
                    System.err.println("   ❌ Erro ao enviar e-mail de pedido recebido: " + e.getMessage());
                }

                // 🔥 Envia e-mail para a loja avisando do novo pedido pago
                try {
                    EmailServiceSendGrid.enviarNovaVendaParaLoja(pedidoId, nomeCliente, emailCliente, telefone, total, frete, meioPagamento, retirarLoja, endereco, itens);
                    System.out.println("   ✅ E-mail enviado para a loja");
                } catch (Exception e) {
                    System.err.println("   ❌ Erro ao enviar e-mail para loja: " + e.getMessage());
                }

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

        private void salvarNotificacaoNoBanco(String codPeca, String cliente, String emailCliente, double valor, double frete, String meioPagamento, boolean retirarLoja, String endereco, String pedidoId, String telefone, String itens) {
            Connection con = null;
            PreparedStatement stmt = null;

            try {
                con = ConnectionDB.getConnectionCloud();

                String sql = "INSERT INTO notificacoes_pendentes " +
                        "(pedido_id, cod_peca, cliente, telefone, email, valor, frete, meio_pagamento, " +
                        "endereco, retirar_loja, itens, data_criacao, status, lida) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'PENDENTE', 0)";
                
                stmt = con.prepareStatement(sql);

                stmt.setString(1, pedidoId);
                stmt.setString(2, codPeca);
                stmt.setString(3, cliente);
                stmt.setString(4, telefone);
                stmt.setString(5, emailCliente);
                stmt.setDouble(6, valor);
                stmt.setDouble(7, frete);
                stmt.setString(8, meioPagamento);
                stmt.setString(9, endereco);
                stmt.setBoolean(10, retirarLoja);
                stmt.setString(11, itens);

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

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // ✅ CORS garantido
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
    // HANDLER: CONFIRMAR PEDIDO (LOJA)
    // ==========================================
    static class ConfirmarPedidoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // ✅ CORS garantido
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

                JsonObject json = gson.fromJson(body, JsonObject.class);
                String pedidoId = json.get("pedidoId").getAsString();
                String status = json.get("status").getAsString(); // "RETIRADA" ou "DESPACHADO"
                String emailCliente = json.get("email").getAsString();
                String nomeCliente = json.get("nome").getAsString();

                // 🔥 Atualiza o status no banco (opcional)
//                atualizarStatusPedido(pedidoId, status);

                // 🔥 Envia e-mail para o cliente
                String assunto = "📦 Pedido #" + pedidoId + " - PORTOBELLA";
                String corpo;
                if ("RETIRADA".equals(status)) {
                    corpo = "Olá " + nomeCliente + ",\n\n" +
                            "✅ Seu pedido está disponível para retirada na loja!\n" +
                            "📍 Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS\n" +
                            "🕐 Horário: Segunda a Sexta, 10h às 18h\n\n" +
                            "Obrigado por comprar na PORTOBELLA! 💛";
                } else if ("DESPACHADO".equals(status)) {
                    corpo = "Olá " + nomeCliente + ",\n\n" +
                            "🚚 Seu pedido foi despachado para o endereço informado!\n" +
                            "📦 O código de rastreio será enviado em breve.\n\n" +
                            "Obrigado por comprar na PORTOBELLA! 💛";
                } else {
                    corpo = "Olá " + nomeCliente + ",\n\n" +
                            "Status do seu pedido #" + pedidoId + " foi atualizado para: " + status + ".\n\n" +
                            "Obrigado por comprar na PORTOBELLA! 💛";
                }

                EmailService.enviarCupomAssincrono(emailCliente, assunto, corpo);
                System.out.println("   ✅ E-mail enviado para o cliente: " + emailCliente);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("mensagem", "Pedido confirmado e cliente notificado por e-mail!");
                sendResponse(exchange, 200, gson.toJson(response));

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }

        private void atualizarStatusPedido(String pedidoId, String status) {
            // 🔥 Atualiza a tabela notificacoes_pendentes ou vendas
            // Exemplo:
            // UPDATE notificacoes_pendentes SET status_loja = ? WHERE pedido_id = ?
            System.out.println("📝 Atualizando status do pedido " + pedidoId + " para " + status);
            // Você pode implementar com ConnectionDB
        }
    }

    // ==========================================
    // HANDLER: CRIAR PAGAMENTO
    // ==========================================
    static class CriarPagamentoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // 🔥 Passa pelo sendResponse, que tem CORS
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
                String email = params.getOrDefault("email", "cliente@email.com");
                String telefone = params.getOrDefault("telefone", "51999999999");

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
                    String pedidoId = String.valueOf(System.currentTimeMillis());
                    String linkPagamento = criarLinkMercadoPago(produtoId, nome, valorTotal, pedidoId, email, nome, telefone);
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
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // 🔥 Passa pelo sendResponse, que tem CORS
                return;
            }

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
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, ""); // ✅ CORS garantido
                return;
            }
            try {
                String method = exchange.getRequestMethod();
//                String query = exchange.getRequestURI().getQuery();

                // 🔥 1. Se for GET (teste do MP), processa os parâmetros da URL
//                String method = exchange.getRequestMethod();
                if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
                    sendResponse(exchange, 200, "{\"status\":\"ok\"}");
                    return;
                }

                // 🔥 2. Se for POST, processa o corpo JSON
                if ("POST".equalsIgnoreCase(method)) {
                    String body = new BufferedReader(
                            new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                            .lines().reduce("", (a, b) -> a + b);

                    System.out.println("📢 Webhook POST recebido: " + body);

                    // Se o corpo estiver vazio, retorna 200 (pode ser heartbeat)
                    if (body == null || body.trim().isEmpty()) {
                        sendResponse(exchange, 200, "{\"status\":\"ok\"}");
                        return;
                    }

                    JsonObject notification = gson.fromJson(body, JsonObject.class);
                    String type = notification.get("type").getAsString();

                    if ("payment".equals(type)) {
                        String paymentId = notification.get("data").getAsJsonObject().get("id").getAsString();
                        System.out.println("   🔍 Payment ID: " + paymentId);

                        String status = consultarStatusPagamento(paymentId);
                        System.out.println("   📊 Status: " + status);

                        if ("approved".equals(status)) {
                            System.out.println("   ✅ Pagamento aprovado! Finalizando pedido...");
                            finalizarPedido(paymentId);
                        } else {
                            System.out.println("   ⚠️ Pagamento com status: " + status + " (não processado)");
                        }
                    } else {
                        System.out.println("   ℹ️ Notificação ignorada (tipo: " + type + ")");
                    }

                    sendResponse(exchange, 200, "{\"status\":\"ok\"}");
                    return;
                }

                // Se não for GET nem POST, retorna 405 (Method Not Allowed)
                sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");

            } catch (JsonSyntaxException e) {
                System.err.println("❌ Erro ao parsear JSON: " + e.getMessage());
                sendResponse(exchange, 200, "{\"status\":\"ok\"}");
            } catch (IOException e) {
                System.err.println("❌ Erro no webhook: " + e.getMessage());
                sendResponse(exchange, 200, "{\"status\":\"ok\"}");
            }
        }

//        private String consultarStatusPagamento(String paymentId) {
//            try {
//                String url = "https://api.mercadopago.com/v1/payments/" + paymentId;
//                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
//                conn.setRequestMethod("GET");
//                conn.setRequestProperty("Authorization", "Bearer " + TOKEN_MP);
//
//                int responseCode = conn.getResponseCode();
//                if (responseCode == 200) {
//                    try (BufferedReader in = new BufferedReader(
//                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
//                        StringBuilder response = new StringBuilder();
//                        String line;
//                        while ((line = in.readLine()) != null) {
//                            response.append(line);
//                        }
//                        JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
//                        return json.get("status").getAsString();
//                    }
//                } else {
//                    System.err.println("   ❌ Erro ao consultar pagamento. Código: " + responseCode);
//                    return null;
//                }
//            } catch (JsonSyntaxException | IOException e) {
//                System.err.println("   ❌ Erro ao consultar pagamento: " + e.getMessage());
//                return null;
//            }
//        }

        private void finalizarPedido(String paymentId) {
            // Consulta o pagamento e extrai o external_reference
            String pedidoId = buscarExternalReference(paymentId);
            if (pedidoId == null) {
                System.err.println("   ❌ Não foi possível obter o pedidoId para paymentId: " + paymentId);
                return;
            }
            System.out.println("   🆔 Pedido ID: " + pedidoId);

            System.out.println("   🚀 Finalizando pedido com paymentId: " + paymentId);
            boolean finalizado = PagamentoServer.finalizarPedidoPorPedidoId(pedidoId);
            if (finalizado) {
                System.out.println("   ✅ Pedido " + pedidoId + " finalizado com sucesso via webhook.");
            } else {
                System.err.println("   ❌ Falha ao finalizar pedido " + pedidoId + " via webhook.");
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
    private static String criarLinkMercadoPago(String codPeca, String titulo, double valor, String pedidoId, 
                                            String emailCliente, String nomeCliente, String telefone){
        try {
            String precoFormatado = String.format(java.util.Locale.US, "%.2f", valor);

            String jsonPayload = "{"
                    + "\"items\": [{"
                    + "\"id\": \"" + codPeca + "\","
                    + "\"title\": \"" + titulo + "\","
                    + "\"description\": \"" + titulo + "\","
                    + "\"quantity\": 1,"
                    + "\"currency_id\": \"BRL\","
                    + "\"unit_price\": " + precoFormatado
                    + "}],"
                    + "\"payer\": {"
                    + "\"email\": \"" + emailCliente + "\","
                    + "\"name\": \"" + nomeCliente + "\","
                    + "\"phone\": {"
                    + "\"area_code\": \"51\","
                    + "\"number\": \"" + telefone.replaceAll("\\D", "") + "\""
                    + "}"
                    + "},"
                    + "\"external_reference\": \"" + pedidoId + "\","
                    + "\"back_urls\": {"
                    + "\"success\": \"https://portobellavitrine.com?status=approved\","
                    + "\"failure\": \"https://portobellavitrine.com?status=failure\","
                    + "\"pending\": \"https://portobellavitrine.com?status=pending\""
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
    
    private static String buscarExternalReference(String paymentId) {
        try {
            String url = "https://api.mercadopago.com/v1/payments/" + paymentId;
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + TOKEN_MP);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
                    // Extrai o campo external_reference
                    if (json.has("external_reference") && !json.get("external_reference").isJsonNull()) {
                        return json.get("external_reference").getAsString();
                    }
                }
            } else {
                System.err.println("   ❌ Erro ao consultar pagamento. Código: " + responseCode);
            }
        } catch (JsonSyntaxException | IOException e) {
            System.err.println("   ❌ Erro ao buscar external_reference: " + e.getMessage());
        }
        return null;
    }
    
    // ==========================================
    // HANDLER: FINALIZAR MP (FALLBACK)
    // ==========================================
    static class FinalizarMercadoPagoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
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

                JsonObject json = gson.fromJson(body, JsonObject.class);
                String paymentId = json.get("paymentId").getAsString();
                String pedidoId = json.get("pedidoId").getAsString();

                System.out.println("📥 [FALLBACK-MP] Payment ID: " + paymentId);
                System.out.println("📥 [FALLBACK-MP] Pedido ID: " + pedidoId);

                // 🔥 CONSULTA O STATUS DO PAGAMENTO
                String status = consultarStatusPagamento(paymentId);
                System.out.println("📊 [FALLBACK] Status: " + status);

                if (!"approved".equals(status)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", "Pagamento não aprovado. Status: " + status);
                    sendResponse(exchange, 200, gson.toJson(error)); // 🔥 AQUI ERA json.toJson
                    return;
                }

                // 🔥 FINALIZA O PEDIDO
                boolean finalizado = finalizarPedidoPorPedidoId(pedidoId);

                Map<String, Object> response = new HashMap<>();
                response.put("success", finalizado);
                response.put("mensagem", finalizado ? "Pedido finalizado com sucesso!" : "Erro ao finalizar pedido.");
                sendResponse(exchange, 200, gson.toJson(response)); // 🔥 AQUI ERA json.toJson

            } catch (JsonSyntaxException | IOException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                sendResponse(exchange, 500, gson.toJson(error));
            }
        }
    }
    
    private static boolean finalizarPedidoPorPedidoId(String pedidoId) {
        DadosPedido dados = pedidosPendentes.remove(pedidoId);
        if (dados == null) {
            System.err.println("❌ Dados do pedido não encontrados para: " + pedidoId);
            return false;
        }

        try {
            // 🔥 EXTRAI OS CÓDIGOS DOS ITENS
            List<String> codPecas = dados.itens.stream()
                    .map(item -> (String) item.get("id"))
                    .collect(Collectors.toList());

            // 🔥 CHAMA A RESERVA (REUTILIZA A LÓGICA DO RESERVAR-LOTE)
            boolean reservado = reservarLote(codPecas, pedidoId, dados.email);
            if (!reservado) {
                System.err.println("❌ Falha ao reservar itens para o pedido " + pedidoId);
                return false;
            }

            // 🔥 NOTIFICA A LOJA (REUTILIZA A LÓGICA DO NOTIFICAR)
            notificarLoja(dados, pedidoId);
            
            // 🔥 ADICIONE A THREAD AQUI – APÓS A NOTIFICAÇÃO E ANTES DO RETURN
//            new Thread(() -> {
//                try {
//                    System.out.println("   🌐 Gerando e enviando site (MP)...");
//                    GerarEEnviar.main(new String[0]);
//                    System.out.println("   ✅ Site gerado e enviado com sucesso (MP)!");
//                } catch (Exception e) {
//                    System.err.println("   ❌ Erro ao gerar/enviar site (MP): " + e.getMessage());
//                }
//            }).start();

            System.out.println("✅ Pedido " + pedidoId + " finalizado com sucesso (fallback)");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao finalizar pedido " + pedidoId + ": " + e.getMessage());
            return false;
        }
    }
    
    private static String consultarStatusPagamento(String paymentId) {
        try {
            String url = "https://api.mercadopago.com/v1/payments/" + paymentId;
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + TOKEN_MP);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
                    return json.get("status").getAsString();
                }
            } else {
                System.err.println("   ❌ Erro ao consultar pagamento. Código: " + responseCode);
                return null;
            }
        } catch (JsonSyntaxException | IOException e) {
            System.err.println("   ❌ Erro ao consultar pagamento: " + e.getMessage());
            return null;
        }
    }
    
    private static void notificarLoja(DadosPedido dados, String pedidoId) {
        try {
            // 🔥 PREPARA OS DADOS PARA NOTIFICAÇÃO
            Map<String, Object> notificacao = new HashMap<>();
            notificacao.put("codPeca", dados.itens.stream()
                    .map(i -> (String) i.get("id"))
                    .collect(Collectors.joining(",")));
            notificacao.put("destinatario", dados.destinatario);
            notificacao.put("telefone", dados.telefone);
            notificacao.put("email", dados.email);
            notificacao.put("total", dados.total);
            notificacao.put("subtotal", dados.subtotal);
            notificacao.put("frete", dados.frete);
            notificacao.put("meio", "mercado_pago");
            notificacao.put("endereco", dados.endereco);
            notificacao.put("retirarLoja", dados.retirarLoja);
            notificacao.put("pedidoId", pedidoId);
            notificacao.put("itens", dados.itens);

            // 🔥 CHAMA O MESMO MÉTODO QUE O NOTIFICARSISTEMAHANDLER USA
            // Você pode extrair a lógica de salvarNotificacaoNoBanco e enviar e-mails
            // para um método utilitário, ou chamar diretamente o handler (não é trivial)
            // Por simplicidade, reimplemente a chamada:

            Connection con = ConnectionDB.getConnectionCloud();
            String sql = "INSERT INTO notificacoes_pendentes " +
                    "(pedido_id, cod_peca, cliente, telefone, email, valor, frete, meio_pagamento, " +
                    "endereco, retirar_loja, itens, data_criacao, status, lida) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'PENDENTE', 0)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, pedidoId);
            stmt.setString(2, (String) notificacao.get("codPeca"));
            stmt.setString(3, dados.destinatario);
            stmt.setString(4, dados.telefone);
            stmt.setString(5, dados.email);
            stmt.setDouble(6, dados.total);
            stmt.setDouble(7, dados.frete);
            stmt.setString(8, "mercado_pago");
            stmt.setString(9, dados.endereco);
            stmt.setBoolean(10, dados.retirarLoja);
            stmt.setString(11, gson.toJson(dados.itens));
            stmt.executeUpdate();
            stmt.close();
            con.close();

            // 🔥 ENVIA E-MAILS
            EmailServiceSendGrid.enviarPedidoRecebidoCliente(dados.email, dados.destinatario, pedidoId,
                    dados.subtotal, dados.frete, dados.total, gson.toJson(dados.itens));
            EmailServiceSendGrid.enviarNovaVendaParaLoja(pedidoId, dados.destinatario, dados.email,
                    dados.telefone, dados.total, dados.frete, "mercado_pago", dados.retirarLoja,
                    dados.endereco, gson.toJson(dados.itens));

            System.out.println("   ✅ Loja notificada para o pedido " + pedidoId);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("   ❌ Erro ao notificar loja: " + e.getMessage());
        }
    }
    
//    private List<Map<String, Object>> converterItensArray(JsonArray itensArray) {
//        List<Map<String, Object>> lista = new ArrayList<>();
//        for (int i = 0; i < itensArray.size(); i++) {
//            JsonObject item = itensArray.get(i).getAsJsonObject();
//            Map<String, Object> map = new HashMap<>();
//            map.put("id", item.get("id").getAsString());
//            map.put("nome", item.get("nome").getAsString());
//            map.put("preco", item.get("preco").getAsDouble());
//            map.put("quantidade", item.get("quantidade").getAsInt());
//            lista.add(map);
//        }
//        return lista;
//    }

    // ==========================================
    // PARSE QUERY PARAMS (AUXILIAR)
    // ==========================================
    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return params;
        }

        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                try {
                    params.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], "UTF-8"));
                } catch (java.io.UnsupportedEncodingException e) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }

        return params;
    }

    // ==========================================
    // SEND RESPONSE (AUXILIAR)
    // ==========================================
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        // 🔥 CORS em todas as respostas
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ==========================================
    // MAIN (INICIAR SERVIDOR)
    // ==========================================
    public static void main(String[] args) {
        try {
             // 🔥 VALIDAÇÃO DA VARIÁVEL DE AMBIENTE
            String token = System.getenv("MP_ACCESS_TOKEN");
            if (token == null || token.isEmpty()) {
                System.err.println("❌ ERRO: MP_ACCESS_TOKEN não definido!");
                System.err.println("   Configure a variável de ambiente no Render.");
                System.err.println("   Exemplo: MP_ACCESS_TOKEN=APP_USR-xxxxxxxxxxxx");
                System.exit(1); // Encerra a aplicação
            }
            // Atualiza a constante (se você estiver usando uma variável estática)
            TOKEN_MP = token; // Se TOKEN_MP for static, pode ser atribuído aqui
            GerarSiteEstoque.main(new String[0]);
            iniciar();
            System.out.println("\n✅ Servidor de pagamentos rodando em http://localhost:8080");
            System.out.println("   🔥 PIX: " + CHAVE_PIX);
            System.out.println("   💳 MERCADO PAGO: Link de pagamento");
            System.out.println("   📦 Frete: Cálculo por CEP");
            System.out.println("   🔔 Notificações: /api/pagamentos/notificar");
            System.out.println("   🔍 Consultar: /api/pagamentos/consultar");
            System.out.println("   🔒 Reservar: /api/pagamentos/reservar-lote");
            System.out.println("   🔓 Liberar: /api/pagamentos/liberar-reserva");
            System.out.println("   📦 Produtos: /api/produtos");
            
            System.out.println("\n🚀 Servidor de pagamentos iniciado com sucesso!");
            System.out.println("📌 Servidor rodando... (Ctrl+C para parar)");
            
            // 🔥 Mantém o servidor rodando sem depender de input do usuário
            Thread.currentThread().join();
            
        } catch (IOException e) {
            System.err.println("❌ Erro ao iniciar servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("⚠️ Servidor interrompido");
            Thread.currentThread().interrupt();
        }
    }
}