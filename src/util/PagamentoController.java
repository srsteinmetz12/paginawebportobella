//package util;
//
//import connection.ConnectionDB;
//import com.google.gson.Gson;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.EncodeHintType;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.common.BitMatrix;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import org.springframework.web.bind.annotation.*;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.HashMap;
//import java.util.Hashtable;
//import java.util.Map;
//import javax.imageio.ImageIO;
//
//@RestController
//@RequestMapping("/api/pagamentos")
//@CrossOrigin(origins = "*")
//public class PagamentoController {
//
//    // ==========================================
//    // CONFIGURAÇÕES
//    // ==========================================
//    
//    // 🔥 SUA CHAVE PIX
//    private static final String CHAVE_PIX = "portobella.brecho@gmail.com";
//    
//    // 🔥 SEU NOME (limitado a 25 caracteres para o Pix)
//    private static final String NOME_RECEBEDOR = "Vanderleia Vieira";
//    
//    // 🔥 SUA CIDADE
//    private static final String CIDADE = "PORTO ALEGRE";
//    
//    // 🔥 TOKEN DO MERCADO PAGO
//    private static final String TOKEN_MP = "APP_USR-5504079628127234-061707-4f72faca8cd75c397d89abc34651960f-3480421128";
//    
//    private final Gson gson = new Gson();
//
//    // ==========================================
//    // ENDPOINT: CRIAR PAGAMENTO
//    // ==========================================
//    @GetMapping("/criar")
//    public void criarPagamento(
//            @RequestParam String meio,
//            @RequestParam String id,
//            @RequestParam(required = false, defaultValue = "0") double preco,
//            @RequestParam(required = false, defaultValue = "0") double frete,
//            @RequestParam(required = false) String nome,
//            HttpServletResponse response) throws IOException {
//
//        System.out.println("📝 Criando pagamento para: " + id + " | Meio: " + meio);
//
//        // Busca os dados do produto
//        double precoProduto = preco > 0 ? preco : obterPrecoDoBancoCloud(id);
//        double valorTotal = precoProduto + frete;
//        String nomeProduto = (nome != null && !nome.isEmpty()) ? nome : obterNomeDoBancoCloud(id);
//        if (nomeProduto == null || nomeProduto.isEmpty()) {
//            nomeProduto = "Produto #" + id;
//        }
//
//        // Registra a venda no banco
//        registrarPreVendaNaAiven(id, valorTotal, frete, "Cliente");
//
//        try {
//            if ("pix".equalsIgnoreCase(meio)) {
//                // ==========================================
//                // ✅ PIX: Gera QR Code CORRIGIDO
//                // ==========================================
//                String pedidoId = String.valueOf(System.currentTimeMillis());
//                String payloadPix = gerarPayloadPix(valorTotal, pedidoId);
//                
//                if (payloadPix == null || payloadPix.isEmpty()) {
//                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
//                        "Erro ao gerar código Pix");
//                    return;
//                }
//                
//                System.out.println("✅ Payload Pix gerado com sucesso!");
//                System.out.println("📋 Pedido ID: " + pedidoId);
//                System.out.println("📋 Payload: " + payloadPix);
//                System.out.println("📏 Tamanho: " + payloadPix.length());
//                
//                response.setContentType("application/json");
//                response.setCharacterEncoding("UTF-8");
//                
//                Map<String, Object> jsonResponse = new HashMap<>();
//                jsonResponse.put("success", true);
//                jsonResponse.put("payload", payloadPix);
//                jsonResponse.put("total", valorTotal);
//                jsonResponse.put("pedidoId", pedidoId);
//                
//                response.getWriter().write(gson.toJson(jsonResponse));
//                
//            } else if ("mercado_pago".equalsIgnoreCase(meio)) {
//                // ==========================================
//                // ✅ MERCADO PAGO: Gera link de pagamento
//                // ==========================================
//                String linkPagamento = criarLinkMercadoPago(id, nomeProduto, valorTotal);
//                
//                if (linkPagamento != null && !linkPagamento.isEmpty()) {
//                    response.setContentType("application/json");
//                    response.setCharacterEncoding("UTF-8");
//                    
//                    Map<String, Object> jsonResponse = new HashMap<>();
//                    jsonResponse.put("success", true);
//                    jsonResponse.put("paymentUrl", linkPagamento);
//                    
//                    response.getWriter().write(gson.toJson(jsonResponse));
//                } else {
//                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
//                        "Erro ao gerar link do Mercado Pago");
//                }
//            }
//
//        } catch (IOException e) {
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
//                "Erro: " + e.getMessage());
//        }
//    }
//
//     // ==========================================
//    // GERAR PAYLOAD PIX COMPLETO (VERSÃO CORRIGIDA)
//    // ==========================================
//    public static String gerarPayloadPix(double valor, String pedidoId) {
//        try {
//            System.out.println("🔧 Gerando payload Pix...");
//            System.out.println("💰 Valor: R$ " + valor);
//            System.out.println("📝 Pedido ID: " + pedidoId);
//            
//            // ==========================================
//            // 1. FORMATAR VALOR (CORRIGIDO)
//            // ==========================================
//            String valorFormatado = String.format("%.2f", valor);
//            // ✅ 95.80 -> "95.80" (mantém o ponto!)
//            
//            // ==========================================
//            // 2. CONSTRUIR PAYLOAD (PADRÃO BR CODE)
//            // ==========================================
//            StringBuilder payload = new StringBuilder();
//            
//            // 1. Payload Format Indicator (sempre 000201)
//            payload.append("000201");
//            
//            // 2. Merchant Account Information
//            String gui = "0014BR.GOV.BCB.PIX";
//            String chavePix = "01" + String.format("%02d", CHAVE_PIX.length()) + CHAVE_PIX;
//            String merchantInfo = gui + chavePix;
//            payload.append("26");
//            payload.append(String.format("%02d", merchantInfo.length()));
//            payload.append(merchantInfo);
//            
//            // 3. Merchant Category Code (sempre 0000 para Pix)
//            payload.append("52040000");
//            
//            // 4. Transaction Currency (986 = BRL)
//            payload.append("5303986");
//            
//            // 5. Transaction Amount (CORRIGIDO - mantém o ponto)
//            if (valor > 0) {
//                payload.append("54");
//                payload.append(String.format("%02d", valorFormatado.length()));
//                payload.append(valorFormatado); // ✅ Agora é "95.80"
//            }
//            
//            // 6. Country Code (BR = Brasil)
//            payload.append("5802BR");
//            
//            // 7. Merchant Name (máximo 25 caracteres)
//            String nome = NOME_RECEBEDOR;
//            if (nome.length() > 25) {
//                nome = nome.substring(0, 25);
//            }
//            payload.append("59");
//            payload.append(String.format("%02d", nome.length()));
//            payload.append(nome);
//            
//            // 8. Merchant City (máximo 15 caracteres)
//            String cidade = CIDADE;
//            if (cidade.length() > 15) {
//                cidade = cidade.substring(0, 15);
//            }
//            payload.append("60");
//            payload.append(String.format("%02d", cidade.length()));
//            payload.append(cidade);
//            
//            // 9. Additional Data Field (TXID)
//            String txid = pedidoId;
//            if (txid.length() > 25) {
//                txid = txid.substring(0, 25);
//            }
//            String additionalData = "05" + String.format("%02d", txid.length()) + txid;
//            payload.append("62");
//            payload.append(String.format("%02d", additionalData.length()));
//            payload.append(additionalData);
//            
//            // ==========================================
//            // 3. CALCULAR CRC16 (CORRIGIDO)
//            // ==========================================
//            String payloadSemCRC = payload.toString();
//            String payloadParaCRC = payloadSemCRC + "6304";
//            String crc16 = calcularCRC16(payloadParaCRC);
//            
//            payload.append("6304");
//            payload.append(crc16);
//            
//            String payloadFinal = payload.toString();
//            
//            System.out.println("✅ Payload Pix gerado com sucesso!");
//            System.out.println("📋 Payload: " + payloadFinal);
//            System.out.println("📏 Tamanho: " + payloadFinal.length());
//            System.out.println("✅ Começa com 000201? " + payloadFinal.startsWith("000201"));
//            System.out.println("✅ Tem BR.GOV.BCB.PIX? " + payloadFinal.contains("BR.GOV.BCB.PIX"));
//            System.out.println("✅ Tem 6304? " + payloadFinal.contains("6304"));
//            
//            return payloadFinal;
//            
//        } catch (Exception e) {
//            System.err.println("❌ Erro ao gerar payload Pix: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
//    
//    // ==========================================
//    // CALCULAR CRC16 (CORRIGIDO - ALGORITMO PADRÃO)
//    // ==========================================
//    private static String calcularCRC16(String input) {
//        try {
//            int crc = 0xFFFF;
//            byte[] bytes = input.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
//            
//            for (byte b : bytes) {
//                crc ^= (b & 0xFF) << 8; // ✅ CORRIGIDO: desloca byte para MSB
//                for (int i = 0; i < 8; i++) {
//                    if ((crc & 0x8000) != 0) { // ✅ CORRIGIDO: verifica MSB
//                        crc = (crc << 1) ^ 0x1021; // ✅ Polinômio correto (MSB-first)
//                    } else {
//                        crc = crc << 1;
//                    }
//                }
//            }
//            
//            String result = String.format("%04X", crc & 0xFFFF);
//            System.out.println("✅ CRC16 calculado: " + result);
//            return result;
//            
//        } catch (Exception e) {
//            System.err.println("❌ Erro ao calcular CRC16: " + e.getMessage());
//            return "0000";
//        }
//    }
//    
//    // ==========================================
//    // GERAR QR CODE COMO IMAGEM
//    // ==========================================
//    public static void gerarQRCode(String codigoPix, String caminhoArquivo, int tamanho) throws Exception {
//        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
//        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
//        hints.put(EncodeHintType.MARGIN, 2);
//        
//        MultiFormatWriter writer = new MultiFormatWriter();
//        BitMatrix matrix = writer.encode(codigoPix, BarcodeFormat.QR_CODE, tamanho, tamanho, hints);
//        
//        BufferedImage image = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_RGB);
//        for (int x = 0; x < tamanho; x++) {
//            for (int y = 0; y < tamanho; y++) {
//                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
//            }
//        }
//        
//        File arquivo = new File(caminhoArquivo);
//        arquivo.getParentFile().mkdirs();
//        ImageIO.write(image, "PNG", arquivo);
//        
//        System.out.println("✅ QR Code gerado: " + caminhoArquivo);
//    }
//
//    // ==========================================
//    // ✅ MERCADO PAGO: Criar link de pagamento
//    // ==========================================
//    private String criarLinkMercadoPago(String codPeca, String titulo, double valor) {
//        try {
//            String precoFormatado = String.format(java.util.Locale.US, "%.2f", valor);
//            
//            // Monta o JSON da preferência
//            String jsonPayload = "{"
//                + "\"items\": [{"
//                + "\"id\": \"" + codPeca + "\","
//                + "\"title\": \"" + titulo + "\","
//                + "\"quantity\": 1,"
//                + "\"currency_id\": \"BRL\","
//                + "\"unit_price\": " + precoFormatado
//                + "}],"
//                + "\"back_urls\": {"
//                + "\"success\": \"https://srsteinmetz12.github.io/sucesso.html\","
//                + "\"failure\": \"https://srsteinmetz12.github.io/falha.html\","
//                + "\"pending\": \"https://srsteinmetz12.github.io/pendente.html\""
//                + "},"
//                + "\"auto_return\": \"approved\""
//                + "}";
//
//            System.out.println("📤 JSON MP: " + jsonPayload);
//
//            // Faz a requisição para o Mercado Pago
//            java.net.URL url = new java.net.URL("https://api.mercadopago.com/checkout/preferences");
//            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("Authorization", "Bearer " + TOKEN_MP);
//            conn.setDoOutput(true);
//
//            try (java.io.OutputStream os = conn.getOutputStream()) {
//                os.write(jsonPayload.getBytes("utf-8"));
//            }
//
//            int responseCode = conn.getResponseCode();
//            
//            if (responseCode == 200 || responseCode == 201) {
//                java.io.BufferedReader br = new java.io.BufferedReader(
//                    new java.io.InputStreamReader(conn.getInputStream(), "utf-8"));
//                StringBuilder res = new StringBuilder();
//                String line;
//                while ((line = br.readLine()) != null) {
//                    res.append(line.trim());
//                }
//                
//                String txt = res.toString();
//                int inicio = txt.indexOf("\"init_point\":\"") + 14;
//                int fim = txt.indexOf("\"", inicio);
//                String link = txt.substring(inicio, fim).replace("\\/", "/");
//                
//                System.out.println("🔗 Link MP gerado: " + link);
//                return link;
//            } else {
//                System.err.println("❌ MP rejeitou. Código: " + responseCode);
//            }
//        } catch (IOException e) {
//            System.err.println("❌ Erro MP: " + e.getMessage());
//        }
//        
//        return null;
//    }
//
//    // ==========================================
//    // MÉTODOS DO BANCO
//    // ==========================================
//    private void registrarPreVendaNaAiven(String codPeca, double valorTotal, double frete, String endereco) {
//        String sql = "INSERT INTO vendas (datavenda, codpeca, valorvenda, valorfrete, endereco_entrega, status_pagamento) VALUES (CURDATE(), ?, ?, ?, ?, 'PENDENTE')";
//        try (Connection con = ConnectionDB.getConnectionCloud();
//             PreparedStatement stmt = con.prepareStatement(sql)) {
//            stmt.setString(1, codPeca);
//            stmt.setDouble(2, valorTotal);
//            stmt.setDouble(3, frete);
//            stmt.setString(4, endereco);
//            stmt.executeUpdate();
//        } catch (Exception e) {
//        }
//    }
//
//    private double obterPrecoDoBancoCloud(String codPeca) {
//        try (Connection con = ConnectionDB.getConnectionCloud();
//             PreparedStatement stmt = con.prepareStatement("SELECT precosug FROM estoque WHERE codpeca = ?")) {
//            stmt.setString(1, codPeca);
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) return rs.getDouble("precosug");
//        } catch (Exception ex) {
//            System.err.println("Erro ao buscar preço: " + ex.getMessage());
//        }
//        return 0.0;
//    }
//
//    private String obterNomeDoBancoCloud(String codPeca) {
//        try (Connection con = ConnectionDB.getConnectionCloud();
//             PreparedStatement stmt = con.prepareStatement("SELECT itemdesc FROM estoque WHERE codpeca = ?")) {
//            stmt.setString(1, codPeca);
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) return rs.getString("itemdesc");
//        } catch (Exception ex) {
//            System.err.println("Erro ao buscar nome: " + ex.getMessage());
//        }
//        return null;
//    }
//}