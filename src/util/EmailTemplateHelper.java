package util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class EmailTemplateHelper {

    // ==========================================
    // FORMATAR ITENS (JSON -> HTML)
    // ==========================================
    public static String formatarItensHtml(String itensJson) {
        if (itensJson == null || itensJson.isEmpty()) {
            return "Nenhum item";
        }

        // 🔥 VERIFICA SE O CONTEÚDO É UM JSON ARRAY VÁLIDO
        String trimmed = itensJson.trim();
        if (!trimmed.startsWith("[")) {
            System.err.println("⚠️ Itens não é um JSON Array: " + trimmed);
            return "Itens não disponíveis";
        }

        try {
            JsonArray array = JsonParser.parseString(itensJson).getAsJsonArray();
            if (array.size() == 0) return "Nenhum item";

            StringBuilder sb = new StringBuilder();
            sb.append("<ul style='list-style: none; padding: 0;'>");
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                String nome = obj.get("nome").getAsString();
                double preco = obj.get("preco").getAsDouble();
                int qtd = obj.get("quantidade").getAsInt();
                sb.append("<li style='padding: 4px 0; border-bottom: 1px solid #eee;'>")
                  .append(nome)
                  .append(" – Qtd: ").append(qtd)
                  .append(" – R$ ").append(String.format("%.2f", preco * qtd))
                  .append("</li>");
            }
            sb.append("</ul>");
            return sb.toString();

        } catch (JsonSyntaxException e) {
            System.err.println("❌ Erro ao parsear itens: " + e.getMessage());
            return "Itens não disponíveis";
        }
    }

    // ==========================================
    // GERAR BARRA DE EVOLUÇÃO
    // ==========================================
    public static String gerarBarraEvolucao(int etapaConcluida) {
        System.out.println("🔥 [TABELA] gerarBarraEvolucao chamada com etapa=" + etapaConcluida);
        String[] etapas = {"Recebido", "Pagamento confirmado", "Em separação", "Disponível / Despachado"};
        StringBuilder sb = new StringBuilder();

        sb.append("<div style='font-family: Arial, sans-serif; max-width: 100%; margin: 15px 0; padding: 10px 5px; background: #f9f9f9; border-radius: 10px; text-align: center;'>");
        sb.append("<p style='font-size: 13px; font-weight: bold; color: #1E1E1E; margin: 0 0 10px 0;'>📦 Acompanhe seu pedido</p>");

        sb.append("<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='border-collapse: collapse;'>");
        sb.append("<tr>");

        for (int i = 0; i < etapas.length; i++) {
            boolean concluida = (i < etapaConcluida);
            boolean ativa = (i == etapaConcluida - 1 && etapaConcluida <= 4 && etapaConcluida > 0);
            String corFundo = concluida ? "#00a650" : (ativa ? "#f39c12" : "#d3d3d3");
            String corTexto = concluida ? "#00a650" : (ativa ? "#f39c12" : "#aaaaaa");

            sb.append("<td style='text-align: center; padding: 0 3px; vertical-align: top; width: 25%;'>");

            // Círculo com flex para centralizar conteúdo
            sb.append("<div style='width: 22px; height: 22px; border-radius: 50%; background: ")
            .append(corFundo)
            .append("; color: #fff; font-size: 14px; font-weight: bold; text-align: center; line-height: 22px; margin: 0 auto; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
            if (concluida) sb.append("✓");
            else if (ativa) sb.append("•");
            else sb.append(" ");
            sb.append("</div>");

            // Texto abaixo
            sb.append("<div style='font-size: 8px; color: ").append(corTexto).append("; margin-top: 4px; font-weight: ")
              .append(concluida || ativa ? "bold" : "normal")
              .append("; text-align: center; line-height: 1.2; word-wrap: break-word; max-width: 100%; padding: 0 2px;'>");
            sb.append(etapas[i]);
            sb.append("</div>");

            sb.append("</td>");

            if (i < etapas.length - 1) {
                String corLinha = (i < etapaConcluida) ? "#00a650" : "#d3d3d3";
                sb.append("<td style='text-align: center; padding: 0; width: 8%; vertical-align: middle;'>");
                sb.append("<div style='height: 2px; background: ").append(corLinha).append("; width: 100%;'></div>");
                sb.append("</td>");
            }
        }

        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</div>");
        return sb.toString();
    }
    
    public static String gerarResumoFinanceiro(double subtotal, double frete, double total) {
        return "<div style='background: #f5f5f5; padding: 10px; border-radius: 8px; margin: 10px 0;'>" +
               "  <p style='margin: 5px 0;'><strong>Subtotal:</strong> R$ " + String.format("%.2f", subtotal) + "</p>" +
               "  <p style='margin: 5px 0;'><strong>Frete:</strong> R$ " + String.format("%.2f", frete) + "</p>" +
               "  <p style='margin: 5px 0; font-size: 18px; font-weight: bold; color: #00a650;'>" +
               "    <strong>Total:</strong> R$ " + String.format("%.2f", total) +
               "  </p>" +
               "</div>";
    }
    
    public static String gerarCupomFiscal(String pedidoId, String nomeCliente, String itensHtml, double subtotal, double frete, double total) {
        String data = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String hora = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        return "<div style='border: 2px solid #1E1E1E; border-radius: 10px; padding: 15px; margin: 15px 0; background: #fefefe;'>" +
               "  <div style='text-align: center; border-bottom: 1px dashed #ccc; padding-bottom: 10px;'>" +
               "    <h3 style='margin: 0; color: #1E1E1E; font-size: 18px;'>🛍️ PORTOBELLA Brechó & Outlet</h3>" +
               "    <p style='margin: 2px 0; font-size: 12px; color: #555;'>CNPJ: 47.878.220/0001-86</p>" +
               "    <p style='margin: 2px 0; font-size: 12px; color: #555;'>Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS</p>" +
               "  </div>" +
               "  <div style='padding: 10px 0;'>" +
               "    <p style='margin: 3px 0; font-size: 13px;'><strong>Pedido:</strong> #" + pedidoId + "</p>" +
               "    <p style='margin: 3px 0; font-size: 13px;'><strong>Cliente:</strong> " + nomeCliente + "</p>" +
               "    <p style='margin: 3px 0; font-size: 13px;'><strong>Data:</strong> " + data + " – " + hora + "</p>" +
               "  </div>" +
               "  <div style='border-top: 1px dashed #ccc; border-bottom: 1px dashed #ccc; padding: 10px 0;'>" +
               "    <p style='font-weight: bold; margin: 0 0 5px 0; font-size: 14px;'>Itens:</p>" +
               itensHtml +
               "  </div>" +
               "  <div style='padding: 10px 0; text-align: right;'>" +
               "    <p style='margin: 3px 0; font-size: 13px;'><strong>Subtotal:</strong> R$ " + String.format("%.2f", subtotal) + "</p>" +
               "    <p style='margin: 3px 0; font-size: 13px;'><strong>Frete:</strong> R$ " + String.format("%.2f", frete) + "</p>" +
               "    <p style='margin: 3px 0; font-size: 18px; font-weight: bold; color: #00a650;'>Total: R$ " + String.format("%.2f", total) + "</p>" +
               "  </div>" +
               "  <div style='text-align: center; border-top: 1px dashed #ccc; padding-top: 10px;'>" +
               "    <p style='margin: 0; font-size: 11px; color: #888;'>Cupom Não Fiscal – Este documento não é nota fiscal</p>" +
               "    <p style='margin: 0; font-size: 11px; color: #888;'>💛 Obrigada pela preferência! 💛</p>" +
               "  </div>" +
               "</div>";
    }
}