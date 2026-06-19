package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigLoader {
    // Variável global única que será preenchida
    private static final Properties props = new Properties();
    private static final String NOME_ARQUIVO = "config.properties";
    
    static {
        // 🔥 NOVO: Tenta carregar das variáveis de ambiente primeiro
        String envHost = System.getenv("DB_CLOUD_HOST");
        if (envHost != null && !envHost.isEmpty()) {
            // Se as variáveis de ambiente existem, monta a URL e configura
            String envUser = System.getenv("DB_CLOUD_USER");
            String envPassword = System.getenv("DB_CLOUD_PASSWORD");
            String envPort = System.getenv("DB_CLOUD_PORT");
            String envDatabase = System.getenv("DB_CLOUD_DATABASE");

            if (envUser != null && envPassword != null && envPort != null && envDatabase != null) {
                // Monta a URL de conexão
                String cloudUrl = "jdbc:mysql://" + envHost + ":" + envPort + "/" + envDatabase + "?useSSL=true&serverTimezone=UTC";

                // Coloca as propriedades no props para serem usadas pelo get()
                props.setProperty("db.cloud_url", cloudUrl);
                props.setProperty("db.cloud_user", envUser);
                props.setProperty("db.cloud_password", envPassword);
                props.setProperty("db.cloud_host", envHost);
                props.setProperty("db.cloud_port", envPort);
                props.setProperty("db.cloud_database", envDatabase);

                System.out.println("ConfigLoader: Configurações carregadas das variáveis de ambiente.");
//                return; // Sai do bloco static, pois já carregou
            }
        }
    
    // ==========================================
    // SEU CÓDIGO ORIGINAL CONTINUA ABAIXO
    // ==========================================
        boolean carregou = false;

        try {
            // 1. TENTATIVA EM PRODUÇÃO: Pega o caminho físico exato da pasta onde o .JAR está executando
            String path = ConfigLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(path);
            File configFile = new File(jarFile.getParent(), NOME_ARQUIVO);

            if (configFile.exists()) {
                // O uso do try() aqui já fecha o arquivo automaticamente ao terminar
                try (FileInputStream in = new FileInputStream(configFile)) {
                    props.load(in); // ✅ CORREÇÃO: Alimenta a variável GLOBAL, sem o 'Properties' na frente
                    carregou = true;
                    System.out.println("ConfigLoader: Configurações carregadas da pasta do JAR: " + configFile.getAbsolutePath());
                }
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("ConfigLoader: Falha ao tentar buscar arquivo ao lado do JAR: " + e.getMessage());
        }

        // 2. TENTATIVA EM DESENVOLVIMENTO: Se não encontrou ao lado do JAR, tenta ler da raiz do projeto (Para a IDE NetBeans funcionar)
        if (!carregou) {
            File arquivoIDE = new File(NOME_ARQUIVO);
            if (arquivoIDE.exists()) {
                try (FileInputStream in = new FileInputStream(arquivoIDE)) {
                    props.load(in); // ✅ Alimenta a variável GLOBAL
                    System.out.println("ConfigLoader: Configurações carregadas no modo Desenvolvimento (IDE).");
                } catch (IOException ex) {
                    Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Erro ao ler arquivo na IDE", ex);
                }
            } else {
                System.err.println("❌ Erro Crítico: O arquivo " + NOME_ARQUIVO + " não foi localizado em nenhum diretório!");
            }
        }
    }

    public static String get(String key) {
        // 🔥 NOVO: Tenta variável de ambiente diretamente (fallback)
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue.trim();
        }
        String valor = props.getProperty(key);
        return (valor != null) ? valor.trim() : null; // Clean Code: Evita retornar strings com espaços em branco acidentais
    }

    public static int getInt(String key) {
        String valor = get(key);
        return (valor != null) ? Integer.parseInt(valor) : 0;
    }
    
    public static String getCopyright() {
        int anoAtual = java.time.Year.now().getValue();
        return "Copyright © 2022-" + anoAtual + " SRS Consultoria TI LTDA";
    }
}
