package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.DefaultTableModel;

public class TemplateCupom {

    public static String gerarHtmlCupom(String idVenda, DefaultTableModel modeloTabela, String descricaoRealPeca, String marcaRealPeca, double totalCompra, String formaPagamento) {
        String dataAtual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String horaAtual = new SimpleDateFormat("HH:mm:ss").format(new Date());
        
        StringBuilder linhasItensHtml = new StringBuilder();
        int totalLinhas = modeloTabela.getRowCount();
        
        for (int i = 0; i < totalLinhas; i++) {
            // Puxa o código da peça da Coluna 5 e o valor da Coluna 4
            String codPeca = String.valueOf(modeloTabela.getValueAt(i, 5)).trim();
            String valorItemTexto = String.valueOf(modeloTabela.getValueAt(i, 4)).trim().replace(",", ".");
            
            double valorItem = 0.0;
            try { valorItem = Double.parseDouble(valorItemTexto); } catch (NumberFormatException ignored) {}
            
            // 🔥 SOLUÇÃO: Em vez de ler a coluna 6 (que tem o cliente), usa as variáveis reais passadas por parâmetro
            String primeiraPalavra = "PEÇA";
            if (descricaoRealPeca != null && !descricaoRealPeca.trim().isEmpty()) {
                primeiraPalavra = descricaoRealPeca.trim().split(" ")[0].toUpperCase();
            }
            
            String marca = (marcaRealPeca != null && !marcaRealPeca.trim().isEmpty()) ? marcaRealPeca.trim().toUpperCase() : "BRECHÓ";
            
            // Monta a linha perfeita no padrão de mercado
            linhasItensHtml.append("<tr>")
                           .append("  <td>").append(codPeca).append(" ").append(primeiraPalavra).append(" ").append(marca).append("</td>")
                           .append("  <td style=\"text-align: center;\">1</td>")
                           .append("  <td style=\"text-align: right;\">R$ ").append(String.format("%.2f", valorItem)).append("</td>")
                           .append("</tr>");
        }
        
        return "<html>" +
               "<body style=\"font-family: 'Courier New', Courier, monospace; width: 320px; font-size: 12px; line-height: 1.4; color: #000; margin: 0 auto; padding: 10px;\">" +
               "    <div style=\"text-align: center; font-weight: bold;\">" +
               "        PORTOBELLA Brechó & Outlet<br>" +
               "        AV. CRISTÓVÃO COLOMBO, 2149 - Loja 15  - PORTO ALEGRE/RS<br>" +
               "        ------------------------------------------<br>" +
               "        CUPOM NÃO FISCAL - RECIBO DE VENDA<br>" +
               "        ------------------------------------------" +
               "    </div>" +
               "    <div style=\"margin: 10px 0;\">" +
               "        <strong>DATA:</strong> " + dataAtual + " &nbsp;&nbsp; <strong>HORA:</strong> " + horaAtual + "<br>" +
               "        <strong>VENDA Nº:</strong> " + idVenda + " &nbsp;&nbsp;&nbsp;&nbsp; <strong>OPERADOR:</strong> CAIXA 01<br>" +
               "        <strong>CLIENTE:</strong> CONSUMIDOR FINAL" +
               "    </div>" +
               "    ------------------------------------------<br>" +
               "    <table style=\"width: 100%; font-size: 12px; font-family: inherit;\">" +
               "        <tr style=\"font-weight: bold;\">" +
               "            <td style=\"width: 50%;\">ITEM (CÓD)</td>" +
               "            <td style=\"text-align: center; width: 20%;\">QTD</td>" +
               "            <td style=\"text-align: right; width: 30%;\">VALOR</td>" +
               "        </tr>" +
               "        " + linhasItensHtml.toString() + "" + 
               "    </table>" +
               "    ------------------------------------------<br>" +
               "    <div style=\"text-align: right; font-weight: bold; font-size: 13px;\">" +
               "        TOTAL DA COMPRA: R$ " + String.format("%.2f", totalCompra) + "" +
               "    </div>" +
               "    <div style=\"margin-top: 5px;\">" +
               "        <strong>FORMA PAGTO:</strong> " + formaPagamento + "" +
               "    </div>" +
               "    ------------------------------------------<br>" +
               "    <div style=\"text-align: center; font-size: 11px; font-style: italic;\">" +
               "        Obrigado pela preferência!<br>" +
               "        Peças de brechó promovem o consumo sustentável.<br>" +
               "        Trocas em até 7 dias com a etiqueta fixada.<br><br>" +
               "        <strong>SóBrechó ERP - Powered by SRS Consultoria</strong>" +
               "    </div>" +
               "</body>" +
               "</html>";
    }
}
