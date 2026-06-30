package dao;

import connection.ConnectionDB;
import static dao.VendasDAO.dbRows;
import static dao.VendasDAO.tabela;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import models.Entregas;
import models.Etiqueta;
import util.ConfigLoader;
import util.MensagemSistema;
import views.TelaEntregas;
import views.TelaFornecedor;

public class EntregasDAO {
    public String resultado;
    private String sql;
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    JTable table = new JTable();
    DefaultTableModel model = new DefaultTableModel();
    ResultSet rs;
    Connection con;
    Connection con2;
    ResultSetMetaData metaData;
    JFrame frame;
    String favicon = ConfigLoader.get("sistema.favicon");
    
    public void pesquisaClientePendentesRetireLojaTabelaEntregas(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con = ConnectionDB.getConnection();

        // 🎨 Paleta de Cores Idêntica ao Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro = java.awt.Color.WHITE;
        java.awt.Color cinzaClaroHeader = new java.awt.Color(220, 220, 220); 
        java.awt.Color cinzaEscuroTexto = new java.awt.Color(40, 40, 40); 

        // Configuração Única do Frame Escuro Customizado (Sem barra nativa)
        frame = new JFrame();
        frame.setUndecorated(true); 
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 500);     
        frame.getContentPane().setBackground(grafiteProfundo);
        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); 

        // ─── BARRA DE TÍTULO CUSTOMIZADA ───
        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
        barraTitulo.setBackground(pretoCabecalho);
        barraTitulo.setPreferredSize(new java.awt.Dimension(1000, 35));
        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        // Painel Esquerdo: Logo + Título
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);

        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
        try {
            java.net.URL urlLogo = getClass().getResource(favicon); 
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível carregar o logo na barra: " + ex.getMessage());
        }
        painelEsquerdo.add(lblLogo);

        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Entregas Retire Loja Pendentes - Local");
        lblTituloText.setForeground(brancoPuro);
        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        painelEsquerdo.add(lblTituloText);
        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

        // Painel Direito: Assinatura + Botão Fechar (X)
        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
        painelDireito.setOpaque(false);

        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); 
        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11)); 
        painelDireito.add(lblDesenvolvedor);

        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnFechar.setForeground(brancoPuro);
        btnFechar.setBackground(pretoCabecalho);
        btnFechar.setFocusPainted(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnFechar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnFechar.setBackground(java.awt.Color.RED); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) { btnFechar.setBackground(pretoCabecalho); }
        });
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) { frame.dispose(); }
        });
        painelDireito.add(btnFechar);
        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);

        // Código para Arrastar a Janela
        final java.awt.Point pontoArrastar = new java.awt.Point();
        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pontoArrastar.x = e.getX(); pontoArrastar.y = e.getY();
            }
        });
        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                java.awt.Point p = frame.getLocation();
                frame.setLocation(p.x + e.getX() - pontoArrastar.x, p.y + e.getY() - pontoArrastar.y);
            }
        });

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

        // ─── CUSTOMIZAÇÃO DA TABELA ───
        table = new JTable();
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table.setModel(model);             

        table.setBackground(grafiteClaro);
        table.setForeground(brancoPuro);
        table.setGridColor(cinzaLinhas);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(cinzaLinhas);
        table.setSelectionForeground(brancoPuro);

        // ─── CABEÇALHO ───
        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
        cabecalho.setOpaque(true);
        cabecalho.setBackground(cinzaClaroHeader); 
        cabecalho.setForeground(cinzaEscuroTexto); 
        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        sql = "SELECT idvenda, datavenda, nomecli, codpeca, entregue, dataentrega, status, tipoentrega, canal FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='RETIRE_LOJA' AND canal='WEB' ORDER BY idvenda ASC";
        System.out.println(sql);       

        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            rs = stmt.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           

            for (int i = 1; i <= columnCount; i++) {
                String nomeOriginal = metaData.getColumnName(i).toLowerCase();
                String nomeAmigavel = nomeOriginal; 

                switch (nomeOriginal) {
                    case "idvenda":     nomeAmigavel = "ID VENDA"; break;
                    case "datavenda":   nomeAmigavel = "DATA VENDA"; break;
                    case "nomecli":     nomeAmigavel = "CLIENTE"; break;
                    case "codpeca":     nomeAmigavel = "PEÇA"; break;
                    case "entregue":    nomeAmigavel = "ENTREGUE?"; break;
                    case "dataentrega": nomeAmigavel = "DATA ENTREGA"; break;
                    case "status":      nomeAmigavel = "SITUAÇÃO"; break;
                    case "tipoentrega": nomeAmigavel = "TIPO ENVIO"; break;
                    case "canal":       nomeAmigavel = "CANAL"; break;
                }
                model.addColumn(nomeAmigavel);
            }

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(table);
            scrollPane.setBackground(grafiteProfundo);
            scrollPane.getViewport().setBackground(grafiteProfundo);
            scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            frame.add(scrollPane, java.awt.BorderLayout.CENTER);
            
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("Acessou o banco Local com sucesso!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con != null) con.close();
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
                System.out.println("Fim da Pesquisa!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexão local: " + ex.getMessage());
            }
        }
    }
    
    public void pesquisaClientePendentesRetireLojaTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con2 = ConnectionDB.getConnectionCloud();

        // 🎨 Paleta de Cores Idêntica ao Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro = java.awt.Color.WHITE;

        // 🕶️ Tom cinza claro premium de fundo com letras bem escuras para contraste suave
        java.awt.Color cinzaClaroHeader = new java.awt.Color(220, 220, 220); 
        java.awt.Color cinzaEscuroTexto = new java.awt.Color(40, 40, 40); 

        // Configuração Única do Frame Escuro Customizado (Sem barra nativa)
        frame = new JFrame();
        frame.setUndecorated(true); 
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 500); // Padronizado para o tamanho maior ideal da tabela     
        frame.getContentPane().setBackground(grafiteProfundo);
        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); 

        // ─── BARRA DE TÍTULO CUSTOMIZADA ───
        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
        barraTitulo.setBackground(pretoCabecalho);
        barraTitulo.setPreferredSize(new java.awt.Dimension(1000, 35));
        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        // Painel Esquerdo: Logo + Título
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);

        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
        try {
            java.net.URL urlLogo = getClass().getResource(favicon); 
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível carregar o logo na barra: " + ex.getMessage());
        }
        painelEsquerdo.add(lblLogo);

        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Entregas Retire Loja Pendentes");
        lblTituloText.setForeground(brancoPuro);
        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        painelEsquerdo.add(lblTituloText);
        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

        // Painel Direito: Assinatura + Botão Fechar (X)
        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
        painelDireito.setOpaque(false);

        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); 
        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11)); 
        painelDireito.add(lblDesenvolvedor);
        painelDireito.setOpaque(false);

        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); 
        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11)); 
        painelDireito.add(lblDesenvolvedor);

        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnFechar.setForeground(brancoPuro);
        btnFechar.setBackground(pretoCabecalho);
        btnFechar.setFocusPainted(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnFechar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnFechar.setBackground(java.awt.Color.RED); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) { btnFechar.setBackground(pretoCabecalho); }
        });
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) { frame.dispose(); }
        });
        painelDireito.add(btnFechar);
        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);

        // Código para Arrastar a Janela
        final java.awt.Point pontoArrastar = new java.awt.Point();
        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pontoArrastar.x = e.getX(); pontoArrastar.y = e.getY();
            }
        });
        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                java.awt.Point p = frame.getLocation();
                frame.setLocation(p.x + e.getX() - pontoArrastar.x, p.y + e.getY() - pontoArrastar.y);
            }
        });

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

        // ─── CUSTOMIZAÇÃO DA TABELA ───
        table = new JTable();
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table.setModel(model);             

        table.setBackground(grafiteClaro);
        table.setForeground(brancoPuro);
        table.setGridColor(cinzaLinhas);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(cinzaLinhas);
        table.setSelectionForeground(brancoPuro);

        // ─── CABEÇALHO EM TOM CINZA CLARO COM LETRAS ESCURAS ───
        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
        cabecalho.setOpaque(true);
        cabecalho.setBackground(cinzaClaroHeader); 
        cabecalho.setForeground(cinzaEscuroTexto); 
        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        sql = "SELECT idvenda, datavenda, nomecli, codpeca, entregue, dataentrega, status, tipoentrega, canal FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='RETIRE_LOJA' AND canal='WEB' ORDER BY idvenda ASC";
        System.out.println(sql);       

        try (PreparedStatement localStmt = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt;
            rs = stmt2.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           

            for (int i = 1; i <= columnCount; i++) {
                String nomeOriginal = metaData.getColumnName(i).toLowerCase();
                String nomeAmigavel = nomeOriginal; // Nome padrão caso não entre no filtro

                // Mapeie aqui o nome do Banco -> Nome para o Operador
                switch (nomeOriginal) {
                    case "idvenda":     nomeAmigavel = "ID VENDA"; break;
                    case "datavenda":   nomeAmigavel = "DATA VENDA"; break;
                    case "nomecli":     nomeAmigavel = "CLIENTE"; break;
                    case "codpeca":     nomeAmigavel = "PEÇA"; break;
                    case "entregue":    nomeAmigavel = "ENTREGUE?"; break;
                    case "dataentrega": nomeAmigavel = "DATA ENTREGA"; break;
                    case "status":      nomeAmigavel = "SITUAÇÃO"; break;
                    case "tipoentrega": nomeAmigavel = "TIPO ENVIO"; break;
                    case "canal":       nomeAmigavel = "CANAL"; break;
                }
                model.addColumn(nomeAmigavel); // Adiciona o nome bonito na tabela
            }

            boolean possuiDados = false;
            while (rs.next()) {
                possuiDados = true;
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }             
                model.addRow(row);
            }

            if (possuiDados) {
                table.setModel(model);
                // Customiza o painel de scroll para sumir com o fundo branco restante
                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setBackground(grafiteProfundo);
                scrollPane.getViewport().setBackground(grafiteProfundo);
                scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
                frame.add(scrollPane, java.awt.BorderLayout.CENTER);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: " + table.getRowCount());               
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
        } catch(SQLException ex) {
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                con2.close();
                System.out.println("Conexão Cloud encerrada!");
            } catch (SQLException ex) {
                System.out.println("Erro ao fechar conexão: " + ex.getMessage());
            }
        }
    }
    
    public void pesquisaClientePendentesEntregaEnderecoTabelaEntregas(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con = ConnectionDB.getConnection();

        // 🎨 Paleta de Cores Idêntica ao Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro = java.awt.Color.WHITE;
        java.awt.Color cinzaClaroHeader = new java.awt.Color(220, 220, 220); 
        java.awt.Color cinzaEscuroTexto = new java.awt.Color(40, 40, 40); 

        // Configuração Única do Frame Escuro Customizado (Sem barra nativa)
        frame = new JFrame();
        frame.setUndecorated(true); 
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 500);     
        frame.getContentPane().setBackground(grafiteProfundo);
        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); 

        // ─── BARRA DE TÍTULO CUSTOMIZADA ───
        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
        barraTitulo.setBackground(pretoCabecalho);
        barraTitulo.setPreferredSize(new java.awt.Dimension(1000, 35));
        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        // Painel Esquerdo: Logo + Título
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);

        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
        try {
            java.net.URL urlLogo = getClass().getResource(favicon); 
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível carregar o logo na barra: " + ex.getMessage());
        }
        painelEsquerdo.add(lblLogo);

        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Entregas Endereço Pendentes - Local");
        lblTituloText.setForeground(brancoPuro);
        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        painelEsquerdo.add(lblTituloText);
        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

        // Painel Direito: Assinatura + Botão Fechar (X)
        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
        painelDireito.setOpaque(false);

        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); 
        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11)); 
        painelDireito.add(lblDesenvolvedor);

        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnFechar.setForeground(brancoPuro);
        btnFechar.setBackground(pretoCabecalho);
        btnFechar.setFocusPainted(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnFechar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnFechar.setBackground(java.awt.Color.RED); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) { btnFechar.setBackground(pretoCabecalho); }
        });
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) { frame.dispose(); }
        });
        painelDireito.add(btnFechar);
        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);

        // Código para Arrastar a Janela
        final java.awt.Point pontoArrastar = new java.awt.Point();
        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pontoArrastar.x = e.getX(); pontoArrastar.y = e.getY();
            }
        });
        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                java.awt.Point p = frame.getLocation();
                frame.setLocation(p.x + e.getX() - pontoArrastar.x, p.y + e.getY() - pontoArrastar.y);
            }
        });

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

        // ─── CUSTOMIZAÇÃO DA TABELA ───
        table = new JTable();
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table.setModel(model);             

        table.setBackground(grafiteClaro);
        table.setForeground(brancoPuro);
        table.setGridColor(cinzaLinhas);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(cinzaLinhas);
        table.setSelectionForeground(brancoPuro);

        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
        cabecalho.setOpaque(true);
        cabecalho.setBackground(cinzaClaroHeader); 
        cabecalho.setForeground(cinzaEscuroTexto); 
        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";
        System.out.println(sql);       

        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            rs = stmt.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           

            for (int i = 1; i <= columnCount; i++) {
                String nomeOriginal = metaData.getColumnName(i).toLowerCase();
                String nomeAmigavel = nomeOriginal; 

                switch (nomeOriginal) {
                    case "idvenda":     nomeAmigavel = "ID VENDA"; break;
                    case "datavenda":   nomeAmigavel = "DATA VENDA"; break;
                    case "nomecli":     nomeAmigavel = "CLIENTE"; break;
                    case "codpeca":     nomeAmigavel = "PEÇA"; break;
                    case "valorfrete":  nomeAmigavel = "VALOR FRETE"; break;
                    case "fretepago":   nomeAmigavel = "FRETE PAGO?"; break;
                    case "entregue":    nomeAmigavel = "ENTREGUE?"; break;
                    case "dataentrega": nomeAmigavel = "DATA ENTREGA"; break;
                    case "status":      nomeAmigavel = "SITUAÇÃO"; break;
                    case "tipoentrega": nomeAmigavel = "TIPO ENVIO"; break;
                    case "canal":       nomeAmigavel = "CANAL"; break;
                }
                model.addColumn(nomeAmigavel);
            }

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(table);
            scrollPane.setBackground(grafiteProfundo);
            scrollPane.getViewport().setBackground(grafiteProfundo);
            scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            frame.add(scrollPane, java.awt.BorderLayout.CENTER);
            
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("Acessou o banco Local com sucesso!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con != null) con.close();
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
                System.out.println("Fim da Pesquisa!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexão local: " + ex.getMessage());
            }
        }
    }
    
    public void pesquisaClientePendentesEntregaEnderecoTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con2 = ConnectionDB.getConnectionCloud();

        // 🎨 Paleta de Cores Idêntica ao Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro = java.awt.Color.WHITE;

        // 🕶️ Tom cinza claro premium de fundo com letras bem escuras para contraste suave
        java.awt.Color cinzaClaroHeader = new java.awt.Color(220, 220, 220); 
        java.awt.Color cinzaEscuroTexto = new java.awt.Color(40, 40, 40); 

        // Configuração Única do Frame Escuro Customizado (Sem barra nativa do Windows)
        frame = new JFrame();
        frame.setUndecorated(true); 
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 500);     
        frame.getContentPane().setBackground(grafiteProfundo);
        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); 

        // ─── BARRA DE TÍTULO CUSTOMIZADA ───
        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
        barraTitulo.setBackground(pretoCabecalho);
        barraTitulo.setPreferredSize(new java.awt.Dimension(1000, 35));
        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        // Painel Esquerdo: Logo + Título
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);

        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
        try {
            java.net.URL urlLogo = getClass().getResource(favicon); 
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível carregar o logo na barra: " + ex.getMessage());
        }
        painelEsquerdo.add(lblLogo);

        // Título específico desta listagem de rotas/envios por endereço
        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Entregas Endereço Pendentes");
        lblTituloText.setForeground(brancoPuro);
        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        painelEsquerdo.add(lblTituloText);
        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

        // Painel Direito: Assinatura + Botão Fechar (X)
        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
        painelDireito.setOpaque(false);

        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); 
        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11)); 
        painelDireito.add(lblDesenvolvedor);

        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnFechar.setForeground(brancoPuro);
        btnFechar.setBackground(pretoCabecalho);
        btnFechar.setFocusPainted(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnFechar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnFechar.setBackground(java.awt.Color.RED); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) { btnFechar.setBackground(pretoCabecalho); }
        });
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) { frame.dispose(); }
        });
        painelDireito.add(btnFechar);
        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);

        // Código para Arrastar a Janela
        final java.awt.Point pontoArrastar = new java.awt.Point();
        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pontoArrastar.x = e.getX(); pontoArrastar.y = e.getY();
            }
        });
        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                java.awt.Point p = frame.getLocation();
                frame.setLocation(p.x + e.getX() - pontoArrastar.x, p.y + e.getY() - pontoArrastar.y);
            }
        });

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

        // ─── CUSTOMIZAÇÃO DA TABELA ───
        table = new JTable();
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table.setModel(model);             

        table.setBackground(grafiteClaro);
        table.setForeground(brancoPuro);
        table.setGridColor(cinzaLinhas);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(cinzaLinhas);
        table.setSelectionForeground(brancoPuro);

        // Cabeçalho com contraste suave (Cinza Claro + Letras Escuras)
        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
        cabecalho.setOpaque(true);
        cabecalho.setBackground(cinzaClaroHeader); 
        cabecalho.setForeground(cinzaEscuroTexto); 
        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";
        System.out.println(sql);       

        try (PreparedStatement localStmt = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt;
            rs = stmt2.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           

            for (int i = 1; i <= columnCount; i++) {
                String nomeOriginal = metaData.getColumnName(i).toLowerCase();
                String nomeAmigavel = nomeOriginal; // Nome padrão caso não entre no filtro

                // Mapeie aqui o nome do Banco -> Nome para o Operador
                switch (nomeOriginal) {
                    case "idvenda":     nomeAmigavel = "ID VENDA"; break;
                    case "datavenda":   nomeAmigavel = "DATA VENDA"; break;
                    case "nomecli":     nomeAmigavel = "CLIENTE"; break;
                    case "codpeca":     nomeAmigavel = "PEÇA"; break;
                    case "valorfrete":  nomeAmigavel = "VALOR FRETE"; break;
                    case "fretepago":   nomeAmigavel = "FRETE PAGO?"; break;
                    case "entregue":    nomeAmigavel = "ENTREGUE?"; break;
                    case "dataentrega": nomeAmigavel = "DATA ENTREGA"; break;
                    case "status":      nomeAmigavel = "SITUAÇÃO"; break;
                    case "tipoentrega": nomeAmigavel = "TIPO ENVIO"; break;
                    case "canal":       nomeAmigavel = "CANAL"; break;
                }

                model.addColumn(nomeAmigavel); // Adiciona o nome bonito na tabela
            }

            boolean possuiDados = false;
            while (rs.next()) {
                possuiDados = true;
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }             
                model.addRow(row);
            }

            if (possuiDados) {
                // CORREÇÃO: Removido o JFrame antigo nativo do Windows que sobrescrevia o design
                table.setModel(model);

                // Customiza o painel de scroll para preencher a área vazia com grafite profundo
                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setBackground(grafiteProfundo);
                scrollPane.getViewport().setBackground(grafiteProfundo);
                scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

                frame.add(scrollPane, java.awt.BorderLayout.CENTER);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }

            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: " + table.getRowCount());               
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
        } catch(SQLException ex) {
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                con2.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void carregaNomeClientePendentesRetireLojaTabelaEntregas(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='RETIRE_LOJA' ORDER BY idvenda ASC";
        System.out.println(sql);       
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            rs = stmt.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           
            
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }
            
            StringBuilder construtorNomes = new StringBuilder();
            boolean primeiroNome = true;
            
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }             
                model.addRow(row);
                
                String nome = rs.getString(3); 
                if (nome != null && !nome.trim().isEmpty()) {
                    if (!primeiroNome) {
                        construtorNomes.append(";");
                    }
                    construtorNomes.append(nome.trim());
                    primeiroNome = false;
                }
            }
            
            if (construtorNomes.length() > 0) {
                e.setNomecli(construtorNomes.toString()); 
                System.out.println("Nomes de entregas locais concatenados para o cache: " + construtorNomes.toString());
            } else {
                e.setNomecli("");
            }
            
            dbRows = table.getRowCount(); // Atualiza a variável global da classe
            System.out.println("Numero de linhas do banco: " + table.getRowCount());               
            System.out.println("Acessou o banco de dados Local com sucesso!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con != null) con.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
  
    public void carregaNomeClientePendentesRetireLojaTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException{                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='RETIRE_LOJA' ORDER BY idvenda ASC";
        System.out.println(sql);       
        try (PreparedStatement localStmt = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt;
            rs = stmt2.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }
            // 🔥 CORREÇÃO 2: Estrutura o StringBuilder para agrupar os nomes das clientes para a RAM
            StringBuilder construtorNomes = new StringBuilder();
            boolean primeiroNome = true;
            
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }             
                model.addRow(row);
                
                // Captura o nome da cliente (Ajuste o índice se a coluna nomecli não for a 3)
                // Se preferir puxar por texto para risco zero de índice: String nome = rs.getString("nomecli");
                String nome = rs.getString(3); 
                if (nome != null && !nome.trim().isEmpty()) {
                    if (!primeiroNome) {
                        construtorNomes.append(";");
                    }
                    construtorNomes.append(nome.trim());
                    primeiroNome = false;
                }
            }
            // 🔥 CORREÇÃO 3: Injeta a String final fatiada com ";" de volta no objeto de modelo
            // Isso permite que o método carregarCacheBuscaClientes() leia os nomes com sucesso [links: 10]
            if (construtorNomes.length() > 0) {
                e.setNomecli(construtorNomes.toString()); 
                System.out.println("Nomes de entregas concatenados para o cache: " + construtorNomes.toString());
            } else {
                e.setNomecli("");
            }
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: "+table.getRowCount());               
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void carregaNomeClientePendentesEntregaEnderecoTabelaEntregas(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con = ConnectionDB.getConnection();
        
        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";
        System.out.println(sql);       
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            
            rs = stmt.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }
            
            StringBuilder construtorNomes = new StringBuilder();
            boolean primeiroNome = true;
            
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }             
                model.addRow(row);
                
                String nome = rs.getString(3); 
                if (nome != null && !nome.trim().isEmpty()) {
                    if (!primeiroNome) {
                        construtorNomes.append(";");
                    }
                    construtorNomes.append(nome.trim());
                    primeiroNome = false;
                }
            }
            
            if (construtorNomes.length() > 0) {
                e.setNomecli(construtorNomes.toString()); 
                System.out.println("Nomes de entregas endereço locais concatenados para o cache: " + construtorNomes.toString());
            } else {
                e.setNomecli("");
            }
            
            dbRows = table.getRowCount(); // Sincroniza variável global
            System.out.println("Numero de linhas do banco: " + dbRows);               
            System.out.println("Acessou o banco de dados Local com sucesso!");
            System.out.println("----------------------------------");
        } catch(SQLException ex) {
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex; // Mantém a propagação de exceção da assinatura
        } finally {
            try {
                if (rs != null) rs.close();
                if (con != null) con.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
   
    public void carregaNomeClientePendentesEntregaEnderecoTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con2 = ConnectionDB.getConnectionCloud();
        
        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";
        System.out.println(sql);       
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2;
            rs = stmt2.executeQuery();           
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }
            
            // 🔥 PADRÃO RETIRE LOJA: Prepara o agrupador de nomes para alimentar o cache local
            StringBuilder construtorNomes = new StringBuilder();
            boolean primeiroNome = true;
            
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }             
                model.addRow(row);
                
                // Captura o nome da cliente (Mantendo o índice 3 correspondente ao 'nomecli')
                String nome = rs.getString(3); 
                if (nome != null && !nome.trim().isEmpty()) {
                    if (!primeiroNome) {
                        construtorNomes.append(";");
                    }
                    construtorNomes.append(nome.trim());
                    primeiroNome = false;
                }
            }
            
            // 🔥 PADRÃO RETIRE LOJA: Injeta o texto fatiado com ";" de volta no objeto de modelo
            if (construtorNomes.length() > 0) {
                e.setNomecli(construtorNomes.toString()); 
                System.out.println("Nomes de entregas endereço concatenados para o cache: " + construtorNomes.toString());
            } else {
                e.setNomecli("");
            }
            
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: " + dbRows);               
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            // FECHAMENTO DEFENSIVO INDIVIDUAL (Blindagem total do Brechó) [links: 10]
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void perquisarEntregaFinalizadaParaTabelaRetireLoja(Entregas e) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        // Ajustado para o parâmetro seguro '?' evitando falhas de execução no PreparedStatement
        sql = "SELECT * FROM entregas WHERE idvenda = ? AND tipoentrega = 'RETIRE_LOJA' AND status = 'ENTREGUE'";
        System.out.println(sql);       
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            // Injeta com segurança a variável estática vinda da sua View
            stmt.setInt(1, TelaEntregas.idVenda);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));                              
                System.out.println("Acessou o banco de dados local com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Item não cadastrado localmente!");
                System.out.println("----------------------------------");          
            }
        } catch (SQLException ex) {
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex; // Mantém a propagação da assinatura original
        } finally {
            try {
                if (rs != null) rs.close();
                if (con != null) con.close();
                System.out.println("Fim da atualização!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void perquisarEntregaFinalizadaParaTabelaRetireLojaCloud(Entregas e) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM entregas WHERE idvenda='"+TelaEntregas.idVenda+"' AND tipoentrega='RETIRE_LOJA' AND status='ENTREGUE'";
        System.out.println(sql);       
         try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2;
            rs = stmt2.executeQuery();
            if(rs.next()){
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));                              
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");          
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con2.close();
                System.out.println("Fim da atualização!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void pesquisarEntregaFinalizadaParaTabelaEntregaEndereco(Entregas e) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM entregas WHERE idvenda = ? AND tipoentrega = 'ENTREGA_ENDERECO'";
        System.out.println(sql);       
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            
            stmt.setInt(1, TelaEntregas.idVenda);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));                              
                System.out.println("Acessou o banco de dados Local com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Item não cadastrado localmente!");
                System.out.println("----------------------------------");          
            }
        } catch (SQLException ex) {
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con != null) con.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void pesquisarEntregaFinalizadaParaTabelaEntregaEnderecoCloud(Entregas e) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM entregas WHERE idvenda='"+TelaEntregas.idVenda+"' AND tipoentrega='ENTREGA_ENDERECO'";
        System.out.println(sql);       
        try (PreparedStatement localStmt = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt;
            rs = stmt2.executeQuery();
            if(rs.next()){
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));                              
                System.out.println("Acessou o banco de dados na Cloud com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");          
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con2.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void buscaNomeClienteEntrega(Entregas e) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();        
        sql = "SELECT nomecli FROM entregas WHERE nomecli LIKE ? AND status= 'DISPONIVEL' AND tipoentrega='RETIRE_LOJA' ORDER BY nomecli LIMIT 15";
        System.out.println(sql);
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            stmt.setString(1, TelaEntregas.nomeClienteEntrega + "%");
            rs = stmt.executeQuery();
            StringBuilder localTodosNomes = new StringBuilder();
            boolean temResultados = false;
            
            while (rs.next()) {
                if (temResultados) {
                    localTodosNomes.append(";");
                }
                localTodosNomes.append(rs.getString("nomecli"));
                temResultados = true;
            }
            
            if (temResultados) {
                e.setNomecli(localTodosNomes.toString());
                System.out.println("Encontrados " + (localTodosNomes.toString().split(";").length) + " clientes");
            } else {
                e.setNomecli("");
                System.out.println("Nenhum cliente encontrado!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            e.setNomecli("");
            System.out.println("----------------------------------");
            throw ex; // Mantém a propagação de exceção da assinatura
        } finally {
            if (con != null) {
                con.close();
            }
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }          
    }

    public void buscaNomeClienteEntregaCloud(Entregas e) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();        
        sql = "SELECT nomecli FROM entregas WHERE nomecli LIKE ? AND status= 'DISPONIVEL' AND tipoentrega='RETIRE_LOJA' ORDER BY nomecli LIMIT 15";
        System.out.println(sql);
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            
            stmt2.setString(1, TelaEntregas.nomeClienteEntrega + "%");
            rs = stmt2.executeQuery();
            StringBuilder localTodosNomes = new StringBuilder();
            boolean temResultados = false;
            
            while (rs.next()) {
                if (temResultados) {
                    localTodosNomes.append(";");
                }
                localTodosNomes.append(rs.getString("nomecli"));
                temResultados = true;
            }
            
            if (temResultados) {
                e.setNomecli(localTodosNomes.toString());
                System.out.println("Encontrados " + (localTodosNomes.toString().split(";").length) + " clientes na Cloud");
            } else {
                e.setNomecli("");
                System.out.println("Nenhum cliente encontrado na Cloud!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro na Nuvem: " + ex.getMessage());
            e.setNomecli("");
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (con2 != null) {
                con2.close();
            }
            System.out.println("Conexão Cloud encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }          
    }
    
    public void buscaNomeClienteEntregaEndereco(Entregas e) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();        
        sql = "SELECT nomecli FROM entregas WHERE nomecli LIKE ? AND status= 'DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY nomecli LIMIT 15";
        System.out.println(sql);
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            stmt.setString(1, TelaEntregas.nomeClienteEntrega + "%");
            rs = stmt.executeQuery();
            StringBuilder localTodosNomes = new StringBuilder();
            boolean temResultados = false;
            
            while (rs.next()) {
                if (temResultados) {
                    localTodosNomes.append(";");
                }
                localTodosNomes.append(rs.getString("nomecli"));
                temResultados = true;
            }
            
            if (temResultados) {
                e.setNomecli(localTodosNomes.toString());
                System.out.println("Encontrados " + (localTodosNomes.toString().split(";").length) + " clientes");
            } else {
                e.setNomecli("");
                System.out.println("Nenhum cliente encontrado!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            e.setNomecli("");
            System.out.println("----------------------------------");
            throw ex; // Mantém a propagação de exceção da assinatura
        } finally {
            if (con != null) {
                con.close();
            }
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }          
    }
    
    public void buscaNomeClienteEntregaEnderecoCloud(Entregas e) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();        
        sql = "SELECT nomecli FROM entregas WHERE nomecli LIKE ? AND status= 'DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY nomecli LIMIT 15";
        System.out.println(sql);
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            
            stmt2.setString(1, TelaEntregas.nomeClienteEntrega + "%");
            rs = stmt2.executeQuery();
            StringBuilder localTodosNomes = new StringBuilder();
            boolean temResultados = false;
            
            while (rs.next()) {
                if (temResultados) {
                    localTodosNomes.append(";");
                }
                localTodosNomes.append(rs.getString("nomecli"));
                temResultados = true;
            }
            
            if (temResultados) {
                e.setNomecli(localTodosNomes.toString());
                System.out.println("Encontrados " + (localTodosNomes.toString().split(";").length) + " clientes na Cloud");
            } else {
                e.setNomecli("");
                System.out.println("Nenhum cliente encontrado na Cloud!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro na Nuvem: " + ex.getMessage());
            e.setNomecli("");
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (con2 != null) {
                con2.close();
            }
            System.out.println("Conexão Cloud encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }          
    }
    
    public void pesquisaPedidosEntreguesRetireLojaTabelaEntregas(Entregas e) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        stmt = null;       
        frame = new JFrame("ENTREGAS FINALIZADAS");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 500);       
        // Create JTable
        table.setModel(model);       
        sql = "SELECT * FROM entregas WHERE status='ENTREGUE' AND tipoentrega='RETIRE_LOJA' ORDER BY idvenda ASC";
        System.out.println(sql);      
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));
                // Get metadata to create columns
                metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();           
                // Add columns to table model
                for (int i = 1; i <= columnCount; i++) {
                    model.addColumn(metaData.getColumnName(i));
                }
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }             
                    model.addRow(row);
                }           
                tabela = table.getColumnModel();
                dbRows = table.getRowCount();
                System.out.println("Numero de linhas do banco: "+table.getRowCount());
                frame.add(new JScrollPane(table));
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);               
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da atualização!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void pesquisaPedidosEntreguesRetireLojaTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {
        if (model != null) {
            model.setColumnCount(0);
            model.setRowCount(0);
            model.fireTableStructureChanged();
        }
        con2 = ConnectionDB.getConnectionCloud();

        // 🎨 Paleta de Cores Idêntica ao Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas     = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho  = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro      = java.awt.Color.WHITE;

        // 🕶️ Tom cinza claro premium de fundo com letras bem escuras para contraste suave
        java.awt.Color cinzaClaroHeader = new java.awt.Color(220, 220, 220); 
        java.awt.Color cinzaEscuroTexto = new java.awt.Color(40, 40, 40); 
        // Inicializa a tabela base local
        table = new JTable();
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table.setModel(model);

        sql = "SELECT idvenda, datavenda, nomecli, codpeca, entregue, dataentrega, status, tipoentrega, canal FROM entregas WHERE status='ENTREGUE' AND tipoentrega='RETIRE_LOJA' AND canal='WEB' AND datavenda >= DATE_SUB(NOW(), INTERVAL 60 DAY) ORDER BY idvenda ASC";
        System.out.println(sql);       
        // 🚀 EXECUÇÃO ASSÍNCRONA PARALELA: Abre a janela e descarrega a Cloud ao fundo sem delay
        new Thread(() -> {
            try {
                stmt2 = con2.prepareStatement(sql);
                rs = stmt2.executeQuery(); // CORREÇÃO: Sem passar a string sql de forma redundante
                metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();           
                // Tradutor inteligente de cabeçalhos das colunas
                for (int i = 1; i <= columnCount; i++) {
                    String nomeOriginal = metaData.getColumnName(i).toLowerCase();
                    String nomeAmigavel = nomeOriginal;
                    switch (nomeOriginal) {
                        case "idvenda":     nomeAmigavel = "ID VENDA"; break;
                        case "datavenda":   nomeAmigavel = "DATA VENDA"; break;
                        case "nomecli":     nomeAmigavel = "CLIENTE"; break;
                        case "codpeca":     nomeAmigavel = "CÓD. PEÇA"; break;
                        case "entregue":    nomeAmigavel = "ENTREGUE?"; break;
                        case "dataentrega": nomeAmigavel = "DATA ENTREGA"; break;
                        case "status":      nomeAmigavel = "SITUAÇÃO"; break;
                        case "tipoentrega": nomeAmigavel = "TIPO ENVIO"; break;
                        case "canal":       nomeAmigavel = "CANAL"; break;
                    }
                    model.addColumn(nomeAmigavel);
                }

                java.text.SimpleDateFormat formatoEntrada = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.text.SimpleDateFormat formatoBrasil = new java.text.SimpleDateFormat("dd/MM/yyyy");

                boolean possuiDados = false;

                // CORREÇÃO CONTÁBIL: Usa diretamente o laço while. Garante que nenhuma linha suma da listagem! 📋
                while (rs.next()) {
                    possuiDados = true;
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        Object valorOriginal = rs.getObject(i);
                        String nomeCol = metaData.getColumnName(i).toLowerCase();

                        // Converte as datas para padrão brasileiro
                        if (valorOriginal != null && (nomeCol.contains("data") || valorOriginal instanceof java.sql.Date)) {
                            try {
                                java.util.Date dataConvertida = formatoEntrada.parse(valorOriginal.toString());
                                row[i - 1] = formatoBrasil.format(dataConvertida);
                            } catch (ParseException ex) {
                                row[i - 1] = valorOriginal;
                            }
                        } else {
                            row[i - 1] = valorOriginal;
                        }
                    }             
                    model.addRow(row);
                }

                if (possuiDados) {
                    // Atualiza a variável de modelo global com segurança
//                    model = modeloNovo;

                    java.awt.EventQueue.invokeLater(() -> {
                        // Instancia e configura o JFrame Dark
                        frame = new JFrame();
                        frame.setUndecorated(true); 
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.setSize(900, 500);     
                        frame.getContentPane().setBackground(grafiteProfundo);
                        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); 

                        // Barra superior Premium
                        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
                        barraTitulo.setBackground(pretoCabecalho);
                        barraTitulo.setPreferredSize(new java.awt.Dimension(1000, 35));
                        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

                        // Lado Esquerdo (Logo + Texto)
                        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
                        painelEsquerdo.setOpaque(false);
                        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
                        try {
                            java.net.URL urlLogo = getClass().getResource(favicon); 
                            if (urlLogo != null) {
                                java.awt.Image imgRed = new ImageIcon(urlLogo).getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                                lblLogo.setIcon(new ImageIcon(imgRed));
                            }
                        } catch (Exception ex) {}
                        painelEsquerdo.add(lblLogo);

                        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Entregas Retire Loja Finalizadas");
                        lblTituloText.setForeground(brancoPuro);
                        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                        painelEsquerdo.add(lblTituloText);
                        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

                        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
                        painelDireito.setOpaque(false);

                        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
                        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); 
                        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11)); 
                        painelDireito.add(lblDesenvolvedor);

                        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
                        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                        btnFechar.setForeground(brancoPuro);
                        btnFechar.setBackground(pretoCabecalho);
                        btnFechar.setFocusPainted(false);
                        btnFechar.setBorderPainted(false);
                        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

                        btnFechar.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseEntered(java.awt.event.MouseEvent evt) { btnFechar.setBackground(java.awt.Color.RED); }
                            @Override
                            public void mouseExited(java.awt.event.MouseEvent evt) { btnFechar.setBackground(pretoCabecalho); }
                        });
                        btnFechar.addActionListener(new java.awt.event.ActionListener() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent evt) { frame.dispose(); }
                        });
                        painelDireito.add(btnFechar);

                        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);

                        // Motor de Arrasto da Janela
                        final java.awt.Point pt = new java.awt.Point();
                        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mousePressed(java.awt.event.MouseEvent e) { pt.x = e.getX(); pt.y = e.getY(); }
                        });
                        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseDragged(java.awt.event.MouseEvent e) {
                                java.awt.Point p = frame.getLocation();
                                frame.setLocation(p.x + e.getX() - pt.x, p.y + e.getY() - pt.y);
                            }
                        });

                        frame.setLayout(new java.awt.BorderLayout());
                        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

                        // 🛠️ MONTAGEM DA TABELA: Associa diretamente o modelo já processado e limpo de clones
                        table = new JTable(model);
                        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                        table.setBackground(grafiteClaro);
                        table.setForeground(brancoPuro);
                        table.setGridColor(cinzaLinhas);
                        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                        table.setRowHeight(24);
                        table.setSelectionBackground(cinzaLinhas);
                        table.setSelectionForeground(brancoPuro);

                        for (int col = 0; col < table.getColumnCount(); col++){
                             table.getColumnModel().getColumn(col).setPreferredWidth(120);
                        }

                        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
                        cabecalho.setOpaque(true);
                        cabecalho.setBackground(cinzaClaroHeader); 
                        cabecalho.setForeground(cinzaEscuroTexto); 
                        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

                        tabela = table.getColumnModel();
                        dbRows = table.getRowCount();
                        JScrollPane scrollPane = new JScrollPane(table);
                        scrollPane.setBackground(grafiteProfundo);
                        scrollPane.getViewport().setBackground(grafiteProfundo);
                        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
                        frame.getContentPane().setBackground(grafiteProfundo);
                        frame.add(scrollPane, java.awt.BorderLayout.CENTER);

                        // 🔥 TRATAMENTO EXCLUSIVO: Altera o ícone de café desta janela flutuante na Barra de Tarefas
                        try {
                            String caminhoFavicon = ConfigLoader.get(favicon);
                            if (caminhoFavicon != null && !caminhoFavicon.trim().isEmpty()) {
                                if (!caminhoFavicon.startsWith("/")) {
                                    caminhoFavicon = "/" + caminhoFavicon;
                                }
                                java.net.URL urlFavicon = getClass().getResource(caminhoFavicon);
                                if (urlFavicon != null) {
                                    // Aplica o escudo da SRS especificamente na variável 'frame'
                                    frame.setIconImage(new javax.swing.ImageIcon(urlFavicon).getImage());
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Aviso: Falha ao aplicar ícone no frame flutuante: " + ex.getMessage());
                        }
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    });
                }else{
                    // Se a busca for vazia, apenas exibe o aviso Dark sem abrir a janela por trás
                    java.awt.EventQueue.invokeLater(() -> {
                        MensagemSistema.mostrarAvisoDark(null, "Nenhum pedido de Retire Loja entregue no histórico.");
                    });                    
                }
                System.out.println("Numero de linhas do banco: " + table.getRowCount());
            } catch(SQLException ex) {
                System.err.println("Erro operacional: " + ex.getMessage());
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt2 != null) stmt2.close();
                    con2.close();
                    System.out.println("Canais de histórico de retiradas encerrados em background.");
                } catch (SQLException ex) {
                    System.err.println("Erro ao encerrar canais: " + ex.getMessage());
                }
            }
        }).start();               
    }
    
    public void atualizaStatusEntregueRetireLojaTabelaEntregas (Entregas e) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "UPDATE entregas SET status='ENTREGUE', entregue=true, dataentrega='"+TelaEntregas.dataEntrega+"'";
        System.out.println(sql);      
        try {
            stmt = con.prepareStatement(sql);                                        
            stmt.executeUpdate();      
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------------------");
        } finally{
            try {
                con.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }     
    }
    
    public void atualizaStatusEntregueRetireLojaTabelaEntregasCloud (Entregas e) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE entregas SET status='ENTREGUE', entregue=true, dataentrega='"+TelaEntregas.dataEntrega+"' WHERE idvenda= '"+TelaEntregas.idVenda+"'";
        System.out.println(sql);      
        try {
            stmt2 = con2.prepareStatement(sql);                                        
            stmt2.executeUpdate();      
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------------------");
        } finally{
            try {
                con2.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }     
    }
    
    public void atualizaStatusDisponivelRetireLojaTabelaEntregas(Entregas e) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        sql = "UPDATE entregas SET status='DISPONIVEL' WHERE id = ?";
        System.out.println(sql);       
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            // Define o idVenda inteiro vindo da sua View
            stmt.setInt(1, TelaEntregas.idVenda);
            stmt.executeUpdate();      
            
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (con != null) {
                    con.close();               
                }
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    public void atualizaStatusDisponivelRetireLojaTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE entregas SET status='DISPONIVEL' WHERE id = ?";
        System.out.println(sql);              
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2;            
            stmt2.setInt(1, TelaEntregas.idVenda);
            stmt2.executeUpdate();                 
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro na Cloud: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (con2 != null) {
                    con2.close();               
                }
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro na Cloud: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void atualizaStatusDisponivelEntregaEnderecoTabelaEntregas(Entregas e) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        // Ajustado para o parâmetro seguro '?' garantindo estabilidade e compatibilidade JDBC
        sql = "UPDATE entregas SET status='DISPONIVEL' WHERE id = ?";
        System.out.println(sql);       
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            // Define o idVenda inteiro vindo da sua View
            stmt.setInt(1, TelaEntregas.idVenda);
            stmt.executeUpdate();       
            
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (con != null) {
                    con.close();               
                }
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    public void atualizaStatusDisponivelEntregaEnderecoTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE entregas SET status='DISPONIVEL' WHERE id = ?";
        System.out.println(sql);       
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            
            stmt2.setInt(1, TelaEntregas.idVenda);
            stmt2.executeUpdate();       
            
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro na Cloud: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (con2 != null) {
                    con2.close();               
                }
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro na Cloud: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
//    public void pesquisarPedidosPendentesEntregaEnderecoTabelaEntregas(Entregas e) throws ClassNotFoundException {        
//        frame = new JFrame("ENTREGAS PENDENTES");
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.setSize(900, 400);       
//        table.setModel(model);        
//        con = ConnectionDB.getConnection();  
//        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";       
//        
//        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
//            this.stmt = localStmt; // Sincroniza com o atributo global da classe
//            
//            // Correção interna: Removido o argumento 'sql' para evitar erros no driver JDBC
//            rs = stmt.executeQuery();
//            
//            if (rs.next()) {
//                // Get metadata to create columns
//                metaData = rs.getMetaData();
//                int columnCount = metaData.getColumnCount();           
//                // Add columns to table model
//                for (int i = 1; i <= columnCount; i++) {
//                    model.addColumn(metaData.getColumnName(i));
//                }
//                
//                // Correção do Bug do Registro Sumido: 'do-while' processa o primeiro rs.next() com sucesso
//                do {
//                    e.setId(rs.getInt(1));
//                    e.setDatavenda(rs.getDate(2));
//                    e.setNomecli(rs.getString(3));
//                    e.setCodpeca(rs.getString(4));
//                    e.setValorfrete(rs.getDouble(5));
//                    e.setFretepago(rs.getBoolean(6));
//                    e.setEntregue(rs.getBoolean(7));
//                    e.setDataentrega(rs.getDate(8));
//                    e.setStatus(rs.getString(9));
//                    e.setTipoentrega(rs.getString(10));
//                    e.setCanal(rs.getString(11));               
//                    
//                    Object[] row = new Object[columnCount];
//                    for (int i = 1; i <= columnCount; i++) {
//                        row[i - 1] = rs.getObject(i);
//                    }             
//                    model.addRow(row);
//                } while (rs.next());
//                
//                tabela = table.getColumnModel();
//                dbRows = table.getRowCount();
//                System.out.println("Numero de linhas do banco: " + table.getRowCount());
//                frame.add(new JScrollPane(table));
//                frame.setVisible(true);
//                frame.setLocationRelativeTo(null);               
//                System.out.println("Acessou o banco de dados com sucesso!");
//                System.out.println("----------------------------------");
//            } else {
//                System.out.println("Item não cadastrado!");
//                System.out.println("----------------------------------");
//            }
//        } catch (SQLException ex) {
//            System.out.println("Erro: " + ex.getMessage());
//            System.out.println("----------------------------------");
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (con != null) con.close();
//                System.out.println("Fim da atualização!");
//                System.out.println("Conexão encerrada!");
//                System.out.println("----------------------------------");
//            } catch (SQLException ex) {
//                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
//                System.out.println("Conexão erro: " + ex.getMessage());
//                System.out.println("----------------------------------");
//            }
//        } 
//    }

