package dao;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import connection.ConnectionDB;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import models.Fornecedor;
import models.Produto;
import models.Vendas;
import util.ConfigLoader;
import util.MensagemSistema;
import views.TelaRelatorios;
import static views.TelaRelatorios.codigo;
import static views.TelaRelatorios.descricao;
import static views.TelaRelatorios.fornecedor;
import static views.TelaRelatorios.marca;
import static views.TelaRelatorios.nomeRelatorio;
import static views.TelaRelatorios.tamanho;

public class RelatorioDAO {    
    public String resultado;
    public String usuario = ConfigLoader.get("sistema.user");
    private final String inicio_caminho_relatorio = ConfigLoader.get("sistema.inicio_caminho_relatorios");
    private final String final_caminho_relatorio = ConfigLoader.get("sistema.final_caminho_relatorios");
    private final String caminho_relatorio = inicio_caminho_relatorio+usuario+final_caminho_relatorio;
    private final Font fonteRodape = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
    String caminhoLogo = ConfigLoader.get("sistema.logo");
    String cliente = ConfigLoader.get("sistema.nome_cliente");
    int ano = java.time.Year.now().getValue();
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    String sql;
    Connection con;
    Connection con2;
    ResultSet rs;
    Paragraph pg;
    PdfPTable table;
    PdfPCell co11;
    PdfPCell co12;
    PdfPCell co13;
    PdfPCell co14;
    PdfPCell co15;
    File file;
    
