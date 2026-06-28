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
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificacaoVendasService {

    private static final Gson gson = new Gson();
    private static ScheduledExecutorService scheduler;
    private static boolean executando = true;
    private static JFrame popupFrame;
    
    // ==========================================
    // 🔥 FILA DE NOTIFICAÇÕES (FIFO)
    // ==========================================
    private static final Queue<Notificacao> filaNotificacoes = new LinkedList<>();
    private static final AtomicBoolean processando = new AtomicBoolean(false);
    private static final AtomicBoolean popupAberto = new AtomicBoolean(false);
    private static final AtomicInteger totalNotificacoes = new AtomicInteger(0);
    private static final AtomicInteger notificacaoAtual = new AtomicInteger(0);

    // ==========================================
    // CLASSE PARA ARMAZENAR DADOS DA NOTIFICAÇÃO
    // ==========================================
    private static class Notificacao {
        int id;
        String pedidoId;
        String cliente;
        String telefone;
        double valor;
        String codPeca;
        String meioPagamento;
        boolean retirarLoja;
        String endereco;
        String dataCriacao;

        Notificacao(int id, String pedidoId, String cliente, String telefone, 
                    double valor, String codPeca, String meioPagamento, 
                    boolean retirarLoja, String endereco, String dataCriacao) {
            this.id = id;
            this.pedidoId = pedidoId;
            this.cliente = cliente;
            this.telefone = telefone;
            this.valor = valor;
            this.codPeca = codPeca;
            this.meioPagamento = meioPagamento;
            this.retirarLoja = retirarLoja;
            this.endereco = endereco;
            this.dataCriacao = dataCriacao;
        }
    }

    public static void iniciar() {
        System.out.println("🔔 ========================================");
        System.out.println("🔔 SERVIÇO DE NOTIFICAÇÕES - PORTOBELLA");
        System.out.println("🔔 ========================================");
        System.out.println("🔄 Consultando notificações a cada 5 segundos...");
        System.out.println("📋 Modo FIFO - Processando em ordem de chegada");
        System.out.println("🔔 ========================================");

        // Resetar estados
        synchronized (filaNotificacoes) {
            filaNotificacoes.clear();
        }
        processando.set(false);
        popupAberto.set(false);
        totalNotificacoes.set(0);
        notificacaoAtual.set(0);

        scheduler = Executors.newScheduledThreadPool(2);
        
        // ==========================================
        // TAREFA 1: BUSCAR NOTIFICAÇÕES NO BANCO
        // ==========================================
        scheduler.scheduleAtFixedRate(() -> {
            if (executando) {
                try {
                    buscarNotificacoes();
                } catch (Exception e) {
                    System.err.println("❌ Erro ao buscar notificações: " + e.getMessage());
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
        
        // ==========================================
        // TAREFA 2: PROCESSAR FILA
        // ==========================================
        scheduler.scheduleAtFixedRate(() -> {
            if (executando) {
                try {
                    processarFila();
                } catch (Exception e) {
                    System.err.println("❌ Erro ao processar fila: " + e.getMessage());
                }
            }
        }, 1, 2, TimeUnit.SECONDS);
    }

    // ==========================================
    // BUSCAR NOTIFICAÇÕES NO BANCO
    // ==========================================
    private static void buscarNotificacoes() {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            
            String sql = "SELECT id, pedido_id, cod_peca, cliente, telefone, valor, " +
                         "meio_pagamento, endereco, retirar_loja, itens, data_criacao " +
                         "FROM notificacoes_pendentes " +
                         "WHERE status = 'PENDENTE' AND lida = 0 " +
                         "ORDER BY data_criacao ASC";
            
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();

            int adicionadas = 0;
            synchronized (filaNotificacoes) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    
                    boolean jaNaFila = filaNotificacoes.stream().anyMatch(n -> n.id == id);
                    
                    if (!jaNaFila) {
                        Notificacao notif = new Notificacao(
                            id,
                            rs.getString("pedido_id"),
                            rs.getString("cliente"),
                            rs.getString("telefone"),
                            rs.getDouble("valor"),
                            rs.getString("cod_peca"),
                            rs.getString("meio_pagamento"),
                            rs.getBoolean("retirar_loja"),
                            rs.getString("endereco"),
                            rs.getString("data_criacao")
                        );
                        filaNotificacoes.add(notif);
                        adicionadas++;
                        System.out.println("📥 Notificação adicionada à fila: " + notif.pedidoId + " (ID: " + id + ")");
                    }
                }
                
                if (adicionadas > 0) {
                    totalNotificacoes.set(filaNotificacoes.size());
                    System.out.println("📋 Total na fila: " + filaNotificacoes.size() + " notificação(ões)");
                }
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
    // PROCESSAR FILA (FIFO)
    // ==========================================
    private static void processarFila() {
        // Verifica se pode processar
        if (popupAberto.get()) {
            return;
        }
        
        if (processando.get()) {
            return;
        }
        
        Notificacao notif = null;
        synchronized (filaNotificacoes) {
            if (filaNotificacoes.isEmpty()) {
                return;
            }
            notif = filaNotificacoes.poll();
        }
        
        if (notif == null) {
            return;
        }
        
        // Marca como processando
        processando.set(true);
        popupAberto.set(true);
        
        System.out.println("🔄 Processando notificação: " + notif.pedidoId + " (Restam: " + filaNotificacoes.size() + ")");
        
        // Exibe o popup
        exibirPopup(notif);
    }

    // ==========================================
    // EXIBIR POPUP
    // ==========================================
    private static void exibirPopup(Notificacao notif) {
        SwingUtilities.invokeLater(() -> {
            // Fecha popup anterior se existir
            if (popupFrame != null && popupFrame.isVisible()) {
                popupFrame.dispose();
                popupFrame = null;
            }

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
            popupFrame.setUndecorated(true);

            // PAINEL PRINCIPAL
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 175, 55), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));

            // BARRA DE TÍTULO (PARA ARRASTAR)
            JPanel titleBar = new JPanel(new BorderLayout());
            titleBar.setBackground(new Color(45, 45, 45));
            titleBar.setPreferredSize(new Dimension(0, 35));
            
            // ==========================================
            // 🔥 CALCULA O CONTADOR CORRETAMENTE
            // ==========================================
            int restantes;
            synchronized (filaNotificacoes) {
                restantes = filaNotificacoes.size();
            }
            int atual = notificacaoAtual.incrementAndGet();
            int total = totalNotificacoes.get();
            
            String titulo = "🛍️ NOVA VENDA ONLINE";
            String infoFila = null;
            
            if (total > 0) {
                infoFila = "📋 " + atual + "ª de " + total;
                if (restantes > 0) {
                    infoFila += " (mais " + restantes + " aguardando)";
                }
                titulo += " (" + infoFila + ")";
            }
            
            JLabel lblTitle = new JLabel(titulo);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTitle.setForeground(new Color(212, 175, 55));
            lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
            titleBar.add(lblTitle, BorderLayout.WEST);
            
            JButton btnClose = new JButton("✕");
            btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnClose.setForeground(Color.WHITE);
            btnClose.setBackground(new Color(45, 45, 45));
            btnClose.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
            btnClose.setFocusPainted(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btnClose.setBackground(new Color(160, 40, 40));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btnClose.setBackground(new Color(45, 45, 45));
                }
            });
            btnClose.addActionListener(e -> {
                popupFrame.dispose();
                popupFrame = null;
                popupAberto.set(false);
                processando.set(false);
                System.out.println("🚪 Popup fechado pelo usuário (X)");
            });
            titleBar.add(btnClose, BorderLayout.EAST);
            
            // ADICIONA ARRASTAR
            final int[] coordX = {0};
            final int[] coordY = {0};
            
            titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    coordX[0] = evt.getX();
                    coordY[0] = evt.getY();
                }
            });
            
            titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseDragged(java.awt.event.MouseEvent evt) {
                    popupFrame.setLocation(evt.getXOnScreen() - coordX[0], evt.getYOnScreen() - coordY[0]);
                }
            });

            panel.add(titleBar, BorderLayout.NORTH);

            // CORPO DO POPUP
            JPanel bodyPanel = new JPanel();
            bodyPanel.setLayout(new BorderLayout(10, 10));
            bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

            // INFORMA POSIÇÃO NA FILA
            JPanel infoFilaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            infoFilaPanel.setBackground(Color.WHITE);
            infoFilaPanel.setOpaque(true);
            
            JLabel lblInfoFila = new JLabel(infoFila);
            lblInfoFila.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblInfoFila.setForeground(new Color(100, 100, 100));
            infoFilaPanel.add(lblInfoFila);
            bodyPanel.add(infoFilaPanel, BorderLayout.NORTH);

            // DADOS
            JPanel dadosPanel = new JPanel();
            dadosPanel.setLayout(new GridLayout(0, 2, 10, 10));
            dadosPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

            Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
            Font valueFont = new Font("Segoe UI", Font.PLAIN, 13);

            addCampo(dadosPanel, "📋 Pedido:", notif.pedidoId, labelFont, valueFont);
            addCampo(dadosPanel, "👤 Cliente:", notif.cliente, labelFont, valueFont);
            addCampo(dadosPanel, "📱 Telefone:", notif.telefone, labelFont, valueFont);
            addCampo(dadosPanel, "💰 Valor:", "R$ " + String.format("%.2f", notif.valor).replace(".", ","), labelFont, valueFont);
            addCampo(dadosPanel, "🏷️ Código:", notif.codPeca, labelFont, valueFont);
            addCampo(dadosPanel, "💳 Pagamento:", notif.meioPagamento.toUpperCase(), labelFont, valueFont);
            
            String tipoEntrega = notif.retirarLoja ? "📍 RETIRADA NA LOJA" : "🚚 ENTREGA";
            Color corEntrega = notif.retirarLoja ? new Color(0, 166, 80) : new Color(0, 158, 227);
            addCampoColorido(dadosPanel, "📦 Entrega:", tipoEntrega, labelFont, valueFont, corEntrega);
            
            String enderecoExibicao = notif.endereco.length() > 50 ? notif.endereco.substring(0, 50) + "..." : notif.endereco;
            addCampo(dadosPanel, "📍 Endereço:", enderecoExibicao, labelFont, valueFont);

            bodyPanel.add(dadosPanel, BorderLayout.CENTER);

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
            bodyPanel.add(instrucoesPanel, BorderLayout.NORTH);

            panel.add(bodyPanel, BorderLayout.CENTER);

            // BOTÕES
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

            JButton btnConfirmar = new JButton("✅ CONFIRMAR PAGAMENTO");
            btnConfirmar.setBackground(new Color(0, 166, 80));
            btnConfirmar.setForeground(Color.WHITE);
            btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btnConfirmar.setPreferredSize(new Dimension(250, 50));
            btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnConfirmar.addActionListener(e -> {
                // Fecha popup
                popupFrame.dispose();
                popupFrame = null;
                
                System.out.println("✅ Cliente confirmou pagamento: " + notif.pedidoId);
                
                // Processa em thread separada
                new Thread(() -> {
                    responderNotificacao(notif.id, notif.pedidoId, true);
                    // Libera a fila
                    popupAberto.set(false);
                    processando.set(false);
                    System.out.println("🔄 Fila liberada para próxima notificação");
                }).start();
            });

            JButton btnRejeitar = new JButton("❌ REJEITAR");
            btnRejeitar.setBackground(new Color(200, 50, 50));
            btnRejeitar.setForeground(Color.WHITE);
            btnRejeitar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnRejeitar.setPreferredSize(new Dimension(150, 50));
            btnRejeitar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRejeitar.addActionListener(e -> {
                // Fecha popup
                popupFrame.dispose();
                popupFrame = null;
                
                System.out.println("❌ Cliente rejeitou pagamento: " + notif.pedidoId);
                
                // Processa em thread separada
                new Thread(() -> {
                    responderNotificacao(notif.id, notif.pedidoId, false);
                    // Libera a fila
                    popupAberto.set(false);
                    processando.set(false);
                    System.out.println("🔄 Fila liberada para próxima notificação");
                }).start();
            });

            btnPanel.add(btnConfirmar);
            btnPanel.add(btnRejeitar);
            panel.add(btnPanel, BorderLayout.SOUTH);

            popupFrame.add(panel);
            popupFrame.setVisible(true);
        });
    }

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
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(cor);
        panel.add(val);
    }

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
                        
                        registrarVenda(codPeca, cliente, valor, meioPagamento, pedidoId, endereco, retirarLoja, telefone);
                        atualizarEstoque(codPeca);
                    }
                    rs.close();
                    stmtSelect.close();
                }
                
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "✅ " + (aprovado ? "Venda confirmada e registrada!" : "Venda rejeitada!"),
                        "Sucesso", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao responder notificação: " + e.getMessage());
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                    "❌ Erro ao processar resposta: " + e.getMessage(),
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            });
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }

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
        if (popupFrame != null) {
            popupFrame.dispose();
            popupFrame = null;
        }
        synchronized (filaNotificacoes) {
            filaNotificacoes.clear();
        }
        processando.set(false);
        popupAberto.set(false);
        System.out.println("⏹️ Serviço de notificações parado.");
    }
}