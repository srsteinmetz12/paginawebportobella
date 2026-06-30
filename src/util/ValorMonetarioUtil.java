package util;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Classe utilitária para tratamento de valores monetários
 * Suporta entrada com vírgula ou ponto e converte para formato do banco
 */
public class ValorMonetarioUtil {
    
    // ==========================================
    // CONSTANTES
    // ==========================================
    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final DecimalFormat FORMATADOR_BR = new DecimalFormat("#,##0.00", 
            new DecimalFormatSymbols(LOCALE_BR));
    private static final DecimalFormat FORMATADOR_BANCO = new DecimalFormat("0.00", 
            new DecimalFormatSymbols(Locale.US));
    
    // ==========================================
    // 1. CONVERTER STRING PARA DOUBLE
    // ==========================================
    /**
     * Converte uma string de valor para double
     * Aceita vírgula ou ponto como separador decimal
     * Ex: "1.000,50" -> 1000.50 | "1000.50" -> 1000.50 | "1,000.50" -> 1000.50
     * @param valor
     * @return 
     */
    public static double converterParaDouble(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0;
        }
        
        try {
            String valorLimpo = valor.trim();
            
            // ==========================================
            // PASSO 1: REMOVER FORMATADORES DE MILHAR
            // ==========================================
            // Se tiver ponto e vírgula (ex: 1.000,50)
            if (valorLimpo.contains(".") && valorLimpo.contains(",")) {
                // Remove os pontos (separador de milhar)
                valorLimpo = valorLimpo.replace(".", "");
                // Substitui vírgula por ponto (separador decimal)
                valorLimpo = valorLimpo.replace(",", ".");
            } 
            // Se tiver apenas vírgula (ex: 1000,50)
            else if (valorLimpo.contains(",") && !valorLimpo.contains(".")) {
                // Substitui vírgula por ponto
                valorLimpo = valorLimpo.replace(",", ".");
            }
            // Se tiver apenas ponto, já está no formato correto
            
            // ==========================================
            // PASSO 2: REMOVER CARACTERES NÃO NUMÉRICOS
            // ==========================================
            valorLimpo = valorLimpo.replaceAll("[^0-9.]", "");
            
            // ==========================================
            // PASSO 3: CORRIGIR MÚLTIPLOS PONTOS
            // ==========================================
            if (valorLimpo.indexOf(".") != valorLimpo.lastIndexOf(".")) {
                // Mantém apenas o último ponto como separador decimal
                String[] partes = valorLimpo.split("\\.");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < partes.length - 1; i++) {
                    sb.append(partes[i]);
                }
                sb.append(".").append(partes[partes.length - 1]);
                valorLimpo = sb.toString();
            }
            
            return Double.parseDouble(valorLimpo);
            
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Erro ao converter valor: " + valor + " - " + e.getMessage());
            return 0.0;
        }
    }
    
    // ==========================================
    // 2. FORMATAR PARA BANCO (PONTO DECIMAL)
    // ==========================================
    /**
     * Formata um double para string com 2 casas decimais (ponto decimal)
     * Ex: 1000.5 -> "1000.50"
     * @param valor
     * @return 
     */
    public static String formatarParaBanco(double valor) {
        return FORMATADOR_BANCO.format(valor);
    }
    
    /**
     * Formata um double para string com 2 casas decimais (ponto decimal)
     * Ex: "1.000,50" -> "1000.50"
     * @param valor
     * @return 
     */
    public static String formatarParaBanco(String valor) {
        double valorDouble = converterParaDouble(valor);
        return formatarParaBanco(valorDouble);
    }
    
    // ==========================================
    // 3. FORMATAR PARA EXIBIÇÃO (VÍRGULA DECIMAL)
    // ==========================================
    /**
     * Formata um double para string com 2 casas decimais (vírgula decimal)
     * Ex: 1000.5 -> "1.000,50"
     * @param valor
     * @return 
     */
    public static String formatarParaExibicao(double valor) {
        return FORMATADOR_BR.format(valor);
    }
    
    /**
     * Formata uma string para exibição
     * Ex: "1000.50" -> "1.000,50"
     * @param valor
     * @return 
     */
    public static String formatarParaExibicao(String valor) {
        double valorDouble = converterParaDouble(valor);
        return formatarParaExibicao(valorDouble);
    }
    
    // ==========================================
    // 4. VALIDAR SE É NÚMERO VÁLIDO
    // ==========================================
    public static boolean isValorValido(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return false;
        }
        try {
            converterParaDouble(valor);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==========================================
    // 5. MÁSCARA PARA CAMPO DE TEXTO
    // ==========================================
    /**
     * Aplica máscara para permitir apenas números, vírgula e ponto
     * @param campo
     */
    public static void aplicarMascaraValor(JTextField campo) {
        campo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                String texto = campo.getText();
                
                // Permite apenas números, vírgula e ponto
                if (!Character.isDigit(c) && c != ',' && c != '.') {
                    evt.consume();
                    return;
                }
                
                // Impede mais de uma vírgula ou ponto
                if ((c == ',' || c == '.') && (texto.contains(",") || texto.contains("."))) {
                    evt.consume();
                    return;
                }
                
                // Limita o tamanho (evita valores muito grandes)
                if (texto.length() >= 15) {
                    evt.consume();
                }
            }
        });
        
        // ==========================================
        // 🔥 FORMATAR AO PERDER O FOCO
        // ==========================================
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String texto = campo.getText();
                if (!texto.isEmpty() && isValorValido(texto)) {
                    double valor = converterParaDouble(texto);
                    campo.setText(formatarParaExibicao(valor));
                }
            }
        });
    }
    
    // ==========================================
    // 6. APLICAR MÁSCARA EM MÚLTIPLOS CAMPOS
    // ==========================================
    public static void aplicarMascaraEmCampos(JTextField... campos) {
        for (JTextField campo : campos) {
            aplicarMascaraValor(campo);
        }
    }
    
    // ==========================================
    // 7. OBTER VALOR DO CAMPO
    // ==========================================
    public static double getValorDoCampo(JTextField campo) {
        return converterParaDouble(campo.getText());
    }
    
    public static void setValorNoCampo(JTextField campo, double valor) {
        campo.setText(formatarParaExibicao(valor));
    }
    
    public static void setValorNoCampo(JTextField campo, String valor) {
        campo.setText(formatarParaExibicao(valor));
    }
    
    // ==========================================
    // 8. TESTE
    // ==========================================
    public static void main(String[] args) {
        System.out.println("========== TESTE CONVERSÃO ==========");
        
        String[] testes = {
            "1.000,50",
            "1000,50",
            "1000.50",
            "1.000,50",
            "1.500,75",
            "0,50",
            "0.50",
            "10.000,00"
        };
        
        for (String teste : testes) {
            double valor = converterParaDouble(teste);
            System.out.println("Entrada: " + teste);
            System.out.println("  Double: " + valor);
            System.out.println("  Banco:  " + formatarParaBanco(valor));
            System.out.println("  Exibir: " + formatarParaExibicao(valor));
            System.out.println("---");
        }
    }
}