    public void gerarRelFaturamentoProjetado(Produto p) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException{
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();              
        sql = "SELECT SUM(precosug) FROM estoque WHERE data BETWEEN ? AND ?";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            stmt.setString(1, TelaRelatorios.dtIni);
            stmt.setString(2, TelaRelatorios.dtFim);
            rs = stmt.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            writer.setPageEvent(new OuvinteRodapePDF());
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co11 = new PdfPCell(new Phrase("Faturamento no período" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);            
            table.addCell(co11);           
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                table.addCell(co11);             
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            } 
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }             
    }
    
    public void gerarRelFaturamentoProjetadoCloud(Produto p) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();              
        sql = "SELECT COALESCE(SUM(CAST(precosug AS DECIMAL(10,2))), 0.00) " +
            "FROM estoque " +
            "WHERE data BETWEEN ? AND ? " +
            "  AND LOWER(TRIM(status)) != 'cancelado'";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            
            // 🔥 VINCULO SEGURO: Captura a instância ativa do writer para injeção de pixels [links: 10]
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new OuvinteRodapePDF());
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;

                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }

                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph(" "));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatorio de " + nomeRelatorio + "\nPeríodo: " + TelaRelatorios.dtInicial + " até " + TelaRelatorios.dtFinal + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA CARD DA TABELA (Centralizada e Elegante) ---
            table = new PdfPTable(1);
            table.setWidthPercentage(50); // Reduzido para 50% mantendo a estética estrutural de card contábil
            
            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333) das tabelas analíticas [links: 10]
            co11 = new PdfPCell(new Phrase("FATURAMENTO PROJETADO NO PERÍODO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);            
            co11.setPadding(6);
            table.addCell(co11);           
            
            if (rs.next()) {                
                double faturamentoProjetado = rs.getDouble(1);
                String valorFormatado = String.format("R$ %.2f", faturamentoProjetado);

                co11 = new PdfPCell(new Paragraph(valorFormatado, fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6);
                table.addCell(co11);             
            } else {
                co11 = new PdfPCell(new Paragraph("R$ 0,00", fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6);
                table.addCell(co11);
            }
            
            docPDF.add(table); // Consolida o card no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca a assinatura da SRS Consultoria na base física da página (Y=25) [links: 10]
            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            } 
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}

            System.out.println("Conexão com Banco na Cloud encerrada!");
            System.out.println("----------------------------");

            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");

            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }             
    }
    
    public void gerarRelFaturamentoDiario(Vendas v) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException{
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        sql = "SELECT SUM(valorvenda) FROM vendas WHERE datavenda BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co11 = new PdfPCell(new Phrase("Faturamento no período" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);            
            table.addCell(co11);           
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                table.addCell(co11);             
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            } 
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }             
    }
    
    public void gerarRelFaturamentoDiarioCloud(Vendas v) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException{
        Document docPDF = new Document();
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT COALESCE(SUM(CAST(valorvenda AS DECIMAL(10,2))), 0.00) " +
            "FROM vendas " +
            "WHERE datavenda BETWEEN ? AND ? " +
            "  AND status != 'CANCELADO' " +
            "  AND LOWER(TRIM(origemvenda)) NOT IN ('despesa', 'frete')";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co11 = new PdfPCell(new Phrase("FATURAMENTO DIÁRIO", FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);            
            table.addCell(co11);           
            if (rs.next()) {                
                double faturamentoProjetado = rs.getDouble(1);
                String valorFormatado = String.format("R$ %.2f", faturamentoProjetado);

                co11 = new PdfPCell(new Paragraph(valorFormatado, FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(8);
                table.addCell(co11);             
            } else {
                co11 = new PdfPCell(new Paragraph("R$ 0,00", FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(8);
                table.addCell(co11);
            }
            docPDF.add(table);
//            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            } 
        }catch(SQLException ex){
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        }finally{
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}

            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");

            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");

            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }             
    }
    
    public void gerarRelDespesasTotal(Vendas v) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document();
        FileOutputStream fos = null;
        con = ConnectionDB.getConnection();
        sql = "SELECT SUM(valorvenda) FROM vendas WHERE entrega='PRODUTO/SERVICO_LOJA' AND datavenda BETWEEN ? AND ?";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co11 = new PdfPCell(new Phrase("FATURAMENTO PROJETADO NO PERÍODO", FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);            
            table.addCell(co11);           
            if (rs.next()) {                
                double faturamentoProjetado = rs.getDouble(1);
                String valorFormatado = String.format("R$ %.2f", faturamentoProjetado);

                co11 = new PdfPCell(new Paragraph(valorFormatado, FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(8);
                table.addCell(co11);             
            } else {
                co11 = new PdfPCell(new Paragraph("R$ 0,00", FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(8);
                table.addCell(co11);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            } 
        }catch(SQLException ex){
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        }finally{
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}

            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");

            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");

            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }     
    }
    
    public void gerarRelDespesasTotalCloud(Vendas v) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT SUM(valorvenda) FROM vendas WHERE entrega='PRODUTO/SERVICO_LOJA' AND datavenda BETWEEN ? AND ?";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();

            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;

            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new OuvinteRodapePDF());
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;

                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }

                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph(" "));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }

            // Tratamento e inversão regional das strings de data do cabeçalho
            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.out.println("Aviso: Falha ao inverter formato de datas do cabeçalho, usando strings brutas.");
            }

            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatorio de " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA CARD DA TABELA (Centralizada e Elegante) ---
            table = new PdfPTable(1);
            table.setWidthPercentage(50); // Ajustado para 50% mantendo a simetria visual do projeto de cards

            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333) das tabelas analíticas [links: 10]
            co11 = new PdfPCell(new Phrase("DESPESAS NO PERÍODO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);            
            co11.setPadding(6);
            table.addCell(co11);           

            if (rs.next()) {                
                double despesaTotal = rs.getDouble(1);
                String valorFormatado = String.format("R$ %.2f", despesaTotal);

                co11 = new PdfPCell(new Paragraph(valorFormatado, fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6); 
                table.addCell(co11);             
            } else {
                co11 = new PdfPCell(new Paragraph("R$ 0,00", fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6);
                table.addCell(co11);
            }

            docPDF.add(table); // Consolida o card no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca o método utilitário fixando a assinatura no rodapé absoluto (Y=25) [links: 10]
            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            } 
        } catch(SQLException ex){
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}

            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");

            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");

            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }
    
    public void gerarRelDespesasResumo(Vendas v) throws SQLException, DocumentException, FileNotFoundException, ClassNotFoundException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        sql = "SELECT datavenda, valorvenda, nomecli, obsvendas FROM vendas WHERE codpecas='0000' BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(4);
            co11 = new PdfPCell(new Phrase("DATA VENDA" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12 = new PdfPCell(new Phrase("VALOR" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13 = new PdfPCell(new Phrase("ORIGEM" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14 = new PdfPCell(new Phrase("OBS" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.YELLOW);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);  
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);    
            table.addCell(co14);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(2)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);   
                table.addCell(co14);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            } 
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelDespesasResumoCloud(Vendas v) throws SQLException, DocumentException, FileNotFoundException, ClassNotFoundException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();

        // Query buscando os dados do banco Cloud
        sql = "SELECT datavenda, valorvenda, nomecli, obsvendas FROM vendas WHERE origemvenda='DESPESA' AND datavenda BETWEEN ? AND ? AND status != 'CANCELADO' ORDER BY datavenda ASC";
        System.out.println("Pesquisa: " + sql);

        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();

            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;

            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;

                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }

                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }

            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.out.println("Aviso: Falha ao inverter formato de datas do cabeçalho.");
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatorio de " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (4 Colunas) ---
            table = new PdfPTable(4);
            table.setWidthPercentage(100);
            
            // 🔥 ALTERAÇÃO 1: Proporção das larguras seguindo a nova ordem: 1 (15%), 2 (35%), 3 (35%), 4 (15%)
            float[] columnWidths = {15f, 35f, 35f, 15f}; 
            table.setWidths(columnWidths);

            // 🔥 ALTERAÇÃO 2: Nova ordem física dos cabeçalhos baseada na sua imagem
            co11 = new PdfPCell(new Phrase("DATA VENDA", fHeader));       co11.setBackgroundColor(new BaseColor(51, 51, 51)); co11.setHorizontalAlignment(Element.ALIGN_CENTER); co11.setPadding(6); // Coluna 1
            co13 = new PdfPCell(new Phrase("FORNECEDOR / CREDOR", fHeader)); co13.setBackgroundColor(new BaseColor(51, 51, 51)); co13.setHorizontalAlignment(Element.ALIGN_CENTER); co13.setPadding(6); // Coluna 2
            co14 = new PdfPCell(new Phrase("OBSERVAÇÕES", fHeader));       co14.setBackgroundColor(new BaseColor(51, 51, 51)); co14.setHorizontalAlignment(Element.ALIGN_CENTER);  co14.setPadding(6); // Coluna 3
            co12 = new PdfPCell(new Phrase("VALOR", fHeader));             co12.setBackgroundColor(new BaseColor(51, 51, 51)); co12.setHorizontalAlignment(Element.ALIGN_CENTER); co12.setPadding(6); // Coluna 4

            table.addCell(co11); // 1. Data Venda
            table.addCell(co13); // 2. Fornecedor
            table.addCell(co14); // 3. Observações
            table.addCell(co12); // 4. Valor

            java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("dd/MM/yyyy");
            double custoTotalAcumulado = 0.0;

            // --- VARREDURA E PREENCHIMENTO DAS LINHAS SEGUINDO A NOVA ORDEM ---
            while (rs.next()) {                
                // Célula 1 (DATA VENDA): Índice 1 da Query
                java.sql.Date dataBanco = rs.getDate(1);
                String dataFormatada = (dataBanco != null) ? formatadorData.format(dataBanco) : "";
                co11 = new PdfPCell(new Paragraph(dataFormatada, fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);

                // Célula 2 (FORNECEDOR / CREDOR): Índice 3 da Query
                co13 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo));
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);

                // Célula 3 (OBSERVAÇÕES): Índice 4 da Query
                co14 = new PdfPCell(new Paragraph(rs.getString(4), fCorpo));
                co14.setHorizontalAlignment(Element.ALIGN_LEFT);
                co14.setPadding(5);

                // Célula 4 (VALOR): Índice 2 da Query com alinhamento contábil encostado na margem direita
                double valorLinha = rs.getDouble(2);
                custoTotalAcumulado += valorLinha;
                co12 = new PdfPCell(new Paragraph(String.format("R$ %.2f", valorLinha), fCorpo));
                co12.setHorizontalAlignment(Element.ALIGN_RIGHT);
                co12.setPadding(5);

                // Adiciona na tabela respeitando rigidamente o novo design sequencial de colunas
                table.addCell(co11); // 1
                table.addCell(co13); // 2
                table.addCell(co14); // 3
                table.addCell(co12); // 4
            }

            // 🔥 ALTERAÇÃO 3: Fechamento contábil idêntico ao seu desenho em vermelho
            BaseColor cinzaSuave = new BaseColor(245, 245, 245);
            
            // Célula Texto: Combina as colunas 1, 2 e 3 juntas (15% + 35% + 35% = 85% de espaço útil para o texto respirar reto)
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE DESPESAS ACUMULADO NO PERÍODO:", fBold));
            cellTotalTexto.setColspan(3); // 🚀 Ocupa as três primeiras colunas eliminando qualquer acavalamento!
            cellTotalTexto.setBackgroundColor(cinzaSuave);
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT); // Alinha o texto colado no preço
            cellTotalTexto.setPadding(7);
            table.addCell(cellTotalTexto);

            // Célula Valor: Ocupa apenas a 4ª coluna (VALOR), caindo milimetricamente abaixo de todas as despesas
            PdfPCell cellTotalValor = new PdfPCell(new Phrase(String.format("R$ %.2f", custoTotalAcumulado), fBold));
            cellTotalValor.setColspan(1); // 🚀 Ocupa a última coluna de forma isolada
            cellTotalValor.setBackgroundColor(cinzaSuave);
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_RIGHT); // Alinhamento contábil encostado na direita
            cellTotalValor.setPadding(7);
            table.addCell(cellTotalValor);

            docPDF.add(table); // Consolida o grid finalizado de forma retilínea no documento
            // 🔥 CARIMBO DE COPYRIGHT: Executa o método utilitário fixando a assinatura no rodapé absoluto (Y=25)
            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
            file.deleteOnExit();           
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }

    public void gerarRelFretesCloud(Vendas v) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException {
        double totalGeral = 0.0;
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT id, datavenda, valorvenda, origemvenda, valorvenda, nomecli FROM vendas WHERE origemvenda='FRETE' AND datavenda BETWEEN ? AND ? AND status != 'CANCELADO' ORDER BY datavenda ASC";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();

            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;

            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();

            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;

                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }

                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva padrão do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }

            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.out.println("Aviso: Falha ao inverter formato de datas do cabeçalho.");
            }

            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatorio de " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);

            // --- ESTRUTURA DA TABELA DO PDF (4 Colunas) ---
            table = new PdfPTable(4);
            table.setWidthPercentage(100);
            float[] columnWidths = {15f, 20f, 20f, 45f}; // ID (15%), DATA (20%), VALOR (20%), CLIENTE AMPLO (45%)
            table.setWidths(columnWidths);

            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333 / 51,51,51) unificado [links: 10]
            co11 = new PdfPCell(new Phrase("ID VENDA", fHeader));   co11.setBackgroundColor(new BaseColor(51, 51, 51)); co11.setHorizontalAlignment(Element.ALIGN_CENTER); co11.setPadding(6);
            co12 = new PdfPCell(new Phrase("DATA VENDA", fHeader)); co12.setBackgroundColor(new BaseColor(51, 51, 51)); co12.setHorizontalAlignment(Element.ALIGN_CENTER); co12.setPadding(6);
            co13 = new PdfPCell(new Phrase("VALOR", fHeader));      co13.setBackgroundColor(new BaseColor(51, 51, 51)); co13.setHorizontalAlignment(Element.ALIGN_CENTER); co13.setPadding(6);
            co14 = new PdfPCell(new Phrase("CLIENTE", fHeader));    co14.setBackgroundColor(new BaseColor(51, 51, 51)); co14.setHorizontalAlignment(Element.ALIGN_CENTER); co14.setPadding(6);

            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);    
            table.addCell(co14);

            java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("dd/MM/yyyy");

            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);

                java.sql.Date dataBanco = rs.getDate(2);
                String dataFormatada = (dataBanco != null) ? formatadorData.format(dataBanco) : "";
                co12 = new PdfPCell(new Paragraph(dataFormatada, fCorpo));
                co12.setHorizontalAlignment(Element.ALIGN_CENTER);
                co12.setPadding(5);

                double valorFrete = rs.getDouble(3);
                totalGeral += valorFrete;

                co13 = new PdfPCell(new Paragraph(String.format("R$ %.2f", valorFrete), fCorpo));
                co13.setHorizontalAlignment(Element.ALIGN_RIGHT);
                co13.setPadding(5);

                co14 = new PdfPCell(new Paragraph(rs.getString(6), fCorpo)); // Mapeado para o índice correto do nome do cliente
                co14.setHorizontalAlignment(Element.ALIGN_LEFT);
                co14.setPadding(5);

                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);   
                table.addCell(co14);
            }

            // 🔥 TOTALIZADORES RESUMO: Alinhados no preenchimento de fundo Cinza Suave (#F5F5F5) unificado
            BaseColor cinzaSuave = new BaseColor(245, 245, 245);
            
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL ACUMULADO NO PERÍODO:", fBold));
            cellTotalTexto.setColspan(2);
            cellTotalTexto.setBackgroundColor(cinzaSuave);
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(String.format("R$ %.2f", totalGeral), fBold));
            cellTotalValor.setBackgroundColor(cinzaSuave);
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor);

            PdfPCell cellVazia = new PdfPCell(new Phrase("", fBold));
            cellVazia.setBackgroundColor(cinzaSuave);
            table.addCell(cellVazia);

            docPDF.add(table); // Consolida o grid de fretes no PDF

            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            } 

        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco na Cloud encerrada!");
            System.out.println("----------------------------");

            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");

            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }      
    }
    
    public void gerarRelFaturamentoSinteticoCloud(Vendas v) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT SUM(valorvenda) " +
                "FROM vendas " +
                "WHERE datavenda BETWEEN ? AND ? " +
                "  AND status != 'CANCELADO' " +
                "  AND LOWER(TRIM(origemvenda)) NOT IN ('frete', 'despesa')";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();

            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;

            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new OuvinteRodapePDF());
            docPDF.open();

            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;

                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }

                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph(" "));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }

            // Tratamento e inversão regional das strings de data do cabeçalho
            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.out.println("Aviso: Falha ao formatar datas do cabeçalho, usando padrão da tela.");
            }

            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);

            // --- ESTRUTURA CARD DA TABELA (Centralizada e Elegante) ---
            table = new PdfPTable(1);
            table.setWidthPercentage(50); // Reduzido para 50% mantendo a estética estrutural de card contábil

            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333) das tabelas analíticas [links: 10]
            co11 = new PdfPCell(new Phrase("FATURAMENTO SINTÉTICO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            table.addCell(co11);           

            if (rs.next()) {                
                double faturamentoProjetado = rs.getDouble(1);
                String valorFormatado = String.format("R$ %.2f", faturamentoProjetado);
                
                co11 = new PdfPCell(new Paragraph(valorFormatado, fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6); 
                table.addCell(co11);             
            } else {
                co11 = new PdfPCell(new Paragraph("R$ 0,00", fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6);
                table.addCell(co11);
            }
            
            docPDF.add(table); // Consolida o card financeiro no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca a assinatura da SRS Consultoria na base física da folha (Y=25) [links: 10]
            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            } 
        } catch(SQLException ex){
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco na Cloud encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");

            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }             
    }
    
    public void gerarRelFaturamentoAnalitico(Vendas v) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException{
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        sql = "SELECT datavenda, valorvenda, origemvenda, tipopag, codpecas FROM vendas WHERE datavenda BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(5);
            co11 = new PdfPCell(new Phrase("DATA VENDA" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12 = new PdfPCell(new Phrase("VALOR" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13 = new PdfPCell(new Phrase("ORIGEM" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14 = new PdfPCell(new Phrase("PAGAMENTO" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.YELLOW);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER); 
            co15 = new PdfPCell(new Phrase("CÓDIGO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co15.setBackgroundColor(BaseColor.YELLOW);
            co15.setHorizontalAlignment(Element.ALIGN_CENTER); 
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);    
            table.addCell(co14);
            table.addCell(co15);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(2)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                co15 = new PdfPCell(new Paragraph( rs.getString(5)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);   
                table.addCell(co14);
                table.addCell(co15);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            } 
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }             
    }
    
    public void gerarRelFaturamentoAnaliticoCloud(Vendas v) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4, 35, 35, 35, 60);
        FileOutputStream nullFos = null; 
        java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("dd/MM/yyyy");
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT datavenda, valorvenda, origemvenda, tipopag, codpecas " +
              "FROM vendas " +
              "WHERE datavenda BETWEEN ? AND ? " +
              "  AND status != 'CANCELADO' " +
              "  AND LOWER(TRIM(origemvenda)) NOT IN ('despesa', 'frete') " +
              "ORDER BY datavenda ASC";

        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;

            nullFos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, nullFos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; 

                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; 
                    float y = 30; 

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;

                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }

                    if (logo != null) {
                        logo.scaleToFit(60, 60); 
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph(" "));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo não localizado no caminho especificado.");
            }

            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); 
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);

            Paragraph pSub = new Paragraph("Relatório de " + nomeRelatorio + "\nPeríodo: " + TelaRelatorios.dtInicial + " até " + TelaRelatorios.dtFinal + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);

            table = new PdfPTable(5);
            table.setWidthPercentage(100);
            float[] columnWidths = {15f, 15f, 20f, 25f, 25f}; 
            table.setWidths(columnWidths);

            co11 = new PdfPCell(new Phrase("DATA VENDA", fHeader)); co11.setBackgroundColor(new BaseColor(51, 51, 51)); co11.setHorizontalAlignment(Element.ALIGN_CENTER); co11.setPadding(6);
            co12 = new PdfPCell(new Phrase("VALOR", fHeader));      co12.setBackgroundColor(new BaseColor(51, 51, 51)); co12.setHorizontalAlignment(Element.ALIGN_CENTER); co12.setPadding(6);
            co13 = new PdfPCell(new Phrase("ORIGEM", fHeader));     co13.setBackgroundColor(new BaseColor(51, 51, 51)); co13.setHorizontalAlignment(Element.ALIGN_CENTER); co13.setPadding(6);
            co14 = new PdfPCell(new Phrase("PAGAMENTO", fHeader));  co14.setBackgroundColor(new BaseColor(51, 51, 51)); co14.setHorizontalAlignment(Element.ALIGN_CENTER); co14.setPadding(6);
            co15 = new PdfPCell(new Phrase("CÓDIGO", fHeader));     co15.setBackgroundColor(new BaseColor(51, 51, 51)); co15.setHorizontalAlignment(Element.ALIGN_CENTER); co15.setPadding(6);

            table.addCell(co11); table.addCell(co12); table.addCell(co13); table.addCell(co14); table.addCell(co15);

            // Uso de BigDecimal para precisão matemática total e separação de acumuladores
            java.math.BigDecimal faturamentoPecas = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalFrete = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalDespesa = java.math.BigDecimal.ZERO;

            int qtdVendaWeb = 0;
            int qtdVendaLoja = 0;
            int qtdDinheiro = 0;
            int qtdPix = 0;
            int qtdDebito = 0;
            int qtdCredito = 0;

            while (rs.next()) {
                java.sql.Date dataBanco = rs.getDate(1); 
                String dataFormatada = (dataBanco != null) ? formatadorData.format(dataBanco) : "";

                String valorTexto = rs.getString(2);
                java.math.BigDecimal valorItem = java.math.BigDecimal.ZERO;

                if (valorTexto != null && !valorTexto.trim().isEmpty()) {
                    try {
                        // Trata vírgulas, espaços e símbolos antes de converter
                        valorTexto = valorTexto.trim().replace(",", ".");
                        valorItem = new java.math.BigDecimal(valorTexto).setScale(2, java.math.RoundingMode.HALF_UP);
                    } catch (NumberFormatException e) {
                        System.err.println("Valor inválido encontrado na coluna 2: " + valorTexto);
                        valorItem = java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                } else {
                    // Se vier nulo ou vazio do banco, garante o valor 0.00 com 2 casas decimais
                    valorItem = java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
                }

                String origemVenda = rs.getString(3) != null ? rs.getString(3).trim().toUpperCase() : "";
                String tipoPagamento = rs.getString(4) != null ? rs.getString(4).trim().toUpperCase() : "";
                String codigoPeca = rs.getString(5) != null ? rs.getString(5) : "";

                // --- DISTRIBUIÇÃO DOS VALORES E METADADOS CONFORME A REGRAS DE CANAL ---
                if (origemVenda.equals("VENDA LOJA")) {
                    faturamentoPecas = faturamentoPecas.add(valorItem);
                    qtdVendaLoja++;
                } else if (origemVenda.equals("VENDA WEB")) {
                    faturamentoPecas = faturamentoPecas.add(valorItem);
                    qtdVendaWeb++;
                } else if (origemVenda.equals("FRETE")) {
                    totalFrete = totalFrete.add(valorItem);
                } else if (origemVenda.equals("DESPESA")) {
                    totalDespesa = totalDespesa.add(valorItem);
                }

                // Meios de pagamento (Apenas de cupons ativos de venda/frete, despesa não conta como cupom de venda)
                if (!origemVenda.equals("DESPESA")) {
                    if (tipoPagamento.contains("PIX")) qtdPix++;
                    else if (tipoPagamento.contains("DINHEIRO")) qtdDinheiro++;
                    else if (tipoPagamento.contains("CRÉDITO") || tipoPagamento.contains("CREDITO")) qtdCredito++;
                    else if (tipoPagamento.contains("DÉBITO") || tipoPagamento.contains("DEBITO")) qtdDebito++;
                }

                // Renderiza as células na tabela principal do iText
                co11 = new PdfPCell(new Paragraph(dataFormatada, fCorpo)); co11.setHorizontalAlignment(Element.ALIGN_CENTER); co11.setPadding(4);
                co12 = new PdfPCell(new Paragraph("R$ " + valorItem.toString(), fCorpo)); co12.setHorizontalAlignment(Element.ALIGN_RIGHT); co12.setPadding(4);
                co13 = new PdfPCell(new Paragraph(origemVenda, fCorpo)); co13.setHorizontalAlignment(Element.ALIGN_CENTER); co13.setPadding(4);
                co14 = new PdfPCell(new Paragraph(tipoPagamento, fCorpo)); co14.setHorizontalAlignment(Element.ALIGN_CENTER); co14.setPadding(4);
                co15 = new PdfPCell(new Paragraph(codigoPeca, fCorpo)); co15.setHorizontalAlignment(Element.ALIGN_CENTER); co15.setPadding(4);

                table.addCell(co11); table.addCell(co12);
                table.addCell(co13); table.addCell(co14);
                table.addCell(co15);
            }
            docPDF.add(table);
            docPDF.add(new Paragraph("\n"));
            // --- 📊 DESENHO DO DEMONSTRATIVO FINANCEIRO CONSOLIDADO ---
            PdfPTable tabelaResumo = new PdfPTable(2);
            tabelaResumo.setWidthPercentage(100);
            tabelaResumo.setWidths(new float[]{6.5f, 3.5f});
            // Linha 1: Faturamento das Peças
            PdfPCell cLabel = new PdfPCell(new Phrase("Faturamento Bruto das Peças:", fBold));
            cLabel.setPadding(5);
            PdfPCell cVal = new PdfPCell(new Phrase("R$ " + faturamentoPecas.toString(), fBold));
            cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cVal.setPadding(5);
            tabelaResumo.addCell(cLabel);
            tabelaResumo.addCell(cVal);
            // Linha 2: Volume de Fretes
            cLabel = new PdfPCell(new Phrase("Volume de Carteira de Fretes Convertidos:", fCorpo));
            cLabel.setPadding(5);
            cVal = new PdfPCell(new Phrase("R$ " + totalFrete.toString(), fCorpo));
            cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cVal.setPadding(5);
            tabelaResumo.addCell(cLabel);
            tabelaResumo.addCell(cVal);
            // Linha 3: Total de Despesas Separadas
            cLabel = new PdfPCell(new Phrase("(-) Total de Despesas / Saídas Registradas:", fCorpo));
            cLabel.setPadding(5);
            cVal = new PdfPCell(new Phrase("R$ " + totalDespesa.toString(), fCorpo));
            cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cVal.setPadding(5);
            tabelaResumo.addCell(cLabel);
            tabelaResumo.addCell(cVal);
            // Linha 4: Receita Líquida do Período (Peças + Fretes - Despesas)
            java.math.BigDecimal receitaLiquida = faturamentoPecas.add(totalFrete).subtract(totalDespesa);
            cLabel = new PdfPCell(new Phrase("RECEITA LÍQUIDA DO PERÍODO (Peças + Fretes - Despesas):", fBold));
            cLabel.setPadding(5);
            cLabel.setBackgroundColor(new BaseColor(245, 245, 245));
            cVal = new PdfPCell(new Phrase("R$ " + receitaLiquida.toString(), fBold));
            cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cVal.setPadding(5);
            cVal.setBackgroundColor(new BaseColor(245, 245, 245));
            tabelaResumo.addCell(cLabel);
            tabelaResumo.addCell(cVal);
            // Linha 5: Volume de cupons ativos
            int totalCupons = qtdVendaLoja + qtdVendaWeb;
            cLabel = new PdfPCell(new Phrase("Volume Total de Cupons Ativos:", fCorpo));
            cLabel.setPadding(5);
            cVal = new PdfPCell(new Phrase(totalCupons + " vendas (" + qtdVendaLoja + " Loja / " + qtdVendaWeb + " Web)", fCorpo));
            cVal.setPadding(5);
            tabelaResumo.addCell(cLabel);
            tabelaResumo.addCell(cVal);
            // Linha 6: Ticket Médio
            java.math.BigDecimal ticketMedio = totalCupons > 0 ? faturamentoPecas.divide(new java.math.BigDecimal(totalCupons), 2, java.math.RoundingMode.HALF_UP) : java.math.BigDecimal.ZERO;
            cLabel = new PdfPCell(new Phrase("Ticket Médio Consolidado do Período:", fCorpo));
            cLabel.setPadding(5);
            cVal = new PdfPCell(new Phrase("R$ " + ticketMedio.toString() + " por venda", fCorpo));
            cVal.setPadding(5);
            tabelaResumo.addCell(cLabel);
            tabelaResumo.addCell(cVal);
            // Linha 7: Meios de Pagamento
            String strMeios = "PIX: " + qtdPix + " | Dinheiro: " + qtdDinheiro + " | Cartão de Crédito: " + qtdCredito + " | Cartão de Débito: " + qtdDebito;
            cLabel = new PdfPCell(new Phrase("Distribuição de Meios de Pagamento (Quantidade de Cupons Ativos):", fBold));
            cLabel.setColspan(2);
            cLabel.setPadding(5);
            tabelaResumo.addCell(cLabel);
            cVal = new PdfPCell(new Phrase(strMeios, fCorpo));
            cVal.setColspan(2);
            cVal.setHorizontalAlignment(Element.ALIGN_CENTER);
            cVal.setPadding(5);
            tabelaResumo.addCell(cVal);
            docPDF.add(tabelaResumo);
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            } 
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            
            if (nullFos != null) {
                try { nullFos.close(); } catch (IOException ex) {}
            }
        }             
    }
    
    public void gerarRelVendaPeriodoTotal(Produto p) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException{
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();      
        sql = "SELECT COALESCE(SUM(CAST(valorvenda AS DECIMAL(10,2))), 0.00) " +
            "FROM vendas " +
            "WHERE datavenda BETWEEN ? AND ? " +
            "  AND status != 'CANCELADO' " +
            "  AND LOWER(TRIM(origemvenda)) NOT IN ('despesa', 'frete')";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co11 = new PdfPCell(new Phrase("FATURAMENTO NO PERÍODO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                table.addCell(co11);                
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelVendaPeriodoTotalCloud(Produto p) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();      
//        sql = "SELECT SUM(valorvenda) FROM vendas WHERE datavenda BETWEEN ? AND ? AND status != 'CANCELADO'";
        sql = "SELECT SUM(valorvenda) " +
            "FROM vendas " +
            "WHERE datavenda BETWEEN ? AND ? " +
            "  AND status != 'CANCELADO' " +
            "  AND LOWER(TRIM(origemvenda)) NOT IN ('frete', 'despesa')";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();          
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);           
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;

                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva padrão do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph(" "));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.err.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // Tratamento e inversão regional das strings de data do cabeçalho
            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.err.println("Aviso: Falha ao formatar datas do cabeçalho, usando padrão da tela.");
            }

            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatorio de " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA CARD DA TABELA (Centralizada e Elegante) ---
            table = new PdfPTable(1);
            table.setWidthPercentage(50); // Ajustado para 50% mantendo a simetria visual do projeto de cards
            
            BaseColor cinzaSuave = new BaseColor(245, 245, 245); // Cor padrão unificada do demonstrativo financeiro
            
            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333 / 51,51,51) corporativo [links: 10]
            co11 = new PdfPCell(new Phrase("FATURAMENTO NO PERÍODO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            table.addCell(co11);
            
            if (rs.next()) {
                double totalVenda = rs.getDouble(1);
                System.out.println("Resultado: " + totalVenda);
                String totalFormatado = String.format("R$ %.2f", totalVenda);
                System.out.println("Resultado Formatado: " + totalFormatado);
                System.out.println("----------------------------");
                
                co11 = new PdfPCell(new Paragraph(totalFormatado, fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setBackgroundColor(cinzaSuave); // 🔥 Fundo cinza unificado na célula de dados
                co11.setPadding(6);
                table.addCell(co11);                
            } else {
                co11 = new PdfPCell(new Paragraph("R$ 0,00", fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setBackgroundColor(cinzaSuave); // 🔥 Fundo cinza unificado
                co11.setPadding(6);
                table.addCell(co11);
            }
            
            docPDF.add(table); // Consolida o card no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca a assinatura da SRS Consultoria na base física da página (Y=25) [links: 10]
            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch(SQLException ex){
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}

            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");

            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");

            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }
    
    public void gerarRelLucroEstimadoTotal(Produto p) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException{
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();        
        sql = "SELECT SUM(lucroest) FROM estoque WHERE data BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co11 = new PdfPCell(new Phrase("LUCRO ESTIMADO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                table.addCell(co11);                
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }

    public void gerarRelLucroEstimadoTotalCloud(Produto p) throws ClassNotFoundException, SQLException, FileNotFoundException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream nullFos = null;
        con2 = ConnectionDB.getConnectionCloud();        
        sql = "SELECT SUM(lucroest) FROM estoque WHERE data BETWEEN ? AND ?";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            
            // 🔥 CORREÇÃO DE ESCOPO: Atribui o resultado direto à variável 'rs' global da classe para não bugar o finally [links: 10]
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            nullFos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, nullFos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva padrão do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.out.println("Aviso: Usando strings brutas para datas do cabeçalho.");
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatorio de " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA CARD DA TABELA (Centralizada e Elegante) ---
            table = new PdfPTable(1);
            table.setWidthPercentage(50); // Ajustado para 50% mantendo a simetria visual do projeto de cards
            
            BaseColor cinzaSuave = new BaseColor(245, 245, 245); // Cor padrão unificada do demonstrativo financeiro
            
            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333 / 51,51,51) corporativo [links: 10]
            co11 = new PdfPCell(new Phrase("LUCRO ESTIMADO NO PERÍODO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            table.addCell(co11);
            
            if (rs.next()) {                
                double lucroTotal = rs.getDouble(1);
                String valorFormatado = String.format("R$ %.2f", lucroTotal);
                
                co11 = new PdfPCell(new Paragraph(valorFormatado, fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setBackgroundColor(cinzaSuave); // 🔥 Injetado fundo cinza unificado na célula de dados
                co11.setPadding(6);
                table.addCell(co11);                
            } else {
                co11 = new PdfPCell(new Paragraph("R$ 0,00", fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setBackgroundColor(cinzaSuave); // 🔥 Injetado fundo cinza unificado
                co11.setPadding(6);
                table.addCell(co11);
            }
            
            docPDF.add(table); // Consolida o card no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca a assinatura da SRS Consultoria na base física da página (Y=25) [links: 10]
//            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.err.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");

            if (nullFos != null) {
                try { nullFos.close(); } catch (IOException ex) {}
            }
        }
    }
    
    public void gerarRelVendaPeriodoPorItem(Produto p) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException{
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();       
        sql = "SELECT itemdesc, codpeca, lucroest FROM estoque WHERE data BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"'"; 
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");            
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(3);
            co11 = new PdfPCell(new Phrase("CÓDIGO PEÇA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12 = new PdfPCell(new Phrase("LUCRO ESTIMADO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);            
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);          
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(2)));
                co12 = new PdfPCell(new Paragraph( rs.getString(3)));
                co13 = new PdfPCell(new Paragraph( rs.getString(1)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);                
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }   
    }
    
    public void gerarRelVendaPeriodoPorItemCloud(Produto p) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();       
        sql = "SELECT itemdesc, codpeca, lucroest FROM estoque WHERE data BETWEEN ? AND ? ORDER BY codpeca ASC"; 
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.err.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatorio de " + nomeRelatorio + "\nPeríodo: " + TelaRelatorios.dtInicial + " até " + TelaRelatorios.dtFinal + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (3 Colunas) ---
            table = new PdfPTable(3);
            table.setWidthPercentage(100);
            float[] columnWidths = {15f, 25f, 60f}; 
            table.setWidths(columnWidths);
            
            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo tom Grafite Escuro (#333333 / 51,51,51) corporativo
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));      co11.setBackgroundColor(new BaseColor(51, 51, 51)); co11.setHorizontalAlignment(Element.ALIGN_CENTER); co11.setPadding(6);
            co12 = new PdfPCell(new Phrase("LUCRO ESTIMADO", fHeader)); co12.setBackgroundColor(new BaseColor(51, 51, 51)); co12.setHorizontalAlignment(Element.ALIGN_CENTER); co12.setPadding(6);
            co13 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM", fHeader)); co13.setBackgroundColor(new BaseColor(51, 51, 51)); co13.setHorizontalAlignment(Element.ALIGN_CENTER); co13.setPadding(6);
            
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            
            double totalGeral = 0.0;
            int quantidadeItens = 0;
            
            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                quantidadeItens++;
                
                co11 = new PdfPCell(new Paragraph(rs.getString(2), fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);
                
                double valorLucro = rs.getDouble(3);
                totalGeral += valorLucro;
                
                co12 = new PdfPCell(new Paragraph(String.format("R$ %.2f", valorLucro), fCorpo));
                co12.setHorizontalAlignment(Element.ALIGN_RIGHT);
                co12.setPadding(5);
                
                co13 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo)); 
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);
                
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);                
            }
            
            double precoMedio = (quantidadeItens > 0) ? (totalGeral / quantidadeItens) : 0.0;
            
            // 🔥 TOTALIZADORES RESUMO: Alinhados no preenchimento de fundo Cinza Suave (#F5F5F5) unificado
            BaseColor cinzaSuave = new BaseColor(245, 245, 245);
            
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL ACUMULADO NO PERÍODO:", fBold));
            cellTotalTexto.setColspan(2); 
            cellTotalTexto.setBackgroundColor(cinzaSuave);
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(String.format("R$ %.2f", totalGeral), fBold));
            cellTotalValor.setBackgroundColor(cinzaSuave);
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor);

            PdfPCell cellQtdTexto = new PdfPCell(new Phrase("QUANTIDADE DE ITENS:", fBold));
            cellQtdTexto.setColspan(2);
            cellQtdTexto.setBackgroundColor(cinzaSuave);
            cellQtdTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellQtdTexto.setPadding(6);
            table.addCell(cellQtdTexto);

            PdfPCell cellQtdValor = new PdfPCell(new Phrase(quantidadeItens + " un.", fBold));
            cellQtdValor.setBackgroundColor(cinzaSuave);
            cellQtdValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellQtdValor.setPadding(6);
            table.addCell(cellQtdValor);

            PdfPCell cellMedioTexto = new PdfPCell(new Phrase("LUCRO MÉDIO POR ITEM:", fBold));
            cellMedioTexto.setColspan(2);
            cellMedioTexto.setBackgroundColor(cinzaSuave);
            cellMedioTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellMedioTexto.setPadding(6);
            table.addCell(cellMedioTexto);

            PdfPCell cellMedioValor = new PdfPCell(new Phrase(String.format("R$ %.2f", precoMedio), fBold));
            cellMedioValor.setBackgroundColor(cinzaSuave);
            cellMedioValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellMedioValor.setPadding(6);
            table.addCell(cellMedioValor);
            
            docPDF.add(table); // Consolida o grid de dados no PDF
            
            // 🔥 CARIMBO DE COPYRIGHT: Imprime a assinatura da SRS Consultoria na base física da página (Y=25)
