package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class EmailService {
    
    public static void enviarCupomAssincrono(final String emailDestino, final String corpoHtml, final String idVenda) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String host = "smtp.gmail.com";
                int portaTls = 587;
                String usuario = "portobella.brecho@gmail.com";
                String senha = "mpsihqyoyjnmgkty"; // Lembrete: Se der erro de autenticação, troque pela Senha de App da Google

                try {
                    // 1. Conexão inicial via Socket TCP convencional (Porta 587 - TLS)
                    Socket socket = new Socket(host, portaTls);
                    BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    PrintWriter escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                    lerResposta(leitor); // Saudação do servidor
                    
                    // 2. Comando EHLO
                    escritor.println("EHLO " + host);
                    lerResposta(leitor);

                    // 3. Comando STARTTLS para criptografar a conexão
                    escritor.println("STARTTLS");
                    lerResposta(leitor);

                    // 4. Upgrade do Socket convencional para Socket SSL/TLS seguro
                    SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    SSLSocket sslSocket = (SSLSocket) ssf.createSocket(socket, host, portaTls, true);
                    sslSocket.startHandshake();

                    // Atualiza os fluxos de leitura e escrita com a criptografia ativa
                    leitor = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), "UTF-8"));
                    escritor = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream(), "UTF-8"), true);

                    // 5. Novo EHLO sob o túnel criptografado
                    escritor.println("EHLO " + host);
                    lerResposta(leitor);

                    // 6. Autenticação Base64 no SMTP
                    escritor.println("AUTH LOGIN");
                    lerResposta(leitor);

                    // Envia usuário criptografado em Base64
                    escritor.println(Base64.getEncoder().encodeToString(usuario.getBytes("UTF-8")));
                    lerResposta(leitor);

                    // Envia a senha criptografada em Base64
                    escritor.println(Base64.getEncoder().encodeToString(senha.getBytes("UTF-8")));
                    lerResposta(leitor);

                    // 7. Configuração do Envelope da Mensagem
                    escritor.println("MAIL FROM:<" + usuario + ">");
                    lerResposta(leitor);

                    escritor.println("RCPT TO:<" + emailDestino + ">");
                    lerResposta(leitor);

                    // 8. Início do Bloco de Dados do E-mail
                    escritor.println("DATA");
                    lerResposta(leitor);

                    // Cabeçalhos obrigatórios do protocolo para evitar cair no SPAM
                    escritor.println("From: Portobella Brechó <" + usuario + ">");
                    escritor.println("To: " + emailDestino);
                    escritor.println("Subject: Seu Cupom Não Fiscal - Venda #" + idVenda + " - Portobella Brechó & Outlet");
                    escritor.println("MIME-Version: 1.0");
                    escritor.println("Content-Type: text/html; charset=utf-8");
                    escritor.println(); // Linha em branco obrigatória separando cabeçalho do corpo

                    // Conteúdo do Cupom HTML
                    escritor.println(corpoHtml);
                    
                    // Finaliza o bloco DATA com um ponto final sozinho em uma linha
                    escritor.println(".");
                    lerResposta(leitor);

                    // 9. Finaliza a sessão com o servidor
                    escritor.println("QUIT");
                    lerResposta(leitor);

                    // Fecha as conexões físicas
                    escritor.close();
                    leitor.close();
                    sslSocket.close();
                    socket.close();

                    System.out.println("Cupom enviado via Socket TCP para: " + emailDestino);

                } catch (Exception ex) {
                    System.err.println("Erro crítico no envio via Socket: " + ex.getMessage());
                }
            }

            // Método utilitário para ler e limpar o buffer de resposta do Gmail
            private void lerResposta(BufferedReader leitor) throws Exception {
                String linha = leitor.readLine();
                System.out.println("SMTP: " + linha);
                // Protocolos SMTP podem enviar múltiplas linhas iniciadas por hífen (ex: 250-)
                while (linha != null && linha.length() >= 4 && linha.charAt(3) == '-') {
                    linha = leitor.readLine();
                    System.out.println("SMTP: " + linha);
                }
            }
        }).start();
    }
}
