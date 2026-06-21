package dao;

import connection.ConnectionDB;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Produto;
import util.MensagemSistema;
import static views.TelaFinanceiro.codPeca;
import views.TelaFornecedor;
import java.io.IOException;
import java.io.InputStreamReader;

    public class ProdutoDAO {
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        private final LocalDate dataDoDia = LocalDate.now();
        Connection con;
        Connection con2;
        String sql;
        ResultSet rs;
        Date novaData;

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
            writer.println("    .card { background-color: #2D2D2D; border: 1px solid #464646; border-radius: 12px; padding: 15px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.3); display: flex; flex-direction: column; justify-content: space-between; }");
            writer.println("    .card img { width: 100%; height: 280px; object-fit: cover; border-radius: 8px; background-color: #141414; margin-bottom: 12px; }");
            writer.println("    .sem-foto { width: 100%; height: 280px; border-radius: 8px; background-color: #141414; display: flex; align-items: center; justify-content: center; color: #666666; font-size: 13px; font-weight: bold; border: 1px dashed #464646; margin-bottom: 12px; }");
            writer.println("    .card h3 { margin: 5px 0 2px 0; font-size: 14px; color: #FFFFFF; text-transform: uppercase; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: bold; }");
            writer.println("    .card .info { color: #B0B0B0; font-size: 12px; margin: 4px 0 2px 0; }");
            writer.println("    .card .codigo-item { color: #A0A0A0; font-size: 12px; font-weight: bold; margin: 4px 0; text-transform: uppercase; }");
            writer.println("    .card .preco { color: #FFFFFF; font-size: 20px; font-weight: 800; margin: 8px 0; border-top: 1px solid #3D3D3D; padding-top: 10px; }");
            writer.println("    .btn-add-carrinho { display: block; width: 100%; background-color: #009ee3; color: #FFFFFF; padding: 12px; border: none; border-radius: 8px; font-weight: bold; font-size: 13px; text-transform: uppercase; cursor: pointer; margin-top: 10px; text-align: center; transition: background 0.3s; }");
            writer.println("    .btn-add-carrinho:hover { background-color: #0077b3; }");
            writer.println("    footer { text-align: center; padding: 30px; color: #555555; font-size: 11px; margin-top: 20px; border-top: 1px solid #2D2D2D; }");
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
            writer.println("    .modal-pix { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.95); z-index: 9999; display: none; align-items: center; justify-content: center; }");
            writer.println("    .modal-pix-content { background: #2D2D2D; padding: 30px; border-radius: 15px; max-width: 450px; width: 90%; text-align: center; border: 1px solid #464646; max-height: 95vh; overflow-y: auto; }");
            writer.println("    .modal-pix-content h3 { margin-top: 0; color: #FFF; }");
            writer.println("    .modal-pix-content .valor { color: #00a650; font-size: 24px; font-weight: bold; }");
            writer.println("    .modal-pix-content .descricao { color: #A0A0A0; font-size: 14px; }");
            writer.println("    .qr-container { background: #FFF; padding: 15px; border-radius: 10px; display: inline-block; margin: 10px 0; }");
            writer.println("    .qr-container canvas, .qr-container img { max-width: 200px; height: auto; }");
            writer.println("    .pix-code-area { margin: 15px 0; }");
            writer.println("    .pix-code-area textarea { width: 100%; background: #1E1E1E; color: #FFF; border: 1px solid #464646; padding: 10px; border-radius: 5px; font-size: 11px; resize: none; }");
            writer.println("    .btn-copiar { background: #00a650; color: #FFF; padding: 8px 20px; border: none; border-radius: 5px; cursor: pointer; margin-top: 5px; }");
            writer.println("    .btn-copiar:hover { background: #008f44; }");
            writer.println("    .status-pagamento { padding: 12px; border-radius: 8px; background: #856404; color: #FFF; font-weight: bold; margin-top: 10px; }");
            writer.println("    .status-pagamento.aprovado { background: #155724; }");
            writer.println("    .btn-fechar-modal { background: none; border: none; color: #888; cursor: pointer; margin-top: 15px; font-size: 13px; }");
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
                String caminhoImagemBanco = rs.getString("imagem");

                String codExibicao = (codigoItem == null) ? "S/C" : codigoItem;

                writer.println("      <div class='card' data-busca='" + descricao.toLowerCase() + " tam: " + tamanho.toLowerCase() + " ref: " + codExibicao.toLowerCase() + "'>");
                
                // ==========================================
                // LÓGICA DE FOTOS COM CÓDIGO DO ITEM
                // ==========================================
//                String codExibicao = (codigoItem == null) ? "S/C" : codigoItem;
                String nomeFotoWeb = "/fotos/" + codExibicao + ".jpg";
                // Tenta copiar a foto do banco para a pasta web
                boolean fotoCopiadaComSucesso = false;

                if (caminhoImagemBanco != null && !caminhoImagemBanco.trim().isEmpty()) {
                    java.io.File fotoOriginal = new java.io.File(caminhoImagemBanco);
                    if (fotoOriginal.exists()) {
                        java.io.File fotoDestinoWeb = new java.io.File(diretorioDocumentos + "\\" + nomeFotoWeb.replace("/", "\\"));
                        try {
                            java.nio.file.Files.copy(fotoOriginal.toPath(), fotoDestinoWeb.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            fotoCopiadaComSucesso = true;
                        } catch (java.io.IOException e) {}
                    }
                }

                if (fotoCopiadaComSucesso) {
                    writer.println("        <img src='" + nomeFotoWeb + "' alt='" + descricao + "'>");
                } else {
                    writer.println("        <div class='sem-foto'>Sem Foto</div>");
                }

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
            // MODAL DO CARRINHO (FORMATO EXATO DA IMAGEM)
            // ==========================================
            writer.println("  <div id='modalCarrinho' class='modal'>");
            writer.println("    <div class='modal-content' style='max-width:600px;'>");
            writer.println("      <h3>🛒 Meu Carrinho</h3>");
            writer.println("");
            writer.println("      <!-- LISTA DE ITENS -->");
            writer.println("      <div id='carrinhoItens' style='max-height:200px; overflow-y:auto; margin:10px 0;'></div>");
            writer.println("");
            writer.println("      <!-- RESUMO DO CARRINHO -->");
            writer.println("      <div class='carrinho-resumo'>");
            writer.println("        <div class='linha'><span>Subtotal:</span><span id='carrinhoSubtotal'>R$ 0,00</span></div>");
            writer.println("        <div class='linha'><span>Frete:</span><span id='carrinhoFrete'>R$ 0,00</span></div>");
            writer.println("        <div class='total'><span>TOTAL:</span><span id='carrinhoTotal'>R$ 0,00</span></div>");
            writer.println("      </div>");
            writer.println("");
            writer.println("      <!-- ========================================== -->");
            writer.println("      <!-- CALCULAR FRETE                           -->");
            writer.println("      <!-- ========================================== -->");
            writer.println("      <div style='margin:12px 0; padding:15px; background:#1E1E1E; border-radius:8px; border:1px solid #464646;'>");
            writer.println("        <label style='color:#A0A0A0; font-size:13px; font-weight:bold;'>📦 Calcular Frete</label>");
            writer.println("");
            writer.println("        <!-- CEP + BOTÃO -->");
            writer.println("        <div style='display:flex; gap:10px; margin-top:8px;'>");
            writer.println("          <input type='text' id='cepCarrinho' placeholder='CEP (ex: 90240580)' style='flex:1; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:14px;'>");
            writer.println("          <button onclick='calcularFreteCarrinho()' style='padding:10px 20px; background:#009ee3; color:#FFF; border:none; border-radius:6px; cursor:pointer; white-space:nowrap; font-weight:bold;'>");
            writer.println("            🔍 Calcular");
            writer.println("          </button>");
            writer.println("        </div>");
            writer.println("");
            writer.println("        <!-- RESULTADO DO FRETE -->");
            writer.println("        <div id='freteResultado' style='display:none; margin-top:10px; color:#FFF; font-size:14px;'>");
            writer.println("          ✅ <strong>Frete: R$ <span id='freteValor'>0,00</span></strong>");
            writer.println("          <span style='color:#A0A0A0; font-size:12px; margin-left:10px;'>(Prazo: <span id='fretePrazo'>-</span>)</span>");
            writer.println("        </div>");
            writer.println("");
            writer.println("        <!-- ========================================== -->");
            writer.println("        <!-- ENDEREÇO DE ENTREGA                      -->");
            writer.println("        <!-- ========================================== -->");
            writer.println("        <div id='enderecoCampos' style='display:none; margin-top:12px; padding-top:12px; border-top:1px solid #464646;'>");
            writer.println("          <label style='color:#A0A0A0; font-size:12px;'>📍 Endereço de Entrega</label>");
            writer.println("");
            writer.println("          <!-- RUA + NÚMERO -->");
            writer.println("          <div style='display:flex; gap:10px; margin-top:5px;'>");
            writer.println("            <input type='text' id='endRua' placeholder='Rua' style='flex:2; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px;'>");
            writer.println("            <input type='text' id='endNumero' placeholder='Nº' style='flex:0.5; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px;'>");
            writer.println("          </div>");
            writer.println("");
            writer.println("          <!-- COMPLEMENTO -->");
            writer.println("          <div style='display:flex; gap:10px; margin-top:5px;'>");
            writer.println("            <input type='text' id='endComplemento' placeholder='Complemento' style='flex:1; padding:10px; background:#2D2D2D; border:1px solid #464646; color:#FFF; border-radius:6px; font-size:13px;'>");
            writer.println("          </div>");
            writer.println("");
            writer.println("          <!-- BAIRRO + CIDADE + UF -->");
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
            // MODAL PIX (QR CODE)
            // ==========================================
            writer.println("  <div id='modalPix' class='modal-pix'>");
            writer.println("    <div class='modal-pix-content'>");
            writer.println("      <h3>💳 Pagamento via Pix</h3>");
            writer.println("      <div class='valor' id='modalValor'></div>");
            writer.println("      <div class='descricao' id='modalDescricao'></div>");
            writer.println("      <div class='qr-container' id='qrCodeContainer'></div>");
            writer.println("      <div class='pix-code-area'>");
            writer.println("        <label style='color:#A0A0A0;font-size:12px;'>📋 Código copia e cola:</label>");
            writer.println("        <textarea id='pixCode' rows='3' readonly></textarea>");
            writer.println("        <button class='btn-copiar' onclick='copiarPix()'>📋 Copiar código</button>");
            writer.println("      </div>");
            writer.println("      <div id='statusPagamento' class='status-pagamento'>⏳ Aguardando pagamento...</div>");
            writer.println("      <button class='btn-fechar-modal' onclick='fecharModalPix()'>Cancelar</button>");
            writer.println("    </div>");
            writer.println("  </div>");

            // ==========================================
            // JAVASCRIPT COMPLETO
            // ==========================================
            writer.println("  <script>");
            writer.println("    // ========================================");
            writer.println("    // CONFIGURAÇÕES");
            writer.println("    // ========================================");
            writer.println("    const CHAVE_PIX = 'portobella.brecho@gmail.com'; // ⚠️ SUBSTITUA PELA SUA CHAVE PIX");
            writer.println("    const NOME_RECEBEDOR = 'Vanderleia Vieira Moraes Lemos Steinmetz'; // ⚠️ SUBSTITUA PELO SEU NOME");
            writer.println("    const URL_BACKEND = 'https://paginawebportobella-1.onrender.com';");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // CARRINHO DE COMPRAS");
            writer.println("    // ========================================");
            writer.println("    let carrinho = { itens: [], frete: 0, cep: '' };");
            writer.println("");
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
            writer.println("    // CALCULAR FRETE + CHECKBOX");
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
            writer.println("            // ==========================================");
            writer.println("            // 🔥 EXIBE O CHECKBOX COM FRETE E PRAZO");
            writer.println("            // ==========================================");
            writer.println("            let resultado = document.getElementById('freteResultado');");
            writer.println("            resultado.style.display = 'block';");
            writer.println("            document.getElementById('freteValor').textContent = 'R$ ' + data.frete.toFixed(2).replace('.', ',');");
            writer.println("            document.getElementById('fretePrazo').textContent = data.prazo || 'Não informado';");
            writer.println("");
            writer.println("            // ==========================================");
            writer.println("            // 🔥 AUTO-PREENCHER CAMPOS DE ENDEREÇO");
            writer.println("            // ==========================================");
            writer.println("            preencherEndereco(data);");
            writer.println("");
            writer.println("            // Atualiza o total do carrinho");
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
            writer.println("    // ========================================");
            writer.println("    // PREENCHER CAMPOS DE ENDEREÇO");
            writer.println("    // ========================================");
            writer.println("    function preencherEndereco(dados) {");
            writer.println("      // Exibe os campos de endereço");
            writer.println("      let enderecoDiv = document.getElementById('enderecoCampos');");
            writer.println("      enderecoDiv.style.display = 'block';");
            writer.println("");
            writer.println("      // Preenche os campos (se veio do ViaCEP)");
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
            writer.println("      // Se o ViaCEP não retornou, tenta buscar via API novamente");
            writer.println("      if(!dados.logradouro && !dados.bairro) {");
            writer.println("        buscarEnderecoCompleto(dados.cep);");
            writer.println("      }");
            writer.println("");
            writer.println("      // Focus no campo Número");
            writer.println("      setTimeout(() => {");
            writer.println("        document.getElementById('endNumero').focus();");
            writer.println("      }, 300);");
            writer.println("    }");
            writer.println("");
            writer.println("    // ========================================");
            writer.println("    // BUSCAR ENDEREÇO COMPLETO (fallback)");
            writer.println("    // ========================================");
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
            writer.println("    // ========================================");
            writer.println("    // FINALIZAR COMPRA (COM ENDEREÇO)");
            writer.println("    // ========================================");
            writer.println("    function finalizarCompraCarrinho(metodo) {");
            writer.println("      if(carrinho.itens.length === 0) {");
            writer.println("        alert('Seu carrinho está vazio!');");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      // ==========================================");
            writer.println("      // 🔥 PEGA OS DADOS DOS CAMPOS DE ENDEREÇO");
            writer.println("      // ==========================================");
            writer.println("      let rua = document.getElementById('endRua').value.trim();");
            writer.println("      let numero = document.getElementById('endNumero').value.trim();");
            writer.println("      let complemento = document.getElementById('endComplemento').value.trim();");
            writer.println("      let bairro = document.getElementById('endBairro').value.trim();");
            writer.println("      let cidade = document.getElementById('endCidade').value.trim();");
            writer.println("      let uf = document.getElementById('endUf').value.trim().toUpperCase();");
            writer.println("      let cep = document.getElementById('cepCarrinho').value.trim().replace(/\\D/g, '');");
            writer.println("");
            writer.println("      // ==========================================");
            writer.println("      // 🔥 VALIDA OS CAMPOS OBRIGATÓRIOS");
            writer.println("      // ==========================================");
            writer.println("      if(!rua || !numero || !bairro || !cidade || !uf || cep.length !== 8) {");
            writer.println("        alert('⚠️ Por favor, preencha todos os campos do endereço:\\n\\n' +");
            writer.println("          '• Rua\\n' +");
            writer.println("          '• Número\\n' +");
            writer.println("          '• Bairro\\n' +");
            writer.println("          '• Cidade\\n' +");
            writer.println("          '• UF\\n' +");
            writer.println("          '• CEP (8 dígitos)');");
            writer.println("        return;");
            writer.println("      }");
            writer.println("");
            writer.println("      // ==========================================");
            writer.println("      // MONTA O ENDEREÇO COMPLETO");
            writer.println("      // ==========================================");
            writer.println("      let enderecoCompleto = rua + ', ' + numero;");
            writer.println("      if(complemento) enderecoCompleto += ' - ' + complemento;");
            writer.println("      enderecoCompleto += ' - ' + bairro + ' - ' + cidade + '/' + uf;");
            writer.println("");
            writer.println("      // ==========================================");
            writer.println("      // CALCULA TOTAIS");
            writer.println("      // ==========================================");
            writer.println("      let subtotal = carrinho.itens.reduce((total, item) => total + (item.preco * item.quantidade), 0);");
            writer.println("      let valorTotal = subtotal + carrinho.frete;");
            writer.println("");
            writer.println("      fecharCarrinho();");
            writer.println("");
            writer.println("      // ==========================================");
            writer.println("      // LOADING");
            writer.println("      // ==========================================");
            writer.println("      let loading = document.createElement('div');");
            writer.println("      loading.id = 'loadingFinal';");
            writer.println("      loading.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.8);z-index:9999;display:flex;align-items:center;justify-content:center;color:#FFF;font-size:18px;';");
            writer.println("      loading.innerHTML = '⏳ Processando pedido...<br><small>Aguarde</small>';");
            writer.println("      document.body.appendChild(loading);");
            writer.println("");
            writer.println("      // ==========================================");
            writer.println("      // ENVIA PARA O BACKEND");
            writer.println("      // ==========================================");
            writer.println("      let itens = carrinho.itens.map(item => ({ id: item.id, nome: item.nome, preco: item.preco, quantidade: item.quantidade }));");
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
            writer.println("          cep: cep,");
            writer.println("          endereco: enderecoCompleto");
            writer.println("        })");
            writer.println("      })");
            writer.println("      .then(response => response.json())");
            writer.println("      .then(data => {");
            writer.println("        document.getElementById('loadingFinal')?.remove();");
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
            writer.println("    // EXIBIR MODAL PIX");
            writer.println("    // ========================================");
            writer.println("    function exibirModalPix(payload, valor, nome) {");
            writer.println("      let container = document.getElementById('qrCodeContainer');");
            writer.println("      container.innerHTML = '';");
            writer.println("      document.getElementById('modalValor').textContent = 'R$ ' + valor.toFixed(2).replace('.', ',');");
            writer.println("      document.getElementById('modalDescricao').textContent = nome;");
            writer.println("      document.getElementById('pixCode').value = payload;");
            writer.println("");
            writer.println("      try {");
            writer.println("        new QRCode(container, { text: payload, width: 200, height: 200, colorDark: '#000000', colorLight: '#FFFFFF', correctLevel: QRCode.CorrectLevel.H });");
            writer.println("      } catch(e) {");
            writer.println("        container.innerHTML = '<p style=\"color:red;\">⚠️ Erro ao gerar QR Code</p>';");
            writer.println("      }");
            writer.println("");
            writer.println("      document.getElementById('statusPagamento').className = 'status-pagamento';");
            writer.println("      document.getElementById('statusPagamento').textContent = '⏳ Aguardando pagamento...';");
            writer.println("      document.getElementById('modalPix').style.display = 'flex';");
            writer.println("");
            writer.println("      setTimeout(() => {");
            writer.println("        if(document.getElementById('modalPix').style.display === 'flex') {");
            writer.println("          if(confirm('✅ Já realizou o pagamento?\\n\\nClique em OK para confirmar.')) {");
            writer.println("            let statusDiv = document.getElementById('statusPagamento');");
            writer.println("            statusDiv.className = 'status-pagamento aprovado';");
            writer.println("            statusDiv.textContent = '✅ Pagamento confirmado! Obrigado! 🎉';");
            writer.println("            setTimeout(() => { fecharModalPix(); alert('✅ Pagamento confirmado!'); location.reload(); }, 2000);");
            writer.println("          }");
            writer.println("        }");
            writer.println("      }, 10000);");
            writer.println("    }");
            writer.println("");
            writer.println("    function fecharModalPix() {");
            writer.println("      document.getElementById('modalPix').style.display = 'none';");
            writer.println("    }");
            writer.println("");
            writer.println("    function copiarPix() {");
            writer.println("      let texto = document.getElementById('pixCode');");
            writer.println("      texto.select();");
            writer.println("      navigator.clipboard.writeText(texto.value).then(() => alert('📋 Código Pix copiado!')).catch(() => { document.execCommand('copy'); alert('📋 Código Pix copiado!'); });");
            writer.println("    }");
            writer.println("");
            writer.println("    function mostrarNotificacao(mensagem) {");
            writer.println("      let notificacao = document.createElement('div');");
            writer.println("      notificacao.className = 'notificacao';");
            writer.println("      notificacao.textContent = mensagem;");
            writer.println("      document.body.appendChild(notificacao);");
            writer.println("      setTimeout(() => { notificacao.style.opacity = '0'; notificacao.style.transition = 'opacity 0.5s'; setTimeout(() => notificacao.remove(), 500); }, 3000);");
            writer.println("    }");
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

            // ==========================================
            // GIT AUTOMÁTICO COM VERIFICAÇÃO COMPLETA
            // ==========================================
            System.out.println("Iniciando sincronização automática com o GitHub...");
            try {
                java.io.File pastaOrigem = new java.io.File(diretorioDocumentos);

                // 1. Verifica/Configura remote
                Process remoteCheck = Runtime.getRuntime().exec(new String[]{"git", "remote", "get-url", "origin"}, null, pastaOrigem);
                int remoteExit = remoteCheck.waitFor();

                if (remoteExit != 0) {
                    String token = "ghp_jbHaoTzV1RpikgH8fsAtqxFCnT3LlK3wvKXA";
                    String remoteUrl = "https://srsteinmetz12:" + token + "@github.com/srsteinmetz12/paginawebportobella.git";
                    Process addRemote = Runtime.getRuntime().exec(new String[]{"git", "remote", "add", "origin", remoteUrl}, null, pastaOrigem);
                    addRemote.waitFor();
                    System.out.println("📌 Remote configurado.");
                }

                // 2. git add -f
                Process addProcess = Runtime.getRuntime().exec(new String[]{"git", "add", "-f", "index.html"}, null, pastaOrigem);
                int addResult = addProcess.waitFor();
                System.out.println("git add resultado: " + addResult);

                if (addResult == 0) {
                    // 3. git commit
                    Process commitProcess = Runtime.getRuntime().exec(new String[]{"git", "commit", "-m", "Atualização automática do estoque"}, null, pastaOrigem);
                    int commitResult = commitProcess.waitFor();
                    System.out.println("git commit resultado: " + commitResult);

                    if (commitResult != 0) {
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(commitProcess.getErrorStream()));
                        String line;
                        System.out.println("⚠️ Erro no commit:");
                        while ((line = errorReader.readLine()) != null) {
                            System.out.println("   " + line);
                        }
                        errorReader.close();
                    }

                    // 4. git push --force
                    Process pushProcess = Runtime.getRuntime().exec(new String[]{"git", "push", "origin", "main", "--force"}, null, pastaOrigem);
                    int pushResult = pushProcess.waitFor();
                    System.out.println("git push resultado: " + pushResult);

                    if (pushResult == 0) {
                        System.out.println("🚀 SUCESSO! O catálogo está atualizado e online!");
                    } else {
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(pushProcess.getErrorStream()));
                        String line;
                        System.out.println("⚠️ Erro no push:");
                        while ((line = errorReader.readLine()) != null) {
                            System.out.println("   " + line);
                        }
                        errorReader.close();
                    }
                    // Força o Git a ignorar mudanças no index.html após o push
                    Process resetProcess = Runtime.getRuntime().exec(new String[]{"git", "reset", "HEAD", "index.html"}, null, pastaOrigem);
                    resetProcess.waitFor();
                    
                } else {
                    System.out.println("⚠️ Erro ao adicionar arquivo index.html");
                }

            } catch (Exception e) {
                System.err.println("⚠️ Erro ao executar Git: " + e.getMessage());
                e.printStackTrace();
            }
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
        
    private String criarLinkMercadoPago(String codPeca, String descricao, double preco) {
        try {
            String descLimpa = descricao.trim().replace("\"", "'");
            String precoFormatado = String.format(java.util.Locale.US, "%.2f", preco);

            String jsonPayload = "{"
                + "\"items\": [{"
                + "  \"id\": \"PEC_" + codPeca + "\","
                + "  \"title\": \"Peça #" + codPeca + " - " + descLimpa + "\","
                + "  \"quantity\": 1,"
                + "  \"currency_id\": \"BRL\","
                + "  \"unit_price\": " + precoFormatado
                + "}],"
                + "\"back_urls\": {"
                + "  \"success\": \"https://github.io\","
                + "  \"failure\": \"https://github.io\""
                + "},"
                + "\"auto_return\": \"approved\""
                + "}";

            // ====================================================================================
            // CORREÇÃO 1: URL DA API CORRIGIDA PARA ENTRAR NO MERCADO PAGO DE FATO
            // ====================================================================================
            java.net.URL url = new java.net.URL("https://mercadopago.com"); // CORREÇÃO AQUI
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authorization", "Bearer APP_USR-8224138031673829-061507-627628d63a0255bfe7edad866f99f9ea-3473567863");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Idempotency-Key", java.util.UUID.randomUUID().toString());

            conn.setDoOutput(true);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 201 || responseCode == 200) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    String respostaTexto = response.toString();

                    if (respostaTexto.contains("\"init_point\":\"")) {
                        int inicio = respostaTexto.indexOf("\"init_point\":\"") + 14;
                        int fim = respostaTexto.indexOf("\"", inicio);
                        return respostaTexto.substring(inicio, fim).replace("\\/", "/");
                    }
                }
            } else {
                System.err.println("🔴 [MERCADO PAGO REJEITOU] Código HTTP: " + responseCode);
                try (java.io.BufferedReader brErr = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    String line;
                    System.err.print("Detalhe do erro da API: ");
                    while ((line = brErr.readLine()) != null) System.err.println(line);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.err.println("❌ [ERRO SISTÊMICO NATIVO]: " + e.getMessage());
        }

        // ====================================================================================
        // CORREÇÃO 2: LINK DO WHATSAPP REESCRITO COM PROTOCOLO DE ENVIO REAL
        // Substitua o '5551999999999' pelo WhatsApp real da Portobella
        // ====================================================================================
        try {
            String mensagemWhats = java.net.URLEncoder.encode("Olá Portobella! Quero comprar a peça: " + descricao + " (Código: #" + codPeca + ")", "UTF-8");
            return "https://whatsapp.com" + mensagemWhats; // CORREÇÃO AQUI
        } catch (Exception e) {
            return "https://whatsapp.com"; // CORREÇÃO AQUI
        }
    }

    public double buscarPrecoProduto(String produtoId) throws ClassNotFoundException {
        double preco = 0.0;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            String sql = "SELECT precosug FROM estoque WHERE codpeca = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, produtoId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                preco = rs.getDouble("precosug");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar preço: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }

        return preco;
    }

    /**
     * Busca o nome do produto no banco Aiven
     */
    public String buscarNomeProduto(String produtoId) throws ClassNotFoundException {
        String nome = "Produto PORTOBELLA";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            String sql = "SELECT itemdesc FROM estoque WHERE codpeca = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, produtoId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                nome = rs.getString("itemdesc");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar nome: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }

        return nome;
    }

    /**
     * Marca produto como vendido no banco Aiven
     */
    public void marcarProdutoComoVendido(String produtoId) throws ClassNotFoundException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            String sql = "UPDATE estoque SET status = 'VENDIDO', data_venda = CURRENT_TIMESTAMP WHERE codpeca = ? AND status = 'DISPONIVEL'";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, produtoId);

            int atualizados = stmt.executeUpdate();
            if (atualizados > 0) {
                System.out.println("✅ Produto " + produtoId + " marcado como VENDIDO!");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao marcar produto como vendido: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }

    
    public void processarVendasMercadoPago() throws ClassNotFoundException {
        String token = "APP_USR-5504079628127234-061707-4f72faca8cd75c397d89abc34651960f-3480421128"; 

        try {
            // 1. URL CORRIGIDA: Filtra apenas pagamentos "approved" (aprovados) ordenados pelos mais novos
            java.net.URL url = new java.net.URL("https://mercadopago.com");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String jsonResponse = response.toString();

                // Abre a conexão com a nuvem da Aiven para processar as baixas
                try (java.sql.Connection conCloud = ConnectionDB.getConnectionCloud()) {

                    // 2. PARSE CORRIGIDO: Procuramos o bloco "additional_info" de cada pagamento aprovado
                    int indexPagamento = 0;
                    while ((indexPagamento = jsonResponse.indexOf("\"additional_info\":", indexPagamento)) != -1) {

                        // Encontra o bloco de itens comprados dentro deste pagamento específico
                        int indexItems = jsonResponse.indexOf("\"items\":", indexPagamento);
                        if (indexItems == -1) {
                            indexPagamento += 18;
                            continue;
                        }

                        // Captura o ID real do PRODUTO (codpeca) dentro do item
                        int indexIdProduto = jsonResponse.indexOf("\"id\":\"", indexItems);
                        if (indexIdProduto == -1) {
                            indexPagamento += 18;
                            continue;
                        }

                        int inicioId = indexIdProduto + 6;
                        int fimId = jsonResponse.indexOf("\"", inicioId);
                        String codPecaPago = jsonResponse.substring(inicioId, fimId);

                        // Captura o valor real pago por este produto (transaction_amount)
                        int indexPreco = jsonResponse.indexOf("\"transaction_amount\":", indexPagamento);
                        if (indexPreco == -1) {
                            indexPagamento = fimId;
                            continue;
                        }
                        int fimPreco = jsonResponse.indexOf(",", indexPreco);
                        // Caso o preço seja o último campo do bloco, encerra na chave de fechamento
                        if (jsonResponse.indexOf("}", indexPreco) < fimPreco) {
                            fimPreco = jsonResponse.indexOf("}", indexPreco);
                        }
                        String precoTexto = jsonResponse.substring(indexPreco + 21, fimPreco).trim();
                        double valorPago = Double.parseDouble(precoTexto);

                        // 1. QUERY CLOUD: Verifica se a peça ainda consta como DISPONIVEL na Aiven
                        String sqlCheck = "SELECT status FROM estoque WHERE codpeca = ?";
                        try (java.sql.PreparedStatement stmtCheck = conCloud.prepareStatement(sqlCheck)) {
                            stmtCheck.setString(1, codPecaPago);
                            try (java.sql.ResultSet rsCheck = stmtCheck.executeQuery()) {
                                // Dentro do seu método processarVendasMercadoPago(), quando identificar um produto aprovado:
                                if (rsCheck.next() && "DISPONIVEL".equals(rsCheck.getString("status"))) {

                                    // 1. BAIXA NO ESTOQUE: Muda o item para VENDIDO na nuvem da Aiven
                                    String sqlUpdateEstoque = "UPDATE estoque SET status = 'VENDIDO' WHERE codpeca = ?";
                                    try (java.sql.PreparedStatement stmtUp = conCloud.prepareStatement(sqlUpdateEstoque)) {
                                        stmtUp.setString(1, codPecaPago);
                                        stmtUp.executeUpdate();
                                    }

                                    // 2. ATUALIZA A VENDA: Confirma o pagamento daquela intenção registrada
                                    String sqlUpdateVenda = "UPDATE vendas SET status_pagamento = 'APROVADO' WHERE codpeca = ? AND status_pagamento = 'PENDENTE'";
                                    try (java.sql.PreparedStatement stmtVenda = conCloud.prepareStatement(sqlUpdateVenda)) {
                                        stmtVenda.setString(1, codPecaPago);
                                        stmtVenda.executeUpdate();
                                    }

                                    // 3. BUSCA OS DADOS DE FRETE E ENDEREÇO DA TABELA PARA TE MOSTRAR
                                    String sqlDadosEntrega = "SELECT valorfrete, endereco_entrega FROM vendas WHERE codpeca = ? AND status_pagamento = 'APROVADO' ORDER BY id_venda DESC LIMIT 1";
                                    double fretePago = 0;
                                    String enderecoCompleto = "Não informado";

                                    try (java.sql.PreparedStatement stmtDados = conCloud.prepareStatement(sqlDadosEntrega)) {
                                        stmtDados.setString(1, codPecaPago);
                                        try (java.sql.ResultSet rsDados = stmtDados.executeQuery()) {
                                            if (rsDados.next()) {
                                                fretePago = rsDados.getDouble("valorfrete");
                                                enderecoCompleto = rsDados.getString("endereco_entrega");
                                            }
                                        }
                                    }

                                    // 4. NOTIFICAÇÃO COMPLETA NA TELA DO SEU SISTEMA DESKTOP
                                    final String pecaNotificacao = codPecaPago;
                                    final double valorNotificacao = valorPago;
                                    final double freteNotificacao = fretePago;
                                    final String enderecoNotificacao = enderecoCompleto;

                                    javax.swing.SwingUtilities.invokeLater(() -> {
                                        javax.swing.JOptionPane.showMessageDialog(null, 
                                            "✨ SUCESSO! NOVA VENDA CONFIRMADA! ✨\n\n" +
                                            "📦 Peça Código: #" + pecaNotificacao + "\n" +
                                            "💵 Valor do Item: R$ " + (valorNotificacao - freteNotificacao) + "\n" +
                                            "🚚 Frete Pago: R$ " + freteNotificacao + "\n" +
                                            "💰 Valor Total Recebido: R$ " + valorNotificacao + "\n\n" +
                                            "📍 ENDEREÇO PARA ENVIO:\n" + enderecoNotificacao + "\n\n" +
                                            "O item já foi baixado como VENDIDO na nuvem Aiven!", 
                                            "PORTOBELLA - Automação de Vendas", 
                                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                                    });
                                }

                            }
                        }

                        // Avança o ponteiro para processar o próximo pagamento da lista
                        indexPagamento = jsonResponse.indexOf("},", indexPreco);
                        if (indexPagamento == -1) break;
                    }
                }
            } else {
                System.err.println("Erro na API Mercado Pago. Código HTTP: " + conn.getResponseCode());
            }
        } catch (java.awt.HeadlessException | java.io.IOException | NumberFormatException | java.sql.SQLException e) {
            System.err.println("[ERRO AUTOMAÇÃO DE BAIXA]: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
    public void saveItem(Produto p) throws ClassNotFoundException, SQLException, Exception {
        con = ConnectionDB.getConnection();    
        sql = "INSERT INTO estoque(tipoitem, codforn, nomeforn, data, itemdesc, lote, codpeca, marca, tamanho, valorpago, precosug, lucroest, perclucro, obs, imagem, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";        
        System.out.println("Pesquisa: "+sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            stmt.setString(1, p.getTipoitem());
            stmt.setString(2, p.getCodforn());
            stmt.setString(3, p.getNomeforn());
            stmt.setDate(4, new java.sql.Date(p.getData().getTime()));
            stmt.setString(5, p.getItemdescricao());
            stmt.setString(6, p.getUltimoLote());
            stmt.setString(7, p.getCodpeca());
            stmt.setString(8, p.getMarca());
            stmt.setString(9, p.getTamanho());
            stmt.setDouble(10, p.getValorpago());
            stmt.setDouble(11, p.getPrecosugerido());
            stmt.setDouble(12, p.getLucroestimado());
            stmt.setInt(13, p.getPercentlucro());
            stmt.setString(14, p.getObservacao());
            stmt.setBytes(15, p.getImagem());
            stmt.setString(16, p.getStatus());            
            stmt.execute();
//            JOptionPane.showMessageDialog(null, "ITEM CADASTRADO COM SUCESSO!");
            System.out.println("----------------------------------");
            System.out.println("Item Salvo com sucesso!");
            System.out.println("----------------------------------");
        }catch (SQLException ex){
            MensagemSistema.mostrarAvisoDark(null, "Erro ao salvar o Item: "+ex);
            System.out.println("Erro ao inserir dados: " +ex.toString());
            System.out.println("----------------------------------");
            System.out.println("Erro ao salvar o Item!");
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão de banco encerrada!");
            System.out.println("----------------------------------");
        }           
    }
    
        public void saveItemCloud(Produto p) throws ClassNotFoundException, SQLException, Exception {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "INSERT INTO estoque(tipoitem, codforn, nomeforn, data, itemdesc, lote, codpeca, marca, tamanho, valorpago, precosug, lucroest, perclucro, obs, imagem, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";       
        System.out.println("Pesquisa: " + sql);
        
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, p.getTipoitem());
            stmt2.setString(2, p.getCodforn());
            stmt2.setString(3, p.getNomeforn());
            stmt2.setDate(4, new java.sql.Date(p.getData().getTime()));
            stmt2.setString(5, p.getItemdescricao());
            stmt2.setString(6, p.getUltimoLote());
            stmt2.setString(7, p.getCodpeca());
            stmt2.setString(8, p.getMarca());
            stmt2.setString(9, p.getTamanho());
            stmt2.setDouble(10, p.getValorpago());
            stmt2.setDouble(11, p.getPrecosugerido());
            stmt2.setDouble(12, p.getLucroestimado());
            stmt2.setInt(13, p.getPercentlucro());
            stmt2.setString(14, p.getObservacao());
            stmt2.setBytes(15, p.getImagem());
            stmt2.setString(16, p.getStatus());
            int linhasInseridas = stmt2.executeUpdate();           
            
            System.out.println("----------------------------------");
            System.out.println("Item Salvo com sucesso na Cloud! Registros: " + linhasInseridas);
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            MensagemSistema.mostrarAvisoDark(null, "Erro ao salvar o Item na Cloud: " + ex.getMessage());
            System.err.println("Erro ao inserir dados: " + ex.toString());
            System.out.println("----------------------------------");
            System.err.println("Erro ao salvar o Item na Cloud!");
            System.out.println("----------------------------------");
        } finally {
            if (stmt2 != null) try { stmt2.close(); } catch (SQLException e) {}
            if (con2 != null) try { con2.close(); } catch (SQLException e) {}
            System.out.println("Conexão de banco encerrada!");
            System.out.println("----------------------------------");
        }         
    }

    
    public void selectItem(String s, Produto p) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM estoque WHERE codpeca = ?"; 
        System.out.println("Pesquisa: " + sql);
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, s);
            rs = stmt.executeQuery();           
            if(rs.next()){
                p.setTipoitem(rs.getString(1));
                p.setCodforn(rs.getString(2));
                p.setUltimoLote(rs.getString(3));
                p.setNomeforn(rs.getString(4));
                p.setData(rs.getDate(5));               
                p.setItemdescricao(rs.getString(6));
                p.setCodpeca(rs.getString(7));
                p.setMarca(rs.getString(8));
                p.setTamanho(rs.getString(9));
                p.setValorpago(rs.getDouble(10));
                p.setPrecosugerido(rs.getDouble(11));
                p.setLucroestimado(rs.getDouble(12));
                p.setPercentlucro(rs.getInt(13));
                p.setObservacao(rs.getString(14));
                p.setImagem(rs.getBytes(15));
                p.setStatus(rs.getString(16));
                p.setDatavenda(rs.getDate(17));              
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch(SQLException ex){
                System.err.println("Erro: " + ex.getMessage());
                System.err.println("----------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.err.println("----------------------------------");
            }
        }  
    }
    
    public void selectItemCloud(String s, Produto p) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM estoque WHERE codpeca = ?";
        System.out.println("Pesquisa: " + sql);      
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, s);
            rs = stmt2.executeQuery();           
            if(rs.next()){
                p.setTipoitem(rs.getString(1));
                p.setCodforn(rs.getString(2));               
                p.setNomeforn(rs.getString(3));
                p.setData(rs.getDate(4));               
                p.setItemdescricao(rs.getString(5));
                p.setUltimoLote(rs.getString(6));
                p.setCodpeca(rs.getString(7));
                p.setMarca(rs.getString(8));
                p.setTamanho(rs.getString(9));
                p.setValorpago(rs.getDouble(10));
                p.setPrecosugerido(rs.getDouble(11));
                p.setLucroestimado(rs.getDouble(12));
                p.setPercentlucro(rs.getInt(13));
                p.setObservacao(rs.getString(14));
                p.setImagem(rs.getBytes(15));
                p.setStatus(rs.getString(16));
                p.setDatavenda(rs.getDate(17));                             
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch(SQLException ex){
            System.err.println("Erro: " + ex.getMessage());
            System.err.println("----------------------------------");
        } finally {
            try {
                con2.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Conexão erro: " + ex.getMessage());
                System.err.println("----------------------------------");
            }
        }
    }
    
    public void deleteItem (Produto p) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "DELETE FROM estoque WHERE codpeca =?"; 
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            stmt.setString(1, p.getCodpeca());
            stmt.executeUpdate();           
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");                      
        }catch(SQLException ex){
            System.err.println("Erro ao tentar remover o item!");
            System.err.println("Erro: "+ex.getMessage());
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da exclusão!");
            System.out.println("----------------------------------");
        }       
    }
    
    public void deleteItemCloud(Produto p) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "DELETE FROM estoque WHERE codpeca = ?";
        System.out.println("Pesquisa: " + sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            stmt2.setString(1, p.getCodpeca());
            int linhasAfetadas = stmt2.executeUpdate();           
            
            System.out.println("Acessou o banco de dados na cloud com sucesso!");
            System.out.println("Itens removidos na Cloud: " + linhasAfetadas);
            System.out.println("----------------------------------");                      
        } catch (SQLException ex) {
            System.out.println("Erro ao tentar remover o item na cloud!");
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("Fim da exclusão na cloud!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de exclusão: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }       
    }
    
    public void updateItem (Produto p) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "UPDATE estoque SET tipoitem=?, codforn=?, nomeforn=?, data=?, itemdesc=?, lote=?, codpeca=?, marca=?, tamanho=?, valorpago=?, precosug=?, lucroest=?, perclucro=?, obs=?, imagem=?, status=?, datavenda=? WHERE codpeca=?";
        System.out.println("Pesquisa: "+sql);
        try{
            stmt = con.prepareStatement(sql);
            stmt.setString(1, p.getTipoitem());
            stmt.setString(2, p.getCodforn());
            stmt.setString(3, p.getNomeforn());
            stmt.setDate(4, new java.sql.Date(p.getData().getTime()));           
            stmt.setString(5, p.getItemdescricao());
            stmt.setString(6, p.getUltimoLote());
            stmt.setString(7, p.getCodpeca());
            stmt.setString(8, p.getMarca());
            stmt.setString(9, p.getTamanho());
            stmt.setDouble(10, p.getValorpago());
            stmt.setDouble(11, p.getPrecosugerido());
            stmt.setDouble(12, p.getLucroestimado());
            stmt.setInt(13, p.getPercentlucro());
            stmt.setString(14, p.getObservacao());
            stmt.setBytes(15, p.getImagem());
            stmt.setString(16, p.getStatus());
            stmt.setDate(17, new java.sql.Date(p.getDatavenda().getTime()));
            stmt.setString(18, p.getCodpeca());           
            stmt.execute();            
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");            
        }catch(SQLException ex){
            System.err.println("Erro: "+ex.getMessage());
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Atualização!");
            System.out.println("----------------------------------");
        }
    }
    
        public void updateItemCloud(Produto p) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE estoque SET tipoitem=?, codforn=?, nomeforn=?, data=?, itemdes=?, lote=?, codpeca=?, marca=?, tamanho=?, valorpago=?, precosugerido=?, lucroestimado=?, percentlucro=?, observacao=?, imagem=?, status=?, datavenda=? WHERE codpeca=?";
        System.out.println("Pesquisa: " + sql);
        
        try {
            stmt2 = con2.prepareStatement(sql);           
            stmt2.setString(1, p.getTipoitem());
            stmt2.setString(2, p.getCodforn());
            stmt2.setString(3, p.getNomeforn());
            stmt2.setDate(4, new java.sql.Date(p.getData().getTime()));           
            stmt2.setString(5, p.getItemdescricao());
            stmt2.setString(6, p.getUltimoLote());
            stmt2.setString(7, p.getCodpeca());
            stmt2.setString(8, p.getMarca());
            stmt2.setString(9, p.getTamanho());
            stmt2.setDouble(10, p.getValorpago());
            stmt2.setDouble(11, p.getPrecosugerido());
            stmt2.setDouble(12, p.getLucroestimado());
            stmt2.setInt(13, p.getPercentlucro());
            stmt2.setString(14, p.getObservacao());
            stmt2.setBytes(15, p.getImagem());
            stmt2.setString(16, p.getStatus());
            if (p.getDatavenda() != null) {
                stmt2.setDate(17, new java.sql.Date(p.getDatavenda().getTime()));
            } else {
                stmt2.setNull(17, java.sql.Types.DATE); // Injeta NULL de forma segura no MySQL Cloud
            }           
            stmt2.setString(18, p.getCodpeca());           
            int linhasAtualizadas = stmt2.executeUpdate();            
            
            System.out.println("Acessou o banco de dados na cloud com sucesso!");
            System.out.println("Registros atualizados na Cloud: " + linhasAtualizadas);
            System.out.println("----------------------------------");            
        } catch (SQLException ex) {
            System.err.println("Erro ao atualizar item na Cloud: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("Fim da Atualização na Cloud!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao encerrar conexões de atualização: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    
    public void selectCodPeca(Produto p) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT MAX(codpeca) FROM estoque";
        System.out.println("Pesquisa: "+sql);        
        try{
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                p.setCodpeca(rs.getString(1));                              
                System.out.println("Acessou o banco de dados na cloud com sucesso!");
                System.out.println("----------------------------------");
            }else{
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }
        }catch(SQLException ex){
            System.err.println("Erro: "+ex.getMessage());
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }
    
    public void selectCodPecaCloud(Produto p) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT MAX(codpeca) FROM estoque"; 
        System.out.println("Pesquisa: " + sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery();            
            if (rs.next()) {
                p.setCodpeca(rs.getString(1));                              
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Item não cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("Fim da Pesquisa!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de busca: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void atualizaStatusEstoque(Produto p) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        novaData = new Date(System.currentTimeMillis());          
        if(codPeca.contains(";")){
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
            ArrayList<String> produtoLista = new ArrayList();
            String[] listaProduto = codPeca.split(";");
            System.out.println("lista de itens: "+listaProduto.length);
            for(String produto : listaProduto){
                String produtoLimpo = produto.trim();
                if(!produtoLimpo.isEmpty()){
                    produtoLista.add(produtoLimpo);
                    System.out.println("Item adicionado: "+produtoLimpo);
                    System.out.println("------------------------------------");
                }
                String sql = "UPDATE estoque SET datavenda=?, status=? WHERE codPeca =?";
                System.out.println("SQL: "+sql);
                System.out.println(novaData +" - "+ produtoLimpo);
                try{
                    stmt = con.prepareStatement(sql);
                    stmt.setDate(1, novaData);
                    stmt.setString(2, "VENDIDO");
                    stmt.setString(3, produtoLimpo);
                    stmt.execute();
                    System.out.println("Atualizou o banco de dados com sucesso!");
                    System.out.println("----------------------------------");
                }catch(SQLException ex){
                    System.err.println("Erro: "+ex.getMessage());
                    System.out.println("----------------------------------");  
                }
            }
        } else{
            sql = "UPDATE estoque SET datavenda=?, status=? WHERE codPeca = ?";
            System.out.println("Pesquisa: "+sql);
            System.out.println(novaData +" - "+ codPeca);
            try{
                stmt = con.prepareStatement(sql);
                stmt.setDate(1, novaData);
                stmt.setString(2, "Vendido");
                stmt.setString(3, codPeca);
                stmt.execute();
                System.out.println("Atualizou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            }catch(SQLException ex){
                System.err.println("Erro: "+ex.getMessage());
                System.out.println("----------------------------------");  
            }
        }
        con.close();
        System.out.println("Conexão encerrada!");
        System.out.println("Fim da Atualizacao!");
        System.out.println("----------------------------------");
    }
    
        public void atualizaStatusEstoqueCloud(Produto p) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        novaData = new java.sql.Date(System.currentTimeMillis());       
        sql = "UPDATE estoque SET datavenda=?, status=? WHERE codpeca = ?";
        
        try {
            stmt2 = con2.prepareStatement(sql);            
            if (codPeca != null && codPeca.contains(";")) {
                String[] listaProduto = codPeca.split(";");
                System.out.println("Lista de múltiplos itens detectada. Total: " + listaProduto.length);
                
                for (String produto : listaProduto) {
                    String produtoLimpo = produto.trim();
                    if (!produtoLimpo.isEmpty()) {
                        System.out.println("Baixando item do lote: " + produtoLimpo);                       
                        stmt2.setDate(1, novaData);
                        stmt2.setString(2, "VENDIDO"); 
                        stmt2.setString(3, produtoLimpo);                       
                        stmt2.executeUpdate();
                        System.out.println("Item " + produtoLimpo + " atualizado com sucesso na Cloud.");
                    }
                }
            } else {
                String pecaUnica = (codPeca != null) ? codPeca.trim() : "";
                System.out.println("Baixando item único no balcão: " + pecaUnica);
                System.out.println(novaData + " - " + pecaUnica);               
                stmt2.setDate(1, novaData);
                stmt2.setString(2, "VENDIDO");
                stmt2.setString(3, pecaUnica);              
                stmt2.executeUpdate();
                System.out.println("Item único atualizado com sucesso na Cloud.");
            }           
            System.out.println("Atualizou o banco de dados com sucesso!");
            System.out.println("----------------------------------");           
        } catch (SQLException ex) {
            System.err.println("Erro SQL ao atualizar status de estoque: " + ex.getMessage());
            System.out.println("----------------------------------");  
        } finally {
            try {
                if (stmt2 != null) stmt2.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("Fim da Atualizacao!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de baixa: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
}