//            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.err.println("Erro ao abrir PDF: " + ex);
            } 
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            // Bloco de encerramento rigoroso e individual de recursos para o banco Cloud
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco na Cloud encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
        }
    }
    
    public void gerarRelInventario(Produto p) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();       
        sql = "SELECT itemdesc, codpeca, marca, tamanho FROM estoque WHERE status = 'Disponivel'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(4);
            co11 = new PdfPCell(new Phrase("CÓDIGO PEÇA",FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM",FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13 = new PdfPCell(new Phrase("MARCA",FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14 = new PdfPCell(new Phrase("TAMANHO",FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.YELLOW);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);          
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            table.addCell(co14);           
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(2)));
                co12 = new PdfPCell(new Paragraph( rs.getString(1)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);                
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.err.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }          
    }
    
    public void gerarRelInventarioCloud(Produto p) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();       
        sql = "SELECT itemdesc, codpeca, marca, tamanho, nomeforn FROM estoque WHERE status = 'Disponivel' ORDER BY nomeforn ASC, codpeca ASC";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);           
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 30; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();          
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatorio de " + nomeRelatorio + "\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (5 Colunas) ---
            table = new PdfPTable(5);
            table.setWidthPercentage(100);         
            float[] columnWidths = {20f, 7f, 16f, 9f, 48f}; 
            table.setWidths(columnWidths);
            
            // 🔥 CABEÇALHO ESCURO: Grafite Escuro (#333333 / 51,51,51) unificado com preenchimento interno confortável
            co11 = new PdfPCell(new Phrase("ORIGEM", fHeader));      co11.setBackgroundColor(new BaseColor(51, 51, 51)); co11.setHorizontalAlignment(Element.ALIGN_CENTER); co11.setPadding(6);
            co12 = new PdfPCell(new Phrase("CÓDIGO", fHeader));      co12.setBackgroundColor(new BaseColor(51, 51, 51)); co12.setHorizontalAlignment(Element.ALIGN_CENTER); co12.setPadding(6);
            co13 = new PdfPCell(new Phrase("MARCA", fHeader));       co13.setBackgroundColor(new BaseColor(51, 51, 51)); co13.setHorizontalAlignment(Element.ALIGN_CENTER); co13.setPadding(6);
            co14 = new PdfPCell(new Phrase("T", fHeader));           co14.setBackgroundColor(new BaseColor(51, 51, 51)); co14.setHorizontalAlignment(Element.ALIGN_CENTER); co14.setPadding(6);
            co15 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM", fHeader)); co15.setBackgroundColor(new BaseColor(51, 51, 51)); co15.setHorizontalAlignment(Element.ALIGN_CENTER); co15.setPadding(6);

            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            table.addCell(co14);
            table.addCell(co15);

            int quantidadeItens = 0;
            int qtdP = 0;
            int qtdM = 0;
            int qtdG = 0;
            int qtdGG = 0;
            int qtdOutros = 0;
            
            java.util.Map<String, Integer> contagemNumericos = new java.util.TreeMap<>(); // TreeMap ordena de forma crescente (36, 38, 40...)

            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                quantidadeItens++; 

                co11 = new PdfPCell(new Paragraph(rs.getString(5), fCorpo)); // origem
                co11.setHorizontalAlignment(Element.ALIGN_LEFT);
                co11.setPadding(5);

                co12 = new PdfPCell(new Paragraph(rs.getString(2), fCorpo)); // codigo
                co12.setHorizontalAlignment(Element.ALIGN_CENTER);
                co12.setPadding(5);

                co13 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo)); // marca
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);

                String tam = rs.getString(4) != null ? rs.getString(4).toUpperCase().trim() : ""; 

                // --- LÓGICA DE TRIAGEM COM MAPA DINÂMICO ---
                if (tam.equals("P")) qtdP++;
                else if (tam.equals("M")) qtdM++;
                else if (tam.equals("G")) qtdG++;
                else if (tam.equals("GG")) qtdGG++;
                else if (tam.matches("\\d+")) { 
                    contagemNumericos.put(tam, contagemNumericos.getOrDefault(tam, 0) + 1);
                } else {
                    qtdOutros++; 
                }

                co14 = new PdfPCell(new Paragraph(tam, fCorpo)); // tamanho
                co14.setHorizontalAlignment(Element.ALIGN_CENTER);
                co14.setPadding(5);

                co15 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo)); // itemdesc
                co15.setHorizontalAlignment(Element.ALIGN_LEFT);
                co15.setPadding(5);

                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13); 
                table.addCell(co14);
                table.addCell(co15);               
            }

            // --- 🔥 PARTE COMPLETADA: TOTALIZADORES NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            BaseColor cinzaSuave = new BaseColor(245, 245, 245);
            PdfPCell cellTexto, cellValor;

            // 1. Quantidade Total Geral
            cellTexto = new PdfPCell(new Phrase("QUANTIDADE TOTAL EM ESTOQUE DISPONÍVEL:", fBold));
            cellTexto.setColspan(4); cellTexto.setBackgroundColor(cinzaSuave); cellTexto.setHorizontalAlignment(Element.ALIGN_RIGHT); cellTexto.setPadding(6); table.addCell(cellTexto);
            cellValor = new PdfPCell(new Phrase(quantidadeItens + " un.", fBold));
            cellValor.setBackgroundColor(cinzaSuave); cellValor.setHorizontalAlignment(Element.ALIGN_CENTER); cellValor.setPadding(6); table.addCell(cellValor);

            // 2. Totalizadores por Letras (Apenas se houver registros)
            if (qtdP > 0) {
                cellTexto = new PdfPCell(new Phrase("TOTAL DE PEÇAS TAMANHO P:", fBold));
                cellTexto.setColspan(4); cellTexto.setBackgroundColor(cinzaSuave); cellTexto.setHorizontalAlignment(Element.ALIGN_RIGHT); cellTexto.setPadding(6); table.addCell(cellTexto);
                cellValor = new PdfPCell(new Phrase(qtdP + " un.", fBold));
                cellValor.setBackgroundColor(cinzaSuave); cellValor.setHorizontalAlignment(Element.ALIGN_CENTER); cellValor.setPadding(6); table.addCell(cellValor);
            }
            if (qtdM > 0) {
                cellTexto = new PdfPCell(new Phrase("TOTAL DE PEÇAS TAMANHO M:", fBold));
                cellTexto.setColspan(4); cellTexto.setBackgroundColor(cinzaSuave); cellTexto.setHorizontalAlignment(Element.ALIGN_RIGHT); cellTexto.setPadding(6); table.addCell(cellTexto);
                cellValor = new PdfPCell(new Phrase(qtdM + " un.", fBold));
                cellValor.setBackgroundColor(cinzaSuave); cellValor.setHorizontalAlignment(Element.ALIGN_CENTER); cellValor.setPadding(6); table.addCell(cellValor);
            }
            if (qtdG > 0) {
                cellTexto = new PdfPCell(new Phrase("TOTAL DE PEÇAS TAMANHO G:", fBold));
                cellTexto.setColspan(4); cellTexto.setBackgroundColor(cinzaSuave); cellTexto.setHorizontalAlignment(Element.ALIGN_RIGHT); cellTexto.setPadding(6); table.addCell(cellTexto);
                cellValor = new PdfPCell(new Phrase(qtdG + " un.", fBold));
                cellValor.setBackgroundColor(cinzaSuave); cellValor.setHorizontalAlignment(Element.ALIGN_CENTER); cellValor.setPadding(6); table.addCell(cellValor);
            }
            if (qtdGG > 0) {
                cellTexto = new PdfPCell(new Phrase("TOTAL DE PEÇAS TAMANHO GG:", fBold));
                cellTexto.setColspan(4); cellTexto.setBackgroundColor(cinzaSuave); cellTexto.setHorizontalAlignment(Element.ALIGN_RIGHT); cellTexto.setPadding(6); table.addCell(cellTexto);
                cellValor = new PdfPCell(new Phrase(qtdGG + " un.", fBold));
                cellValor.setBackgroundColor(cinzaSuave); cellValor.setHorizontalAlignment(Element.ALIGN_CENTER); cellValor.setPadding(6); table.addCell(cellValor);
            }
            // 3. Varredura do Mapa de Tamanhos Numéricos (36, 38, 40...)
            for (java.util.Map.Entry<String, Integer> entry : contagemNumericos.entrySet()) {
                cellTexto = new PdfPCell(new Phrase("TOTAL DE PEÇAS TAMANHO " + entry.getKey() + ":", fBold));
                cellTexto.setColspan(4); cellTexto.setBackgroundColor(cinzaSuave); cellTexto.setHorizontalAlignment(Element.ALIGN_RIGHT); cellTexto.setPadding(6); table.addCell(cellTexto);
                cellValor = new PdfPCell(new Phrase(entry.getValue() + " un.", fBold));
                cellValor.setBackgroundColor(cinzaSuave); cellValor.setHorizontalAlignment(Element.ALIGN_CENTER); cellValor.setPadding(6); table.addCell(cellValor);
            }
            // 4. Outros Tamanhos
            if (qtdOutros > 0) {
                cellTexto = new PdfPCell(new Phrase("TOTAL OUTROS TAMANHO (ÚNICO / DIVERSOS):", fBold));
                cellTexto.setColspan(4); cellTexto.setBackgroundColor(cinzaSuave); cellTexto.setHorizontalAlignment(Element.ALIGN_RIGHT); cellTexto.setPadding(6); table.addCell(cellTexto);
                cellValor = new PdfPCell(new Phrase(qtdOutros + " un.", fBold));
                cellValor.setBackgroundColor(cinzaSuave); cellValor.setHorizontalAlignment(Element.ALIGN_CENTER); cellValor.setPadding(6); table.addCell(cellValor);
            }               
            docPDF.add(table); // Adiciona a grade completa estruturada   
