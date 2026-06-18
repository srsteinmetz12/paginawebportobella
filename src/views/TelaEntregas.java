package views;

import dao.EntregasDAO;
import dao.SacolaDAO;
import dao.VendasDAO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import models.Entregas;
import models.Sacola;
import models.Vendas;
import util.ConfigLoader;
import util.MensagemSistema;

public class TelaEntregas extends javax.swing.JFrame {

    private Date dataVendaEntrega;
    public static int idVendasEntrega;
    private String pecaEntrega;
    private double freteValorEntrega;
    private boolean fretePagoEntrega;
    public static String nomeClienteEntrega;
    public static Date dataEntregaRetire;
    public static String dataEntregaEndereco;
    private String itemEntrega;
    public static Boolean entregue;
    public static Date dataEntrega;
    public static String status;
    public static String tipoEntrega;
    public static String canal;
    public static DefaultTableModel entregaEnderecoCliente;
    public static DefaultTableModel entregaRetireLoja;
    public static int idVenda;
    public static String dataVenda;
    public static String dataEntregue;
    public static String dataQueFoiEntregue;   
    EntregasDAO edao = new EntregasDAO();
    Entregas e = new Entregas();
    VendasDAO vdao = new VendasDAO();
    Vendas v = new Vendas();
    SacolaDAO sdao = new SacolaDAO();
    Sacola s = new Sacola();
    private JPopupMenu popupSugestoes;
    private JList<String> listaSugestoes;
    private final DefaultListModel<String> listModel;
    private List<String> listaCacheClientes = new ArrayList<>();
    private List<String> listaCacheClientesEndereco = new ArrayList<>();
    private Timer timerBusca;
    private Timer buscaTimer;
    private final boolean itemSelecionado = false;
    public static String nomesStr;
    String dataVendaEntr;
    private long ultimoDisparoBusca = 0;
    int anoAtual = java.time.Year.now().getValue();
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaEntregas() {
        this.setUndecorated(true);         
        initComponents();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
        // --- 1. DEFINIÇÃO DA PALETA LUXO/MODA PREMIUM (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color grafiteFundoGrid = new java.awt.Color(35, 35, 35);    // #232323
        java.awt.Color cinzaBordasGrid = new java.awt.Color(60, 60, 60);    // Linhas internas
        java.awt.Color cinzaLinhas     = new java.awt.Color(51, 51, 51);    // Separadores discretos
        java.awt.Color vermelhoSair      = new java.awt.Color(160, 40, 40);   // Hover do X

        // --- 2. FORÇAR A PINTURA DE TODOS OS PAINÉIS DE FUNDO ORIGINAIS ---
        this.getContentPane().setBackground(grafiteProfundo);
        jPanel1.setBackground(grafiteProfundo);   jPanel1.setOpaque(true);
        jPanel2.setBackground(grafiteProfundo);   jPanel2.setOpaque(true);
        jPanel3.setBackground(grafiteProfundo);   jPanel3.setOpaque(true);
        
        // Remove as molduras chanfradas grossas antigas do NetBeans para suavizar o visual
        abaRetireLoja.setBorder(null);
        jPanel1.setBorder(null);
        jPanel2.setBorder(null);

        // --- 3. ⚖️ HIERARQUIA DE FONTES OPERACIONAIS EM BRANCO PURO ---
        jLabel2.setForeground(brancoPuro);
        jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12)); // NOME CLIENTE (Retire)
        
        LabelNomeCliente.setForeground(brancoPuro);
        LabelNomeCliente.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12)); // NOME CLIENTE (Entrega)

        // --- 4. CAMPOS DE ENTRADA DARK ORIGINAIS ---
        javax.swing.JTextField[] todosCamposTexto = {
            campoNomeClienteRetireLoja, campoNomeClienteEntregaEndereco
        };
        for (javax.swing.JTextField txt : todosCamposTexto) {
            txt.setBackground(grafiteClaro);
            txt.setForeground(brancoPuro);
            txt.setCaretColor(brancoPuro);
            txt.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); // Borda fina e plana
            txt.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        }

        // --- 5. SOLUÇÃO DAS TABELAS: CABEÇALHO COM LEITURA FORTE BRANCA ---
        javax.swing.table.TableCellRenderer renderizadorMoviEntregas = new javax.swing.table.TableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
                // Criamos o painel customizado para desenhar a célula do cabeçalho do zero
                javax.swing.JPanel painelCelula = new javax.swing.JPanel(new java.awt.BorderLayout());

                // 🔥 A COR CORRETA: Cinza claro tridimensional idêntico ao da imagem circular
                painelCelula.setBackground(new java.awt.Color(215, 217, 220)); 

                // Garante o texto limpo e convertido para MAIÚSCULAS
                String textoColuna = (v == null) ? "" : v.toString().trim().toUpperCase();

                javax.swing.JLabel labelTexto = new javax.swing.JLabel(textoColuna);
                labelTexto.setForeground(new java.awt.Color(30, 30, 30)); // Texto escuro forte de alta leitura
                labelTexto.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                labelTexto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT); // Alinhado à esquerda igual ao de baixo

                // Espaçamento interno de 5 pixels para o texto respirar
                labelTexto.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
                painelCelula.add(labelTexto, java.awt.BorderLayout.CENTER);

                // 🔥 O SEGREDO DO VISUAL: Borda chanfrada que cria o relevo e as linhas verticais separadoras
                painelCelula.setBorder(javax.swing.BorderFactory.createBevelBorder(
                    javax.swing.border.BevelBorder.RAISED, 
                    new java.awt.Color(245, 245, 245), // Linha clara (brilho superior)
                    new java.awt.Color(160, 160, 160)  // Linha escura (sombra divisória lateral)
                ));

                return painelCelula;
            }
        };

        // Tabela 1: Retire Loja
        tabelaRetireLoja.getTableHeader().setDefaultRenderer(renderizadorMoviEntregas);
        tabelaRetireLoja.setBackground(grafiteFundoGrid);
        tabelaRetireLoja.setForeground(brancoPuro);
        tabelaRetireLoja.setGridColor(cinzaBordasGrid);
        tabelaRetireLoja.setBorder(null);
        tabelaRetireLoja.setRowHeight(22);
        jScrollPane1.getViewport().setBackground(grafiteProfundo);
        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaBordasGrid, 1));

        // Tabela 2: Entrega Endereço
        tabelaEntregaEndereco.getTableHeader().setDefaultRenderer(renderizadorMoviEntregas);
        tabelaEntregaEndereco.setBackground(grafiteFundoGrid);
        tabelaEntregaEndereco.setForeground(brancoPuro);
        tabelaEntregaEndereco.setGridColor(cinzaBordasGrid);
        tabelaEntregaEndereco.setBorder(null);
        tabelaEntregaEndereco.setRowHeight(22);
        jScrollPane3.getViewport().setBackground(grafiteProfundo);
        jScrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaBordasGrid, 1));

        // --- 6. 🔥 FIXAÇÃO DAS ABAS ORIGINAIS DO TOPO VIA RENDERIZADOR CUSTOMIZADO MATRIZ ---
        // Desenha a cor diretamente na malha de pixels, ignorando os conflitos do NetBeans
        abaRetireLoja.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(java.awt.Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    g.setColor(new java.awt.Color(212, 175, 55)); // Aba clicada vira Dourado Ouro legítimo
                } else {
                    g.setColor(new java.awt.Color(45, 45, 45));    // Aba de fundo vira Grafite Claro
                }
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintContentBorder(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
                // Arremata e remove o contorno branco tridimensional nativo que vazava ao redor do painel
                g.setColor(new java.awt.Color(28, 28, 28)); 
                g.drawRect(0, 0, 0, 0);
            }
        });

        // Configuração das letras das abas originais para casarem com o novo motor de desenho
        abaRetireLoja.setBackground(grafiteClaro);
        abaRetireLoja.setForeground(brancoPuro); // Letras ficam brancas fortes e nítidas
        abaRetireLoja.putClientProperty("JTabbedPane.selectedForeground", grafiteProfundo); // Letras escuras na aba ativa
        abaRetireLoja.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

        // --- 7. ESTILIZAÇÃO DOS SEPARADORES DE LINHA ---
        javax.swing.JSeparator[] todosSeparadores = {
            jSeparator1, jSeparator2, jSeparator3, jSeparator4,
            jSeparator7, jSeparator8, jSeparator9, jSeparator10
        };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            sep.setForeground(cinzaLinhas);
            sep.setBackground(cinzaLinhas);
        }

        // --- 8. VETOR DE BOTÕES ORIGINAIS PLANOS (FLAT STYLE) ---
        javax.swing.JButton[] todosBotoesAcao = {
            buttonPendentesRetireLoja, buttonEntregarRetireLoja, 
            buttonPesquisarClienteRetireLoja, buttonLimpaCamposTabelaRetire, buttonEntreguesRetireLoja,
            buttonPesquisarClienteEntregaEndereco, buttonEntregarEntregaEndereco, 
            buttonPendentesrEntregaEndereco, buttonLimpaCamposTabelaEndereco, buttonEntreguesEntregaEndereco,
            buttonGeraEtiquetaEnvio
        };

        for (javax.swing.JButton btn : todosBotoesAcao) {
            btn.setBackground(grafiteClaro);
            btn.setForeground(brancoPuro);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false); // Arranca o efeito cinza arredondado tridimensional antigo
            btn.putClientProperty("JButton.buttonType", "square"); // Força cantos retos modernos
        }

        // --- 9. ESTILIZAÇÃO EXCLUSIVA DOS DOIS BOTÕES MENU (Tom Bronze Acobreado) ---
        java.awt.Color bronzeAcobreado = new java.awt.Color(140, 120, 83); // #8C7853
        javax.swing.JButton[] botoesMenuExclusivos = { buttonMenuRetire, buttonMenuEntregaEndereco };
        for (javax.swing.JButton btnMenu : botoesMenuExclusivos) {
            btnMenu.setBackground(bronzeAcobreado);
            btnMenu.setForeground(brancoPuro);
            btnMenu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btnMenu.setFocusPainted(false);
            btnMenu.setBorderPainted(false);
            btnMenu.putClientProperty("JButton.buttonType", "square");
        }

        // --- 10. 🔥 BARRA DE TÍTULO PREMIUM COMPLETA DA LOGÍSTICA ---
        javax.swing.JPanel barraTituloPremium = new javax.swing.JPanel();
        barraTituloPremium.setBackground(grafiteClaro);
        barraTituloPremium.setOpaque(true);
        barraTituloPremium.setLayout(new java.awt.BorderLayout(15, 0));
        barraTituloPremium.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0)); 
        
        // Identificação Comercial do Cliente à Esquerda
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Logística de Entregas e Envios");
        lblClienteBarra.setForeground(brancoPuro);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);
        
        // Assinatura e Botão de Fechar à Direita [1]
        javax.swing.JPanel painelDireitoBarra = new javax.swing.JPanel();
        painelDireitoBarra.setBackground(grafiteClaro);
        painelDireitoBarra.setOpaque(true);
        painelDireitoBarra.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));
        
        javax.swing.JLabel lblDevBarra = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDevBarra.setForeground(douradoOuro); 
        lblDevBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireitoBarra.add(lblDevBarra);
        
        // Botão X com hover vermelho
        javax.swing.JButton btnFecharJanela = new javax.swing.JButton(" X ");
        btnFecharJanela.setBackground(grafiteClaro); 
        btnFecharJanela.setForeground(brancoPuro);
        btnFecharJanela.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnFecharJanela.setFocusPainted(false);
        btnFecharJanela.setBorderPainted(false);
        btnFecharJanela.setPreferredSize(new java.awt.Dimension(45, 30));
        btnFecharJanela.putClientProperty("JButton.buttonType", "square");
        
        btnFecharJanela.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override public void mouseEntered(java.awt.event.MouseEvent e) { btnFecharJanela.setBackground(vermelhoSair); }
        @Override public void mouseExited(java.awt.event.MouseEvent e) { btnFecharJanela.setBackground(grafiteClaro); }
        });
        btnFecharJanela.addActionListener(e -> { this.dispose(); });
        painelDireitoBarra.add(btnFecharJanela);
        barraTituloPremium.add(painelDireitoBarra, java.awt.BorderLayout.EAST);
        // Determina as dimensões da barra fixa no topo [1]
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);
        // --- 11. MOTOR DE MOVIMENTAÇÃO DA JANELA POR ARRASTE ---
        final int[] coordX = {0}; final int[] coordY = {0};
        final javax.swing.JFrame janelaAtual = this;
        barraTituloPremium.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override public void mousePressed(java.awt.event.MouseEvent e) { coordX[0] = e.getX(); coordY[0] = e.getY(); }
        });
        barraTituloPremium.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
        @Override public void mouseDragged(java.awt.event.MouseEvent e) { janelaAtual.setLocation(e.getXOnScreen() - coordX[0], e.getYOnScreen() - coordY[0]); }
        });
        // --- 12. 🔥 INSERÇÃO SUPERIOR ABSOLUTA COM RECUO DE SEGURANÇA MANDATÓRIO ---
        // Força a barra premium a flutuar na camada mais alta da janela, resolvendo o sumiço
        this.getLayeredPane().add(barraTituloPremium, javax.swing.JLayeredPane.PALETTE_LAYER);
        // Empurra o contêiner interno completo 35 pixels para baixo, fazendo as abas reaparecerem nítidas!
        try {
        ((javax.swing.JComponent)this.getContentPane()).setBorder(javax.swing.BorderFactory.createEmptyBorder(35, 0, 0, 0));
        } catch(Exception e) {
        try { jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(35, 0, 0, 0)); } catch(Exception ex) {}
        try { jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(35, 0, 0, 0)); } catch(Exception ex) {}
        }
        // Recarrega as matrizes gráficas e centraliza no meio do monitor
        this.revalidate();
        this.repaint();
        
        this.setLocationRelativeTo(null);
