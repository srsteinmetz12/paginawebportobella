package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import io.nayuki.qrcodegen.QrCode;
import views.TelaEstoque;

public class GeradorQRCode extends javax.swing.JFrame {
    
//    public static void main(String[] args) throws IOException {
//		genQRCode();		
//    }

//    public void genQRCode() throws IOException {
//        String dadosQRCode = TelaEstoque.codigoDoItem + " " + TelaEstoque.precoDoItem;          // User-supplied Unicode text
//        QrCode.Ecc errCorLvl = QrCode.Ecc.LOW;  // Error correction level
//
//        QrCode qr = QrCode.encodeText(dadosQRCode, errCorLvl);  // Make the QR Code symbol
//        
//        BufferedImage imagemQRCode = toImage(qr, 10, 4);          // Convert to bitmap image
//        File imgFile = new File("C:\\Users\\DBC\\Documents\\Barcodes\\" + dadosQRCode + ".png");   // File path for output
//        
//        // 🔥 TRAVA DE SEGURANÇA JAVASE: Garante a criação física da pasta Barcodes caso ela não exista em disco
//        if (imgFile.getParentFile() != null && !imgFile.getParentFile().exists()) {
//            System.out.println("Criando pasta de etiquetas ausente em background: " + imgFile.getParentFile().getAbsolutePath());
//            imgFile.getParentFile().mkdirs(); 
//        }
//        
//        ImageIO.write(imagemQRCode, "png", imgFile);              // Write image to file

//        String svg = toSvgString(qr, 4, "#FFFFFF", "#000000");  // Convert to SVG XML code
//        File svgFile = new File("C:\\Users\\DBC\\Documents\\Barcodes\\hello-world-QR.svg");          // File path for output
//        Files.write(svgFile.toPath(),                           // Write image to file
//                svg.getBytes(StandardCharsets.UTF_8));
        
//    }
    
    

//    private static BufferedImage toImage(QrCode qr, int scale, int border) {
//        return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
//    }
//    
//    private static BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
//        Objects.requireNonNull(qr);
//        if (scale <= 0 || border < 0)
//                throw new IllegalArgumentException("Value out of range");
//        if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
//                throw new IllegalArgumentException("Scale or border too large");
//
//        BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
//        for (int y = 0; y < result.getHeight(); y++) {
//                for (int x = 0; x < result.getWidth(); x++) {
//                        boolean color = qr.getModule(x / scale - border, y / scale - border);
//                        result.setRGB(x, y, color ? darkColor : lightColor);
//                }
//        }
//        return result;
//    }
//    
//    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
//    private static String toSvgString(QrCode qr, int border, String lightColor, String darkColor) {
//        Objects.requireNonNull(qr);
//        Objects.requireNonNull(lightColor);
//        Objects.requireNonNull(darkColor);
//        if (border < 0)
//                throw new IllegalArgumentException("Border must be non-negative");
//        long brd = border;
//        StringBuilder sb = new StringBuilder()
//                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
//                .append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n")
//                .append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 %1$d %1$d\" stroke=\"none\">\n",
//                        qr.size + brd * 2))
//                .append("\t<rect width=\"100%\" height=\"100%\" fill=\"" + lightColor + "\"/>\n")
//                .append("\t<path d=\"");
//        for (int y = 0; y < qr.size; y++) {
//                for (int x = 0; x < qr.size; x++) {
//                        if (qr.getModule(x, y)) {
//                                if (x != 0 || y != 0)
//                                        sb.append(" ");
//                                sb.append(String.format("M%d,%dh1v1h-1z", x + brd, y + brd));
//                        }
//                }
//        }
//        return sb
//                .append("\" fill=\"" + darkColor + "\"/>\n")
//                .append("</svg>\n")
//                .toString();
//    }

//    public class GeradorQRCode { // REMOVIDO o extends JFrame desnecessário 🧹

        /**
         * Motor de geração dinâmica de QR Code para as etiquetas do brechó.
         * @param dados O texto/código que será criptografado no QR Code.
         * @param pastaDestino O diretório dinâmico configurado no sistema.
         * @param nomeArquivo O nome amigável do arquivo .png final.
         * @return Retorna a imagem BufferedImage para renderizar direto na Label do Swing.
     * @throws java.io.IOException
         */
    public static BufferedImage criarQR(String dados, String pastaDestino, String nomeArquivo) throws IOException {
        Objects.requireNonNull(dados, "Os dados do QR Code não podem ser nulos!");

        QrCode.Ecc errCorLvl = QrCode.Ecc.LOW; // Nível de correção de erro padrão
        QrCode qr = QrCode.encodeText(dados, errCorLvl); // Processa os dados de forma isolada

        // Converte o código gerado em uma imagem bitmap (escala 10, borda 4)
        BufferedImage imagemQRCode = toImage(qr, 10, 4); 

        // Substitui o caminho absoluto fixo C:\ pelo diretório inteligente do seu projeto 📁
        File imgFile = new File(pastaDestino + File.separator + nomeArquivo + ".png");
        System.out.println("Arquivo criando em: "+pastaDestino + File.separator + nomeArquivo + ".png");

        // Trava de Segurança JAVASE: Cria as pastas físicas dinamicamente caso não existam
        if (imgFile.getParentFile() != null && !imgFile.getParentFile().exists()) {
            System.out.println("Criando estrutura de diretórios em background: " + imgFile.getParentFile().getAbsolutePath());
            imgFile.getParentFile().mkdirs(); 
        }

        // Grava fisicamente a etiqueta em disco
        ImageIO.write(imagemQRCode, "png", imgFile); 

        return imagemQRCode; // Retorna a imagem pronta para ir direto para a tela do operador
    }

    private static BufferedImage toImage(QrCode qr, int scale, int border) {
        return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
    }

    private static BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
        Objects.requireNonNull(qr);
        if (scale <= 0 || border < 0)
                throw new IllegalArgumentException("Value out of range");
        if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
                throw new IllegalArgumentException("Scale or border too large");

        BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < result.getHeight(); y++) {
                for (int x = 0; x < result.getWidth(); x++) {
                        boolean color = qr.getModule(x / scale - border, y / scale - border);
                        result.setRGB(x, y, color ? darkColor : lightColor);
                }
        }
        return result;
    }
}
  