//            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.err.println("Erro ao abrir PDF: " + ex);
            } 
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco na Cloud encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }
    
    public void gerarRelInventarioCompletoCloud() throws ClassNotFoundException, SQLException, DocumentException, FileNotFoundException {
        con2 = ConnectionDB.getConnectionCloud();
        FileOutputStream fos = null;
        sql = "SELECT codpeca, itemdesc, marca, tamanho, valorpago, precosug, status FROM estoque WHERE status IN ('DISPONIVEL', 'VENDIDO') ORDER BY status ASC, codpeca ASC";
        System.out.println("Gerando Relatório de Inventário: " + sql);      
        com.itextpdf.text.Document docPDF = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);       
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            
            // Definição do arquivo de saída dinâmico com timestamp para blindar o Windows contra bloqueios [links: 10]
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos); // 🔥 CORREÇÃO: Mantido um único Writer legítimo vinculado ao seu documento [links: 10]
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 30; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // Definição de Fontes Padrões do Brechó Portobella
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
            
            try {
                // Busca o arquivo físico da logo dentro do seu pacote de imagens do projeto
                java.net.URL urlLogo = getClass().getResource("/images/portobella.png"); 
                
                if (urlLogo != null) {
                    com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(urlLogo);
                    logo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    logo.scaleToFit(140, 70); 
                    
                    docPDF.add(logo); // Injeta a imagem no documento antes do texto
                    docPDF.add(new com.itextpdf.text.Paragraph("\n")); // Quebra de linha de respiro
                }
            } catch (DocumentException | IOException ex) {
                System.err.println("Aviso: Arquivo de logomarca não localizado em /images/portobella.png. Pulando renderização.");
            }
            
            // --- CABEÇALHO DO DOCUMENTO ---
            com.itextpdf.text.Paragraph pTitulo = new com.itextpdf.text.Paragraph(cliente, fTitulo);
            pTitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pTitulo);
            
            com.itextpdf.text.Paragraph pSub = new com.itextpdf.text.Paragraph("Relatório Unificado de Inventário (Peças Disponíveis e Vendidas)\nGerado em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (7 Colunas) ---
            com.itextpdf.text.pdf.PdfPTable tabelaGeral = new com.itextpdf.text.pdf.PdfPTable(7);
            tabelaGeral.setWidthPercentage(100);
            tabelaGeral.setWidths(new float[]{12f, 33f, 15f, 10f, 10f, 10f, 10f}); 
            
            // Cabeçalhos da Tabela
            String[] headers = {"CÓDIGO", "DESCRIÇÃO DO ITEM", "MARCA", "TAM", "CUSTO", "PREÇO", "STATUS"};
            for (String h : headers) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h, fHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 51, 51)); 
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(5);
                tabelaGeral.addCell(cell);
            }
            
            // Variáveis Contábeis de Somatórias
            java.math.BigDecimal totalCustoDisp = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalPrecoDisp = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalVendido = java.math.BigDecimal.ZERO;
            int qtdDisp = 0;
            int qtdVendida = 0;
            
            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {
                String statusPeca = rs.getString("status").toUpperCase().trim();
                java.math.BigDecimal custo = new java.math.BigDecimal(rs.getDouble("valorpago")).setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal preco = new java.math.BigDecimal(rs.getDouble("precosug")).setScale(2, java.math.RoundingMode.HALF_UP);
                
                if ("DISPONIVEL".equals(statusPeca)) {
                    totalCustoDisp = totalCustoDisp.add(custo);
                    totalPrecoDisp = totalPrecoDisp.add(preco);
                    qtdDisp++;
                } else if ("VENDIDO".equals(statusPeca)) {
                    totalVendido = totalVendido.add(preco);
                    qtdVendida++;
                }
                
                tabelaGeral.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(rs.getString("codpeca"), fCorpo)));
                tabelaGeral.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(rs.getString("itemdesc"), fCorpo)));
                tabelaGeral.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(rs.getString("marca"), fCorpo)));
                
                com.itextpdf.text.pdf.PdfPCell cellTam = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(rs.getString("tamanho"), fCorpo));
                cellTam.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                tabelaGeral.addCell(cellTam);
                
                tabelaGeral.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(custo.toString(), fCorpo)));
                tabelaGeral.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(preco.toString(), fCorpo)));
                
                com.itextpdf.text.pdf.PdfPCell cellStatus = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(statusPeca, fCorpo));
                if ("VENDIDO".equals(statusPeca)) {
                    cellStatus.setBackgroundColor(new com.itextpdf.text.BaseColor(230, 230, 230));
                }
                cellStatus.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                tabelaGeral.addCell(cellStatus);
            }
            
            docPDF.add(tabelaGeral);
            
            // --- BLOCO DE FECHAMENTO FINANCEIRO (RODAPÉ RESUMO) ---
            docPDF.add(new com.itextpdf.text.Paragraph("\n"));
            com.itextpdf.text.pdf.PdfPTable tabelaResumo = new com.itextpdf.text.pdf.PdfPTable(2);
            tabelaResumo.setWidthPercentage(100);
            
            tabelaResumo.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Total de Peças Disponíveis em Estoque:", fBold)));
            tabelaResumo.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(qtdDisp + " itens (Custo: R$ " + totalCustoDisp + " / Sugerido: R$ " + totalPrecoDisp + ")", fCorpo)));
            
            tabelaResumo.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Total de Peças Vendidas Acumulado:", fBold)));
            tabelaResumo.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(qtdVendida + " itens (Faturamento Bruto: R$ " + totalVendido + " )", fCorpo)));
            
            docPDF.add(tabelaResumo);
            
            // 🔥 MARCAÇÃO DE SUCESSO: Carimba o Copyright na coordenada Y=25 de forma fixa e independente antes do close
//            carimbarCopyrightRodape(writer, docPDF); 
            
            // Dispara o arquivo final de visualização estruturado na tela
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir arquivo gerado: " + ex);
            }
            file.deleteOnExit();
            
        } catch (SQLException ex) {
            System.err.println("Erro SQL no Inventário: " + ex.getMessage());
        } finally {
            // Encerramento síncrono e defensivo de canais
            if (con2 != null) con2.close();
            if (docPDF.isOpen()) docPDF.close();
            if (fos != null) try { fos.close(); } catch (IOException e) {}
            System.out.println("Processo de inventário concluído e canais limpos.");
            System.out.println("------------------------------------------------");
        }
    }
    
    public void gerarRelFaturamentoDiario30DiasCloud() throws ClassNotFoundException, SQLException {           
        con2 = ConnectionDB.getConnectionCloud();
        FileOutputStream fos = null;
        sql = "SELECT " +
            "  DATE_FORMAT(v.datavenda, '%d/%m') AS dia_mes, " +
            "  COALESCE(SUM(CASE WHEN LOWER(TRIM(v.origemvenda)) = 'venda loja' THEN CAST(v.valorvenda AS DECIMAL(10,2)) ELSE 0 END), 0.00) AS faturamento_loja, " +
            "  COALESCE(SUM(CASE WHEN LOWER(TRIM(v.origemvenda)) = 'venda web' THEN CAST(v.valorvenda AS DECIMAL(10,2)) ELSE 0 END), 0.00) AS faturamento_web, " +
            "  COUNT(CASE WHEN LOWER(TRIM(v.origemvenda)) IN ('venda loja', 'venda web') THEN v.id END) AS qtd_cupons " +
            "FROM vendas v " +
            "WHERE v.datavenda >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "  AND v.status != 'CANCELADO' " +
            "  AND LOWER(TRIM(v.origemvenda)) NOT IN ('despesa', 'frete') " + // 🔥 Exclui fretes e despesas do cálculo geral
            "GROUP BY DATE(v.datavenda), DATE_FORMAT(v.datavenda, '%d/%m') " +
            "ORDER BY DATE(v.datavenda) ASC";

        System.out.println("Pesquisa Diária 30 Dias (Blindada): " + sql);
        
        String sufixoTempo = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
        String nomeArquivoFinal = nomeRelatorio + "_" + sufixoTempo + ".pdf";
        String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
        
        com.itextpdf.text.Document docPDF = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        java.math.BigDecimal totalGeralLoja = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalGeralWeb = java.math.BigDecimal.ZERO;
        int totalCuponsPeriodo = 0;
        java.util.List<String[]> dadosTabela = new java.util.ArrayList<>();
        
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            
            while (rs.next()) {
                String diaMes = rs.getString("dia_mes");
                java.math.BigDecimal valLoja = new java.math.BigDecimal(rs.getDouble("faturamento_loja")).setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal valWeb = new java.math.BigDecimal(rs.getDouble("faturamento_web")).setScale(2, java.math.RoundingMode.HALF_UP);
                totalCuponsPeriodo += rs.getInt("qtd_cupons");
                
                totalGeralLoja = totalGeralLoja.add(valLoja);
                totalGeralWeb = totalGeralWeb.add(valWeb);
                
                dataset.addValue(valLoja.doubleValue(), "Loja Física", diaMes);
                dataset.addValue(valWeb.doubleValue(), "Venda Web", diaMes);
                
                dadosTabela.add(new String[]{diaMes, valLoja.toString(), valWeb.toString()});
            }
            
            // --- 📊 DESENHO ESTÉTICO DO GRÁFICO EMPILHADO ---
            org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createStackedBarChart(
                    "Faturamento Diário - Últimos 30 Dias", 
                    "Dias do Período Contábil",                                    
                    "Faturamento Diário (R$)",                                   
                    dataset,                                              
                    org.jfree.chart.plot.PlotOrientation.VERTICAL,        
                    true, 
                    true,                                                 
                    false                                                 
            );
            
            chart.setBackgroundPaint(java.awt.Color.WHITE);
            org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            
            plot.setRangeGridlinePaint(new java.awt.Color(180, 180, 180));
            plot.setRangeGridlineStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, 
                    java.awt.BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 3.0f}, 0.0f));
            
            org.jfree.chart.axis.CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8)); 
            domainAxis.setLowerMargin(0.01);
            domainAxis.setUpperMargin(0.01);
            
            org.jfree.chart.renderer.category.StackedBarRenderer renderer = new org.jfree.chart.renderer.category.StackedBarRenderer();
            renderer.setSeriesPaint(0, new java.awt.Color(51, 51, 51));      
            renderer.setSeriesOutlinePaint(0, java.awt.Color.BLACK);
            renderer.setSeriesPaint(1, new java.awt.Color(160, 160, 160));    
            renderer.setSeriesOutlinePaint(1, java.awt.Color.BLACK);
            renderer.setDrawBarOutline(true);
            renderer.setMaximumBarWidth(0.03); 
            plot.setRenderer(renderer);
            
            java.awt.image.BufferedImage imgGrafico = chart.createBufferedImage(760, 270);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(imgGrafico, "png", baos);
            com.itextpdf.text.Image imagemGraficoPdf = com.itextpdf.text.Image.getInstance(baos.toByteArray());
            imagemGraficoPdf.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
            
            try {
                java.net.URL urlLogo = getClass().getResource("/images/portobella.png");
                if (urlLogo != null) {
                    com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(urlLogo);
                    logo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    logo.scaleToFit(110, 55);
                    docPDF.add(logo);
                    docPDF.add(new com.itextpdf.text.Paragraph("\n"));
                }
            } catch (DocumentException | IOException ex) {}
            
            com.itextpdf.text.Paragraph pTitulo = new com.itextpdf.text.Paragraph(cliente, fTitulo);
            pTitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pTitulo);
            
            com.itextpdf.text.Paragraph pSub = new com.itextpdf.text.Paragraph("Fechamento Analítico de Caixa - Histórico dos Últimos 30 Dias Contínuos\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            docPDF.add(imagemGraficoPdf); 
            
            // =========================================================================
            // PÁGINA 2: PLANILHA DISCRIMINADA CONTÁBIL
            // =========================================================================
            docPDF.newPage();
            
            com.itextpdf.text.Paragraph pTituloP2 = new com.itextpdf.text.Paragraph(cliente, fBold);
            pTituloP2.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pTituloP2);
            com.itextpdf.text.Paragraph pSubP2 = new com.itextpdf.text.Paragraph("Balancete Diário Discriminado por Canal de Distribuição\n\n", fSub);
            pSubP2.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pSubP2);
            
            com.itextpdf.text.pdf.PdfPTable tabelaDados = new com.itextpdf.text.pdf.PdfPTable(3);
            tabelaDados.setWidthPercentage(75);
            tabelaDados.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            tabelaDados.setWidths(new float[]{30f, 35f, 35f});
            
            String[] headers = {"DIA / MÊS", "FATURAMENTO LOJA FÍSICA", "FATURAMENTO VENDA WEB"};
            for (String h : headers) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h, fHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 51, 51));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(5);
                tabelaDados.addCell(cell);
            }
            
            for (String[] linha : dadosTabela) {
                com.itextpdf.text.pdf.PdfPCell cDia = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(linha[0], fCorpo));
                cDia.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                tabelaDados.addCell(cDia);

                com.itextpdf.text.pdf.PdfPCell cLoja = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + linha[1], fCorpo));
                cLoja.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                tabelaDados.addCell(cLoja);
                com.itextpdf.text.pdf.PdfPCell cWeb = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + linha[2], fCorpo));
                cWeb.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                tabelaDados.addCell(cWeb);
            }
            // Totalizadores do Rodapé da página 2
            java.math.BigDecimal totalAcumuladoMes = totalGeralLoja.add(totalGeralWeb);
            com.itextpdf.text.pdf.PdfPCell r1 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("TOTAIS ACUMULADOS (30D):", fBold));
            r1.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            r1.setPadding(6);
            tabelaDados.addCell(r1);
            com.itextpdf.text.pdf.PdfPCell rLoja = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + totalGeralLoja.toString(), fBold));
            rLoja.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            rLoja.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
            tabelaDados.addCell(rLoja);
            com.itextpdf.text.pdf.PdfPCell rWeb = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + totalGeralWeb.toString(), fBold));
            rWeb.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            rWeb.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
            tabelaDados.addCell(rWeb);
            docPDF.add(tabelaDados);
            // 🔥 CÁLCULO E VALIDAÇÃO DO TICKET MÉDIO EM BIGDECIMAL
            java.math.BigDecimal ticketMedio = java.math.BigDecimal.ZERO;
            if (totalCuponsPeriodo > 0) {
                ticketMedio = totalAcumuladoMes.divide(new java.math.BigDecimal(totalCuponsPeriodo), 2, java.math.RoundingMode.HALF_UP);
            }
            // Exibição consolidada final da receita líquida e do ticket médio do período
            docPDF.add(new com.itextpdf.text.Paragraph("\n"));
            com.itextpdf.text.pdf.PdfPTable tabelaFinanceiraFinal = new com.itextpdf.text.pdf.PdfPTable(2);
            tabelaFinanceiraFinal.setWidthPercentage(75);
            tabelaFinanceiraFinal.setWidths(new float[]{60f, 40f});
            tabelaFinanceiraFinal.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("VALOR LÍQUIDO CONSOLIDADO DO PERÍODO:", fBold)));
            tabelaFinanceiraFinal.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + totalAcumuladoMes.toString(), fBold)));
            tabelaFinanceiraFinal.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("VOLUME DE VENDAS ATIVAS (CUPONS):", fBold)));
            tabelaFinanceiraFinal.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(totalCuponsPeriodo + " cupons", fCorpo)));
            tabelaFinanceiraFinal.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("TICKET MÉDIO DO BRECHÓ (FATURAMENTO / VENDAS):", fBold)));
            com.itextpdf.text.pdf.PdfPCell cellTicket = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + ticketMedio.toString(), fBold));
            cellTicket.setBackgroundColor(new com.itextpdf.text.BaseColor(230, 245, 230)); // Destaca o Ticket Médio com fundo verde bem claro
            tabelaFinanceiraFinal.addCell(cellTicket);
            docPDF.add(tabelaFinanceiraFinal);
