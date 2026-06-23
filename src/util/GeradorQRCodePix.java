//package util;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.util.Hashtable;
//import javax.imageio.ImageIO;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.EncodeHintType;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.common.BitMatrix;
//
//public class GeradorQRCodePix {
//    
//    // ==========================================
//    // CONFIGURAÇÕES - ALTERE AQUI!
//    // ==========================================
//    private static final String CHAVE_PIX = "portobella.brecho@gmail.com";
//    private static final String NOME_RECEBEDOR = "Vanderleia Vieira";
//    private static final String CIDADE = "PORTO ALEGRE";
//    
//    // ==========================================
//    // GERAR PAYLOAD PIX COMPLETO (VERSÃO CORRIGIDA)
//    // ==========================================
//    public static String gerarPayloadPix(double valor, String pedidoId) {
//        try {
//            System.out.println("🔧 Gerando payload Pix...");
//            System.out.println("💰 Valor: R$ " + valor);
//            System.out.println("📝 Pedido ID: " + pedidoId);
//            
//            // ==========================================
//            // 1. FORMATAR VALOR (CORRIGIDO)
//            // ==========================================
//            String valorFormatado = String.format("%.2f", valor);
//            // ✅ 95.80 -> "95.80" (mantém o ponto!)
//            
//            // ==========================================
//            // 2. CONSTRUIR PAYLOAD (PADRÃO BR CODE)
//            // ==========================================
//            StringBuilder payload = new StringBuilder();
//            
//            // 1. Payload Format Indicator (sempre 000201)
//            payload.append("000201");
//            
//            // 2. Merchant Account Information
//            String gui = "0014BR.GOV.BCB.PIX";
//            String chavePix = "01" + String.format("%02d", CHAVE_PIX.length()) + CHAVE_PIX;
//            String merchantInfo = gui + chavePix;
//            payload.append("26");
//            payload.append(String.format("%02d", merchantInfo.length()));
//            payload.append(merchantInfo);
//            
//            // 3. Merchant Category Code (sempre 0000 para Pix)
//            payload.append("52040000");
//            
//            // 4. Transaction Currency (986 = BRL)
//            payload.append("5303986");
//            
//            // 5. Transaction Amount (CORRIGIDO - mantém o ponto)
//            if (valor > 0) {
//                payload.append("54");
//                payload.append(String.format("%02d", valorFormatado.length()));
//                payload.append(valorFormatado); // ✅ Agora é "95.80"
//            }
//            
//            // 6. Country Code (BR = Brasil)
//            payload.append("5802BR");
//            
//            // 7. Merchant Name (máximo 25 caracteres)
//            String nome = NOME_RECEBEDOR;
//            if (nome.length() > 25) {
//                nome = nome.substring(0, 25);
//            }
//            payload.append("59");
//            payload.append(String.format("%02d", nome.length()));
//            payload.append(nome);
//            
//            // 8. Merchant City (máximo 15 caracteres)
//            String cidade = CIDADE;
//            if (cidade.length() > 15) {
//                cidade = cidade.substring(0, 15);
//            }
//            payload.append("60");
//            payload.append(String.format("%02d", cidade.length()));
//            payload.append(cidade);
//            
//            // 9. Additional Data Field (TXID)
//            String txid = pedidoId;
//            if (txid.length() > 25) {
//                txid = txid.substring(0, 25);
//            }
//            String additionalData = "05" + String.format("%02d", txid.length()) + txid;
//            payload.append("62");
//            payload.append(String.format("%02d", additionalData.length()));
//            payload.append(additionalData);
//            
//            // ==========================================
//            // 3. CALCULAR CRC16 (CORRIGIDO)
//            // ==========================================
//            String payloadSemCRC = payload.toString();
//            String payloadParaCRC = payloadSemCRC + "6304";
//            String crc16 = calcularCRC16(payloadParaCRC);
//            
//            payload.append("6304");
//            payload.append(crc16);
//            
//            String payloadFinal = payload.toString();
//            
//            System.out.println("✅ Payload Pix gerado com sucesso!");
//            System.out.println("📋 Payload: " + payloadFinal);
//            System.out.println("📏 Tamanho: " + payloadFinal.length());
//            System.out.println("✅ Começa com 000201? " + payloadFinal.startsWith("000201"));
//            System.out.println("✅ Tem BR.GOV.BCB.PIX? " + payloadFinal.contains("BR.GOV.BCB.PIX"));
//            System.out.println("✅ Tem 6304? " + payloadFinal.contains("6304"));
//            
//            return payloadFinal;
//            
//        } catch (Exception e) {
//            System.err.println("❌ Erro ao gerar payload Pix: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
//    
//    // ==========================================
//    // CALCULAR CRC16 (CORRIGIDO - ALGORITMO PADRÃO)
//    // ==========================================
//    private static String calcularCRC16(String input) {
//        try {
//            int crc = 0xFFFF;
//            byte[] bytes = input.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
//            
//            for (byte b : bytes) {
//                crc ^= (b & 0xFF) << 8; // ✅ CORRIGIDO: desloca byte para MSB
//                for (int i = 0; i < 8; i++) {
//                    if ((crc & 0x8000) != 0) { // ✅ CORRIGIDO: verifica MSB
//                        crc = (crc << 1) ^ 0x1021; // ✅ Polinômio correto (MSB-first)
//                    } else {
//                        crc = crc << 1;
//                    }
//                }
//            }
//            
//            String result = String.format("%04X", crc & 0xFFFF);
//            System.out.println("✅ CRC16 calculado: " + result);
//            return result;
//            
//        } catch (Exception e) {
//            System.err.println("❌ Erro ao calcular CRC16: " + e.getMessage());
//            return "0000";
//        }
//    }
//    
//    // ==========================================
//    // GERAR QR CODE COMO IMAGEM
//    // ==========================================
//    public static void gerarQRCode(String codigoPix, String caminhoArquivo, int tamanho) throws Exception {
//        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
//        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
//        hints.put(EncodeHintType.MARGIN, 2);
//        
//        MultiFormatWriter writer = new MultiFormatWriter();
//        BitMatrix matrix = writer.encode(codigoPix, BarcodeFormat.QR_CODE, tamanho, tamanho, hints);
//        
//        BufferedImage image = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_RGB);
//        for (int x = 0; x < tamanho; x++) {
//            for (int y = 0; y < tamanho; y++) {
//                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
//            }
//        }
//        
//        File arquivo = new File(caminhoArquivo);
//        arquivo.getParentFile().mkdirs();
//        ImageIO.write(image, "PNG", arquivo);
//        
//        System.out.println("✅ QR Code gerado: " + caminhoArquivo);
//    }
//    
//    // ==========================================
//    // MÉTODO MAIN PARA TESTE
//    // ==========================================
//    public static void main(String[] args) {
//        try {
//            System.out.println("========== TESTE PIX ==========");
//            
//            double valor = 95.80;
//            String pedidoId = String.valueOf(System.currentTimeMillis());
//            
//            String payload = gerarPayloadPix(valor, pedidoId);
//            
//            if (payload != null) {
//                System.out.println("\n✅ Payload gerado com sucesso!");
//                System.out.println("📋 Payload: " + payload);
//                System.out.println("📏 Tamanho: " + payload.length());
//                
//                // Gera QR Code para teste
//                gerarQRCode(payload, "qrcode_pix.png", 400);
//            }
//            
//        } catch (Exception e) {
//            System.err.println("❌ Erro: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}