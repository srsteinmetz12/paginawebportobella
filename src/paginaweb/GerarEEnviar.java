package paginaweb;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

public class GerarEEnviar {
    private static final String DEPLOY_HOOK_URL = "https://api.vercel.com/v1/integrations/deploy/prj_3zRVfNgvzV1inSyYCeUemoccVoI1/IizRlk19xg";

    public static void main(String[] args) {
        try {
            // 1. Gera o HTML
            new GerarSiteEstoque().gerarSiteEstoque();

            // 2. Aciona o Deploy Hook da Vercel
            acionarDeployHook();

            System.out.println("✅ Processo completo (HTML gerado + deploy acionado)!");
        } catch (ClassNotFoundException | InterruptedException | SQLException e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    private static void acionarDeployHook() {
        try {
            URL url = new URL(DEPLOY_HOOK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                System.out.println("   ✅ Deploy Hook acionado com sucesso!");
            } else {
                System.err.println("   ❌ Deploy Hook retornou código: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("   ❌ Erro ao acionar Deploy Hook: " + e.getMessage());
        }
    }
}