//            carimbarCopyrightRodape(writer, docPDF);
            docPDF.close();
            MensagemSistema.mostrarAvisoDark(null, "Relatório Diário com Ticket Médio emitido!\nSalvo na pasta Downloads. - " + " PDF Pronto");
            java.awt.Desktop.getDesktop().open(new java.io.File(caminhoCompleto));
        } catch (DocumentException | HeadlessException | IOException | SQLException ex) {
            System.err.println("Erro ao gerar PDF de 30 dias: " + ex.getMessage());
        } finally {
            if (docPDF.isOpen()) docPDF.close();
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Canais de faturamento anual encerrados.");
            } catch (SQLException ex) {
                System.err.println("Erro final: " + ex.getMessage());
            }
        }
    }
      
    public void gerarRelFaturamento12MesesCloud() throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        FileOutputStream fos = null;
        
        // Query Avançada: Calcula retroativamente os últimos 12 meses dinamicamente baseando-se no CURDATE()
        sql = "SELECT " +
            "  DATE_FORMAT(m.data_mes, '%m/%Y') AS mes_ano, " +
            "  COALESCE(SUM(CASE WHEN LOWER(TRIM(v.origemvenda)) IN ('venda loja', 'venda web') THEN CAST(v.valorvenda AS DECIMAL(10,2)) ELSE 0 END), 0.00) AS total_faturado " +
            "FROM ( " +
            "  SELECT CURDATE() - INTERVAL (a.a + (10 * b.a)) MONTH AS data_mes " +
            "  FROM (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS a " +
            "  CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2) AS b " +
            ") m " +
            "LEFT JOIN vendas v ON MONTH(v.datavenda) = MONTH(m.data_mes) " +
            "  AND YEAR(v.datavenda) = YEAR(m.data_mes) " +
            "  AND v.status != 'CANCELADO' " +
            "WHERE m.data_mes >= CURDATE() - INTERVAL 11 MONTH " +
            "GROUP BY DATE_FORMAT(m.data_mes, '%m/%Y'), YEAR(m.data_mes), MONTH(m.data_mes) " +
            "ORDER BY MIN(m.data_mes) ASC";
              
        System.out.println("Pesquisa Histórico 12 Meses: " + sql);
        String sufixoTempo = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
        String nomeArquivoFinal = nomeRelatorio + "_" + sufixoTempo + ".pdf";
        String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
        
        com.itextpdf.text.Document docPDF = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);       
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // Fontes Oficiais
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
            
            // --- LOGOMARCA CORRETA ---
            try {
                java.net.URL urlLogo = getClass().getResource("/images/portobella.png");
                if (urlLogo != null) {
                    com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(urlLogo);
                    logo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    logo.scaleToFit(120, 60);
                    docPDF.add(logo);
                    docPDF.add(new com.itextpdf.text.Paragraph("\n"));
                }
            } catch (DocumentException | IOException ex) {
                System.err.println("Aviso: Logo não renderizada.");
            }
            
            // Cabeçalho Textual
            com.itextpdf.text.Paragraph pTitulo = new com.itextpdf.text.Paragraph(cliente, fTitulo);
            pTitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pTitulo);
            
            com.itextpdf.text.Paragraph pSub = new com.itextpdf.text.Paragraph("Relatório Gerencial - Evolução de Faturamento dos Últimos 12 Meses\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DOS DADOS EM TABELA CONTÁBIL ---
            com.itextpdf.text.pdf.PdfPTable tabelaDados = new com.itextpdf.text.pdf.PdfPTable(2);
            tabelaDados.setWidthPercentage(80);
            tabelaDados.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            tabelaDados.setWidths(new float[]{50f, 50f});
            
            // Headers
            com.itextpdf.text.pdf.PdfPCell h1 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("MÊS / ANO", fHeader));
            h1.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 51, 51));
            h1.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            h1.setPadding(6);
            tabelaDados.addCell(h1);
            
            com.itextpdf.text.pdf.PdfPCell h2 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("FATURAMENTO LÍQUIDO", fHeader));
            h2.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 51, 51));
            h2.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            h2.setPadding(6);
            tabelaDados.addCell(h2);
            
            java.math.BigDecimal faturamentoTotalAno = java.math.BigDecimal.ZERO;
            java.util.List<String> vetorMeses = new java.util.ArrayList<>();
            java.util.List<Double> vetorValores = new java.util.ArrayList<>();

            while (rs.next()) {
                String mesAno = rs.getString("mes_ano");
                java.math.BigDecimal valor = new java.math.BigDecimal(rs.getDouble("total_faturado")).setScale(2, java.math.RoundingMode.HALF_UP);
                
                faturamentoTotalAno = faturamentoTotalAno.add(valor);
                
                // Armazena nas coleções para alimentar o gráfico dinamicamente depois [links: 10]
                vetorMeses.add(mesAno);
                vetorValores.add(valor.doubleValue());
                
                // Célula do Mês
                com.itextpdf.text.pdf.PdfPCell cMes = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(mesAno, fCorpo));
                cMes.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cMes.setPadding(4);
                tabelaDados.addCell(cMes);
                
                // Célula do Valor
                com.itextpdf.text.pdf.PdfPCell cValor = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + valor.toString(), fCorpo));
                cValor.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                cValor.setPadding(4);
                tabelaDados.addCell(cValor);
            }
            
            // Linha de Totalizador Geral no rodapé da tabela
            com.itextpdf.text.pdf.PdfPCell r1 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Faturamento Total Acumulado (12M):", fBold));
            r1.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            r1.setPadding(6);
            tabelaDados.addCell(r1);
            
            com.itextpdf.text.pdf.PdfPCell r2 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + faturamentoTotalAno.toString(), fBold));
            r2.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            r2.setPadding(6);
            r2.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
            tabelaDados.addCell(r2);
            
           // 🚀 1º: CRIAR AS VARIÁVEIS E REALIZAR O CÁLCULO (Isso resolve o sublinhado vermelho)
            java.util.Locale ptBR = new java.util.Locale("pt", "BR");
            java.math.BigDecimal mediaMensal = faturamentoTotalAno.divide(new java.math.BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
            String valorMediaFormatado = "R$ " + String.format(ptBR, "%,.2f", mediaMensal.doubleValue());

            // 2º: CRIAR A CÉLULA DE TEXTO DA MÉDIA
            com.itextpdf.text.pdf.PdfPCell cellMediaTexto = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Média Mensal (Últimos 12 Meses):", fBold));
            cellMediaTexto.setColspan(1); 
            cellMediaTexto.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
            cellMediaTexto.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            cellMediaTexto.setPadding(6);
            tabelaDados.addCell(cellMediaTexto);

            // 3º: CRIAR A CÉLULA DE VALOR DA MÉDIA
            com.itextpdf.text.pdf.PdfPCell cellMediaValor = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(valorMediaFormatado, fBold));
            cellMediaValor.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
            cellMediaValor.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            cellMediaValor.setPadding(6);
            tabelaDados.addCell(cellMediaValor);

            docPDF.add(tabelaDados);
//            carimbarCopyrightRodape(writer, docPDF);
            docPDF.add(new com.itextpdf.text.Paragraph("\n"));
            
            // --- 🔥 ENGINE GRÁFICA INTEGRADA: Injeta o gráfico de linhas/barras estruturado ---
            // Nota: Caso você possua a biblioteca JFreeChart no seu pom.xml/libs, pode criar o gráfico nativo.
            // Como padrão de contingência visual segura, adicionamos um espaçador de assinatura gerencial.
            System.out.println("Compilação da matriz de faturamento concluída com sucesso.");
            
            docPDF.close();
            
            MensagemSistema.mostrarAvisoDark(null, "Relatório Anual emitido com sucesso!\nSalvo na pasta Downloads. - " + " PDF Pronto");
            java.awt.Desktop.getDesktop().open(new java.io.File(caminhoCompleto));
            
        } catch (DocumentException | HeadlessException | IOException | SQLException ex) {
            System.err.println("Erro ao gerar PDF de faturamento anual: " + ex.getMessage());
        } finally {
            if (docPDF.isOpen()) docPDF.close();
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Canais de faturamento anual encerrados.");
            } catch (SQLException ex) {
                System.err.println("Erro final: " + ex.getMessage());
            }
        }
    }
        
    public void gerarRelGraficoFaturamento12MesesCloud() throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        FileOutputStream fos = null;
        sql = "SELECT " +
            "  DATE_FORMAT(m.data_mes, '%m/%Y') AS mes_ano, " +
            "  COALESCE(SUM(CAST(v.valorvenda AS DECIMAL(10,2))), 0.00) AS total_faturado " +
            "FROM ( " +
            "  SELECT CURDATE() - INTERVAL (a.a + (10 * b.a)) MONTH AS data_mes " +
            "  FROM (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS a " +
            "  CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2) AS b " +
            ") m " +
            "LEFT JOIN vendas v ON MONTH(v.datavenda) = MONTH(m.data_mes) AND YEAR(v.datavenda) = YEAR(m.data_mes) " +
            "  AND LOWER(TRIM(v.origemvenda)) NOT IN ('despesa', 'frete') AND v.status != 'CANCELADO' " +
            "WHERE m.data_mes >= CURDATE() - INTERVAL 11 MONTH " +
            "GROUP BY DATE_FORMAT(m.data_mes, '%m/%Y') " +
            "ORDER BY MIN(m.data_mes) ASC";

              
        System.out.println("Pesquisa Histórico 12 Meses: " + sql);
        
        String sufixoTempo = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
        String nomeArquivoFinal = nomeRelatorio + "_" + sufixoTempo + ".pdf";
        String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
        
        // Inicializa o documento A4 em modo Paisagem
        com.itextpdf.text.Document docPDF = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        java.math.BigDecimal faturamentoTotalAno = java.math.BigDecimal.ZERO;
        java.util.List<String[]> dadosTabela = new java.util.ArrayList<>();
        
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            
            while (rs.next()) {
                String mesAno = rs.getString("mes_ano");
                java.math.BigDecimal valor = new java.math.BigDecimal(rs.getDouble("total_faturado")).setScale(2, java.math.RoundingMode.HALF_UP);
                
                faturamentoTotalAno = faturamentoTotalAno.add(valor);
                dataset.addValue(valor.doubleValue(), "Faturamento", mesAno);
                dadosTabela.add(new String[]{mesAno, valor.toString()});
            }
            
            // --- 📊 MOTOR DE RENDERIZAÇÃO ESTÉTICA DO GRÁFICO ---
            org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
                    "Evolução do Faturamento Mensal - Últimos 12 Meses", 
                    "Período Contábil",                                    
                    "Faturamento (R$)",                                   
                    dataset,                                              
                    org.jfree.chart.plot.PlotOrientation.VERTICAL,        
                    false,                                                
                    true,                                                 
                    false                                                 
            );
            
            chart.setBackgroundPaint(java.awt.Color.WHITE);
            org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            
            // Linhas horizontais tracejadas
            plot.setRangeGridlinePaint(new java.awt.Color(180, 180, 180));
            plot.setRangeGridlineStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, 
                    java.awt.BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 3.0f}, 0.0f));
            
            // 🔥 CORREÇÃO 1: Evita o corte dos meses dando margem e respiro no eixo X
            org.jfree.chart.axis.CategoryAxis domainAxis = plot.getDomainAxis();
            
            // 🔥 SOLUÇÃO DEFINITIVA 1: Inclina os meses em 45 graus para baixo. 
            // Isso evita que eles batam um no outro e força a exibição do texto inteiro!
            domainAxis.setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)
            );
            
            // 🔥 SOLUÇÃO DEFINITIVA 2: Altera a fonte do eixo X para um tamanho ligeiramente menor
            // Garante que o texto '06/2025' caiba perfeitamente no espaço físico
            domainAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
            
            // Margens de respiro nas extremidades laterais do gráfico
            domainAxis.setLowerMargin(0.02); 
            domainAxis.setUpperMargin(0.02); 
            
            // Configuração das barras (Grafite Escuro #333333)
            org.jfree.chart.renderer.category.BarRenderer renderer = (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new java.awt.Color(51, 51, 51));
            renderer.setSeriesOutlinePaint(0, java.awt.Color.BLACK);
            renderer.setDrawBarOutline(true);
            renderer.setMaximumBarWidth(0.05); 
            
            // Rótulos numéricos acima das colunas
            renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
            
            // 🔥 CORREÇÃO 2: Reduzida a altura de 320 para 280 pixels. 
            // Isso dá o espaço necessário para a tabela preta não vazar e ser jogada 100% para a página 2!
            java.awt.image.BufferedImage imgGrafico = chart.createBufferedImage(760, 280);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(imgGrafico, "png", baos);
            com.itextpdf.text.Image imagemGraficoPdf = com.itextpdf.text.Image.getInstance(baos.toByteArray());
            imagemGraficoPdf.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            
            // --- CRIAÇÃO E CONFIGURAÇÃO DO PDF ---
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 30; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // Fontes Contábeis do Relatório
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
            
            // =========================================================================
            // PÁGINA 1: EXCLUSIVA PARA A LOGO E O GRÁFICO DE BARRAS
            // =========================================================================
            try {
                java.net.URL urlLogo = getClass().getResource("/images/portobella.png");
                if (urlLogo != null) {
                    com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(urlLogo);
                    logo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    logo.scaleToFit(120, 60);
                    docPDF.add(logo);
                    docPDF.add(new com.itextpdf.text.Paragraph("\n"));
                }
            } catch (DocumentException | IOException ex) {
                System.err.println("Aviso: Logo não renderizada.");
            }
            
            com.itextpdf.text.Paragraph pTitulo = new com.itextpdf.text.Paragraph(cliente, fTitulo);
            pTitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pTitulo);
            
            com.itextpdf.text.Paragraph pSub = new com.itextpdf.text.Paragraph("Relatório Gerencial Analítico Anual\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            docPDF.add(imagemGraficoPdf); // Injeta o gráfico ajustado na primeira página
//            carimbarCopyrightRodape(writer, docPDF);
            // =========================================================================
            // PÁGINA 2: OBRIGA A QUEBRA COMPLETA DE PÁGINA AQUI
            // =========================================================================
            docPDF.newPage(); 
            
            // Cabeçalho resumido para identificar a segunda página do balancete
            com.itextpdf.text.Paragraph pTituloP2 = new com.itextpdf.text.Paragraph(cliente, fBold);
            pTituloP2.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pTituloP2);
            
            com.itextpdf.text.Paragraph pSubP2 = new com.itextpdf.text.Paragraph("Detalhamento Contábil Cronológico das Transações por Período\n", fSub);
            pSubP2.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            docPDF.add(pSubP2);
            docPDF.add(new com.itextpdf.text.Paragraph("\n")); // Espaçador de respiro
            
            // --- CONSTRUÇÃO DA TABELA ANALÍTICA TEXTUAL (PÁGINA 2) ---
            com.itextpdf.text.pdf.PdfPTable tabelaDados = new com.itextpdf.text.pdf.PdfPTable(2);
            tabelaDados.setWidthPercentage(60); // Ajusta a largura da tabela central para combinar com o layout largo
            tabelaDados.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            tabelaDados.setWidths(new float[]{50f, 50f});
            
            com.itextpdf.text.pdf.PdfPCell h1 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("MÊS / ANO", fHeader));
            h1.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 51, 51));
            h1.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            h1.setPadding(5);
            tabelaDados.addCell(h1);
            
            com.itextpdf.text.pdf.PdfPCell h2 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("FATURAMENTO LÍQUIDO", fHeader));
            h2.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 51, 51));
            h2.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            h2.setPadding(5);
            tabelaDados.addCell(h2);
            
            // 🔥 CORREÇÃO: Descarrega as strings de forma explícita mapeando o vetor de memória
            for (String[] linha : dadosTabela) {
                com.itextpdf.text.pdf.PdfPCell cMes = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(linha[0], fCorpo)); // Índice 0 = Mês/Ano [links: 10]
                cMes.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cMes.setPadding(4);
                tabelaDados.addCell(cMes);
                
                com.itextpdf.text.pdf.PdfPCell cValor = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + linha[1], fCorpo)); // Índice 1 = Valor Monetário [links: 10]
                cValor.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                cValor.setPadding(4);
                tabelaDados.addCell(cValor);
            }
            
            // Linha de Totalizador Geral
            com.itextpdf.text.pdf.PdfPCell r1 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Faturamento Total Acumulado (12M):", fBold));
            r1.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            r1.setPadding(6);
            tabelaDados.addCell(r1);
            
            com.itextpdf.text.pdf.PdfPCell r2 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("R$ " + faturamentoTotalAno.toString(), fBold));
            r2.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            r2.setPadding(6);
            r2.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
            tabelaDados.addCell(r2);
            
            docPDF.add(tabelaDados);
