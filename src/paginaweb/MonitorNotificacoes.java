/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paginaweb;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class MonitorNotificacoes {

    private static final Gson gson = new Gson();
    private static final String PASTA_NOTIFICACOES = System.getProperty("user.home") + "/Desktop/notificacoes_venda";
    private static boolean executando = true;
    private static JFrame popupFrame;

    public static void iniciarMonitor() {
        System.out.println("🔔 Iniciando monitor de notificações...");
        System.out.println("📁 Pasta: " + PASTA_NOTIFICACOES);

        // Criar pasta se não existir
        new File(PASTA_NOTIFICACOES).mkdirs();

        // Iniciar thread de monitoramento
        new Thread(() -> {
            while (executando) {
                try {
                    // Verifica arquivos de notificação
                    File pasta = new File(PASTA_NOTIFICACOES);
                    File[] arquivos = pasta.listFiles((dir, name) -> 
                        name.startsWith("venda_") && name.endsWith(".json") && !name.contains("resposta"));

                    if (arquivos != null) {
                        for (File arquivo : arquivos) {
                            try {
                                // Lê a notificação
                                String json = new String(Files.readAllBytes(arquivo.toPath()));
                                JsonObject dados = gson.fromJson(json, JsonObject.class);

                                // Exibe o popup
                                exibirPopup(dados, arquivo.getName());

                                // Aguarda resposta do usuário
                                // A resposta é salva em um arquivo de resposta
                                String pedidoId = dados.get("pedidoId").getAsString();
                                String respostaArquivo = PASTA_NOTIFICACOES + "/resposta_" + pedidoId + ".json";

                                // Aguarda até o popup ser fechado
                                while (popupFrame != null && popupFrame.isVisible()) {
                                    Thread.sleep(500);
                                }

                            } catch (JsonSyntaxException | IOException | InterruptedException e) {
                                System.err.println("❌ Erro ao processar notificação: " + e.getMessage());
                            }
                        }
                    }

                    Thread.sleep(2000); // Verifica a cada 2 segundos

                } catch (InterruptedException e) {
                    System.out.println("⏹️ Monitor interrompido");
                    break;
                } catch (Exception e) {
                    System.err.println("❌ Erro no monitor: " + e.getMessage());
                }
            }
        }).start();
    }

    // ==========================================
    // EXIBIR POPUP DE NOTIFICAÇÃO
    // ==========================================
    private static void exibirPopup(JsonObject dados, String nomeArquivo) {
        SwingUtilities.invokeLater(() -> {
            // ==========================================
            // 🔥 SINAL SONORO (BIP)
            // ==========================================
            Toolkit.getDefaultToolkit().beep();
            // Alternativa: tocar som personalizado
            try {
                // Tenta tocar som do sistema
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao tocar som: " + e.getMessage());
            }

            // ==========================================
            // DADOS DA NOTIFICAÇÃO
            // ==========================================
            String pedidoId = dados.get("pedidoId").getAsString();
            String cliente = dados.get("cliente").getAsString();
            String telefone = dados.has("telefone") ? dados.get("telefone").getAsString() : "Não informado";
            double valor = dados.get("valor").getAsDouble();
            String codPeca = dados.get("codPeca").getAsString();
            String meioPagamento = dados.get("meioPagamento").getAsString();
            boolean retirarLoja = dados.get("retirarLoja").getAsBoolean();
            String endereco = dados.get("endereco").getAsString();
            String data = dados.get("data").getAsString();

            // ==========================================
            // CRIAR POPUP
            // ==========================================
            popupFrame = new JFrame("🔔 NOVA VENDA - PORTOBELLA");
            popupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            popupFrame.setSize(500, 550);
            popupFrame.setLocationRelativeTo(null);
            popupFrame.setAlwaysOnTop(true);

            // Painel principal
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // ==========================================
            // HEADER
            // ==========================================
            JLabel lblTitulo = new JLabel("🛍️ NOVA VENDA ONLINE", JLabel.CENTER);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
            lblTitulo.setForeground(new Color(0, 158, 227));
            panel.add(lblTitulo, BorderLayout.NORTH);

            // ==========================================
            // DADOS
            // ==========================================
            JPanel dadosPanel = new JPanel();
            dadosPanel.setLayout(new GridLayout(0, 2, 10, 8));
            dadosPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            // Formatação dos labels
            Font labelFont = new Font("Arial", Font.BOLD, 13);
            Font valueFont = new Font("Arial", Font.PLAIN, 13);

            addCampo(dadosPanel, "📋 Pedido:", pedidoId, labelFont, valueFont);
            addCampo(dadosPanel, "👤 Cliente:", cliente, labelFont, valueFont);
            addCampo(dadosPanel, "📱 Telefone:", telefone, labelFont, valueFont);
            addCampo(dadosPanel, "💰 Valor:", "R$ " + String.format("%.2f", valor).replace(".", ","), labelFont, valueFont);
            addCampo(dadosPanel, "🏷️ Código:", codPeca, labelFont, valueFont);
            addCampo(dadosPanel, "💳 Pagamento:", meioPagamento.toUpperCase(), labelFont, valueFont);
            
            String tipoEntrega = retirarLoja ? "📍 RETIRADA NA LOJA" : "🚚 ENTREGA";
            Color corEntrega = retirarLoja ? new Color(0, 166, 80) : new Color(0, 158, 227);
            addCampoColorido(dadosPanel, "📦 Entrega:", tipoEntrega, labelFont, valueFont, corEntrega);
            
            addCampo(dadosPanel, "📅 Data:", data, labelFont, valueFont);
            addCampo(dadosPanel, "📍 Endereço:", endereco.length() > 40 ? endereco.substring(0, 40) + "..." : endereco, labelFont, valueFont);

            panel.add(dadosPanel, BorderLayout.CENTER);

            // ==========================================
            // BOTÕES
            // ==========================================
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

            // Botão APROVAR (Verde)
            JButton btnAprovar = new JButton("✅ APROVAR VENDA");
            btnAprovar.setBackground(new Color(0, 166, 80));
            btnAprovar.setForeground(Color.WHITE);
            btnAprovar.setFont(new Font("Arial", Font.BOLD, 14));
            btnAprovar.setPreferredSize(new Dimension(200, 45));
            btnAprovar.addActionListener(e -> {
                responderNotificacao(pedidoId, true);
                popupFrame.dispose();
                popupFrame = null;
            });

            // Botão REJEITAR (Vermelho)
            JButton btnRejeitar = new JButton("❌ REJEITAR VENDA");
            btnRejeitar.setBackground(new Color(200, 50, 50));
            btnRejeitar.setForeground(Color.WHITE);
            btnRejeitar.setFont(new Font("Arial", Font.BOLD, 14));
            btnRejeitar.setPreferredSize(new Dimension(200, 45));
            btnRejeitar.addActionListener(e -> {
                responderNotificacao(pedidoId, false);
                popupFrame.dispose();
                popupFrame = null;
            });

            btnPanel.add(btnAprovar);
            btnPanel.add(btnRejeitar);
            panel.add(btnPanel, BorderLayout.SOUTH);

            popupFrame.add(panel);
            popupFrame.setVisible(true);

            // ==========================================
            // SINAL SONORO NOVO (BIP DUPLO)
            // ==========================================
            try {
                Toolkit.getDefaultToolkit().beep();
                Thread.sleep(300);
                Toolkit.getDefaultToolkit().beep();
            } catch (InterruptedException e) {
                System.err.println("⚠️ Erro ao tocar som: " + e.getMessage());
            }
        });
    }

    // ==========================================
    // ADICIONAR CAMPO AO PAINEL
    // ==========================================
    private static void addCampo(JPanel panel, String label, String valor, Font labelFont, Font valueFont) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(labelFont);
        lbl.setForeground(new Color(100, 100, 100));
        panel.add(lbl);

        JLabel val = new JLabel(valor);
        val.setFont(valueFont);
        val.setForeground(Color.BLACK);
        panel.add(val);
    }

    // ==========================================
    // ADICIONAR CAMPO COLORIDO AO PAINEL
    // ==========================================
    private static void addCampoColorido(JPanel panel, String label, String valor, Font labelFont, Font valueFont, Color cor) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(labelFont);
        lbl.setForeground(new Color(100, 100, 100));
        panel.add(lbl);

        JLabel val = new JLabel(valor);
        val.setFont(valueFont);
        val.setForeground(cor);
        val.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(val);
    }

    // ==========================================
    // RESPONDER NOTIFICAÇÃO
    // ==========================================
    private static void responderNotificacao(String pedidoId, boolean aprovado) {
        try {
            String respostaArquivo = PASTA_NOTIFICACOES + "/resposta_" + pedidoId + ".json";
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("pedidoId", pedidoId);
            resposta.put("aprovado", aprovado);
            resposta.put("data", new java.util.Date().toString());

            String json = gson.toJson(resposta);
            try (FileWriter writer = new FileWriter(respostaArquivo)) {
                writer.write(json);
            }

            System.out.println("📤 Resposta enviada: " + (aprovado ? "APROVADO" : "REJEITADO"));

        } catch (IOException e) {
            System.err.println("❌ Erro ao responder: " + e.getMessage());
        }
    }

    // ==========================================
    // PARAR MONITOR
    // ==========================================
    public static void pararMonitor() {
        executando = false;
        System.out.println("⏹️ Monitor parado.");
    }

    // ==========================================
    // MAIN PARA TESTE
    // ==========================================
    public static void main(String[] args) {
        iniciarMonitor();
        
        // Mantém o programa rodando
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

