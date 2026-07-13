package paginaweb;

public class GerarEEnviar {
    public static void main(String[] args) {
        try {
            // 1. Gera o HTML
            new GerarSiteEstoque().gerarSiteEstoque();
            
            // 2. Envia para o GitHub
            new EnviarGit().enviarParaGitHub();
            
            System.out.println("✅ Processo completo!");
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }
}