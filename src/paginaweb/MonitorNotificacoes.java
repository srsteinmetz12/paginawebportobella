package paginaweb;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
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

        new File(PASTA_NOTIFICACOES).mkdirs();

        new Thread(() -> {
            while (executando) {
                try {
                    File pasta = new File(PASTA_NOTIFICACOES);
                    File[] arquivos = pasta.listFiles((dir, name) -> 
                        name.startsWith("venda_") && name.endsWith(".json") && !name.contains("resposta"));

                    if (arquivos != null) {
                        for (File arquivo : arquivos) {
                            try {
                                String json = new String(Files.readAllBytes(arquivo.toPath()));
                                JsonObject dados = gson.fromJson(json, JsonObject.class);
                                exibirPopup(dados, arquivo.getName());
                            } catch (JsonSyntaxException | IOException e) {
                                System.err.println("❌ Erro ao processar notificação: " + e.getMessage());
                            }
                        }
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
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
            // SINAL SONORO (BIP DUPLO)
            // ==========================================
            Toolkit.getDefaultToolkit().beep();
            try { Thread.sleep(300); } catch (InterruptedException e) {}
            Toolkit.getDefaultToolkit().beep();

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
            popupFrame.setSize(550, 600);
            popupFrame.setLocationRelativeTo(null);
            popupFrame.setAlwaysOnTop(true);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // ==========================================
            // HEADER
            // ==========================================
            JPanel headerPanel = new JPanel(new BorderLayout());
            JLabel lblTitulo = new JLabel("🛍️ NOVA VENDA ONLINE", JLabel.CENTER);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
            lblTitulo.setForeground(new Color(0, 158, 227));
            headerPanel.add(lblTitulo, BorderLayout.CENTER);
            
            JLabel lblData = new JLabel(data, JLabel.RIGHT);
            lblData.setFont(new Font("Arial", Font.PLAIN, 11));
            lblData.setForeground(Color.GRAY);
            headerPanel.add(lblData, BorderLayout.SOUTH);
            panel.add(headerPanel, BorderLayout.NORTH);

            // ==========================================
            // DADOS
            // ==========================================
            JPanel dadosPanel = new JPanel();
            dadosPanel.setLayout(new GridLayout(0, 2, 10, 10));
            dadosPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

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
            
            String enderecoExibicao = endereco.length() > 50 ? endereco.substring(0, 50) + "..." : endereco;
            addCampo(dadosPanel, "📍 Endereço:", enderecoExibicao, labelFont, valueFont);

            panel.add(dadosPanel, BorderLayout.CENTER);

            // ==========================================
            // INSTRUÇÕES
            // ==========================================
            JPanel instrucoesPanel = new JPanel();
            instrucoesPanel.setLayout(new BorderLayout());
            JLabel lblInstrucoes = new JLabel(
                "<html><center><font color='#FF6B6B'>⚠️ <b>ATENÇÃO ATENDENTE</b></font><br>" +
                "<font size='2'>Verifique se o pagamento foi confirmado no banco ou Mercado Pago.<br>" +
                "Clique em <b>CONFIRMAR PAGAMENTO</b> para registrar a venda.</font></center></html>"
            );
            lblInstrucoes.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            instrucoesPanel.add(lblInstrucoes, BorderLayout.CENTER);
            panel.add(instrucoesPanel, BorderLayout.NORTH);

            // ==========================================
            // BOTÕES
            // ==========================================
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

            JButton btnConfirmar = new JButton("✅ CONFIRMAR PAGAMENTO");
            btnConfirmar.setBackground(new Color(0, 166, 80));
            btnConfirmar.setForeground(Color.WHITE);
            btnConfirmar.setFont(new Font("Arial", Font.BOLD, 16));
            btnConfirmar.setPreferredSize(new Dimension(250, 50));
            btnConfirmar.addActionListener(e -> {
                responderNotificacao(pedidoId, true);
                popupFrame.dispose();
                popupFrame = null;
            });

            JButton btnRejeitar = new JButton("❌ REJEITAR");
            btnRejeitar.setBackground(new Color(200, 50, 50));
            btnRejeitar.setForeground(Color.WHITE);
            btnRejeitar.setFont(new Font("Arial", Font.BOLD, 14));
            btnRejeitar.setPreferredSize(new Dimension(150, 50));
            btnRejeitar.addActionListener(e -> {
                responderNotificacao(pedidoId, false);
                popupFrame.dispose();
                popupFrame = null;
            });

            btnPanel.add(btnConfirmar);
            btnPanel.add(btnRejeitar);
            panel.add(btnPanel, BorderLayout.SOUTH);

            popupFrame.add(panel);
            popupFrame.setVisible(true);
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
        val.setForeground(Color.WHITE);
        panel.add(val);
    }

    private static void addCampoColorido(JPanel panel, String label, String valor, Font labelFont, Font valueFont, Color cor) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(labelFont);
        lbl.setForeground(new Color(100, 100, 100));
        panel.add(lbl);

        JLabel val = new JLabel(valor);
        val.setFont(new Font("Arial", Font.BOLD, 13));
        val.setForeground(cor);
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

            try (FileWriter writer = new FileWriter(respostaArquivo)) {
                gson.toJson(resposta, writer);
            }

            System.out.println("📤 Resposta enviada: " + (aprovado ? "APROVADO ✅" : "REJEITADO ❌"));

        } catch (JsonIOException | IOException e) {
            System.err.println("❌ Erro ao responder: " + e.getMessage());
        }
    }

    public static void pararMonitor() {
        executando = false;
    }

    public static void main(String[] args) {
        iniciarMonitor();
        while (true) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
        }
    }
}