//        this.setIconImage(new ImageIcon(getClass().getResource("/images/favicon.png")).getImage());      
        this.setTitle(util.ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI LTDA");
        configurarCampoClienteRetireLoja();
        configurarCampoClienteEntregaEndereco();
        configurarFocoRetireLoja();
        configurarFocoEntregaEndereco();
        configurarCampoRetireLoja();
        configurarCampoEntregaEndereco();
        
        listModel = new DefaultListModel<>();
        listaSugestoes = new JList<>(listModel);
        listaSugestoes.setBackground(new Color(255, 255, 255));
        listaSugestoes.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        listaSugestoes.setFixedCellHeight(25);      
        listaSugestoes.setSelectionBackground(new Color(0, 120, 215));
        listaSugestoes.setSelectionForeground(Color.WHITE);       
        popupSugestoes = new JPopupMenu();
        popupSugestoes.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(listaSugestoes);
        scrollPane.setPreferredSize(new Dimension(campoNomeClienteRetireLoja.getWidth(), 150));
        scrollPane.setPreferredSize(new Dimension(campoNomeClienteEntregaEndereco.getWidth(), 150));
        popupSugestoes.add(scrollPane);
        timerBusca = new Timer(500, (ActionEvent e) -> {
            try {
                // Identifica qual dos dois campos está com o foco do teclado no milissegundo do disparo [links: 10]
                if (campoNomeClienteRetireLoja.hasFocus()) {
                    if (campoNomeClienteRetireLoja.getText().trim().length() >= 2) {
                        buscarSugestaoNome();
                    }
                } else if (campoNomeClienteEntregaEndereco.hasFocus()) {
                    if (campoNomeClienteEntregaEndereco.getText().trim().length() >= 2) {
                        buscarSugestaoNomeEndereco();
                    }
                }
            } catch (ClassNotFoundException | SQLException ex) {
                System.err.println("Erro no timer de entregas: " + ex);
            }
        });
        timerBusca.setRepeats(false);
        campoNomeClienteRetireLoja.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void reiniciarTimer() {
                // Como o timerBusca foi instanciado acima, essa checagem nunca mais dará NullPointerException
                if (timerBusca != null) {
                    timerBusca.stop(); // Interrompe o cronômetro da letra anterior
                    timerBusca.start(); // Inicia a contagem limpa de 500ms
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
        });  

        campoNomeClienteEntregaEndereco.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void reiniciarTimer() {
                if (timerBusca != null) {
                    timerBusca.stop(); 
                    timerBusca.start(); 
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
        });
        
        // Evento de clique na lista para autocompletar o campo que está ativo
        listaSugestoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String selecionado = listaSugestoes.getSelectedValue();
                if (selecionado != null) {
                    if (campoNomeClienteRetireLoja.hasFocus()) {
                        campoNomeClienteRetireLoja.setText(selecionado);
                    } else if (campoNomeClienteEntregaEndereco.hasFocus()) {
                        campoNomeClienteEntregaEndereco.setText(selecionado);
                    }
                    popupSugestoes.setVisible(false);
                }
            }
        });
        // Evento Unificado da tecla ENTER na JList [links: 10]
        listaSugestoes.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String selecionado = listaSugestoes.getSelectedValue();
                    if (selecionado != null) {
                        if (campoNomeClienteRetireLoja.hasFocus()) {
                            campoNomeClienteRetireLoja.setText(selecionado);
                        } else if (campoNomeClienteEntregaEndereco.hasFocus()) {
                            campoNomeClienteEntregaEndereco.setText(selecionado);
                        }
                        popupSugestoes.setVisible(false);
                    }
                }
            }
        });
        // 🚀 ABERTURA INSTANTÂNEA: Dispara o carregamento do cache em SEGUNDO PLANO
        new Thread(() -> {
            try {
                System.out.println("Iniciando carregamento do cache de clientes da Cloud em background...");

                // Carrega os dois caches de rede de forma paralela sem travar a interface visual
                carregarCacheBuscaClientes();
                carregarCacheBuscaClientesEndereco();

                System.out.println("Cache de clientes Cloud atualizado com sucesso!");
            } catch (Exception ex) {
                System.err.println("Erro ao carregar cache em background: " + ex.getMessage());
            }
        }).start(); // Inicializa a Thread e libera a tela principal na hora!
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane3 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator12 = new javax.swing.JSeparator();
        jSeparator13 = new javax.swing.JSeparator();
        jSeparator14 = new javax.swing.JSeparator();
        jSeparator15 = new javax.swing.JSeparator();
        jSeparator16 = new javax.swing.JSeparator();
        jSeparator17 = new javax.swing.JSeparator();
        jSeparator18 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        abaRetireLoja = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaRetireLoja = new javax.swing.JTable();
        buttonPendentesRetireLoja = new javax.swing.JButton();
        buttonEntregarRetireLoja = new javax.swing.JButton();
        buttonMenuRetire = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        campoNomeClienteRetireLoja = new javax.swing.JTextField();
        buttonPesquisarClienteRetireLoja = new javax.swing.JButton();
        buttonLimpaCamposTabelaRetire = new javax.swing.JButton();
        buttonEntreguesRetireLoja = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        jSeparator10 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaEntregaEndereco = new javax.swing.JTable();
        buttonPesquisarClienteEntregaEndereco = new javax.swing.JButton();
        buttonEntregarEntregaEndereco = new javax.swing.JButton();
        buttonMenuEntregaEndereco = new javax.swing.JButton();
        LabelNomeCliente = new javax.swing.JLabel();
        campoNomeClienteEntregaEndereco = new javax.swing.JTextField();
        buttonPendentesrEntregaEndereco = new javax.swing.JButton();
        buttonLimpaCamposTabelaEndereco = new javax.swing.JButton();
        buttonEntreguesEntregaEndereco = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        buttonGeraEtiquetaEnvio = new javax.swing.JButton();

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 204, 255));

        jPanel3.setBackground(new java.awt.Color(204, 204, 255));

        abaRetireLoja.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        abaRetireLoja.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        tabelaRetireLoja.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        tabelaRetireLoja.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        tabelaRetireLoja.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "VENDA", "DATA", "CLIENTE", "PEÇA", "VALOR FRETE", "ENTREGUE", "STATUS", "DATA ENTREGA"
            }
        ));
        jScrollPane1.setViewportView(tabelaRetireLoja);

        buttonPendentesRetireLoja.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonPendentesRetireLoja.setText("PENDENTES");
        buttonPendentesRetireLoja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPendentesRetireLojaActionPerformed(evt);
            }
        });

        buttonEntregarRetireLoja.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonEntregarRetireLoja.setText("ENTREGAR");
        buttonEntregarRetireLoja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEntregarRetireLojaActionPerformed(evt);
            }
        });

        buttonMenuRetire.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenuRetire.setText("MENU");
        buttonMenuRetire.setPreferredSize(new java.awt.Dimension(131, 31));
        buttonMenuRetire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuRetireActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel2.setText("NOME CLIENTE");

        campoNomeClienteRetireLoja.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoNomeClienteRetireLoja.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoNomeClienteRetireLoja.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoNomeClienteRetireLoja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNomeClienteRetireLojaActionPerformed(evt);
            }
        });

        buttonPesquisarClienteRetireLoja.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonPesquisarClienteRetireLoja.setText("PESQUISAR");
        buttonPesquisarClienteRetireLoja.setPreferredSize(new java.awt.Dimension(131, 31));
        buttonPesquisarClienteRetireLoja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisarClienteRetireLojaActionPerformed(evt);
            }
        });

        buttonLimpaCamposTabelaRetire.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonLimpaCamposTabelaRetire.setText("LIMPAR");
        buttonLimpaCamposTabelaRetire.setPreferredSize(new java.awt.Dimension(137, 31));
        buttonLimpaCamposTabelaRetire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimpaCamposTabelaRetireActionPerformed(evt);
            }
        });

        buttonEntreguesRetireLoja.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonEntreguesRetireLoja.setText("ENTREGUES");
        buttonEntreguesRetireLoja.setBorder(null);
        buttonEntreguesRetireLoja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEntreguesRetireLojaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator10, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator8)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(buttonMenuRetire, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonLimpaCamposTabelaRetire, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonPendentesRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonEntreguesRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonEntregarRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSeparator9)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 829, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 829, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(85, 85, 85))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(campoNomeClienteRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonPesquisarClienteRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoNomeClienteRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonPesquisarClienteRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonMenuRetire, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonLimpaCamposTabelaRetire, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonPendentesRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonEntreguesRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonEntregarRetireLoja, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44))
        );

        abaRetireLoja.addTab("RETIRE LOJA", jPanel1);

        jPanel2.setBackground(new java.awt.Color(204, 204, 255));

        tabelaEntregaEndereco.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        tabelaEntregaEndereco.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        tabelaEntregaEndereco.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "VENDA", "DATA", "CLIENTE", "PEÇA", "VALOR FRETE", "ENTREGUE", "STATUS", "DATA ENTREGA"
            }
        ));
        jScrollPane3.setViewportView(tabelaEntregaEndereco);

        buttonPesquisarClienteEntregaEndereco.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonPesquisarClienteEntregaEndereco.setText("PESQUISAR");
        buttonPesquisarClienteEntregaEndereco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisarClienteEntregaEnderecoActionPerformed(evt);
            }
        });

        buttonEntregarEntregaEndereco.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonEntregarEntregaEndereco.setText("ENTREGAR");
        buttonEntregarEntregaEndereco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEntregarEntregaEnderecoActionPerformed(evt);
            }
        });

        buttonMenuEntregaEndereco.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenuEntregaEndereco.setText("MENU");
        buttonMenuEntregaEndereco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuEntregaEnderecoActionPerformed(evt);
            }
        });

        LabelNomeCliente.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        LabelNomeCliente.setText("NOME CLIENTE");

        campoNomeClienteEntregaEndereco.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoNomeClienteEntregaEndereco.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoNomeClienteEntregaEndereco.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoNomeClienteEntregaEndereco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNomeClienteEntregaEnderecoActionPerformed(evt);
            }
        });

        buttonPendentesrEntregaEndereco.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonPendentesrEntregaEndereco.setText("PENDENTES");
        buttonPendentesrEntregaEndereco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPendentesrEntregaEnderecoActionPerformed(evt);
            }
        });

        buttonLimpaCamposTabelaEndereco.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonLimpaCamposTabelaEndereco.setText("LIMPAR");
        buttonLimpaCamposTabelaEndereco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimpaCamposTabelaEnderecoActionPerformed(evt);
            }
        });

        buttonEntreguesEntregaEndereco.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonEntreguesEntregaEndereco.setText("ENTREGUES");
        buttonEntreguesEntregaEndereco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEntreguesEntregaEnderecoActionPerformed(evt);
            }
        });

        buttonGeraEtiquetaEnvio.setBackground(new java.awt.Color(255, 153, 255));
        buttonGeraEtiquetaEnvio.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        buttonGeraEtiquetaEnvio.setText("GERAR ETIQUETA");
        buttonGeraEtiquetaEnvio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGeraEtiquetaEnvioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(90, 90, 90)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3)
                    .addComponent(jSeparator2)
                    .addComponent(jSeparator3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator4)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(buttonMenuEntregaEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonLimpaCamposTabelaEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonPendentesrEntregaEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonEntreguesEntregaEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonEntregarEntregaEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(2, 2, 2))
                    .addComponent(LabelNomeCliente)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(campoNomeClienteEntregaEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonPesquisarClienteEntregaEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(348, 348, 348)
                        .addComponent(buttonGeraEtiquetaEnvio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 825, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LabelNomeCliente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoNomeClienteEntregaEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonPesquisarClienteEntregaEndereco)
                    .addComponent(buttonGeraEtiquetaEnvio, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 31, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonMenuEntregaEndereco)
                    .addComponent(buttonEntregarEntregaEndereco)
                    .addComponent(buttonPendentesrEntregaEndereco)
                    .addComponent(buttonLimpaCamposTabelaEndereco)
                    .addComponent(buttonEntreguesEntregaEndereco))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        abaRetireLoja.addTab("ENTREGA ENDERECO", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(abaRetireLoja)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(abaRetireLoja)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonPendentesRetireLojaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPendentesRetireLojaActionPerformed
        pesquisaPedidosPendentesRetireLoja();
    }//GEN-LAST:event_buttonPendentesRetireLojaActionPerformed

    private void buttonEntregarRetireLojaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEntregarRetireLojaActionPerformed
        nomeClienteEntrega = e.getNomecli();
        try {
            pesquisarNomeClienteRetireLoja();
            status = e.getStatus();
            idVenda = e.getId();
            if(idVenda != 0 && status.equals("DISPONIVEL") ){
                System.out.println("Iniciando atualização de entrega para Retire Loja...");
                System.out.println("----------------------");
                dataEntrega = (Date.valueOf(LocalDate.now()));//data que vai para fazer a query no banco
                System.out.println("Data de entrega: "+dataEntrega);
                nomeClienteEntrega = e.getNomecli();
                atualizaStatusEntregueParaTodos();
                pesquisarEntregaFinalizadaRetireLoja();
                idVendasEntrega = e.getId();
                dataVendaEntrega = (Date) e.getDatavenda();
                ajusteDataVendaEntrega();
                nomeClienteEntrega = e.getNomecli();
                pecaEntrega = e.getCodpeca();
                freteValorEntrega = e.getValorfrete();               
                entregue = e.getEntregue();
                status = e.getStatus();
                dataEntrega = (Date) e.getDataentrega();
                ajusteDataEntrega();
                DefaultTableModel retireLoja = (DefaultTableModel) tabelaRetireLoja.getModel();
                if(retireLoja.getRowCount() < 40){    
                    retireLoja.addRow(new Object[]{
                        this.idVendasEntrega, 
                        this.dataVenda, 
                        this.nomeClienteEntrega, 
                        this.pecaEntrega, 
                        this.freteValorEntrega, 
                        this.entregue, 
                        this.status, 
                        this.dataEntregue
                    });
                }else{
                    MensagemSistema.mostrarAvisoDark(this, "Limite máximo de 30 vendas atingido!");
                }               
                System.out.println("----------------------");      
                MensagemSistema.mostrarAvisoDark(this, "Entrega RETIRE LOJA Finalizada com Sucesso!");               
            }else{
                MensagemSistema.mostrarAvisoDark(this, "ID de Venda Inválido ou Item Já Finalizado!");
            }           
        } catch (ClassNotFoundException ex) {
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_buttonEntregarRetireLojaActionPerformed

    private void buttonMenuRetireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuRetireActionPerformed
        new TelaMenu().setVisible(true);
        dispose();
    }//GEN-LAST:event_buttonMenuRetireActionPerformed

    private void buttonPesquisarClienteRetireLojaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisarClienteRetireLojaActionPerformed
        nomeClienteEntrega = campoNomeClienteRetireLoja.getText();
        if(nomeClienteEntrega != null){
            System.out.println("Iniciando pesquisa cliente retire loja..."); 
            try {
                pesquisarNomeClienteRetireLoja();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Erro: "+ex);
            }
            idVendasEntrega = e.getId();
            if(idVendasEntrega != 0){
                System.out.println("ID :"+idVendasEntrega);
                dataVendaEntrega = (Date) e.getDatavenda();
                ajusteDataVendaEntrega();
                nomeClienteEntrega = e.getNomecli();
                itemEntrega = String.valueOf(e.getCodpeca());
                freteValorEntrega = e.getValorfrete();
                entregue = e.getEntregue();
                status = e.getStatus();
                dataEntrega = (Date) e.getDataentrega();
                ajusteDataEntrega();
                System.out.println("Carregando dados da pesquisa na tabela...");
                System.out.println("-----------------------------------------");
                DefaultTableModel retireLoja = (DefaultTableModel) tabelaRetireLoja.getModel();
                if(retireLoja.getRowCount() < 40){
                        retireLoja.addRow(new Object[]{
                            this.idVendasEntrega,
                            dataVenda,
                            this.nomeClienteEntrega,
                            this.itemEntrega,
                            this.freteValorEntrega,
                            this.entregue,
                            this.status,
                            dataEntregue
                        });
                        if (tabelaRetireLoja.getColumnModel().getColumnCount() >= 3) {
                            tabelaRetireLoja.getColumnModel().getColumn(2).setPreferredWidth(180); // Dá bastante folga para o nome [stem-calculative-problem-solving]
                            tabelaRetireLoja.getColumnModel().getColumn(0).setPreferredWidth(30);  // Encolhe o ID da Venda [stem-calculative-problem-solving]
                            tabelaRetireLoja.getColumnModel().getColumn(1).setPreferredWidth(60);  // Ajusta a Data [stem-calculative-problem-solving]
                            tabelaRetireLoja.getColumnModel().getColumn(3).setPreferredWidth(30);
                            tabelaRetireLoja.getColumnModel().getColumn(5).setPreferredWidth(45);
                        }
                    }else{
                        MensagemSistema.mostrarAvisoDark(this, "Limite máximo de 30 vendas atingido!");
                    }
            }
        }else{
            MensagemSistema.mostrarAvisoDark(this, "Cliente Não Encontrado!");
        }
        campoNomeClienteRetireLoja.setText("");
    }//GEN-LAST:event_buttonPesquisarClienteRetireLojaActionPerformed

    private void campoNomeClienteRetireLojaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNomeClienteRetireLojaActionPerformed
        try {
            edao.pesquisaClientePendentesRetireLojaTabelaEntregasCloud(e);
        } catch (ClassNotFoundException ex) {
            System.out.println("Erro: "+ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
    }//GEN-LAST:event_campoNomeClienteRetireLojaActionPerformed

    private void buttonLimpaCamposTabelaRetireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimpaCamposTabelaRetireActionPerformed
        DefaultTableModel retireLoja = (DefaultTableModel) tabelaRetireLoja.getModel();
        retireLoja.setRowCount(0); // Remove todas as linhas
        System.out.println("Tabela limpa: " + retireLoja.getRowCount() + " linhas");
    }//GEN-LAST:event_buttonLimpaCamposTabelaRetireActionPerformed

    private void buttonEntreguesRetireLojaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEntreguesRetireLojaActionPerformed
        try {
            edao.pesquisaPedidosEntreguesRetireLojaTabelaEntregasCloud(e);          
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro de Driver: " + ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro de SQL: " + ex);
        }
    }//GEN-LAST:event_buttonEntreguesRetireLojaActionPerformed

    private void buttonEntreguesEntregaEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEntreguesEntregaEnderecoActionPerformed
        try {
            edao.pesquisaPedidosEntreguesEntregaEnderecoTabelaEntregasCloud(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro de Driver: " + ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro de SQL: " + ex);
        }
    }//GEN-LAST:event_buttonEntreguesEntregaEnderecoActionPerformed

    private void buttonLimpaCamposTabelaEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimpaCamposTabelaEnderecoActionPerformed
        DefaultTableModel entregaEndereco = (DefaultTableModel) tabelaEntregaEndereco.getModel();
        entregaEndereco.setRowCount(0); // Remove todas as linhas
        System.out.println("Tabela limpa: " + entregaEndereco.getRowCount() + " linhas");
    }//GEN-LAST:event_buttonLimpaCamposTabelaEnderecoActionPerformed

    private void buttonPendentesrEntregaEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPendentesrEntregaEnderecoActionPerformed
        pesquisaPedidosPendentesEntregaEndereco();
    }//GEN-LAST:event_buttonPendentesrEntregaEnderecoActionPerformed

    private void buttonMenuEntregaEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuEntregaEnderecoActionPerformed
        new TelaMenu().setVisible(true);
        dispose();
    }//GEN-LAST:event_buttonMenuEntregaEnderecoActionPerformed

    private void buttonEntregarEntregaEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEntregarEntregaEnderecoActionPerformed
        try {
            edao.pesquisarClienteEntregaEnderecoTabelaEntregasCloud(e);
            System.out.println("Iniciando a entrega do item...");
            idVenda = e.getId();
            status = e.getStatus();
            System.out.println("Id de venda: "+idVenda+" Status: "+status);
            if(idVenda != 0 && status.equals(("DISPONIVEL"))){
                System.out.println("Iniciando atualização de entrega para Entrega Endereço...");
                System.out.println("----------------------");
                dataEntrega = (Date.valueOf(LocalDate.now()));
                System.out.println("Data de entrega: "+dataEntrega);
                atualizaStatusEntregueParaTodos();
                pesquisarEntregaFinalizadaEntregaEndereco();
                idVendasEntrega = e.getId();
                dataVendaEntrega = (Date) e.getDatavenda();
                ajusteDataVendaEntrega();
                nomeClienteEntrega = e.getNomecli();
                pecaEntrega = e.getCodpeca();
                freteValorEntrega = e.getValorfrete();
                entregue = e.getEntregue();
                status = e.getStatus();
                dataEntrega = (Date) e.getDataentrega();
                ajusteDataEntrega();
                DefaultTableModel entregaEnderecoCliente = (DefaultTableModel) tabelaEntregaEndereco.getModel();
                entregaEnderecoCliente.addRow(new Object[]{   
                    this.idVendasEntrega, 
                    this.dataVenda, 
                    this.nomeClienteEntrega, 
                    this.pecaEntrega, 
                    this.freteValorEntrega, 
                    this.entregue, 
                    this.status, 
                    this.dataEntregue
                });
                System.out.println("----------------------");
                MensagemSistema.mostrarAvisoDark(this, "ENTREGA ENDEREÇO Finalizada com sucesso!");
            }else{
                MensagemSistema.mostrarAvisoDark(this, "ID de Venda inválido ou finalizado!");
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Erro: "+ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonEntregarEntregaEnderecoActionPerformed

    @SuppressWarnings("static-access")
    private void buttonPesquisarClienteEntregaEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisarClienteEntregaEnderecoActionPerformed
        nomeClienteEntrega = campoNomeClienteEntregaEndereco.getText();
        try {
            if(!(nomeClienteEntrega == null || "".equals(nomeClienteEntrega))){
                pesquisarNomeClienteEntregaEndereco();
                idVendasEntrega = e.getId();
                if(idVendasEntrega != 0){
                    ajusteDataVendaEntrega();
                    nomeClienteEntrega = e.getNomecli();
                    pecaEntrega = e.getCodpeca();
                    freteValorEntrega = e.getValorfrete();
                    entregue = e.getEntregue();
                    System.out.println("Entregue: "+entregue);
                    status = e.getStatus();
                    System.out.println("Status: "+status);
                    dataEntrega = (Date)e.getDataentrega();
                    ajusteDataEntrega();
                    DefaultTableModel entregaEnderecoCliente = (DefaultTableModel) tabelaEntregaEndereco.getModel();
                    if(entregaEnderecoCliente.getRowCount() < 40){
                        entregaEnderecoCliente.addRow(new Object[]{
                            this.idVendasEntrega,
                            this.dataVenda,
                            this.nomeClienteEntrega,
                            this.pecaEntrega,
                            this.freteValorEntrega,
                            this.entregue,
                            this.status,
                            this.dataEntregue
                        });
                        if (tabelaEntregaEndereco.getColumnModel().getColumnCount() >= 3) {
                            tabelaEntregaEndereco.getColumnModel().getColumn(2).setPreferredWidth(180); // Dá bastante folga para o nome [stem-calculative-problem-solving]
                            tabelaEntregaEndereco.getColumnModel().getColumn(0).setPreferredWidth(30);  // Encolhe o ID da Venda [stem-calculative-problem-solving]
                            tabelaEntregaEndereco.getColumnModel().getColumn(1).setPreferredWidth(60);  // Ajusta a Data [stem-calculative-problem-solving]
                            tabelaEntregaEndereco.getColumnModel().getColumn(3).setPreferredWidth(40);
                        }
                    }else{
                        MensagemSistema.mostrarAvisoDark(this, "Limite máximo de 30 vendas atingido!");
                    }
                }else{
                    MensagemSistema.mostrarAvisoDark(rootPane, "Dado pesquisado não existe na base!");
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
        campoNomeClienteEntregaEndereco.setText("");
    }//GEN-LAST:event_buttonPesquisarClienteEntregaEnderecoActionPerformed

    private void campoNomeClienteEntregaEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNomeClienteEntregaEnderecoActionPerformed
        try {
            edao.pesquisaClientePendentesEntregaEnderecoTabelaEntregasCloud(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
    }//GEN-LAST:event_campoNomeClienteEntregaEnderecoActionPerformed

    private void buttonGeraEtiquetaEnvioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGeraEtiquetaEnvioActionPerformed
        // 1. Captura o nome atual do cliente preenchido na tela (pode vir da busca preditiva)
        String clienteSelecionado = campoNomeClienteEntregaEndereco.getText().trim(); // Substitua pelo nome exato do seu JTextField

        if (clienteSelecionado.isEmpty()) {
            
            MensagemSistema.mostrarAvisoDark(this, "Por favor, selecione ou informe o nome do cliente para gerar a etiqueta! - "+" Aviso");
            return;
        }
         boolean conf = MensagemSistema.mostrarDecisaoDark(this, "Buscar endereço de \"" + clienteSelecionado + "\" na Cloud e gerar etiqueta?");
        
        if (!conf) {
            return;
        }
        
        try {
            System.out.println("Buscando dados cadastrais na base Cloud...");
            models.Etiqueta destinatario = edao.buscarEnderecoClienteCloud(clienteSelecionado);
            // Validação defensiva: se o cliente não tiver endereço ou não existir, avisa o operador
            if (destinatario == null) {
                MensagemSistema.mostrarAvisoDark(this, "Cliente localizado, mas o endereço está em branco no cadastro!" + "Erro de Cadastro");
                return;
            }
            // 3. Montagem automática do Remetente (Puxa do config.properties do brechó)
            models.Etiqueta remetente = new models.Etiqueta();
            remetente.setNome(ConfigLoader.get("sistema.nome_cliente"));
            remetente.setEndereco(ConfigLoader.get("loja.endereco"));
            remetente.setNumero(ConfigLoader.get("loja.numero"));
            remetente.setComplemento(ConfigLoader.get("loja.complemento"));
            remetente.setBairro(ConfigLoader.get("loja.bairro"));
            remetente.setCidade(ConfigLoader.get("loja.cidade"));
            remetente.setUf(ConfigLoader.get("loja.uf"));
            remetente.setCep(ConfigLoader.get("loja.cep"));
            // 4. Dispara a Engine do iText para construir o PDF com o código de barras do CEP
            util.GeradorEtiquetasCorreios gerador = new util.GeradorEtiquetasCorreios();
            gerador.gerarEtiquetaEnvio(destinatario, remetente);

            System.out.println("Etiqueta gerada com sucesso baseada nos dados do banco.");

        } catch (HeadlessException | ClassNotFoundException | SQLException ex) {
            System.err.println("Erro crítico ao processar etiqueta automática: " + ex.getMessage());
            MensagemSistema.mostrarAvisoDark(this, "Falha na comunicação Cloud: " + ex.getMessage());
        }       
    }//GEN-LAST:event_buttonGeraEtiquetaEnvioActionPerformed

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaEntregas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaEntregas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaEntregas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaEntregas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaEntregas().setVisible(true);
            }
        });
    }
   ///////////// METODOS DA CLASSE ////////////// 
    public void ajusteDataVendaEntrega(){
        System.out.println("Iniciando ajuste Data Venda Entrega...");
        java.sql.Date dataOriginal = (java.sql.Date) e.getDatavenda();
        if (dataOriginal != null) {
            String dia = dataOriginal.toString().substring(8,10);
            String mes = dataOriginal.toString().substring(5,7);
            String ano = dataOriginal.toString().substring(0,4);
            dataVenda = dia + "/" + mes + "/" + ano;
            System.out.println("Data Formatada para Tela: " + dataVenda);
            // Se você precisa atualizar a variável 'dataVendaEntrega', mantenha a original
            dataVendaEntrega = dataOriginal; 
        }
        System.out.println("Finalizando ajuste Data Venda Entrega...");
        System.out.println("----------------------------------------");       
    }
    
    public void ajusteDataEntrega(){
        System.out.println("Iniciando ajuste Data Entrega...");
        System.out.println(dataEntrega);
        if(dataEntrega != null){
            String diae = dataEntrega.toString();
            String de = diae.substring(8,10);
            String mese = dataEntrega.toString();
            String me = mese.substring(5,7);
            String anoe = dataEntrega.toString();
            String ae = anoe.substring(0,4);
            dataEntregue = de+"/"+me+"/"+ae;
            System.out.println("Data Entrega ajustada: "+dataEntregue);
        }else{
            System.out.println("Sem data de entrega no momento!");
        }
        System.out.println("Finalizando ajuste Data Entrega...");
        System.out.println("----------------------------------");
    }
    
    public void pesquisaPedidosPendentesEntregaEndereco() {
        try {
            System.out.println("Iniciando pesquisa pedidos pendentes...");
            edao.pesquisaClientePendentesEntregaEnderecoTabelaEntregasCloud(e);
            System.out.println("Finalizando pesquisa pedidos pendentes...");
            System.out.println("----------------------------------");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: " + ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: " + ex);
            MensagemSistema.mostrarAvisoDark(this, "Erro de comunicação com a Cloud: " + ex.getMessage());
        }
    }
    
    public void pesquisaPedidosPendentesRetireLoja() {
        try {
            System.out.println("Iniciando pesquisa pedidos pendentes...");
            edao.pesquisaClientePendentesRetireLojaTabelaEntregasCloud(e);
            System.out.println("Finalizando pesquisa pedidos pendentes...");
            System.out.println("----------------------------------");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro de Infraestrutura: " + ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro SQL Cloud: " + ex);
            MensagemSistema.mostrarAvisoDark(this, "Erro de comunicação com a Cloud: " + ex.getMessage());
        }
    }
  
    private void pesquisarNomeClienteEntregaEndereco() throws ClassNotFoundException {
        System.out.println("Iniciando pesquisa nome cliente...");
        try {
            edao.pesquisarClienteEntregaEnderecoTabelaEntregasCloud(e);
            System.out.println("Finalizando pesquisa nome cliente...");
            System.out.println("----------------------------------");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro de Infraestrutura: " + ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro SQL Cloud: " + ex);
            MensagemSistema.mostrarAvisoDark(this, "Erro de comunicação com a Cloud: " + ex.getMessage());
        }
    }
    
    private void pesquisarNomeClienteRetireLoja() throws ClassNotFoundException {
        System.out.println("Iniciando pesquisa nome cliente...");
        try {
            edao.pesquisarClienteRetireLojaTabelaEntregasCloud(e);
            System.out.println("Finalizando pesquisa nome cliente...");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
    
    private void pesquisarEntregaFinalizadaRetireLoja() throws ClassNotFoundException {
        System.out.println("Iniciando pesquisa entrega finalizada...");
        try {
            edao.perquisarEntregaFinalizadaParaTabelaRetireLojaCloud(e);
            System.out.println("Finalizando pesquisa entrega finalizada...");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
    
    private void pesquisarEntregaFinalizadaEntregaEndereco() throws ClassNotFoundException {
        System.out.println("Iniciando pesquisa entrega finalizada...");
        try {
            edao.pesquisarEntregaFinalizadaParaTabelaEntregaEnderecoCloud(e);
            System.out.println("Finalizando pesquisa entrega finalizada...");
            System.out.println("----------------------------------");
        } catch (SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
    
    public void carregarCacheBuscaClientes() {
        if (listaCacheClientes != null && !listaCacheClientes.isEmpty()) {
            return; // Aborta ida à nuvem se a lista já estiver na RAM [links: 10]
        }
        try {
            System.out.println("Carregando cache inicial Cloud para Retire na Loja...");
            edao.carregaNomeClientePendentesRetireLojaTabelaEntregasCloud(e);           
            String nomesFatiados = e.getNomecli();
            if (nomesFatiados != null && !nomesFatiados.isEmpty()) {
                listaCacheClientes = Arrays.asList(nomesFatiados.split(";"));
                System.out.println("Cache da Tela populado com " + listaCacheClientes.size() + " nomes.");
            } else {
                listaCacheClientes = new ArrayList<>();
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro ao inicializar cache Retire Loja: " + ex.getMessage());
        }
    }
    
    public void carregarCacheBuscaClientesEndereco() {
        if (listaCacheClientesEndereco != null && !listaCacheClientesEndereco.isEmpty()) {
            return; // Aborta ida à nuvem se a lista já estiver na RAM [links: 10]
        }
        try {
            System.out.println("Carregando cache inicial Cloud para Entrega Endereço...");
            edao.carregaNomeClientePendentesEntregaEnderecoTabelaEntregasCloud(e);           
            String nomesFatiados = e.getNomecli();
            if (nomesFatiados != null && !nomesFatiados.isEmpty()) {
                listaCacheClientesEndereco = Arrays.asList(nomesFatiados.split(";"));
                System.out.println("Cache da Tela populado com " + listaCacheClientesEndereco.size() + " nomes.");
            } else {
                listaCacheClientesEndereco = new ArrayList<>();
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro ao inicializar cache Retire Loja: " + ex.getMessage());
        }
    }
    
    public void buscarSugestaoNome() throws ClassNotFoundException, SQLException {
        long tempoAtual = System.currentTimeMillis();
        if (tempoAtual - ultimoDisparoBusca < 300) {
            return; // Derruba o método na hora se ele tentar rodar duplicado
        }
        ultimoDisparoBusca = tempoAtual;

        nomeClienteEntrega = campoNomeClienteRetireLoja.getText().trim();
        System.out.println("Buscando: "+nomeClienteEntrega);
        listModel.clear();

        if (nomeClienteEntrega.length() < 2) {
            if (popupSugestoes != null) {
                popupSugestoes.setVisible(false);
            }
            return;
        }
        //Adicionar à lista
        String termoUpper = nomeClienteEntrega.toUpperCase();
        for (String clienteItem : listaCacheClientes) {
            if (clienteItem.toUpperCase().contains(termoUpper)) {
                listModel.addElement(clienteItem.trim());
            }
        }
        // Mostrar popup se houver resultados
        if (listModel.getSize() > 0 && popupSugestoes != null) {
            if (listModel.getSize() > 0 && popupSugestoes != null) {
                System.out.println("Popup mostrado");
                SwingUtilities.invokeLater(() -> {
                    int largura = campoNomeClienteRetireLoja.getWidth() > 0 ? campoNomeClienteRetireLoja.getWidth() : 280;
                    popupSugestoes.getComponent(0).setPreferredSize(new Dimension(largura, 150));
                    popupSugestoes.pack(); 
                    popupSugestoes.show(campoNomeClienteRetireLoja, 0, campoNomeClienteRetireLoja.getHeight());
                });
            } else {
                if (popupSugestoes != null && popupSugestoes.isVisible()) {
                    System.out.println("Popup escondido - sem resultados reais");
                    popupSugestoes.setVisible(false);
                }
            }  
        } else {          
            popupSugestoes.setVisible(false);      
        }
    }
    
    public void buscarSugestaoNomeEndereco() throws ClassNotFoundException, SQLException {
        long tempoAtual = System.currentTimeMillis();
        if (tempoAtual - ultimoDisparoBusca < 300) {
            return;
        }
        ultimoDisparoBusca = tempoAtual;
        nomeClienteEntrega = campoNomeClienteEntregaEndereco.getText().trim();
        System.out.println("Buscando: "+nomeClienteEntrega);
        listModel.clear();

        if (nomeClienteEntrega.length() < 2) {
            if (popupSugestoes != null) {
                popupSugestoes.setVisible(false);
            }
            return;
        }
        //Adicionar à lista
        String termoUpper = nomeClienteEntrega.toUpperCase();
        for (String clienteItem : listaCacheClientesEndereco) {
            if (clienteItem.toUpperCase().contains(termoUpper)) {
                listModel.addElement(clienteItem.trim());
            }
        }
        // Mostrar popup se houver resultados
        if (listModel.getSize() > 0 && popupSugestoes != null) {
            // Se o popup já estiver visível, não faz nada e mantém ele aberto!
            // Se não estiver visível, executa a abertura normal colada no campo
            if (!popupSugestoes.isVisible() && campoNomeClienteEntregaEndereco.isShowing()) {
                SwingUtilities.invokeLater(() -> {
                    int largura = campoNomeClienteEntregaEndereco.getWidth() > 0 ? campoNomeClienteEntregaEndereco.getWidth() : 280;
                    popupSugestoes.getComponent(0).setPreferredSize(new Dimension(largura, 150));
                    popupSugestoes.pack(); 
                    popupSugestoes.show(campoNomeClienteEntregaEndereco, 0, campoNomeClienteEntregaEndereco.getHeight());
                    System.out.println("Popup mostrado e travado.");                   
                });
            }
        } else {          
            // 🔥 Só esconde automaticamente se a busca na RAM zerar de verdade (nenhum nome bater)
            if (popupSugestoes != null && popupSugestoes.isVisible()) {
                popupSugestoes.setVisible(false);      
                System.out.println("Popup escondido - zero correspondências.");
            }
        }
    }
    
    private void configurarCampoClienteRetireLoja() {
        campoNomeClienteRetireLoja.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    // Seta para baixo: navegar para próximo item
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                            if (popupSugestoes != null && popupSugestoes.isVisible() && listModel.getSize() > 0) {
                                listaSugestoes.requestFocus();
                                listaSugestoes.setSelectedIndex(0);
                            }   break;
                        case KeyEvent.VK_UP:
                            if (popupSugestoes != null && popupSugestoes.isVisible() && listModel.getSize() > 0) {
                                listaSugestoes.requestFocus();
                                listaSugestoes.setSelectedIndex(listModel.getSize() - 1);
                            }   break;
                        case KeyEvent.VK_ESCAPE:
                            if (popupSugestoes != null) {
                                popupSugestoes.setVisible(false);
                            }   break;
                        default:
                            buscarSugestaoNome();
                            break;
                    }
                } catch (ClassNotFoundException | SQLException ex) {
                    System.err.println("Erro: "+ex);
                }
            }
        });
    }
    
    private void configurarCampoClienteEntregaEndereco() {
        campoNomeClienteEntregaEndereco.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    // Seta para baixo: navegar para próximo item
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                            if (popupSugestoes != null && popupSugestoes.isVisible() && listModel.getSize() > 0) {
                                listaSugestoes.requestFocus();
                                listaSugestoes.setSelectedIndex(0);
                            }   break;
                        case KeyEvent.VK_UP:
                            if (popupSugestoes != null && popupSugestoes.isVisible() && listModel.getSize() > 0) {
                                listaSugestoes.requestFocus();
                                listaSugestoes.setSelectedIndex(listModel.getSize() - 1);
                            }   break;
                        case KeyEvent.VK_ESCAPE:
                            if (popupSugestoes != null) {
                                popupSugestoes.setVisible(false);
                            }   break;
                        default:
                            buscarSugestaoNomeEndereco();
                            break;
                    }
                } catch (ClassNotFoundException | SQLException ex) {
                    System.err.println("Erro: "+ex);
                }
            }
        });
    }
    
    private void configurarFocoRetireLoja() {
        campoNomeClienteRetireLoja.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Pequeno delay para permitir clique no popup
                Timer timer = new Timer(5000, event -> {
                    if (popupSugestoes != null && !listaSugestoes.hasFocus() && !campoNomeClienteRetireLoja.hasFocus()) {
                        popupSugestoes.setVisible(false);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
    
    private void configurarFocoEntregaEndereco() {
        campoNomeClienteRetireLoja.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Pequeno delay para permitir clique no popup
                Timer timer = new Timer(5000, event -> {
                    if (popupSugestoes != null && !listaSugestoes.hasFocus() && !campoNomeClienteEntregaEndereco.hasFocus()) {
                        popupSugestoes.setVisible(false);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
    
    private void configurarBuscaAutomaticaRetireLoja() {
        // Usar DocumentListener (melhor que KeyListener)
        campoNomeClienteRetireLoja.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            private void iniciarBuscaComDelay() {
                // Cancelar busca anterior
                if (buscaTimer != null && buscaTimer.isRunning()) {
                    buscaTimer.stop();
                }
                
                // Aguardar 500ms após parar de digitar
                buscaTimer = new Timer(5000, evt -> {
                    try {
                        if (!itemSelecionado) {
                            buscarSugestaoNome();
//                            buscarSugestaoNome2();
                        }
                    } catch (ClassNotFoundException | SQLException ex) {
                        System.err.println("Erro: "+ex);
                    }
                });
                buscaTimer.setRepeats(false);
                buscaTimer.start();
            }
        });
    }
    private void configurarBuscaAutomaticaEntregaEndereco() {
        // Usar DocumentListener (melhor que KeyListener)
        campoNomeClienteEntregaEndereco.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            private void iniciarBuscaComDelay() {
                // Cancelar busca anterior
                if (buscaTimer != null && buscaTimer.isRunning()) {
                    buscaTimer.stop();
                }
                
                // Aguardar 500ms após parar de digitar
                buscaTimer = new Timer(5000, evt -> {
                    try {
                        if (!itemSelecionado) {
                            buscarSugestaoNome();
//                            buscarSugestaoNome2();
                        }
                    } catch (ClassNotFoundException | SQLException ex) {
                        System.err.println("Erro: "+ex);
                    }
                });
                buscaTimer.setRepeats(false);
                buscaTimer.start();
            }
        });
    }
    
    private void configurarCampoRetireLoja() {
        campoNomeClienteRetireLoja.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Ignorar teclas de navegação
                if (e.getKeyCode() == KeyEvent.VK_DOWN || 
                    e.getKeyCode() == KeyEvent.VK_UP ||
                    e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    return;
                }               
                try {
                    buscarSugestaoNome();
                } catch (ClassNotFoundException | SQLException ex) {
                    System.err.println("Erro: "+ex);
                }
            }          
            @Override
            public void keyPressed(KeyEvent e) {
                // Navegar no popup
                if (e.getKeyCode() == KeyEvent.VK_DOWN && popupSugestoes.isVisible()) {
                    listaSugestoes.requestFocus();
                    if (listModel.getSize() > 0) {
                        listaSugestoes.setSelectedIndex(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && popupSugestoes.isVisible()) {
                    popupSugestoes.setVisible(false);
//                    popupAtivo = false;
                }
            }
        });
    }
    
    private void configurarCampoEntregaEndereco() {
        campoNomeClienteEntregaEndereco.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Ignorar teclas de navegação
                if (e.getKeyCode() == KeyEvent.VK_DOWN || 
                    e.getKeyCode() == KeyEvent.VK_UP ||
                    e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    return;
                }               
                try {
                    buscarSugestaoNome();
                } catch (ClassNotFoundException | SQLException ex) {
                    System.err.println("Erro: "+ex);
                }
            }          
            @Override
            public void keyPressed(KeyEvent e) {
                // Navegar no popup
                if (e.getKeyCode() == KeyEvent.VK_DOWN && popupSugestoes.isVisible()) {
                    listaSugestoes.requestFocus();
                    if (listModel.getSize() > 0) {
                        listaSugestoes.setSelectedIndex(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && popupSugestoes.isVisible()) {
                    popupSugestoes.setVisible(false);
//                    popupAtivo = false;
                }
            }
        });
    }
    
    public void atualizaStatusEntregueParaTodos(){
        try {
            vdao.atualizaStatusEntregueTabelaVendas(v);
            vdao.atualizaStatusEntregueTabelaVendasCloud(v);
            System.out.println("Atualizou tabela VENDAS!");
            System.out.println("------------------------");
            sdao.atualizaStatusEntregueTabelaSacola(s);
            sdao.atualizaStatusEntregueTabelaSacolaCloud(s);
            System.out.println("Atualizou tabela SACOLA!");
            System.out.println("------------------------");
            edao.atualizaStatusEntregueRetireLojaTabelaEntregas(e);
            edao.atualizaStatusEntregueRetireLojaTabelaEntregasCloud(e);
            System.out.println("Atualizou tabela ENTREGAS!");
            System.out.println("------------------------");
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }             
    }
    
    public void retornaDadosBaseEntregaEndereco() throws SQLException{
        try {
            edao.pesquisarPedidosPendentesEntregaEnderecoTabelaEntregas(e);
            dataVendaEntrega = (Date) e.getDatavenda();
            idVendasEntrega = e.getId();
            nomeClienteEntrega = e.getNomecli();
            itemEntrega = String.valueOf(e.getCodpeca());
            freteValorEntrega = e.getValorfrete();
            dataEntrega = (Date)e.getDataentrega();
            entregue = e.getEntregue(); 
            System.out.println("Dados da tabela: "+dataVendaEntrega+"/"+idVendasEntrega+"/"+nomeClienteEntrega+"/"+itemEntrega+"/"+freteValorEntrega+"/"+fretePagoEntrega+"/"+entregue+"/"+dataEntrega);                      
            DefaultTableModel entregaEndereco = (DefaultTableModel) tabelaEntregaEndereco.getModel();
            entregaEndereco.addRow(new Object[] {this.idVendasEntrega, this.dataVendaEntrega, this.nomeClienteEntrega, this.itemEntrega, this.freteValorEntrega, this.fretePagoEntrega, this.entregue, this.dataEntrega});
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaEntregas.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
    }
    
    public void carregarTabelaRetireLoja() throws ClassNotFoundException, SQLException{
        tabelaRetireLoja.getColumnModel().getColumn(0).setPreferredWidth(5);
        tabelaRetireLoja.getColumnModel().getColumn(1).setPreferredWidth(4);
        tabelaRetireLoja.getColumnModel().getColumn(2).setPreferredWidth(12);
        tabelaRetireLoja.getColumnModel().getColumn(3).setPreferredWidth(20); 
        tabelaRetireLoja.getSelectedRows();        
    }
    
    public void carregarTabelaEntregaEndereco() throws ClassNotFoundException, SQLException{
        tabelaEntregaEndereco.getColumnModel().getColumn(0).setPreferredWidth(5);
        tabelaEntregaEndereco.getColumnModel().getColumn(1).setPreferredWidth(4);
        tabelaEntregaEndereco.getColumnModel().getColumn(2).setPreferredWidth(12);
        tabelaEntregaEndereco.getColumnModel().getColumn(3).setPreferredWidth(20); 
        tabelaEntregaEndereco.getSelectedRows();         
    }
    
    public void acertaData(){
//        String date = campoDataEntregaRetireLoja.getText();
//        String ano = date.substring(6,10);
//        System.out.println(ano);
//        String mes = date.substring(3,5);
//        System.out.println(mes);
//        String dia = date.substring(0,2);
//        System.out.println(dia);
//        campoDataEntregaRetireLoja.setText(""+ano+"-"+mes+"-"+dia+"");
//        dataEntregaRetire = campoDataEntregaRetireLoja.getText();
//        System.out.println(dataEntregaRetire);
    }
    
    public void removerLinhaTabela(){
        int linhaSelecionada = tabelaRetireLoja.getSelectedRow();
        if (linhaSelecionada >= 0) {
            DefaultTableModel retireLoja = (DefaultTableModel) tabelaRetireLoja.getModel();
            retireLoja.removeRow(linhaSelecionada);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelNomeCliente;
    private javax.swing.JTabbedPane abaRetireLoja;
    private javax.swing.JButton buttonEntregarEntregaEndereco;
    private javax.swing.JButton buttonEntregarRetireLoja;
    private javax.swing.JButton buttonEntreguesEntregaEndereco;
    private javax.swing.JButton buttonEntreguesRetireLoja;
    private javax.swing.JButton buttonGeraEtiquetaEnvio;
    private javax.swing.JButton buttonLimpaCamposTabelaEndereco;
    private javax.swing.JButton buttonLimpaCamposTabelaRetire;
    private javax.swing.JButton buttonMenuEntregaEndereco;
    private javax.swing.JButton buttonMenuRetire;
    private javax.swing.JButton buttonPendentesRetireLoja;
    private javax.swing.JButton buttonPendentesrEntregaEndereco;
    private javax.swing.JButton buttonPesquisarClienteEntregaEndereco;
    private javax.swing.JButton buttonPesquisarClienteRetireLoja;
    private javax.swing.JTextField campoNomeClienteEntregaEndereco;
    private javax.swing.JTextField campoNomeClienteRetireLoja;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JSeparator jSeparator18;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable tabelaEntregaEndereco;
    private javax.swing.JTable tabelaRetireLoja;
    // End of variables declaration//GEN-END:variables
   
}
