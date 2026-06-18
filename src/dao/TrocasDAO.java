package dao;

import connection.ConnectionDB;
import static dao.VendasDAO.dbRows;
import static dao.VendasDAO.tabela;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import models.Cliente;
import models.Produto;
import models.Trocas;
import util.ConfigLoader;
import static views.TelaFinanceiro.idTrocas;
import static views.TelaFinanceiro.nomeCliente;
import views.TelaFornecedor;
import views.TelaTrocas;
import static views.TelaTrocas.codigoPeca;

public class TrocasDAO {
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    private static String sql;
    Connection con;
    Connection con2;
    ResultSet rs;
    JFrame frame;
    JTable table;
    DefaultTableModel model;
    ClienteDAO cdao = new ClienteDAO();
    Cliente c = new Cliente();
    ProdutoDAO pdao = new ProdutoDAO();
    Produto p = new Produto();
    ResultSetMetaData metaData;
    StringBuilder todosNomes;
    String favicon = ConfigLoader.get("sistema.favicon");

    public void saveCredito(Trocas t) throws ClassNotFoundException, SQLException, Exception {
        con = ConnectionDB.getConnection();
        sql = "INSERT INTO trocas(id, nomecliente, datatroca, pecatroca, pecavalor, creditocliente, obs, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            stmt = con.prepareStatement(sql);//tabela
            stmt.setInt(1, t.getId());
            stmt.setString(2, t.getNomeCliente());
            stmt.setDate(3, new java.sql.Date(t.getDataTroca().getTime()));
            stmt.setString(4, t.getPecaTroca());
            stmt.setDouble(5, t.getPecaValor());
            stmt.setDouble(6, t.getCreditoCliente());
            stmt.setString(7, t.getObs());
            stmt.setString(8, t.getStatus());
            stmt.execute();
            JOptionPane.showMessageDialog(null, "Credito cadastrado com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Credito Salvo com sucesso!");
            System.out.println("----------------------------------");
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null, "Erro ao salvar o Credito: "+ex);
            System.out.println("Erro ao inserir dados: " +ex.toString());
            System.out.println("----------------------------------");
            System.out.println("Erro ao salvar o Credito!");
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão de banco encerrada!");
            System.out.println("----------------------------------");
        }
    }

    public void saveCreditoCloud(Trocas t) throws ClassNotFoundException, SQLException, Exception {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "INSERT INTO trocas(id, nomecliente, datatroca, pecatroca, pecavalor, creditocliente, obs, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            stmt2 = con2.prepareStatement(sql);//tabela
            stmt2.setInt(1, t.getId());
            stmt2.setString(2, t.getNomeCliente());
            stmt2.setDate(3, new java.sql.Date(t.getDataTroca().getTime()));
            stmt2.setString(4, t.getPecaTroca());
            stmt2.setDouble(5, t.getPecaValor());
            stmt2.setDouble(6, t.getCreditoCliente());
            stmt2.setString(7, t.getObs());
            stmt2.setString(8, t.getStatus());
            stmt2.execute();
            JOptionPane.showMessageDialog(null, "Credito cadastrado na Cloud com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Credito Salvo com sucesso!");
            System.out.println("----------------------------------");
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null, "Erro ao salvar o Credito: "+ex);
            System.out.println("Erro ao inserir dados: " +ex.toString());
            System.out.println("----------------------------------");
            System.out.println("Erro ao salvar o Credito!");
            System.out.println("----------------------------------");
        }finally{
            con2.close();
            System.out.println("Conexão de banco encerrada!");
            System.out.println("----------------------------------");
        }
    }

    public void selectIdTrocas(Trocas t) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "SELECT MAX(id) FROM trocas";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                t.setId(rs.getInt(1));
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }
            stmt.execute();
            System.out.println("Acessou o banco de dados!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro ao buscar dados: " +ex.toString());
            System.out.println("Erro ao pesquisar id!");
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }
    }

    public void selectIdTrocasCloud(Trocas t) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT MAX(id) FROM trocas";
        System.out.println(sql);
        try {
            stmt2 = con2.prepareStatement(sql);//tabela
            rs = stmt2.executeQuery();
            if(rs.next()){
                t.setId(rs.getInt(1));
                System.out.println("Acessou o banco de dados na Cloud com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }
            stmt2.execute();
            System.out.println("Acessou o banco de dados!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro ao buscar dados: " +ex.toString());
            System.err.println("Erro ao pesquisar id!");
            System.out.println("----------------------------------");
        }finally{
            con2.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }
    }

//    public void atualizaTabelaCreditoLoja() throws ClassNotFoundException{
//        con = ConnectionDB.getConnection();
//        // Create JFrame
//        frame = new JFrame("Creditos Ativos");
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.setSize(900, 300);
//         // Create JTable
//        table = new JTable();
//        model = new DefaultTableModel();
//        table.setModel(model);
//        sql = "SELECT * FROM trocas where status='ATIVO' ORDER BY ID ASC";
//        System.out.println(sql);
//         try {
//            stmt = con.prepareStatement(sql);
//            rs = stmt.executeQuery();
//            // Get metadata to create columns
//            metaData = rs.getMetaData();
//            int columnCount = metaData.getColumnCount();
//            // Add columns to table model
//            for (int i = 1; i <= columnCount; i++) {
//                model.addColumn(metaData.getColumnName(i));
//            }
//            while (rs.next()) {
//                Object[] row = new Object[columnCount];
//                for (int i = 1; i <= columnCount; i++) {
//                    row[i - 1] = rs.getObject(i);
//                }
//                model.addRow(row);
//            }
//            tabela = table;
//            System.out.println("consulta Creditos Ativos: "+tabela);
//            dbRows = table.getRowCount();
//            System.out.println("Numero de linhas do banco: "+table.getRowCount());
//            frame.add(new JScrollPane(table));
//            frame.setVisible(true);
//            frame.setLocationRelativeTo(null);
//            System.out.println("Acessou o banco de dados com sucesso!");
//            System.out.println("----------------------------------");
//            System.out.println("----------------------------------");
//        } catch(SQLException ex){
//                System.out.println("Erro: "+ ex.getMessage());
//                System.out.println("----------------------------------");
//        }finally{
//            try {
//                con.close();
//                System.out.println("Fim da busca!");
//                System.out.println("Conexão encerrada!");
//                System.out.println("----------------------------------");
//            } catch (SQLException ex) {
//                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
//                System.out.println("Conexão erro: "+ ex.getMessage());
//                System.out.println("----------------------------------");
//            }
//        }
//    }

    public void atualizaTabelaCreditoLoja() throws ClassNotFoundException {
        con = ConnectionDB.getConnection();

         // 🎨 Definição da Paleta de Cores Dark do Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro = java.awt.Color.WHITE;

        // Create JFrame sem a barra branca nativa
        frame = new JFrame();
        frame.setUndecorated(true); // Remove a barra branca do Windows 🛠️
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 380); // Ajustado para dar espaço à nova barra superior
        frame.getContentPane().setBackground(grafiteProfundo);
        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); // Borda externa fina da janela

        // ─── CRIAÇÃO DA BARRA DE TÍTULO CUSTOMIZADA ───
        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
        barraTitulo.setBackground(pretoCabecalho);
        barraTitulo.setPreferredSize(new java.awt.Dimension(900, 35));
        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        // Painel Esquerdo: Logo + Texto
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);

        // 🖼️ Adicionando o seu Logo (Ajuste o caminho da imagem do seu projeto)
        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
        try {
            // Substitua pelo caminho correto do seu logo (ex: "/imagens/logo.png" ou na raiz)
            java.net.URL urlLogo = getClass().getResource("/views/logo_icone.png");
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                // Redimensiona o logo para caber perfeitamente na barra (20x20 pixels)
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception e) {
            System.err.println("Não foi possível carregar o logo na barra: " + e.getMessage());
        }
        painelEsquerdo.add(lblLogo);

        // Texto do Título
        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Créditos Ativos");
        lblTituloText.setForeground(brancoPuro);
        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        painelEsquerdo.add(lblTituloText);
        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

         // ─── PAINEL DIREITO: TEXTO DE CONSULTORIA + BOTÃO FECHAR ───
        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
        painelDireito.setOpaque(false);

        // Label com os créditos de desenvolvimento (Mesmo padrão da tela de trás)
        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55)); // Um cinza elegante e discreto
        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireito.add(lblDesenvolvedor);

        // Botão Fechar Customizado (X)
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frame.dispose();
            }
        });
        painelDireito.add(btnFechar);

        // Adiciona o painel completo na extremidade direita (EAST) da barra de títulos
        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);
    //    barraTitulo.add(btnFechar, java.awt.BorderLayout.EAST);

        // 🫳 Permite arrastar a janela pela barra customizada (já que a nativa sumiu)
        final java.awt.Point pontoArrastar = new java.awt.Point();
        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pontoArrastar.x = e.getX();
                pontoArrastar.y = e.getY();
            }
        });
        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                java.awt.Point p = frame.getLocation();
                frame.setLocation(p.x + e.getX() - pontoArrastar.x, p.y + e.getY() - pontoArrastar.y);
            }
        });

        // Define o layout geral do Frame para organizar a barra no topo e a tabela no centro
        frame.setLayout(new java.awt.BorderLayout());
        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

        // Create JTable
        table = new JTable();
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table.setModel(model);

        // Estilização do Corpo da Tabela
        table.setBackground(grafiteClaro);
        table.setForeground(brancoPuro);
        table.setGridColor(cinzaLinhas);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(cinzaLinhas);
        table.setSelectionForeground(brancoPuro);

        // Estilização do Cabeçalho da Tabela
        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
        cabecalho.setBackground(java.awt.Color.DARK_GRAY);
        cabecalho.setForeground(brancoPuro);
        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

        sql = "SELECT * FROM trocas where status='ATIVO' ORDER BY ID ASC";
        System.out.println(sql);

        try {
            stmt = con.prepareStatement(sql);
            // CORREÇÃO: Executa sem passar a string sql novamente, pois ela já foi tratada no prepareStatement
            rs = stmt.executeQuery();

            // Get metadata to create columns
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Add columns to table model
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i).toUpperCase()); // Cabeçalho em CAIXA ALTA
            }

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            tabela = table;
            System.out.println("consulta Creditos Ativos: " + tabela);
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: " + table.getRowCount());

            // 🎨 Criação e Customização do Painel de Rolagem (Scroll)
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBackground(grafiteProfundo);
            scrollPane.getViewport().setBackground(grafiteProfundo);
            scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));

            // Adiciona um espaçamento (borda interna invisível) para a tabela não colar nos cantos da janela
            scrollPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
                javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)
            ));

            frame.add(scrollPane);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
        } catch(SQLException ex) {
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    public void atualizaTabelaCreditoLojaCloud() throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();

         // 🎨 Definição da Paleta de Cores Dark do Projeto Central
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro = java.awt.Color.WHITE;

        // Create JFrame sem a barra branca nativa
        frame = new JFrame();
        frame.setUndecorated(true); // Remove a barra branca do Windows 🛠️
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 380); // Ajustado para dar espaço à nova barra superior
        frame.getContentPane().setBackground(grafiteProfundo);
        frame.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); // Borda externa fina da janela

        // ─── CRIAÇÃO DA BARRA DE TÍTULO CUSTOMIZADA ───
        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
        barraTitulo.setBackground(pretoCabecalho);
        barraTitulo.setPreferredSize(new java.awt.Dimension(900, 35));
        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        // Painel Esquerdo: Logo + Texto
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);

        // 🖼️ Adicionando o seu Logo (Ajuste o caminho da imagem do seu projeto)
        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
        try {
            // Substitua pelo caminho correto do seu logo (ex: "/imagens/logo.png" ou na raiz)
            java.net.URL urlLogo = getClass().getResource(favicon);
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                // Redimensiona o logo para caber perfeitamente na barra (20x20 pixels)
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception e) {
            System.err.println("Não foi possível carregar o logo na barra: " + e.getMessage());
        }
        painelEsquerdo.add(lblLogo);

        // Texto do Título
        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Créditos Ativos");
        lblTituloText.setForeground(brancoPuro);
        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        painelEsquerdo.add(lblTituloText);
        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

         // ─── PAINEL DIREITO: TEXTO DE CONSULTORIA + BOTÃO FECHAR ───
        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
        painelDireito.setOpaque(false);

        // Label com os créditos de desenvolvimento (Mesmo padrão da tela de trás)
        javax.swing.JLabel lblDesenvolvedor = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDesenvolvedor.setForeground(new java.awt.Color(212, 175, 55));
        lblDesenvolvedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireito.add(lblDesenvolvedor);

        // Botão Fechar Customizado (X)
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frame.dispose();
            }
        });
        painelDireito.add(btnFechar);

        // Adiciona o painel completo na extremidade direita (EAST) da barra de títulos
        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);
    //    barraTitulo.add(btnFechar, java.awt.BorderLayout.EAST);

        // 🫳 Permite arrastar a janela pela barra customizada (já que a nativa sumiu)
        final java.awt.Point pontoArrastar = new java.awt.Point();
        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pontoArrastar.x = e.getX();
                pontoArrastar.y = e.getY();
            }
        });
        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                java.awt.Point p = frame.getLocation();
                frame.setLocation(p.x + e.getX() - pontoArrastar.x, p.y + e.getY() - pontoArrastar.y);
            }
        });

        // Define o layout geral do Frame para organizar a barra no topo e a tabela no centro
        frame.setLayout(new java.awt.BorderLayout());
        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

        // Create JTable
        table = new JTable();
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table.setModel(model);

        // Estilização do Corpo da Tabela
        table.setBackground(grafiteClaro);
        table.setForeground(brancoPuro);
        table.setGridColor(cinzaLinhas);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(cinzaLinhas);
        table.setSelectionForeground(brancoPuro);

        // Estilização do Cabeçalho da Tabela
        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
        cabecalho.setBackground(grafiteClaro);
        cabecalho.setForeground(new java.awt.Color(40, 40, 40));
        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        sql = "SELECT * FROM trocas where status='ATIVO' ORDER BY ID ASC";
        System.out.println(sql);

        try {
            stmt2 = con2.prepareStatement(sql);
            // CORREÇÃO: Executa sem passar a string sql novamente, pois ela já foi tratada no prepareStatement
            rs = stmt2.executeQuery();

            // Get metadata to create columns
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Add columns to table model
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i).toUpperCase()); // Cabeçalho em CAIXA ALTA
            }

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            tabela = table;
            System.out.println("consulta Creditos Ativos: " + tabela);
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: " + table.getRowCount());

            // 🎨 Criação e Customização do Painel de Rolagem (Scroll)
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBackground(grafiteProfundo);
            scrollPane.getViewport().setBackground(grafiteProfundo);
            scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));

            // Adiciona um espaçamento (borda interna invisível) para a tabela não colar nos cantos da janela
            scrollPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
                javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)
            ));

             // Injeta o grid no centro do frame (abaixo da barra customizada)
            frame.getContentPane().setBackground(grafiteProfundo);
            frame.add(scrollPane, java.awt.BorderLayout.CENTER);
            
            // 🔥 TRATAMENTO EXCLUSIVO: Altera o ícone de café desta janela flutuante na Barra de Tarefas
            try {
                String caminhoFavicon = ConfigLoader.get("sistema.favicon");
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
            } catch (Exception e) {
                System.err.println("Aviso: Falha ao aplicar ícone no frame flutuante: " + e.getMessage());
            }
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                con2.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    public void atualizaStatusCredito(Trocas t) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "UPDATE trocas SET status='FINALIZADO' WHERE id="+idTrocas+"";
        System.out.println(sql);
        try{
            stmt = con.prepareStatement(sql);
            stmt.execute();
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
        }catch(SQLException ex){
            System.err.println("Erro: "+ex.getMessage());
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Atualização!");
            System.out.println("----------------------------------");
        }
    }

    public void atualizaItemStatusDisponivel(Produto p) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "UPDATE estoque SET status='DISPONIVEL', datavenda=NULL WHERE codpeca = ?";
        System.out.println("Query de Devolução ao Estoque preparada.");

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, codigoPeca);
            int linhasAfetadas = stmt.executeUpdate();

            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("Linhas atualizadas no estoque: " + linhasAfetadas);
            System.out.println("----------------------------------");

        } catch (SQLException ex) {
            System.err.println("Erro ao atualizar status do item: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException ex) {}
            if (con != null) try { con.close(); } catch (SQLException ex) {}

            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Atualização!");
            System.out.println("----------------------------------");
        }
    }

    public void atualizaItemStatusDisponivelCloud(Produto p) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE estoque SET status='DISPONIVEL', datavenda=NULL WHERE codpeca = ?";
        System.out.println("Query de Devolução ao Estoque preparada.");

        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, codigoPeca);
            int linhasAfetadas = stmt2.executeUpdate();

            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("Linhas atualizadas no estoque: " + linhasAfetadas);
            System.out.println("----------------------------------");

        } catch (SQLException ex) {
            System.err.println("Erro ao atualizar status do item: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}

            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Atualização!");
            System.out.println("----------------------------------");
        }
    }

    public void buscaNomeCliente(Trocas t) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT nomecliente FROM trocas WHERE nomecliente LIKE ? AND status= 'ATIVO' ORDER BY nomecliente LIMIT 15";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, TelaTrocas.nomeCliente + "%");
            rs = stmt.executeQuery();
            todosNomes = new StringBuilder();
            boolean temResultados = false;
            while (rs.next()) {
                if (temResultados) {
                    todosNomes.append(";");
                }
                todosNomes.append(rs.getString("nomecliente"));
                temResultados = true;
            }
            if (temResultados) {
                t.setNomeCliente(todosNomes.toString());
                System.out.println("Encontrados " + (todosNomes.toString().split(";").length) + " clientes");
            } else {
                t.setNomeCliente("");
                System.out.println("Nenhum cliente encontrado!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            t.setNomeCliente("");
            System.out.println("----------------------------------");
        } finally {
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }

    public void buscaNomeClienteCloud(Cliente c) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT nomecli FROM cliente WHERE nomecli LIKE ? ORDER BY nomecli LIMIT 15";
        System.out.println(sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaTrocas.nomeCliente + "%");
            rs = stmt2.executeQuery();
            todosNomes = new StringBuilder();
            boolean temResultados = false;
            while (rs.next()) {
                if (temResultados) {
                    todosNomes.append(";");
                }
                todosNomes.append(rs.getString("nomecli"));
                temResultados = true;
            }
            if (temResultados) {
                c.setNomeCli(todosNomes.toString());
                System.out.println("Encontrados " + (todosNomes.toString().split(";").length) + " clientes");
            } else {
                c.setNomeCli("");
                System.out.println("Nenhum cliente encontrado!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            c.setNomeCli("");
            System.out.println("----------------------------------");
        } finally {
            con2.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }

    public double buscarSaldoValeAtivoCloud(String nomeCliente) throws ClassNotFoundException, SQLException {
        String sql = "SELECT SUM(creditocliente) FROM trocas WHERE nomecliente = ? AND status = 'ATIVO'";

        try (Connection con = ConnectionDB.getConnectionCloud();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, nomeCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1); // Retorna a soma de créditos ativos
                }
            }
        }
        return 0.0;
    }

    public void baixarValeUtilizadoCloud(String nomeCliente) throws ClassNotFoundException, SQLException {
        String sql = "UPDATE trocas SET status = 'UTILIZADO' WHERE nomecliente = ? AND status = 'ATIVO'";

        try (Connection con = ConnectionDB.getConnectionCloud();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, nomeCliente);
            stmt.executeUpdate();
            System.out.println("Créditos de vale-troca do cliente " + nomeCliente + " foram baixados.");
        }
    }

    public void buscaHistoricoClienteTrocasCloud(String nomeCliente) throws ClassNotFoundException, SQLException {
        // 🎨 Paleta de Cores Idêntica ao Projeto Central (Modo Dark Premium)
        java.awt.Color grafiteProfundo = new java.awt.Color(30, 30, 30);
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas     = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho  = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro      = java.awt.Color.WHITE;
        java.awt.Color cinzaClaroHeader = new java.awt.Color(220, 220, 220);
        java.awt.Color cinzaEscuroTexto = new java.awt.Color(40, 40, 40);

        // CORREÇÃO SQL: Adicionado o "FROM" que faltava na estrutura da query 🛠️
        String sql = "SELECT id, datatroca, nomecliente, obs, creditocliente, status FROM trocas WHERE nomecliente = ? ORDER BY id DESC";
        System.out.println(sql);

        // 🚀 EXECUÇÃO ASSÍNCRONA EM SEGUNDO PLANO: Busca na nuvem sem congelar o Caixa
        new Thread(() -> {
            try (java.sql.Connection con2 = ConnectionDB.getConnectionCloud();
                 java.sql.PreparedStatement stmt2 = con2.prepareStatement(sql)) {

                stmt2.setString(1, nomeCliente);
                java.sql.ResultSet rs = stmt2.executeQuery();
                java.sql.ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // 🛠️ Definição das colunas traduzidas em Caixa Alta
                java.util.ArrayList<String> colunasBonitas = new java.util.ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String nomeOriginal = metaData.getColumnName(i).toLowerCase();
                    String nomeAmigavel = nomeOriginal;
                    switch (nomeOriginal) {
                        case "id":          nomeAmigavel = "ID TROCA"; break;
                        case "datatroca": nomeAmigavel = "DATA LANÇAMENTO"; break;
                        case "nomecliente": nomeAmigavel = "CLIENTE DO BRECHÓ"; break;
                        case "obs":   nomeAmigavel = "MOTIVO / DESCRIÇÃO"; break;
                        case "creditocliente":       nomeAmigavel = "VALOR CRÉDITO (R$)"; break;
                        case "status":      nomeAmigavel = "SITUAÇÃO DO VALE"; break;
                    }
                    colunasBonitas.add(nomeAmigavel);
                }

                // Inicializa o modelo de dados de forma isolada
                javax.swing.table.DefaultTableModel modeloNovo = new javax.swing.table.DefaultTableModel(colunasBonitas.toArray(), 0) {
                    @Override public boolean isCellEditable(int row, int col) { return false; }
                };

                java.text.SimpleDateFormat formatoEntrada = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.text.SimpleDateFormat formatoBrasil = new java.text.SimpleDateFormat("dd/MM/yyyy");

                boolean possuiDados = false;

                while (rs.next()) {
                    possuiDados = true;
                    Object[] row = new Object[columnCount];

                    for (int i = 1; i <= columnCount; i++) {
                        Object valorOriginal = rs.getObject(i);
                        String nomeCol = metaData.getColumnName(i).toLowerCase();

                        if (valorOriginal != null) {
                            // 1. Converte e formata as datas de lançamento para padrão brasileiro 🇧🇷
                            if (nomeCol.contains("data") || valorOriginal instanceof java.sql.Date) {
                                try {
                                    java.util.Date dataConvertida = formatoEntrada.parse(valorOriginal.toString());
                                    row[i - 1] = formatoBrasil.format(dataConvertida);
                                } catch (Exception ex) {
                                    row[i - 1] = valorOriginal.toString();
                                }
                            // 2. Formata valores contábeis para exibir com vírgula de forma amigável
                            } else if (nomeCol.contains("valor")) {
                                row[i - 1] = String.format("R$ %.2f", Double.parseDouble(valorOriginal.toString())).replace(".", ",");
                            } else {
                                row[i - 1] = valorOriginal.toString().toUpperCase(); // Coloca textos em caixa alta
                            }
                        } else {
                            row[i - 1] = "";
                        }
                    }
                    modeloNovo.addRow(row);
                }

                // ─── 🚀 CONSTRUÇÃO DO RECEPTÁCULO VISUAL (Roda estritamente se houver registros) ───
                if (possuiDados) {
                    java.awt.EventQueue.invokeLater(() -> {
                        javax.swing.JFrame frameHist = new javax.swing.JFrame();
                        frameHist.setUndecorated(true);
                        frameHist.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                        frameHist.setSize(900, 400);
                        frameHist.getContentPane().setBackground(grafiteProfundo);
                        frameHist.getRootPane().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));

                        // Barra superior integrada
                        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
                        barraTitulo.setBackground(pretoCabecalho);
                        barraTitulo.setPreferredSize(new java.awt.Dimension(900, 35));
                        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

                        // Lado Esquerdo (Logo + Título dinâmico)
                        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
                        painelEsquerdo.setOpaque(false);
                        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
                        try {
                            java.net.URL urlLogo = getClass().getResource("/views/logo_icone.png");
                            if (urlLogo != null) {
                                java.awt.Image imgRed = new javax.swing.ImageIcon(urlLogo).getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                                lblLogo.setIcon(new javax.swing.ImageIcon(imgRed));
                            }
                        } catch (Exception ex) {}
                        painelEsquerdo.add(lblLogo);

                        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Histórico de Créditos: " + nomeCliente.toUpperCase());
                        lblTituloText.setForeground(brancoPuro);
                        lblTituloText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                        painelEsquerdo.add(lblTituloText);
                        barraTitulo.add(painelEsquerdo, java.awt.BorderLayout.WEST);

                        // Lado Direito (X + Desenvolvedor)
                        javax.swing.JPanel painelDireito = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
                        painelDireito.setOpaque(false);
                        javax.swing.JLabel lblDev = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
                        lblDev.setForeground(new java.awt.Color(212, 175, 55));
                        lblDev.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
                        painelDireito.add(lblDev);

                        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
                        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                        btnFechar.setForeground(brancoPuro);
                        btnFechar.setBackground(pretoCabecalho);
                        btnFechar.setFocusPainted(false);
                        btnFechar.setBorderPainted(false);
                        btnFechar.addActionListener(evt -> frameHist.dispose());
                        painelDireito.add(btnFechar);
                        barraTitulo.add(painelDireito, java.awt.BorderLayout.EAST);

                        // Motor de Arrasto fluído
                        final java.awt.Point pt = new java.awt.Point();
                        barraTitulo.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override public void mousePressed(java.awt.event.MouseEvent e) { pt.x = e.getX(); pt.y = e.getY(); }
                        });
                        barraTitulo.addMouseMotionListener(new java.awt.event.MouseAdapter() {
                            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                                java.awt.Point p = frameHist.getLocation();
                                frameHist.setLocation(p.x + e.getX() - pt.x, p.y + e.getY() - pt.y);
                            }
                        });

                        frameHist.setLayout(new java.awt.BorderLayout());
                        frameHist.add(barraTitulo, java.awt.BorderLayout.NORTH);

                        // Configura a JTable acoplada ao modelo processado sem colunas fantasmas
                        javax.swing.JTable tableHist = new javax.swing.JTable(modeloNovo);
                        tableHist.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
                        tableHist.setBackground(grafiteClaro);
                        tableHist.setForeground(brancoPuro);
                        tableHist.setGridColor(cinzaLinhas);
                        tableHist.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                        tableHist.setRowHeight(24);
                        tableHist.setSelectionBackground(cinzaLinhas);
                        tableHist.setSelectionForeground(brancoPuro);

                        for (int col = 0; col < tableHist.getColumnCount(); col++){
                             tableHist.getColumnModel().getColumn(col).setPreferredWidth(130);
                        }

                        javax.swing.table.JTableHeader cabecalho = tableHist.getTableHeader();

                        cabecalho.setOpaque(true);
                        cabecalho.setBackground(cinzaClaroHeader);
                        cabecalho.setForeground(cinzaEscuroTexto);
                        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

                        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(tableHist);
                        scrollPane.setBackground(grafiteProfundo);
                        scrollPane.getViewport().setBackground(grafiteProfundo);
                        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        frameHist.add(scrollPane, java.awt.BorderLayout.CENTER);
                        frameHist.setLocationRelativeTo(null);
                        frameHist.setVisible(true); // Abre a listagem instantaneamente!
                    });
                } else {
                    java.awt.EventQueue.invokeLater(() -> {
                        util.MensagemSistema.mostrarAvisoDark(null, "CLIENTE NÃO POSSUI HISTÓRICO DE TROCAS!");
                    });
                }
                System.out.println("Busca do Histórico de Créditos finalizada na Nuvem.");
            } catch (Exception ex) {
                System.err.println("Erro crítico no Histórico de Trocas Cloud: " + ex.getMessage());                    
            }
        }).start();
    }       
}
