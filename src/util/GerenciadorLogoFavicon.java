package util;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.net.URL;

public class GerenciadorLogoFavicon {
    
    public static void aplicarFaviconGlobal(JFrame frame) {
        if (frame == null) return;
        
        try {
            // Puxa o caminho do arquivo de propriedades usando a sua classe homologada
            String caminhoFavicon = util.ConfigLoader.get("sistema.favicon"); // Ajuste o pacote do seu ConfigLoader
            
            if (caminhoFavicon != null && !caminhoFavicon.trim().isEmpty()) {
                if (!caminhoFavicon.startsWith("/")) {
                    caminhoFavicon = "/" + caminhoFavicon;
                }
                
                URL urlFavicon = GerenciadorLogoFavicon.class.getResource(caminhoFavicon);
                if (urlFavicon != null) {
                    // Aplica o escudo dourado na instância recebida
                    frame.setIconImage(new ImageIcon(urlFavicon).getImage());
                } else {
                    System.err.println("Aviso: Favicon não localizado no caminho do recurso interno.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao aplicar favicon no frame: " + e.getMessage());
        }
    }
}
