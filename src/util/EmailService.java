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

public class EmailService {

    // ==========================================
    // CONFIGURAÇÕES SMTP
    // ==========================================
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT_TLS = 465;
    private static final String EMAIL_REMETENTE = "portobella.brecho@gmail.com";
    private static final String SENHA_REMETENTE = "mpsihqyoyjnmgkty";

    // ==========================================
    // MÉTODO BASE DE ENVIO (COM LOGS)
    // ==========================================
    private static void enviarEmailBase(final String destinatario, final String assunto, final String corpoHtml) {
        if (destinatario == null || destinatario.isEmpty() || "Não informado".equals(destinatario)) {
            System.err.println("⚠️ [EMAIL] Destinatário inválido. E-mail não enviado.");
            return;
        }

        System.out.println("📧 [EMAIL] Iniciando envio para: " + destinatario);
        System.out.println("📧 [EMAIL] Assunto: " + assunto);
        System.out.println("📧 [EMAIL] Corpo (primeiros 100 chars): " + 
                           (corpoHtml != null ? corpoHtml.substring(0, Math.min(100, corpoHtml.length())) : "null") + "...");

        new Thread(() -> {
            long inicio = System.currentTimeMillis();
            Socket socket = null;
            SSLSocket sslSocket = null;
            BufferedReader leitor = null;
            PrintWriter escritor = null;

            try {
                System.out.println("📧 [EMAIL] Conectando a " + SMTP_HOST + ":" + SMTP_PORT_TLS + "...");
                socket = new Socket(SMTP_HOST, SMTP_PORT_TLS);
                socket.setSoTimeout(20000); // 20 segundos de timeout
                System.out.println("📧 [EMAIL] Socket conectado.");

                leitor = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                // Saudação
                String saudacao = lerResposta(leitor);
                System.out.println("📧 [EMAIL] Saudação: " + saudacao);

                // EHLO
                escritor.println("EHLO " + SMTP_HOST);
                String ehlo = lerResposta(leitor);
                System.out.println("📧 [EMAIL] EHLO: " + ehlo);

                // STARTTLS (necessário para porta 587, mas na 465 já é SSL direto)
                // Como estamos usando porta 465, não precisamos.

                // Upgrade para SSL
                System.out.println("📧 [EMAIL] Criando SSLSocket...");
                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) ssf.createSocket(socket, SMTP_HOST, SMTP_PORT_TLS, true);
                sslSocket.setSoTimeout(20000);
                sslSocket.startHandshake();
                System.out.println("📧 [EMAIL] Handshake SSL concluído.");

                // Atualiza fluxos
                leitor = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), "UTF-8"));
                escritor = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream(), "UTF-8"), true);

                // EHLO criptografado
                escritor.println("EHLO " + SMTP_HOST);
                lerResposta(leitor);
                System.out.println("📧 [EMAIL] EHLO (SSL) OK.");

                // Autenticação
                System.out.println("📧 [EMAIL] Autenticando...");
                escritor.println("AUTH LOGIN");
                lerResposta(leitor);
                escritor.println(Base64.getEncoder().encodeToString(EMAIL_REMETENTE.getBytes("UTF-8")));
                lerResposta(leitor);
                escritor.println(Base64.getEncoder().encodeToString(SENHA_REMETENTE.getBytes("UTF-8")));
                String authResp = lerResposta(leitor);
                System.out.println("📧 [EMAIL] Autenticação: " + authResp);

                // Envelope
                System.out.println("📧 [EMAIL] Enviando MAIL FROM...");
                escritor.println("MAIL FROM:<" + EMAIL_REMETENTE + ">");
                lerResposta(leitor);

                System.out.println("📧 [EMAIL] Enviando RCPT TO...");
                escritor.println("RCPT TO:<" + destinatario + ">");
                lerResposta(leitor);

                // Dados
                System.out.println("📧 [EMAIL] Enviando DATA...");
                escritor.println("DATA");
                lerResposta(leitor);

                // Cabeçalhos
                escritor.println("From: PORTOBELLA Brechó <" + EMAIL_REMETENTE + ">");
                escritor.println("To: " + destinatario);
                escritor.println("Subject: " + assunto);
                escritor.println("MIME-Version: 1.0");
                escritor.println("Content-Type: text/html; charset=UTF-8");
                escritor.println(); // Linha em branco

                // Corpo
                escritor.println(corpoHtml);
                escritor.println(".");
                lerResposta(leitor);
                System.out.println("📧 [EMAIL] Dados enviados.");

                // QUIT
                escritor.println("QUIT");
                lerResposta(leitor);
                System.out.println("📧 [EMAIL] QUIT enviado.");

                long fim = System.currentTimeMillis();
                System.out.println("✅ [EMAIL] E-mail enviado para " + destinatario + " em " + (fim - inicio) + "ms.");

            } catch (Exception e) {
                long fim = System.currentTimeMillis();
                System.err.println("❌ [EMAIL] Erro ao enviar e-mail para " + destinatario + " após " + (fim - inicio) + "ms.");
                System.err.println("❌ [EMAIL] Mensagem: " + e.getMessage());
                e.printStackTrace(); // 🔥 IMPRIME O STACK TRACE COMPLETO
            } finally {
                try {
                    if (escritor != null) escritor.close();
                    if (leitor != null) leitor.close();
                    if (sslSocket != null) sslSocket.close();
                    if (socket != null) socket.close();
                    System.out.println("📧 [EMAIL] Conexões fechadas.");
                } catch (IOException ignored) {}
            }
        }).start();
    }

    // ==========================================
    // MÉTODO AUXILIAR PARA LER RESPOSTAS SMTP
    // ==========================================
    private static String lerResposta(BufferedReader leitor) throws Exception {
        StringBuilder sb = new StringBuilder();
        String linha = leitor.readLine();
        sb.append(linha);
        System.out.println("SMTP: " + linha);
        while (linha != null && linha.length() >= 4 && linha.charAt(3) == '-') {
            linha = leitor.readLine();
            sb.append("\n").append(linha);
            System.out.println("SMTP: " + linha);
        }
        return sb.toString();
    }

    // ==========================================
    // MÉTODOS PÚBLICOS (COM LOGS)
    // ==========================================
    public static void enviarCupomAssincrono(final String emailDestino, final String corpoHtml, final String idVenda) {
        System.out.println("📧 [CUPOM] Solicitado envio para: " + emailDestino);
        String assunto = "Seu Cupom Não Fiscal - Venda #" + idVenda + " - PORTOBELLA Brechó & Outlet";
        enviarEmailBase(emailDestino, assunto, corpoHtml);
    }

    public static void enviarNovaVendaParaLoja(String pedidoId, String cliente, String emailCliente,
                                               String telefone, double valorTotal, String meioPagamento,
                                               boolean retirarLoja, String endereco, String itens) {
        System.out.println("📧 [LOJA] Nova venda - Pedido #" + pedidoId);
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
                "<p>📌 Aguardando confirmação da loja para finalizar o pedido.</p>" +
                "<p><small>Mensagem gerada automaticamente pelo sistema.</small></p>";

        // 🔥 DESTINATÁRIO ALTERADO PARA TESTE
        String destinatarioLoja = "deka.modass@gmail.com";
        System.out.println("📧 [LOJA] Enviando para: " + destinatarioLoja);
        enviarEmailBase(destinatarioLoja, assunto, corpoHtml);
    }

    public static void enviarConfirmacaoParaCliente(String emailCliente, String nomeCliente,
                                                    String pedidoId, double valor, String itens) {
        System.out.println("📧 [CONFIRMAÇÃO] Cliente " + emailCliente + " - Pedido #" + pedidoId);
        String assunto = "✅ Pagamento Confirmado - Pedido #" + pedidoId;
        String corpoHtml = "<h2>✅ Pagamento Confirmado - PORTOBELLA Brechó & Outlet</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Seu pagamento do pedido #" + pedidoId + " foi <strong>confirmado</strong>!</p>" +
                "<p><strong>Valor:</strong> R$ " + String.format("%.2f", valor) + "</p>" +
                "<p><strong>Itens:</strong> " + itens + "</p>" +
                "<br><p><strong>PORTOBELLA Brechó & Outlet</strong><br>" +
                "Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
                "<p>Pedido: #" + pedidoId + "</p>" +
                "<p>Obrigado pela preferência! 💛</p>";
        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    public static void enviarRejeicaoParaCliente(String emailCliente, String nomeCliente,
                                                 String pedidoId, String motivo) {
        System.out.println("📧 [REJEIÇÃO] Cliente " + emailCliente + " - Pedido #" + pedidoId);
        String assunto = "❌ Pagamento Rejeitado - Pedido #" + pedidoId;
        String corpoHtml = "<h2>❌ Pagamento Rejeitado - PORTOBELLA Brechó & Outlet</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Infelizmente seu pagamento do pedido #" + pedidoId + " <strong>não foi aprovado</strong>.</p>" +
                "<p><strong>Motivo:</strong> " + (motivo != null ? motivo : "Não informado") + "</p>" +
                "<p>Entre em contato conosco para mais informações.</p>" +
                "<br><p>Equipe PORTOBELLA Brechó & Outlet</p>";
        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    public static void enviarProntoParaRetirada(String emailCliente, String nomeCliente, String pedidoId) {
        System.out.println("📧 [RETIRADA] Cliente " + emailCliente + " - Pedido #" + pedidoId);
        String assunto = "📦 Pedido pronto para retirada - #" + pedidoId;
        String corpoHtml = "<h2>📦 Pedido pronto para retirada!</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Seu pedido #" + pedidoId + " já está disponível na loja.</p>" +
                "<p><strong>📍 Endereço:</strong> Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
                "<p><strong>🕐 Horário:</strong> Segunda a Sexta, 10h às 18h</p>" +
                "<p>⚠️ <strong>Não se esqueça:</strong> Leve seu documento de identificação e o número do pedido.</p>" +
                "<br><p>Obrigado por comprar na PORTOBELLA Brechó & Outlet! 💛</p>";
        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    public static void enviarPedidoDespachado(String emailCliente, String nomeCliente,
                                              String pedidoId, String codigoRastreio) {
        System.out.println("📧 [DESPACHO] Cliente " + emailCliente + " - Pedido #" + pedidoId);
        String assunto = "🚚 Pedido despachado - #" + pedidoId;
        String codigo = (codigoRastreio != null && !codigoRastreio.isEmpty()) ?
                codigoRastreio : "será enviado em breve";
        String corpoHtml = "<h2>🚚 Pedido despachado!</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Seu pedido #" + pedidoId + " foi <strong>despachado</strong> para o endereço informado.</p>" +
                "<p><strong>📦 Código de rastreio:</strong> " + codigo + "</p>" +
                "<p>O prazo de entrega será informado pelos Correios.</p>" +
                "<br><p>Obrigado por comprar na PORTOBELLA! 💛</p>";
        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    public static void enviarEmailGenerico(String destinatario, String assunto, String corpoHtml) {
        System.out.println("📧 [GENERICO] Enviando para: " + destinatario);
        enviarEmailBase(destinatario, assunto, corpoHtml);
    }
}