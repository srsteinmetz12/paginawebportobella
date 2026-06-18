/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;
import java.io.IOException;
import models.Etiqueta;

public class GeradorEtiquetasCorreios {
    private final String usuario = ConfigLoader.get("sistema.user");
    private final String inicio_caminho = ConfigLoader.get("sistema.inicio_caminho_relatorios");
    private final String final_caminho = ConfigLoader.get("sistema.final_caminho_relatorios");
    private final String caminho_pasta = inicio_caminho + usuario + final_caminho;

    public void gerarEtiquetaEnvio(Etiqueta dest, Etiqueta remetente) {
        // Define o tamanho da folha A6 exato para 1 etiqueta padrão (105mm x 148mm = 298pt x 420pt)
        Rectangle tamanhoEtiqueta = new Rectangle(298, 420);
        Document docPDF = new Document(tamanhoEtiqueta, 10, 10, 10, 10);
        FileOutputStream fos = null;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeArquivo = "Etiqueta_Correios_" + timestamp + ".pdf";
        File arquivoFinal = new File(caminho_pasta, nomeArquivo);

        try {
            fos = new FileOutputStream(arquivoFinal);
            PdfWriter writer = PdfWriter.getInstance(docPDF, fos);
            docPDF.open();

            // Tipografia Padrão Correios
            Font fTitulo = FontFactory.getFont("Arial", 11, Font.BOLD, BaseColor.BLACK);
            Font fBold = FontFactory.getFont("Arial", 9, Font.BOLD, BaseColor.BLACK);
            Font fCorpo = FontFactory.getFont("Arial", 8, Font.BOLD, BaseColor.BLACK);
            Font fCep = FontFactory.getFont("Arial", 14, Font.BOLD, BaseColor.BLACK);

            // --- BLOCO DO DESTINATÁRIO (BORDA GROSSA) ---
            PdfPTable tabelaDest = new PdfPTable(1);
            tabelaDest.setWidthPercentage(100);

            PdfPCell cHeader = new PdfPCell(new Phrase("DESTINATÁRIO", fTitulo));
            cHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cHeader.setPadding(4);
            cHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabelaDest.addCell(cHeader);

            // Conteúdo do Endereço
            StringBuilder endGrid = new StringBuilder();
            endGrid.append(dest.getNome().toUpperCase()).append("\n")
                   .append(dest.getEndereco()).append(", No ").append(dest.getNumero()).append("\n");
            if(dest.getComplemento() != null && !dest.getComplemento().isEmpty()){
                endGrid.append(dest.getComplemento()).append("\n");
            }
            endGrid.append(dest.getBairro()).append("\n")
                   .append(dest.getCidade()).append(" / ").append(dest.getUf()).append("\n");

            PdfPCell cCorpo = new PdfPCell(new Phrase(endGrid.toString(), fBold));
            cCorpo.setPadding(6);
            cCorpo.setBorder(Rectangle.NO_BORDER);
            tabelaDest.addCell(cCorpo);

            // Bloco Destacado do CEP
            PdfPCell cCep = new PdfPCell(new Phrase("CEP: " + dest.getCep(), fCep));
            cCep.setPadding(5);
            cCep.setHorizontalAlignment(Element.ALIGN_LEFT);
            cCep.setBorder(Rectangle.NO_BORDER);
            tabelaDest.addCell(cCep);

            // --- GERAÇÃO DO CÓDIGO DE BARRAS DO CEP (Padrão de triagem linear Correios) ---
            Barcode128 barcodeCep = new Barcode128();
            barcodeCep.setCodeType(Barcode128.CODE128);
            // Limpa hífens do CEP para gerar o código de barras puro
            barcodeCep.setCode(dest.getCep().replace("-", "").trim()); 
            barcodeCep.setFont(null); // Oculta o texto abaixo das barras para ficar limpo
            com.itextpdf.text.Image imgBarraCep = barcodeCep.createImageWithBarcode(writer.getDirectContent(), null, null);
            imgBarraCep.setAlignment(Element.ALIGN_LEFT);
            imgBarraCep.scaleToFit(160, 35);
            
            PdfPCell cBarra = new PdfPCell(imgBarraCep);
            cBarra.setPadding(5);
            cBarra.setBorder(Rectangle.NO_BORDER);
            tabelaDest.addCell(cBarra);

            docPDF.add(tabelaDest);

            // Separador Visual Estilo Serrilhado
            docPDF.add(new Paragraph("\n---------------------------------------------------------------------------------\n", fCorpo));

            // --- BLOCO DO REMETENTE (RODAPÉ MENOR) ---
            PdfPTable tabelaRem = new PdfPTable(1);
            tabelaRem.setWidthPercentage(100);

            PdfPCell cHeaderRem = new PdfPCell(new Phrase("REMETENTE", fBold));
            cHeaderRem.setBorder(Rectangle.NO_BORDER);
            tabelaRem.addCell(cHeaderRem);

            StringBuilder remGrid = new StringBuilder();
            remGrid.append(remetente.getNome().toUpperCase()).append("\n")
                   .append(remetente.getEndereco()).append(", ").append(remetente.getNumero()).append(" - ").append(remetente.getComplemento()).append("\n")                   
                   .append(remetente.getBairro()).append(" - CEP: ").append(remetente.getCep()).append("\n")
                   .append(remetente.getCidade()).append(" / ").append(remetente.getUf());

            PdfPCell cCorpoRem = new PdfPCell(new Phrase(remGrid.toString(), fCorpo));
            cCorpoRem.setBorder(Rectangle.NO_BORDER);
            cCorpoRem.setPaddingTop(4);
            tabelaRem.addCell(cCorpoRem);

            docPDF.add(tabelaRem);

            docPDF.close();

            // Dispara visualização instantânea na tela para impressão física
            if (arquivoFinal.exists()) {
                Desktop.getDesktop().open(arquivoFinal);
            }

        } catch (DocumentException | IOException ex) {
            System.err.println("Erro ao construir etiqueta iText: " + ex.getMessage());
        } finally {
            if (fos != null) {
                try { fos.close(); 
                } catch (IOException ex) {
                    System.err.println("Erro: "+ex);
                }
            }
        }
    }
}
