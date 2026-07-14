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
    
//    public static void enviarCupomAssincrono(final String emailDestino, final String corpoHtml, final String idVenda) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String host = "smtp.gmail.com";
//                int portaTls = 587;
//                String usuario = "portobella.brecho@gmail.com";
//                String senha = "mpsihqyoyjnmgkty"; // Lembrete: Se der erro de autenticação, troque pela Senha de App da Google
//
//                try {
//                    // 1. Conexão inicial via Socket TCP convencional (Porta 587 - TLS)
//                    Socket socket = new Socket(host, portaTls);
//                    BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//                    PrintWriter escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
//
//                    lerResposta(leitor); // Saudação do servidor
//                    
//                    // 2. Comando EHLO
//                    escritor.println("EHLO " + host);
//                    lerResposta(leitor);
//
//                    // 3. Comando STARTTLS para criptografar a conexão
//                    escritor.println("STARTTLS");
//                    lerResposta(leitor);
//
//                    // 4. Upgrade do Socket convencional para Socket SSL/TLS seguro
//                    SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
//                    SSLSocket sslSocket = (SSLSocket) ssf.createSocket(socket, host, portaTls, true);
//                    sslSocket.startHandshake();
//
//                    // Atualiza os fluxos de leitura e escrita com a criptografia ativa
//                    leitor = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), "UTF-8"));
//                    escritor = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream(), "UTF-8"), true);
//
//                    // 5. Novo EHLO sob o túnel criptografado
//                    escritor.println("EHLO " + host);
//                    lerResposta(leitor);
//
//                    // 6. Autenticação Base64 no SMTP
//                    escritor.println("AUTH LOGIN");
//                    lerResposta(leitor);
//
//                    // Envia usuário criptografado em Base64
//                    escritor.println(Base64.getEncoder().encodeToString(usuario.getBytes("UTF-8")));
//                    lerResposta(leitor);
//
//                    // Envia a senha criptografada em Base64
//                    escritor.println(Base64.getEncoder().encodeToString(senha.getBytes("UTF-8")));
//                    lerResposta(leitor);
//
//                    // 7. Configuração do Envelope da Mensagem
//                    escritor.println("MAIL FROM:<" + usuario + ">");
//                    lerResposta(leitor);
//
//                    escritor.println("RCPT TO:<" + emailDestino + ">");
//                    lerResposta(leitor);
//
//                    // 8. Início do Bloco de Dados do E-mail
//                    escritor.println("DATA");
//                    lerResposta(leitor);
//
//                    // Cabeçalhos obrigatórios do protocolo para evitar cair no SPAM
//                    escritor.println("From: Portobella Brechó <" + usuario + ">");
//                    escritor.println("To: " + emailDestino);
//                    escritor.println("Subject: Seu Cupom Não Fiscal - Venda #" + idVenda + " - Portobella Brechó & Outlet");
//                    escritor.println("MIME-Version: 1.0");
//                    escritor.println("Content-Type: text/html; charset=utf-8");
//                    escritor.println(); // Linha em branco obrigatória separando cabeçalho do corpo
//
//                    // Conteúdo do Cupom HTML
//                    escritor.println(corpoHtml);
//                    
//                    // Finaliza o bloco DATA com um ponto final sozinho em uma linha
//                    escritor.println(".");
//                    lerResposta(leitor);
//
//                    // 9. Finaliza a sessão com o servidor
//                    escritor.println("QUIT");
//                    lerResposta(leitor);
//
//                    // Fecha as conexões físicas
//                    escritor.close();
//                    leitor.close();
//                    sslSocket.close();
//                    socket.close();
//
//                    System.out.println("Cupom enviado via Socket TCP para: " + emailDestino);
//
//                } catch (Exception ex) {
//                    System.err.println("Erro crítico no envio via Socket: " + ex.getMessage());
//                }
//            }
//            
//            // Método utilitário para ler e limpar o buffer de resposta do Gmail
//            private void lerResposta(BufferedReader leitor) throws Exception {
//                String linha = leitor.readLine();
//                System.out.println("SMTP: " + linha);
//                // Protocolos SMTP podem enviar múltiplas linhas iniciadas por hífen (ex: 250-)
//                while (linha != null && linha.length() >= 4 && linha.charAt(3) == '-') {
//                    linha = leitor.readLine();
//                    System.out.println("SMTP: " + linha);
//                }
//            }
//        }).start();
//    }
     

    // ==========================================
    // CONFIGURAÇÕES SMTP
    // ==========================================
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT_TLS = 587;
    private static final String EMAIL_REMETENTE = "portobella.brecho@gmail.com";
    private static final String SENHA_REMETENTE = "mpsihqyoyjnmgkty";

    // ==========================================
    // MÉTODO BASE DE ENVIO (PRIVADO)
    // ==========================================
    private static void enviarEmailBase(final String destinatario, final String assunto, final String corpoHtml) {
        new Thread(() -> {
            Socket socket = null;
            SSLSocket sslSocket = null;
            BufferedReader leitor = null;
            PrintWriter escritor = null;

            try {
                // 1. Conexão inicial
                socket = new Socket(SMTP_HOST, SMTP_PORT_TLS);
                leitor = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                lerResposta(leitor); // Saudação do servidor

                // 2. EHLO
                escritor.println("EHLO " + SMTP_HOST);
                lerResposta(leitor);

                // 3. STARTTLS
                escritor.println("STARTTLS");
                lerResposta(leitor);

                // 4. Upgrade para SSL
                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) ssf.createSocket(socket, SMTP_HOST, SMTP_PORT_TLS, true);
                sslSocket.startHandshake();

                // Atualiza fluxos
                leitor = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), "UTF-8"));
                escritor = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream(), "UTF-8"), true);

                // 5. EHLO criptografado
                escritor.println("EHLO " + SMTP_HOST);
                lerResposta(leitor);

                // 6. Autenticação
                escritor.println("AUTH LOGIN");
                lerResposta(leitor);
                escritor.println(Base64.getEncoder().encodeToString(EMAIL_REMETENTE.getBytes("UTF-8")));
                lerResposta(leitor);
                escritor.println(Base64.getEncoder().encodeToString(SENHA_REMETENTE.getBytes("UTF-8")));
                lerResposta(leitor);

                // 7. Envelope
                escritor.println("MAIL FROM:<" + EMAIL_REMETENTE + ">");
                lerResposta(leitor);
                escritor.println("RCPT TO:<" + destinatario + ">");
                lerResposta(leitor);

                // 8. Dados
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

                // 9. Encerrar
                escritor.println("QUIT");
                lerResposta(leitor);

                System.out.println("✅ E-mail enviado para: " + destinatario);

            } catch (Exception e) {
                System.err.println("❌ Erro ao enviar e-mail para " + destinatario + ": " + e.getMessage());
            } finally {
                try {
                    if (escritor != null) escritor.close();
                    if (leitor != null) leitor.close();
                    if (sslSocket != null) sslSocket.close();
                    if (socket != null) socket.close();
                } catch (IOException ignored) {}
            }
        }).start();
    }

    // ==========================================
    // MÉTODO AUXILIAR PARA LER RESPOSTAS SMTP
    // ==========================================
    private static void lerResposta(BufferedReader leitor) throws Exception {
        String linha = leitor.readLine();
        System.out.println("SMTP: " + linha);
        while (linha != null && linha.length() >= 4 && linha.charAt(3) == '-') {
            linha = leitor.readLine();
            System.out.println("SMTP: " + linha);
        }
    }

    // ==========================================
    // MÉTODO EXISTENTE (MANTIDO PARA COMPATIBILIDADE)
    // ==========================================
    public static void enviarCupomAssincrono(final String emailDestino, final String corpoHtml, final String idVenda) {
        String assunto = "Seu Cupom Não Fiscal - Venda #" + idVenda + " - Portobella Brechó & Outlet";
        enviarEmailBase(emailDestino, assunto, corpoHtml);
    }

    // ==========================================
    // 1. E-MAIL PARA LOJA – NOVA VENDA CONFIRMADA
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
                "<p>📌 Aguardando confirmação da loja para finalizar o pedido.</p>" +
                "<p><small>Mensagem gerada automaticamente pelo sistema.</small></p>";

        enviarEmailBase("portobella.brecho@gmail.com", assunto, corpoHtml);
    }

    // ==========================================
    // 2. E-MAIL PARA CLIENTE – CONFIRMAÇÃO COM CUPOM
    // ==========================================
    public static void enviarConfirmacaoParaCliente(String emailCliente, String nomeCliente,
                                                    String pedidoId, double valor, String itens) {
        String assunto = "✅ Pagamento Confirmado - Pedido #" + pedidoId;

        String corpoHtml = "<h2>✅ Pagamento Confirmado - PORTOBELLA</h2>" +
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

    // ==========================================
    // 3. E-MAIL PARA CLIENTE – REJEIÇÃO DO PAGAMENTO
    // ==========================================
    public static void enviarRejeicaoParaCliente(String emailCliente, String nomeCliente,
                                                 String pedidoId, String motivo) {
        String assunto = "❌ Pagamento Rejeitado - Pedido #" + pedidoId;

        String corpoHtml = "<h2>❌ Pagamento Rejeitado - PORTOBELLA</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Infelizmente seu pagamento do pedido #" + pedidoId + " <strong>não foi aprovado</strong>.</p>" +
                "<p><strong>Motivo:</strong> " + (motivo != null ? motivo : "Não informado") + "</p>" +
                "<p>Entre em contato conosco para mais informações.</p>" +
                "<br><p>Equipe PORTOBELLA</p>";

        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    // ==========================================
    // 4. E-MAIL PARA CLIENTE – PRONTO PARA RETIRADA
    // ==========================================
    public static void enviarProntoParaRetirada(String emailCliente, String nomeCliente, String pedidoId) {
        String assunto = "📦 Pedido pronto para retirada - #" + pedidoId;

        String corpoHtml = "<h2>📦 Pedido pronto para retirada!</h2>" +
                "<p>Olá " + nomeCliente + ",</p>" +
                "<p>Seu pedido #" + pedidoId + " já está disponível na loja.</p>" +
                "<p><strong>📍 Endereço:</strong> Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
                "<p><strong>🕐 Horário:</strong> Segunda a Sexta, 10h às 18h</p>" +
                "<p>⚠️ <strong>Não se esqueça:</strong> Leve seu documento de identificação e o número do pedido.</p>" +
                "<br><p>Obrigado por comprar na PORTOBELLA! 💛</p>";

        enviarEmailBase(emailCliente, assunto, corpoHtml);
    }

    // ==========================================
    // 5. E-MAIL PARA CLIENTE – PEDIDO DESPACHADO
    // ==========================================
    public static void enviarPedidoDespachado(String emailCliente, String nomeCliente,
                                              String pedidoId, String codigoRastreio) {
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

    // ==========================================
    // 6. E-MAIL GENÉRICO (PARA USO GERAL)
    // ==========================================
    public static void enviarEmailGenerico(String destinatario, String assunto, String corpoHtml) {
        enviarEmailBase(destinatario, assunto, corpoHtml);
    }
}