//    public void pesquisarPedidosPendentesEntregaEnderecoTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {        
//        frame = new JFrame("ENTREGAS PENDENTES - CLOUD");
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.setSize(900, 400);       
//        table.setModel(model);        
//        con2 = ConnectionDB.getConnectionCloud();  
//        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";       
//        
//        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
//            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
//            
//            rs = stmt2.executeQuery();
//            
//            if (rs.next()) {
//                metaData = rs.getMetaData();
//                int columnCount = metaData.getColumnCount();           
//                for (int i = 1; i <= columnCount; i++) {
//                    model.addColumn(metaData.getColumnName(i));
//                }
//                
//                do {
//                    e.setId(rs.getInt(1));
//                    e.setDatavenda(rs.getDate(2));
//                    e.setNomecli(rs.getString(3));
//                    e.setCodpeca(rs.getString(4));
//                    e.setValorfrete(rs.getDouble(5));
//                    e.setFretepago(rs.getBoolean(6));
//                    e.setEntregue(rs.getBoolean(7));
//                    e.setDataentrega(rs.getDate(8));
//                    e.setStatus(rs.getString(9));
//                    e.setTipoentrega(rs.getString(10));
//                    e.setCanal(rs.getString(11));               
//                    
//                    Object[] row = new Object[columnCount];
//                    for (int i = 1; i <= columnCount; i++) {
//                        row[i - 1] = rs.getObject(i);
//                    }             
//                    model.addRow(row);
//                } while (rs.next());
//                
//                tabela = table.getColumnModel();
//                dbRows = table.getRowCount();
//                System.out.println("Numero de linhas do banco Cloud: " + table.getRowCount());
//                frame.add(new JScrollPane(table));
//                frame.setVisible(true);
//                frame.setLocationRelativeTo(null);               
//                System.out.println("Acessou a Cloud com sucesso!");
//                System.out.println("----------------------------------");
//            } else {
//                System.out.println("Item não cadastrado na Cloud!");
//                System.out.println("----------------------------------");
//            }
//        } catch (SQLException ex) {
//            System.out.println("Erro na Cloud: " + ex.getMessage());
//            System.out.println("----------------------------------");
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (con2 != null) con2.close();
//                System.out.println("Fim da atualização!");
//                System.out.println("Conexão encerrada!");
//                System.out.println("----------------------------------");
//            } catch (SQLException ex) {
//                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
//                System.out.println("Conexão erro na Cloud: " + ex.getMessage());
//                System.out.println("----------------------------------");
//            }
//        } 
//    }
    
    public boolean pesquisarPedidosPendentesEntregaEnderecoTabelaEntregas(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";
        System.out.println(sql); 
        
        boolean possuiDados = false;
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            rs = stmt.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }
            while (rs.next()) {
                possuiDados = true;
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }             
                model.addRow(row);
            }
            
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco Local: " + dbRows);               
            
            // O IF RETO NO DAO: Se encontrou linhas, abre a janela flutuante local.
            if (possuiDados && dbRows > 0) {
                frame = new JFrame("ENTREGAS PENDENTES");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(1000, 500);
                table.setModel(model);
                frame.add(new JScrollPane(table));
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } 
            
            System.out.println("Acessou o banco de dados Local com sucesso!");
            System.out.println("----------------------------------");
            
            return possuiDados;
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con != null) con.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }

    public boolean pesquisarPedidosPendentesEntregaEnderecoTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {                     
        model.setRowCount(0);
        model.setColumnCount(0);
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";
        System.out.println(sql); 
        
        boolean possuiDados = false;
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            
            rs = stmt2.executeQuery();
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }
            while (rs.next()) {
                possuiDados = true;
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }             
                model.addRow(row);
            }
            
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco Cloud: " + dbRows);               
            
            // O IF RETO NO DAO: Se encontrou linhas, abre a janela flutuante Cloud.
            if (possuiDados && dbRows > 0) {
                frame = new JFrame("ENTREGAS PENDENTES");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(1000, 500);
                table.setModel(model);
                frame.add(new JScrollPane(table));
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } 
            
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
            
            return possuiDados;
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con2 != null) con2.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
  
    public void pesquisarPedidosEntreguesEntregaEnderecoTabelaEntregas(Entregas e) throws ClassNotFoundException{        
        // Create JFrame
        frame = new JFrame("ENTREGAS FINALIZADAS");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 400);       
        // Create JTable
        table.setModel(model);       
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM entregas WHERE status='ENTREGUE' AND tipoentrega='ENTREGA_ENDERECO' ORDER BY idvenda ASC";      
        try {
            stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));               
                 // Get metadata to create columns
                metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();           
                // Add columns to table model
                for (int i = 1; i <= columnCount; i++) {
                    model.addColumn(metaData.getColumnName(i));
                }
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }             
                    model.addRow(row);
                }            
                tabela = table.getColumnModel();
                dbRows = table.getRowCount();
                System.out.println("Numero de linhas do banco: "+table.getRowCount());
                frame.add(new JScrollPane(table));
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);                
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da atualização!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void pesquisaPedidosEntreguesEntregaEnderecoTabelaEntregas(Entregas e) throws ClassNotFoundException, SQLException {
        if (model != null) {
            model.setColumnCount(0);
            model.setRowCount(0);
            model.fireTableStructureChanged();
        }
        con = ConnectionDB.getConnection();

        // 🎨 Paleta de Cores Idêntica ao Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas     = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho  = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro      = java.awt.Color.WHITE;
        java.awt.Color cinzaClaroHeader = new java.awt.Color(220, 220, 220); 
        java.awt.Color cinzaEscuroTexto = new java.awt.Color(40, 40, 40); 
        
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        sql = "SELECT * FROM entregas WHERE status='ENTREGUE' AND tipoentrega='ENTREGA_ENDERECO' AND datavenda >= DATE_SUB(NOW(), INTERVAL 60 DAY) ORDER BY idvenda ASC";
        System.out.println(sql);       

        new Thread(() -> {
            // O try-with-resources gerencia o Statement local interno da Thread com total segurança
            try (PreparedStatement localStmt = con.prepareStatement(sql)) {
                this.stmt = localStmt; // Sincroniza com o atributo global da classe
                rs = stmt.executeQuery(); 
                metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();           

                model.setColumnCount(0);

                for (int i = 1; i <= columnCount; i++) {
                    String nomeOriginal = metaData.getColumnName(i).toLowerCase();
                    String nomeAmigavel = nomeOriginal;
                    switch (nomeOriginal) {
                        case "idvenda":     nomeAmigavel = "ID VENDA"; break;
                        case "datavenda":   nomeAmigavel = "DATA VENDA"; break;
                        case "nomecli":     nomeAmigavel = "CLIENTE"; break;
                        case "codpeca":     nomeAmigavel = "CÓD. PEÇA"; break;
                        case "valorfrete":  nomeAmigavel = "VALOR FRETE"; break;
                        case "fretepago":   nomeAmigavel = "FRETE PAGO?"; break;
                        case "entregue":    nomeAmigavel = "ENTREGUE?"; break;
                        case "dataentrega": nomeAmigavel = "DATA ENTREGA"; break;
                        case "status":      nomeAmigavel = "SITUAÇÃO"; break;
                        case "tipoentrega": nomeAmigavel = "TIPO ENVIO"; break;
                        case "canal":       nomeAmigavel = "CANAL"; break;
                    }
                    model.addColumn(nomeAmigavel);
                }

                java.text.SimpleDateFormat formatoEntrada = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.text.SimpleDateFormat formatoBrasil = new java.text.SimpleDateFormat("dd/MM/yyyy");

                boolean possuiDados = false;

                while (rs.next()) {
                    possuiDados = true;
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        Object valorOriginal = rs.getObject(i);
                        String nomeCol = metaData.getColumnName(i).toLowerCase();

                        if (valorOriginal != null && (nomeCol.contains("data") || valorOriginal instanceof java.sql.Date)) {
                            try {
                                java.util.Date dataConvertida = formatoEntrada.parse(valorOriginal.toString());
                                row[i - 1] = formatoBrasil.format(dataConvertida);
                            } catch (java.text.ParseException ex) {
                                row[i - 1] = valorOriginal;
                            }
                        } else {
                            row[i - 1] = valorOriginal;
                        }
                    }             
                    model.addRow(row);
                }

                if (possuiDados) {
                    java.awt.EventQueue.invokeLater(() -> {
                        frame = new JFrame();
                        frame.setUndecorated(true); 
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.setSize(900, 500);     
                        frame.getContentPane().setBackground(grafiteProfundo);
                        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); 

                        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
                        barraTitulo.setBackground(pretoCabecalho);
                        barraTitulo.setPreferredSize(new java.awt.Dimension(1000, 35));
                        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

                        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
                        painelEsquerdo.setOpaque(false);
                        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
                        try {
                            java.net.URL urlLogo = getClass().getResource(favicon); 
                            if (urlLogo != null) {
                                java.awt.Image imgRed = new javax.swing.ImageIcon(urlLogo).getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                                lblLogo.setIcon(new javax.swing.ImageIcon(imgRed));
                            }
                        } catch (Exception ex) {}
                        painelEsquerdo.add(lblLogo);

                        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Histórico de Entregas por Endereço - Local");
                        lblTituloText.setForeground(brancoPuro);
                        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                        painelEsquerdo.add(lblTituloText);
                        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

                        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
                        painelDireito.setOpaque(false);
                        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
                        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); 
                        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
                        painelDireito.add(lblDesenvolvedor);

                        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
                        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                        btnFechar.setForeground(brancoPuro);
                        btnFechar.setBackground(pretoCabecalho);
                        btnFechar.setFocusPainted(false);
                        btnFechar.setBorderPainted(false);
                        btnFechar.addActionListener(evt -> frame.dispose());
                        painelDireito.add(btnFechar);

                        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);

                        // ─── MOTOR DE ARRASTO RECOMPILADO ───
                        final java.awt.Point pontoArrastar = new java.awt.Point();
                        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                                pontoArrastar.x = evt.getX(); pontoArrastar.y = evt.getY();
                            }
                        });
                        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
                            @Override public void mouseDragged(java.awt.event.MouseEvent evt) {
                                java.awt.Point p = frame.getLocation();
                                frame.setLocation(p.x + evt.getX() - pontoArrastar.x, p.y + evt.getY() - pontoArrastar.y);
                            }
                        });

                        frame.setLayout(new java.awt.BorderLayout());
                        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

                        table = new JTable(model);
                        table.setBackground(grafiteClaro);
                        table.setForeground(brancoPuro);
                        table.setGridColor(cinzaLinhas);
                        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                        table.setRowHeight(24);
                        table.setSelectionBackground(cinzaLinhas);
                        table.setSelectionForeground(brancoPuro);

                        javax.swing.table.JTableHeader header = table.getTableHeader();
                        header.setOpaque(true);
                        header.setBackground(cinzaClaroHeader); 
                        header.setForeground(cinzaEscuroTexto); 
                        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

                        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(table);
                        scroll.setBackground(grafiteProfundo);
                        scroll.getViewport().setBackground(grafiteProfundo);
                        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                        frame.add(scroll, java.awt.BorderLayout.CENTER);
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);});}dbRows = table.getRowCount();
                        System.out.println("Acessou o banco de dados Local com sucesso! Linhas: " + dbRows);
                        System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro SQL Local: " + ex.getMessage());
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (con != null) con.close();
                    System.out.println("Conexão Local encerrada com segurança!");
                    System.out.println("Fim da Pesquisa!");
                    System.out.println("----------------------------------");
                } catch (SQLException ex) {}
            }
        }).start();
    }
    
    public void pesquisaPedidosEntreguesEntregaEnderecoTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {
        if (model != null) {
            model.setColumnCount(0);
            model.setRowCount(0);
            model.fireTableStructureChanged();
        }
        con2 = ConnectionDB.getConnectionCloud();

        // 🎨 Paleta de Cores Idêntica ao Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas     = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho  = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro      = java.awt.Color.WHITE;
        java.awt.Color cinzaClaroHeader = new java.awt.Color(220, 220, 220); 
        java.awt.Color cinzaEscuroTexto = new java.awt.Color(40, 40, 40); 
        // Inicializa o modelo de dados de forma isolada
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        sql = "SELECT * FROM entregas WHERE status='ENTREGUE' AND tipoentrega='ENTREGA_ENDERECO' AND datavenda >= DATE_SUB(NOW(), INTERVAL 60 DAY) ORDER BY idvenda ASC";
        System.out.println(sql);       

        new Thread(() -> {
            try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
                this.stmt2 = localStmt2;
                rs = stmt2.executeQuery(); 
                metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();           

                // REMOÇÃO DE CLONES: Garante que o modelo comece com ZERO colunas antes da tradução
                model.setColumnCount(0);

                // Tradutor inteligente de cabeçalhos das colunas
                for (int i = 1; i <= columnCount; i++) {
                    String nomeOriginal = metaData.getColumnName(i).toLowerCase();
                    String nomeAmigavel = nomeOriginal;
                    switch (nomeOriginal) {
                        case "idvenda":     nomeAmigavel = "ID VENDA"; break;
                        case "datavenda":   nomeAmigavel = "DATA VENDA"; break;
                        case "nomecli":     nomeAmigavel = "CLIENTE"; break;
                        case "codpeca":     nomeAmigavel = "CÓD. PEÇA"; break;
                        case "valorfrete":  nomeAmigavel = "VALOR FRETE"; break;
                        case "fretepago":   nomeAmigavel = "FRETE PAGO?"; break;
                        case "entregue":    nomeAmigavel = "ENTREGUE?"; break;
                        case "dataentrega": nomeAmigavel = "DATA ENTREGA"; break;
                        case "status":      nomeAmigavel = "SITUAÇÃO"; break;
                        case "tipoentrega": nomeAmigavel = "TIPO ENVIO"; break;
                        case "canal":       nomeAmigavel = "CANAL"; break;
                    }
                    model.addColumn(nomeAmigavel);
                }

                java.text.SimpleDateFormat formatoEntrada = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.text.SimpleDateFormat formatoBrasil = new java.text.SimpleDateFormat("dd/MM/yyyy");

                boolean possuiDados = false;

                while (rs.next()) {
                    possuiDados = true;
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        Object valorOriginal = rs.getObject(i);
                        String nomeCol = metaData.getColumnName(i).toLowerCase();

                        if (valorOriginal != null && (nomeCol.contains("data") || valorOriginal instanceof java.sql.Date)) {
                            try {
                                java.util.Date dataConvertida = formatoEntrada.parse(valorOriginal.toString());
                                row[i - 1] = formatoBrasil.format(dataConvertida);
                            } catch (java.text.ParseException ex) {
                                row[i - 1] = valorOriginal;
                            }
                        } else {
                            row[i - 1] = valorOriginal;
                        }
                    }             
                    model.addRow(row);
                }

                if (possuiDados) {
                    java.awt.EventQueue.invokeLater(() -> {
                        // Instancia e configura o JFrame Dark de forma isolada
                        frame = new JFrame();
                        frame.setUndecorated(true); 
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.setSize(900, 500);     
                        frame.getContentPane().setBackground(grafiteProfundo);
                        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); 

                        // Barra superior Premium
                        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
                        barraTitulo.setBackground(pretoCabecalho);
                        barraTitulo.setPreferredSize(new java.awt.Dimension(1000, 35));
                        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

                        // Lado Esquerdo (Logo + Texto)
                        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
                        painelEsquerdo.setOpaque(false);
                        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
                        try {
                            java.net.URL urlLogo = getClass().getResource(favicon); 
                            if (urlLogo != null) {
                                java.awt.Image imgRed = new ImageIcon(urlLogo).getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                                lblLogo.setIcon(new ImageIcon(imgRed));
                            }
                        } catch (Exception ex) {}
                        painelEsquerdo.add(lblLogo);

                        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Histórico de Entregas por Endereço");
                        lblTituloText.setForeground(brancoPuro);
                        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                        painelEsquerdo.add(lblTituloText);
                        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

                        // Lado Direito (X + Assinatura)
                        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
                        painelDireito.setOpaque(false);
                        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
                        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); 
                        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
                        painelDireito.add(lblDesenvolvedor);

                        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
                        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                        btnFechar.setForeground(brancoPuro);
                        btnFechar.setBackground(pretoCabecalho);
                        btnFechar.setFocusPainted(false);
                        btnFechar.setBorderPainted(false);
                        btnFechar.addActionListener(evt -> frame.dispose());
                        painelDireito.add(btnFechar);

                        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);

                        // Motor de Arrasto da Janela
                        final java.awt.Point pt = new java.awt.Point();
                        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mousePressed(java.awt.event.MouseEvent e) { pt.x = e.getX(); pt.y = e.getY(); }
                        });
                        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseDragged(java.awt.event.MouseEvent e) {
                                java.awt.Point p = frame.getLocation();
                                frame.setLocation(p.x + e.getX() - pt.x, p.y + e.getY() - pt.y);
                            }
                        });
                            
                        frame.setLayout(new java.awt.BorderLayout());
                        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

                        // FIXAÇÃO DA TABELA: Vincula o modelo limpo e desativa os clones automáticos do Swing
                        table = new JTable(model);
                        table.setAutoCreateColumnsFromModel(false); // Mata a clonagem de cabeçalhos!
                        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                        table.setBackground(grafiteClaro);
                        table.setForeground(brancoPuro);
                        table.setGridColor(cinzaLinhas);
                        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                        table.setRowHeight(24);
                        table.setSelectionBackground(cinzaLinhas);
                        table.setSelectionForeground(brancoPuro);

                        for (int col = 0; col < table.getColumnCount(); col++){
                             table.getColumnModel().getColumn(col).setPreferredWidth(120);
                        }

                        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
                        cabecalho.setOpaque(true);
                        cabecalho.setBackground(cinzaClaroHeader); 
                        cabecalho.setForeground(cinzaEscuroTexto); 
                        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

                        tabela = table.getColumnModel();
                        dbRows = table.getRowCount();

                        JScrollPane scrollPane = new JScrollPane(table);
                        scrollPane.setBackground(grafiteProfundo);
                        scrollPane.getViewport().setBackground(grafiteProfundo);
                        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        frame.add(scrollPane, java.awt.BorderLayout.CENTER);
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                        });
                } else {java.awt.EventQueue.invokeLater(() -> {
                    util.MensagemSistema.mostrarAvisoDark(null, "Nenhum histórico de Entrega Endereço finalizado.");

                    });
                }
                System.out.println("Numero de linhas do banco: " + model.getRowCount());
            } catch(SQLException ex) {
                System.err.println("Erro operacional controlado: " + ex.getMessage());
            } finally  {
                try
                    {if (rs != null) rs.close();
                     if (stmt2 != null) stmt2.close();
                     con2.close();
                    } catch (SQLException ex) {
                        System.err.println("Erro ao fechar conexão: " + ex.getMessage());
                    }                   
                }
        }).start();  
    }
    
    public void inserirDadosComFrete(Entregas e) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        // ==========================================
        // 🔥 SQL COM PEDIDO_ID
        // ==========================================
        sql = "INSERT INTO entregas(idvenda, pedido_id, datavenda, nomecli, codpeca, valorfrete, fretepago, entregue, status, tipoentrega, canal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println(sql);

        // ==========================================
        // 🔥 TRATAR PEDIDO_ID (PODE SER NULO OU VAZIO)
        // ==========================================
        String pedidoId = e.getPedidoId();
        String pedidoIdTratado = null;

        if (pedidoId != null && !pedidoId.trim().isEmpty() && !pedidoId.trim().equals("null")) {
            pedidoIdTratado = pedidoId.trim();
            System.out.println("📋 Pedido ID: " + pedidoIdTratado);
        } else {
            pedidoIdTratado = null;
            System.out.println("📋 Pedido ID: NULL (sem pedido do site)");
        }

        System.out.println("📦 [FRETE] Inserindo frete na Cloud...");
        System.out.println("   ID Venda: " + e.getId());
        System.out.println("   Pedido: " + (pedidoIdTratado != null ? pedidoIdTratado : "N/A"));
        System.out.println("   Cliente: " + e.getNomecli());

        // ==========================================
        // TRATAR VALOR DO FRETE (CORRIGIDO)
        // ==========================================
        double valorFrete = 0.0;
        String valorFreteStr = "0.00";

        try {
            Object valorFreteObj = e.getValorfrete();

            if (valorFreteObj != null) {
                // ==========================================
                // SE FOR STRING
                // ==========================================
                if (valorFreteObj instanceof String) {
                    String valorOriginal = (String) valorFreteObj;
                    if (!valorOriginal.trim().isEmpty()) {
                        valorFrete = util.ValorMonetarioUtil.converterParaDouble(valorOriginal);
                        valorFreteStr = util.ValorMonetarioUtil.formatarParaBanco(valorFrete);
                    }
                } 
                // ==========================================
                // SE FOR DOUBLE
                // ==========================================
                else if (valorFreteObj instanceof Double) {
                    valorFrete = (Double) valorFreteObj;
                    valorFreteStr = util.ValorMonetarioUtil.formatarParaBanco(valorFrete);
                }
                // ==========================================
                // SE FOR OUTRO TIPO (NUMBER, INTEGER, ETC)
                // ==========================================
                else if (valorFreteObj instanceof Number) {
                    valorFrete = ((Number) valorFreteObj).doubleValue();
                    valorFreteStr = util.ValorMonetarioUtil.formatarParaBanco(valorFrete);
                }
            }

        } catch (Exception ex) {
            System.err.println("⚠️ Erro ao converter valor do frete: " + ex.getMessage());
            valorFrete = 0.0;
            valorFreteStr = "0.00";
        }

        System.out.println("   💰 Valor Frete: R$ " + valorFreteStr);

        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setInt(1, e.getId());                                         // idvenda
            // ==========================================
            // 🔥 TRATA PEDIDO_ID (NULL OU VALOR)
            // ==========================================
            if (pedidoIdTratado != null) {
                stmt2.setString(2, pedidoIdTratado);
            } else {
                stmt2.setNull(2, java.sql.Types.VARCHAR);
            }
            stmt2.setDate(3, new java.sql.Date(e.getDatavenda().getTime()));    // datavenda
            stmt2.setString(4, e.getNomecli());                                 // nomecli
            stmt2.setString(5, e.getCodpeca());                                 // codpeca
            stmt2.setDouble(6, valorFrete);                                     // valorfrete
            stmt2.setBoolean(7, e.getFretepago());                              // fretepago
            stmt2.setBoolean(8, e.getEntregue());                               // entregue
            stmt2.setString(9, e.getStatus());                                  // status
            stmt2.setString(10, e.getTipoentrega());                            // tipoentrega
            stmt2.setString(11, e.getCanal());                                  // canal

            stmt2.execute();
            System.out.println("✅ Frete salvo com sucesso!");
            System.out.println("   ID Venda: " + e.getId());
            System.out.println("   Pedido: " + (pedidoIdTratado != null ? pedidoIdTratado : "N/A"));
            System.out.println("   Frete: R$ " + valorFreteStr);
            System.out.println("----------------------------------");

        } catch (SQLException ex) {
            System.err.println("❌ Erro ao salvar frete: " + ex.getMessage());
            throw ex;
        } finally {
            if (stmt2 != null) {
                try { stmt2.close(); } catch (SQLException ex) {}
            }
            if (con2 != null) {
                try { con2.close(); } catch (SQLException ex) {}
            }
            System.out.println("Conexão Cloud encerrada!");
            System.out.println("----------------------------------");
        }
    }

    public void inserirDadosComFreteCloud(Entregas e) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();

        // ==========================================
        // 🔥 SQL COM PEDIDO_ID
        // ==========================================
        sql = "INSERT INTO entregas(idvenda, pedido_id, datavenda, nomecli, codpeca, valorfrete, fretepago, entregue, status, tipoentrega, canal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println(sql);

        // ==========================================
        // 🔥 TRATAR PEDIDO_ID (PODE SER NULO OU VAZIO)
        // ==========================================
        String pedidoId = e.getPedidoId();
        String pedidoIdTratado = null;

        if (pedidoId != null && !pedidoId.trim().isEmpty() && !pedidoId.trim().equals("null")) {
            pedidoIdTratado = pedidoId.trim();
            System.out.println("📋 Pedido ID: " + pedidoIdTratado);
        } else {
            pedidoIdTratado = null;
            System.out.println("📋 Pedido ID: NULL (sem pedido do site)");
        }

        System.out.println("📦 [FRETE] Inserindo frete na Cloud...");
        System.out.println("   ID Venda: " + e.getId());
        System.out.println("   Pedido: " + (pedidoIdTratado != null ? pedidoIdTratado : "N/A"));
        System.out.println("   Cliente: " + e.getNomecli());

        // ==========================================
        // TRATAR VALOR DO FRETE (CORRIGIDO)
        // ==========================================
        double valorFrete = 0.0;
        String valorFreteStr = "0.00";

        try {
            Object valorFreteObj = e.getValorfrete();

            if (valorFreteObj != null) {
                // ==========================================
                // SE FOR STRING
                // ==========================================
                if (valorFreteObj instanceof String) {
                    String valorOriginal = (String) valorFreteObj;
                    if (!valorOriginal.trim().isEmpty()) {
                        valorFrete = util.ValorMonetarioUtil.converterParaDouble(valorOriginal);
                        valorFreteStr = util.ValorMonetarioUtil.formatarParaBanco(valorFrete);
                    }
                } 
                // ==========================================
                // SE FOR DOUBLE
                // ==========================================
                else if (valorFreteObj instanceof Double) {
                    valorFrete = (Double) valorFreteObj;
                    valorFreteStr = util.ValorMonetarioUtil.formatarParaBanco(valorFrete);
                }
                // ==========================================
                // SE FOR OUTRO TIPO (NUMBER, INTEGER, ETC)
                // ==========================================
                else if (valorFreteObj instanceof Number) {
                    valorFrete = ((Number) valorFreteObj).doubleValue();
                    valorFreteStr = util.ValorMonetarioUtil.formatarParaBanco(valorFrete);
                }
            }

        } catch (Exception ex) {
            System.err.println("⚠️ Erro ao converter valor do frete: " + ex.getMessage());
            valorFrete = 0.0;
            valorFreteStr = "0.00";
        }

        System.out.println("   💰 Valor Frete: R$ " + valorFreteStr);

        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setInt(1, e.getId());                                         // idvenda
            // ==========================================
            // 🔥 TRATA PEDIDO_ID (NULL OU VALOR)
            // ==========================================
            if (pedidoIdTratado != null) {
                stmt2.setString(2, pedidoIdTratado);
            } else {
                stmt2.setNull(2, java.sql.Types.VARCHAR);
            }
            stmt2.setDate(3, new java.sql.Date(e.getDatavenda().getTime()));    // datavenda
            stmt2.setString(4, e.getNomecli());                                 // nomecli
            stmt2.setString(5, e.getCodpeca());                                 // codpeca
            stmt2.setDouble(6, valorFrete);                                     // valorfrete
            stmt2.setBoolean(7, e.getFretepago());                              // fretepago
            stmt2.setBoolean(8, e.getEntregue());                               // entregue
            stmt2.setString(9, e.getStatus());                                  // status
            stmt2.setString(10, e.getTipoentrega());                            // tipoentrega
            stmt2.setString(11, e.getCanal());                                  // canal

            stmt2.execute();
            System.out.println("✅ Frete salvo com sucesso!");
            System.out.println("   ID Venda: " + e.getId());
            System.out.println("   Pedido: " + (pedidoIdTratado != null ? pedidoIdTratado : "N/A"));
            System.out.println("   Frete: R$ " + valorFreteStr);
            System.out.println("----------------------------------");

        } catch (SQLException ex) {
            System.err.println("❌ Erro ao salvar frete: " + ex.getMessage());
            throw ex;
        } finally {
            if (stmt2 != null) {
                try { stmt2.close(); } catch (SQLException ex) {}
            }
            if (con2 != null) {
                try { con2.close(); } catch (SQLException ex) {}
            }
            System.out.println("Conexão Cloud encerrada!");
            System.out.println("----------------------------------");
        }
    }
    
    public void pesquisarClienteEntregaEnderecoTabelaEntregas(Entregas e) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM entregas WHERE nomecli= '"+TelaEntregas.nomeClienteEntrega+"' AND tipoentrega= 'ENTREGA_ENDERECO'";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            if(rs.next()){
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));               
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    public boolean pesquisarClienteEntregaEnderecoTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM entregas WHERE nomecli = ? AND tipoentrega = 'ENTREGA_ENDERECO' AND status = 'DISPONIVEL'";
        System.out.println(sql);
        
        boolean localizouRegistro = false;
        
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaEntregas.nomeClienteEntrega);
            rs = stmt2.executeQuery();           
            if (rs.next()) {
                localizouRegistro = true; // 
                
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));               
                
                System.out.println("Acessou o banco de dados na Cloud com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }            
            return localizouRegistro;       
        } catch (SQLException ex) {
            System.err.println("Erro na consulta de entrega endereço: " + ex.getMessage());
            System.out.println("----------------------------------");
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public boolean pesquisarClienteRetireLojaTabelaEntregasCloud(Entregas e) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM entregas WHERE nomecli = ? AND tipoentrega = 'RETIRE_LOJA' AND status = 'DISPONIVEL' AND canal = 'WEB'";
        System.out.println(sql);        
        boolean localizouRegistro = false;       
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaEntregas.nomeClienteEntrega);
            rs = stmt2.executeQuery();           
            if (rs.next()) {
                localizouRegistro = true;
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));               
                
                System.out.println("Acessou o banco de dados na Cloud com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }           
            return localizouRegistro;           
        } catch (SQLException ex) {
            System.err.println("Erro na consulta de retirada em loja: " + ex.getMessage());
            System.out.println("----------------------------------");
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Fim do acesso!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }


    public void inserirDadosSemFrete(Entregas e) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        frame = new JFrame("Entregas Retire Pendentes");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 400);        
        table.setModel(model);      
        sql = "INSERT INTO entregas(idvenda, datavenda, nomecli, codpeca, valorfrete, fretepago, entregue, dataentrega, status, tipoentrega, canal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            stmt.setInt(1, e.getId());
            stmt.setDate(2, new java.sql.Date(e.getDatavenda().getTime()));
            stmt.setString(3, e.getNomecli());
            stmt.setString(4, e.getCodpeca());
            stmt.setDouble(5, e.getValorfrete());
            stmt.setBoolean(6, e.getFretepago());
            stmt.setBoolean(7, e.getEntregue());
            stmt.setDate(8, new java.sql.Date(e.getDatavenda().getTime()));
            stmt.setString(9, e.getStatus());
            stmt.setString(10, e.getTipoentrega());
            stmt.setString(11, e.getCanal());                
            stmt.execute();
            MensagemSistema.mostrarAvisoDark(null, "Frete cadastrado com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Frete Salvo com sucesso!");
            System.out.println("----------------------------------");
        }catch (SQLException ex){
            MensagemSistema.mostrarAvisoDark(null, "Erro ao salvar o Frete: "+ex);
            System.out.println("Erro ao inserir dados: " +ex.toString());
            System.out.println("----------------------------------");
            System.out.println("Erro ao salvar o Frete!");
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão de banco encerrada!");
            System.out.println("----------------------------------");
        }
    }
    
    public void inserirDadosSemFreteCloud(Entregas e) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "INSERT INTO entregas(idvenda, datavenda, nomecli, codpeca, valorfrete, fretepago, entregue, dataentrega, status, tipoentrega, canal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println(sql);
        try {
            stmt2 = con2.prepareStatement(sql);//tabela
            stmt2.setInt(1, e.getId());
            stmt2.setDate(2, new java.sql.Date(e.getDatavenda().getTime()));
            stmt2.setString(3, e.getNomecli());
            stmt2.setString(4, e.getCodpeca());
            stmt2.setDouble(5, e.getValorfrete());
            stmt2.setBoolean(6, e.getFretepago());
            stmt2.setBoolean(7, e.getEntregue());
            stmt2.setDate(8, new java.sql.Date(e.getDatavenda().getTime()));
            stmt2.setString(9, e.getStatus());
            stmt2.setString(10, e.getTipoentrega());
            stmt2.setString(11, e.getCanal());                
            stmt2.execute();
            MensagemSistema.mostrarAvisoDark(null, "Frete cadastrado com sucesso na Cloud!");
            System.out.println("----------------------------------");
            System.out.println("Frete Salvo com sucessona Cloud!");
            System.out.println("----------------------------------");
        }catch (SQLException ex){
            MensagemSistema.mostrarAvisoDark(null, "Erro ao salvar o Frete na Cloud: "+ex);
            System.out.println("Erro ao inserir dados na Cloud: " +ex.toString());
            System.out.println("----------------------------------");
            System.out.println("Erro ao salvar o Frete na Cloud!");
            System.out.println("----------------------------------");
        }finally{
            con2.close();
            System.out.println("Conexão de banco encerrada na Cloud!");
            System.out.println("----------------------------------");
        }
    }

    public void atualizaStatusEntregueTabelaEntregas(Entregas e) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        stmt = null;
        
        sql = "UPDATE ENTREGAS \n" +
                    "SET status = 'ENTREGUE',\n" +
                    "    dataentrega = '"+TelaEntregas.dataEntrega+"',\n" +
                    "    entregue = true\n" +
                    "WHERE idvenda = '"+TelaEntregas.idVenda+"'";
                          
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);                             
            stmt.executeUpdate();       
            System.out.println("Atualizou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------------------");
        } finally{
            try {
                con.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    public void pesquisarClienteRetireLoja(Entregas e) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
         // Create JTable
        table.setModel(model);
        System.out.println("----------------------------");
        System.out.println("Iniciando busca na base...");
        System.out.println("----------------------------");       
        String sql = "SELECT * FROM entregas WHERE nomecli= '"+TelaEntregas.nomeClienteEntrega+"' AND tipoentrega= 'RETIRE_LOJA' AND canal = 'WEB'";
        System.out.println("SQL: "+sql);
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            if(rs.next()){
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));           
                // Get metadata to create columns
                metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();          
                // Add columns to table model
                for (int i = 1; i <= columnCount; i++) {
                    model.addColumn(metaData.getColumnName(i));
                }
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }             
                    model.addRow(row);
                }           
                tabela = table.getColumnModel();
                System.out.println("consultaEntregasPendentes: "+tabela);
                dbRows = table.getRowCount();
                System.out.println("Numero de linhas do banco: "+table.getRowCount());
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
                System.out.println("----------------------------");
                System.out.println("Finalizando busca na base...");
                System.out.println("----------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da atualização!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void pesquisarClienteRetireLojaCloud(Entregas e) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
         // Create JTable
        table.setModel(model);
//        System.out.println("----------------------------");
//        System.out.println("Iniciando busca na base...");
//        System.out.println("----------------------------");       
        String sql = "SELECT * FROM entregas WHERE nomecli= '"+TelaEntregas.nomeClienteEntrega+"' AND tipoentrega= 'RETIRE_LOJA' AND canal = 'WEB'";
        System.out.println("SQL: "+sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            if(rs.next()){
                e.setId(rs.getInt(1));
                e.setDatavenda(rs.getDate(2));
                e.setNomecli(rs.getString(3));
                e.setCodpeca(rs.getString(4));
                e.setValorfrete(rs.getDouble(5));
                e.setFretepago(rs.getBoolean(6));
                e.setEntregue(rs.getBoolean(7));
                e.setDataentrega(rs.getDate(8));
                e.setStatus(rs.getString(9));
                e.setTipoentrega(rs.getString(10));
                e.setCanal(rs.getString(11));           
                // Get metadata to create columns
                metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();          
                // Add columns to table model
                for (int i = 1; i <= columnCount; i++) {
                    model.addColumn(metaData.getColumnName(i));
                }
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }             
                    model.addRow(row);
                }           
                tabela = table.getColumnModel();
                System.out.println("consultaEntregasPendentes: "+tabela);
                dbRows = table.getRowCount();
                System.out.println("Numero de linhas do banco: "+table.getRowCount());
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
                System.out.println("----------------------------");
                System.out.println("Finalizando busca na base...");
                System.out.println("----------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con2.close();
                System.out.println("Fim da atualização!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
     
    public void pesquisarStatusDisponivelClienteRetireLoja(Entregas e) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "SELECT nomecli FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='RETIRE_LOJA' AND canal='WEB'";
        System.out.println("SQL: "+sql);
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            if(rs.next()){               
                e.setStatus(rs.getString(9)); 
                e.setTipoentrega(rs.getString(10));
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Pesquisa NÃO Encontrada!");
                System.out.println("----------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da Pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void pesquisarStatusDisponivelClienteEntregaEndereco(Entregas e) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "SELECT nomecli FROM entregas WHERE status='DISPONIVEL' AND tipoentrega='RETIRE_LOJA' AND canal='WEB'";
        System.out.println("SQL: "+sql);
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            if(rs.next()){
                e.setStatus(rs.getString(9));               
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Pesquisa NÃO Encontrada!");
                System.out.println("----------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da Pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public Etiqueta buscarEnderecoClienteCloud(String nomeCliente) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        // Captura todas as colunas de endereço mapeadas na sua tabela de clientes
        sql = "SELECT nomecli, cepcli, cidadecli, ufcli, enderecocli, numerocli, complementocli, bairrocli FROM cliente WHERE nomecli = ? LIMIT 1";
        System.out.println("Query de Endereço Postal: " + sql);
        
        Etiqueta dest = null;
        
        try (PreparedStatement stmt = con2.prepareStatement(sql)) {
            stmt.setString(1, nomeCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dest = new models.Etiqueta();
                    dest.setNome(rs.getString("nomecli"));
                    dest.setCep(rs.getString("cepcli"));
                    dest.setCidade(rs.getString("cidadecli"));
                    dest.setUf(rs.getString("ufcli"));
                    dest.setEndereco(rs.getString("enderecocli"));
                    dest.setNumero(rs.getString("numerocli"));
                    dest.setComplemento(rs.getString("complementocli"));
                    dest.setBairro(rs.getString("bairrocli"));
                }
            }
            return dest;
        } finally {
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Busca de endereço postal finalizada.");
            System.out.println("----------------------------------");
        }
    }
    
    public void confirmarPagamentoRateioFreteTabelaEntregas(String nomeCliente, java.math.BigDecimal freteTotal) throws SQLException, ClassNotFoundException {
        con2 = ConnectionDB.getConnectionCloud();

        // 1. Busca as linhas dos itens na tabela ENTREGAS que pertencem ao cliente e cujo frete ainda NÃO está pago
        // IMPORTANTE: Ajuste o nome da coluna booleana (ex: 'pago' ou 'frete_pago') conforme seu banco
        String sqlBuscarItens = "SELECT id FROM entregas " +
                               "WHERE UPPER(TRIM(nomecli)) = ? " +
                               "  AND fretepago = false";

        java.util.List<Integer> idsEntregasPendentes = new java.util.ArrayList<>();

        try (PreparedStatement stmtBusca = con2.prepareStatement(sqlBuscarItens)) {
            stmtBusca.setString(1, nomeCliente.trim().toUpperCase());
            try (ResultSet rsBusca = stmtBusca.executeQuery()) {
                while (rsBusca.next()) {
                    idsEntregasPendentes.add(rsBusca.getInt("id"));
                }
            }
        }

        int totalItensEntrega = idsEntregasPendentes.size();
        System.out.println("Itens localizados para rateio na tabela entregas: " + totalItensEntrega);

        // 2. Se houver registros na fila de entrega, executa as atualizações financeiras
        if (totalItensEntrega > 0) {
            java.math.BigDecimal fretePorItem = java.math.BigDecimal.ZERO;
            java.math.BigDecimal diferencaCentavos = java.math.BigDecimal.ZERO;

            // Calcula a divisão apenas se o frete for maior que zero
            if (freteTotal.compareTo(java.math.BigDecimal.ZERO) > 0) {
                fretePorItem = freteTotal.divide(new java.math.BigDecimal(totalItensEntrega), 2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal freteMultiplicado = fretePorItem.multiply(new java.math.BigDecimal(totalItensEntrega));
                diferencaCentavos = freteTotal.subtract(freteMultiplicado);
            }

            // Ativa controle de transação para garantir consistência entre as duas tabelas
            con2.setAutoCommit(false);

            try {
                // 3. Executa o RATEIO linha por linha gravando na coluna 'valorfrete' da tabela ENTREGAS
                String sqlUpdateEntregas = "UPDATE entregas SET valorfrete = ?, pago = true WHERE id = ?";
                try (PreparedStatement stmtUpdateEntregas = con2.prepareStatement(sqlUpdateEntregas)) {
                    for (int i = 0; i < totalItensEntrega; i++) {
                        int idEntrega = idsEntregasPendentes.get(i);
                        java.math.BigDecimal valorFreteDestaPeca = fretePorItem;

                        if (i == 0) {
                            valorFreteDestaPeca = fretePorItem.add(diferencaCentavos); // Ajusta a dízima no primeiro item
                        }

                        stmtUpdateEntregas.setBigDecimal(1, valorFreteDestaPeca);
                        stmtUpdateEntregas.setInt(2, idEntrega);
                        stmtUpdateEntregas.addBatch();
                    }
                    stmtUpdateEntregas.executeBatch();
                }

                // 4. Registra o pagamento ÚNICO e total do frete na tabela VENDAS
                // Ajuste as colunas conforme os campos obrigatórios da sua tabela vendas (ex: datavenda, valorvenda, origemvenda)
                String sqlInsertVendaFrete = "INSERT INTO vendas (datavenda, valorvenda, origemvenda, nomecli, status) " +
                                             "VALUES (CURDATE(), ?, 'frete', ?, 'FINALIZADO')";
                try (PreparedStatement stmtVendas = con2.prepareStatement(sqlInsertVendaFrete)) {
                    stmtVendas.setBigDecimal(1, freteTotal);
                    stmtVendas.setString(2, nomeCliente.trim().toUpperCase());
                    stmtVendas.executeUpdate();
                }

                con2.commit(); // Confirma o fechamento das duas tabelas simultaneamente na nuvem
                System.out.println("Sucesso: Rateio realizado em 'entregas' e pagamento total registrado em 'vendas'.");

            } catch (SQLException ex) {
                con2.rollback(); // Cancela toda a operação em caso de falha de conexão para proteger o caixa
                throw ex;
            } finally {
                con2.setAutoCommit(true);
            }
        } else {
            System.out.println("Aviso: Nenhum item pendente de frete foi localizado para o cliente: " + nomeCliente);
        }
    }
}
