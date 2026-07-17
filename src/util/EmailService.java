package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import static util.EmailTemplateHelper.*;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String EMAIL_REMETENTE = "portobella.brecho@gmail.com";
    private static final String SENHA_REMETENTE = "mpsihqyoyjnmgkty"; // 🔥 ATUALIZE COM SENHA DE APP

    private static void enviarEmailBase(final String destinatario, final String assunto, final String corpoHtml) {
        if (destinatario == null || destinatario.isEmpty() || "Não informado".equals(destinatario)) {
            System.err.println("⚠️ E-mail não enviado: destinatário inválido.");
            return;
        }

        new Thread(() -> {
            Socket socket = null;
            SSLSocket sslSocket = null;
            BufferedReader leitor = null;
            PrintWriter escritor = null;

            try {
                socket = new Socket(SMTP_HOST, SMTP_PORT);
                socket.setSoTimeout(20000);
                leitor = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                lerResposta(leitor); // Saudação

                escritor.println("EHLO " + SMTP_HOST);
                lerResposta(leitor);

                escritor.println("STARTTLS");
                lerResposta(leitor);

                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) ssf.createSocket(socket, SMTP_HOST, SMTP_PORT, true);
                sslSocket.startHandshake();

                leitor = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), "UTF-8"));
                escritor = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream(), "UTF-8"), true);

                escritor.println("EHLO " + SMTP_HOST);
                lerResposta(leitor);

                escritor.println("AUTH LOGIN");
                lerResposta(leitor);
                escritor.println(Base64.getEncoder().encodeToString(EMAIL_REMETENTE.getBytes("UTF-8")));
                lerResposta(leitor);
                escritor.println(Base64.getEncoder().encodeToString(SENHA_REMETENTE.getBytes("UTF-8")));
                lerResposta(leitor);

                escritor.println("MAIL FROM:<" + EMAIL_REMETENTE + ">");
                lerResposta(leitor);
                escritor.println("RCPT TO:<" + destinatario + ">");
                lerResposta(leitor);

                escritor.println("DATA");
                lerResposta(leitor);

                escritor.println("From: PORTOBELLA Brechó <" + EMAIL_REMETENTE + ">");
                escritor.println("To: " + destinatario);
                escritor.println("Subject: " + assunto);
                escritor.println("MIME-Version: 1.0");
                escritor.println("Content-Type: text/html; charset=UTF-8");
                escritor.println();
                escritor.println(corpoHtml);
                escritor.println(".");
                lerResposta(leitor);

                escritor.println("QUIT");
                lerResposta(leitor);

                System.out.println("✅ E-mail SMTP enviado para: " + destinatario);

            } catch (Exception e) {
                System.err.println("❌ Erro SMTP para " + destinatario + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                try { if (escritor != null) escritor.close(); } catch (Exception ignore) {}
                try { if (leitor != null) leitor.close(); } catch (Exception ignore) {}
                try { if (sslSocket != null) sslSocket.close(); } catch (Exception ignore) {}
                try { if (socket != null) socket.close(); } catch (Exception ignore) {}
            }
        }).start();
    }

    private static void lerResposta(BufferedReader leitor) throws Exception {
        String linha = leitor.readLine();
        System.out.println("SMTP: " + linha);
        while (linha != null && linha.length() >= 4 && linha.charAt(3) == '-') {
            linha = leitor.readLine();
            System.out.println("SMTP: " + linha);
        }
    }

    // ==========================================
    // MÉTODOS PÚBLICOS (MANTENHA TODOS OS QUE O DESKTOP USA)
    // ==========================================
    public static void enviarCupomAssincrono(final String emailDestino, final String corpoHtml, final String idVenda) {
        String assunto = "Seu Cupom Não Fiscal - Venda #" + idVenda + " - PORTOBELLA Brechó & Outlet";
        enviarEmailBase(emailDestino, assunto, corpoHtml);
    }

    public static void enviarConfirmacaoParaCliente(String emailCliente, String nomeCliente,
                                                String pedidoId, double valor, String itens) {
        String assunto = "✅ Pagamento Confirmado - Pedido #" + pedidoId + " - PORTOBELLA";
        String itensHtml = formatarItensHtml(itens);
        String barra = gerarBarraEvolucao(2); // 🔥 ETAPA 2: Pagamento confirmado

        String corpoHtml = 
        "<!DOCTYPE html>" +
        "<html>" +
        "<head>" +
        "  <meta charset='UTF-8'>" +
        "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
        "  <style>" +
        "    @media only screen and (max-width: 600px) {" +
        "      .container { width: 100% !important; padding: 10px !important; }" +
        "      .header h2 { font-size: 20px !important; }" +
        "      .content { padding: 15px !important; }" +
        "      .content h2 { font-size: 18px !important; }" +
        "      .content p { font-size: 14px !important; }" +
        "      .barra { padding: 10px !important; }" +
        "      .barra table { width: 100% !important; }" +
        "      .barra td { font-size: 10px !important; }" +
        "      .footer { font-size: 11px !important; padding: 10px !important; }" +
        "      .itens li { font-size: 13px !important; }" +
        "    }" +
        "  </style>" +
        "</head>" +
        "<body style='margin: 0; padding: 0; background-color: #f9f9f9; font-family: Arial, sans-serif;'>" +
        "<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width: 600px; background-color: #ffffff; border-radius: 10px; margin: 20px auto; border-collapse: collapse;'>" +
        "  <tr>" +
        "    <td style='background-color: #1E1E1E; color: #FFF; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;'>" +
        "      <h2 style='margin: 0; font-size: 24px; letter-spacing: 2px;'>🛍️ PORTOBELLA</h2>" +
        "      <p style='margin: 0; font-size: 12px; opacity: 0.8;'>Brechó & Outlet</p>" +
        "    </td>" +
        "  </tr>" +
        "  <tr>" +
        "    <td style='padding: 20px;'>" +
        "      <h2 style='color: #1E1E1E; font-size: 22px;'>📥 Pedido recebido com sucesso!</h2>" +
        "      <p style='color: #333; font-size: 16px;'>Olá <strong>" + nomeCliente + "</strong>,</p>" +
        "      <p style='color: #333; font-size: 16px;'>Seu pedido <strong>#" + pedidoId + "</strong> foi recebido e encaminhado para a loja.</p>" +
        "      <p style='font-size: 16px;'><strong>Valor:</strong> R$ " + String.format("%.2f", valor) + "</p>" +
        "      <p style='font-size: 16px;'><strong>Itens:</strong></p>" +
        itensHtml +
        "      <br>" +
        "      <div class='barra' style='padding: 10px 0;'>" + barra + "</div>" +
        "      <br>" +
        "      <p style='color: #555; font-size: 15px;'>Assim que o pagamento for confirmado, você receberá outro e-mail com o cupom fiscal.</p>" +
        "      <br>" +
        "      <div style='background-color: #f0f0f0; padding: 15px; border-radius: 8px; text-align: center;'>" +
        "        <p style='margin: 0; color: #333; font-size: 14px;'><strong>PORTOBELLA Brechó & Outlet</strong></p>" +
        "        <p style='margin: 0; color: #555; font-size: 12px;'>📍 Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
        "        <p style='margin: 0; color: #555; font-size: 12px;'>🕐 Segunda a Sexta, 10h às 18h</p>" +
        "      </div>" +
        "      <br>" +
        "      <p style='text-align: center; color: #888; font-size: 12px;'>Obrigado por comprar na PORTOBELLA! 💛</p>" +
        "    </td>" +
        "  </tr>" +
        "</table>" +
        "</body>" +
        "</html>";

        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    public static void enviarRejeicaoParaCliente(String emailCliente, String nomeCliente,
                                                 String pedidoId, String motivo) {
        String assunto = "❌ Pagamento Rejeitado - Pedido #" + pedidoId;
        String corpoHtml = "<h2>❌ Pagamento Rejeitado - PORTOBELLA</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Infelizmente seu pagamento do pedido #" + pedidoId + " <strong>não foi aprovado</strong>.</p>" +
                "<p><strong>Motivo:</strong> " + (motivo != null ? motivo : "Não informado") + "</p>" +
                "<p>Entre em contato conosco.</p>";
        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    public static void enviarProntoParaRetirada(String emailCliente, String nomeCliente, String pedidoId) {
        String assunto = "📦 Pedido pronto para retirada - #" + pedidoId;
        String corpoHtml = "<h2>📦 Pedido pronto para retirada!</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Seu pedido #" + pedidoId + " já está disponível na loja.</p>" +
                "<p><strong>📍 Endereço:</strong> Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
                "<p><strong>🕐 Horário:</strong> Segunda a Sexta, 10h às 18h</p>" +
                "<br><p>Obrigado por comprar na PORTOBELLA! 💛</p>";
        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    public static void enviarPedidoDespachado(String emailCliente, String nomeCliente,
                                              String pedidoId, String codigoRastreio) {
        String assunto = "🚚 Pedido despachado - #" + pedidoId;
        String codigo = (codigoRastreio != null && !codigoRastreio.isEmpty()) ?
                codigoRastreio : "será enviado em breve";
        String corpoHtml = "<h2>🚚 Pedido despachado!</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Seu pedido #" + pedidoId + " foi <strong>despachado</strong>.</p>" +
                "<p><strong>📦 Código de rastreio:</strong> " + codigo + "</p>" +
                "<br><p>Obrigado por comprar na PORTOBELLA! 💛</p>";
        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }
}