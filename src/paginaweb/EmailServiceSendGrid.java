package paginaweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailServiceSendGrid {

    private static final String EMAIL_REMETENTE = "portobella.brecho@gmail.com";

    // ==========================================
    // MÉTODO PRINCIPAL (ENVIO VIA SENDGRID)
    // ==========================================
    private static void enviarEmailSendGrid(final String destinatario, final String assunto, final String corpoHtml) {
        new Thread(() -> {
            try {
                String apiKey = System.getenv("SENDGRID_API_KEY");
                if (apiKey == null || apiKey.isEmpty()) {
                    System.err.println("❌ SENDGRID_API_KEY não configurada.");
                    return;
                }

                // 🔥 JSON CORRETO
                String json = String.format(
                    "{\"personalizations\":[{\"to\":[{\"email\":\"%s\"}]}],\"from\":{\"email\":\"%s\"},\"subject\":\"%s\",\"content\":[{\"type\":\"text/html\",\"value\":\"%s\"}]}",
                    destinatario,
                    EMAIL_REMETENTE,
                    assunto,
                    corpoHtml.replace("\"", "\\\"").replace("\n", "\\n")
                );

                System.out.println("📧 [SENDGRID] Enviando para: " + destinatario);

                URL url = new URL("https://api.sendgrid.com/v3/mail/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    System.out.println("✅ E-mail enviado para: " + destinatario);
                } else {
                    // 🔥 LÊ O ERRO
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                    }
                    System.err.println("❌ Erro " + code + ": " + sb.toString());
                }
            } catch (IOException e) {
                System.err.println("❌ Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ==========================================
    // MÉTODO EXCLUSIVO PARA NOTIFICAR A LOJA
    // ==========================================
    public static void enviarNovaVendaParaLoja(String pedidoId, String cliente, String emailCliente,
                                               String telefone, double valorTotal, String meioPagamento,
                                               boolean retirarLoja, String endereco, String itens) {
        String tipoEntrega = retirarLoja ? "📍 Retirada na Loja" : "🚚 Entrega via Frete";
        String assunto = "🛍️ NOVA VENDA CONFIRMADA - Pedido #" + pedidoId;

        String corpoHtml = "<h2>🛍️ NOVA VENDA CONFIRMADA - SITE</h2>" +
                "<p><strong>Pedido:</strong> #" + pedidoId + "</p>" +
                "<p><strong>Cliente:</strong> " + cliente + "</p>" +
                "<p><strong>E-mail:</strong> " + emailCliente + "</p>" +
                "<p><strong>Telefone:</strong> " + telefone + "</p>" +
                "<p><strong>Total:</strong> R$ " + String.format("%.2f", valorTotal) + "</p>" +
                "<p><strong>Meio de pagamento:</strong> " + meioPagamento + "</p>" +
                "<p><strong>Tipo de entrega:</strong> " + tipoEntrega + "</p>" +
                "<p><strong>Endereço:</strong> " + endereco + "</p>" +
                "<p><strong>Itens:</strong> " + itens + "</p>" +
                "<hr>" +
                "<p>📌 Aguardando confirmação da loja para finalizar o pedido.</p>";

        enviarEmailSendGrid("portobella.brecho@gmail.com", assunto, corpoHtml);
    }
}
