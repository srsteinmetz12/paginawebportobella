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

        // Container principal com fundo e bordas arredondadas
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 100%; margin: 10px 0; padding: 10px; background: #f5f5f5; border-radius: 8px;'>");
        sb.append("<p style='text-align: center; font-weight: bold; color: #1E1E1E; margin: 0 0 10px 0; font-size: 14px;'>📦 Acompanhe seu pedido</p>");

        // Usando divs com flex (compatível com a maioria dos clientes de e-mail)
        sb.append("<div style='display: flex; align-items: center; justify-content: space-between; flex-wrap: nowrap;'>");

        for (int i = 0; i < etapas.length; i++) {
            boolean concluida = (i < etapaConcluida);
            boolean ativa = (i == etapaConcluida - 1 && etapaConcluida <= 4 && etapaConcluida > 0);
            String cor = concluida ? "#00a650" : (ativa ? "#f39c12" : "#cccccc");
            String textoCor = concluida ? "#00a650" : (ativa ? "#f39c12" : "#999999");

            // Item da etapa
            sb.append("<div style='display: flex; flex-direction: column; align-items: center; flex: 1; min-width: 0;'>");

            // Círculo
            sb.append("<div style='width: 28px; height: 28px; line-height: 28px; border-radius: 50%; background: ").append(cor).append("; color: #fff; font-weight: bold; text-align: center; font-size: 14px; margin: 0 auto;'>");
            if (concluida) sb.append("✓");
            else if (ativa) sb.append("•");
            else sb.append(" ");
            sb.append("</div>");

            // Texto da etapa (com quebra de linha automática)
            sb.append("<div style='font-size: 10px; color: ").append(textoCor).append("; margin-top: 4px; font-weight: ").append(concluida || ativa ? "bold" : "normal").append("; text-align: center; word-wrap: break-word; max-width: 80px; line-height: 1.2;'>");
            sb.append(etapas[i]);
            sb.append("</div>");

            sb.append("</div>");

            // Linha de conexão (exceto após a última etapa)
            if (i < etapas.length - 1) {
                String corLinha = (concluida || (i < etapaConcluida)) ? "#00a650" : "#cccccc";
                sb.append("<div style='flex: 1; height: 2px; background: ").append(corLinha).append("; margin: 0 2px; min-width: 10px;'></div>");
            }
        }

        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }
}