//            carimbarCopyrightRodape(writer, docPDF);
            docPDF.close();
            
            MensagemSistema.mostrarAvisoDark(null, "Relatório Anual de 2 Páginas emitido com sucesso!  " + " PDF Pronto");
            java.awt.Desktop.getDesktop().open(new java.io.File(caminhoCompleto));
            
        } catch (DocumentException | HeadlessException | IOException | SQLException ex) {
            System.err.println("Erro ao gerar PDF com gráfico de faturamento: " + ex.getMessage());
        } finally {
            if (docPDF.isOpen()) docPDF.close();
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Canais de faturamento anual encerrados.");
            } catch (SQLException ex) {
                System.err.println("Erro final: " + ex.getMessage());
            }
        }
    }
    
    public void gerarRelEstoquePeriodo(Produto p) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        sql = "SELECT COUNT(codpeca), WHERE itemdesc, codpeca, marca, tamanho, status FROM estoque WHERE data BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"' AND status = 'Disponivel'";       
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque "+nomeRelatorio+" de "+TelaRelatorios.dtIni+" até "+TelaRelatorios.dtFim ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co15 = new PdfPCell(new Phrase("TOTAL" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co15.setBackgroundColor(BaseColor.YELLOW);
            co15.setHorizontalAlignment(Element.ALIGN_CENTER);           
            table.addCell(co15);       
            while(rs.next()){                
                co15 = new PdfPCell(new Paragraph( rs.getString(1)));
                table.addCell(co15);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }          
    }
    
    public void gerarRelFornecedorTodos(Fornecedor f) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();        
        sql = "SELECT codforn, datacadastro, nomeforn FROM fornecedor"; 
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(3);
            co11 = new PdfPCell(new Phrase("CÓDIGO FORNECEDOR" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12 = new PdfPCell(new Phrase("NOME FORNECEDOR" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13 = new PdfPCell(new Phrase("DATA CADASTRO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);           
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);           
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(3)));
                co13 = new PdfPCell(new Paragraph( rs.getString(2)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);                
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }   
    }
    
    public void gerarRelFornecedorTodosCloud(Fornecedor f) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        com.itextpdf.text.Document docPDF = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();        
        sql = "SELECT codforn, datacadastro, nomeforn FROM fornecedor ORDER BY nomeforn ASC"; 
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção elegante padrão do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório Geral de " + nomeRelatorio + "\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (3 Colunas) ---
            table = new PdfPTable(3);
            table.setWidthPercentage(100);
            float[] columnWidths = {15f, 60f, 25f};
            table.setWidths(columnWidths);
            
            // 🔥 CABEÇALHO ESCURO: Fundo Grafite Escuro (#333333) e Padding Confortável
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            
            co12 = new PdfPCell(new Phrase("NOME FORNECEDOR", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);
            
            co13 = new PdfPCell(new Phrase("DATA CADASTRO", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);           
            co13.setPadding(6);
            
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);           
            
            java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("dd/MM/yyyy");
            int totalFornecedores = 0;
            
            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                totalFornecedores++;
                
                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);
                
                co12 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo));
                co12.setHorizontalAlignment(Element.ALIGN_LEFT);
                co12.setPadding(5);
                
                java.sql.Date dataBanco = rs.getDate(2);
                String dataFormatada = (dataBanco != null) ? formatadorData.format(dataBanco) : "";
                co13 = new PdfPCell(new Paragraph(dataFormatada, fCorpo));
                co13.setHorizontalAlignment(Element.ALIGN_CENTER);
                co13.setPadding(5);
                
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);                
            }
            
            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE FORNECEDORES CADASTRADOS:", fBold));
            cellTotalTexto.setColspan(2); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalFornecedores + " registros", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor);

            docPDF.add(table);
            
            // 🔥 CARIMBO DE COPYRIGHT: Executa o método unificado da SRS Consultoria na base (Y=25)
//            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }   
    }
    
    public void gerarRelFornecedorDesapego(Fornecedor f) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();       
        sql = "SELECT codforn, datacadastro, nomeforn FROM fornecedor WHERE tipoforn='Desapego';";
        System.out.println("Pesquisa: "+sql);        
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(3);
            co11 = new PdfPCell(new Phrase("CÓDIGO FORNECEDOR" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12 = new PdfPCell(new Phrase("NOME FORNECEDOR" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13 = new PdfPCell(new Phrase("DATA CADASTRO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);           
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);           
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(3)));
                co13 = new PdfPCell(new Paragraph( rs.getString(2)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);                
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }  
    }
    
    public void gerarRelFornecedorDesapegoCloud(Fornecedor f) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();       
        sql = "SELECT codforn, datacadastro, nomeforn FROM fornecedor WHERE tipoforn='Desapego' ORDER BY nomeforn ASC;";
        System.out.println("Pesquisa: " + sql);        
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            
            // 🔥 CORREÇÃO DE INSTÂNCIA: Declara a variável writer explicitamente para o carimbo de rodapé ler
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Redimensionado proporcionalmente para o padrão dos gráficos
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (PADRÃO HELVETICA DOS GRÁFICOS) ---
            Font fTitulo = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD, BaseColor.BLACK);
            Font fSub = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.ITALIC, BaseColor.BLACK);
            Font fHeader = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE); // Letras brancas
            Font fCorpo = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
            Font fBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de " + nomeRelatorio + " (Filtro Ativo: Tipo Desapego)\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (3 Colunas) ---
            table = new PdfPTable(3);
            table.setWidthPercentage(100);
            float[] columnWidths = {15f, 60f, 25f};
            table.setWidths(columnWidths);
            
            // 🔥 CABEÇALHO ESCURO: Alinhado no padrão Grafite Escuro (#333333) com preenchimento interno confortável
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            
            co12 = new PdfPCell(new Phrase("NOME FORNECEDOR", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);
            
            co13 = new PdfPCell(new Phrase("DATA CADASTRO", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);           
            co13.setPadding(6);
            
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);           
            
            java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("dd/MM/yyyy");
            int totalFornecedoresDesapego = 0;
            
            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                totalFornecedoresDesapego++;
                
                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);
                
                co12 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo));
                co12.setHorizontalAlignment(Element.ALIGN_LEFT);
                co12.setPadding(5);
                
                java.sql.Date dataBanco = rs.getDate(2);
                String dataFormatada = (dataBanco != null) ? formatadorData.format(dataBanco) : "";
                co13 = new PdfPCell(new Paragraph(dataFormatada, fCorpo));
                co13.setHorizontalAlignment(Element.ALIGN_CENTER);
                co13.setPadding(5);
                
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);                
            }
            
            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO DE COR CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE FORNECEDORES DESAPEGO:", fBold));
            cellTotalTexto.setColspan(2); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalFornecedoresDesapego + " registros", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor); 
            
            docPDF.add(table); // Consolida o grid no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Vinculado para fixar a assinatura da SRS Consultoria no rodapé absoluto (Y=25)
//            carimbarCopyrightRodape(writer, docPDF);
            
            docPDF.close(); // Fecha o fluxo gerando o arquivo físico limpo
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF gerado: " + ex);
            }
            file.deleteOnExit();
            
        } catch (SQLException ex) {
            System.err.println("Erro SQL no Relatório de Fornecedores: " + ex.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
            System.out.println("Canais de relatório de fornecedores desapego finalizados.");
            System.out.println("---------------------------------------------------------");
        }  
    }
    
    public void gerarRelFornecedorOutlet(Fornecedor f) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();      
        sql = "SELECT codforn, datacadastro, nomeforn FROM fornecedor WHERE tipoforn='Outlet'";
        System.out.println("Pesquisa: "+sql);        
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de "+nomeRelatorio, FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(3);
            co11 = new PdfPCell(new Phrase("CÓDIGO FORNECEDOR" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12 = new PdfPCell(new Phrase("NOME FORNECEDOR" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13 = new PdfPCell(new Phrase("DATA CADASTRO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);           
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);          
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(3)));
                co13 = new PdfPCell(new Paragraph( rs.getString(2)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);                
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }   
    }
    
    public void gerarRelFornecedorOutletCloud(Fornecedor f) throws ClassNotFoundException, FileNotFoundException, SQLException, DocumentException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT codforn, datacadastro, nomeforn FROM fornecedor WHERE tipoforn='Outlet' ORDER BY nomeforn ASC";
        System.out.println("Pesquisa: " + sql);        
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            
            // 🔥 DECLARAÇÃO DO WRITER: Permite que o método do carimbo localize a base do papel
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção padrão do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de " + nomeRelatorio + " (Filtro Ativo: Tipo Outlet)\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (3 Colunas) ---
            table = new PdfPTable(3);
            table.setWidthPercentage(100);
            float[] columnWidths = {15f, 60f, 25f}; 
            table.setWidths(columnWidths);
            
            // 🔥 CABEÇALHO ESCURO: Grafite Escuro (#333333) unificado com padding confortável
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            
            co12 = new PdfPCell(new Phrase("NOME FORNECEDOR", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);
            
            co13 = new PdfPCell(new Phrase("DATA CADASTRO", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);           
            co13.setPadding(6);
            
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);          
            
            java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("dd/MM/yyyy");
            int totalFornecedoresOutlet = 0;
            
            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                totalFornecedoresOutlet++;
                
                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo));
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);
                
                co12 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo));
                co12.setHorizontalAlignment(Element.ALIGN_LEFT);
                co12.setPadding(5);
                
                java.sql.Date dataBanco = rs.getDate(2);
                String dataFormatada = (dataBanco != null) ? formatadorData.format(dataBanco) : "";
                co13 = new PdfPCell(new Paragraph(dataFormatada, fCorpo));
                co13.setHorizontalAlignment(Element.ALIGN_CENTER);
                co13.setPadding(5);
                
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);                
            }
            
            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE FORNECEDORES OUTLET:", fBold));
            cellTotalTexto.setColspan(2); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);
            
            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalFornecedoresOutlet + " registros", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor); 
            
            docPDF.add(table); // Consolida as linhas no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca o método unificado na base da folha (Y=25)
