
package dao;

import connection.ConnectionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import models.Vendas;
import views.TelaEntregas;
import views.TelaFinanceiro;
import views.TelaFornecedor;
import views.TelaSacola;

public class VendasDAO {
    
    private final LocalDate dataDoDia = LocalDate.now();
    public static String DadosEntrega;
    public static Object tabela;
    public static int dbRows;
    public static String nomeCliente;
    public static String dataCompra;
    public static Double valorPago;
    public static String codPeca;
    public static int idVenda;
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    Connection con;
    Connection con2;
    String sql;
    ResultSet rs;
    DefaultTableModel tableModelEntregas;
    ResultSetMetaData metaData;
    StringJoiner joiner;
    DefaultTableModel model;
    JFrame frame;
    JTable table;
    
    public void saveVendas(Vendas v) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "INSERT INTO vendas(id, datavenda, origemvenda, tipopag, valorvenda, codpecas, nomecli, obsvendas, entrega, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println(sql);

        // ==========================================
        // 🔥 TRATAR VALOR DA VENDA
        // ==========================================
        String valorOriginal = v.getValorVenda();
        double valorDouble = 0.0;
        String valorTratado = "0.00";

        if (valorOriginal != null && !valorOriginal.trim().isEmpty()) {
            try {
                // Converte usando a classe utilitária
                valorDouble = util.ValorMonetarioUtil.converterParaDouble(valorOriginal);
                // Formata para o banco (com ponto)
                valorTratado = util.ValorMonetarioUtil.formatarParaBanco(valorDouble);
                System.out.println("💰 Valor original: " + valorOriginal);
                System.out.println("💰 Valor tratado: " + valorTratado);
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao converter valor: " + e.getMessage());
                valorTratado = "0.00";
            }
        }

