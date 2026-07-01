package paginaweb;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Serviço agendado para atualizar a vitrine virtual a cada 2 dias
 * 
 * @author SRS Consultoria TI
 */
public class ScheduleAtualizacaoSite {

    private static ScheduledExecutorService scheduler;
    private static boolean executando = true;
    
    // ==========================================
    // CONSTANTES
    // ==========================================
    private static final long INTERVALO_INICIAL = 10; // 10 segundos (tempo para iniciar)
    private static final long INTERVALO_DIAS = 2;    // 2 dias
    private static final long INTERVALO_SEGUNDOS = INTERVALO_DIAS * 24 * 60 * 60; // 2 dias em segundos

    /**
     * Inicia o serviço agendado
     */
    public static void iniciar() {
        System.out.println("🔔 ========================================");
        System.out.println("🔔 SERVIÇO AGENDADO - ATUALIZAÇÃO DO SITE");
        System.out.println("🔔 ========================================");
        System.out.println("🔄 Próxima atualização em: " + INTERVALO_DIAS + " dia(s)");
        System.out.println("⏰ Intervalo: " + INTERVALO_SEGUNDOS + " segundos");
        System.out.println("🔔 ========================================");

        scheduler = Executors.newScheduledThreadPool(1);
        
        // ==========================================
        // AGENDAR TAREFA
        // ==========================================
        scheduler.scheduleAtFixedRate(() -> {
            if (executando) {
                try {
                    System.out.println("🔄 [SCHEDULE] Iniciando atualização automática do site...");
                    atualizarSite();
                } catch (Exception e) {
                    System.err.println("❌ [SCHEDULE] Erro ao atualizar site: " + e.getMessage());
                }
            }
        }, INTERVALO_INICIAL, INTERVALO_SEGUNDOS, TimeUnit.SECONDS);
        
        System.out.println("✅ Serviço agendado iniciado com sucesso!");
        System.out.println("📅 Próxima execução: " + calcularProximaExecucao());
        System.out.println("🔔 ========================================");
    }

    /**
     * Para o serviço agendado
     */
    public static void parar() {
        executando = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        System.out.println("⏹️ Serviço agendado parado.");
    }

    /**
     * Executa a atualização do site
     */
    private static void atualizarSite() {
        try {
            GerarSiteEstoque gerador = new GerarSiteEstoque();
            gerador.gerarSiteEstoque();
            
            System.out.println("✅ [SCHEDULE] Site atualizado com sucesso!");
            System.out.println("📅 Próxima atualização: " + calcularProximaExecucao());
            System.out.println("----------------------------------");
            
        } catch (ClassNotFoundException | InterruptedException | SQLException e) {
            System.err.println("❌ [SCHEDULE] Erro ao gerar site: " + e.getMessage());
        }
    }

    /**
     * Calcula a data da próxima execução
     */
    private static String calcularProximaExecucao() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, (int) INTERVALO_DIAS);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    /**
     * Método para testar a execução imediata
     */
    public static void executarAgora() {
        System.out.println("🔄 [SCHEDULE] Executando atualização imediata...");
        atualizarSite();
    }

    /**
     * Main para teste
     * @param args
     */
    public static void main(String[] args) {
        iniciar();
        
        // Mantém o programa rodando
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}