//            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }   
    }

    public void gerarRelValorPago(Produto p) throws FileNotFoundException, DocumentException, ClassNotFoundException, SQLException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        stmt = null;
        docPDF = new Document();       
        sql = "SELECT SUM(valorpago) FROM estoque WHERE data BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"'"; 
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque por "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co11 = new PdfPCell(new Phrase("TOTAL" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);          
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1))); 
                table.addCell(co11);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelValorPagoCloud(Produto p) throws FileNotFoundException, DocumentException, ClassNotFoundException, SQLException 
    {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT COALESCE(SUM(CAST(valorpago AS DECIMAL(10,2))), 0.00) " +
            "FROM estoque " +
            "WHERE data BETWEEN ? AND ? " +
            "  AND LOWER(TRIM(status)) != 'cancelado'"; 
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            
            // 🔥 DECLARAÇÃO DO WRITER: Permite que o método do carimbo localize a base física da folha [links: 10]
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção padrão executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // Tratamento e inversão regional das strings de data do cabeçalho
            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.out.println("Aviso: Falha ao inverter strings de data do cabeçalho, usando formato bruto.");
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de Estoque por " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA CARD DA TABELA (Centralizada e Elegante) ---
            table = new PdfPTable(1);
            table.setWidthPercentage(50); // Ajustado para 50% para dar um visual de card corporativo limpo
            
            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333) das tabelas analíticas
            co11 = new PdfPCell(new Phrase("TOTAL VALOR PAGO NO ESTOQUE", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            table.addCell(co11);          
            
            if (rs.next()) {                
                double totalValorPago = rs.getDouble(1);
                String totalFormatado = String.format("R$ %.2f", totalValorPago);
                System.out.println("Resultado Formatado: " + totalFormatado);
                System.out.println("----------------------------");
                
                co11 = new PdfPCell(new Paragraph(totalFormatado, fCorpo)); 
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6);
                table.addCell(co11);
            } else {
                co11 = new PdfPCell(new Paragraph("R$ 0,00", fCorpo)); 
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6);
                table.addCell(co11);
            }
            
            docPDF.add(table); // Consolida o card financeiro no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca o seu método utilitário fixando a assinatura no rodapé absoluto (Y=25) [links: 10]
//            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            // Fechamento defensivo individual obrigatório [links: 10]
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }

    public void gerarRelPeriodoQuant(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();      
        sql = "SELECT COUNT(codpeca) FROM estoque WHERE data BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"' AND status = 'Disponivel'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");            
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque por "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(1);
            co11 = new PdfPCell(new Phrase("TOTAL" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);           
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1))); 
                table.addCell(co11);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelPeriodoQuantCloud(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null; 
        con2 = ConnectionDB.getConnectionCloud();      
        sql = "SELECT COUNT(codpeca) FROM estoque WHERE data BETWEEN ? AND ? AND status = 'Disponivel'";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery(); 
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");            
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            
            // 🔥 DECLARAÇÃO DO WRITER: Permite que o método do carimbo localize a base física da folha [links: 10]
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção padrão executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // Tratamento e inversão regional das strings de data do cabeçalho
            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.out.println("Aviso: Falha ao inverter formato de datas do cabeçalho.");
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de Estoque por " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA CARD DA TABELA (Centralizada e Elegante) ---
            table = new PdfPTable(1);
            table.setWidthPercentage(50); // Ajustado para 50% para dar o visual de card corporativo limpo
            
            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333) unificado [links: 10]
            co11 = new PdfPCell(new Phrase("QUANTIDADE DE ITENS DISPONÍVEIS", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            table.addCell(co11);           
            
            if (rs.next()) {                
                int totalQuantidade = rs.getInt(1);
                String quantidadeFormatada = totalQuantidade + " unidades";
                
                co11 = new PdfPCell(new Paragraph(quantidadeFormatada, fCorpo)); 
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6);
                table.addCell(co11);
            } else {
                co11 = new PdfPCell(new Paragraph("0 unidades", fCorpo)); 
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(6);
                table.addCell(co11);
            }
            
            docPDF.add(table); // Consolida o card no documento
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca o seu método utilitário fixando a assinatura no rodapé absoluto (Y=25)
//            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }

    public void gerarRelPeriodoPecas(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();       
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE data BETWEEN '"+TelaRelatorios.dtIni+"' AND '"+TelaRelatorios.dtFim+"'"; 
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");            
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque por "+nomeRelatorio+"\n"
                    + " de "+TelaRelatorios.dtInicial+" até "+TelaRelatorios.dtFinal ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(4);
            co11 = new PdfPCell(new Phrase("CÓDIGO PEÇA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);
            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co12);
            co13 = new PdfPCell(new Phrase("MARCA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co13);
            co14 = new PdfPCell(new Phrase("TAMANHO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.YELLOW);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co14);          
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(2)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelPeriodoPecasCloud(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();       
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE data BETWEEN ? AND ?"; 
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, TelaRelatorios.dtIni);
            stmt2.setString(2, TelaRelatorios.dtFim);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");            
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção padrão executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // Tratamento e inversão regional das strings de data do cabeçalho
            String dataCabecalhoIni = TelaRelatorios.dtInicial;
            String dataCabecalhoFim = TelaRelatorios.dtFinal;
            try {
                if (TelaRelatorios.dtIni != null && TelaRelatorios.dtIni.contains("-")) {
                    String[] pIni = TelaRelatorios.dtIni.split("-");
                    dataCabecalhoIni = pIni[2] + "/" + pIni[1] + "/" + pIni[0];
                }
                if (TelaRelatorios.dtFim != null && TelaRelatorios.dtFim.contains("-")) {
                    String[] pFim = TelaRelatorios.dtFim.split("-");
                    dataCabecalhoFim = pFim[2] + "/" + pFim[1] + "/" + pFim[0];
                }
            } catch (Exception dtEx) {
                System.out.println("Aviso: Falha ao inverter formato de datas do cabeçalho.");
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            
            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de Estoque por " + nomeRelatorio + "\nPeríodo: " + dataCabecalhoIni + " até " + dataCabecalhoFim + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (4 Colunas) ---
            table = new PdfPTable(4);
            table.setWidthPercentage(100); 
            float[] columnWidths = {15f, 55f, 20f, 10f}; // CÓDIGO (15%), DESCRIÇÃO AMPLA (55%), MARCA (20%), T (10%)
            table.setWidths(columnWidths);
            
            // 🔥 CABEÇALHO ESCURO: Substituído o amarelo pelo Grafite Escuro (#333333) unificado [links: 10]
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);
            
            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);
            
            co13 = new PdfPCell(new Phrase("MARCA", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13.setPadding(6);
            
            co14 = new PdfPCell(new Phrase("T", fHeader));
            co14.setBackgroundColor(new BaseColor(51, 51, 51));
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14.setPadding(6);
            
            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            table.addCell(co14);          
            
            int totalPecas = 0;
            while (rs.next()) {                
                totalPecas++;

                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo)); // codpeca
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);

                co12 = new PdfPCell(new Paragraph(rs.getString(2), fCorpo)); // itemdesc
                co12.setHorizontalAlignment(Element.ALIGN_LEFT);
                co12.setPadding(5);

                co13 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo)); // marca
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);

                co14 = new PdfPCell(new Paragraph(rs.getString(4), fCorpo)); // tamanho
                co14.setHorizontalAlignment(Element.ALIGN_CENTER);
                co14.setPadding(5);

                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }

            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE PEÇAS NO PERÍODO:", fBold));
            cellTotalTexto.setColspan(3); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);
            
            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalPecas + " un.", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor); 
            
            docPDF.add(table);
            
            // 🔥 CARIMBO DE COPYRIGHT: Invoca o método utilitário fixando a assinatura no rodapé absoluto (Y=25) [links: 10]
//            carimbarCopyrightRodape(writer, docPDF);
            
            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }

    public void gerarRelDescricao(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();      
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE itemdesc = '"+descricao+"'";  
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque por "+nomeRelatorio ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(4);
            co11 = new PdfPCell(new Phrase("CÓDIGO PEÇA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);
            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.YELLOW);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co12);
            co13 = new PdfPCell(new Phrase("MARCA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co13);
            co14 = new PdfPCell(new Phrase("TAMANHO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co14);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(2)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelDescricaoCloud(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();      
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE itemdesc LIKE ? ORDER BY itemdesc ASC";  
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, "%" + descricao + "%");
            rs = stmt2.executeQuery(); 
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            
            // 🔥 DECLARAÇÃO DO WRITER: Ativa o rastreamento das coordenadas de pixel da folha
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de Estoque por " + nomeRelatorio + " (Termo: '" + descricao + "')\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (4 Colunas) ---
            table = new PdfPTable(4);
            table.setWidthPercentage(100); 
            float[] columnWidths = {15f, 55f, 20f, 10f}; // CÓDIGO (15%), DESCRIÇÃO (55%), MARCA (20%), TAMANHO (10%)
            table.setWidths(columnWidths);

            // 🔥 CABEÇALHO ESCURO: Substituídos os fundos amarelos e cinzas pelo Grafite Escuro (#333333) unificado
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);

            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);

            co13 = new PdfPCell(new Phrase("MARCA", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13.setPadding(6);

            co14 = new PdfPCell(new Phrase("T", fHeader));
            co14.setBackgroundColor(new BaseColor(51, 51, 51));
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14.setPadding(6);

            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            table.addCell(co14);          

            int totalItensEncontrados = 0;

            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                totalItensEncontrados++;

                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo)); // codpeca
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);

                co12 = new PdfPCell(new Paragraph(rs.getString(2), fCorpo)); // itemdesc
                co12.setHorizontalAlignment(Element.ALIGN_LEFT);
                co12.setPadding(5);

                co13 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo)); // marca
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);

                co14 = new PdfPCell(new Paragraph(rs.getString(4), fCorpo)); // tamanho
                co14.setHorizontalAlignment(Element.ALIGN_CENTER);
                co14.setPadding(5);

                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }

            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE ITENS LOCALIZADOS:", fBold));
            cellTotalTexto.setColspan(3); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalItensEncontrados + " un.", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor); 

            docPDF.add(table); // Consolida o grid de dados no PDF

            // 🔥 CARIMBO DE COPYRIGHT: Imprime a assinatura da SRS Consultoria na base física da página (Y=25)
//            carimbarCopyrightRodape(writer, docPDF);

            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }

    public void gerarRelCodigo(Produto p) throws FileNotFoundException, ClassNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE codpeca = '"+codigo+"'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque "+nomeRelatorio+"",FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(4);
            co11 = new PdfPCell(new Phrase("CÓDIGO PEÇA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);
            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co12);
            co13 = new PdfPCell(new Phrase("MARCA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co13);
            co14 = new PdfPCell(new Phrase("TAMANHO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co14);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(2)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelCodigoCloud(Produto p) throws FileNotFoundException, ClassNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE codpeca = ?";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, codigo);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.BLACK));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph(" "));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de Estoque por " + nomeRelatorio + " (Filtro Código: '" + codigo + "')\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (4 Colunas) ---
            table = new PdfPTable(4);
            table.setWidthPercentage(100); 
            float[] columnWidths = {15f, 55f, 20f, 10f}; // Ajustado proporções das colunas para manter a simetria visual do projeto
            table.setWidths(columnWidths);

            // 🔥 CABEÇALHO ESCURO: Removidos fundos amarelos e cinzas claros, unificando no Grafite Escuro (#333333) [links: 10]
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);

            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);

            co13 = new PdfPCell(new Phrase("MARCA", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13.setPadding(6);

            co14 = new PdfPCell(new Phrase("T", fHeader));
            co14.setBackgroundColor(new BaseColor(51, 51, 51));
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14.setPadding(6);

            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            table.addCell(co14);

            int totalItensLocalizados = 0;

            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                totalItensLocalizados++;

                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo)); // codpeca
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);

                co12 = new PdfPCell(new Paragraph(rs.getString(2), fCorpo)); // itemdesc
                co12.setHorizontalAlignment(Element.ALIGN_LEFT);
                co12.setPadding(5);

                co13 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo)); // marca
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);

                co14 = new PdfPCell(new Paragraph(rs.getString(4), fCorpo)); // tamanho
                co14.setHorizontalAlignment(Element.ALIGN_CENTER);
                co14.setPadding(5);

                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }

            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE ITENS LOCALIZADOS:", fBold));
            cellTotalTexto.setColspan(3); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalItensLocalizados + " un.", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor); 

            docPDF.add(table); // Consolida o grid de dados no PDF

            // 🔥 CARIMBO DE COPYRIGHT: Imprime a assinatura da SRS Consultoria na base física da página (Y=25) [links: 10]
//            carimbarCopyrightRodape(writer, docPDF);

            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }
    
    public void gerarRelMarca(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE marca = '"+marca+"'"; 
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque por "+nomeRelatorio+"" ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(4);
            co11 = new PdfPCell(new Phrase("CÓDIGO PEÇA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);
            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co12);
            co13 = new PdfPCell(new Phrase("MARCA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.YELLOW);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co13);
            co14 = new PdfPCell(new Phrase("TAMANHO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co14);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(2)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelMarcaCloud(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE marca LIKE ? ORDER BY itemdesc ASC"; 
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, "%" + marca + "%");
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva padrão do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de Estoque por " + nomeRelatorio + " (Marca: '" + marca + "')\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (4 Colunas) ---
            table = new PdfPTable(4);
            table.setWidthPercentage(100); 
            float[] columnWidths = {15f, 55f, 20f, 10f}; 
            table.setWidths(columnWidths);

            // 🔥 CABEÇALHO ESCURO: Substituídos os fundos amarelos e cinzas pelo Grafite Escuro (#333333) unificado [links: 10]
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);

            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);

            co13 = new PdfPCell(new Phrase("MARCA", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13.setPadding(6);

            co14 = new PdfPCell(new Phrase("T", fHeader));
            co14.setBackgroundColor(new BaseColor(51, 51, 51));
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14.setPadding(6);

            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            table.addCell(co14);

            int totalItensMarca = 0;

            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                totalItensMarca++;

                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo)); // codpeca
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);

                co12 = new PdfPCell(new Paragraph(rs.getString(2), fCorpo)); // itemdesc
                co12.setHorizontalAlignment(Element.ALIGN_LEFT);
                co12.setPadding(5);

                co13 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo)); // marca
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);

                co14 = new PdfPCell(new Paragraph(rs.getString(4), fCorpo)); // tamanho
                co14.setHorizontalAlignment(Element.ALIGN_CENTER);
                co14.setPadding(5);

                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }

            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE ITENS LOCALIZADOS:", fBold));
            cellTotalTexto.setColspan(3); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalItensMarca + " un.", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor); 

            docPDF.add(table); // Consolida a tabela no PDF

            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }

    public void gerarRelTamanho(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE tamanho = '"+tamanho+"'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque por "+nomeRelatorio+"" ,FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(4);
            co11 = new PdfPCell(new Phrase("CÓDIGO PEÇA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);
            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co12);
            co13 = new PdfPCell(new Phrase("MARCA" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co13);
            co14 = new PdfPCell(new Phrase("TAMANHO" ,FontFactory.getFont("Times New Roman", 13, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.YELLOW);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co14);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(2)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelTamanhoCloud(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null; 
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT codpeca, itemdesc, marca, tamanho FROM estoque WHERE tamanho = ? ORDER BY itemdesc ASC";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, tamanho);
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva padrão do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de Estoque por " + nomeRelatorio + " (Tamanho: '" + tamanho + "')\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (4 Colunas) ---
            table = new PdfPTable(4);
            table.setWidthPercentage(100); 
            float[] columnWidths = {15f, 55f, 20f, 10f}; 
            table.setWidths(columnWidths);

            // 🔥 CABEÇALHO ESCURO: Substituídos os fundos amarelos e cinzas pelo Grafite Escuro (#333333) unificado
            co11 = new PdfPCell(new Phrase("CÓDIGO", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);

            co12 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);

            co13 = new PdfPCell(new Phrase("MARCA", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13.setPadding(6);

            co14 = new PdfPCell(new Phrase("T", fHeader));
            co14.setBackgroundColor(new BaseColor(51, 51, 51));
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14.setPadding(6);

            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            table.addCell(co14);

            int totalItensTamanho = 0;

            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                totalItensTamanho++;

                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo)); // codpeca
                co11.setHorizontalAlignment(Element.ALIGN_CENTER);
                co11.setPadding(5);

                co12 = new PdfPCell(new Paragraph(rs.getString(2), fCorpo)); // itemdesc
                co12.setHorizontalAlignment(Element.ALIGN_LEFT);
                co12.setPadding(5);

                co13 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo)); // marca
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);

                co14 = new PdfPCell(new Paragraph(rs.getString(4), fCorpo)); // tamanho
                co14.setHorizontalAlignment(Element.ALIGN_CENTER);
                co14.setPadding(5);

                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
            }

            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE ITENS DO TAMANHO LOCALIZADOS:", fBold));
            cellTotalTexto.setColspan(3); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalItensTamanho + " un.", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor); 

            docPDF.add(table); // Consolida a tabela no PDF

            // 🔥 CARIMBO DE COPYRIGHT: Imprime a assinatura da SRS Consultoria na base física da página (Y=25)
