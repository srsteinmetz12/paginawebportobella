package util;

import java.util.*;
import java.util.concurrent.*;

public class ClienteCacheManager {
    private static final int MAX_CACHE_SIZE = 100;
    private static final long CACHE_EXPIRATION_MS = 60_000; // 1 minuto
    
    private static Map<String, CacheEntry> cache = new LinkedHashMap<String, CacheEntry>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };
    
    private static final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    
    static {
        // Limpa cache expirado a cada 30 segundos
        cleaner.scheduleAtFixedRate(() -> {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    static class CacheEntry {
        List<String> resultados;
        long timestamp;
        
        CacheEntry(List<String> resultados) {
            this.resultados = new ArrayList<>(resultados);
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRATION_MS;
        }
    }
    
    public static List<String> get(String prefixo) {
        if (prefixo == null || prefixo.trim().isEmpty()) return null;
        CacheEntry entry = cache.get(prefixo.toLowerCase().trim());
        if (entry != null && !entry.isExpired()) {
            System.out.println("Cache HIT: " + prefixo);
            return entry.resultados;
        }
        System.out.println("Cache MISS: " + prefixo);
        return null;
    }
    
    public static void put(String prefixo, List<String> resultados) {
        if (prefixo == null || prefixo.trim().isEmpty()) return;
        if (resultados == null || resultados.isEmpty()) return;
        cache.put(prefixo.toLowerCase().trim(), new CacheEntry(resultados));
    }
    
    public static void clear() {
        cache.clear();
    }
    
    public static void remove(String prefixo) {
        cache.remove(prefixo.toLowerCase().trim());
    }
    
    public static void shutdown() {
        cleaner.shutdown();
    }
}
