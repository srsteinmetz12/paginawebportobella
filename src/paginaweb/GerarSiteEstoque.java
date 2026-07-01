package paginaweb;

import connection.ConnectionDB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe responsável por gerar o catálogo HTML da vitrine virtual
 * Baseado no estoque disponível do banco de dados
 * 
 * @author PORTOBELLA
 * @version 1.0
 */
public class GerarSiteEstoque {
    
    private String sql;

    // ==========================================
    // CONSTANTES
    // ==========================================
    private static final String DIRETORIO_DOCUMENTOS = "C:\\Users\\DBC\\Documents\\estoqueVitrineWeb";
    private static final String CAMINHO_ARQUIVO = DIRETORIO_DOCUMENTOS + "\\index.html";
    private static final String SUBPASTA_FOTOS = DIRETORIO_DOCUMENTOS + "\\fotos";
    private static final String URL_BACKEND = "https://paginawebportobella-1.onrender.com";
    
    // ==========================================
    // CONEXÕES
    // ==========================================
    private Connection con2;
    private PreparedStatement stmt2;
    private ResultSet rs;

    // ==========================================
    // CONSTRUTOR
    // ==========================================
    public GerarSiteEstoque() {
        // Construtor padrão
    }

    // ==========================================
    // MÉTODO PRINCIPAL - GERAR SITE
    // ==========================================
    public void gerarSiteEstoque() throws ClassNotFoundException, java.sql.SQLException, InterruptedException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT codpeca, itemdesc, tamanho, precosug, imagem FROM estoque WHERE status = 'DISPONIVEL' ORDER BY itemdesc ASC";
        System.out.println("Gerando catálogo web premium: " + sql);

        String diretorioDocumentos = "C:\\Users\\DBC\\Documents\\estoqueVitrineWeb";
        String caminhoArquivo = diretorioDocumentos + "\\index.html";

        String subpastaFotosWeb = diretorioDocumentos + "\\fotos";
        new java.io.File(subpastaFotosWeb).mkdirs();

