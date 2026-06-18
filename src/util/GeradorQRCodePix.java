package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class GeradorQRCodePix {
    
    // ⚠️ COLOQUE SUA CHAVE PIX AQUI
    // Pode ser: CPF, CNPJ, Email, Celular (com DDD), ou Chave Aleatória
    private static final String CHAVE_PIX = "portobella.brecho@gmail.com"; // Substitua pela sua
    private static final String NOME_RECEBEDOR = "Vanderleia Vieira Moraes Lemos Steinmetz 99393557004";
    
    // Cidade onde a conta foi aberta (geralmente BRASIL)
    private static final String CIDADE = "BRASIL";
    
    /**
     * Gera o código Pix Copia e Cola (formato BR Code)
     * @param valor
     * @param descricao
     * @return 
     */
    public static String gerarCodigoPix(double valor, String descricao) {
        // Formata o valor com 2 casas decimais
        String valorFormatado = String.format("%.2f", valor).replace(",", ".");
        
        // Monta o payload Pix (formato BR Code)
        StringBuilder pix = new StringBuilder();
        
        // 1. Payload Format Indicator
        pix.append("000201");
        
        // 2. Merchant Account Information (GUI + Chave Pix)
        pix.append("26330014BR.GOV.BCB.PIX0111").append(CHAVE_PIX);
        
        // 3. Merchant Category Code (sempre 0000 para Pix)
        pix.append("52040000");
        
        // 4. Transaction Currency (986 = BRL)
        pix.append("5303986");
        
        // 5. Transaction Amount
        if (valor > 0) {
            pix.append("54").append(String.format("%02d", valorFormatado.length())).append(valorFormatado);
        }
        
        // 6. Country Code (BR = Brasil)
        pix.append("5802BR");
        
        // 7. Merchant Name
        pix.append("59").append(String.format("%02d", NOME_RECEBEDOR.length())).append(NOME_RECEBEDOR);
        
        // 8. Merchant City
        pix.append("60").append(String.format("%02d", CIDADE.length())).append(CIDADE);
        
        // 9. Additional Data Field Template (descrição opcional)
        if (descricao != null && !descricao.isEmpty()) {
            String desc = descricao.length() > 20 ? descricao.substring(0, 20) : descricao;
            pix.append("62").append(String.format("%02d", desc.length() + 4)).append("0504").append(desc);
        }
        
        // 10. CRC16 (checksum)
        String crc = calcularCRC16(pix.toString());
        pix.append("6304").append(crc);
        
        return pix.toString();
    }
    
    /**
     * Calcula o CRC16 do payload Pix
     */
    private static String calcularCRC16(String payload) {
        int crc = 0xFFFF;
        byte[] bytes = payload.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc = crc << 1;
                }
            }
        }
        
        return String.format("%04X", crc & 0xFFFF);
    }
    
    /**
     * Gera a imagem do QR Code e salva como arquivo
     * @param codigoPix
     * @param caminhoArquivo
     * @param tamanho
     * @throws java.lang.Exception
     */
    public static void gerarQRCode(String codigoPix, String caminhoArquivo, int tamanho) throws Exception {
        @SuppressWarnings("UseOfObsoleteCollectionType")
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);
        
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix = writer.encode(codigoPix, BarcodeFormat.QR_CODE, tamanho, tamanho, hints);
        
        // Converte para imagem
        BufferedImage image = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < tamanho; x++) {
            for (int y = 0; y < tamanho; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        
        // Salva como PNG
        File arquivo = new File(caminhoArquivo);
        arquivo.getParentFile().mkdirs();
        ImageIO.write(image, "PNG", arquivo);
        
        System.out.println("✅ QR Code gerado: " + caminhoArquivo);
    }
    
    /**
     * Gera QR Code e retorna o código Pix
     * @param valor
     * @param descricao
     * @param caminhoArquivo
     * @return 
     * @throws java.lang.Exception
     */
    public static String gerarQRCodePix(double valor, String descricao, String caminhoArquivo) throws Exception {
        String codigoPix = gerarCodigoPix(valor, descricao);
        gerarQRCode(codigoPix, caminhoArquivo, 300);
        
        System.out.println("📋 Código Pix Copia e Cola:");
        System.out.println(codigoPix);
        
        return codigoPix;
    }
    
    // ==========================================
    // TESTE
    // ==========================================
    public static void main(String[] args) {
        try {
            // Gera QR Code para um produto
            double valor = 89.90;
            String descricao = "BLUSA SEDA PRETA";
            String caminho = "C:\\Users\\DBC\\Documents\\estoqueVitrineWeb\\qr_pix_" + System.currentTimeMillis() + ".png";
            
            String codigoPix = gerarQRCodePix(valor, descricao, caminho);
            
            System.out.println("✅ QR Code gerado com sucesso!");
            System.out.println("📁 Arquivo: " + caminho);
            
        } catch (Exception ex) {
            System.err.println("Erro: "+ex);
        }
    }
}