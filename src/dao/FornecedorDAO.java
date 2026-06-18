
package dao;

import connection.ConnectionDB;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import models.Fornecedor;
import views.TelaFornecedor;
import static views.TelaFornecedor.s;

public class FornecedorDAO{
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    Connection con;
    Connection con2;
    String sql;
    ResultSet rs;
    
    public void saveFornecedor(Fornecedor f) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();        
        sql = "INSERT INTO fornecedor(tipoforn, codforn, datacadastro, ultimolote, precomedio, nomeforn, cpfcnpj, cep, cidade, endereco, numero, complemento, bairro, email, telefone, siterede, obs) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";        
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, f.getTipoforn());
            stmt.setString(2, f.getCodforn());
            stmt.setDate(3, new java.sql.Date(f.getDataCadastramento().getTime()));
            stmt.setString(4, f.getUltimoLote());
            stmt.setDouble(5, f.getPrecoMedio());
            stmt.setString(6, f.getNomeforn());
            stmt.setString(7, f.getCpfCnpj());
            stmt.setString(8, f.getCep());
            stmt.setString(9, f.getCidade());
            stmt.setString(10, f.getEndereco());
            stmt.setString(11, f.getNumero());
            stmt.setString(12, f.getComplemento());
            stmt.setString(13, f.getBairro());
            stmt.setString(14, f.getEmail());
            stmt.setString(15, f.getTelefone());
            stmt.setString(16, f.getSiteRede());
            stmt.setString(17, f.getObservacao());            
            stmt.execute();           
            System.out.println("Acessou o banco de dados!");
            System.out.println("----------------------------------");
        }catch(SQLException ex){
            System.out.println("Erro ao inserir dados: " +ex.toString()); 
            System.out.println("Erro ao cadastrar fornecedor!");
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }           
    }
    
    public void saveFornecedorCloud(Fornecedor f) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();        
        sql = "INSERT INTO fornecedor(tipoforn, codforn, datacadastro, ultimolote, precomedio, nomeforn, cpfcnpj, cep, cidade, uf, endereco, numero, complemento, bairro, email, telefone, siterede, obs) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";        
        System.out.println("Pesquisa: " + sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, f.getTipoforn());
            stmt2.setString(2, f.getCodforn());
            stmt2.setDate(3, new java.sql.Date(f.getDataCadastramento().getTime()));
            stmt2.setString(4, f.getUltimoLote());
            stmt2.setDouble(5, f.getPrecoMedio());
            stmt2.setString(6, f.getNomeforn());
            stmt2.setString(7, f.getCpfCnpj());
            stmt2.setString(8, f.getCep());
            stmt2.setString(9, f.getCidade());
            stmt2.setString(10, f.getUF());
            stmt2.setString(11, f.getEndereco());
            stmt2.setString(12, f.getNumero());
            stmt2.setString(13, f.getComplemento());
            stmt2.setString(14, f.getBairro());
            stmt2.setString(15, f.getEmail());
            stmt2.setString(16, f.getTelefone());
            stmt2.setString(17, f.getSiteRede());
            stmt2.setString(18, f.getObservacao());            
            int linhasInseridas = stmt2.executeUpdate();           
            
            System.out.println("Acessou o banco de dados! Registros inseridos: " + linhasInseridas);
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            System.err.println("Erro ao inserir dados: " + ex.toString()); 
            System.err.println("Erro ao cadastrar fornecedor!");
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("Fim da inclusão!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de fornecedor: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }           
    }
    
    public void updateFornecedor(Fornecedor f) throws ClassNotFoundException, SQLException, ParseException{
        con = ConnectionDB.getConnection();  
        sql = "UPDATE fornecedor SET tipoforn=?, codforn=?, datacadastro=?, ultimolote=?, precomedio=?, nomeforn=?, cpfcnpj=?, cep=?, cidade=?, endereco=?, numero=?, complemento=?, bairro=?, email=?, telefone=?, siterede=?, obs=? WHERE codforn=?";
        try{
            stmt = con.prepareStatement(sql);
            stmt.setString(1, f.getTipoforn());
            stmt.setString(2, f.getCodforn());
            stmt.setDate(3, new java.sql.Date(f.getDataCadastramento().getTime()));
            stmt.setString(4, f.getUltimoLote());
            stmt.setDouble(5, f.getPrecoMedio());
            stmt.setString(6, f.getNomeforn());
            stmt.setString(7, f.getCpfCnpj());
            stmt.setString(8, f.getCep());
            stmt.setString(9, f.getCidade());
            stmt.setString(10, f.getUF());
            stmt.setString(11, f.getEndereco());
            stmt.setString(12, f.getNumero());
            stmt.setString(13, f.getComplemento());
            stmt.setString(14, f.getBairro());
            stmt.setString(15, f.getEmail());
            stmt.setString(16, f.getTelefone());
            stmt.setString(17, f.getSiteRede());
            stmt.setString(18, f.getObservacao());           
            stmt.execute();           
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");           
        }catch(SQLException ex){
            System.err.println("Erro: "+ex.getMessage());
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualização!");
            System.out.println("----------------------------------");
        }
    }
    
        public void updateFornecedorCloud(Fornecedor f) throws ClassNotFoundException, SQLException, ParseException {
        con2 = ConnectionDB.getConnectionCloud();  
        sql = "UPDATE fornecedor SET tipoforn=?, codforn=?, datacadastro=?, ultimolote=?, precomedio=?, nomeforn=?, cpfcnpj=?, cep=?, cidade=?, uf=?, endereco=?, numero=?, complemento=?, bairro=?, email=?, telefone=?, siterede=?, obs=? WHERE codforn=?";
        System.out.println("Pesquisa: " + sql);
        
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, f.getTipoforn());
            stmt2.setString(2, f.getCodforn());
            stmt2.setDate(3, new java.sql.Date(f.getDataCadastramento().getTime()));
            stmt2.setString(4, f.getUltimoLote());
            stmt2.setDouble(5, f.getPrecoMedio());
            stmt2.setString(6, f.getNomeforn());
            stmt2.setString(7, f.getCpfCnpj());
            stmt2.setString(8, f.getCep());
            stmt2.setString(9, f.getCidade());
            stmt2.setString(10, f.getUF());
            stmt2.setString(11, f.getEndereco());
            stmt2.setString(12, f.getNumero());
            stmt2.setString(13, f.getComplemento());
            stmt2.setString(14, f.getBairro());
            stmt2.setString(15, f.getEmail());
            stmt2.setString(16, f.getTelefone());
            stmt2.setString(17, f.getSiteRede());
            stmt2.setString(18, f.getObservacao());
            stmt2.setString(19, f.getCodforn());           
            int linhasAtualizadas = stmt2.executeUpdate();                      
            System.out.println("Acessou o banco de dados com sucesso! Registros alterados: " + linhasAtualizadas);
            System.out.println("----------------------------------");           
        } catch (SQLException ex) {
            System.err.println("Erro ao atualizar fornecedor na Cloud: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
                System.out.println("Fim da Atualização!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao encerrar conexões de fornecedor: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    
    public void deleteFornecedor(Fornecedor f) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();    
        sql = "DELETE from fornecedor WHERE codforn=?";      
        try{
            stmt = con.prepareStatement(sql);
            stmt.setString(1, f.getCodforn());
            stmt.executeUpdate();           
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");                      
        }catch(SQLException ex){
            System.out.println("Erro ao tentar remover o registro!");
            System.out.println("----------------------------------");
            System.out.println("Erro: "+ex.getMessage());
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("----------------------------------");
            System.out.println("Fim da exclusão!");
            System.out.println("----------------------------------");
        }       
    }
    
        public void deleteFornecedorCloud(Fornecedor f) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();    
        sql = "DELETE FROM fornecedor WHERE codforn = ?";      
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, f.getCodforn());
            int linhasAfetadas = stmt2.executeUpdate();                      
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("Fornecedores removidos na Cloud: " + linhasAfetadas);
            System.out.println("----------------------------------");                      
        } catch (SQLException ex) {
            System.err.println("Erro ao tentar remover o registro!");
            System.out.println("----------------------------------");
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
                System.out.println("Fim da exclusão!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de exclusão de fornecedor: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }       
    }

    
    public void selectFornecedor(Fornecedor f) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException{
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM fornecedor WHERE codforn ="+s+"";       
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                f.setTipoforn(rs.getString(1));
                f.setCodforn(rs.getString(2));               
                f.setDataCadastramento(rs.getDate(3));
                f.setUltimoLote(rs.getString(4));
                f.setPrecoMedio(rs.getDouble(5));
                f.setNomeforn(rs.getString(6));
                f.setCpfCnpj(rs.getString(7));
                f.setCep(rs.getString(8));
                f.setCidade(rs.getString(9));
                f.setEndereco(rs.getString(10));
                f.setNumero(rs.getString(11));
                f.setComplemento(rs.getString(12));
                f.setBairro(rs.getString(13));
                f.setEmail(rs.getString(14));
                f.setTelefone(rs.getString(15));
                f.setSiteRede(rs.getString(16));
                f.setObservacao(rs.getString(17));                
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                JOptionPane.showMessageDialog(null, "Fornecedor inválido ou não cadastrado!");
                System.out.println("Fornecedor não cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("----------------------------------");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("----------------------------------");
            }
        }  
    }
    
    public void selectFornecedorCloud(String s, Fornecedor f) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM fornecedor WHERE codforn = ?";       
        System.out.println("Pesquisa: " + sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, s);
            rs = stmt2.executeQuery();          
            if (rs.next()) {
                f.setTipoforn(rs.getString(1));
                f.setCodforn(rs.getString(2));               
                f.setDataCadastramento(rs.getDate(3));
                f.setUltimoLote(rs.getString(4));
                f.setPrecoMedio(rs.getDouble(5));
                f.setNomeforn(rs.getString(6));
                f.setCpfCnpj(rs.getString(7));
                f.setCep(rs.getString(8));
                f.setCidade(rs.getString(9));
                f.setUF(rs.getString(10));
                f.setEndereco(rs.getString(11));
                f.setNumero(rs.getString(12));
                f.setComplemento(rs.getString(13));
                f.setBairro(rs.getString(14));
                f.setEmail(rs.getString(15));
                f.setTelefone(rs.getString(16));
                f.setSiteRede(rs.getString(17));
                f.setObservacao(rs.getString(18));              
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            } else {
                JOptionPane.showMessageDialog(null, "Fornecedor inválido ou não cadastrado!");
                System.out.println("Fornecedor não cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("----------------------------------");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }  
    }

    public void selectCodFornecedor(Fornecedor f) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT MAX(codforn) FROM fornecedor";       
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                f.setCodforn(rs.getString(1));
                f.setUltimoLote(rs.getString(2));
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Fornecedor não cadastrado!");
                System.out.println("----------------------------------");
            }
        }catch(SQLException ex){
            System.err.println("Erro: "+ex.getMessage());
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }
    
        public void selectCodFornecedorCloud(Fornecedor f) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();        
        sql = "SELECT MAX(codforn), MAX(ultimolote) FROM fornecedor";       
        System.out.println("Pesquisa: " + sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            
            if (rs.next()) {
                f.setCodforn(rs.getString(1));
                f.setUltimoLote(rs.getString(2));
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Fornecedor não cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch (SQLException ex) {
            System.err.println("Erro na consulta do código máximo: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("Fim da Pesquisa!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de metadados: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
}
