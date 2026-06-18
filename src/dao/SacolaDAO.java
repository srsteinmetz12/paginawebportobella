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
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import models.Sacola;
import util.MensagemSistema;
import views.TelaEntregas;
import views.TelaFornecedor;
import views.TelaSacola;

public class SacolaDAO {
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    Connection con;
    Connection con2;
    String sql;
    JFrame frame;
    JTable table;
    DefaultTableModel model;
    ResultSet rs;
    ResultSetMetaData metaData;
    StringBuilder todosNomes;
    
    public void saveSacola(Sacola s) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();       
        sql = "INSERT INTO sacola (id, datavenda, valorvenda, codpecas, nomecli, status) VALUES (?, ?, ?, ?, ?, ?)";
        System.out.println("Pesquisa: " + sql);        
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, s.getVendaId());
            stmt.setDate(2, new java.sql.Date(s.getDataCompra().getTime()));
            stmt.setString(3, s.getValorCompra());
            stmt.setString(4, s.getCodigoPeca());
            stmt.setString(5, s.getNomeCliente());           
            stmt.setString(6, s.getStatus());
            int linhasInseridas = stmt.executeUpdate();           
            System.out.println("Acessou o banco de dados! Registros: " + linhasInseridas);
            System.out.println("----------------------------------");
        } catch(SQLException ex) {
            System.err.println("Erro ao inserir dados: " + ex.toString()); 
            System.err.println("Erro ao cadastrar sacola na Cloud!");
            System.out.println("----------------------------------");          
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();
                System.out.println("Conexão encerrada na Cloud!");
                System.out.println("Fim da inclusão na Cloud!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao encerrar canais da Cloud: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void saveSacolaCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();       
        sql = "INSERT INTO sacola (id, datavenda, valorvenda, codpecas, nomecli, status) VALUES (?, ?, ?, ?, ?, ?)";
        System.out.println("Pesquisa: " + sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setInt(1, s.getVendaId());
            stmt2.setDate(2, new java.sql.Date(s.getDataCompra().getTime()));
            stmt2.setString(3, s.getValorCompra());
            stmt2.setString(4, s.getCodigoPeca());
            stmt2.setString(5, s.getNomeCliente());           
            stmt2.setString(6, s.getStatus());
            int linhasInseridas = stmt2.executeUpdate();           
            System.out.println("Acessou o banco de dados na Cloud! Registros: " + linhasInseridas);
            System.out.println("----------------------------------");
        } catch(SQLException ex) {
            System.err.println("Erro ao inserir dados na Cloud: " + ex.toString()); 
            System.err.println("Erro ao cadastrar sacola na Cloud!");
            System.out.println("----------------------------------");          
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada na Cloud!");
                System.out.println("Fim da inclusão na Cloud!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao encerrar canais da Cloud: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void consultaSacolasPendentes(Sacola s) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        stmt = null;       
        // Create JFrame
        frame = new JFrame("Sacola Pendentes");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 400);       
         // Create JTable
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model);             
        sql = "SELECT * FROM sacola WHERE status='EM_SEPARACAO' ORDER BY ID ASC";
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();           
            // Get metadata to create columns
            ResultSetMetaData metaData = rs.getMetaData();
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
            System.out.println("Numero de linhas do banco: "+dbRows);
            frame.add(new JScrollPane(table));
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);                   
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void consultaSacolasPendentesCloud(Sacola s) throws ClassNotFoundException, SQLException {
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
            java.net.URL urlLogo = getClass().getResource("/views/logo_icone.png"); 
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível carregar o logo na barra: " + ex.getMessage());
        }
        painelEsquerdo.add(lblLogo);

        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Sacolas Pendentes");
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

        // ─── CABEÇALHO EM TOM CINZA CLARO COM LETRAS ESCURAS ───
        javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
        cabecalho.setOpaque(true);
        cabecalho.setBackground(cinzaClaroHeader); 
        cabecalho.setForeground(cinzaEscuroTexto); 
        cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));
              
        sql = "SELECT * FROM sacola WHERE status='EM_SEPARACAO' ORDER BY ID ASC";
        System.out.println(sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();           
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           

            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i).toUpperCase());
            }
            java.text.SimpleDateFormat formatoEntrada = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat formatoBrasil = new java.text.SimpleDateFormat("dd/MM/yyyy");
            boolean possuiDados = false;
             while (rs.next()) {
                possuiDados = true;
                Object[] row = new Object[columnCount];

                for (int i = 1; i <= columnCount; i++) {
                    Object valorOriginal = rs.getObject(i);
                    String nomeColuna = metaData.getColumnName(i).toLowerCase();

                    // 🇧🇷 Identifica colunas de data (ex: datavenda, dataentrega, data)
                    if (valorOriginal != null && (nomeColuna.contains("data") || valorOriginal instanceof java.sql.Date)) {
                        try {
                            // Tenta converter o formato internacional para o padrão brasileiro
                            java.util.Date dataConvertida = formatoEntrada.parse(valorOriginal.toString());
                            row[i - 1] = formatoBrasil.format(dataConvertida);
                        } catch (ParseException ex) {
                            // Caso falhe ou a data já esteja em outro formato, mantém o valor original para não quebrar
                            row[i - 1] = valorOriginal;
                        }
                    } else {
                        // Se não for uma coluna de data, mantém o dado original (Texto, ID, Valor)
                        row[i - 1] = valorOriginal;
                    }
                }             
                model.addRow(row);
            }

            dbRows = model.getRowCount();
            System.out.println("Numero de linhas do banco: " + dbRows);

            if (possuiDados) {
                // Reaplica as propriedades do modelo e as cores escuras nas linhas
                table.setModel(model);
                table.setBackground(grafiteClaro);
                table.setForeground(brancoPuro);
                table.setGridColor(cinzaLinhas);
                table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                table.setRowHeight(24);
                table.setSelectionBackground(cinzaLinhas);
                table.setSelectionForeground(brancoPuro);

                // Reaplica o cabeçalho personalizado
//                javax.swing.table.JTableHeader cabecalho = table.getTableHeader();
                cabecalho.setOpaque(true);
                cabecalho.setBackground(cinzaClaroHeader); 
                cabecalho.setForeground(cinzaEscuroTexto); 
                cabecalho.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                cabecalho.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

                // Salva de forma segura o ColumnModel após a tabela estar totalmente montada 🛠️
                tabela = table.getColumnModel();

                // Customiza o painel de scroll para sumir com o fundo cinza claro vazio de baixo
                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setBackground(grafiteProfundo);
                scrollPane.getViewport().setBackground(grafiteProfundo);
                scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

                frame.add(scrollPane, java.awt.BorderLayout.CENTER);
                frame.setLocationRelativeTo(null);                   
                frame.setVisible(true);
                System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            } else {
                // Se não houver sacolas pendentes, avisa o operador usando seu aviso escuro blindado!
                MensagemSistema.mostrarAvisoDark(null, "Nenhuma sacola pendente em separação no momento.");
            }
            System.out.println("----------------------------------");
        } catch(SQLException ex) {
            System.err.println("Erro SQL: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexões da Cloud totalmente encerradas.");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar canais: " + ex.getMessage());
            }
        }
    }
    
    public void consultaClienteNome(Sacola s) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model);               
        sql = "SELECT * FROM sacola WHERE nomecli = ? AND status = 'EM_SEPARACAO' ORDER BY ID DESC;";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, TelaSacola.nomeCliente); 
            rs = stmt.executeQuery();          
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
                    s.setNomeCliente(rs.getString(6));
                    s.setDataCompra(rs.getDate(2));
                    s.setValorCompra(rs.getString(3));
                    s.setCodigoPeca(rs.getString(5));
                    s.setVendaId(rs.getInt(1));
                    s.setStatus(rs.getString(4));
                }             
                model.addRow(row);
            }           
            tabela = table.getColumnModel();
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: "+dbRows);
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
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
    
    public void consultaClienteNomeCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model);
        sql = "SELECT * FROM sacola WHERE nomecli = ? AND status = 'EM_SEPARACAO' ORDER BY ID DESC";
        System.out.println(sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaSacola.nomeCliente);          
            rs = stmt2.executeQuery();                    
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
                    s.setNomeCliente(rs.getString(6));
                    s.setDataCompra(rs.getDate(2));
                    s.setValorCompra(rs.getString(3));
                    s.setCodigoPeca(rs.getString(5));
                    s.setVendaId(rs.getInt(1));
                    s.setStatus(rs.getString(4));
                }             
                model.addRow(row);
            }                      
            tabela = table.getColumnModel();
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: " + dbRows);
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
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
    
    public java.util.List<Sacola> consultaClienteNomeCloud(String nomePesquisado) throws ClassNotFoundException, SQLException {
    con2 = ConnectionDB.getConnectionCloud();
    java.util.List<Sacola> listaSacolas = new java.util.ArrayList<>();
    
    // SQL limpo e focado
    sql = "SELECT id, datavenda, valorvenda, status, codpecas, nomecli FROM sacola WHERE nomecli = ? AND status = 'EM_SEPARACAO' ORDER BY id DESC";
    System.out.println(sql);       
    
    try {
        stmt2 = con2.prepareStatement(sql);
        stmt2.setString(1, nomePesquisado);          
        rs = stmt2.executeQuery();                    
        
        while (rs.next()) {
            // Cria uma nova instância de objeto para cada linha encontrada no banco
            Sacola item = new Sacola();
            item.setVendaId(rs.getInt(1));
            item.setDataCompra(rs.getDate(2));
            item.setValorCompra(rs.getString(3));
            item.setStatus(rs.getString(4));
            item.setCodigoPeca(rs.getString(5));
            item.setNomeCliente(rs.getString(6));
            
            // Adiciona o item capturado na lista de retorno
            listaSacolas.add(item);
        }                      
        
        System.out.println("Número de linhas localizadas no banco: " + listaSacolas.size());
        System.out.println("Acessou o banco de dados na Cloud com sucesso!");
        System.out.println("----------------------------------");
        return listaSacolas; // Retorna a lista cheia com todas as peças do cliente
        
    } catch (SQLException ex) {
        System.err.println("Erro na busca: " + ex.getMessage());
        System.out.println("----------------------------------");
        throw ex;
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt2 != null) stmt2.close();
            if (con2 != null) con2.close();
            System.out.println("Conexão com a nuvem encerrada de forma segura!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro ao fechar conexão: " + ex.getMessage());
        }
    }
}
    
    public void consultaSacola(Sacola s) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM sacola where status='EM_SEPARACAO' ORDER BY ID DESC";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                s.setNomeCliente(rs.getString(6));
                s.setDataCompra(rs.getDate(2));
                s.setValorCompra(rs.getString(3));
                s.setCodigoPeca(rs.getString(5));
                s.setVendaId(rs.getInt(1));
                s.setStatus(rs.getString(4));               
            }                       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void consultaSacolaCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM sacola where status='EM_SEPARACAO' ORDER BY ID DESC";
        System.out.println(sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();          
            while (rs.next()) {
                s.setNomeCliente(rs.getString(6));
                s.setDataCompra(rs.getDate(2));
                s.setValorCompra(rs.getString(3));
                s.setCodigoPeca(rs.getString(5));
                s.setVendaId(rs.getInt(1));
                s.setStatus(rs.getString(4));               
            }                       
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    public void buscaUltimoId(Sacola s) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM vendas WHERE id = ? AND entrega = 'Entrega Endereco' AND status = 'EM_SEPARACAO'";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, TelaSacola.id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                s.setNomeCliente(rs.getString(7));
                s.setDataCompra(rs.getDate(2));
                s.setValorCompra(rs.getString(5));
                s.setCodigoPeca(rs.getString(6));
                s.setVendaId(rs.getInt(1));
                s.setStatus(rs.getString(10));                
            }                       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
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
    
    public void buscaUltimoIdCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM vendas WHERE id = ? AND entrega = 'Entrega Endereco' AND status = 'EM_SEPARACAO'";
        System.out.println(sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setInt(1, TelaSacola.id);
            rs = stmt2.executeQuery();           
            while (rs.next()) {
                s.setNomeCliente(rs.getString(7));
                s.setDataCompra(rs.getDate(2));
                s.setValorCompra(rs.getString(5));
                s.setCodigoPeca(rs.getString(6));
                s.setVendaId(rs.getInt(1));
                s.setStatus(rs.getString(10));                
            }                       
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: " + ex.getMessage());
                System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
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
    
    public void buscaNomeClienteSacola(Sacola s) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT nomecli FROM sacola WHERE nomecli LIKE ? AND status='EM_SEPARACAO' ORDER BY nomecli LIMIT 15";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, TelaSacola.nomeCliente + "%");
            rs = stmt.executeQuery();
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
                s.setNomeCliente(todosNomes.toString());
                System.out.println("Encontrados " + (todosNomes.toString().split(";").length) + " clientes");
            } else {
                s.setNomeCliente("");
                System.out.println("Nenhum cliente encontrado!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            s.setNomeCliente("");
            System.out.println("----------------------------------");
        } finally {
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }
    
    public void buscaNomeClienteSacolaCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();

        // CORREÇÃO CRÍTICA: Adicionado o DISTINCT para o MySQL nunca trazer nomes repetidos
        sql = "SELECT DISTINCT nomecli FROM sacola WHERE nomecli LIKE ? AND status='EM_SEPARACAO' ORDER BY nomecli LIMIT 15";
    //    sql = "SELECT DISTINCT nomecli FROM sacola WHERE status='EM_SEPARACAO' ORDER BY nomecli LIMIT 15";
        System.out.println("Query de Cache de Sacolas: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaSacola.nomeCliente + "%");
            rs = stmt2.executeQuery();
            todosNomes = new StringBuilder();
            boolean temResultados = false;
            while (rs.next()) {
                String nome = rs.getString("nomecli");
                if (nome != null && !nome.trim().isEmpty()) {
                    if (temResultados) {
                        todosNomes.append(";");
                    }
                    todosNomes.append(nome.trim());
                    temResultados = true;
                }
            }
            if (temResultados) {
                s.setNomeCliente(todosNomes.toString());
                System.out.println("Encontrados " + (todosNomes.toString().split(";").length) + " clientes únicos para o cache.");
            } else {
                s.setNomeCliente("");
                System.out.println("Nenhum cliente encontrado!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro ao gerar cache de nomes de sacolas: " + ex.getMessage());
            s.setNomeCliente("");
            System.out.println("----------------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com a Cloud encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }
        
    public void carregaSacolaTresMesesMais(Sacola s) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        // Create JFrame
        frame = new JFrame("Itens com mais de 3 Meses");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 400);      
         // Create JTable
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model);              
        sql = "SELECT * FROM sacola WHERE datavenda < DATE_SUB(CURDATE(), INTERVAL 3 MONTH)AND status IS NULL";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
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
            System.out.println("Sacolas Mais de 3 Meses: "+tabela);
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: "+table.getRowCount());
            if(dbRows != 0){
                frame.add(new JScrollPane(table));
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }else{
                MensagemSistema.mostrarAvisoDark(null, "Nenhum dado existente!");
            }            
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void atualizaStatusDisponivelTodos(Sacola s) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "UPDATE SACOLA t1\n" +
              "JOIN ENTREGAS t2 ON t1.status = t2.status\n" +
              "JOIN VENDAS t3 ON t1.status = t3.status\n" +
              "SET t1.status = 'DISPONIVEL',\n" +
              "    t2.status = 'DISPONIVEL',\n" +
              "    t3.status = 'DISPONIVEL'\n" +
              "WHERE t1.id = ?";
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, TelaSacola.id);
            stmt.executeUpdate();       
            System.out.println("Atualizou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.out.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }  
    }
    
    public void atualizaStatusDisponivelTodosCloud(Sacola s) throws ClassNotFoundException, SQLException {        
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE SACOLA t1\n" +
              "JOIN ENTREGAS t2 ON t1.status = t2.status\n" +
              "JOIN VENDAS t3 ON t1.status = t3.status\n" +
              "SET t1.status = 'DISPONIVEL',\n" +
              "    t2.status = 'DISPONIVEL',\n" +
              "    t3.status = 'DISPONIVEL'\n" +
              "WHERE t1.id = ?";
              
        System.out.println(sql);       
        try {
            stmt2 = con2.prepareStatement(sql);                             
            stmt2.setInt(1, TelaSacola.id);           
            int linhasAfetadas = stmt2.executeUpdate();                 
            System.out.println("Atualizou o banco de dados na Cloud com sucesso! Linhas alteradas: " + linhasAfetadas);
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }  
    }
    
    public void atualizaStatusEntregueTabelaSacola(Sacola s) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "UPDATE sacola SET status= 'ENTREGUE' WHERE id= '"+TelaEntregas.idVenda+"'";       
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);                             
            stmt.setInt(1, TelaEntregas.idVenda);                 
            int linhasAfetadas = stmt.executeUpdate();       
            System.out.println("Atualizou o banco de dados com sucesso! Linhas alteradas: " + linhasAfetadas);
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void atualizaStatusEntregueTabelaSacolaCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE sacola SET status = 'ENTREGUE' WHERE id = ?";       
        System.out.println(sql);             
        try {
            stmt2 = con2.prepareStatement(sql);                             
            stmt2.setInt(1, TelaEntregas.idVenda);                 
            int linhasAfetadas = stmt2.executeUpdate();                  
            System.out.println("Atualizou o banco de dados com sucesso! Linhas alteradas: " + linhasAfetadas);
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void atualizaStatusDisponivelTabelaSacola(Sacola s) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "UPDATE sacola SET status = 'DISPONIVEL' WHERE id = ?";      
        System.out.println(sql);     
        try {
            stmt = con.prepareStatement(sql); 
            stmt.setInt(1, TelaSacola.id);
            stmt.executeUpdate();  
            System.out.println("Atualizou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void atualizaStatusDisponivelTabelaSacolaCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE sacola SET status = 'DISPONIVEL' WHERE id = ?";
        System.out.println(sql);               
        try {
            stmt2 = con2.prepareStatement(sql);                             
            stmt2.setInt(1, TelaSacola.id);           
            int linhasAfetadas = stmt2.executeUpdate();           
            System.out.println("Atualizou o banco de dados na Cloud com sucesso! Linhas alteradas: " + linhasAfetadas);
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void atualizaStatusDisponivelTabelaVendas(Sacola s) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "UPDATE vendas SET status = 'DISPONIVEL' WHERE id = ?";       
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, TelaSacola.id);
            stmt.executeUpdate();       
            System.out.println("Atualizou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void atualizaStatusDisponivelTabelaVendasCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE vendas SET status = 'DISPONIVEL' WHERE id = ?";        
        System.out.println(sql);               
        try {
            stmt2 = con2.prepareStatement(sql);                             
            stmt2.setInt(1, TelaSacola.id);           
            int linhasAfetadas = stmt2.executeUpdate();                 
            System.out.println("Atualizou o banco de dados na Cloud com sucesso! Linhas alteradas: " + linhasAfetadas);
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void atualizaStatusDisponivelTabelaEntregas(Sacola s) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "UPDATE entregas SET status = 'DISPONIVEL' WHERE idvenda = ?";       
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, TelaSacola.id);
            stmt.executeUpdate();       
            System.out.println("Atualizou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void atualizaStatusDisponivelTabelaEntregasCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();      
        sql = "UPDATE entregas SET status = 'DISPONIVEL' WHERE idvenda = ?";        
        System.out.println(sql);              
        try {
            stmt2 = con2.prepareStatement(sql);                             
            stmt2.setInt(1, TelaSacola.id);           
            int linhasAfetadas = stmt2.executeUpdate();                  
            System.out.println("Atualizou o banco de dados na Cloud com sucesso! Linhas alteradas: " + linhasAfetadas);
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();               
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        } 
    }
    
    public void consultaNomeClienteDisponiveis(Sacola s) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();   
        // Create JFrame
        frame = new JFrame("Sacolas Disponíveis");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 180);       
         // Create JTable
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model);               
        sql = "SELECT * FROM sacola WHERE nomecli = ? AND status = 'DISPONIVEL' ORDER BY ID DESC";
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, TelaSacola.nomeClienteDisponiveis);
            rs = stmt.executeQuery();          
            // Get metadata to create columns
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();           
            // Add columns to table model
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }
             // Add rows to table model
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: "+table.getRowCount());
            if(dbRows != 0){
                frame.add(new JScrollPane(table));
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }else{
                MensagemSistema.mostrarAvisoDark(null, "Nenhum dado existente!");
            }           
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
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
    
    public void consultaNomeClienteDisponiveisCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();

        // --- 1. INICIALIZAÇÃO DA JANELA PRINCIPAL SEM DECORAÇÕES ---
        frame = new javax.swing.JFrame("Sacolas Disponíveis");
        frame.setUndecorated(true); // Remove as bordas nativas brancas do Windows
        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 240); // Ajustado para acomodar perfeitamente a barra customizada + grid
        frame.setLayout(new java.awt.BorderLayout());

        // Definição de cores do projeto (Garante compatibilidade com as variáveis da classe)
        java.awt.Color pretoCabecalho  = new java.awt.Color(20, 20, 20);
        java.awt.Color cinzaLinhas     = new java.awt.Color(51, 51, 51);
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255);
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);
        java.awt.Color grafiteFundoGrid = new java.awt.Color(35, 35, 35);
        java.awt.Color cinzaBordasGrid = new java.awt.Color(60, 60, 60);

        // --- 2. ─── BARRA DE TÍTULO CUSTOMIZADA DO PROJETO ─── ---
        javax.swing.JPanel barraTitulo = new javax.swing.JPanel(new java.awt.BorderLayout());
        barraTitulo.setBackground(pretoCabecalho);
        barraTitulo.setPreferredSize(new java.awt.Dimension(900, 35));
        barraTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        // Painel Esquerdo: Logo + Título
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);

        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
        try {
            java.net.URL urlLogo = getClass().getResource("/views/logo_icone.png"); 
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível carregar o logo na barra: " + ex.getMessage());
        }
        painelEsquerdo.add(lblLogo);

        javax.swing.JLabel lblTituloText = new javax.swing.JLabel("Sacolas Disponíveis");
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
        
        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                // Ativa a suavização de bordas (Anti-Aliasing) para o círculo ficar perfeito
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Pinta o fundo do círculo de acordo com a cor atual (pretoCabecalho ou Vermelho)
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                
                g2.dispose();
                super.paintComponent(g); // Desenha o texto "X" por cima do círculo
            }
        };
