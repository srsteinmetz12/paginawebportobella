package paginaweb;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import connection.ConnectionDB;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificacaoVendasService {

    private static final Gson gson = new Gson();
    private static ScheduledExecutorService scheduler;
    private static boolean executando = true;
    
    // ==========================================
    // FILA DE NOTIFICAÇÕES (FIFO)
    // ==========================================
    private static final Queue<Notificacao> filaNotificacoes = new LinkedList<>();
    private static final Object lockFila = new Object();
    private static volatile boolean processando = false;
    private static volatile JDialog popupDialog = null;
    private static volatile Notificacao notificacaoAtual = null;
    private static final Set<Integer> notificacoesEmProcessamento = new HashSet<>();

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
        String itens;

        Notificacao(int id, String pedidoId, String cliente, String telefone, 
                    double valor, String codPeca, String meioPagamento, 
                    boolean retirarLoja, String endereco, String dataCriacao,
                    String itens) {
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
            this.itens = itens;
        }
    }

    public static void iniciar() {
        System.out.println("🔔 ========================================");
        System.out.println("🔔 SERVIÇO DE NOTIFICAÇÕES - PORTOBELLA");
        System.out.println("🔔 ========================================");
        System.out.println("🔄 Consultando notificações a cada 5 segundos...");
        System.out.println("📋 Modo FIFO - Processando em ordem de chegada");
        System.out.println("🔔 ========================================");

        synchronized (lockFila) {
            filaNotificacoes.clear();
            processando = false;
            notificacoesEmProcessamento.clear();
        }

        scheduler = Executors.newScheduledThreadPool(2);
        
        scheduler.scheduleAtFixedRate(() -> {
            if (executando) {
                try {
                    buscarNotificacoes();
                } catch (Exception e) {
                    System.err.println("❌ Erro ao buscar notificações: " + e.getMessage());
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
        
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
            stmt.setQueryTimeout(10);
            rs = stmt.executeQuery();

            synchronized (lockFila) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    boolean jaNaFila = filaNotificacoes.stream().anyMatch(n -> n.id == id);
                    boolean emProcessamento = notificacoesEmProcessamento.contains(id);
                    
                    if (!jaNaFila && !emProcessamento) {
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
                            rs.getString("data_criacao"),
                            rs.getString("itens")
                        );
                        filaNotificacoes.add(notif);
                        System.out.println("📥 [BUSCA] Adicionada: " + notif.pedidoId + " (ID: " + id + ")");
                    }
                }
            }

        } catch (Exception e) {
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
        synchronized (lockFila) {
            if (processando || filaNotificacoes.isEmpty()) {
                return;
            }
            
            notificacaoAtual = filaNotificacoes.poll();
            if (notificacaoAtual == null) {
                return;
            }
            
            processando = true;
            notificacoesEmProcessamento.add(notificacaoAtual.id);
            
            System.out.println("🔄 [PROCESSAR] Iniciando: " + notificacaoAtual.pedidoId + 
                              " (Restam: " + filaNotificacoes.size() + ")");
        }
        
        SwingUtilities.invokeLater(() -> {
            exibirPopup(notificacaoAtual);
        });
    }

    // ==========================================
    // EXIBIR POPUP
    // ==========================================
    private static void exibirPopup(Notificacao notif) {
        // Fecha popup anterior
        if (popupDialog != null && popupDialog.isVisible()) {
            popupDialog.dispose();
            popupDialog = null;
        }

        // SINAL SONORO
        Toolkit.getDefaultToolkit().beep();
        try { Thread.sleep(300); } catch (InterruptedException e) {}
        Toolkit.getDefaultToolkit().beep();

        // Cria o popup
        popupDialog = new JDialog();
        popupDialog.setTitle("🔔 NOVA VENDA - PORTOBELLA");
        popupDialog.setSize(580, 620);
        popupDialog.setLocationRelativeTo(null);
        popupDialog.setAlwaysOnTop(true);
        popupDialog.setResizable(false);
        popupDialog.setUndecorated(true);
        popupDialog.setModal(false);
        popupDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        popupDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                System.out.println("🚪 [POPUP] Fechado: " + notif.pedidoId);
                synchronized (lockFila) {
                    processando = false;
                    notificacoesEmProcessamento.remove(notif.id);
                    notificacaoAtual = null;
                }
                popupDialog = null;
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(212, 175, 55), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // BARRA DE TÍTULO
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(45, 45, 45));
        titleBar.setPreferredSize(new Dimension(0, 35));
        
        int restantes;
        synchronized (lockFila) {
            restantes = filaNotificacoes.size();
        }
        
        String titulo = "🛍️ NOVA VENDA ONLINE";
        if (restantes > 0) {
            titulo += " (mais " + restantes + " aguardando)";
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
            popupDialog.dispose();
            popupDialog = null;
            synchronized (lockFila) {
                processando = false;
                notificacoesEmProcessamento.remove(notif.id);
                notificacaoAtual = null;
            }
        });
        titleBar.add(btnClose, BorderLayout.EAST);
        
        // ARRASTAR
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
                popupDialog.setLocation(evt.getXOnScreen() - coordX[0], evt.getYOnScreen() - coordY[0]);
            }
        });

        panel.add(titleBar, BorderLayout.NORTH);

        // CORPO DO POPUP
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BorderLayout(10, 10));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        JPanel infoFilaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoFilaPanel.setBackground(Color.WHITE);
        infoFilaPanel.setOpaque(true);
        JLabel lblInfoFila = new JLabel("📋 " + (restantes + 1) + "ª notificação" + (restantes > 0 ? " (" + restantes + " aguardando)" : ""));
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
            System.out.println("✅ [POPUP] CONFIRMAR: " + notif.pedidoId);
            
            popupDialog.dispose();
            popupDialog = null;
            
            synchronized (lockFila) {
                processando = false;
                notificacoesEmProcessamento.remove(notif.id);
                notificacaoAtual = null;
            }
            
            new Thread(() -> {
                try {
                    responderNotificacao(notif, true);
                } catch (Exception ex) {
                    System.err.println("❌ Erro: " + ex.getMessage());
                }
            }).start();
        });

        JButton btnRejeitar = new JButton("❌ REJEITAR");
        btnRejeitar.setBackground(new Color(200, 50, 50));
        btnRejeitar.setForeground(Color.WHITE);
        btnRejeitar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRejeitar.setPreferredSize(new Dimension(150, 50));
        btnRejeitar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRejeitar.addActionListener(e -> {
            System.out.println("❌ [POPUP] REJEITAR: " + notif.pedidoId);
            
            popupDialog.dispose();
            popupDialog = null;
            
            synchronized (lockFila) {
                processando = false;
                notificacoesEmProcessamento.remove(notif.id);
                notificacaoAtual = null;
            }
            
            new Thread(() -> {
                try {
                    responderNotificacao(notif, false);
                } catch (Exception ex) {
                    System.err.println("❌ Erro: " + ex.getMessage());
                }
            }).start();
        });

        btnPanel.add(btnConfirmar);
        btnPanel.add(btnRejeitar);
        panel.add(btnPanel, BorderLayout.SOUTH);

        popupDialog.add(panel);
        popupDialog.setVisible(true);
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

    // ==========================================
    // RESPONDER NOTIFICAÇÃO (COM HISTÓRICO)
    // ==========================================
    private static void responderNotificacao(Notificacao notif, boolean aprovado) {
        System.out.println("📤 [RESPONDER] Iniciando: " + notif.pedidoId + " (aprovado=" + aprovado + ")");
        
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            
            if (con == null || con.isClosed()) {
                Thread.sleep(2000);
                con = ConnectionDB.getConnectionCloud();
                if (con == null) {
                    throw new SQLException("Não foi possível reconectar ao banco");
                }
            }
            
            String status = aprovado ? "CONFIRMADO" : "REJEITADO";
            
            // ==========================================
            // 1. ATUALIZAR NOTIFICACOES_PENDENTES
            // ==========================================
            String sql = "UPDATE notificacoes_pendentes SET status = ?, lida = 1, data_confirmacao = NOW() WHERE id = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, notif.id);
            stmt.setQueryTimeout(10);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ [RESPONDER] Notificação #" + notif.id + " -> " + status);
                
                if (aprovado) {
                    // ==========================================
                    // 2. MOVER PARA HISTÓRICO (CONFIRMADO)
                    // ==========================================
                    moverParaHistorico(con, notif);
                    
                    // ==========================================
                    // 3. REGISTRAR VENDA - RETORNA O ID
                    // ==========================================
                    System.out.println("📦 [RESPONDER] Registrando venda e baixando estoque...");
                    int idVenda = registrarVenda(con, notif);
                    
                    // ==========================================
                    // 4. REGISTRAR SACOLA E ENTREGA COM O ID_VENDA
                    // ==========================================
                    if (idVenda > 0) {
                        registrarSacola(con, notif);
                        registrarEntrega(con, notif);
                    }
                    
                    baixarEstoque(con, notif.itens);
                    
                    mostrarMensagemTray("✅ Venda CONFIRMADA!", "Pedido: " + notif.pedidoId, TrayIcon.MessageType.INFO);
                } else {
                    // ==========================================
                    // REJEITADO - REGISTRA NO HISTÓRICO
                    // ==========================================
                    moverParaHistoricoRejeitado(con, notif);
                    mostrarMensagemTray("❌ Venda REJEITADA!", "Pedido: " + notif.pedidoId, TrayIcon.MessageType.WARNING);
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ [RESPONDER] Erro: " + e.getMessage());
            e.printStackTrace();
            mostrarMensagemTray("❌ Erro!", e.getMessage(), TrayIcon.MessageType.ERROR);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
        
        System.out.println("📤 [RESPONDER] Finalizado: " + notif.pedidoId);
    }

    // ==========================================
    // 2A. MOVER PARA HISTÓRICO (CONFIRMADO)
    // ==========================================
    private static void moverParaHistorico(Connection con, Notificacao notif) {
        PreparedStatement stmt = null;
        
        try {
            String sql = "INSERT INTO notificacoes_historico " +
                         "(notificacao_id, pedido_id, cod_peca, cliente, telefone, valor, " +
                         "meio_pagamento, endereco, retirar_loja, itens, status, data_criacao, data_confirmacao) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, notif.id);
            stmt.setString(2, notif.pedidoId);
            stmt.setString(3, notif.codPeca);
            stmt.setString(4, notif.cliente);
            stmt.setString(5, notif.telefone);
            stmt.setDouble(6, notif.valor);
            stmt.setString(7, notif.meioPagamento);
            stmt.setString(8, notif.endereco);
            stmt.setBoolean(9, notif.retirarLoja);
            stmt.setString(10, notif.itens);
            stmt.setString(11, "CONFIRMADO");
            stmt.setTimestamp(12, getDataCriacao(notif.pedidoId));
            stmt.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
            
            stmt.setQueryTimeout(10);
            stmt.executeUpdate();
            
            System.out.println("   ✅ Movido para histórico (CONFIRMADO): " + notif.pedidoId);
            
        } catch (Exception e) {
            System.err.println("   ❌ Erro ao mover para histórico: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // 2B. MOVER PARA HISTÓRICO (REJEITADO)
    // ==========================================
    private static void moverParaHistoricoRejeitado(Connection con, Notificacao notif) {
        PreparedStatement stmt = null;
        
        try {
            String sql = "INSERT INTO notificacoes_historico " +
                         "(notificacao_id, pedido_id, cod_peca, cliente, telefone, valor, " +
                         "meio_pagamento, endereco, retirar_loja, itens, status, data_criacao, data_confirmacao) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, notif.id);
            stmt.setString(2, notif.pedidoId);
            stmt.setString(3, notif.codPeca);
            stmt.setString(4, notif.cliente);
            stmt.setString(5, notif.telefone);
            stmt.setDouble(6, notif.valor);
            stmt.setString(7, notif.meioPagamento);
            stmt.setString(8, notif.endereco);
            stmt.setBoolean(9, notif.retirarLoja);
            stmt.setString(10, notif.itens);
            stmt.setString(11, "REJEITADO");
            stmt.setTimestamp(12, getDataCriacao(notif.pedidoId));
            stmt.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
            
            stmt.setQueryTimeout(10);
            stmt.executeUpdate();
            
            System.out.println("   ✅ Movido para histórico (REJEITADO): " + notif.pedidoId);
            
        } catch (Exception e) {
            System.err.println("   ❌ Erro ao mover para histórico (rejeitado): " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // BUSCAR DATA_CRIACAO DA NOTIFICACAO
    // ==========================================
    private static Timestamp getDataCriacao(String pedidoId) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            con = ConnectionDB.getConnectionCloud();
            String sql = "SELECT data_criacao FROM notificacoes_pendentes WHERE pedido_id = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, pedidoId);
            stmt.setQueryTimeout(10);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getTimestamp("data_criacao");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao buscar data_criacao: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
        
        return new Timestamp(System.currentTimeMillis());
    }

    // ==========================================
    // 3. REGISTRAR VENDA (RETORNA O ID_GERADO)
    // ==========================================
    private static int registrarVenda(Connection con, Notificacao notif) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int idVenda = 0;
        
        try {
            // ==========================================
            // ESTRUTURA DA TABELA VENDAS:
            // datavenda, origemvenda, tipopag, valorvenda, 
            // codpecas, nomecli, obsvendas, entrega, status, pedido_id
            // ==========================================
            String sql = "INSERT INTO vendas (datavenda, origemvenda, tipopag, valorvenda, codpecas, nomecli, obsvendas, entrega, status, pedido_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            stmt = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.setString(2, "SITE");
            stmt.setString(3, notif.meioPagamento);
            stmt.setDouble(4, notif.valor);
            stmt.setString(5, notif.codPeca);
            stmt.setString(6, notif.cliente);
            stmt.setString(7, "Pedido: " + notif.pedidoId + " | " + notif.endereco);
            stmt.setString(8, notif.retirarLoja ? "RETIRADA NA LOJA" : "ENTREGA");
            stmt.setString(9, "EM_SEPARACAO");
            stmt.setString(10, notif.pedidoId);
            stmt.setQueryTimeout(10);
            stmt.executeUpdate();
            
            // ==========================================
            // 🔥 RECUPERA O ID GERADO
            // ==========================================
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                idVenda = rs.getInt(1);
            }
            
            System.out.println("   ✅ Venda registrada (ID: " + idVenda + ", Pedido: " + notif.pedidoId + ")");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erro ao registrar venda: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
        
        return idVenda;
    }

    // ==========================================
    // 4. REGISTRAR SACOLA
    // ==========================================
    private static void registrarSacola(Connection con, Notificacao notif) {
        PreparedStatement stmt = null;
        
        try {
            // ==========================================
            // ESTRUTURA DA TABELA SACOLA:
            // pedido_id, datavenda, valorvenda, status, 
            // codpecas, nomecli, tipoentrega
            // ==========================================
            String sql = "INSERT INTO sacola (pedido_id, datavenda, valorvenda, status, codpecas, nomecli, tipoentrega) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            stmt = con.prepareStatement(sql);
            stmt.setString(1, notif.pedidoId);
            stmt.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.setDouble(3, notif.valor);
            stmt.setString(4, "EM_SEPARACAO");
            stmt.setString(5, notif.codPeca);
            stmt.setString(6, notif.cliente);
            stmt.setString(7, notif.retirarLoja ? "RETIRE_LOJA" : "ENTREGA");
            stmt.setQueryTimeout(10);
            stmt.executeUpdate();
            
            System.out.println("   ✅ Sacola registrada (Pedido: " + notif.pedidoId + ")");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erro ao registrar sacola: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // 5. REGISTRAR ENTREGA
    // ==========================================
    private static void registrarEntrega(Connection con, Notificacao notif) {
        PreparedStatement stmt = null;
        
        try {
            // ==========================================
            // ESTRUTURA DA TABELA ENTREGAS:
            // pedido_id, datavenda, nomecli, codpeca, valorfrete, 
            // fretepago, entregue, status, tipoentrega, canal
            // ==========================================
            String tipoEntrega = notif.retirarLoja ? "RETIRE_LOJA" : "ENTREGA";
            
            String sql = "INSERT INTO entregas (pedido_id, datavenda, nomecli, codpeca, valorfrete, fretepago, entregue, status, tipoentrega, canal) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            stmt = con.prepareStatement(sql);
            stmt.setString(1, notif.pedidoId);
            stmt.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.setString(3, notif.cliente);
            stmt.setString(4, notif.codPeca);
            stmt.setDouble(5, 0.0);
            stmt.setBoolean(6, false);
            stmt.setBoolean(7, false);
            stmt.setString(8, "EM_SEPARACAO");
            stmt.setString(9, tipoEntrega);
            stmt.setString(10, "SITE");
            stmt.setQueryTimeout(10);
            stmt.executeUpdate();
            
            System.out.println("   ✅ Entrega registrada (Pedido: " + notif.pedidoId + ", " + tipoEntrega + ")");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erro ao registrar entrega: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // 6. BAIXAR ESTOQUE (COM DATAVENDA)
    // ==========================================
    private static void baixarEstoque(Connection con, String itensJson) {
        if (itensJson == null || itensJson.isEmpty()) {
            return;
        }

        PreparedStatement stmt = null;
        
        try {
            JsonArray itensArray = gson.fromJson(itensJson, JsonArray.class);
            
            if (itensArray == null || itensArray.size() == 0) {
                return;
            }
            
            System.out.println("📦 [ESTOQUE] Baixando " + itensArray.size() + " item(ns)...");
            
            for (int i = 0; i < itensArray.size(); i++) {
                JsonObject item = itensArray.get(i).getAsJsonObject();
                String codPeca = item.get("id").getAsString();
                int quantidade = item.get("quantidade").getAsInt();
                
                String sql = "UPDATE estoque SET status = 'VENDIDO', datavenda = CURDATE() WHERE codpeca = ? AND status = 'DISPONIVEL' LIMIT ?";
                
                stmt = con.prepareStatement(sql);
                stmt.setString(1, codPeca);
                stmt.setInt(2, quantidade);
                stmt.setQueryTimeout(10);
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("   ✅ Estoque baixado: " + codPeca + " (" + rows + " unidade(s))");
                } else {
                    System.err.println("   ⚠️ Produto não encontrado: " + codPeca);
                }
                
                stmt.close();
            }

        } catch (Exception e) {
            System.err.println("   ❌ Erro ao baixar estoque: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    // MOSTRAR MENSAGEM NA BANDEJA
    // ==========================================
    private static void mostrarMensagemTray(String titulo, String mensagem, TrayIcon.MessageType tipo) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (SystemTray.isSupported()) {
                    SystemTray tray = SystemTray.getSystemTray();
                    Image image = Toolkit.getDefaultToolkit().createImage("");
                    TrayIcon trayIcon = new TrayIcon(image, "PORTOBELLA");
                    trayIcon.setImageAutoSize(true);
                    
                    tray.add(trayIcon);
                    trayIcon.displayMessage(titulo, mensagem, tipo);
                    
                    new Timer(3000, e -> {
                        tray.remove(trayIcon);
                    }).start();
                } else {
                    JOptionPane.showMessageDialog(null, mensagem, titulo, 
                        tipo == TrayIcon.MessageType.INFO ? JOptionPane.INFORMATION_MESSAGE :
                        tipo == TrayIcon.MessageType.WARNING ? JOptionPane.WARNING_MESSAGE :
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, mensagem, titulo, JOptionPane.INFORMATION_MESSAGE);
                System.err.println("⚠️ Erro ao mostrar mensagem na bandeja: " + e.getMessage());
            }
        });
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
        if (popupDialog != null) {
            popupDialog.dispose();
            popupDialog = null;
        }
        synchronized (lockFila) {
            filaNotificacoes.clear();
            processando = false;
            notificacoesEmProcessamento.clear();
            notificacaoAtual = null;
        }
        System.out.println("⏹️ Serviço de notificações parado.");
    }
}