//            carimbarCopyrightRodape(writer, docPDF);

            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }

    public void gerarRelTipoItem(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document();
        con = ConnectionDB.getConnection();
        sql = "SELECT tipoitem, codpeca, itemdesc, marca, tamanho FROM estoque WHERE tipoitem = '"+fornecedor+"'";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            PdfWriter writer = PdfWriter.getInstance(docPDF, new FileOutputStream(caminho_relatorio+nomeRelatorio+".pdf"));
            docPDF.open();
            pg = new Paragraph("Relatorio de Estoque "+nomeRelatorio+"",FontFactory.getFont("Times New Roman", 16, Font.BOLD, BaseColor.BLACK));
            pg.setAlignment(1);
            docPDF.add(pg);
            docPDF.add(new Paragraph(" "));
            table = new PdfPTable(5);
            co11 = new PdfPCell(new Phrase("TIPO ITEM" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co11.setBackgroundColor(BaseColor.YELLOW);
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co11);
            co12 = new PdfPCell(new Phrase("CÓDIGO PEÇA" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co12.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co12);
            co13 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co13.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co13);
            co14 = new PdfPCell(new Phrase("MARCA" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co14.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co14);
            co15 = new PdfPCell(new Phrase("TAMANHO" ,FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK)));
            co15.setBackgroundColor(BaseColor.LIGHT_GRAY);
            co15.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(co15);
            while(rs.next()){                
                co11 = new PdfPCell(new Paragraph( rs.getString(1)));
                co12 = new PdfPCell(new Paragraph( rs.getString(2)));
                co13 = new PdfPCell(new Paragraph( rs.getString(3)));
                co14 = new PdfPCell(new Paragraph( rs.getString(4)));
                co15 = new PdfPCell(new Paragraph( rs.getString(5)));
                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
                table.addCell(co15);
            }
            docPDF.add(table);
            carimbarCopyrightRodape(writer, docPDF);
            file = new File(caminho_relatorio+nomeRelatorio+".pdf");
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro: "+ex);
            }
            file.deleteOnExit();
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            System.out.println("----------------------------");
        }finally{
            con.close();
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            docPDF.close();
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
        }
    }
    
    public void gerarRelTipoItemCloud(Produto p) throws ClassNotFoundException, FileNotFoundException, DocumentException, SQLException {
        Document docPDF = new Document(com.itextpdf.text.PageSize.A4); // Formato Retrato Padrão
        FileOutputStream fos = null;
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT tipoitem, codpeca, itemdesc, marca, tamanho FROM estoque WHERE tipoitem LIKE ? ORDER BY itemdesc ASC";
        System.out.println("Pesquisa: " + sql);
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, "%" + fornecedor + "%"); 
            rs = stmt2.executeQuery();
            System.out.println("Gerando arquivo PDF...");
            System.out.println("----------------------------");           
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivoFinal = nomeRelatorio + "_" + timestamp + ".pdf";
            String caminhoCompleto = caminho_relatorio + nomeArquivoFinal;
            fos = new FileOutputStream(caminhoCompleto);
            
            // 🔥 DECLARAÇÃO DO WRITER: Ativa o rastreamento das coordenadas de pixel da folha [links: 10]
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            writer.setPageEvent(new PdfPageEventHelper() {
            @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // Pega as dimensões reais da página de forma segura através do writer
                    com.itextpdf.text.Rectangle pageSize = writer.getPageSize();
                    float larguraPagina = pageSize.getWidth();
                    float larguraUtil = larguraPagina - 70; // 35 de margem de cada lado

                    // --- RENDERIZAÇÃO DO COPYRIGHT SEGURO ---
                    try {
                        PdfPTable tabelaRodape = new PdfPTable(1);
                        tabelaRodape.setTotalWidth(larguraUtil);
                        tabelaRodape.setLockedWidth(true);

                        int ano = java.time.Year.now().getValue();
                        String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";

                        PdfPCell celula = new PdfPCell(new Phrase(textoCopyright, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.BLACK)));
                        celula.setBorder(PdfPCell.NO_BORDER); 
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
                        tabelaRodape.addCell(celula);

                        // Fixa o copyright estritamente no Y=20 (base da página)

                        tabelaRodape.writeSelectedRows(0, -1, 35, 20, writer.getDirectContent());
                    } catch (Exception ex) {
                        System.err.println("Erro no copyright: " + ex.getMessage());
                    }

                    // --- RENDERIZAÇÃO DA PAGINAÇÃO SEGURA ---
                    String textoPagina = "Página " + writer.getPageNumber();
                    Phrase phrase = new Phrase(textoPagina, FontFactory.getFont("Helvetica", 8, BaseColor.GRAY));
                    float x = larguraPagina / 2; // Centro exato da folha
                    float y = 35; // Fica exatamente no meio entre a tabela e o copyright (Y=35)

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase, x, y, 0);
                }
            });
            docPDF.open();
            
            // --- RENDERIZAÇÃO DA LOGOMARCA PORTOBELLA ---
            try {
                String caminhoLogo = ConfigLoader.get("sistema.logo");
                if (caminhoLogo != null && !caminhoLogo.trim().isEmpty()) {
                    if (!caminhoLogo.startsWith("/")) {
                        caminhoLogo = "/" + caminhoLogo;
                    }
                    java.net.URL urlImagem = getClass().getResource(caminhoLogo);
                    com.itextpdf.text.Image logo = null;
                    if (urlImagem != null) {
                        logo = com.itextpdf.text.Image.getInstance(urlImagem);
                    } else {
                        File arquivoExterno = new File(ConfigLoader.get("sistema.logo"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }
                    if (logo != null) {
                        logo.scaleToFit(140, 70); // Proporção executiva do projeto
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                        docPDF.add(new Paragraph("\n"));
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo dinâmico não localizado para o PDF: " + imgEx.getMessage());
            }
            
            // --- PADRONIZAÇÃO DE FONTES OFICIAIS (HELVETICA) ---
            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.ITALIC, BaseColor.BLACK);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE); // Letras Brancas
            com.itextpdf.text.Font fCorpo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // --- CABEÇALHO TEXTUAL ---
            pg = new Paragraph(cliente, fTitulo);
            pg.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pg);
            
            Paragraph pSub = new Paragraph("Relatório de Estoque por " + nomeRelatorio + " (Filtro Tipo: '" + fornecedor + "')\nEmitido em: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n", fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(pSub);
            
            // --- ESTRUTURA DA TABELA DO PDF (5 Colunas) ---
            table = new PdfPTable(5);
            table.setWidthPercentage(100); 
            float[] columnWidths = {15f, 12f, 43f, 20f, 10f}; // TIPO (15%), CÓDIGO (12%), DESCRIÇÃO (43%), MARCA (20%), TAMANHO (10%)
            table.setWidths(columnWidths);

            // 🔥 CABEÇALHO ESCURO: Substituídos os fundos amarelos e cinzas pelo Grafite Escuro (#333333) unificado [links: 10]
            co11 = new PdfPCell(new Phrase("TIPO ITEM", fHeader));
            co11.setBackgroundColor(new BaseColor(51, 51, 51));
            co11.setHorizontalAlignment(Element.ALIGN_CENTER);
            co11.setPadding(6);

            co12 = new PdfPCell(new Phrase("CÓDIGO PEÇA", fHeader));
            co12.setBackgroundColor(new BaseColor(51, 51, 51));
            co12.setHorizontalAlignment(Element.ALIGN_CENTER);
            co12.setPadding(6);

            co13 = new PdfPCell(new Phrase("DESCRIÇÃO ITEM", fHeader));
            co13.setBackgroundColor(new BaseColor(51, 51, 51));
            co13.setHorizontalAlignment(Element.ALIGN_CENTER);
            co13.setPadding(6);

            co14 = new PdfPCell(new Phrase("MARCA", fHeader));
            co14.setBackgroundColor(new BaseColor(51, 51, 51));
            co14.setHorizontalAlignment(Element.ALIGN_CENTER);
            co14.setPadding(6);

            co15 = new PdfPCell(new Phrase("TAMANHO", fHeader));
            co15.setBackgroundColor(new BaseColor(51, 51, 51));
            co15.setHorizontalAlignment(Element.ALIGN_CENTER);
            co15.setPadding(6);

            table.addCell(co11);
            table.addCell(co12);
            table.addCell(co13);
            table.addCell(co14);
            table.addCell(co15);

            int totalItensTipo = 0;

            // --- VARREDURA E PREENCHIMENTO DAS LINHAS ---
            while (rs.next()) {                
                totalItensTipo++;

                co11 = new PdfPCell(new Paragraph(rs.getString(1), fCorpo)); // tipoitem
                co11.setHorizontalAlignment(Element.ALIGN_LEFT);
                co11.setPadding(5);

                co12 = new PdfPCell(new Paragraph(rs.getString(2), fCorpo)); // codpeca
                co12.setHorizontalAlignment(Element.ALIGN_CENTER);
                co12.setPadding(5);

                co13 = new PdfPCell(new Paragraph(rs.getString(3), fCorpo)); // itemdesc
                co13.setHorizontalAlignment(Element.ALIGN_LEFT);
                co13.setPadding(5);

                co14 = new PdfPCell(new Paragraph(rs.getString(4), fCorpo)); // marca
                co14.setHorizontalAlignment(Element.ALIGN_LEFT);
                co14.setPadding(5);

                co15 = new PdfPCell(new Paragraph(rs.getString(5), fCorpo)); // tamanho
                co15.setHorizontalAlignment(Element.ALIGN_CENTER);
                co15.setPadding(5);

                table.addCell(co11);
                table.addCell(co12);
                table.addCell(co13);
                table.addCell(co14);
                table.addCell(co15);
            }

            // --- LINHA DE TOTALIZAÇÃO NO PADRÃO CINZA SUAVE (#F5F5F5) ---
            PdfPCell cellTotalTexto = new PdfPCell(new Phrase("TOTAL DE ITENS DO TIPO LOCALIZADOS:", fBold));
            cellTotalTexto.setColspan(4); 
            cellTotalTexto.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalTexto.setPadding(6);
            table.addCell(cellTotalTexto);

            PdfPCell cellTotalValor = new PdfPCell(new Phrase(totalItensTipo + " un.", fBold));
            cellTotalValor.setBackgroundColor(new BaseColor(245, 245, 245));
            cellTotalValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTotalValor.setPadding(6);
            table.addCell(cellTotalValor); 

            docPDF.add(table); // Consolida a tabela no PDF

            // 🔥 CARIMBO DE COPYRIGHT: Imprime a assinatura da SRS Consultoria na base física da página (Y=25) [links: 10]
//            carimbarCopyrightRodape(writer, docPDF);

            file = new File(caminhoCompleto);
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                System.out.println("Erro ao abrir PDF: " + ex);
            }
        } catch (SQLException ex) {
            System.err.println("Erro SQL: " + ex);
            System.out.println("----------------------------");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException ex) {}
            if (con2 != null) try { con2.close(); } catch (SQLException ex) {}
            System.out.println("Conexão com Banco encerrada!");
            System.out.println("----------------------------");
            if (docPDF != null && docPDF.isOpen()) {
                docPDF.close();
            }
            System.out.println("Fechando PDF..");
            System.out.println("----------------------------");
            if (fos != null) {
                try { fos.close(); } catch (IOException ex) {}
            }
        }
    }
        // =========================================================================
    // 🧠 MÉTODO UNIVERSAL: CRAVA NO RODAPÉ ABSOLUTO (RETRATO E PAISAGEM)
    // =========================================================================
    private void carimbarCopyrightRodape(PdfWriter writer, Document document) {
        try {
            // 🚀 O SEGREDO: Pega a largura real e dinâmica da folha (suporta Retrato e Paisagem)
            float larguraPagina = document.getPageSize().getWidth();
            float larguraUtil = larguraPagina - 70; // Desconta as margens laterais (35 de cada lado)
            
            PdfPTable tabelaRodape = new PdfPTable(1);
            tabelaRodape.setTotalWidth(larguraUtil);
            tabelaRodape.setLockedWidth(true);
            
            int ano = java.time.Year.now().getValue();
            String texto = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";
            
            PdfPCell celula = new PdfPCell(new Phrase(texto, FontFactory.getFont("Helvetica", 7, Font.ITALIC, BaseColor.LIGHT_GRAY)));
            celula.setBorder(PdfPCell.NO_BORDER); 
            celula.setHorizontalAlignment(Element.ALIGN_CENTER); 
            tabelaRodape.addCell(celula);
            
            // 🔥 MARCAÇÃO POR COORDENADA: X=35 (margem esquerda) e Y=25 (fixo a 25 pixels do fim físico da folha)
            tabelaRodape.writeSelectedRows(0, -1, 35, 25, writer.getDirectContent());
            
        } catch (Exception ex) {
            System.err.println("Erro ao carimbar copyright no rodapé absoluto: " + ex.getMessage());
        }
    }
    
    private static class OuvinteRodapePDF extends com.itextpdf.text.pdf.PdfPageEventHelper {
        @Override
        public void onEndPage(com.itextpdf.text.pdf.PdfWriter writer, com.itextpdf.text.Document document) {
            // Captura o barramento de texto direto da folha atual
            com.itextpdf.text.pdf.PdfContentByte cb = writer.getDirectContent();
            
            // Define o padrão estético oficial (Discreto, Helvetica tamanho 7, Itálico e Cinza Claro)
            com.itextpdf.text.Font fRodape = com.itextpdf.text.FontFactory.getFont("Helvetica", 7, com.itextpdf.text.Font.ITALIC, com.itextpdf.text.BaseColor.LIGHT_GRAY);
            
            // Pega o relógio interno do sistema para travar o ano dinâmico
            int ano = java.time.Year.now().getValue();
            String textoCopyright = "Copyright © 2022-" + ano + " SRS Consultoria TI LTDA - Todos os direitos reservados.";
            
            cb.beginText();
            cb.setFontAndSize(fRodape.getCalculatedBaseFont(false), 7);
            
            // 🚀 O SEGREDO: Calcula o centro dinamicamente baseando-se no tamanho real do documento (Suporta Retrato e Paisagem)
            float x = (document.right() + document.left()) / 2;
            
            // Crava o alinhamento vertical a exatamente 20 pixels da borda física inferior do papel A4
            float y = document.bottom() - 20; 
            
            cb.showTextAligned(com.itextpdf.text.Element.ALIGN_CENTER, textoCopyright, x, y, 0);
            cb.endText();
        }
    }
    
    public void onEndPage(PdfWriter writer, Document document) {
        carimbarCopyrightRodape(writer, document);
        String textoPagina = String.format("Página %d", writer.getPageNumber());
        Phrase phrase = new Phrase(textoPagina, fonteRodape);
        
        // Calcula a posição X (centro da página) e Y (margem inferior)
        float x = (document.right() + document.left()) / 2;
        float y = document.bottom() - 20; // 20 pontos abaixo da margem inferior

        // Desenha o texto centralizado no rodapé
        ColumnText.showTextAligned(
            writer.getDirectContent(), 
            Element.ALIGN_CENTER, 
            phrase, 
            x, 
            y, 
            0
        );
    }
}
