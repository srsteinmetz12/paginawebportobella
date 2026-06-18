package dao;

import connection.ConnectionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.ContasFixas;
import views.TelaFornecedor;

public class ContasFixasDAO {
    
    private PreparedStatement stmt2 = null;
    private PreparedStatement stmt = null;
    private Connection con2 = null;
    private Connection con = null;
    private ResultSet rs = null;
    private String sql;

    // =========================================================================
    // 1. MÉTODO PARA LISTAR AS CONTAS DIRETO DA AIVEN CLOUD
    // =========================================================================
    public List<ContasFixas> listarContas() throws ClassNotFoundException, SQLException {
        List<ContasFixas> lista = new ArrayList<>();
        con = ConnectionDB.getConnection();
        
        sql = "SELECT id, credor, mescomp, descricao, valor, vencimento, pago, data_pagamento FROM contas_fixas ORDER BY mescomp DESC, vencimento ASC";
        System.out.println("Buscando contas fixas locais: " + sql);
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            rs = stmt.executeQuery(); 
            
            while (rs.next()) {
                ContasFixas cf = new ContasFixas();
                cf.setId(rs.getInt("id"));
                cf.setCredor(rs.getString("credor"));                 
                cf.setMesComp(rs.getString("mescomp"));       
                cf.setDescricao(rs.getString("descricao"));
                cf.setValor(rs.getBigDecimal("valor"));
                cf.setVencimento(rs.getInt("vencimento"));
                cf.setPago(rs.getBoolean("pago"));
                cf.setDataPagamento(rs.getDate("data_pagamento"));
                lista.add(cf);
            }
            System.out.println("Acessou o banco Local com sucesso! Registros carregados: " + lista.size());
            System.out.println("----------------------------------");
            
        } catch (SQLException ex) {
            System.err.println("Erro ao listar contas fixas locais: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con != null) con.close();
                System.out.println("Fim da busca de contas!");
                System.out.println("Conexão encerrada com o banco Local!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                // Mantém o padrão de log que você utiliza no catch do finally
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lista;
    }

    public List<ContasFixas> listarContasCloud() throws ClassNotFoundException, SQLException {
        List<ContasFixas> lista = new ArrayList<>();
        con2 = ConnectionDB.getConnectionCloud();
        
        sql = "SELECT id, credor, mescomp, descricao, valor, vencimento, pago, data_pagamento FROM contas_fixas ORDER BY mescomp DESC, vencimento ASC";
        System.out.println("Buscando contas fixas: " + sql);
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            rs = stmt2.executeQuery(); 
            
            while (rs.next()) {
                ContasFixas cf = new ContasFixas();
                cf.setId(rs.getInt("id"));
                cf.setCredor(rs.getString("credor"));                 
                cf.setMesComp(rs.getString("mescomp"));       
                cf.setDescricao(rs.getString("descricao"));
                cf.setValor(rs.getBigDecimal("valor"));
                cf.setVencimento(rs.getInt("vencimento"));
                cf.setPago(rs.getBoolean("pago"));
                cf.setDataPagamento(rs.getDate("data_pagamento"));
                lista.add(cf);
            }
            System.out.println("Acessou a Cloud com sucesso! Registros carregados: " + lista.size());
            System.out.println("----------------------------------");
            
        } catch (SQLException ex) {
            System.err.println("Erro ao listar contas fixas da Cloud: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con2 != null) con2.close();
                System.out.println("Fim da busca de contas!");
                System.out.println("Conexão encerrada com a Cloud!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lista;
    }

    // =========================================================================
    // 2. O MÉTODO DO CHECKBOX: ATUALIZA O STATUS DE PAGAMENTO NA CLOUD
    // =========================================================================
    public void atualizarStatusPagamentoPorOcorrencia(String ocorrencia, boolean statusPago) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        
        sql = "UPDATE contas_fixas SET pago = ?, data_pagamento = ? WHERE descricao = ?";
        System.out.println("Atualizando status de pagamento local: " + sql);
        
        java.sql.Date dataAtualLocal = statusPago ? new java.sql.Date(System.currentTimeMillis()) : null;
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            stmt.setBoolean(1, statusPago);
            stmt.setDate(2, dataAtualLocal);
            stmt.setString(3, ocorrencia.trim());
            
            int linhasAfetadas = stmt.executeUpdate(); 
            System.out.println("Persistência concluída no banco Local com sucesso! Linhas alteradas: " + linhasAfetadas);
            System.out.println("----------------------------------");
            
        } catch (SQLException ex) {
            System.err.println("Erro ao salvar status de pagamento local: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (con != null) con.close();
                System.out.println("Fim do processo de atualização!");
                System.out.println("Conexão encerrada com o banco Local!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void atualizarStatusPagamentoPorOcorrenciaCloud(String ocorrencia, boolean statusPago) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        
        sql = "UPDATE contas_fixas SET pago = ?, data_pagamento = ? WHERE descricao = ?";
        System.out.println("Atualizando status de pagamento: " + sql);
        
        java.sql.Date dataAtualCloud = statusPago ? new java.sql.Date(System.currentTimeMillis()) : null;
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            
            stmt2.setBoolean(1, statusPago);
            stmt2.setDate(2, dataAtualCloud);
            stmt2.setString(3, ocorrencia.trim());
            
            int linhasAfetadas = stmt2.executeUpdate(); 
            System.out.println("Persistência concluída na Cloud com sucesso! Linhas alteradas: " + linhasAfetadas);
            System.out.println("----------------------------------");
            
        } catch (SQLException ex) {
            System.err.println("Erro ao salvar status de pagamento na Cloud: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (con2 != null) con2.close();
                System.out.println("Fim do processo de atualização!");
                System.out.println("Conexão encerrada com a Cloud!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // =========================================================================
    // 3. FUNÇÃO INCLUIR: INSERIR NOVA CONTA FIXA NA CLOUD
    // =========================================================================
    public void salvarNovaConta(ContasFixas cf) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "INSERT INTO contas_fixas (credor, mescomp, descricao, valor, vencimento, pago) VALUES (?, ?, ?, ?, ?, FALSE)";
        System.out.println("Inserindo conta fixa local: " + sql);
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            stmt.setString(1, cf.getCredor().trim());
            stmt.setString(2, cf.getMesComp()); 
            stmt.setString(3, cf.getDescricao().trim());
            stmt.setBigDecimal(4, cf.getValor());
            stmt.setInt(5, cf.getVencimento());
            
            stmt.executeUpdate();
            System.out.println("Nova conta cadastrada com sucesso no banco Local!");
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    public void salvarNovaContaCloud(ContasFixas cf) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "INSERT INTO contas_fixas (credor, mescomp, descricao, valor, vencimento, pago) VALUES (?, ?, ?, ?, ?, FALSE)";
        System.out.println("Inserindo conta fixa: " + sql);
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            
            stmt2.setString(1, cf.getCredor().trim());
            stmt2.setString(2, cf.getMesComp()); 
            stmt2.setString(3, cf.getDescricao().trim());
            stmt2.setBigDecimal(4, cf.getValor());
            stmt2.setInt(5, cf.getVencimento());
            
            stmt2.executeUpdate();
            System.out.println("Nova conta cadastrada com sucesso na Aiven Cloud!");
        } finally {
            if (con2 != null) {
                con2.close();
            }
        }
    }
    
    // =========================================================================
    // 4. FUNÇÃO EDITAR: ATUALIZAR CONTA EXISTENTE NA CLOUD
    // =========================================================================
    public void editarConta(ContasFixas cf) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "UPDATE contas_fixas SET credor = ?, mescomp = ?, valor = ?, vencimento = ? WHERE descricao = ?";
        System.out.println("Editando conta fixa local: " + sql);
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            stmt.setString(1, cf.getCredor().trim());
            stmt.setString(2, cf.getMesComp());
            stmt.setBigDecimal(3, cf.getValor());
            stmt.setInt(4, cf.getVencimento());
            stmt.setString(5, cf.getDescricao().trim());
            
            stmt.executeUpdate();
            System.out.println("Conta atualizada com sucesso no banco Local!");
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    public void editarContaCloud(ContasFixas cf) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE contas_fixas SET credor = ?, mescomp = ?, valor = ?, vencimento = ? WHERE descricao = ?";
        System.out.println("Editando conta fixa: " + sql);
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            
            stmt2.setString(1, cf.getCredor().trim());
            stmt2.setString(2, cf.getMesComp());
            stmt2.setBigDecimal(3, cf.getValor());
            stmt2.setInt(4, cf.getVencimento());
            stmt2.setString(5, cf.getDescricao().trim());
            
            stmt2.executeUpdate();
            System.out.println("Conta updated com sucesso na Aiven Cloud!");
        } finally {
            if (con2 != null) {
                con2.close();
            }
        }
    }
    
    // =========================================================================
    // 5. FUNÇÃO EXCLUIR: REMOVER CONTA FIXA DA CLOUD
    // =========================================================================
    public void excluirConta(String descricao) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "DELETE FROM contas_fixas WHERE descricao = ?";
        System.out.println("Excluindo conta fixa local: " + sql);
        
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt; // Sincroniza com o atributo global da classe
            
            stmt.setString(1, descricao.trim());
            stmt.executeUpdate();
            System.out.println("Conta removida com sucesso no banco Local!");
        } finally {
            if (con != null) {
                con.close();
            }
            System.out.println("Processo de exclusão encerrado.");
            System.out.println("----------------------------------");
        }
    }

    public void excluirContaCloud(String descricao) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "DELETE FROM contas_fixas WHERE descricao = ?";
        System.out.println("Excluindo conta fixa: " + sql);
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2; // Sincroniza com o atributo global da nuvem
            
            stmt2.setString(1, descricao.trim());
            stmt2.executeUpdate();
            System.out.println("Conta removida com sucesso da Aiven Cloud!");
        } finally {
            if (con2 != null) {
                con2.close();
            }
            System.out.println("Processo de exclusão encerrado.");
            System.out.println("----------------------------------");
        }
    }
}
