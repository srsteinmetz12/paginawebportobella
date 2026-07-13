package paginaweb;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EnviarGit {

    private static final String DIRETORIO_LOCAL = "C:\\Users\\DBC\\Documents\\estoqueVitrineWeb";
    private static final String DIRETORIO_RENDER = "/app/estoqueVitrineWeb";

    public void enviarParaGitHub() throws Exception {
        String diretorio = System.getenv("RENDER") != null ? DIRETORIO_RENDER : DIRETORIO_LOCAL;
        File indexFile = new File(diretorio, "index.html");

        if (!indexFile.exists()) {
            System.err.println("❌ index.html não encontrado em: " + diretorio);
            return;
        }

        System.out.println("📤 Enviando " + indexFile.getAbsolutePath() + " para o GitHub...");

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String[] cmdAdd, cmdCommit, cmdPush;

        if (isWindows) {
            cmdAdd = new String[]{"cmd.exe", "/c", "cd /d " + diretorio + " && git add index.html"};
            cmdCommit = new String[]{"cmd.exe", "/c", "cd /d " + diretorio + " && git commit -m \"Atualização automática - " + new java.util.Date() + "\""};
            cmdPush = new String[]{"cmd.exe", "/c", "cd /d " + diretorio + " && git push origin main"};
        } else {
            cmdAdd = new String[]{"git", "-C", diretorio, "add", "index.html"};
            cmdCommit = new String[]{"git", "-C", diretorio, "commit", "-m", "Atualização automática - " + new java.util.Date()};
            cmdPush = new String[]{"git", "-C", diretorio, "push", "origin", "main"};
        }

        executarComando(cmdAdd);
        executarComando(cmdCommit);
        executarComando(cmdPush);

        System.out.println("✅ Enviado para o GitHub com sucesso!");
    }

    private void executarComando(String... comando) throws Exception {
        Process process = Runtime.getRuntime().exec(comando);
        int exitCode = process.waitFor();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   " + line);
            }
        }

        if (exitCode != 0) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("   ❌ " + line);
                }
            }
            throw new Exception("Comando falhou com código: " + exitCode);
        }
    }

    public static void main(String[] args) {
        try {
            new EnviarGit().enviarParaGitHub();
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }
}