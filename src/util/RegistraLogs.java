package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistraLogs {
    
    public void registrarLog(String mensagem) {
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // O parâmetro 'true' no FileWriter serve para ADICIONAR texto sem apagar o que já existe
        try (FileWriter fw = new FileWriter("historico_erros.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println("[" + dataHora + "] ERRO: " + mensagem);

        } catch (Exception ex) {
            // Se falhar até o log, mostramos no console do NetBeans
            System.err.println("Não foi possível gravar no arquivo de log: " + ex.getMessage());
        }
    }  
}