        try (java.io.PrintWriter writer = new java.io.PrintWriter(caminhoArquivo, "UTF-8");
             PreparedStatement localStmt2 = con2.prepareStatement(sql)) {

            this.stmt2 = localStmt2;
            rs = stmt2.executeQuery();

            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='pt-BR'>");
            writer.println("<head>");
            writer.println("  <meta charset='UTF-8'>");
            writer.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            writer.println("  <title>PORTOBELLA Brechó & Outlet</title>");
            writer.println("  <script src='https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js'></script>");
            writer.println("  <style>");
            writer.println("    * { margin: 0; padding: 0; box-sizing: border-box; }");
            writer.println("    body { background-color: #1E1E1E; color: #FFFFFF; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; }");
            writer.println("    header { background-color: #141414; padding: 15px 30px; border-bottom: 2px solid #464646; position: sticky; top: 0; z-index: 100; display: flex; justify-content: space-between; align-items: center; }");
            writer.println("    header h1 { margin: 0; color: #FFFFFF; font-size: 22px; letter-spacing: 3px; font-weight: 900; text-transform: uppercase; }");
            writer.println("    header p { margin: 0; color: #A0A0A0; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; }");
            writer.println("    .carrinho-icon { position: relative; cursor: pointer; font-size: 28px; }");
            writer.println("    .carrinho-contador { position: absolute; top: -8px; right: -8px; background: #00a650; color: #FFF; border-radius: 50%; padding: 2px 8px; font-size: 12px; font-weight: bold; }");
            writer.println("    .busca-container { max-width: 1160px; margin: 20px auto 0 auto; padding: 0 20px; }");
            writer.println("    .busca-input { width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #464646; background-color: #2D2D2D; color: #FFFFFF; font-size: 14px; }");
            writer.println("    .container { max-width: 1200px; margin: 20px auto; padding: 0 20px; }");
            writer.println("    .vitrine { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }");

            // ==========================================
            // ESTILOS DO CARROSSEL
            // ==========================================
            writer.println("    .card { background-color: #2D2D2D; border: 1px solid #464646; border-radius: 12px; padding: 15px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.3); display: flex; flex-direction: column; justify-content: space-between; position: relative; }");
            writer.println("    .galeria { position: relative; width: 100%; height: 280px; overflow: hidden; border-radius: 8px; background: #141414; margin-bottom: 12px; }");
            writer.println("    .galeria .slide { width: 100%; height: 100%; object-fit: cover; display: none; }");
            writer.println("    .galeria .slide.ativo { display: block; }");
            writer.println("    .galeria .seta { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.6); color: white; border: none; width: 30px; height: 30px; border-radius: 50%; cursor: pointer; font-size: 16px; z-index: 10; transition: background 0.3s; }");
            writer.println("    .galeria .seta:hover { background: rgba(0,0,0,0.9); }");
            writer.println("    .galeria .seta.anterior { left: 5px; }");
            writer.println("    .galeria .seta.proximo { right: 5px; }");
            writer.println("    .galeria .indicadores { position: absolute; bottom: 10px; left: 50%; transform: translateX(-50%); display: flex; gap: 6px; z-index: 10; }");
            writer.println("    .galeria .dot { width: 8px; height: 8px; border-radius: 50%; background: rgba(255,255,255,0.4); cursor: pointer; transition: all 0.3s; }");
            writer.println("    .galeria .dot.ativo { background: #FFFFFF; transform: scale(1.2); }");
            writer.println("    .galeria .contador-fotos { position: absolute; top: 8px; right: 8px; background: rgba(0,0,0,0.7); color: #FFF; padding: 2px 10px; border-radius: 12px; font-size: 11px; z-index: 10; }");
            writer.println("    .card .sem-foto { width: 100%; height: 280px; border-radius: 8px; background-color: #141414; display: flex; align-items: center; justify-content: center; color: #666666; font-size: 13px; font-weight: bold; border: 1px dashed #464646; margin-bottom: 12px; }");
            writer.println("    .card h3 { margin: 5px 0 2px 0; font-size: 14px; color: #FFFFFF; text-transform: uppercase; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: bold; }");
            writer.println("    .card .info { color: #B0B0B0; font-size: 12px; margin: 4px 0 2px 0; }");
            writer.println("    .card .codigo-item { color: #A0A0A0; font-size: 12px; font-weight: bold; margin: 4px 0; text-transform: uppercase; }");
            writer.println("    .card .preco { color: #FFFFFF; font-size: 20px; font-weight: 800; margin: 8px 0; border-top: 1px solid #3D3D3D; padding-top: 10px; }");
            writer.println("    .btn-add-carrinho { display: block; width: 100%; background-color: #009ee3; color: #FFFFFF; padding: 12px; border: none; border-radius: 8px; font-weight: bold; font-size: 13px; text-transform: uppercase; cursor: pointer; margin-top: 10px; text-align: center; transition: background 0.3s; }");
            writer.println("    .btn-add-carrinho:hover { background-color: #0077b3; }");
            writer.println("    footer { text-align: center; padding: 30px; color: #555555; font-size: 11px; margin-top: 20px; border-top: 1px solid #2D2D2D; }");

            // ==========================================
            // ESTILOS DOS MODAIS
            // ==========================================
            writer.println("    .modal { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.85); z-index: 9998; display: none; align-items: center; justify-content: center; }");
            writer.println("    .modal-content { background: #2D2D2D; padding: 30px; border-radius: 15px; max-width: 550px; width: 90%; text-align: center; border: 1px solid #464646; max-height: 95vh; overflow-y: auto; }");
            writer.println("    .modal-content h3 { margin-top: 0; color: #FFF; }");
            writer.println("    .modal-content label { color: #A0A0A0; font-size: 12px; display: block; text-align: left; margin-top: 10px; }");
            writer.println("    .modal-content input { width: 100%; padding: 10px; margin-top: 5px; background: #1E1E1E; border: 1px solid #464646; color: #FFF; border-radius: 6px; }");
            writer.println("    .modal-content .btn-mercadopago { width: 100%; padding: 12px; background: #009ee3; color: #FFF; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; margin-top: 10px; }");
            writer.println("    .modal-content .btn-mercadopago:hover { background: #0077b3; }");
            writer.println("    .modal-content .btn-pix { width: 100%; padding: 12px; background: #00a650; color: #FFF; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; margin-top: 10px; }");
            writer.println("    .modal-content .btn-pix:hover { background: #008f44; }");
            writer.println("    .modal-content .btn-cancelar { background: none; border: none; color: #888; cursor: pointer; margin-top: 10px; }");
            writer.println("    .modal-content .btn-cancelar:hover { color: #FFF; }");
            writer.println("    .modal-content .produto-nome { color: #FFF; font-size: 16px; margin: 10px 0; }");
            writer.println("    .modal-content .produto-preco { color: #00a650; font-size: 24px; font-weight: bold; margin: 10px 0; }");
            writer.println("    .modal-content .btn-calcular-frete { width: 100%; padding: 10px; background: #009ee3; color: #FFF; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; margin-top: 5px; }");
            writer.println("    .modal-content .btn-calcular-frete:hover { background: #0077b3; }");
            writer.println("    .modal-content .frete-resultado { margin-top: 8px; font-size: 13px; color: #A0A0A0; display: none; }");
            writer.println("    .modal-content .frete-resultado.sucesso { color: #00a650; display: block; }");
            // ==========================================
            // ESTILOS DO MODAL PIX
            // ==========================================
            writer.println("    .modal-pix { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.95); z-index: 9999; display: none; align-items: center; justify-content: center; }");
            writer.println("    .modal-pix-content { background: #2D2D2D; padding: 30px; border-radius: 15px; max-width: 450px; width: 90%; text-align: center; border: 1px solid #464646; max-height: 95vh; overflow-y: auto; }");
            writer.println("    .modal-pix-content h3 { margin-top: 0; color: #FFF; font-size: 20px; }");
            writer.println("    .modal-pix-content .valor { color: #00a650; font-size: 28px; font-weight: bold; margin: 10px 0; }");
            writer.println("    .modal-pix-content .descricao { color: #A0A0A0; font-size: 14px; margin-bottom: 15px; }");
            writer.println("    .qr-container { background: #FFFFFF; padding: 15px; border-radius: 10px; display: inline-block; margin: 10px 0; min-height: 230px; min-width: 230px; }");
            writer.println("    .qr-container canvas, .qr-container img { max-width: 200px; height: auto; }");
            writer.println("    .pix-code-area { margin: 15px 0; }");
            writer.println("    .pix-code-area textarea { width: 100%; background: #1E1E1E; color: #FFF; border: 1px solid #464646; padding: 10px; border-radius: 5px; font-size: 11px; resize: none; font-family: monospace; }");
            writer.println("    .pix-code-area textarea:focus { outline: none; border-color: #009ee3; }");
            writer.println("    .btn-copiar { background: #00a650; color: #FFF; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-weight: bold; transition: background 0.3s; }");
            writer.println("    .btn-copiar:hover { background: #008f44; }");
            writer.println("    .btn-copiar-alt { background: #009ee3; color: #FFF; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-weight: bold; transition: background 0.3s; }");
            writer.println("    .btn-copiar-alt:hover { background: #0077b3; }");
            writer.println("    .status-pagamento { padding: 12px; border-radius: 8px; background: #856404; color: #FFF; font-weight: bold; margin-top: 10px; }");
            writer.println("    .status-pagamento.aprovado { background: #155724; }");
            writer.println("    .status-pagamento.erro { background: #721c24; }");
            writer.println("    .btn-fechar-modal { background: none; border: none; color: #888; cursor: pointer; margin-top: 15px; font-size: 13px; transition: color 0.3s; }");
            writer.println("    .btn-fechar-modal:hover { color: #FFF; }");
            writer.println("    .carrinho-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #3D3D3D; }");
            writer.println("    .carrinho-item-info { flex: 1; text-align: left; }");
            writer.println("    .carrinho-item-info .nome { font-size: 14px; }");
            writer.println("    .carrinho-item-info .detalhe { font-size: 12px; color: #A0A0A0; }");
            writer.println("    .carrinho-item-preco { font-weight: bold; color: #00a650; margin: 0 10px; }");
            writer.println("    .carrinho-item-remover { background: none; border: none; color: #e53e3e; cursor: pointer; font-size: 18px; }");
            writer.println("    .carrinho-resumo { border-top: 1px solid #464646; padding-top: 15px; }");
            writer.println("    .carrinho-resumo .linha { display: flex; justify-content: space-between; color: #A0A0A0; margin-top: 5px; }");
            writer.println("    .carrinho-resumo .total { display: flex; justify-content: space-between; font-size: 20px; font-weight: bold; color: #00a650; margin-top: 10px; padding-top: 10px; border-top: 1px solid #464646; }");
            writer.println("    .carrinho-vazio { color: #A0A0A0; padding: 30px 0; text-align: center; }");
            writer.println("    .notificacao { position: fixed; bottom: 20px; right: 20px; background: #00a650; color: #FFF; padding: 15px 25px; border-radius: 8px; z-index: 99999; box-shadow: 0 4px 12px rgba(0,0,0,0.3); animation: slideIn 0.5s ease; }");
            writer.println("    @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }");
            writer.println("  </style>");
            writer.println("</head>");
            writer.println("<body>");

            // ==========================================
            // HEADER COM CARRINHO
            // ==========================================
            writer.println("  <header>");
            writer.println("    <div>");
            writer.println("      <h1>PORTOBELLA</h1>");
            writer.println("      <p>Brechó & Outlet • Vitrine Virtual</p>");
            writer.println("    </div>");
            writer.println("    <div class='carrinho-icon' onclick='abrirCarrinho()'>");
            writer.println("      🛒");
            writer.println("      <span class='carrinho-contador' id='carrinhoContador'>0</span>");
            writer.println("    </div>");
            writer.println("  </header>");

            // ==========================================
            // BUSCA
            // ==========================================
            writer.println("  <div class='busca-container'>");
            writer.println("    <input type='text' id='inputBusca' onkeyup='filtrarProdutos()' placeholder='Buscar por nome, tamanho ou código...' class='busca-input'>");
            writer.println("  </div>");

            // ==========================================
            // VITRINE
            // ==========================================
            writer.println("  <div class='container'>");
            writer.println("    <div class='vitrine' id='listaVitrine'>");

            int contador = 0;
            System.out.println("\n============ INICIANDO EXTRAÇÃO DE PRODUTOS ============");

            while (rs.next()) {
                contador++;
                String codigoItem = rs.getString("codpeca");
                String descricao = rs.getString("itemdesc");
                String tamanho = rs.getString("tamanho");
                double preco = rs.getDouble("precosug");
                String imagensConcatenadas = rs.getString("imagem");

                String codExibicao = (codigoItem == null) ? "S/C" : codigoItem;

                // ==========================================
                // SEPARA AS IMAGENS POR ;
                // ==========================================
                String[] imagens = new String[0];
                if (imagensConcatenadas != null && !imagensConcatenadas.trim().isEmpty()) {
                    imagens = imagensConcatenadas.split(";");
                    for (int i = 0; i < imagens.length; i++) {
                        imagens[i] = imagens[i].trim();
                    }
                }

                // ==========================================
                // CARD DO PRODUTO
                // ==========================================
                writer.println("      <div class='card' data-busca='" + descricao.toLowerCase() + " tam: " + tamanho.toLowerCase() + " ref: " + codExibicao.toLowerCase() + "'>");

                // ==========================================
                // GALERIA DE IMAGENS (CARROSSEL)
                // ==========================================
                writer.println("        <div class='galeria' id='galeria-" + codExibicao + "'>");

                if (imagens.length == 0 || (imagens.length == 1 && imagens[0].isEmpty())) {
                    writer.println("          <div class='sem-foto'>Sem Foto</div>");
                } else {
                    int totalImagens = Math.min(imagens.length, 5);

                    for (int i = 0; i < totalImagens; i++) {
                        String classeAtivo = (i == 0) ? " ativo" : "";
                        writer.println("          <img src='" + imagens[i] + "' alt='" + descricao + " - Foto " + (i + 1) + "' class='slide" + classeAtivo + "'>");
                    }

                    if (totalImagens > 1) {
                        writer.println("          <button class='seta anterior' onclick='mudarSlide(\"" + codExibicao + "\", -1)'>❮</button>");
                        writer.println("          <button class='seta proximo' onclick='mudarSlide(\"" + codExibicao + "\", 1)'>❯</button>");

                        writer.println("          <div class='indicadores' id='indicadores-" + codExibicao + "'>");
                        for (int i = 0; i < totalImagens; i++) {
                            String classeDot = (i == 0) ? " ativo" : "";
                            writer.println("            <span class='dot" + classeDot + "' onclick='irParaSlide(\"" + codExibicao + "\", " + i + ")'></span>");
                        }
                        writer.println("          </div>");

                        writer.println("          <span class='contador-fotos'>1/" + totalImagens + "</span>");
                    }
                }

                writer.println("        </div>");

                // ==========================================
                // INFORMAÇÕES DO PRODUTO
                // ==========================================
                writer.println("        <h3>" + descricao + "</h3>");
                writer.println("        <div class='info'>Tamanho: " + tamanho + "</div>");
                writer.println("        <div class='codigo-item'>Código: " + codExibicao + "</div>");
                writer.println("        <div class='preco'>R$ " + String.format("%.2f", preco).replace(".", ",") + "</div>");
                writer.println("        <button class='btn-add-carrinho' onclick='adicionarAoCarrinho(\"" + codExibicao + "\", \"" + descricao.replace("\"", "\\\"") + "\", " + preco + ")'>");
                writer.println("          🛒 Adicionar ao Carrinho");
                writer.println("        </button>");
                writer.println("      </div>");
            }
            writer.println("    </div>");
            writer.println("  </div>");
            // ==========================================
            // RODAPÉ
            // ==========================================
            writer.println("  <footer>");
            writer.println("    <p>&copy; 2026 PORTOBELLA Brechó & Outlet. Todos os direitos reservados.</p>");
            writer.println("  </footer>");

            // ==========================================
            // MODAL DO CARRINHO (COM CHECKBOX E DESTINATÁRIO)
            // ==========================================
            writer.println("  <div id='modalCarrinho' class='modal'>");
            writer.println("    <div class='modal-content' style='max-width:600px;'>");
            writer.println("      <h3>🛒 Meu Carrinho</h3>");
            writer.println("");
            writer.println("      <div id='carrinhoItens' style='max-height:200px; overflow-y:auto; margin:10px 0;'></div>");
            writer.println("");
            writer.println("      <div class='carrinho-resumo'>");
            writer.println("        <div class='linha'><span>Subtotal:</span><span id='carrinhoSubtotal'>R$ 0,00</span></div>");
            writer.println("        <div class='linha'><span>Frete:</span><span id='carrinhoFrete'>R$ 0,00</span></div>");
            writer.println("        <div class='total'><span>TOTAL:</span><span id='carrinhoTotal'>R$ 0,00</span></div>");
            writer.println("      </div>");
            writer.println("");
            writer.println("      <!-- ========================================== -->");
            writer.println("      <!-- 🔥 OPÇÃO DE RETIRADA NA LOJA               -->");
            writer.println("      <!-- ========================================== -->");
            writer.println("      <div style='margin:12px 0; padding:15px; background:#1E1E1E; border-radius:8px; border:1px solid #464646;'>");
            writer.println("        <label style='color:#A0A0A0; font-size:13px; font-weight:bold; display:flex; align-items:center; gap:10px; cursor:pointer;'>");
            writer.println("          <input type='checkbox' id='chkRetirarLoja' onchange='toggleEntrega()' style='width:18px; height:18px; cursor:pointer;'>");
            writer.println("          📍 Retirar na Loja (Avenida Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre - RS)");
            writer.println("        </label>");
            writer.println("        <p style='color:#666; font-size:11px; margin-top:5px;'>⚠️ Ao marcar esta opção, o frete será ZERADO e o endereço não será necessário.</p>");
            writer.println("      </div>");
            writer.println("");
            writer.println("      <!-- ========================================== -->");
            writer.println("      <!-- DADOS DE ENTREGA (Destinatário + Endereço) -->");
            writer.println("      <!-- ========================================== -->");
            writer.println("      <div id='divEntrega' style='margin:12px 0; padding:15px; background:#1E1E1E; border-radius:8px; border:1px solid #464646;'>");
            writer.println("        <label style='color:#A0A0A0; font-size:13px; font-weight:bold;'>📦 Opção de Entrega</label>");
            writer.println("");
            writer.println("        <!-- ========================================== -->");
            writer.println("        <!-- 🔥 CAMPO DESTINATÁRIO                      -->");
            writer.println("        <!-- ========================================== -->");
            writer.println("        <div style='margin-top:8px;'>");
            writer.println("          <label style='color:#A0A0A0; font-size:12px; display:block; text-align:left;'>👤 Destinatário (quem vai receber)</label>");
            writer.println("          <input type='text' id='destinatario' placeholder='Nome completo do destinatário' style='width:100%; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:14px; margin-top:3px;'>");
            writer.println("        </div>");
            writer.println("        <!-- TELEFONE -->");
            writer.println("        <div style='margin-top:8px;'>");
            writer.println("          <label style='color:#A0A0A0; font-size:12px; display:block; text-align:left;'>📱 Telefone para contato</label>");
            writer.println("          <input type='tel' id='telefoneCliente' placeholder='(00) 00000-0000' style='width:100%; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:14px; margin-top:3px;'>");
            writer.println("        </div>");
            writer.println("");
            writer.println("        <!-- ========================================== -->");
            writer.println("        <!-- CALCULAR FRETE                           -->");
            writer.println("        <!-- ========================================== -->");
            writer.println("        <div style='margin-top:10px;'>");
            writer.println("          <label style='color:#A0A0A0; font-size:12px; display:block; text-align:left;'>📦 Calcular Frete</label>");
            writer.println("");
            writer.println("          <div style='display:flex; gap:10px; margin-top:5px;'>");
            writer.println("            <input type='text' id='cepCarrinho' placeholder='CEP (ex: 90240580)' style='flex:1; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:14px;'>");
            writer.println("            <button onclick='calcularFreteCarrinho()' style='padding:10px 20px; background:#009ee3; color:#FFF; border:none; border-radius:6px; cursor:pointer; white-space:nowrap; font-weight:bold;'>");
            writer.println("              🔍 Calcular");
            writer.println("            </button>");
            writer.println("          </div>");
            writer.println("");
            writer.println("          <div id='freteResultado' style='display:none; margin-top:8px; color:#FFF; font-size:14px;'>");
            writer.println("            ✅ <strong>Frete: R$ <span id='freteValor'>0,00</span></strong>");
            writer.println("            <span style='color:#A0A0A0; font-size:12px; margin-left:10px;'>(Prazo: <span id='fretePrazo'>-</span>)</span>");
            writer.println("          </div>");
            writer.println("        </div>");
            writer.println("");
            writer.println("        <!-- ========================================== -->");
            writer.println("        <!-- ENDEREÇO DE ENTREGA                      -->");
            writer.println("        <!-- ========================================== -->");
            writer.println("        <div id='enderecoCampos' style='display:none; margin-top:12px; padding-top:12px; border-top:1px solid #464646;'>");
            writer.println("          <label style='color:#A0A0A0; font-size:12px;'>📍 Endereço de Entrega</label>");
            writer.println("");
            writer.println("          <div style='display:flex; gap:10px; margin-top:5px;'>");
            writer.println("            <input type='text' id='endRua' placeholder='Rua' style='flex:2; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px;'>");
            writer.println("            <input type='text' id='endNumero' placeholder='Nº' style='flex:0.5; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px;'>");
            writer.println("          </div>");
            writer.println("");
            writer.println("          <div style='display:flex; gap:10px; margin-top:5px;'>");
            writer.println("            <input type='text' id='endComplemento' placeholder='Complemento' style='flex:1; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px;'>");
            writer.println("          </div>");
            writer.println("");
            writer.println("          <div style='display:flex; gap:10px; margin-top:5px;'>");
            writer.println("            <input type='text' id='endBairro' placeholder='Bairro' style='flex:1; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px;'>");
            writer.println("            <input type='text' id='endCidade' placeholder='Cidade' style='flex:1; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px;'>");
            writer.println("            <input type='text' id='endUf' placeholder='UF' style='flex:0.3; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px; text-transform:uppercase;'>");
            writer.println("          </div>");
            writer.println("        </div>");
            writer.println("      </div>");
            writer.println("");
            writer.println("      <!-- ========================================== -->");
            writer.println("      <!-- BOTÕES DE PAGAMENTO                       -->");
            writer.println("      <!-- ========================================== -->");
            writer.println("      <button class='btn-mercadopago' onclick='finalizarCompraCarrinho(\"mercado_pago\")'>");
            writer.println("        💳 PAGAR COM MERCADO PAGO");
            writer.println("      </button>");
            writer.println("      <button class='btn-pix' onclick='finalizarCompraCarrinho(\"pix\")'>");
            writer.println("        📱 PAGAR VIA PIX");
            writer.println("      </button>");
            writer.println("      <button class='btn-cancelar' onclick='fecharCarrinho()'>");
            writer.println("        ← Voltar à loja");
            writer.println("      </button>");
            writer.println("    </div>");
            writer.println("  </div>");
            // ==========================================
            // MODAL PIX (COM QR CODE INTEGRADO)
            // ==========================================
            writer.println("  <div id='modalPix' class='modal-pix' style='display:none;'>");
            writer.println("    <div class='modal-pix-content'>");
            writer.println("      <h3>💳 Pagamento via Pix</h3>");
            writer.println("      <div class='valor' id='modalValor'>R$ 0,00</div>");
            writer.println("      <div class='descricao' id='modalDescricao'>Pedido #000</div>");
            writer.println("");
            writer.println("      <div class='qr-container' id='qrCodeContainer'>");
            writer.println("        <p style='color:#888;font-size:13px;'>⏳ Gerando QR Code...</p>");
            writer.println("      </div>");
            writer.println("");
            writer.println("      <div class='pix-code-area'>");
            writer.println("        <label style='color:#A0A0A0;font-size:12px;display:block;text-align:left;margin-bottom:5px;'>📋 Código copia e cola:</label>");
            writer.println("        <textarea id='pixCode' rows='3' readonly style='width:100%;background:#1E1E1E;color:#FFF;border:1px solid #464646;padding:10px;border-radius:5px;font-size:11px;resize:none;font-family:monospace;'></textarea>");
            writer.println("        <div style='display:flex;gap:10px;margin-top:8px;'>");
            writer.println("          <button class='btn-copiar' onclick='copiarPix()' style='flex:1;background:#00a650;color:#FFF;padding:10px;border:none;border-radius:5px;cursor:pointer;font-weight:bold;'>");
            writer.println("            📋 Copiar código");
            writer.println("          </button>");
            writer.println("          <button onclick='copiarPixFallback()' style='flex:1;background:#009ee3;color:#FFF;padding:10px;border:none;border-radius:5px;cursor:pointer;font-weight:bold;'>");
            writer.println("            🔄 Copiar (alternativo)");
            writer.println("          </button>");
            writer.println("        </div>");
            writer.println("");
            writer.println("      <div id='statusPagamento' class='status-pagamento'>⏳ Aguardando pagamento...</div>");
            writer.println("");
            writer.println("      <button class='btn-fechar-modal' onclick='fecharModalPix()' style='background:none;border:none;color:#888;cursor:pointer;margin-top:15px;font-size:13px;'>");
            writer.println("        Cancelar");
            writer.println("      </button>");
            writer.println("    </div>");
            writer.println("  </div>");
            // ==========================================
            // JAVASCRIPT COMPLETO (COM NOTIFICAÇÃO PARA O SISTEMA DESKTOP)
            // ==========================================
            writer.println("  <script>");
            writer.println("    // ========================================");
            writer.println("    // FUNÇÕES DO CARROSSEL");
            writer.println("    // ========================================");
            writer.println("    function mudarSlide(id, direcao) {");
            writer.println("      const galeria = document.getElementById('galeria-' + id);");
            writer.println("      if (!galeria) return;");
            writer.println("      const slides = galeria.querySelectorAll('.slide');");
            writer.println("      const dots = galeria.querySelectorAll('.dot');");
            writer.println("      const contador = galeria.querySelector('.contador-fotos');");
            writer.println("      if (!slides || slides.length === 0) return;");
            writer.println("      let ativo = 0;");
            writer.println("      slides.forEach((slide, i) => { if (slide.classList.contains('ativo')) ativo = i; });");
            writer.println("      let novoAtivo = ativo + direcao;");
            writer.println("      if (novoAtivo < 0) novoAtivo = slides.length - 1;");
            writer.println("      if (novoAtivo >= slides.length) novoAtivo = 0;");
            writer.println("      slides.forEach(slide => slide.classList.remove('ativo'));");
            writer.println("      slides[novoAtivo].classList.add('ativo');");
            writer.println("      if (dots && dots.length > 0) {");
            writer.println("        dots.forEach(dot => dot.classList.remove('ativo'));");
            writer.println("        dots[novoAtivo].classList.add('ativo');");
            writer.println("      }");
            writer.println("      if (contador) {");
            writer.println("        contador.textContent = (novoAtivo + 1) + '/' + slides.length;");
            writer.println("      }");
            writer.println("    }");
            writer.println("");
            writer.println("    function irParaSlide(id, indice) {");
            writer.println("      const galeria = document.getElementById('galeria-' + id);");
            writer.println("      if (!galeria) return;");
            writer.println("      const slides = galeria.querySelectorAll('.slide');");
            writer.println("      const dots = galeria.querySelectorAll('.dot');");
            writer.println("      const contador = galeria.querySelector('.contador-fotos');");
            writer.println("      if (!slides || slides.length === 0 || indice < 0 || indice >= slides.length) return;");
            writer.println("      slides.forEach(slide => slide.classList.remove('ativo'));");
            writer.println("      slides[indice].classList.add('ativo');");
            writer.println("      if (dots && dots.length > 0) {");
            writer.println("        dots.forEach(dot => dot.classList.remove('ativo'));");
            writer.println("        dots[indice].classList.add('ativo');");
            writer.println("      }");
            writer.println("      if (contador) {");
            writer.println("        contador.textContent = (indice + 1) + '/' + slides.length;");
            writer.println("      }");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // CONFIGURAÇÕES");
            writer.println("    // ========================================");
            writer.println("    const URL_BACKEND = 'https://paginawebportobella-1.onrender.com';");
            writer.println("");
            writer.println("    let carrinho = { itens: [], frete: 0, cep: '' };");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // CARRINHO DE COMPRAS");
            writer.println("    // ========================================");
            writer.println("    function adicionarAoCarrinho(id, nome, preco) {");
            writer.println("      let itemExistente = carrinho.itens.find(item => item.id === id);");
            writer.println("      if(itemExistente) {");
            writer.println("        itemExistente.quantidade += 1;");
            writer.println("      } else {");
            writer.println("        carrinho.itens.push({ id: id, nome: nome, preco: preco, quantidade: 1 });");
            writer.println("      }");
            writer.println("      atualizarCarrinho();");
            writer.println("      mostrarNotificacao('✅ ' + nome + ' adicionado ao carrinho!');");
            writer.println("    }");
            writer.println("    window.adicionarAoCarrinho = adicionarAoCarrinho;");
            writer.println("");
            writer.println("    function removerDoCarrinho(id) {");
            writer.println("      carrinho.itens = carrinho.itens.filter(item => item.id !== id);");
            writer.println("      atualizarCarrinho();");
            writer.println("      if(carrinho.itens.length === 0) fecharCarrinho();");
            writer.println("      else renderizarCarrinho();");
            writer.println("    }");
            writer.println("");
            writer.println("    function atualizarCarrinho() {");
            writer.println("      let total = carrinho.itens.reduce((sum, item) => sum + item.quantidade, 0);");
            writer.println("      document.getElementById('carrinhoContador').textContent = total;");
            writer.println("    }");
            writer.println("");
            writer.println("    function abrirCarrinho() {");
            writer.println("      if(carrinho.itens.length === 0) {");
            writer.println("        alert('🛒 Seu carrinho está vazio!');");
            writer.println("        return;");
            writer.println("      }");
            writer.println("      renderizarCarrinho();");
            writer.println("      document.getElementById('modalCarrinho').style.display = 'flex';");
            writer.println("    }");
            writer.println("");
            writer.println("    function fecharCarrinho() {");
            writer.println("      document.getElementById('modalCarrinho').style.display = 'none';");
            writer.println("    }");
            writer.println("");
            writer.println("    function renderizarCarrinho() {");
            writer.println("      let container = document.getElementById('carrinhoItens');");
            writer.println("      let subtotal = 0;");
            writer.println("");
            writer.println("      if(carrinho.itens.length === 0) {");
            writer.println("        container.innerHTML = '<div class=\"carrinho-vazio\">🛒 Seu carrinho está vazio</div>';");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      let html = '';");
            writer.println("      carrinho.itens.forEach(item => {");
            writer.println("        subtotal += item.preco * item.quantidade;");
            writer.println("        html += '<div class=\"carrinho-item\">';");
            writer.println("        html += '  <div class=\"carrinho-item-info\">';");
            writer.println("        html += '    <div class=\"nome\">' + item.nome + '</div>';");
            writer.println("        html += '    <div class=\"detalhe\">Qtd: ' + item.quantidade + ' x R$ ' + item.preco.toFixed(2).replace('.', ',') + '</div>';");
            writer.println("        html += '  </div>';");
            writer.println("        html += '  <span class=\"carrinho-item-preco\">R$ ' + (item.preco * item.quantidade).toFixed(2).replace('.', ',') + '</span>';");
            writer.println("        html += '  <button class=\"carrinho-item-remover\" onclick=\"removerDoCarrinho(\\'' + item.id + '\\')\">🗑️</button>';");
            writer.println("        html += '</div>';");
            writer.println("      });");
            writer.println("");
            writer.println("      container.innerHTML = html;");
            writer.println("");
            writer.println("      document.getElementById('carrinhoSubtotal').textContent = 'R$ ' + subtotal.toFixed(2).replace('.', ',');");
            writer.println("      let total = subtotal + carrinho.frete;");
            writer.println("      document.getElementById('carrinhoTotal').textContent = 'R$ ' + total.toFixed(2).replace('.', ',');");
            writer.println("      document.getElementById('carrinhoFrete').textContent = 'R$ ' + carrinho.frete.toFixed(2).replace('.', ',');");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // CALCULAR FRETE");
            writer.println("    // ========================================");
            writer.println("    function calcularFreteCarrinho() {");
            writer.println("      let cep = document.getElementById('cepCarrinho').value.trim().replace(/\\D/g, '');");
            writer.println("");
            writer.println("      if(cep.length !== 8) {");
            writer.println("        alert('Digite um CEP válido com 8 dígitos (ex: 90240580)');");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      let botao = document.querySelector('#cepCarrinho + button');");
            writer.println("      botao.disabled = true;");
            writer.println("      botao.textContent = '⏳...';");
            writer.println("");
            writer.println("      fetch(URL_BACKEND + '/api/frete/calcular?cep=' + cep)");
            writer.println("        .then(response => response.json())");
            writer.println("        .then(data => {");
            writer.println("          botao.disabled = false;");
            writer.println("          botao.textContent = '🔍 Calcular';");
            writer.println("");
            writer.println("          if(data.success) {");
            writer.println("            carrinho.frete = data.frete;");
            writer.println("            carrinho.cep = cep;");
            writer.println("");
            writer.println("            let resultado = document.getElementById('freteResultado');");
            writer.println("            resultado.style.display = 'block';");
            writer.println("            document.getElementById('freteValor').textContent = 'R$ ' + data.frete.toFixed(2).replace('.', ',');");
            writer.println("            document.getElementById('fretePrazo').textContent = data.prazo || 'Não informado';");
            writer.println("");
            writer.println("            preencherEndereco(data);");
            writer.println("");
            writer.println("            renderizarCarrinho();");
            writer.println("");
            writer.println("          } else {");
            writer.println("            alert('❌ Erro ao calcular frete: ' + (data.error || 'Erro desconhecido'));");
            writer.println("          }");
            writer.println("        })");
            writer.println("        .catch(error => {");
            writer.println("          botao.disabled = false;");
            writer.println("          botao.textContent = '🔍 Calcular';");
            writer.println("          alert('❌ Erro ao conectar: ' + error.message);");
            writer.println("        });");
            writer.println("    }");
            writer.println("");
            writer.println("    function preencherEndereco(dados) {");
            writer.println("      let enderecoDiv = document.getElementById('enderecoCampos');");
            writer.println("      enderecoDiv.style.display = 'block';");
            writer.println("");
            writer.println("      if(dados.logradouro) {");
            writer.println("        document.getElementById('endRua').value = dados.logradouro || '';");
            writer.println("      }");
            writer.println("      if(dados.bairro) {");
            writer.println("        document.getElementById('endBairro').value = dados.bairro || '';");
            writer.println("      }");
            writer.println("      if(dados.cidade) {");
            writer.println("        document.getElementById('endCidade').value = dados.cidade || '';");
            writer.println("      }");
            writer.println("      if(dados.uf) {");
            writer.println("        document.getElementById('endUf').value = dados.uf || '';");
            writer.println("      }");
            writer.println("");
            writer.println("      if(!dados.logradouro && !dados.bairro) {");
            writer.println("        buscarEnderecoCompleto(dados.cep);");
            writer.println("      }");
            writer.println("");
            writer.println("      setTimeout(() => {");
            writer.println("        document.getElementById('endNumero').focus();");
            writer.println("      }, 300);");
            writer.println("");
            writer.println("      setTimeout(atualizarEnderecoCompleto, 500);");
            writer.println("    }");
            writer.println("");
            writer.println("    function buscarEnderecoCompleto(cep) {");
            writer.println("      fetch('https://viacep.com.br/ws/' + cep + '/json/')");
            writer.println("        .then(response => response.json())");
            writer.println("        .then(data => {");
            writer.println("          if(!data.erro) {");
            writer.println("            document.getElementById('endRua').value = data.logradouro || '';");
            writer.println("            document.getElementById('endBairro').value = data.bairro || '';");
            writer.println("            document.getElementById('endCidade').value = data.localidade || '';");
            writer.println("            document.getElementById('endUf').value = data.uf || '';");
            writer.println("          }");
            writer.println("        })");
            writer.println("        .catch(err => console.log('Erro ao buscar endereço:', err));");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // ALTERNAR ENTRE RETIRADA NA LOJA E ENTREGA");
            writer.println("    // ========================================");
            writer.println("    function toggleEntrega() {");
            writer.println("      let chkRetirar = document.getElementById('chkRetirarLoja');");
            writer.println("      let divEntrega = document.getElementById('divEntrega');");
            writer.println("      let freteResultado = document.getElementById('freteResultado');");
            writer.println("      let cepInput = document.getElementById('cepCarrinho');");
            writer.println("      let botaoFrete = document.querySelector('#cepCarrinho + button');");
            writer.println("");
            writer.println("      if (chkRetirar.checked) {");
            writer.println("        divEntrega.style.display = 'none';");
            writer.println("        freteResultado.style.display = 'none';");
            writer.println("        carrinho.frete = 0;");
            writer.println("        cepInput.disabled = true;");
            writer.println("        if (botaoFrete) botaoFrete.disabled = true;");
            writer.println("        document.getElementById('enderecoCompleto').value = 'RETIRADA NA LOJA - ' + (document.getElementById('destinatario').value || 'Cliente');");
            writer.println("        mostrarNotificacao('✅ Retirada na loja selecionada! Frete zerado.');");
            writer.println("      } else {");
            writer.println("        divEntrega.style.display = 'block';");
            writer.println("        cepInput.disabled = false;");
            writer.println("        if (botaoFrete) botaoFrete.disabled = false;");
            writer.println("");
            writer.println("        if (cepInput.value.trim().replace(/\\D/g, '').length === 8) {");
            writer.println("          calcularFreteCarrinho();");
            writer.println("        } else {");
            writer.println("          carrinho.frete = 0;");
            writer.println("          renderizarCarrinho();");
            writer.println("        }");
            writer.println("        atualizarEnderecoCompleto();");
            writer.println("      }");
            writer.println("");
            writer.println("      renderizarCarrinho();");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // ATUALIZAR ENDEREÇO COMPLETO (CAMPO OCULTO)");
            writer.println("    // ========================================");
            writer.println("    function atualizarEnderecoCompleto() {");
            writer.println("      let rua = document.getElementById('endRua').value.trim();");
            writer.println("      let numero = document.getElementById('endNumero').value.trim();");
            writer.println("      let complemento = document.getElementById('endComplemento').value.trim();");
            writer.println("      let bairro = document.getElementById('endBairro').value.trim();");
            writer.println("      let cidade = document.getElementById('endCidade').value.trim();");
            writer.println("      let uf = document.getElementById('endUf').value.trim().toUpperCase();");
            writer.println("      let cep = document.getElementById('cepCarrinho').value.trim();");
            writer.println("");
            writer.println("      let endereco = '';");
            writer.println("      if (rua) endereco += rua;");
            writer.println("      if (numero) endereco += ', ' + numero;");
            writer.println("      if (complemento) endereco += ' - ' + complemento;");
            writer.println("      if (bairro) endereco += ' - ' + bairro;");
            writer.println("      if (cidade) endereco += ' - ' + cidade;");
            writer.println("      if (uf) endereco += '/' + uf;");
            writer.println("      if (cep) endereco += ' | CEP: ' + cep;");
            writer.println("");
            writer.println("      document.getElementById('enderecoCompleto').value = endereco;");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // FINALIZAR COMPRA (GERA PAGAMENTO - NÃO NOTIFICA)");
            writer.println("    // ========================================");
            writer.println("    function finalizarCompraCarrinho(metodo) {");
            writer.println("      if(carrinho.itens.length === 0) {");
            writer.println("        alert('Seu carrinho está vazio!');");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      let retirarLoja = document.getElementById('chkRetirarLoja').checked;");
            writer.println("      let destinatario = document.getElementById('destinatario').value.trim();");
            writer.println("      let telefone = document.getElementById('telefoneCliente').value.trim();");
            writer.println("");
            writer.println("      if (!destinatario || destinatario === '') {");
            writer.println("        alert('⚠️ Por favor, informe o nome de quem vai receber/retirar o pedido!');");
            writer.println("        document.getElementById('destinatario').focus();");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      let rua = document.getElementById('endRua').value.trim();");
            writer.println("      let numero = document.getElementById('endNumero').value.trim();");
            writer.println("      let complemento = document.getElementById('endComplemento').value.trim();");
            writer.println("      let bairro = document.getElementById('endBairro').value.trim();");
            writer.println("      let cidade = document.getElementById('endCidade').value.trim();");
            writer.println("      let uf = document.getElementById('endUf').value.trim().toUpperCase();");
            writer.println("      let cep = document.getElementById('cepCarrinho').value.trim().replace(/\\D/g, '');");
            writer.println("");
            writer.println("      let enderecoCompleto = '';");
            writer.println("");
            writer.println("      if (retirarLoja) {");
            writer.println("        enderecoCompleto = 'RETIRADA NA LOJA - ' + destinatario + ' | Tel: ' + (telefone || 'Não informado') + ' | Av. Cristóvão Colombo, 2149 - Loja 15 - Moinhos de Vento - Porto Alegre/RS';");
            writer.println("        carrinho.frete = 0;");
            writer.println("        console.log('📦 Retirada na loja para: ' + destinatario);");
            writer.println("      } else {");
            writer.println("        if(!rua || !numero || !bairro || !cidade || !uf || cep.length !== 8) {");
            writer.println("          alert('⚠️ Por favor, preencha todos os campos do endereço:\\n\\n' +");
            writer.println("            '• Rua\\n' +");
            writer.println("            '• Número\\n' +");
            writer.println("            '• Bairro\\n' +");
            writer.println("            '• Cidade\\n' +");
            writer.println("            '• UF\\n' +");
            writer.println("            '• CEP (8 dígitos)');");
            writer.println("          return;");
            writer.println("        }");
            writer.println("");
            writer.println("        enderecoCompleto = 'Destinatário: ' + destinatario + ' | Tel: ' + (telefone || 'Não informado') + ' | ';");
            writer.println("        enderecoCompleto += rua + ', ' + numero;");
            writer.println("        if(complemento) enderecoCompleto += ' - ' + complemento;");
            writer.println("        enderecoCompleto += ' - ' + bairro + ' - ' + cidade + '/' + uf;");
            writer.println("        enderecoCompleto += ' | CEP: ' + cep;");
            writer.println("");
            writer.println("        console.log('📦 Entrega para: ' + destinatario);");
            writer.println("        console.log('📍 Endereço: ' + enderecoCompleto);");
            writer.println("      }");
            writer.println("");
            writer.println("      let subtotal = carrinho.itens.reduce((total, item) => total + (item.preco * item.quantidade), 0);");
            writer.println("      let valorTotal = subtotal + carrinho.frete;");
            writer.println("      let pedidoId = String(Date.now());");
            writer.println("");
            writer.println("      fecharCarrinho();");
            writer.println("");
            writer.println("      let loading = document.createElement('div');");
            writer.println("      loading.id = 'loadingFinal';");
            writer.println("      loading.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.8);z-index:9999;display:flex;align-items:center;justify-content:center;color:#FFF;font-size:18px;';");
            writer.println("      loading.innerHTML = '⏳ Processando pedido...<br><small>Aguarde</small>';");
            writer.println("      document.body.appendChild(loading);");
            writer.println("");
            writer.println("      let itens = carrinho.itens.map(item => ({ id: item.id, nome: item.nome, preco: item.preco, quantidade: item.quantidade }));");
            writer.println("");
            writer.println("      window.dadosCompra = {");
            writer.println("        itens: itens,");
            writer.println("        subtotal: subtotal,");
            writer.println("        valorTotal: valorTotal,");
            writer.println("        cep: retirarLoja ? '00000000' : cep,");
            writer.println("        endereco: enderecoCompleto,");
            writer.println("        destinatario: destinatario,");
            writer.println("        telefone: telefone || 'Não informado',");
            writer.println("        retirarLoja: retirarLoja,");
            writer.println("        pedidoId: pedidoId");
            writer.println("      };");
            writer.println("");
            writer.println("      fetch(URL_BACKEND + '/api/pagamentos/finalizar', {");
            writer.println("        method: 'POST',");
            writer.println("        headers: { 'Content-Type': 'application/json' },");
            writer.println("        body: JSON.stringify({");
            writer.println("          meio: metodo,");
            writer.println("          itens: itens,");
            writer.println("          subtotal: subtotal,");
            writer.println("          frete: carrinho.frete,");
            writer.println("          total: valorTotal,");
            writer.println("          cep: retirarLoja ? '00000000' : cep,");
            writer.println("          endereco: enderecoCompleto,");
            writer.println("          destinatario: destinatario,");
            writer.println("          telefone: telefone || 'Não informado',");
            writer.println("          retirarLoja: retirarLoja,");
            writer.println("          pedidoId: pedidoId");
            writer.println("        })");
            writer.println("      })");
            writer.println("      .then(response => response.json())");
            writer.println("      .then(data => {");
            writer.println("        document.getElementById('loadingFinal')?.remove();");
            writer.println("");
            writer.println("        if(data.success) {");
            writer.println("          if(metodo === 'pix') {");
            writer.println("            exibirModalPix(data.payload, data.total, 'Pedido #' + data.pedidoId);");
            writer.println("          } else {");
            writer.println("            window.location.href = data.paymentUrl;");
            writer.println("          }");
            writer.println("          carrinho.itens = [];");
            writer.println("          carrinho.frete = 0;");
            writer.println("          atualizarCarrinho();");
            writer.println("        } else {");
            writer.println("          alert('❌ Erro ao finalizar compra: ' + (data.error || 'Erro desconhecido'));");
            writer.println("        }");
            writer.println("      })");
            writer.println("      .catch(error => {");
            writer.println("        document.getElementById('loadingFinal')?.remove();");
            writer.println("        alert('❌ Erro ao conectar: ' + error.message);");
            writer.println("      });");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // FILTRO DE PRODUTOS");
            writer.println("    // ========================================");
            writer.println("    function filtrarProdutos() {");
            writer.println("      let input = document.getElementById('inputBusca').value.toLowerCase();");
            writer.println("      let cards = document.getElementsByClassName('card');");
            writer.println("      for (let i = 0; i < cards.length; i++) {");
            writer.println("        let txtValue = cards[i].getAttribute('data-busca') || '';");
            writer.println("        cards[i].style.display = txtValue.indexOf(input) > -1 ? '' : 'none';");
            writer.println("      }");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // FUNÇÕES DO PIX");
            writer.println("    // ========================================");
            writer.println("");
            writer.println("    function exibirModalPix(payload, valor, nome) {");
            writer.println("      console.log('📱 Exibindo modal Pix...');");
            writer.println("");
            writer.println("      let container = document.getElementById('qrCodeContainer');");
            writer.println("      container.innerHTML = '<p style=\"color:#888;font-size:13px;\">⏳ Gerando QR Code...</p>';");
            writer.println("");
            writer.println("      document.getElementById('modalValor').textContent = 'R$ ' + valor.toFixed(2).replace('.', ',');");
            writer.println("      document.getElementById('modalDescricao').textContent = nome || 'Pedido #' + Date.now();");
            writer.println("      document.getElementById('pixCode').value = payload || '';");
            writer.println("");
            writer.println("      if (!payload || payload.trim() === '') {");
            writer.println("        container.innerHTML = '<p style=\"color:#ff6b6b;font-size:13px;\">⚠️ Erro: Código Pix não disponível</p>';");
            writer.println("        document.getElementById('statusPagamento').className = 'status-pagamento';");
            writer.println("        document.getElementById('statusPagamento').textContent = '❌ Erro ao gerar Pix';");
            writer.println("        document.getElementById('modalPix').style.display = 'flex';");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      try {");
            writer.println("        if (typeof QRCode === 'undefined') {");
            writer.println("          console.warn('⚠️ QRCode.js não carregado, tentando carregar...');");
            writer.println("          carregarQRCodeLib(function() {");
            writer.println("            gerarQRCode(payload, container);");
            writer.println("          });");
            writer.println("        } else {");
            writer.println("          console.log('✅ QRCode.js já carregado');");
            writer.println("          gerarQRCode(payload, container);");
            writer.println("        }");
            writer.println("      } catch(e) {");
            writer.println("        console.error('❌ Erro ao gerar QR Code:', e);");
            writer.println("        container.innerHTML = '<p style=\"color:#ff6b6b;font-size:13px;\">⚠️ Erro ao gerar QR Code.<br>Use o código copia e cola.</p>';");
            writer.println("      }");
            writer.println("");
            writer.println("      document.getElementById('modalPix').style.display = 'flex';");
            writer.println("      document.getElementById('statusPagamento').className = 'status-pagamento';");
            writer.println("      document.getElementById('statusPagamento').textContent = '⏳ Aguardando pagamento...';");
            writer.println("");
            writer.println("      setTimeout(() => {");
            writer.println("        if (document.getElementById('modalPix').style.display === 'flex') {");
            writer.println("          if (confirm('✅ Já realizou o pagamento?\\n\\nClique em OK para confirmar.')) {");
            writer.println("");
            writer.println("            let statusDiv = document.getElementById('statusPagamento');");
            writer.println("            statusDiv.className = 'status-pagamento';");
            writer.println("            statusDiv.textContent = '📤 Notificando a loja...';");
            writer.println("");
            writer.println("            let dados = window.dadosCompra || {};");
            writer.println("");
            writer.println("            let dadosNotificacao = {");
            writer.println("              codPeca: dados.itens?.[0]?.id || 'DESCONHECIDO',");
            writer.println("              destinatario: dados.destinatario || 'Cliente',");
            writer.println("              telefone: dados.telefone || 'Não informado',");
            writer.println("              total: valor,");
            writer.println("              meio: 'pix',");
            writer.println("              endereco: dados.endereco || 'Não informado',");
            writer.println("              retirarLoja: dados.retirarLoja || false,");
            writer.println("              pedidoId: dados.pedidoId || String(Date.now()),");
            writer.println("              itens: dados.itens || []");
            writer.println("            };");
            writer.println("");
            writer.println("            console.log('📤 Notificando sistema desktop (pagamento confirmado)...', dadosNotificacao);");
            writer.println("");
            writer.println("            fetch(URL_BACKEND + '/api/pagamentos/notificar', {");
            writer.println("              method: 'POST',");
            writer.println("              headers: { 'Content-Type': 'application/json' },");
            writer.println("              body: JSON.stringify(dadosNotificacao)");
            writer.println("            })");
            writer.println("            .then(response => response.json())");
            writer.println("            .then(data => {");
            writer.println("              console.log('📥 Resposta:', data);");
            writer.println("");
            writer.println("              statusDiv.className = 'status-pagamento aprovado';");
            writer.println("              statusDiv.textContent = '✅ Pagamento realizado!\\nA loja irá confirmar em breve.';");
            writer.println("              mostrarNotificacao('✅ Pagamento realizado! Aguarde a confirmação da loja.');");
            writer.println("");
            writer.println("              setTimeout(() => {");
            writer.println("                fecharModalPix();");
            writer.println("                alert('✅ Pedido enviado para a loja!\\n\\nAguardando confirmação do pagamento.\\nObrigado pela compra!');");
            writer.println("                window.dadosCompra = null;");
            writer.println("                location.reload();");
            writer.println("              }, 3000);");
            writer.println("            })");
            writer.println("            .catch(error => {");
            writer.println("              console.error('❌ Erro ao notificar:', error);");
            writer.println("              statusDiv.className = 'status-pagamento';");
            writer.println("              statusDiv.textContent = '✅ Pagamento realizado!\\nA loja será notificada em breve.';");
            writer.println("");
            writer.println("              setTimeout(() => {");
            writer.println("                fecharModalPix();");
            writer.println("                alert('✅ Pedido enviado para a loja!\\n\\nAguardando confirmação do pagamento.\\nObrigado pela compra!');");
            writer.println("                window.dadosCompra = null;");
            writer.println("                location.reload();");
            writer.println("              }, 3000);");
            writer.println("            });");
            writer.println("          }");
            writer.println("        }");
            writer.println("      }, 15000);");
            writer.println("    }");
            writer.println("");
            writer.println("    function carregarQRCodeLib(callback) {");
            writer.println("      let script = document.createElement('script');");
            writer.println("      script.src = 'https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js';");
            writer.println("      script.onload = function() {");
            writer.println("        console.log('✅ QRCode.js carregado com sucesso!');");
            writer.println("        if (callback) callback();");
            writer.println("      };");
            writer.println("      script.onerror = function() {");
            writer.println("        console.error('❌ Erro ao carregar QRCode.js');");
            writer.println("        let container = document.getElementById('qrCodeContainer');");
            writer.println("        container.innerHTML = '<p style=\"color:#ff6b6b;font-size:13px;\">⚠️ Erro ao carregar biblioteca QR Code.<br>Use o código copia e cola.</p>';");
            writer.println("      };");
            writer.println("      document.head.appendChild(script);");
            writer.println("    }");
            writer.println("");
            writer.println("    function gerarQRCode(payload, container) {");
            writer.println("      try {");
            writer.println("        container.innerHTML = '';");
            writer.println("        new QRCode(container, {");
            writer.println("          text: payload,");
            writer.println("          width: 200,");
            writer.println("          height: 200,");
            writer.println("          colorDark: '#000000',");
            writer.println("          colorLight: '#FFFFFF',");
            writer.println("          correctLevel: QRCode.CorrectLevel.H");
            writer.println("        });");
            writer.println("        console.log('✅ QR Code gerado com sucesso!');");
            writer.println("      } catch(e) {");
            writer.println("        console.error('❌ Erro ao gerar QR Code:', e);");
            writer.println("        container.innerHTML = '<p style=\"color:#ff6b6b;font-size:13px;\">⚠️ Erro ao gerar QR Code.<br>Use o código copia e cola.</p>';");
            writer.println("      }");
            writer.println("    }");
            writer.println("");
            writer.println("    function copiarPix() {");
            writer.println("      let texto = document.getElementById('pixCode');");
            writer.println("");
            writer.println("      if (!texto || !texto.value || texto.value.trim() === '') {");
            writer.println("        alert('⚠️ Nenhum código Pix para copiar!');");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      if (navigator.clipboard && navigator.clipboard.writeText) {");
            writer.println("        navigator.clipboard.writeText(texto.value)");
            writer.println("          .then(() => {");
            writer.println("            mostrarNotificacao('✅ Código Pix copiado com sucesso!');");
            writer.println("          })");
            writer.println("          .catch(err => {");
            writer.println("            console.warn('⚠️ Clipboard API falhou:', err);");
            writer.println("            copiarPixFallback();");
            writer.println("          });");
            writer.println("      } else {");
            writer.println("        copiarPixFallback();");
            writer.println("      }");
            writer.println("    }");
            writer.println("");
            writer.println("    function copiarPixFallback() {");
            writer.println("      let texto = document.getElementById('pixCode');");
            writer.println("");
            writer.println("      if (!texto || !texto.value || texto.value.trim() === '') {");
            writer.println("        alert('⚠️ Nenhum código Pix para copiar!');");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      try {");
            writer.println("        texto.select();");
            writer.println("        texto.setSelectionRange(0, 99999);");
            writer.println("        let sucesso = document.execCommand('copy');");
            writer.println("        if (sucesso) {");
            writer.println("          mostrarNotificacao('✅ Código Pix copiado com sucesso!');");
            writer.println("        } else {");
            writer.println("          alert('⚠️ Selecione o código manualmente e copie (Ctrl+C).');");
            writer.println("        }");
            writer.println("      } catch(e) {");
            writer.println("        console.error('❌ Erro ao copiar:', e);");
            writer.println("        alert('⚠️ Erro ao copiar. Selecione o código manualmente.');");
            writer.println("      }");
            writer.println("    }");
            writer.println("");
            writer.println("    function fecharModalPix() {");
            writer.println("      document.getElementById('modalPix').style.display = 'none';");
            writer.println("      let container = document.getElementById('qrCodeContainer');");
            writer.println("      container.innerHTML = '<p style=\"color:#888;font-size:13px;\">⏳ Gerando QR Code...</p>';");
            writer.println("    }");
            writer.println("");
            writer.println("    function mostrarNotificacao(mensagem) {");
            writer.println("      let notificacao = document.createElement('div');");
            writer.println("      notificacao.className = 'notificacao';");
            writer.println("      notificacao.textContent = mensagem;");
            writer.println("      notificacao.style.cssText = 'position:fixed;bottom:20px;right:20px;background:#00a650;color:#FFF;padding:15px 25px;border-radius:8px;z-index:99999;box-shadow:0 4px 12px rgba(0,0,0,0.3);animation:slideIn 0.5s ease;';");
            writer.println("      document.body.appendChild(notificacao);");
            writer.println("      setTimeout(() => {");
            writer.println("        notificacao.style.opacity = '0';");
            writer.println("        notificacao.style.transition = 'opacity 0.5s';");
            writer.println("        setTimeout(() => notificacao.remove(), 500);");
            writer.println("      }, 3000);");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // CARREGAR QRCODE QUANDO A PÁGINA CARREGAR");
            writer.println("    // ========================================");
            writer.println("    document.addEventListener('DOMContentLoaded', function() {");
            writer.println("      console.log('📱 Página carregada.');");
            writer.println("      console.log('QRCode.js disponível:', typeof QRCode !== 'undefined' ? '✅ SIM' : '❌ NÃO');");
            writer.println("");
            writer.println("      if (typeof QRCode === 'undefined') {");
            writer.println("        console.log('🔄 Carregando QRCode.js...');");
            writer.println("        carregarQRCodeLib(function() {");
            writer.println("          console.log('✅ QRCode.js carregado com sucesso!');");
            writer.println("        });");
            writer.println("      }");
            writer.println("    });");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // EVENTOS PARA ATUALIZAR ENDEREÇO COMPLETO");
            writer.println("    // ========================================");
            writer.println("    document.addEventListener('DOMContentLoaded', function() {");
            writer.println("      let campos = ['endRua', 'endNumero', 'endComplemento', 'endBairro', 'endCidade', 'endUf', 'cepCarrinho'];");
            writer.println("      campos.forEach(id => {");
            writer.println("        let el = document.getElementById(id);");
            writer.println("        if (el) {");
            writer.println("          el.addEventListener('change', atualizarEnderecoCompleto);");
            writer.println("          el.addEventListener('keyup', atualizarEnderecoCompleto);");
            writer.println("        }");
            writer.println("      });");
            writer.println("    });");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // FECHAR MODAIS COM ESC");
            writer.println("    // ========================================");
            writer.println("    document.addEventListener('keydown', function(event) {");
            writer.println("      if(event.key === 'Escape') {");
            writer.println("        fecharCarrinho();");
            writer.println("        fecharModalPix();");
            writer.println("      }");
            writer.println("    });");
            writer.println("");
            writer.println("    document.getElementById('modalCarrinho').addEventListener('click', function(event) {");
            writer.println("      if(event.target === this) fecharCarrinho();");
            writer.println("    });");
            writer.println("");
            writer.println("    document.getElementById('modalPix').addEventListener('click', function(event) {");
            writer.println("      if(event.target === this) fecharModalPix();");
            writer.println("    });");
            writer.println("  </script>");
            writer.println("</body>");
            writer.println("</html>");
            
            System.out.println("Catálogo HTML PORTOBELLA gerado com sucesso!");
            System.out.println("📁 Arquivo: " + caminhoArquivo);
            System.out.println("Total de produtos: " + contador);

            // ==========================================
            // GIT AUTOMÁTICO
            // ==========================================
//            System.out.println("Iniciando sincronização automática com o GitHub...");
//            try {
//                java.io.File pastaOrigem = new java.io.File(diretorioDocumentos);
//
//                Process remoteCheck = Runtime.getRuntime().exec(new String[]{"git", "remote", "get-url", "origin"}, null, pastaOrigem);
//                int remoteExit = remoteCheck.waitFor();
//
//                if (remoteExit != 0) {
//                    String token = "ghp_jbHaoTzV1RpikgH8fsAtqxFCnT3LlK3wvKXA";
//                    String remoteUrl = "https://srsteinmetz12:" + token + "@github.com/srsteinmetz12/paginawebportobella.git";
//                    Process addRemote = Runtime.getRuntime().exec(new String[]{"git", "remote", "add", "origin", remoteUrl}, null, pastaOrigem);
//                    addRemote.waitFor();
//                    System.out.println("📌 Remote configurado.");
//                }
//
//                Process addProcess = Runtime.getRuntime().exec(new String[]{"git", "add", "-f", "index.html"}, null, pastaOrigem);
//                int addResult = addProcess.waitFor();
//                System.out.println("git add resultado: " + addResult);
//
//                if (addResult == 0) {
//                    Process commitProcess = Runtime.getRuntime().exec(new String[]{"git", "commit", "-m", "Atualização automática do estoque com carrossel e PIX"}, null, pastaOrigem);
//                    int commitResult = commitProcess.waitFor();
//                    System.out.println("git commit resultado: " + commitResult);
//
//                    if (commitResult != 0) {
//                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(commitProcess.getErrorStream()));
//                        String line;
//                        System.out.println("⚠️ Erro no commit:");
//                        while ((line = errorReader.readLine()) != null) {
//                            System.out.println("   " + line);
//                        }
//                        errorReader.close();
//                    }
//
//                    Process pushProcess = Runtime.getRuntime().exec(new String[]{"git", "push", "origin", "main", "--force"}, null, pastaOrigem);
//                    int pushResult = pushProcess.waitFor();
//                    System.out.println("git push resultado: " + pushResult);
//
//                    if (pushResult == 0) {
//                        System.out.println("🚀 SUCESSO! O catálogo está atualizado e online!");
//                    } else {
//                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(pushProcess.getErrorStream()));
//                        String line;
//                        System.out.println("⚠️ Erro no push:");
//                        while ((line = errorReader.readLine()) != null) {
//                            System.out.println("   " + line);
//                        }
//                        errorReader.close();
//                    }
//
//                    Process resetProcess = Runtime.getRuntime().exec(new String[]{"git", "reset", "HEAD", "index.html"}, null, pastaOrigem);
//                    resetProcess.waitFor();
//                } else {
//                    System.out.println("⚠️ Erro ao adicionar arquivo index.html");
//                }
//
//            } catch (IOException | InterruptedException e) {
//                System.err.println("⚠️ Erro ao executar Git: " + e.getMessage());
//            }
            enviarParaGitHub(diretorioDocumentos);
        } catch (java.io.IOException ex) {
            System.err.println("Erro ao escrever arquivo HTML: " + ex.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (java.sql.SQLException e) { /* ignore */ }
            }
            if (con2 != null) {
                try { con2.close(); } catch (java.sql.SQLException e) { /* ignore */ }
            }
            System.out.println("Conexão Cloud encerrada!");
            System.out.println("----------------------------------");
        }
    }

    // ==========================================
    // MAIN PARA TESTE
    // ==========================================
    public static void main(String[] args) {
        try {
            GerarSiteEstoque gerador = new GerarSiteEstoque();
            gerador.gerarSiteEstoque();
        } catch (ClassNotFoundException | InterruptedException | SQLException e) {
            System.err.println("❌ Erro ao gerar site: " + e.getMessage());
        }
    }
    
    private void enviarParaGitHub(String diretorioDocumentos) {
        System.out.println("📤 [GIT] Iniciando envio para o GitHub...");

        try {
            File pastaOrigem = new File(diretorioDocumentos);

            // ==========================================
            // 1. VERIFICAR SE O ARQUIVO INDEX.HTML EXISTE
            // ==========================================
            File indexFile = new File(pastaOrigem, "index.html");
            if (!indexFile.exists()) {
                System.err.println("❌ [GIT] Arquivo index.html não encontrado!");
                return;
            }

            System.out.println("📄 [GIT] Arquivo index.html encontrado. Tamanho: " + indexFile.length() + " bytes");

            // ==========================================
            // 2. VERIFICAR STATUS DO GIT
            // ==========================================
            System.out.println("📋 [GIT] Verificando status...");
            Process statusProcess = Runtime.getRuntime().exec(new String[]{"git", "status", "--porcelain"}, null, pastaOrigem);
            BufferedReader statusReader = new BufferedReader(new InputStreamReader(statusProcess.getInputStream()));
            String line;
            boolean arquivoModificado = false;
            while ((line = statusReader.readLine()) != null) {
                System.out.println("   " + line);
                if (line.contains("index.html")) {
                    arquivoModificado = true;
                }
            }
            statusReader.close();

            if (!arquivoModificado) {
                System.out.println("ℹ️ [GIT] Nenhuma alteração no index.html. Nada a enviar.");
                return;
            }

            // ==========================================
            // 3. GIT ADD
            // ==========================================
            System.out.println("📤 [GIT] Adicionando index.html...");
            Process addProcess = Runtime.getRuntime().exec(new String[]{"git", "add", "index.html"}, null, pastaOrigem);
            int addResult = addProcess.waitFor();
            System.out.println("   git add resultado: " + addResult);

            if (addResult != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(addProcess.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("   ❌ " + line);
                }
                errorReader.close();
                return;
            }

            // ==========================================
            // 4. GIT COMMIT
            // ==========================================
            System.out.println("📤 [GIT] Commitando...");
            String dataHora = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
            String mensagem = "Atualização automática do estoque - " + dataHora;

            Process commitProcess = Runtime.getRuntime().exec(
                new String[]{"git", "commit", "-m", mensagem}, 
                null, pastaOrigem);
            int commitResult = commitProcess.waitFor();
            System.out.println("   git commit resultado: " + commitResult);

            // Lê a saída do commit
            BufferedReader commitReader = new BufferedReader(new InputStreamReader(commitProcess.getInputStream()));
            while ((line = commitReader.readLine()) != null) {
                System.out.println("   " + line);
            }
            commitReader.close();

            if (commitResult != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(commitProcess.getErrorStream()));
                boolean nothingToCommit = false;
                while ((line = errorReader.readLine()) != null) {
                    if (line.contains("nothing to commit")) {
                        nothingToCommit = true;
                        System.out.println("ℹ️ [GIT] Nada novo para commit.");
                    } else {
                        System.err.println("   ❌ " + line);
                    }
                }
                errorReader.close();

                if (nothingToCommit) {
                    return;
                }
            }

            // ==========================================
            // 5. GIT PUSH (COM FORCE)
            // ==========================================
            System.out.println("📤 [GIT] Enviando para o GitHub...");
            Process pushProcess = Runtime.getRuntime().exec(
                new String[]{"git", "push", "origin", "main", "--force"}, 
                null, pastaOrigem);
            int pushResult = pushProcess.waitFor();
            System.out.println("   git push resultado: " + pushResult);

            // Lê a saída do push
            BufferedReader pushReader = new BufferedReader(new InputStreamReader(pushProcess.getInputStream()));
            while ((line = pushReader.readLine()) != null) {
                System.out.println("   " + line);
            }
            pushReader.close();

            if (pushResult == 0) {
                System.out.println("🚀 [GIT] SUCESSO! Catálogo atualizado online!");

                // ==========================================
                // 6. VERIFICAR SE O ARQUIVO FOI ENVIADO
                // ==========================================
                System.out.println("📋 [GIT] Verificando status após push...");
                Process statusFinal = Runtime.getRuntime().exec(new String[]{"git", "status", "--porcelain"}, null, pastaOrigem);
                BufferedReader finalReader = new BufferedReader(new InputStreamReader(statusFinal.getInputStream()));
                boolean aindaModificado = false;
                while ((line = finalReader.readLine()) != null) {
                    System.out.println("   " + line);
                    if (line.contains("index.html")) {
                        aindaModificado = true;
                    }
                }
                finalReader.close();

                if (aindaModificado) {
                    System.out.println("⚠️ [GIT] O arquivo ainda está modificado localmente. Tentando reset...");
                    Process resetProcess = Runtime.getRuntime().exec(new String[]{"git", "reset", "HEAD", "index.html"}, null, pastaOrigem);
                    resetProcess.waitFor();
                    System.out.println("   git reset HEAD index.html executado.");
                }

            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(pushProcess.getErrorStream()));
                System.err.println("❌ [GIT] Erro no push:");
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("   ❌ " + line);
                }
                errorReader.close();

                // ==========================================
                // TENTATIVA COM git push (sem --force)
                // ==========================================
                System.out.println("🔄 [GIT] Tentando push sem --force...");
                pushProcess = Runtime.getRuntime().exec(
                    new String[]{"git", "push", "origin", "main"}, 
                    null, pastaOrigem);
                pushResult = pushProcess.waitFor();

                if (pushResult == 0) {
                    System.out.println("🚀 [GIT] SUCESSO! Catálogo atualizado online!");
                } else {
                    System.err.println("❌ [GIT] Falha no push. Verifique manualmente.");
                    System.err.println("   Execute: git push origin main --force");
                }
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ [GIT] Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