        System.out.println("IDVenda: " + v.getIdVenda() + 
                           " | Data Venda: " + v.getDataVenda() + 
                           " | Origem Venda: " + v.getOrigemVenda() + 
                           " | Tipo Pago: " + v.getTipoPag() + 
                           " | Valor Venda: " + valorTratado +
                           " | Codigo Peça: " + v.getCodPecas() + 
                           " | Nome Cliente: " + v.getNomeCliente() + 
                           " | OBS: " + v.getObservacao() + 
                           " | Status: " + v.getStatus());

        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, v.getIdVenda());
            stmt.setDate(2, new java.sql.Date(v.getDataVenda().getTime()));
            stmt.setString(3, v.getOrigemVenda());
            stmt.setString(4, v.getTipoPag());
            stmt.setString(5, valorTratado); // 🔥 VALOR TRATADO
            stmt.setString(6, v.getCodPecas());
            stmt.setString(7, v.getNomeCliente());
            stmt.setString(8, v.getObservacao());
            stmt.setString(9, v.getEntrega());
            stmt.setString(10, v.getStatus());
            stmt.execute();

            System.out.println("✅ Venda salva com sucesso na base!");
            System.out.println("   ID: " + v.getIdVenda());
            System.out.println("   Valor: R$ " + valorTratado);
            System.out.println("----------------------------------");

        } catch (SQLException ex) {
            System.err.println("❌ Erro ao inserir dados: " + ex.toString());
            System.err.println("❌ Erro ao cadastrar venda na base!");
            System.err.println("----------------------------------");
            throw ex;
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
            if (con != null) {
                try { con.close(); } catch (SQLException e) {}
            }
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }
    }
    
    public void saveVendasCloud(Vendas v) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();

        // ==========================================
        // 🔥 SQL COM PEDIDO_ID
        // ==========================================
        sql = "INSERT INTO vendas(id, pedido_id, datavenda, origemvenda, tipopag, valorvenda, codpecas, nomecli, obsvendas, entrega, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println(sql);

        // ==========================================
        // 🔥 TRATAR PEDIDO_ID (PODE SER NULO OU VAZIO)
        // ==========================================
        String pedidoId = v.getPedidoId();
        String pedidoIdTratado = null;

        if (pedidoId != null && !pedidoId.trim().isEmpty() && !pedidoId.trim().equals("null")) {
            pedidoIdTratado = pedidoId.trim();
            System.out.println("📋 Pedido ID: " + pedidoIdTratado);
        } else {
            pedidoIdTratado = null;
            System.out.println("📋 Pedido ID: NULL (sem pedido do site)");
        }

        // ==========================================
        // 🔥 TRATAR VALOR DA VENDA
        // ==========================================
        String valorOriginal = v.getValorVenda();
        double valorDouble = 0.0;
        String valorTratado = "0.00";

        if (valorOriginal != null && !valorOriginal.trim().isEmpty()) {
            try {
                valorDouble = util.ValorMonetarioUtil.converterParaDouble(valorOriginal);
                valorTratado = util.ValorMonetarioUtil.formatarParaBanco(valorDouble);
                System.out.println("💰 Valor tratado: " + valorTratado);
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao converter valor: " + e.getMessage());
                valorTratado = "0.00";
            }
        }

        System.out.println("IDVenda: " + v.getIdVenda() + 
                           " | Pedido: " + pedidoIdTratado +
                           " | Data: " + v.getDataVenda() + 
                           " | Origem: " + v.getOrigemVenda() + 
                           " | Tipo: " + v.getTipoPag() + 
                           " | Valor: " + valorTratado +
                           " | Cliente: " + v.getNomeCliente());

        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setInt(1, v.getIdVenda());

            // ==========================================
            // 🔥 TRATA PEDIDO_ID (NULL OU VALOR)
            // ==========================================
            if (pedidoIdTratado != null) {
                stmt2.setString(2, pedidoIdTratado);
            } else {
                stmt2.setNull(2, java.sql.Types.VARCHAR);
            }

            stmt2.setDate(3, new java.sql.Date(v.getDataVenda().getTime()));
            stmt2.setString(4, v.getOrigemVenda());
            stmt2.setString(5, v.getTipoPag());
            stmt2.setString(6, valorTratado);
            stmt2.setString(7, v.getCodPecas());
            stmt2.setString(8, v.getNomeCliente());
            stmt2.setString(9, v.getObservacao());
            stmt2.setString(10, v.getEntrega());
            stmt2.setString(11, v.getStatus());
            stmt2.execute();

            System.out.println("✅ Venda salva com sucesso na Cloud!");
            System.out.println("   ID: " + v.getIdVenda());
            System.out.println("   Pedido: " + (pedidoIdTratado != null ? pedidoIdTratado : "N/A"));
            System.out.println("   Valor: R$ " + valorTratado);
            System.out.println("----------------------------------");

        } catch (SQLException ex) {
            System.err.println("❌ Erro ao inserir dados: " + ex.toString());
            System.err.println("----------------------------------");
            throw ex;
        } finally {
            if (stmt2 != null) {
                try { stmt2.close(); } catch (SQLException e) {}
            }
            if (con2 != null) {
                try { con2.close(); } catch (SQLException e) {}
            }
            System.out.println("Conexão Cloud encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }
    }
    
    public void saveSomenteFrete(Vendas v) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();      
        sql = "INSERT INTO vendas(id, datavenda, origemvenda, tipopag, valorvenda, codpecas, nomecli, obsvendas, entrega, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println(sql);
        System.out.println("IDVenda: "+idVenda+" Data Venda: "+dataCompra+" Origem Venda: "+TelaFinanceiro.origemVenda+" Tipo Pago: "+TelaFinanceiro.tipoPago+" Valor Venda: "+valorPago+" "
                + " Codigo Peça: "+codPeca+" Nome Cliente: "+nomeCliente+" OBS: "+TelaFinanceiro.obs +" Status: "+TelaFinanceiro.status+"" );
        try {
            stmt = con.prepareStatement(sql);//tabela
            stmt.setInt(1, v.getIdVenda());
            stmt.setDate(2, new java.sql.Date(v.getDataVenda().getTime()));
            stmt.setString(3, v.getOrigemVenda());
            stmt.setString(4, v.getTipoPag());
            stmt.setString(5, v.getValorVenda());
            stmt.setString(6, v.getCodPecas());
            stmt.setString(7, v.getNomeCliente());           
            stmt.setString(8, v.getObservacao());
            stmt.setString(9, v.getEntrega());
            stmt.setString(10, v.getStatus());            
            stmt.execute();
            System.out.println("Acessou o banco de dados!");
            System.out.println("----------------------------------");
        }catch(SQLException ex){
            System.out.println("Erro ao inserir dados: " +ex.toString()); 
            System.out.println("Erro ao cadastrar venda!");
            System.out.println("----------------------------------");           
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }
    }
    
    public  void saveVendasOnLine(Vendas v) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "INSERT INTO vendas(id, datavenda, origemvenda, tipopag, valorvenda, codpecas, nomecli, obsvendas, entrega, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            stmt.setInt(1, v.getIdVenda());
            stmt.setDate(2, new java.sql.Date(v.getDataVenda().getTime()));
            stmt.setString(3, v.getOrigemVenda());
            stmt.setString(4, v.getTipoPag());
            stmt.setString(5, v.getValorVenda());
            stmt.setString(6, v.getCodPecas());
            stmt.setString(7, v.getNomeCliente());           
            stmt.setString(8, v.getObservacao());
            stmt.setString(9, v.getEntrega());
            stmt.setString(10, v.getStatus());          
            stmt.execute();
            System.out.println("Acessou o banco de dados!");
            JOptionPane.showMessageDialog(null, "Registro salvo com sucesso!");
            System.out.println("----------------------------------");
        }catch(SQLException ex){
            System.out.println("Erro ao inserir dados: " +ex.toString()); 
            System.out.println("Erro ao cadastrar venda!");
            System.out.println("----------------------------------");           
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }
    }
    
    public void selectIdVenda(Vendas v) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();      
        sql = "SELECT MAX(id) FROM vendas";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            rs = stmt.executeQuery();
            if(rs.next()){
                v.setIdVenda(rs.getInt(1));               
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
                System.out.println("Acessou o banco de dados na Cloud com sucesso!");
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
    
    public void selectIdVendaCloud(Vendas v) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();      
        sql = "SELECT MAX(id) FROM vendas";
        System.out.println(sql);
        try {
            stmt2 = con2.prepareStatement(sql);//tabela
            rs = stmt2.executeQuery();
            if(rs.next()){
                v.setIdVenda(rs.getInt(1));               
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
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
            System.out.println("Erro ao buscar dados: " +ex.toString()); 
            System.out.println("Erro ao pesquisar id!");
            System.out.println("----------------------------------");
        }finally{
            con2.close(); 
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }
    }
    
    public void carregaTabelaValores(Vendas v) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM vendas ORDER BY datavenda="+dataDoDia+" DESC";
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);            
            while(rs.next()){               
                v.setIdVenda(rs.getInt(1));                
                v.setDataVenda(rs.getDate(2));
                v.setOrigemVenda(rs.getString(3));
                v.setTipoPag(rs.getString(4));
                v.setValorVenda(rs.getString(5));
                v.setCodPecas(rs.getString(6));
                v.setNomeCliente(rs.getString(7));                            
                v.setObservacao(rs.getString(8)); 
            }       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }         
    } 

    public void pesquisaEntregasRetireLoja(Vendas v) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM vendas where entrega='Retire Loja'";       
        System.out.println(sql);       
         try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);                      
            while(rs.next()){
                for (int i = 0; i < 15; i++){
                    v.setIdVenda(rs.getInt(1));                
                    v.setDataVenda(rs.getDate(2));
                    v.setNomeCliente(rs.getString(7));
                    v.setCodPecas(rs.getString(6));
                }                                                          
            }       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }  
    }

    public void pesquisaEntregasEndereco(Vendas v) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM vendas where entrega='Entrega Endereco'";
        System.out.println(sql);       
         try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);           
            tableModelEntregas = new DefaultTableModel();
            table = new JTable(tableModelEntregas);           
            metaData = rs.getMetaData();
            int numeroDeColunas = metaData.getColumnCount();
            joiner = new StringJoiner(", ", "[", "]");               
            while(rs.next()){           
                for (int i = 0; i < 15; i++){                 
                    v.setIdVenda(rs.getInt(1));                
                    v.setDataVenda(rs.getDate(2));
                    v.setNomeCliente(rs.getString(7));
                    v.setCodPecas(rs.getString(6));
               }
            }
            DadosEntrega = joiner.toString();
            System.out.println(joiner.toString());
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }  
    }
    
    public void atualizaStatusEntregueTabelaVendas(Vendas v) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "UPDATE vendas SET status= 'ENTREGUE' WHERE id= '"+TelaEntregas.idVenda+"'";      
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
    
    public void atualizaStatusEntregueTabelaVendasCloud(Vendas v) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE vendas SET status= 'ENTREGUE' WHERE id= '"+TelaEntregas.idVenda+"'";      
        System.out.println(sql);       
        try {
            stmt2 = con2.prepareStatement(sql);                             
            stmt2.executeUpdate();       
            System.out.println("Atualizou o banco de dados com sucesso!");
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

    public void atualizaBaseRetireLoja(Vendas v) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM vendas where entrega='Retire Loja' ORDER BY id ASC";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql); 
            while(rs.next()){           
                for (int i = 0; i < 15; i++){
                    v.setIdVenda(rs.getInt(1));                
                    v.setDataVenda(rs.getDate(2));
                    v.setNomeCliente(rs.getString(7));
                    v.setCodPecas(rs.getString(6));
                }                
            }
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }  
    }
    
    public void atualizaStatusDisponivelTabelaVendas(Vendas v) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        sql = "UPDATE vendas SET status='DISPONIVEL' WHERE id= "+TelaSacola.id+"";
        System.out.println(sql);       
         try {
            stmt = con.prepareStatement(sql);                              
            stmt.executeUpdate();       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
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

    public void consultaNomeCliente(Vendas v) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM vendas where nomecli='"+TelaSacola.nomeCliente+"' AND status='EM_SEPARACAO' ORDER BY ID ASC";
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            while(rs.next()){ 
                v.setNomeCliente(rs.getString(7));
                v.setDataVenda(rs.getDate(2));
                v.setIdVenda(rs.getInt(1));
                v.setCodPecas(rs.getString(6));
                v.setValorVenda(rs.getString(5));                         
            }           
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }  
    }
    public void atualizaStatusVendaLoja() throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        sql = "UPDATE vendas SET status='ENTREGUE' WHERE id= "+TelaSacola.id+"";
        System.out.println(sql);        
        try {
            stmt = con.prepareStatement(sql);                              
            stmt.executeUpdate();       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
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
    
    public void atualizaStatusVendaLojaCloud() throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE vendas SET status='ENTREGUE' WHERE id= "+TelaSacola.id+"";
        System.out.println(sql);        
        try {
            stmt2 = con2.prepareStatement(sql);                              
            stmt2.executeUpdate();       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualizacao!");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
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
    
    public void consultaNomeDoCliente(Vendas v) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        // Create JFrame
        frame = new JFrame("Sacola Cliente");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 180);       
         // Create JTable
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model);
        sql = "SELECT * FROM vendas where nomecli='"+TelaSacola.nomeCliente+"' AND status='EM_SEPARACAO' ORDER BY ID ASC";
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
             // Add rows to table model
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }
            dbRows = table.getRowCount();           
            frame.add(new JScrollPane(table));
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);               
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }       
    }
    
    public void consultaNomeClienteDisponiveis(Vendas v) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        // Create JFrame
        frame = new JFrame("Sacolas Disponíveis");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 180);       
         // Create JTable
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model);       
        sql = "SELECT * FROM vendas WHERE nomecli='"+TelaSacola.nomeClienteDisponiveis+"' AND status='DISPONIVEL' ORDER BY ID DESC";
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
             // Add rows to table model
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }
            table.getRowCount();
            frame.add(new JScrollPane(table));
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);                  
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void consultaSacolasPendentes(Vendas v) throws ClassNotFoundException {
        con = ConnectionDB.getConnection();
        // Create JFrame
        frame = new JFrame("Sacola Pendentes");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 400);       
         // Create JTable
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model); 
        sql = "SELECT * FROM vendas where entrega='Entrega Endereco' AND status='EM_SEPARACAO' ORDER BY ID ASC";
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
            System.out.println("consultaSacolasPendentes: "+tabela);
            dbRows = table.getRowCount();
            System.out.println("Numero de linhas do banco: "+table.getRowCount());
            frame.add(new JScrollPane(table));
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);             
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("----------------------------------");
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da busca!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void atualizarVisaoMediaDiariaCloud() {
        String sqlTabela = "SELECT datavenda, SUM(valorvenda) FROM vendas WHERE origemvenda != 'DESPESA' GROUP BY datavenda ORDER BY datavenda DESC";
        String sqlEstatistica = "SELECT COUNT(DISTINCT datavenda), SUM(valorvenda) FROM vendas WHERE origemvenda != 'DESPESA'";

        try (Connection con = ConnectionDB.getConnectionCloud();
             PreparedStatement stmt1 = con.prepareStatement(sqlTabela);
             ResultSet rs1 = stmt1.executeQuery();
             PreparedStatement stmt2 = con.prepareStatement(sqlEstatistica);
             ResultSet rs2 = stmt2.executeQuery()) {

            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");

            // 1. Alimenta a tabela visual de faturamento diário
            while (rs1.next()) {
                java.sql.Date data = rs1.getDate(1);
                double valor = rs1.getDouble(2);
            }
            // 2. Calcula e injeta os valores diretamente nos campos ou nos Cards
            if (rs2.next()) {
                int totalDias = rs2.getInt(1);
                double totalValor = rs2.getDouble(2);
                double mediaDiaria = (totalDias > 0) ? (totalValor / totalDias) : 0.0;
            }
            System.out.println("Visão de Média Diária atualizada com sucesso via Cloud.");

        } catch (Exception ex) {
            System.err.println("Erro ao carregar médias diárias: " + ex.getMessage());
        }
    }
}
    
