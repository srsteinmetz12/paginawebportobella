package util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MercadoPagoIntegration {
    
    // ⚠️ SUBSTITUA PELO SEU ACCESS TOKEN
    private static final String ACCESS_TOKEN = "APP_USR-5504079628127234-061707-4f72faca8cd75c397d89abc34651960f-3480421128";
    private static final String API_URL = "https://api.mercadopago.com/v1/payments";
    private final Gson gson = new Gson();
    
    public MercadoPagoIntegration() {
        System.out.println("✅ Mercado Pago API inicializada!");
    }
    
    /**
     * Cria um pagamento Pix via API REST
     * @param valor
     * @param descricao
     * @param email
     * @param nome
     * @param cpf
     * @return 
     * @throws java.lang.Exception
     */
    public PaymentResponse criarPagamentoPix(double valor, String descricao, String email, String nome, String cpf) throws Exception {
        
        // Validações
        if (email == null || email.trim().isEmpty()) {
            email = "cliente@portobella.com";
        }
        if (nome == null || nome.trim().isEmpty()) {
            nome = "Cliente PORTOBELLA";
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            cpf = "99393557004";
        }
        
        // 1. Monta o JSON da requisição
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("transaction_amount", valor);
        paymentData.put("description", descricao != null ? descricao : "Compra na PORTOBELLA");
        paymentData.put("payment_method_id", "pix");
        
        // 2. Dados do pagador
        Map<String, Object> payer = new HashMap<>();
        payer.put("email", email);
        payer.put("first_name", nome.split(" ")[0]);
        payer.put("last_name", nome.contains(" ") ? nome.substring(nome.indexOf(" ") + 1) : "Cliente");
        
        Map<String, Object> identification = new HashMap<>();
        identification.put("type", "CPF");
        identification.put("number", cpf.replaceAll("\\D", ""));
        payer.put("identification", identification);
        
        paymentData.put("payer", payer);
        
        // 3. Chave de idempotência
        String idempotencyKey = UUID.randomUUID().toString();
        
        // 4. Converte para JSON
        String jsonBody = gson.toJson(paymentData);
        System.out.println("📤 Enviando requisição: " + jsonBody);
        
        // 5. Faz a requisição HTTP
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        conn.setRequestProperty("X-Idempotency-Key", idempotencyKey);
        conn.setDoOutput(true);
        
        // 6. Envia o body
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 7. Lê a resposta
        int responseCode = conn.getResponseCode();
        String responseBody;
        
        if (responseCode >= 200 && responseCode < 300) {
            try (java.util.Scanner s = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A")) {
                responseBody = s.hasNext() ? s.next() : "";
            }
        } else {
            try (java.util.Scanner s = new java.util.Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A")) {
                responseBody = s.hasNext() ? s.next() : "";
            }
            throw new Exception("Erro na API: " + responseCode + " - " + responseBody);
        }
        
        System.out.println("📥 Resposta: " + responseBody);
        
        // 8. Parse da resposta
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        
        PaymentResponse response = new PaymentResponse();
        response.id = jsonResponse.get("id").getAsLong();
        response.status = jsonResponse.get("status").getAsString();
        
        // Extrai QR Code
        if (jsonResponse.has("point_of_interaction")) {
            JsonObject poi = jsonResponse.getAsJsonObject("point_of_interaction");
            if (poi.has("transaction_data")) {
                JsonObject txData = poi.getAsJsonObject("transaction_data");
                if (txData.has("qr_code_base64")) {
                    response.qrCodeBase64 = txData.get("qr_code_base64").getAsString();
                }
                if (txData.has("qr_code")) {
                    response.qrCode = txData.get("qr_code").getAsString();
                }
            }
        }
        
        System.out.println("✅ Pix criado! ID: " + response.id + " | Status: " + response.status);
        
        return response;
    }
    
    /**
     * Consulta o status de um pagamento via API REST
     * @param paymentId
     * @return 
     * @throws java.lang.Exception
     */
    public PaymentStatus consultarStatus(Long paymentId) throws Exception {
        URL url = new URL(API_URL + "/" + paymentId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        
        int responseCode = conn.getResponseCode();
        String responseBody;
        
        if (responseCode >= 200 && responseCode < 300) {
            try (java.util.Scanner s = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A")) {
                responseBody = s.hasNext() ? s.next() : "";
            }
        } else {
            try (java.util.Scanner s = new java.util.Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A")) {
                responseBody = s.hasNext() ? s.next() : "";
            }
            throw new Exception("Erro na API: " + responseCode + " - " + responseBody);
        }
        
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        
        PaymentStatus status = new PaymentStatus();
        status.id = jsonResponse.get("id").getAsLong();
        status.status = jsonResponse.get("status").getAsString();
        
        return status;
    }
    
    // ==========================================
    // CLASSES DE RESPOSTA (Java 8)
    // ==========================================
    public static class PaymentResponse {
        public Long id;
        public String status;
        public String qrCodeBase64;
        public String qrCode;
    }
    
    public static class PaymentStatus {
        public Long id;
        public String status;
    }
}