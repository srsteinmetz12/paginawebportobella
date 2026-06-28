package paginaweb;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import connection.ConnectionDB;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificacaoVendasService {

    private static final Gson gson = new Gson();
    private static ScheduledExecutorService scheduler;
    private static boolean executando = true;
    private static JFrame popupFrame;

    // ==========================================
    // INICIAR O SERVIÇO DE NOTIFICAÇÕES
    // ==========================================
    public static void iniciar() {
        System.out.println("🔔 ========================================");
        System.out.println("🔔 SERVIÇO DE NOTIFICAÇÕES - PORTOBELLA");
        System.out.println("🔔 ========================================");
        System.out.println("🔄 Consultando notificações a cada 5 segundos...");
        System.out.println("🔔 ========================================");

        scheduler = Executors.newScheduledThreadPool(1);
        
        scheduler.scheduleAtFixedRate(() -> {
            if (executando) {
                try {
                    verificarNotificacoes();
                } catch (Exception e) {
                    System.err.println("❌ Erro ao verificar notificações: " + e.getMessage());
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    // ==========================================
    // VERIFICAR NOTIFICAÇÕES NO BANCO
    // ==========================================
    private static void verificarNotificacoes() {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            
            String sql = "SELECT id, pedido_id, cod_peca, cliente, telefone, valor, " +
                         "meio_pagamento, endereco, retirar_loja, itens, data_criacao " +
                         "FROM notificacoes_pendentes " +
                         "WHERE status = 'PENDENTE' AND lida = 0 " +
                         "ORDER BY data_criacao ASC LIMIT 10";
            
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String pedidoId = rs.getString("pedido_id");
                String cliente = rs.getString("cliente");
                String telefone = rs.getString("telefone");
                double valor = rs.getDouble("valor");
                String codPeca = rs.getString("cod_peca");
                String meioPagamento = rs.getString("meio_pagamento");
                boolean retirarLoja = rs.getBoolean("retirar_loja");
                String endereco = rs.getString("endereco");
                String dataCriacao = rs.getString("data_criacao");

                System.out.println("📥 Nova notificação encontrada: " + pedidoId);

                // ==========================================
                // EXIBIR POPUP
                // ==========================================
                exibirPopup(id, pedidoId, cliente, telefone, valor, codPeca, 
                            meioPagamento, retirarLoja, endereco, dataCriacao);
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("❌ Erro ao consultar banco: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // EXIBIR POPUP
    // ==========================================
    private static void exibirPopup(int id, String pedidoId, String cliente, String telefone,
                                     double valor, String codPeca, String meioPagamento,
                                     boolean retirarLoja, String endereco, String dataCriacao) {
        
        SwingUtilities.invokeLater(() -> {
            // SINAL SONORO
            Toolkit.getDefaultToolkit().beep();
            try { Thread.sleep(300); } catch (InterruptedException e) {}
            Toolkit.getDefaultToolkit().beep();

            // CRIAR POPUP
            popupFrame = new JFrame("🔔 NOVA VENDA - PORTOBELLA");
            popupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            popupFrame.setSize(580, 620);
            popupFrame.setLocationRelativeTo(null);
            popupFrame.setAlwaysOnTop(true);
            popupFrame.setResizable(false);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // HEADER
            JPanel headerPanel = new JPanel(new BorderLayout());
            JLabel lblTitulo = new JLabel("🛍️ NOVA VENDA ONLINE", JLabel.CENTER);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
            lblTitulo.setForeground(new Color(0, 158, 227));
            headerPanel.add(lblTitulo, BorderLayout.CENTER);
            
            JLabel lblData = new JLabel("📅 " + dataCriacao, JLabel.RIGHT);
            lblData.setFont(new Font("Arial", Font.PLAIN, 11));
            lblData.setForeground(Color.GRAY);
            headerPanel.add(lblData, BorderLayout.SOUTH);
            panel.add(headerPanel, BorderLayout.NORTH);

            // DADOS
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

            // INSTRUÇÕES
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

            // BOTÕES
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

            JButton btnConfirmar = new JButton("✅ CONFIRMAR PAGAMENTO");
            btnConfirmar.setBackground(new Color(0, 166, 80));
            btnConfirmar.setForeground(Color.WHITE);
            btnConfirmar.setFont(new Font("Arial", Font.BOLD, 16));
            btnConfirmar.setPreferredSize(new Dimension(250, 50));
            btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnConfirmar.addActionListener(e -> {
                responderNotificacao(id, pedidoId, true);
                popupFrame.dispose();
                popupFrame = null;
            });

            JButton btnRejeitar = new JButton("❌ REJEITAR");
            btnRejeitar.setBackground(new Color(200, 50, 50));
            btnRejeitar.setForeground(Color.WHITE);
            btnRejeitar.setFont(new Font("Arial", Font.BOLD, 14));
            btnRejeitar.setPreferredSize(new Dimension(150, 50));
            btnRejeitar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRejeitar.addActionListener(e -> {
                responderNotificacao(id, pedidoId, false);
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
    private static void responderNotificacao(int id, String pedidoId, boolean aprovado) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            
            String status = aprovado ? "CONFIRMADO" : "REJEITADO";
            String sql = "UPDATE notificacoes_pendentes SET status = ?, lida = 1, data_confirmacao = NOW() WHERE id = ?";
            
            stmt = con.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Notificação #" + id + " atualizada para " + status);
                
                if (aprovado) {
                    // Buscar dados para registrar a venda
                    String sqlSelect = "SELECT cod_peca, cliente, valor, meio_pagamento, endereco, retirar_loja, telefone FROM notificacoes_pendentes WHERE id = ?";
                    PreparedStatement stmtSelect = con.prepareStatement(sqlSelect);
                    stmtSelect.setInt(1, id);
                    ResultSet rs = stmtSelect.executeQuery();
                    
                    if (rs.next()) {
                        String codPeca = rs.getString("cod_peca");
                        String cliente = rs.getString("cliente");
                        double valor = rs.getDouble("valor");
                        String meioPagamento = rs.getString("meio_pagamento");
                        String endereco = rs.getString("endereco");
                        boolean retirarLoja = rs.getBoolean("retirar_loja");
                        String telefone = rs.getString("telefone");
                        
                        // Registrar venda e baixar estoque
                        registrarVenda(codPeca, cliente, valor, meioPagamento, pedidoId, endereco, retirarLoja, telefone);
                        atualizarEstoque(codPeca);
                    }
                    rs.close();
                    stmtSelect.close();
                }
                
                JOptionPane.showMessageDialog(null, 
                    "✅ " + (aprovado ? "Venda confirmada e registrada!" : "Venda rejeitada!"),
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao responder notificação: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "❌ Erro ao processar resposta: " + e.getMessage(),
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // REGISTRAR VENDA
    // ==========================================
    private static void registrarVenda(String codPeca, String cliente, double valor,
                                        String meioPagamento, String pedidoId,
                                        String endereco, boolean retirarLoja, String telefone) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            
            String sql = "INSERT INTO vendas (datavenda, codpeca, cliente, valor_total, meio_pagamento, " +
                         "pedido_id, endereco_entrega, retirar_loja, telefone, status_pagamento) " +
                         "VALUES (CURDATE(), ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMADO')";
            
            stmt = con.prepareStatement(sql);
            stmt.setString(1, codPeca);
            stmt.setString(2, cliente);
            stmt.setDouble(3, valor);
            stmt.setString(4, meioPagamento);
            stmt.setString(5, pedidoId);
            stmt.setString(6, endereco);
            stmt.setBoolean(7, retirarLoja);
            stmt.setString(8, telefone);
            stmt.executeUpdate();
            
            System.out.println("   ✅ Venda registrada: " + pedidoId);

        } catch (Exception e) {
            System.err.println("   ❌ Erro ao registrar venda: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // ATUALIZAR ESTOQUE
    // ==========================================
    private static void atualizarEstoque(String codPeca) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            
            String sql = "UPDATE estoque SET status = 'VENDIDO' WHERE codpeca = ? AND status = 'DISPONIVEL'";
            
            stmt = con.prepareStatement(sql);
            stmt.setString(1, codPeca);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("   ✅ Estoque atualizado: " + codPeca + " -> VENDIDO");
            }

        } catch (Exception e) {
            System.err.println("   ❌ Erro ao atualizar estoque: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // PARAR O SERVIÇO
    // ==========================================
    public static void parar() {
        executando = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        System.out.println("⏹️ Serviço de notificações parado.");
    }
}
