package paginaweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import static util.EmailTemplateHelper.formatarItensHtml;
import static util.EmailTemplateHelper.gerarBarraEvolucao;
import static util.EmailTemplateHelper.gerarResumoFinanceiro;

public class EmailServiceSendGrid {

    private static final String EMAIL_REMETENTE = "portobella.brecho@gmail.com";

    // ==========================================
    // MÉTODO BASE (HTTP PARA SENDGRID) – SEM SMTP!
    // ==========================================
    private static void enviarEmailSendGrid(final String destinatario, final String assunto, final String corpoHtml) {
        new Thread(() -> {
            try {
                String apiKey = System.getenv("SENDGRID_API_KEY");
                if (apiKey == null || apiKey.isEmpty()) {
                    System.err.println("❌ SENDGRID_API_KEY não configurada.");
                    return;
                }

                //JSON CORRETO
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
                    System.out.println("✅ E-mail SendGrid enviado para: " + destinatario);
                } else {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                    }
                    System.err.println("❌ Erro " + code + ": " + sb.toString());
                }
            } catch (IOException e) {
                System.err.println("❌ Exceção SendGrid: " + e.getMessage());
            }
        }).start();
    }

    // ==========================================
    // MÉTODO EXCLUSIVO PARA NOTIFICAR A LOJA
    // ==========================================
    public static void enviarNovaVendaParaLoja(String pedidoId, String cliente, String emailCliente,
                                           String telefone, double valorTotal, double frete,
                                           String meioPagamento, boolean retirarLoja,
                                           String endereco, String itens) {
        String tipoEntrega = retirarLoja ? "📍 Retirada na Loja" : "🚚 Entrega via Frete";
        String assunto = "🛍️ NOVA VENDA CONFIRMADA - Pedido #" + pedidoId;

        double subtotal = valorTotal - frete;

        String corpoHtml = "<h2>🛍️ NOVA VENDA CONFIRMADA - SITE</h2>" +
                "<p><strong>Pedido:</strong> #" + pedidoId + "</p>" +
                "<p><strong>Cliente:</strong> " + cliente + "</p>" +
                "<p><strong>E-mail:</strong> " + emailCliente + "</p>" +
                "<p><strong>Telefone:</strong> " + telefone + "</p>" +
                "<p><strong>Subtotal:</strong> R$ " + String.format("%.2f", subtotal) + "</p>" +
                "<p><strong>Frete:</strong> R$ " + String.format("%.2f", frete) + "</p>" +
                "<p><strong>Total:</strong> R$ " + String.format("%.2f", valorTotal) + "</p>" +
                "<p><strong>Meio de pagamento:</strong> " + meioPagamento + "</p>" +
                "<p><strong>Tipo de entrega:</strong> " + tipoEntrega + "</p>" +
                "<p><strong>Endereço:</strong> " + endereco + "</p>" +
                "<p><strong>Itens:</strong> " + itens + "</p>" +
                "<hr>" +
                "<p>📌 Aguardando confirmação da loja para finalizar o pedido.</p>";

        enviarEmailSendGrid("portobella.brecho@gmail.com", assunto, corpoHtml);
    }
    
    // ==========================================
    // E-MAIL PARA CLIENTE - PEDIDO RECEBIDO (AGUARDE CONFIRMAÇÃO)
    // ==========================================
    public static void enviarPedidoRecebidoCliente(String emailCliente, String nomeCliente,
                                               String pedidoId, double subtotal, double frete, double total, String itens) {
        String assunto = "📥 Pedido recebido - #" + pedidoId + " - PORTOBELLA";

        String itensHtml = formatarItensHtml(itens);
        String barra = gerarBarraEvolucao(1);
        String resumo = gerarResumoFinanceiro(subtotal, frete, total);

        String corpoHtml =
            "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>@media only screen and (max-width:600px){.container{width:100%!important;padding:10px!important;}.header h2{font-size:20px!important;}.content{padding:15px!important;}}</style>" +
            "</head><body style='margin:0;padding:0;background:#f9f9f9;font-family:Arial,sans-serif;'>" +
            "<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width:600px;background:#ffffff;border-radius:10px;margin:20px auto;'>" +
            "  <tr><td style='background:#1E1E1E;color:#FFF;padding:20px;text-align:center;border-radius:10px 10px 0 0;'>" +
            "    <h2 style='margin:0;font-size:24px;letter-spacing:2px;'>🛍️ PORTOBELLA</h2>" +
            "    <p style='margin:0;font-size:12px;opacity:0.8;'>Brechó & Outlet</p>" +
            "  </td></tr>" +
            "  <tr><td style='padding:20px;'>" +
            "    <h2 style='color:#1E1E1E;font-size:22px;'>📥 Pedido recebido com sucesso!</h2>" +
            "    <p style='color:#333;font-size:16px;'>Olá <strong>" + nomeCliente + "</strong>,</p>" +
            "    <p style='color:#333;font-size:16px;'>Seu pedido <strong>#" + pedidoId + "</strong> foi recebido e encaminhado para a loja.</p>" +
            "    <p><strong>Itens:</strong></p>" +
            itensHtml +
            "    <br>" +
            resumo +
            "    <br>" +
            barra +
            "    <br>" +
            "    <p style='color:#555;font-size:15px;'>Assim que o pagamento for confirmado, você receberá outro e-mail com o cupom fiscal.</p>" +
            "    <br>" +
            "    <div style='background:#f0f0f0;padding:15px;border-radius:8px;text-align:center;'>" +
            "      <p style='margin:0;color:#333;font-size:14px;'><strong>PORTOBELLA Brechó & Outlet</strong></p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>📍 Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>🕐 Segunda a Sexta, 10h às 18h</p>" +
            "    </div>" +
            "    <br>" +
            "    <p style='text-align:center;color:#888;font-size:12px;'>Obrigado por comprar na PORTOBELLA! 💛</p>" +
            "  </td></tr>" +
            "</table></body></html>";

        enviarEmailSendGrid(emailCliente, assunto, corpoHtml);
    }
}