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
        if (itensJson == null || itensJson.isEmpty()) return "Nenhum item";
        try {
            JsonArray array = JsonParser.parseString(itensJson).getAsJsonArray();
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
            return itensJson; // fallback
        }
    }

    // ==========================================
    // GERAR BARRA DE EVOLUÇÃO
    // ==========================================
    public static String gerarBarraEvolucao(int etapaConcluida) {
        String[] etapas = {"Recebido", "Pagamento confirmado", "Em separação", "Disponível / Despachado"};
        StringBuilder sb = new StringBuilder();

        sb.append("<div style='width: 100%; max-width: 500px; margin: 20px auto; padding: 15px; background: #f9f9f9; border-radius: 8px; font-size: 12px;'>");
        sb.append("<p style='text-align: center; font-weight: bold; color: #1E1E1E; margin: 0 0 15px 0;'>📦 Acompanhe seu pedido</p>");
        sb.append("<table style='width: 100%; table-layout: fixed; border-collapse: collapse;'>");
        sb.append("<tr>");

        for (int i = 0; i < etapas.length; i++) {
            boolean concluida = (i < etapaConcluida);
            boolean ativa = (i == etapaConcluida - 1 && etapaConcluida <= 4 && etapaConcluida > 0);
            String cor = concluida ? "#00a650" : (ativa ? "#f39c12" : "#ccc");
            String textoCor = concluida ? "#00a650" : (ativa ? "#f39c12" : "#999");

            sb.append("<td style='text-align: center; padding: 0 4px; width: 25%;'>");
            sb.append("<div style='width: 30px; height: 30px; line-height: 30px; border-radius: 50%; background: ").append(cor).append("; color: #fff; font-weight: bold; margin: 0 auto; font-size: 14px;'>");
            if (concluida) sb.append("✓");
            else if (ativa) sb.append("•");
            else sb.append(" ");
            sb.append("</div>");
            sb.append("<div style='font-size: 11px; color: ").append(textoCor).append("; margin-top: 5px; font-weight: ").append(concluida || ativa ? "bold" : "normal").append(";'>");
            sb.append(etapas[i]);
            sb.append("</div>");
            sb.append("</td>");

            if (i < etapas.length - 1) {
                sb.append("<td style='width: 10%; text-align: center; padding: 0;'>");
                String corLinha = (concluida || (i < etapaConcluida)) ? "#00a650" : "#ccc";
                sb.append("<div style='height: 2px; background: ").append(corLinha).append("; margin: 15px 0;'></div>");
                sb.append("</td>");
            }
        }
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</div>");
        return sb.toString();
    }
}