//        javax.swing.JButton btnFechar = new javax.swing.JButton(" X ");
        btnFechar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnFechar.setForeground(brancoPuro);
        btnFechar.setBackground(pretoCabecalho);
//        btnFechar.setPreferredSize(new java.awt.Dimension(24, 24)); // Força a largura e altura iguais para virar um círculo perfeito
        btnFechar.setFocusPainted(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setContentAreaFilled(false);
        btnFechar.setOpaque(true); 
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

        // Código para Arrastar a Janela Customizada
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

        frame.add(barraTitulo, java.awt.BorderLayout.NORTH);

        // --- 3. CONSTRUÇÃO E CARREGAMENTO DA TABELA DE DADOS ---
        table = new javax.swing.JTable();
        model = new javax.swing.table.DefaultTableModel();
        table.setModel(model);               

        // Query unificada e ajustada com os nomes reais das colunas da tabela sacola
        sql = "SELECT id AS 'ID', DATE_FORMAT(datavenda, '%d/%m/%Y') AS 'DATA COMPRA', valorvenda AS 'VALORVENDA', status AS 'STATUS', codpecas AS 'CODPECAS', nomecli AS 'NOMECLI' " +
              "FROM sacola " +
              "WHERE nomecli = ? AND status = 'DISPONIVEL' " +
              "ORDER BY id DESC";

        System.out.println(sql);               
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaSacola.nomeClienteDisponiveis);
            rs = stmt2.executeQuery();                     

            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();                       

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

            dbRows = model.getRowCount(); 
            System.out.println("Numero de linhas do banco: " + dbRows);            

            if (dbRows != 0) {
                // Destrava atualizações de renderização assíncronas do Java Swing
                table.getTableHeader().setUpdateTableInRealTime(true);

                // --- 4. RENDERIZADOR TRIDIMENSIONAL INDEPENDENTE DO CABEÇALHO ---
                javax.swing.table.TableCellRenderer renderizadorCabecalho = new javax.swing.table.TableCellRenderer() {
                    @Override
                    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
                        javax.swing.JPanel painelCelula = new javax.swing.JPanel(new java.awt.BorderLayout());
                        painelCelula.setBackground(new java.awt.Color(215, 217, 220)); 

                        String textoColuna = (v == null) ? "" : v.toString().trim().toUpperCase();
                        javax.swing.JLabel labelTexto = new javax.swing.JLabel(textoColuna);
                        labelTexto.setForeground(new java.awt.Color(30, 30, 30)); 
                        labelTexto.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                        labelTexto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

                        labelTexto.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
                        painelCelula.add(labelTexto, java.awt.BorderLayout.CENTER);

                        painelCelula.setBorder(javax.swing.BorderFactory.createBevelBorder(
                            javax.swing.border.BevelBorder.RAISED, 
                            new java.awt.Color(245, 245, 245), 
                            new java.awt.Color(160, 160, 160)  
                        ));

                        return painelCelula;
                    }
                };

                // Força o renderizador fixo em todas as colunas mapeadas por metadados
                table.getTableHeader().setDefaultRenderer(renderizadorCabecalho);
                table.getTableHeader().setReorderingAllowed(false);

                // Configurações do corpo do grid escuro
                table.setBackground(grafiteFundoGrid);
                table.setForeground(brancoPuro);
                table.setGridColor(cinzaBordasGrid);
                table.setRowHeight(22);
                table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));

                // Força o laço a cravar o renderizador nas colunas injetadas pelo driver MySQL
                for (int k = 0; k < table.getColumnCount(); k++) {
                    table.getColumnModel().getColumn(k).setHeaderRenderer(renderizadorCabecalho);
                }

                // Envelopamento do ScrollPane Dark Premium
                javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(table);
                scrollPane.getViewport().setBackground(grafiteProfundo);
                scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaBordasGrid, 1));

                // Injeta o grid no centro do frame (abaixo da barra customizada)
                frame.getContentPane().setBackground(grafiteProfundo);
                frame.add(scrollPane, java.awt.BorderLayout.CENTER);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            } else {
                MensagemSistema.mostrarAvisoDark(null, "Nenhum dado existente para este cliente!");
            }           
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: " + ex.getMessage());
                System.out.println("----------------------------------");
        } finally{
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Fim da busca! Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (Exception ex) {
                System.err.println("Conexão erro: " + ex.getMessage());
            }
        }
    }        


    public void sacolaFinalizadaParaTabelaSacolas(Sacola s) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnectionCloud();       
        sql = "SELECT * FROM sacola WHERE id = ? AND status = 'DISPONIVEL'";
        System.out.println(sql);        
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, TelaSacola.id);
            rs = stmt.executeQuery();          
            while (rs.next()) {
                s.setNomeCliente(rs.getString(6));
                s.setDataCompra(rs.getDate(2));
                s.setValorCompra(rs.getString(3));
                s.setCodigoPeca(rs.getString(5));
                s.setVendaId(rs.getInt(1));
                s.setStatus(rs.getString(4));               
            }                       
            System.out.println("Acessou o banco de dados na Cloud com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: " + ex.getMessage());
                System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
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
    
    public void sacolaFinalizadaParaTabelaSacolasCloud(Sacola s) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();       
        sql = "SELECT * FROM sacola WHERE id = ? AND status = 'DISPONIVEL'";
        System.out.println(sql);        
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setInt(1, TelaSacola.id);
            rs = stmt2.executeQuery();          
            while (rs.next()) {
                s.setNomeCliente(rs.getString(6));
                s.setDataCompra(rs.getDate(2));
                s.setValorCompra(rs.getString(3));
                s.setCodigoPeca(rs.getString(5));
                s.setVendaId(rs.getInt(1));
                s.setStatus(rs.getString(4));               
            }                       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.err.println("Erro: " + ex.getMessage());
                System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
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
}
