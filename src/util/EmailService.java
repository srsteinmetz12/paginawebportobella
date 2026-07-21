package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
//import static util.EmailTemplateHelper.*;

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
                escritor.println("Reply-To: naoresponda@portobella.brecho@gmail.com");
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
            } finally {
                try { if (escritor != null) escritor.close(); } catch (Exception ignore) {}
                try { if (leitor != null) leitor.close(); } catch (IOException ignore) {}
                try { if (sslSocket != null) sslSocket.close(); } catch (IOException ignore) {}
                try { if (socket != null) socket.close(); } catch (IOException ignore) {}
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
    
    // ==========================================
    // 1. E-MAIL PARA CLIENTE – PEDIDO CONFIRMAÇÃO
    // ==========================================
    public static void enviarConfirmacaoParaCliente(String emailCliente, String nomeCliente,
                                                String pedidoId, double subtotal, double frete, double total, String itens) {
        String assunto = "✅ Pagamento Confirmado - Pedido #" + pedidoId + " - PORTOBELLA Brechó & Outlet";

        String itensHtml = EmailTemplateHelper.formatarItensHtml(itens);
        String barra = EmailTemplateHelper.gerarBarraEvolucao(2);
        String resumo = EmailTemplateHelper.gerarResumoFinanceiro(subtotal, frete, total);
        String cupom = EmailTemplateHelper.gerarCupomFiscal(pedidoId, nomeCliente, itensHtml, subtotal, frete, total);

        String corpoHtml =
            "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>@media only screen and (max-width:600px){.container{width:100%!important;padding:10px!important;}.header h2{font-size:20px!important;}.content{padding:15px!important;}}</style>" +
            "</head><body style='margin:0;padding:0;background:#f9f9f9;font-family:Arial,sans-serif;'>" +
            "<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width:600px;background:#fff;border-radius:10px;margin:20px auto;'>" +
            "  <tr><td style='background:#1E1E1E;color:#FFF;padding:20px;text-align:center;border-radius:10px 10px 0 0;'>" +
            "    <h2 style='margin:0;font-size:24px;letter-spacing:2px;'>🛍️ PORTOBELLA</h2>" +
            "    <p style='margin:0;font-size:12px;opacity:0.8;'>Brechó & Outlet</p>" +
            "  </td></tr>" +
            "  <tr><td style='padding:20px;'>" +
            "    <h2 style='color:#1E1E1E;font-size:22px;'>✅ Pagamento Confirmado!</h2>" +
            "    <p style='color:#333;font-size:16px;'>Olá <strong>" + nomeCliente + "</strong>,</p>" +
            "    <p style='color:#333;font-size:16px;'>Seu pagamento do pedido <strong>#" + pedidoId + "</strong> foi confirmado.</p>" +
            "    <br>" +
            cupom +                  // 🔥 CUPOM COMPLETO
            "    <br>" +
            resumo +                 // 🔥 RESUMO FINANCEIRO (SUBTOTAL, FRETE, TOTAL)
            "    <br>" +
            barra +                  // 🔥 BARRA DE EVOLUÇÃO
            "    <br>" +
            "    <div style='background:#f0f0f0;padding:15px;border-radius:8px;text-align:center;'>" +
            "      <p style='margin:0;color:#333;font-size:14px;'><strong>PORTOBELLA Brechó & Outlet</strong></p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>📍 Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>🕐 Segunda a Sexta, 10h às 18h</p>" +
            "    </div>" +
            "    <br>" +
            "    <p style='text-align:center;color:#888;font-size:12px;'>Obrigada por comprar na PORTOBELLA! 💛</p>" +
            "  </td></tr>" +
            "</table></body></html>";

        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }
    
    // ==========================================
    // 2. E-MAIL PARA CLIENTE – PEDIDO REJEITADO
    // ==========================================
    public static void enviarRejeicaoParaCliente(String emailCliente, String nomeCliente,
                                             String pedidoId, String motivo) {
        String assunto = "❌ Pagamento Rejeitado - Pedido #" + pedidoId + " - PORTOBELLA Brechó & Outlet";

        String corpoHtml =
            "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>@media only screen and (max-width:600px){.container{width:100%!important;padding:10px!important;}.header h2{font-size:20px!important;}.content{padding:15px!important;}.footer{font-size:11px!important;}}</style>" +
            "</head><body style='margin:0;padding:0;background:#f9f9f9;font-family:Arial,sans-serif;'>" +
            "<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width:600px;background:#ffffff;border-radius:10px;margin:20px auto;'>" +
            "  <tr><td style='background:#1E1E1E;color:#FFF;padding:20px;text-align:center;border-radius:10px 10px 0 0;'>" +
            "    <h2 style='margin:0;font-size:24px;letter-spacing:2px;'>🛍️ PORTOBELLA</h2>" +
            "    <p style='margin:0;font-size:12px;opacity:0.8;'>Brechó & Outlet</p>" +
            "  </td></tr>" +
            "  <tr><td style='padding:20px;'>" +
            "    <h2 style='color:#c0392b;font-size:22px;'>❌ Pagamento Rejeitado</h2>" +
            "    <p style='color:#333;font-size:16px;'>Olá <strong>" + nomeCliente + "</strong>,</p>" +
            "    <p style='color:#333;font-size:16px;'>Infelizmente seu pagamento do pedido <strong>#" + pedidoId + "</strong> <strong style='color:#c0392b;'>não foi aprovado</strong>.</p>" +
            "    <p style='font-size:16px;'><strong>Motivo:</strong> " + (motivo != null ? motivo : "Não informado") + "</p>" +
            "    <br>" +
            "    <div style='background:#fef0f0;padding:15px;border-radius:8px;border-left:4px solid #c0392b;'>" +
            "      <p style='margin:0;color:#333;font-size:14px;'><strong>🔍 O que fazer agora?</strong></p>" +
            "      <p style='margin:5px 0;color:#555;font-size:13px;'>• Verifique os dados do seu cartão ou conta e tente novamente.</p>" +
            "      <p style='margin:5px 0;color:#555;font-size:13px;'>• Entre em contato conosco para mais informações.</p>" +
            "      <p style='margin:5px 0;color:#555;font-size:13px;'>• Nenhum valor será cobrado.</p>" +
            "    </div>" +
            "    <br>" +
            "    <div style='background:#f0f0f0;padding:15px;border-radius:8px;text-align:center;'>" +
            "      <p style='margin:0;color:#333;font-size:14px;'><strong>PORTOBELLA Brechó & Outlet</strong></p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>📍 Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>🕐 Segunda a Sexta, 10h às 18h</p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>📞 (51) 98233-9066</p>" +
            "    </div>" +
            "    <br>" +
            "    <p style='text-align:center;color:#888;font-size:12px;'>💛 Equipe PORTOBELLA 💛</p>" +
            "  </td></tr>" +
            "</table></body></html>";

        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }
    
    // ==========================================
    // 3. E-MAIL PARA CLIENTE – PEDIDO EM SEPARAÇÃO
    // ==========================================
    public static void enviarPedidoEmSeparacao(String emailCliente, String nomeCliente,
                                               String pedidoId, double subtotal, double frete, double total, String itens) {
        String assunto = "📦 Pedido em separação - #" + pedidoId + " - PORTOBELLA";

        String itensHtml = EmailTemplateHelper.formatarItensHtml(itens);
        String barra = EmailTemplateHelper.gerarBarraEvolucao(3); // Etapa 3
        String resumo = EmailTemplateHelper.gerarResumoFinanceiro(subtotal, frete, total);

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
            "    <h2 style='color:#1E1E1E;font-size:22px;'>📦 Pedido em separação</h2>" +
            "    <p style='color:#333;font-size:16px;'>Olá <strong>" + nomeCliente + "</strong>,</p>" +
            "    <p style='color:#333;font-size:16px;'>Seu pedido <strong>#" + pedidoId + "</strong> está sendo preparado pela nossa equipe.</p>" +
            "    <p style='color:#555;font-size:15px;'>Em breve você receberá a confirmação de disponibilidade ou despacho.</p>" +
            "    <p><strong>Itens:</strong></p>" +
            itensHtml +
            "    <br>" +
            resumo +
            "    <br>" +
            barra +
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

        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    // ==========================================
    // 4. E-MAIL PARA CLIENTE – PRONTO PARA RETIRADA
    // ==========================================
    public static void enviarProntoParaRetirada(String emailCliente, String nomeCliente,
                                                String pedidoId, double subtotal, double frete, double total, String itens) {
        String assunto = "📦 Pedido pronto para retirada - #" + pedidoId + " - PORTOBELLA Brechó & Outlet";

        String itensHtml = EmailTemplateHelper.formatarItensHtml(itens);
        String barra = EmailTemplateHelper.gerarBarraEvolucao(4); // Etapa 4: Disponível
        String resumo = EmailTemplateHelper.gerarResumoFinanceiro(subtotal, frete, total);
        // Opcional: cupom (já enviado antes, mas pode incluir se quiser)

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
            "    <h2 style='color:#1E1E1E;font-size:22px;'>📦 Pedido pronto para retirada!</h2>" +
            "    <p style='color:#333;font-size:16px;'>Olá <strong>" + nomeCliente + "</strong>,</p>" +
            "    <p style='color:#333;font-size:16px;'>Seu pedido <strong>#" + pedidoId + "</strong> já está disponível para retirada na loja.</p>" +
            "    <div style='background:#f0f0f0;padding:15px;border-radius:8px;margin:15px 0;'>" +
            "      <p style='margin:0;color:#333;'><strong>📍 Endereço:</strong> Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
            "      <p style='margin:0;color:#333;'><strong>🕐 Horário:</strong> Segunda a Sexta, 10h às 18h</p>" +
            "      <p style='margin:0;color:#333;'><strong>⚠️ Importante:</strong> Leve seu documento de identificação e o número do pedido.</p>" +
            "    </div>" +
            "    <p><strong>Itens:</strong></p>" +
            itensHtml +
            "    <br>" +
            resumo +
            "    <br>" +
            barra +
            "    <br>" +
            "    <div style='background:#f0f0f0;padding:15px;border-radius:8px;text-align:center;'>" +
            "      <p style='margin:0;color:#333;font-size:14px;'><strong>PORTOBELLA Brechó & Outlet</strong></p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>📍 Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>🕐 Segunda a Sexta, 10h às 18h</p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>📞 (51) 98233-9066</p>" +
            "    </div>" +
            "    <br>" +
            "    <p style='text-align:center;color:#888;font-size:12px;'>Obrigada por comprar na PORTOBELLA! 💛</p>" +
            "  </td></tr>" +
            "</table></body></html>";

        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    // ==========================================
    // 5. E-MAIL PARA CLIENTE – PEDIDO DESPACHADO
    // ==========================================
    public static void enviarPedidoDespachado(String emailCliente, String nomeCliente,
                                              String pedidoId, double subtotal, double frete, double total, String itens,
                                              String codigoRastreio) {
        String assunto = "🚚 Pedido despachado - #" + pedidoId + " - PORTOBELLA Brechó & Outlet";
        String codigo = (codigoRastreio != null && !codigoRastreio.isEmpty()) ? codigoRastreio : "será enviado em breve";
        String itensHtml = EmailTemplateHelper.formatarItensHtml(itens);
        String barra = EmailTemplateHelper.gerarBarraEvolucao(4); // Etapa 4: Despachado
        String resumo = EmailTemplateHelper.gerarResumoFinanceiro(subtotal, frete, total);

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
            "    <h2 style='color:#1E1E1E;font-size:22px;'>🚚 Pedido despachado!</h2>" +
            "    <p style='color:#333;font-size:16px;'>Olá <strong>" + nomeCliente + "</strong>,</p>" +
            "    <p style='color:#333;font-size:16px;'>Seu pedido <strong>#" + pedidoId + "</strong> foi despachado para o endereço informado.</p>" +
            "    <p style='font-size:16px;'><strong>📦 Código de rastreio:</strong> " + codigo + "</p>" +
            "    <p style='color:#555;'>O prazo de entrega será informado pelos Correios.</p>" +
            "    <p><strong>Itens:</strong></p>" +
            itensHtml +
            "    <br>" +
            resumo +
            "    <br>" +
            barra +
            "    <br>" +
            "    <div style='background:#f0f0f0;padding:15px;border-radius:8px;text-align:center;'>" +
            "      <p style='margin:0;color:#333;font-size:14px;'><strong>PORTOBELLA Brechó & Outlet</strong></p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>📍 Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>🕐 Segunda a Sexta, 10h às 18h</p>" +
            "      <p style='margin:0;color:#555;font-size:12px;'>📞 (51) 98233-9066</p>" +
            "    </div>" +
            "    <br>" +
            "    <p style='text-align:center;color:#888;font-size:12px;'>Obrigada por comprar na PORTOBELLA! 💛</p>" +
            "  </td></tr>" +
            "</table></body></html>";

        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }
}