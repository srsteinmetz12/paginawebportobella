package util;

import connection.ConnectionDB;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pagamentos")
@CrossOrigin(origins = "*")
public class PagamentoController {

    // ==========================================
    // CONFIGURAÇÕES
    // ==========================================
    
    // 🔥 SUA CHAVE PIX (para gerar QR Code SEM Mercado Pago)
    private static final String CHAVE_PIX = "portobella.brecho@gmail.com"; // Coloque sua chave Pix aqui
    
    // 🔥 SEU NOME (para o QR Code)
    private static final String NOME_RECEBEDOR = "Vanderleia Vieira Moraes Lemos Steinmetz";
    
    // 🔥 TOKEN DO MERCADO PAGO (SOMENTE para o link de pagamento)
    private static final String TOKEN_MP = "APP_USR-5504079628127234-061707-4f72faca8cd75c397d89abc34651960f-3480421128";
    
    private final Gson gson = new Gson();

    // ==========================================
    // ENDPOINT: CRIAR PAGAMENTO
    // ==========================================
    @GetMapping("/criar")
    public void criarPagamento(
            @RequestParam String meio,
            @RequestParam String id,
            @RequestParam(required = false, defaultValue = "0") double preco,
            @RequestParam(required = false, defaultValue = "0") double frete,
            @RequestParam(required = false) String nome,
            HttpServletResponse response) throws IOException {

        System.out.println("📝 Criando pagamento para: " + id + " | Meio: " + meio);

        // Busca os dados do produto
        double precoProduto = preco > 0 ? preco : obterPrecoDoBancoCloud(id);
        double valorTotal = precoProduto + frete;
        String nomeProduto = (nome != null && !nome.isEmpty()) ? nome : obterNomeDoBancoCloud(id);
        if (nomeProduto == null || nomeProduto.isEmpty()) {
            nomeProduto = "Produto #" + id;
        }

        // Registra a venda no banco
        registrarPreVendaNaAiven(id, valorTotal, frete, "Cliente");

        try {
            if ("pix".equalsIgnoreCase(meio)) {
                // ==========================================
                // ✅ PIX: Gera QR Code SEM Mercado Pago
                // ==========================================
                String payloadPix = gerarPayloadPix(valorTotal, nomeProduto);
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                
                Map<String, Object> jsonResponse = new HashMap<>();
                jsonResponse.put("success", true);
                jsonResponse.put("payload", payloadPix);
                jsonResponse.put("valor", valorTotal);
                jsonResponse.put("produto", nomeProduto);
                
                response.getWriter().write(gson.toJson(jsonResponse));
                
            } else if ("mercado_pago".equalsIgnoreCase(meio)) {
                // ==========================================
                // ✅ MERCADO PAGO: Gera link de pagamento
                // ==========================================
                String linkPagamento = criarLinkMercadoPago(id, nomeProduto, valorTotal);
                
                if (linkPagamento != null && !linkPagamento.isEmpty()) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    Map<String, Object> jsonResponse = new HashMap<>();
                    jsonResponse.put("success", true);
                    jsonResponse.put("paymentUrl", linkPagamento);
                    
                    response.getWriter().write(gson.toJson(jsonResponse));
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Erro ao gerar link do Mercado Pago");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Erro: " + e.getMessage());
        }
    }

    // ==========================================
    // ✅ GERAR PAYLOAD PIX (SEM Mercado Pago)
    // ==========================================
    private String gerarPayloadPix(double valor, String descricao) {
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
        
        System.out.println("✅ Payload Pix gerado SEM Mercado Pago!");
        System.out.println("   Chave: " + CHAVE_PIX);
        System.out.println("   Valor: R$ " + valor);
        
        return payload.toString();
    }

    // ==========================================
    // CALCULAR CRC16 (para o Pix)
    // ==========================================
    private String calcularCRC16(String payload) {
        int crc = 0xFFFF;
        byte[] bytes = payload.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        
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
    // ✅ MERCADO PAGO: Criar link de pagamento
    // ==========================================
    private String criarLinkMercadoPago(String codPeca, String titulo, double valor) {
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

            System.out.println("📤 JSON MP: " + jsonPayload);

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
                
                System.out.println("🔗 Link MP gerado: " + link);
                return link;
            } else {
                System.err.println("❌ MP rejeitou. Código: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("❌ Erro MP: " + e.getMessage());
        }
        
        return null;
    }

    // ==========================================
    // MÉTODOS DO BANCO
    // ==========================================
    private void registrarPreVendaNaAiven(String codPeca, double valorTotal, double frete, String endereco) {
        String sql = "INSERT INTO vendas (datavenda, codpeca, valorvenda, valorfrete, endereco_entrega, status_pagamento) VALUES (CURDATE(), ?, ?, ?, ?, 'PENDENTE')";
        try (Connection con = ConnectionDB.getConnectionCloud();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, codPeca);
            stmt.setDouble(2, valorTotal);
            stmt.setDouble(3, frete);
            stmt.setString(4, endereco);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double obterPrecoDoBancoCloud(String codPeca) {
        try (Connection con = ConnectionDB.getConnectionCloud();
             PreparedStatement stmt = con.prepareStatement("SELECT precosug FROM estoque WHERE codpeca = ?")) {
            stmt.setString(1, codPeca);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("precosug");
        } catch (Exception ex) {
            System.err.println("Erro ao buscar preço: " + ex.getMessage());
        }
        return 0.0;
    }

    private String obterNomeDoBancoCloud(String codPeca) {
        try (Connection con = ConnectionDB.getConnectionCloud();
             PreparedStatement stmt = con.prepareStatement("SELECT itemdesc FROM estoque WHERE codpeca = ?")) {
            stmt.setString(1, codPeca);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("itemdesc");
        } catch (Exception ex) {
            System.err.println("Erro ao buscar nome: " + ex.getMessage());
        }
        return null;
    }
}