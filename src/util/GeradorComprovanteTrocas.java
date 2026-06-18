package util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Rectangle;
import dao.TrocasDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Trocas;

public class GeradorComprovanteTrocas {
    // Define o tamanho da página estilo cupom/recibo (Largura: 300 pontos, Altura adaptável)
    Rectangle tamanhoCupom = new Rectangle(300, 500);
    Document docPDF = new Document(tamanhoCupom, 15, 15, 15, 15); // Margens estreitas
    FileOutputStream fos = null;
    TrocasDAO tdao = new TrocasDAO();
    Trocas t = new Trocas(); 

    // Timestamp para arquivo exclusivo
    public String usuario = ConfigLoader.get("sistema.user");
    private final String inicio_caminho_relatorio = ConfigLoader.get("sistema.inicio_caminho_relatorios");
    private final String final_caminho_relatorio = ConfigLoader.get("sistema.final_caminho_relatorios");
    private final String caminho_relatorio = inicio_caminho_relatorio+usuario+final_caminho_relatorio;
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String nomeArquivo = "Vale_Troca_" + t.getId() + "_" + timestamp + ".pdf";
    String caminhoCompleto = caminho_relatorio + nomeArquivo;

    public void emitirCupomPDF(Trocas t) {
        // Define o tamanho da página estilo cupom/recibo térmico (Largura: 300pt)
        Rectangle tamanhoCupom = new Rectangle(300, 500);
        Document docPDF = new Document(tamanhoCupom, 15, 15, 15, 15); 
        FileOutputStream fos = null;

        // Timestamp para garantir arquivo exclusivo e livre de travas de visualização
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeArquivo = "Vale_Troca_" + t.getId() + "_" + timestamp + ".pdf";
        String caminhoCompleto = caminho_relatorio + nomeArquivo;

        try {
            fos = new FileOutputStream(caminhoCompleto);
            PdfWriter.getInstance(docPDF, fos);
            docPDF.open();

            // 1. CARREGAMENTO DO LOGOTIPO DINÂMICO VIA CONFIGLOADER
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
                        File arquivoExterno = new File(ConfigLoader.get("sistema.favicon"));
                        if (arquivoExterno.exists()) {
                            logo = com.itextpdf.text.Image.getInstance(arquivoExterno.getAbsolutePath());
                        }
                    }

                    if (logo != null) {
                        logo.scaleToFit(50, 50);
                        logo.setAlignment(Element.ALIGN_CENTER);
                        docPDF.add(logo);
                    }
                }
            } catch (DocumentException | IOException imgEx) {
                System.out.println("Aviso: Logotipo não carregado no recibo: " + imgEx.getMessage());
            }

            // 2. TIPOGRAFIA DO COMPROVANTE (Fontes em Negrito Compactas)
            Font fonteTitulo = FontFactory.getFont("Times New Roman", 12, Font.BOLD, BaseColor.BLACK);
            Font fonteSub = FontFactory.getFont("Times New Roman", 9, Font.BOLD, BaseColor.BLACK);
            Font fonteDestaque = FontFactory.getFont("Times New Roman", 11, Font.BOLD, BaseColor.BLACK);
            Font fonteCorpo = FontFactory.getFont("Times New Roman", 9, Font.BOLD, BaseColor.BLACK);

            Paragraph p1 = new Paragraph(ConfigLoader.get("sistema.nome_cliente").toUpperCase(), fonteTitulo);
            p1.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(p1);

            Paragraph p2 = new Paragraph("COMPROVANTE DE VALE-TROCA\n--------------------------------------------", fonteSub);
            p2.setAlignment(Element.ALIGN_CENTER);
            docPDF.add(p2);

            // 3. MONTAGEM DO GRID DE INFORMAÇÕES CONTÁBEIS (2 COLUNAS)
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            float[] columnWidths = {40f, 60f};
            table.setWidths(columnWidths);

            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");

            // Injeta as linhas contábeis lendo os dados reais da transação passada por parâmetro
            adicionarLinhaRecibo(table, "Nº CONTROLE:", String.valueOf(t.getId()), fonteDestaque, fonteDestaque);
            
            // Tratamento preventivo de NullPointer para a data
            String dataFormatada = (t.getDataTroca() != null) ? fmt.format(t.getDataTroca()) : fmt.format(new java.util.Date());
            adicionarLinhaRecibo(table, "DATA EMISSÃO:", dataFormatada, fonteCorpo, fonteCorpo);
            
            adicionarLinhaRecibo(table, "CLIENTE:", t.getNomeCliente(), fonteCorpo, fonteCorpo);
            adicionarLinhaRecibo(table, "PEÇA DEVOLVIDA:", t.getPecaTroca(), fonteCorpo, fonteCorpo);
            adicionarLinhaRecibo(table, "VALOR DA PEÇA:", String.format("R$ %.2f", t.getPecaValor()), fonteCorpo, fonteCorpo);
            
            // Linha de Destaque cinza: Saldo disponível para o brechó abater
            PdfPCell cTexto = new PdfPCell(new Phrase("CRÉDITO DISPONÍVEL:", fonteDestaque));
            cTexto.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cTexto.setPadding(5);
            cTexto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            PdfPCell cValor = new PdfPCell(new Phrase(String.format("R$ %.2f", t.getCreditoCliente()), fonteDestaque));
            cValor.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cValor.setPadding(5);
            cValor.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            table.addCell(cTexto);
            table.addCell(cValor);

            docPDF.add(table);

            // 4. OBSERVACÕES E ASSINATURA DE AUDITORIA
            docPDF.add(new Paragraph(" "));
            Paragraph pObs = new Paragraph("Motivo/Obs: " + (t.getObs() != null ? t.getObs() : "Nenhuma"), fonteSub);
            docPDF.add(pObs);

            docPDF.add(new Paragraph("--------------------------------------------\n"
                    + "Este vale possui validade de 90 dias.\n"
                    + "Apresente este cupom no caixa na sua próxima compra.\n\n\n"
                    + "___________________________________\n"
                    + "Assinatura do Operador de Caixa", fonteSub));

            docPDF.close(); // Fecha o documento fisicamente

            // 5. DISPARADOR AUTOMÁTICO DE IMPRESSÃO VISUAL
            File file = new File(caminhoCompleto);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }

        } catch (DocumentException | IOException ex) {
            Logger.getLogger(GeradorComprovanteTrocas.class.getName()).log(Level.SEVERE, "Erro ao construir PDF do cupom", ex);
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (IOException e) { System.err.println("Erro ao fechar stream: " + e.getMessage()); }
            }
        }
    }
    
    private void adicionarLinhaRecibo(PdfPTable table, String chave, String valor, Font fChave, Font fValor) {
        PdfPCell cellChave = new PdfPCell(new Phrase(chave, fChave));
        cellChave.setBorder(Rectangle.NO_BORDER);
        cellChave.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellChave.setPadding(3);
        
        PdfPCell cellValor = new PdfPCell(new Phrase(valor != null ? valor : "", fValor));
        cellValor.setBorder(Rectangle.NO_BORDER);
        cellValor.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellValor.setPadding(3);
        
        table.addCell(cellChave);
        table.addCell(cellValor);
    }
}
