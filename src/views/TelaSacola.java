package views;

import dao.ClienteDAO;
import dao.EntregasDAO;
import dao.SacolaDAO;
import dao.VendasDAO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import models.Cliente;
import models.Entregas;
import models.Sacola;
import models.Vendas;
import util.ConfigLoader;
import util.MensagemSistema;

public class TelaSacola extends javax.swing.JFrame {

    public static int id;
    public static String dataCompra;
    public static String nomeCliente;
    public static String nomeClienteDisponiveis;
    public static String pecaCodigo;
    public static String valorCompra;
    public static String dataEntrega;
    public  static String status;
    public static String dataCompras;
    public static DefaultTableModel tabelaSacola;
    Sacola s = new Sacola();
    SacolaDAO sdao = new SacolaDAO();
    VendasDAO vdao = new VendasDAO();
    Vendas v = new Vendas();
    Cliente c = new Cliente();
    ClienteDAO cdao = new ClienteDAO();
    Entregas e = new Entregas();
    EntregasDAO edao = new EntregasDAO();
    private JPopupMenu popupSugestoes;
    private JList<String> listaSugestoes;
    private final DefaultListModel<String> listModel;
    private List<String> listaCacheClientes = new ArrayList<>();
    private Timer timerBusca;
    private Timer buscaTimer;
    private final boolean itemSelecionado = false;
    private long ultimoDisparoBusca = 0;
    int anoAtual = java.time.Year.now().getValue();   
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaSacola() throws ClassNotFoundException, InterruptedException { 
        this.setUndecorated(true);
        initComponents();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
                // --- 1. DEFINIÇÃO DA PALETA LUXO/MODA PREMIUM (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color grafiteFundoGrid = new java.awt.Color(35, 35, 35);    // #232323
        java.awt.Color cinzaBordasGrid = new java.awt.Color(60, 60, 60);    // Linhas da tabela
        java.awt.Color cinzaLinhas     = new java.awt.Color(51, 51, 51);    // Separadores discretos
        // --- 2. 🔥 FORÇAR A PINTURA DO PAINEL DE FUNDO (MANTÉM O SEU ALINHAMENTO ORIGINAL) ---
        telaSacola.setBackground(grafiteProfundo);
        telaSacola.setOpaque(true);
        this.getContentPane().setBackground(grafiteProfundo);
        // --- 3. ⚖️ HIERARQUIA DE FONTES OPERACIONAIS EM BRANCO PURO ---
        // Título Indicador de Tela Máximo (SACOLAS - Topo Esquerdo) [links: 10]
        jLabel1.setForeground(brancoPuro);
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        // Subtítulos dos Filtros de Pesquisa (Maiores que os botões, menores que o Título) [links: 10]
        jLabel2.setForeground(brancoPuro);
        jLabel2.setBackground(grafiteProfundo);
        jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15)); // NOME CLIENTE (Pendentes)
        jLabel4.setForeground(brancoPuro);
        jLabel4.setBackground(grafiteProfundo);
        jLabel4.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15)); // NOME CLIENTE (Disponíveis)
        // --- 4. CAMPOS DE ENTRADA DE TEXTO (JTextFields Dark Style) ---
        javax.swing.JTextField[] todosCamposTexto = {
            campoNomeClientePendentes, campoNomeClienteDisponiveis
        };
        for (javax.swing.JTextField txt : todosCamposTexto) {
            txt.setBackground(grafiteClaro);
            txt.setForeground(brancoPuro);
            txt.setCaretColor(brancoPuro);
            txt.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        }
        // --- 5. 🔥 RENDERIZADOR TRIDIMENSIONAL BLINDADO (PADRÃO RELEVO IDÊNTICO AO DE ENTREGAS) ---
        javax.swing.table.TableCellRenderer renderizadorCabecalho = new javax.swing.table.TableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
                // Criamos um painel customizado para desenhar a célula do cabeçalho do zero
                javax.swing.JPanel painelCelula = new javax.swing.JPanel(new java.awt.BorderLayout());
                painelCelula.setBackground(new java.awt.Color(215, 217, 220)); // Tom de cinza idêntico ao de entregas
                
                // Texto interno da coluna
                // Texto interno da coluna convertido OBRIGATORIAMENTE para MAIÚSCULAS (.toUpperCase())
                String textoColuna = (v == null) ? "" : v.toString().trim().toUpperCase();
                
                javax.swing.JLabel labelTexto = new javax.swing.JLabel(textoColuna);
                labelTexto.setForeground(new java.awt.Color(30, 30, 30)); // Texto escuro forte
                labelTexto.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                labelTexto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                
                // Adiciona o espaçamento de 5 pixels na esquerda para o texto não grudar na linha divisória
                labelTexto.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
                painelCelula.add(labelTexto, java.awt.BorderLayout.CENTER);
                
                // 🔥 O SEGREDO DO PADRÃO: Desenha manualmente a borda cinza tridimensional chanfrada (Efeito Relevo)
                // Isso força o aparecimento das linhas verticais separadoras que você precisa
                painelCelula.setBorder(javax.swing.BorderFactory.createBevelBorder(
                    javax.swing.border.BevelBorder.RAISED, 
                    new java.awt.Color(245, 245, 245), // Linha clara (brilho superior)
                    new java.awt.Color(160, 160, 160)  // Linha escura (sombra divisória lateral)
                ));
                
                return painelCelula;
            }
        };

        // Aplica a padronização do cabeçalho na tabela de sacolas
        tabelaSacolas.getTableHeader().setDefaultRenderer(renderizadorCabecalho);
        tabelaSacolas.getTableHeader().setReorderingAllowed(false); // Trava o arrasto das colunas
        
        // Configurações do corpo do grid (Fundo escuro plano no tema Dark)
        tabelaSacolas.setBackground(new java.awt.Color(35, 35, 35)); // Grafite Fundo Grid (#232323)
        tabelaSacolas.setForeground(brancoPuro);
        tabelaSacolas.setGridColor(new java.awt.Color(60, 60, 60)); // Linhas divisórias internas do corpo
        tabelaSacolas.setRowHeight(22); // Espaçamento vertical confortável nas linhas
        tabelaSacolas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12)); // Texto das linhas normal
        
        // Engole por completo a grande caixa branca residual de fundo abaixo do grid
        jScrollPane1.getViewport().setBackground(grafiteProfundo);
        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(60, 60, 60), 1));
        // --- 6. ESTILIZAÇÃO DOS SEPARADORES DE LINHA (JSeparator) ---
        javax.swing.JSeparator[] todosSeparadores = {
            jSeparator1, jSeparator2, jSeparator3, jSeparator4
        };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            sep.setForeground(cinzaLinhas);
            sep.setBackground(cinzaLinhas);
        }
        // --- 7. VETOR INTEGRADO DE BOTÕES: DESIGN PLANO SEM BORDAS ---
        javax.swing.JButton[] todosBotoesAcao = {
            buttonPesquisaNomeClientePendentes, buttonPesquisarClienteDisponiveis,
            buttonMenu, buttonLimparSacola, buttonPendentes, buttonFinalizar
        };
        for (javax.swing.JButton btn : todosBotoesAcao) {
            btn.setBackground(grafiteClaro);
            btn.setForeground(brancoPuro);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false); // 🔥 Remove o contorno cinza tridimensional antigo do Windows! [links: 10]
            btn.putClientProperty("JButton.buttonType", "square"); // Força o FlatLaf Plano [links: 10]
        }
        // 🔥 TOM DE BRONZE ACOBREADO: Destaque sofisticado e visível para o botão de voltar
        java.awt.Color bronzeAcobreado = new java.awt.Color(140, 120, 83); // #8C7853
        java.awt.Color textoBrancoPuro  = new java.awt.Color(255, 255, 255); // #FFFFFF       
        // Ajuste o nome da variável para o botão MENU da tela atual (ex: buttonMenu, buttonMenuRetire)
        buttonMenu.setBackground(bronzeAcobreado);
        buttonMenu.setForeground(textoBrancoPuro); // Letras brancas dão leitura perfeita no bronze
        buttonMenu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        buttonMenu.setFocusPainted(false);
        buttonMenu.setBorderPainted(false);
        buttonMenu.putClientProperty("JButton.buttonType", "square");
                // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NA TELA DE SACOLAS ---
        java.awt.Color grafiteClaroBarra = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuroBarra  = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuroBarra   = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color vermelhoSair      = new java.awt.Color(160, 40, 40);   // Hover vermelho para o X

        // 1. Cria o container da barra física
        javax.swing.JPanel barraTituloPremium = new javax.swing.JPanel();
        barraTituloPremium.setBackground(grafiteClaroBarra);
        barraTituloPremium.setOpaque(true);
        barraTituloPremium.setLayout(new java.awt.BorderLayout(15, 0));
        barraTituloPremium.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0)); 
        
        // 2. Lado Esquerdo: Nome do Cliente e do Sistema
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Gerenciamento de Sacolas Ativas");
        lblClienteBarra.setForeground(brancoPuroBarra);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);
        
        // 3. Lado Direito: Bloco Agrupador (Assinatura + Botão X)
        javax.swing.JPanel painelDireitoBarra = new javax.swing.JPanel();
        painelDireitoBarra.setBackground(grafiteClaroBarra);
        painelDireitoBarra.setOpaque(true);
        painelDireitoBarra.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));
        
        // Texto de Desenvolvimento de Grife
        javax.swing.JLabel lblDevBarra = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDevBarra.setForeground(douradoOuroBarra); 
        lblDevBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireitoBarra.add(lblDevBarra);
        
        // Botão X: Plano e integrado para fechar a tela (Usa dispose para voltar ao menu sem derrubar o sistema)
        javax.swing.JButton btnFecharJanela = new javax.swing.JButton(" X ");
        btnFecharJanela.setBackground(grafiteClaroBarra);
        btnFecharJanela.setForeground(brancoPuroBarra);
        btnFecharJanela.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnFecharJanela.setFocusPainted(false);
        btnFecharJanela.setBorderPainted(false);
        btnFecharJanela.setPreferredSize(new java.awt.Dimension(45, 30)); 
        btnFecharJanela.putClientProperty("JButton.buttonType", "square"); 
        
        // Efeito Visual de acender em vermelho ao passar o mouse
        btnFecharJanela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnFecharJanela.setBackground(vermelhoSair);
                btnFecharJanela.setForeground(brancoPuroBarra);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnFecharJanela.setBackground(grafiteClaroBarra);
                btnFecharJanela.setForeground(brancoPuroBarra);
            }
        });
        
        // Ação de fechamento seguro (Apenas fecha o formulário atual e limpa a memória)
        btnFecharJanela.addActionListener(e -> {
            this.dispose();
        });
        
        painelDireitoBarra.add(btnFecharJanela);
        barraTituloPremium.add(painelDireitoBarra, java.awt.BorderLayout.EAST);
        
        // 4. Posiciona e estica a barra no topo exato da tela de sacolas
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);

        // 5. Motor de Movimentação utilizando a referência corrigida de arrays finais
        final int[] coordX = {0};
        final int[] coordY = {0};
        final javax.swing.JFrame janelaAtual = this; 
        
        barraTituloPremium.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                coordX[0] = evt.getX();
                coordY[0] = evt.getY();
            }
        });
        
        barraTituloPremium.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                janelaAtual.setLocation(evt.getXOnScreen() - coordX[0], evt.getYOnScreen() - coordY[0]);
            }
        });
        // 6. Inserção Inteligente no topo do painel principal (Tratamento para telaSacola que existe na sua classe)
        try {
            telaSacola.add(barraTituloPremium);
            telaSacola.revalidate();
            telaSacola.repaint();
        } catch(Exception e) {
            this.getContentPane().add(barraTituloPremium);
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
        }
        
        this.setLocationRelativeTo(null); // Mantém a tela centralizada
        this.setTitle(ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI Ltda");
        
        configurarCampoCliente();
        configurarFoco();
        configurarCampo();
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
        scrollPane.setPreferredSize(new Dimension(campoNomeClientePendentes.getWidth(), 150));
        popupSugestoes.add(scrollPane);
        timerBusca = new Timer(500, (ActionEvent e) -> {
            try {
                String busca = campoNomeClientePendentes.getText().trim();
                if (busca.isEmpty() || busca.length() < 2) {
                    if (popupSugestoes != null) {
                        popupSugestoes.setVisible(false);
                    }
                    return;
                }
                buscarSugestaoNome();               
            } catch (ClassNotFoundException | SQLException ex) {
                System.err.println("Erro no timer de sacola: " + ex);
            }
        });
        timerBusca.setRepeats(false);
        campoNomeClientePendentes.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void reiniciarTimer() {
                if (timerBusca != null) {
                    timerBusca.stop(); // Interrompe a Thread da letra anterior
                    timerBusca.start(); // Dispara o novo cronômetro limpo do zero
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { reiniciarTimer(); }
        });
         // 4. Adicionar o evento de clique na lista (MousePressed)
        listaSugestoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String selecionado = listaSugestoes.getSelectedValue();
                if (selecionado != null) {
                    campoNomeClientePendentes.setText(selecionado);
                    popupSugestoes.setVisible(false);
                }
            }
        });       
        listaSugestoes.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String selecionado = listaSugestoes.getSelectedValue();
                    if (selecionado != null) {
                        campoNomeClientePendentes.setText(selecionado);
                        popupSugestoes.setVisible(false);
                    }
                }
            }
        });
        // ─── ☁️ MOTOR DE ALTA PERFORMANCE DA SACOLA (Cache em Background) ───
        new Thread(() -> {
            try {
                System.out.println("Carregando cache de clientes da sacola na Cloud em background...");
                
                // Executa a busca pesada na nuvem sem travar a interface gráfica do operador
                carregarCacheBuscaClientesSacola();
                
                System.out.println("Cache do módulo de sacolas atualizado com sucesso!");
            } catch (Exception ex) {
                System.err.println("Erro na carga de cache das sacolas em background: " + ex.getMessage());
            }
        }).start(); // Inicializa a linha de execução paralela
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        telaSacola = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        campoNomeClientePendentes = new javax.swing.JTextField();
        buttonPesquisaNomeClientePendentes = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaSacolas = new javax.swing.JTable();
        buttonMenu = new javax.swing.JButton();
        buttonFinalizar = new javax.swing.JButton();
        buttonPendentes = new javax.swing.JButton();
        campoNomeClienteDisponiveis = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        buttonPesquisarClienteDisponiveis = new javax.swing.JButton();
        buttonLimparSacola = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        telaSacola.setBackground(new java.awt.Color(204, 204, 255));
        telaSacola.setForeground(new java.awt.Color(51, 51, 51));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 30)); // NOI18N
        jLabel1.setText("SACOLAS");
        jLabel1.setToolTipText("");

        jLabel2.setBackground(new java.awt.Color(204, 204, 255));
        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel2.setText("NOME CLIENTE (Pendentes)");

        campoNomeClientePendentes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNomeClientePendentesActionPerformed(evt);
            }
        });

        buttonPesquisaNomeClientePendentes.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonPesquisaNomeClientePendentes.setText("PESQUISAR");
        buttonPesquisaNomeClientePendentes.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonPesquisaNomeClientePendentes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisaNomeClientePendentesActionPerformed(evt);
            }
        });

        tabelaSacolas.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        tabelaSacolas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nome Cliente ", "Data Compra", "Valor Compra", "Código Peça", "Venda ID", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tabelaSacolas);

        buttonMenu.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenu.setText("MENU");
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
            }
        });

        buttonFinalizar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonFinalizar.setText("FINALIZAR");
        buttonFinalizar.setBorder(null);
        buttonFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFinalizarActionPerformed(evt);
            }
        });

        buttonPendentes.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonPendentes.setText("PENDENTES");
        buttonPendentes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPendentesActionPerformed(evt);
            }
        });

        campoNomeClienteDisponiveis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNomeClienteDisponiveisActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel4.setText("NOME CLIENTE (Disponíveis)");

        buttonPesquisarClienteDisponiveis.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonPesquisarClienteDisponiveis.setText("PESQUISAR");
        buttonPesquisarClienteDisponiveis.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonPesquisarClienteDisponiveis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisarClienteDisponiveisActionPerformed(evt);
            }
        });

        buttonLimparSacola.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonLimparSacola.setText("LIMPAR");
        buttonLimparSacola.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimparSacolaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout telaSacolaLayout = new javax.swing.GroupLayout(telaSacola);
        telaSacola.setLayout(telaSacolaLayout);
        telaSacolaLayout.setHorizontalGroup(
            telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(telaSacolaLayout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 862, Short.MAX_VALUE)
                        .addGroup(telaSacolaLayout.createSequentialGroup()
                            .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(telaSacolaLayout.createSequentialGroup()
                                    .addComponent(campoNomeClientePendentes, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonPesquisaNomeClientePendentes, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel2))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(telaSacolaLayout.createSequentialGroup()
                                    .addComponent(campoNomeClienteDisponiveis, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonPesquisarClienteDisponiveis, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(telaSacolaLayout.createSequentialGroup()
                            .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jSeparator3)
                                .addGroup(telaSacolaLayout.createSequentialGroup()
                                    .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(buttonLimparSacola, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonPendentes, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonFinalizar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(3, 3, 3))
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 859, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        telaSacolaLayout.setVerticalGroup(
            telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(telaSacolaLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(campoNomeClientePendentes, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonPesquisaNomeClientePendentes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonPesquisarClienteDisponiveis, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(campoNomeClienteDisponiveis, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(43, 43, 43)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addGroup(telaSacolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonMenu)
                    .addComponent(buttonLimparSacola)
                    .addComponent(buttonPendentes)
                    .addComponent(buttonFinalizar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(telaSacola, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(telaSacola, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        new TelaMenu().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonMenuActionPerformed

    @SuppressWarnings("UseSpecificCatch")
    private void buttonPesquisaNomeClientePendentesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisaNomeClientePendentesActionPerformed
        nomeCliente = campoNomeClientePendentes.getText();
        System.out.println("Iniciando pesquisa para o Cliente: " + nomeCliente);

        if (nomeCliente.trim().isEmpty()) {
            MensagemSistema.mostrarAvisoDark(this, "Por favor, digite o nome de um cliente!");
            return;
        }

        // Obtém o modelo da tabela e limpa as linhas de pesquisas anteriores para atualizar o grid limpo
        DefaultTableModel sacolas = (DefaultTableModel) tabelaSacolas.getModel();
        sacolas.setRowCount(0); 

        java.text.SimpleDateFormat formatadorBR = new java.text.SimpleDateFormat("dd/MM/yyyy");

        try {
            // Altere a chamada passando o texto do campo e recebendo a lista de sacolas pendentes
            java.util.List<Sacola> resultadoBanco = sdao.consultaClienteNomeCloud(nomeCliente);

            if (resultadoBanco.isEmpty()) {
                MensagemSistema.mostrarAvisoDark(this, "Nenhum item pendente (EM_SEPARACAO) encontrado para este cliente!");
                campoNomeClientePendentes.setText("");
                return;
            }

            // Percorre a lista adicionando cada item encontrado na tabela
            resultadoBanco.forEach((item) -> {
                // Tratamento e formatação segura da data
                String dataCompras = "";
                if (item.getDataCompra() != null) {
                    dataCompras = formatadorBR.format(item.getDataCompra());
                }

                // Adiciona a linha no grid
                sacolas.addRow(new Object[]{
                    item.getNomeCliente(),
                    dataCompras,
                    item.getValorCompra(),
                    item.getCodigoPeca(),
                    item.getVendaId(),
                    item.getStatus()
                });
            });

            // Ajusta o tamanho das colunas para melhor leitura visual
            if (tabelaSacolas.getColumnModel().getColumnCount() >= 5) {
                tabelaSacolas.getColumnModel().getColumn(0).setPreferredWidth(150); // Nome do Cliente com mais espaço
                tabelaSacolas.getColumnModel().getColumn(1).setPreferredWidth(45);  // Data Compra
                tabelaSacolas.getColumnModel().getColumn(2).setPreferredWidth(40);  // Valor Compra
                tabelaSacolas.getColumnModel().getColumn(3).setPreferredWidth(40);  // Código Peça
                tabelaSacolas.getColumnModel().getColumn(4).setPreferredWidth(35);  // Venda ID
                tabelaSacolas.getColumnModel().getColumn(5).setPreferredWidth(50);  // Status
            }

            System.out.println("Todas as sacolas enviadas para a tabela com sucesso!");
            System.out.println("--------------------------");

        } catch (Exception ex) {
            MensagemSistema.mostrarAvisoDark(this, "Erro ao processar consulta na nuvem!");
            System.err.println("Erro na TelaSacola: " + ex);
        }    

        campoNomeClientePendentes.setText("");        
    }//GEN-LAST:event_buttonPesquisaNomeClientePendentesActionPerformed

    @SuppressWarnings("UseSpecificCatch")
    private void buttonFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFinalizarActionPerformed
        System.out.println("Iniciando finalização em lote da sacola...");

        DefaultTableModel sacolas = (DefaultTableModel) tabelaSacolas.getModel();
        int totalLinhasTabela = sacolas.getRowCount();

        if (totalLinhasTabela == 0) {
            MensagemSistema.mostrarAvisoDark(this, "Não existem itens na tabela para finalizar!");
            return;
        }

        dataEntregaFormatada();

        int itensProcessadosComSucesso = 0;
        // --- 📝 VARIÁVEL PARA ACUMULAR OS IDS DAS VENDAS ---
        String idsVendasFinalizadas = ""; 

        try {
            for (int i = 0; i < totalLinhasTabela; i++) {
                String nomeCliDaLinha = sacolas.getValueAt(i, 0).toString();
                String dataDaLinha    = sacolas.getValueAt(i, 1).toString();
                String valorDaLinha   = sacolas.getValueAt(i, 2).toString();
                String codigoDaLinha  = sacolas.getValueAt(i, 3).toString();
                int idVendaDaLinha    = Integer.parseInt(sacolas.getValueAt(i, 4).toString());
                String statusDaLinha  = sacolas.getValueAt(i, 5).toString();

                if (idVendaDaLinha != 0) {
                    if (statusDaLinha.equalsIgnoreCase("EM_SEPARACAO")) {

                        this.id = idVendaDaLinha;
                        this.status = statusDaLinha;

                        s.setVendaId(idVendaDaLinha);
                        s.setStatus(statusDaLinha);

                        // Executa a sua query de alteração de status no banco Cloud
                        atualizaStatusDisponivelParaTabelas();

                        // --- 🔗 CONCATENA OS IDS DAS VENDAS PROCESSADAS ---
                        if (idsVendasFinalizadas.isEmpty()) {
                            idsVendasFinalizadas = String.valueOf(idVendaDaLinha);
                        } else {
                            idsVendasFinalizadas += ", " + idVendaDaLinha;
                        }

                        itensProcessadosComSucesso++;
                    }
                }
            }

            // --- 📋 PROCESSAMENTO DO ALERT FINAL UNIFICADO ---
            if (itensProcessadosComSucesso > 0) {
                // Método nativo da sua tela para atualizar a interface
                atualizaItemParaDisponivelTabelaInterface(); 

                // Limpa o grid de sacolas pendentes
                sacolas.setRowCount(0); 

                // --- 📢 MONTAGEM DA SUA MENSAGEM ORIGINAL EM LOTE COM TODOS OS IDS ---
                String dataHoje = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String msgSucesso = "Cliente " + nomeCliente.toUpperCase() + ", ID Venda " + idsVendasFinalizadas + " com SACOLA Finalizada em " + dataHoje;

                // Dispara o JOptionPane padrão ou a sua mensagem customizada com a String montada
                MensagemSistema.mostrarAvisoDark(this, msgSucesso);

                System.out.println("Finalização concluída com sucesso para os IDs: " + idsVendasFinalizadas);
            } else {
                MensagemSistema.mostrarAvisoDark(this, "Nenhum item válido pendente foi processado.");
            }

        } catch (Exception ex) {
            System.err.println("Erro ao finalizar lote de sacolas: " + ex);
        }
    }//GEN-LAST:event_buttonFinalizarActionPerformed

    private void buttonPendentesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPendentesActionPerformed
        try {                       
            sdao.consultaSacolasPendentesCloud(s);            
            try {
                sdao.consultaSacolaCloud(s);
                if (s.getVendaId() <= 0) {
                    System.out.println("Aviso: Nenhuma sacola com status 'EM_SEPARACAO' localizada na Cloud.");
                    MensagemSistema.mostrarAvisoDark(this, "Nenhuma sacola pendente foi localizada na base de dados!");
                    return;
                }
                nomeCliente = s.getNomeCliente();
                if (s.getDataCompra() != null) {
                    dataCompra = s.getDataCompra().toString().replaceAll("-", "/");
                    System.out.println(dataCompra);
                    String dia = dataCompra.substring(8, 10);
                    String mes = dataCompra.substring(5, 7);
                    String ano = dataCompra.substring(0, 4);
                    dataCompra = dia + "/" + mes + "/" + ano;
                    System.out.println(dataCompra);
                } else {
                    dataCompra = "";
                }               
                valorCompra = s.getValorCompra();
                System.out.println(valorCompra);
                pecaCodigo = s.getCodigoPeca();
                System.out.println(pecaCodigo);
                id = s.getVendaId();
                System.out.println(id);
                status = s.getStatus();
                System.out.println(status);                              
                
            } catch (SQLException ex) {
                Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
                 System.err.println("Erro: " + ex);
            }                                
        } catch (ClassNotFoundException ex) {
            System.err.println("Erro: " + ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: " + ex);
        }
    }//GEN-LAST:event_buttonPendentesActionPerformed

    private void buttonPesquisarClienteDisponiveisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisarClienteDisponiveisActionPerformed
        nomeClienteDisponiveis = campoNomeClienteDisponiveis.getText();
        System.out.println("Cliente entregas: "+nomeClienteDisponiveis);       
        try {
            if(nomeClienteDisponiveis != null){
                sdao.consultaNomeClienteDisponiveisCloud(s);
            }else{
                MensagemSistema.mostrarAvisoDark(this, "Cliente não encontrado ou campo está vazio!");
            }           
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
        campoNomeClienteDisponiveis.setText("");
    }//GEN-LAST:event_buttonPesquisarClienteDisponiveisActionPerformed

    private void buttonLimparSacolaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimparSacolaActionPerformed
        DefaultTableModel sacola = (DefaultTableModel) tabelaSacolas.getModel();
        sacola.setRowCount(0); // Remove todas as linhas
        System.out.println("Tabela limpa: " + sacola.getRowCount() + " linhas");
    }//GEN-LAST:event_buttonLimparSacolaActionPerformed

    private void campoNomeClientePendentesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNomeClientePendentesActionPerformed
        System.out.println("Iniciando busca de nomes para pendentes na base...");
        nomeCliente = campoNomeClientePendentes.getText();
        try {
            carregarCacheBuscaClientesSacola();
            buscarSugestaoNome();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_campoNomeClientePendentesActionPerformed

    private void campoNomeClienteDisponiveisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNomeClienteDisponiveisActionPerformed

    }//GEN-LAST:event_campoNomeClienteDisponiveisActionPerformed

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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaSacola.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @SuppressWarnings("override")
            public void run() {            
                try {                                
                    new TelaSacola().setVisible(true);
                } catch (ClassNotFoundException | InterruptedException ex) {
                    Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println("Erro ao carregar a tela Sacola: "+ex);
                }
           }
        });
    }
    
    private void configurarCampoCliente() {
        campoNomeClientePendentes.addKeyListener(new KeyAdapter() {
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
//                            buscarSugestaoNome2();
                            break;
                    }
                } catch (ClassNotFoundException | SQLException ex) {
                    System.err.println("Erro: "+ex);
                }
            }
        });
    }
    
    private void configurarFoco() {
        campoNomeClientePendentes.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Pequeno delay para permitir clique no popup
                Timer timer = new Timer(5000, event -> {
                    if (popupSugestoes != null && !listaSugestoes.hasFocus() && !campoNomeClientePendentes.hasFocus()) {
                        popupSugestoes.setVisible(false);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
    
    private void configurarBuscaAutomatica() {
        // Usar DocumentListener (melhor que KeyListener)
        campoNomeClientePendentes.getDocument().addDocumentListener(new DocumentListener() {
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
                buscaTimer = new Timer(500, evt -> {
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
    
    private void configurarCampo() {
        campoNomeClientePendentes.addKeyListener(new KeyAdapter() {
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
    
    public void carregarCacheBuscaClientesSacola() {
        try {
            System.out.println("Puxando lista contábil de sacolas para a memória RAM...");
            sdao.buscaNomeClienteSacolaCloud(s); 
            String nomesStr = s.getNomeCliente(); 
            
            if (nomesStr != null && !nomesStr.isEmpty()) {
                listaCacheClientes = Arrays.asList(nomesStr.split(";"));
                System.out.println("Cache da TelaSacola carregado com " + listaCacheClientes.size() + " nomes.");
            } else {
                listaCacheClientes = new ArrayList<>();
                System.out.println("-> ATENÇÃO: Nenhuma sacola ativa com status 'EM_SEPARACAO' foi encontrada na Cloud.");
                System.out.println("---------------------------------------------------------------------------------");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Erro ao inicializar cache de sacolas: " + ex.getMessage());
        }
    }
    
    public void buscarSugestaoNome() throws ClassNotFoundException, SQLException {
        long tempoAtual = System.currentTimeMillis();
        // Trava anti-bomba de processador
        if (tempoAtual - ultimoDisparoBusca < 300) {
            return; 
        }
        ultimoDisparoBusca = tempoAtual;
        
        nomeCliente = campoNomeClientePendentes.getText().trim();
        listModel.clear();
        
        if (nomeCliente.length() < 2) {
            if (popupSugestoes != null) popupSugestoes.setVisible(false);
            return;
        }
        if (listaCacheClientes == null || listaCacheClientes.isEmpty()) {
            carregarCacheBuscaClientesSacola();
        }
        
        System.out.println("Buscando localmente na RAM: " + nomeCliente);
        
        // Filtragem flash em memória RAM (Zero idas à nuvem na digitação!) [links: 10]
        String termoUpper = nomeCliente.toUpperCase();
        listaCacheClientes.stream().filter((clienteItem) -> (clienteItem.toUpperCase().contains(termoUpper))).forEachOrdered((clienteItem) -> {
            listModel.addElement(clienteItem.trim());
        });
        
        // 🔥 TRAVA VISUAL DE SUCESSO: Só altera o pop-up se existirem correspondências reais
        if (listModel.getSize() > 0 && popupSugestoes != null) {
            System.out.println("Popup mostrado de forma estável.");
            SwingUtilities.invokeLater(() -> {
                // Força o cálculo dinâmico da largura (Evita o bugar com 0px)
                int largura = campoNomeClientePendentes.getWidth();
                if (largura <= 0) {
                    largura = 280; 
                }
                popupSugestoes.getComponent(0).setPreferredSize(new Dimension(largura, 150));
                popupSugestoes.pack(); 
                popupSugestoes.show(campoNomeClientePendentes, 0, campoNomeClientePendentes.getHeight());
            });
        } else {
            // Só oculta se a busca em memória RAM zerar completamente [links: 10]
            if (popupSugestoes != null && popupSugestoes.isVisible()) {
                System.out.println("Popup ocultado automaticamente - zero resultados.");
                popupSugestoes.setVisible(false);
            }
        }
    }
    
    public void dataEntregaFormatada(){
        LocalDateTime date = LocalDateTime.now();
        String data = date.format(DateTimeFormatter.ISO_DATE);
        String dia = data.substring(8,10);
        String mes = data.substring(5,7);
        String ano = data.substring(0,4);
        dataEntrega = dia+"/"+mes+"/"+ano;
    }
    
    public void atualizaItemParaDisponivelTabelaInterface() throws SQLException{
        try {
            sdao.sacolaFinalizadaParaTabelaSacolasCloud(s);
            sdao.sacolaFinalizadaParaTabelaSacolas(s);
            nomeCliente = s.getNomeCliente();
            dataCompra = s.getDataCompra().toString();
            String diaf = dataCompra.substring(8,10);
            String mesf = dataCompra.substring(5,7);
            String anof = dataCompra.substring(0,4);
            dataCompras = diaf+"/"+mesf+"/"+anof;
            System.out.println("Data Compra: "+dataCompra);
            valorCompra = s.getValorCompra();
            System.out.println("Valor Compra: "+valorCompra);
            id = s.getVendaId();
            status = s.getStatus();                
            DefaultTableModel sacolas = (DefaultTableModel) tabelaSacolas.getModel();   
            sacolas.addRow(new Object[]{
                nomeCliente,
                dataCompras,
                valorCompra,
                pecaCodigo,
                id,
                status
            });                                  
            System.out.println("ID para busca: "+id);
            
        } catch (ClassNotFoundException ex) {
             Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
             System.err.println("Erro: "+ex);
        }       
    }
    
    public void atualizaStatusDisponivelParaTabelas() throws ClassNotFoundException, SQLException{
        System.out.println("Data de entrega: "+dataEntrega);
        System.out.println("-----------------------------------");
        System.out.println("Atualizando Tabela de Vendas");
        System.out.println("-----------------------------------");
        System.out.println("Atualizando Tabela de Sacolas");
        System.out.println("-----------------------------------");
        System.out.println("Atualizando Tabela de Entregas");
        System.out.println("-----------------------------------");
        atualizaStatusDisponivelParaTodos();
        atualizaStatusDisponivelParaTodosCloud();
        System.out.println("Finalizando Atualização das Tabelas");
        System.out.println("-----------------------------------");
//        JOptionPane.showMessageDialog(rootPane, "Cliente "+nomeCliente+", ID Venda "+id+" com SACOLA Finalizada em "+dataEntrega);
    }
    
    public void atualizaStatusDisponivelParaTodos() throws ClassNotFoundException, SQLException{
        try {
            sdao.atualizaStatusDisponivelTabelaVendas(s);       
            sdao.atualizaStatusDisponivelTabelaVendasCloud(s);
            sdao.atualizaStatusDisponivelTabelaSacola(s);            
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Erro: "+ex);
            Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    
    public void atualizaStatusDisponivelParaTodosCloud(){
        try {
            sdao.atualizaStatusDisponivelTabelaSacolaCloud(s);
            sdao.atualizaStatusDisponivelTabelaEntregas(s);
            sdao.atualizaStatusDisponivelTabelaEntregasCloud(s);            
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Erro: "+ex);
            Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void iniciaTabelaSacolasVencidas() throws InterruptedException, ClassNotFoundException{
        try {
            Thread.sleep(5000);
            sdao.carregaSacolaTresMesesMais(s);
        } catch (SQLException ex) {
            Logger.getLogger(TelaSacola.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void carregaTabelaSacola(){
        int n = 0;
        try {
            vdao.pesquisaEntregasEndereco(v);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
        id = v.getIdVenda();
        System.out.println(id);
        dataCompra = (v.getDataVenda().toString().replaceAll("-", "/"));
        System.out.println(dataCompra);
        String dia = dataCompra.substring(8, 10);
        System.out.println(dia);
        String mes = dataCompra.substring(5, 7);
        System.out.println(mes);
        String ano = dataCompra.substring(0, 4);
        System.out.println(ano);
        dataCompra = dia+"/"+mes+"/"+ano;
        System.out.println(dia+"/"+mes+"/"+ano);           
        nomeCliente = v.getNomeCliente();
        System.out.println(nomeCliente);
        DefaultTableModel vendas = (DefaultTableModel) tabelaSacolas.getModel();
        vendas.setNumRows(n);
        for (int i = 1; i <= n; i++) {
           vendas.addRow(new Object[]{this.nomeCliente, this.dataCompra, this.id});
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonFinalizar;
    private javax.swing.JButton buttonLimparSacola;
    private javax.swing.JButton buttonMenu;
    private javax.swing.JButton buttonPendentes;
    private javax.swing.JButton buttonPesquisaNomeClientePendentes;
    private javax.swing.JButton buttonPesquisarClienteDisponiveis;
    private javax.swing.JTextField campoNomeClienteDisponiveis;
    private javax.swing.JTextField campoNomeClientePendentes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTable tabelaSacolas;
    private javax.swing.JPanel telaSacola;
    // End of variables declaration//GEN-END:variables
}
