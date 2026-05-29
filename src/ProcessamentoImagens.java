import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ProcessamentoImagens extends JFrame {

    // ── Paleta de cores ────────────────────────────────────────────────────────
    private static final Color BG_DARK          = new Color(18,  18,  24);
    private static final Color BG_PANEL         = new Color(28,  28,  38);
    private static final Color BG_CARD          = new Color(38,  38,  52);
    private static final Color ACCENT           = new Color(99, 179, 237);
    private static final Color TEXT_PRIMARY     = new Color(230, 230, 240);
    private static final Color TEXT_SECONDARY   = new Color(140, 140, 160);
    private static final Color BORDER_COLOR     = new Color(55,  55,  75);
    private static final Color BTN_ACTION       = new Color(72, 149, 239);
    private static final Color BTN_WARN         = new Color(239, 125,  72);
    private static final Color BTN_NEUTRAL      = new Color(90,  90, 120);
    private static final Color STATUS_OK        = new Color(80, 200, 120);
    private static final Color STATUS_WARN      = new Color(239, 196, 72);

    // ── Fontes ─────────────────────────────────────────────────────────────────
    private static final Font FONT_LABEL        = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN          = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font FONT_STATUS       = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_HEADER       = new Font("Segoe UI", Font.BOLD,  18);

    // ── Componentes principais ─────────────────────────────────────────────────
    private JLabel lblImagemOriginal;
    private JLabel lblImagemTransformada;
    private JLabel lblStatus;
    private JLabel lblDimensoesOrig;
    private JLabel lblDimensoesTrans;

    private BufferedImage imgOriginal;
    private BufferedImage imgTransformada;
    private BufferedImage imgOriginalSalva;
    private java.util.Deque<BufferedImage> historicoOriginal = new java.util.ArrayDeque<>();

    private JButton btnAplicar;
    private JButton btnOriginal;
    private JButton btnHistorico;

    // ══════════════════════════════════════════════════════════════════════════
    //  CONSTRUTOR
    // ══════════════════════════════════════════════════════════════════════════
    public ProcessamentoImagens() {
        aplicarLookAndFeel();

        setTitle("PDI  ·  Sistema de Processamento Digital de Imagens");
        setSize(1280, 760);
        setMinimumSize(new Dimension(960, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_DARK);

        add(criarPainelCabecalho(), BorderLayout.NORTH);
        add(criarPainelImagens(),   BorderLayout.CENTER);
        add(criarPainelRodape(),    BorderLayout.SOUTH);

        criarMenus();
        definirStatus("Bem-vindo! Abra uma imagem para comecar.", STATUS_OK);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOOK & FEEL
    // ══════════════════════════════════════════════════════════════════════════
    private void aplicarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("control",                BG_PANEL);
                    UIManager.put("info",                   BG_CARD);
                    UIManager.put("nimbusBase",             BG_DARK);
                    UIManager.put("nimbusBlueGrey",         BG_PANEL);
                    UIManager.put("nimbusLightBackground",  BG_CARD);
                    UIManager.put("text",                   TEXT_PRIMARY);
                    UIManager.put("menuText",               TEXT_PRIMARY);
                    UIManager.put("nimbusFocus",            ACCENT);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CABECALHO
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel criarPainelCabecalho() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(BG_PANEL);
        painel.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        esq.setOpaque(false);
        JLabel titulo = new JLabel("Image Processing Studio");
        titulo.setFont(FONT_HEADER);
        titulo.setForeground(TEXT_PRIMARY);
        esq.add(titulo);

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        dir.setOpaque(false);
        JLabel autor = new JLabel("Autor: Patrick Andrei Pinheiro de Lemos  ·  Matricula 0403027");
        autor.setFont(FONT_STATUS);
        autor.setForeground(TEXT_SECONDARY);
        dir.add(autor);

        painel.add(esq, BorderLayout.WEST);
        painel.add(dir, BorderLayout.EAST);
        return painel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAINEL CENTRAL
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel criarPainelImagens() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 12, 0));
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(new EmptyBorder(14, 14, 6, 14));
        wrapper.add(criarCardImagem("ORIGINAL",     true));
        wrapper.add(criarCardImagem("TRANSFORMADA", false));
        return wrapper;
    }

    private JPanel criarCardImagem(String titulo, boolean isOriginal) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel cabCard = new JPanel(new BorderLayout());
        cabCard.setOpaque(false);

        JLabel lblTitulo = new JLabel("  " + titulo);
        lblTitulo.setFont(FONT_BTN);
        lblTitulo.setForeground(ACCENT);

        JLabel lblDim = new JLabel("—");
        lblDim.setFont(FONT_STATUS);
        lblDim.setForeground(TEXT_SECONDARY);
        lblDim.setHorizontalAlignment(SwingConstants.RIGHT);

        cabCard.add(lblTitulo, BorderLayout.WEST);
        cabCard.add(lblDim,    BorderLayout.EAST);

        JLabel lblImg = new JLabel(isOriginal
                ? "<html><center><span style='color:#6B6B8A;font-size:13px'>Nenhuma imagem carregada<br><small>Arquivo &gt; Abrir imagem</small></span></center></html>"
                : "<html><center><span style='color:#6B6B8A;font-size:13px'>Aguardando transformacao...</span></center></html>",
                SwingConstants.CENTER);
        lblImg.setOpaque(true);
        lblImg.setBackground(BG_DARK);
        lblImg.setBorder(new LineBorder(BORDER_COLOR, 1));

        JScrollPane scroll = new JScrollPane(lblImg);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);

        if (isOriginal) {
            lblImagemOriginal = lblImg;
            lblDimensoesOrig  = lblDim;
        } else {
            lblImagemTransformada = lblImg;
            lblDimensoesTrans     = lblDim;
        }

        card.add(cabCard, BorderLayout.NORTH);
        card.add(scroll,  BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RODAPE
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel criarPainelRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(BG_PANEL);
        rodape.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

        lblStatus = new JLabel("  Pronto");
        lblStatus.setFont(FONT_STATUS);
        lblStatus.setForeground(STATUS_OK);

        JPanel esqRodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        esqRodape.setOpaque(false);
        JLabel ponto = new JLabel("*");
        ponto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ponto.setForeground(STATUS_OK);
        esqRodape.add(ponto);
        esqRodape.add(lblStatus);

        btnHistorico = criarBotao("Ultima imagem",      BTN_NEUTRAL);
        btnOriginal  = criarBotao("Imagem original",    BTN_WARN);
        btnAplicar   = criarBotao("Usar como original", BTN_ACTION);

        btnHistorico.setVisible(false);
        btnOriginal.setVisible(false);
        btnAplicar.setVisible(false);

        btnAplicar.addActionListener(e -> aplicarTransformadaComoOriginal());
        btnOriginal.addActionListener(e -> RetornaImagemOriginal());
        btnHistorico.addActionListener(e -> RetornaUltimaImagem());

        JPanel dirRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 7));
        dirRodape.setOpaque(false);
        dirRodape.add(btnHistorico);
        dirRodape.add(btnOriginal);
        dirRodape.add(btnAplicar);

        rodape.add(esqRodape, BorderLayout.WEST);
        rodape.add(dirRodape, BorderLayout.EAST);
        return rodape;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILITARIOS VISUAIS
    // ══════════════════════════════════════════════════════════════════════════
    private JButton criarBotao(String texto, Color cor) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? cor.brighter() : cor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private void definirStatus(String msg, Color cor) {
        if (lblStatus != null) {
            lblStatus.setText(msg);
            lblStatus.setForeground(cor);
        }
    }

    private String formatarDimensoes(BufferedImage img) {
        if (img == null) return "—";
        return img.getWidth() + " x " + img.getHeight() + " px";
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOGICA DE CONTROLE
    // ══════════════════════════════════════════════════════════════════════════
    private void aplicarTransformadaComoOriginal() {
        if (imgTransformada == null) return;

        historicoOriginal.push(imgOriginal);
        imgOriginal = imgTransformada;
        imgTransformada = null;

        exibirImagem(lblImagemOriginal, imgOriginal, lblDimensoesOrig);
        limparTransformada();

        btnAplicar.setVisible(false);
        btnOriginal.setVisible(true);
        btnHistorico.setVisible(true);
        definirStatus("Transformacao aplicada como original.", ACCENT);
    }

    private void RetornaImagemOriginal() {
        imgOriginal = imgOriginalSalva;
        imgTransformada = null;
        exibirImagem(lblImagemOriginal, imgOriginal, lblDimensoesOrig);
        limparTransformada();
        btnOriginal.setVisible(false);
        btnHistorico.setVisible(false);
        btnAplicar.setVisible(false);
        definirStatus("Imagem original restaurada.", STATUS_OK);
    }

    private void RetornaUltimaImagem() {
        if (!historicoOriginal.isEmpty()) {
            imgOriginal = historicoOriginal.pop();
            imgTransformada = null;
            exibirImagem(lblImagemOriginal, imgOriginal, lblDimensoesOrig);
            limparTransformada();
            if (historicoOriginal.isEmpty()) {
                btnOriginal.setVisible(false);
                btnHistorico.setVisible(false);
            }
            definirStatus("Historico: imagem anterior restaurada.", STATUS_WARN);
        }
    }

    private void exibirImagem(JLabel label, BufferedImage img, JLabel lblDim) {
        label.setIcon(new ImageIcon(img));
        label.setText("");
        if (lblDim != null) lblDim.setText(formatarDimensoes(img));
    }

    private void limparTransformada() {
        lblImagemTransformada.setIcon(null);
        lblImagemTransformada.setText("<html><center><span style='color:#6B6B8A;font-size:13px'>Aguardando transformacao...</span></center></html>");
        if (lblDimensoesTrans != null) lblDimensoesTrans.setText("—");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENUS
    // ══════════════════════════════════════════════════════════════════════════
    private void criarMenus() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BG_PANEL);
        menuBar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        menuBar.add(criarMenuArquivo());
        menuBar.add(criarMenuComItens("Transformacoes Geometricas",
                "Transladar", "Rotacionar", "Espelhar", "Ampliar", "Reduzir"));
        menuBar.add(criarMenuComItens("Filtros",
                "Grayscale", "Brilho", "Contraste", "Passa Baixa", "Passa Alta", "Threshold"));
        menuBar.add(criarMenuComItens("Morfologia Matematica",
                "Dilatacao", "Erosao", "Abertura", "Fechamento", "Afinamento"));

        JMenu menuExtra = criarMenuBase("Extracao de Caracteristicas");
        menuExtra.add(estilizarItem(new JMenuItem("DESAFIO")));
        menuBar.add(menuExtra);

        setJMenuBar(menuBar);
    }

    private JMenu criarMenuArquivo() {
        JMenu menu = criarMenuBase("Arquivo");
        JMenuItem itemAbrir  = estilizarItem(new JMenuItem("Abrir imagem..."));
        JMenuItem itemSalvar = estilizarItem(new JMenuItem("Salvar imagem..."));
        JMenuItem itemSobre  = estilizarItem(new JMenuItem("Sobre"));
        JMenuItem itemSair   = estilizarItem(new JMenuItem("Sair"));

        itemAbrir.addActionListener(e -> abrirImagem());
        itemSalvar.addActionListener(e -> salvarImagem());
        itemSobre.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Matricula: 0403027\nNome: Patrick Andrei Pinheiro de Lemos\n" +
                "Projeto: Sistema de Processamento de Imagens\nDisciplina: PDI"));
        itemSair.addActionListener(e -> System.exit(0));

        menu.add(itemAbrir);
        menu.add(itemSalvar);
        menu.addSeparator();
        menu.add(itemSobre);
        menu.addSeparator();
        menu.add(itemSair);
        return menu;
    }

    private JMenu criarMenuComItens(String nomeMenu, String... itens) {
        JMenu menu = criarMenuBase(nomeMenu);
        for (String op : itens) {
            JMenuItem item = estilizarItem(new JMenuItem(op));
            item.addActionListener(e -> ProcessaImagem(op));
            menu.add(item);
        }
        return menu;
    }

    private JMenu criarMenuBase(String nome) {
        JMenu menu = new JMenu(nome);
        menu.setFont(FONT_LABEL);
        menu.setForeground(TEXT_PRIMARY);
        menu.getPopupMenu().setBackground(BG_CARD);
        menu.getPopupMenu().setBorder(new LineBorder(BORDER_COLOR, 1));
        return menu;
    }

    private JMenuItem estilizarItem(JMenuItem item) {
        item.setFont(FONT_LABEL);
        item.setForeground(TEXT_PRIMARY);
        item.setBackground(BG_CARD);
        item.setOpaque(true);
        return item;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ABRIR / SALVAR
    // ══════════════════════════════════════════════════════════════════════════
    private void abrirImagem() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecione uma imagem");
        fc.setFileFilter(new FileNameExtensionFilter("Imagens (JPG, PNG, BMP)", "jpg", "jpeg", "png", "bmp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                imgOriginal = ImageIO.read(fc.getSelectedFile());
                imgOriginalSalva = imgOriginal;
                imgTransformada = null;
                exibirImagem(lblImagemOriginal, imgOriginal, lblDimensoesOrig);
                limparTransformada();
                btnAplicar.setVisible(false);
                btnOriginal.setVisible(false);
                btnHistorico.setVisible(false);
                historicoOriginal.clear();
                definirStatus("Imagem carregada: " + fc.getSelectedFile().getName(), STATUS_OK);
                pack();
                setSize(1280, 760);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void salvarImagem() {
        if (imgTransformada == null) {
            JOptionPane.showMessageDialog(this, "Nao ha imagem transformada para salvar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar Imagem Transformada");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".png")) f = new File(f.getAbsolutePath() + ".png");
                ImageIO.write(imgTransformada, "png", f);
                definirStatus("Imagem salva: " + f.getName(), STATUS_OK);
                JOptionPane.showMessageDialog(this, "Imagem salva com sucesso!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PROCESSAMENTO — unico ponto que escreve em imgTransformada e exibe na tela
    // ══════════════════════════════════════════════════════════════════════════
    private void ProcessaImagem(String tecnica) {
        if (imgOriginal == null) {
            JOptionPane.showMessageDialog(this, "Abra uma imagem primeiro", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        definirStatus("Aplicando: " + tecnica + "...", STATUS_WARN);

        BufferedImage resultado = null;

        switch (tecnica) {
            case "Transladar": {
                JTextField txtX = new JTextField(5), txtY = new JTextField(5);
                JPanel p = new JPanel();
                p.add(new JLabel("Eixo X:")); p.add(txtX);
                p.add(Box.createHorizontalStrut(15));
                p.add(new JLabel("Eixo Y:")); p.add(txtY);
                if (JOptionPane.showConfirmDialog(this, p, "Translacao", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    try { resultado = TransladarImagem(imgOriginal, Integer.parseInt(txtX.getText()), Integer.parseInt(txtY.getText())); }
                    catch (NumberFormatException ex) { erroNumero(); }
                }
                break;
            }
            case "Rotacionar": {
                String s = JOptionPane.showInputDialog(this, "Angulo de rotacao (graus):", "45");
                if (s != null && !s.trim().isEmpty()) {
                    try { resultado = RotacionarImagem(imgOriginal, Double.parseDouble(s)); }
                    catch (NumberFormatException ex) { erroNumero(); }
                }
                break;
            }
            case "Espelhar": {
                String[] op = {"Horizontal", "Vertical"};
                int c = JOptionPane.showOptionDialog(this, "Tipo de espelhamento:", "Espelhar", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, op, op);
                if (c != JOptionPane.CLOSED_OPTION) resultado = EspelharImagem(imgOriginal, c == 0);
                break;
            }
            case "Ampliar":
            case "Reduzir": {
                String inputStr = JOptionPane.showInputDialog(this, "Percentual (ex: 50)%:", "50");
                if (inputStr != null && !inputStr.trim().isEmpty()) {
                    try {
                        double s = "Ampliar".equals(tecnica) ? 
                            1 + (Double.parseDouble(inputStr) / 100) : 
                            1 - (Double.parseDouble(inputStr) / 100);
                        resultado = AmpliarReduzirImagem(imgOriginal, s);
                    } catch (NumberFormatException ex) { erroNumero(); }
                }
                break;
            }
            case "Grayscale":
                resultado = GrayscaleImagem(imgOriginal);
                break;
            case "Brilho": {
                String s = JOptionPane.showInputDialog(this, "Fator de brilho:", "100");
                if (s != null && !s.trim().isEmpty()) {
                    try { resultado = BrilhoImagen(imgOriginal, Double.parseDouble(s)); }
                    catch (NumberFormatException ex) { erroNumero(); }
                }
                break;
            }
            case "Contraste": {
                String s = JOptionPane.showInputDialog(this, "Fator de contraste:", "10");
                if (s != null && !s.trim().isEmpty()) {
                    try { resultado = ContrasteImagen(imgOriginal, Double.parseDouble(s)); }
                    catch (NumberFormatException ex) { erroNumero(); }
                }
                break;
            }
            case "Passa Baixa": {
                String[] op = {"Media","Moda","Mediana","Gauss"};
                int c = JOptionPane.showOptionDialog(this, "Aplicar filtro por:", "Passa Baixa", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, op, op);
                if (c != JOptionPane.CLOSED_OPTION) resultado = PassaBaixaImagem(imgOriginal, op[c]);
                break;
            }
            case "Passa Alta": {
                String[] op = {"Roberts","Sobel","Kirsch","Robinson","Marr and Hildreth", "Canny"};
                int c = JOptionPane.showOptionDialog(this, "Aplicar filtro por:", "Passa Alta", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, op, op);
                if (c != JOptionPane.CLOSED_OPTION) resultado = PassaAltaImagem(imgOriginal, op[c]);
                break;
            }
            case "Threshold": {
                String s = JOptionPane.showInputDialog(this, "Valor de limiar (0-255):", "128");
                if (s != null && !s.trim().isEmpty()) {
                    try { resultado = ThresholdImagem(imgOriginal, Integer.parseInt(s)); }
                    catch (NumberFormatException ex) { erroNumero(); }
                }
                break;
            }
            case "Dilatacao": {
                String[] op = {"Disco","Cruz","Quadrado","Hexágono","Segmento de Linha", "Par de pontos"};
                int c = JOptionPane.showOptionDialog(this, "Aplicar morfologia matematica pelo elemento estruturante de:", "Dilatação", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, op, op);
                if (c != JOptionPane.CLOSED_OPTION) resultado = DilatacaoImagem(imgOriginal, op[c]);
                break;
            }
            case "Erosao": {
                
            }
            case "Abertura": {

            }
            case "Fechamento": {

            }
            case "Afinamento": {
                String[] op1 = {"Stentiford","Zhang-Suen","Holt"};
                String[] op2 = {"Disco","Cruz","Quadrado","Hexágono","Segmento de Linha", "Par de pontos"};
                int c1 = JOptionPane.showOptionDialog(this, "Aplicar morfologia matematica por:", "Afinamento", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, op1, op1);
                int c2 = JOptionPane.showOptionDialog(this, "Aplicar morfologia matematica pelo elemento estruturante de:", "Dilatação", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, op2, op2);
                if (c1 != JOptionPane.CLOSED_OPTION && c2 != JOptionPane.CLOSED_OPTION) resultado = AfinamentoImagem(imgOriginal, op1[c1], op2[c2]);
                break;
            }
            default:
                JOptionPane.showMessageDialog(this, "Tecnica '" + tecnica + "' sera implementada em breve.");
                definirStatus("Pronto.", TEXT_SECONDARY);
        }

        // ── Exibicao centralizada ──────────────────────────────────────────────
        if (resultado != null) {
            imgTransformada = resultado;
            exibirImagem(lblImagemTransformada, imgTransformada, lblDimensoesTrans);
            btnAplicar.setVisible(true);
            definirStatus("Transformacao concluida. Dimensoes: " + formatarDimensoes(imgTransformada), STATUS_OK);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private BufferedImage criarImagemEmBranco(BufferedImage orig) { return new BufferedImage(orig.getWidth(), orig.getHeight(), orig.getType()); }

    private int clamp(int v) { return Math.min(255, Math.max(0, v)); }

    private void erroNumero() { JOptionPane.showMessageDialog(this, "Por favor, digite um numero valido.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE); }

    // ══════════════════════════════════════════════════════════════════════════
    //  ALGORITMOS DE PROCESSAMENTO — retornam BufferedImage
    // ══════════════════════════════════════════════════════════════════════════
    public BufferedImage TransladarImagem(BufferedImage img, int tx, int ty) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int ox = x - tx, oy = y - ty;
            saida.setRGB(x, y, (ox >= 0 && ox < w && oy >= 0 && oy < h) ? img.getRGB(ox, oy) : Color.WHITE.getRGB());
        }
        return saida;
    }

    public BufferedImage RotacionarImagem(BufferedImage img, double graus) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        double rad = Math.toRadians(graus), cos = Math.cos(rad), sin = Math.sin(rad);
        int cx = w / 2, cy = h / 2;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int dx = x - cx, dy = y - cy;
            int ox = (int) Math.round(dx * cos + dy * sin) + cx;
            int oy = (int) Math.round(-dx * sin + dy * cos) + cy;
            saida.setRGB(x, y, (ox >= 0 && ox < w && oy >= 0 && oy < h) ? img.getRGB(ox, oy) : Color.WHITE.getRGB());
        }
        return saida;
    }

    public BufferedImage EspelharImagem(BufferedImage img, boolean posicao) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int ox = posicao ? w - 1 - x : x;
            int oy = posicao ? y : h - 1 - y;
            saida.setRGB(x, y, img.getRGB(ox, oy));
        }
        return saida;
    }

    public BufferedImage AmpliarReduzirImagem(BufferedImage img, double fator) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int ox = (int) Math.round(x / fator), oy = (int) Math.round(y / fator);
            saida.setRGB(x, y, (ox >= 0 && ox < w && oy >= 0 && oy < h) ? img.getRGB(ox, oy) : Color.WHITE.getRGB());
        }
        return saida;
    }

    public BufferedImage GrayscaleImagem(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            Color c = new Color(img.getRGB(x, y));
            int g = (int) Math.round(0.2121 * c.getRed() + 0.7154 * c.getGreen() + 0.0721 * c.getBlue());
            //int g = (int) Math.round(0.50 * c.getRed() + 0.419 * c.getGreen() + 0.081 * c.getBlue());

            saida.setRGB(x, y, new Color(g, g, g).getRGB());
        }
        return saida;
    }

    public BufferedImage BrilhoImagen(BufferedImage img, double brilho) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            Color c = new Color(img.getRGB(x, y));
            saida.setRGB(x, y, new Color(
                clamp(c.getRed()   + (int) brilho),
                clamp(c.getGreen() + (int) brilho),
                clamp(c.getBlue()  + (int) brilho)).getRGB());
        }
        return saida;
    }

    public BufferedImage ContrasteImagen(BufferedImage img, double contraste) {
        int w = img.getWidth(), h = img.getHeight(), compens = 128;
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            Color c = new Color(img.getRGB(x, y));
            saida.setRGB(x, y, new Color(
                clamp((int) Math.round((c.getRed()   - compens) * contraste + compens)),
                clamp((int) Math.round((c.getGreen() - compens) * contraste + compens)),
                clamp((int) Math.round((c.getBlue()  - compens) * contraste + compens))).getRGB());
        }
        return saida;
    }

    public BufferedImage PassaBaixaImagem(BufferedImage img, String tecnica) {
        int w = img.getWidth(), h = img.getHeight();
        int w1 = w - 1, h1 = h - 1;

        BufferedImage cinza = GrayscaleImagem(img);

        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) saida.setRGB(x, y, cinza.getRGB(x, y));

        int i, j, novoValor;
        double z;

        switch (tecnica) {
            case "Media" -> {
                int[][] mascara = {
                    {1,1,1},
                    {1,1,1},
                    {1,1,1}
                };
                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    z = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) z += new Color(cinza.getRGB(x + (j-1), y + (i-1))).getRed() * mascara[i][j];
                    novoValor = clamp((int) Math.round(z / 9));
                    saida.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }
            }
            case "Moda" -> {

            }
            case "Mediana" -> {

            }
            case "Gauss" -> {
                int[][] mascara = {
                    {1,2,1},
                    {2,4,2},
                    {1,2,1}
                };
                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    z = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) z += new Color(cinza.getRGB(x + (j-1), y + (i-1))).getRed() * mascara[i][j];
                    novoValor = clamp((int) Math.round(z / 16));
                    saida.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }

            }
            default -> {}
        }

        return saida;
    }

    public BufferedImage PassaAltaImagem(BufferedImage img, String tecnica) {
        int w = img.getWidth(), h = img.getHeight();
        int w1 = w - 1, h1 = h - 1;

        BufferedImage cinza = GrayscaleImagem(img);

        BufferedImage saida = criarImagemEmBranco(img);
        int i, j, novoValor;
        double z;

        switch (tecnica) {
            case "Roberts" -> {

            }
            case "Sobel" -> {

            }
            case "Kirsch" -> {

            }
            case "Robinson" -> {

            }
            case "Marr and Hildreth" -> {
                int[][] mascaraGauss = {
                    {1,2,1},
                    {2,4,2},
                    {1,2,1}
                };
                BufferedImage gaussiana = criarImagemEmBranco(img);

                for (int y = 0; y < h; y++) {
                    gaussiana.setRGB(0, y, cinza.getRGB(0, y));
                    gaussiana.setRGB(w - 1, y, cinza.getRGB(w - 1, y));
                }
                for (int x = 0; x < w; x++) {
                    gaussiana.setRGB(x, 0, cinza.getRGB(x, 0));
                    gaussiana.setRGB(x, h - 1, cinza.getRGB(x, h - 1));
                }

                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    z = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) z += new Color(cinza.getRGB(x + (j-1), y + (i-1))).getRed() * mascaraGauss[i][j];
                    novoValor = clamp((int) Math.round(z / 16));
                    gaussiana.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }

                int[][] mascaraLaplaciano = {
                    {0,1,0},
                    {1,-4,1},
                    {0,1,0}
                };
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) saida.setRGB(x, y, Color.BLACK.getRGB());

                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    z = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) z += new Color(gaussiana.getRGB(x + (j-1), y + (i-1))).getRed() * mascaraLaplaciano[i][j];
                    novoValor = clamp((int) Math.abs(z));
                    saida.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }
            }
            case "Canny" -> {
                int[][] mascaraGauss = {
                    {1,2,1},
                    {2,4,2},
                    {1,2,1}
                };
                BufferedImage gaussiana = criarImagemEmBranco(img);

                for (int y = 0; y < h; y++) {
                    gaussiana.setRGB(0, y, cinza.getRGB(0, y));
                    gaussiana.setRGB(w - 1, y, cinza.getRGB(w - 1, y));
                }
                for (int x = 0; x < w; x++) {
                    gaussiana.setRGB(x, 0, cinza.getRGB(x, 0));
                    gaussiana.setRGB(x, h - 1, cinza.getRGB(x, h - 1));
                }

                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    z = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) z += new Color(cinza.getRGB(x + (j-1), y + (i-1))).getRed() * mascaraGauss[i][j];
                    novoValor = clamp((int) Math.round(z / 16));
                    gaussiana.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }

                int[][] sobelX = {
                    {-1,0,1},
                    {-2,0,2},
                    {-1,0,1}
                };
                int[][] sobelY = {
                    {-1,-2,-1},
                    { 0, 0, 0},
                    { 1, 2, 1}
                };
                double[] magnitude = new double[w * h];
                double[] angulo    = new double[w * h];

                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    double gx = 0, gy = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) {
                        int pixel = new Color(gaussiana.getRGB(x + (j-1), y + (i-1))).getRed();
                        gx += pixel * sobelX[i][j];
                        gy += pixel * sobelY[i][j];
                    }
                    magnitude[y * w + x] = Math.sqrt(gx * gx + gy * gy);
                    angulo[y * w + x]    = Math.toDegrees(Math.atan2(gy, gx));
                }

                double[] suprimido = new double[w * h];
                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    double ang = angulo[y * w + x];
                    if (ang < 0) ang += 180;
                    double mag = magnitude[y * w + x];
                    double n1, n2;
                    if      (ang < 22.5  || ang >= 157.5) { n1 = magnitude[y * w + (x+1)];     n2 = magnitude[y * w + (x-1)]; }
                    else if (ang < 67.5)                  { n1 = magnitude[(y+1) * w + (x-1)]; n2 = magnitude[(y-1) * w + (x+1)]; }
                    else if (ang < 112.5)                 { n1 = magnitude[(y+1) * w + x];      n2 = magnitude[(y-1) * w + x]; }
                    else                                  { n1 = magnitude[(y-1) * w + (x-1)]; n2 = magnitude[(y+1) * w + (x+1)]; }
                    suprimido[y * w + x] = (mag >= n1 && mag >= n2) ? mag : 0;
                }

                final int LIMITE_ALTO = 100, LIMITE_BAIXO = 50;
                final int FRACO = 128, FORTE = 255;

                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) saida.setRGB(x, y, Color.BLACK.getRGB());

                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    double mag = suprimido[y * w + x];
                    if      (mag >= LIMITE_ALTO)  novoValor = FORTE;
                    else if (mag >= LIMITE_BAIXO) novoValor = FRACO;
                    else                          novoValor = 0;
                    saida.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }

                boolean mudou = true;
                while (mudou) {
                    mudou = false;
                    for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                        if (new Color(saida.getRGB(x, y)).getRed() == FRACO) {
                            boolean conectado = false;
                            for (i = -1; i <= 1 && !conectado; i++) for (j = -1; j <= 1 && !conectado; j++) 
                                if (new Color(saida.getRGB(x + j, y + i)).getRed() == FORTE) conectado = true;
                            if (conectado) { saida.setRGB(x, y, new Color(FORTE, FORTE, FORTE).getRGB()); mudou = true; }
                        }
                    }
                }

                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++)
                    if (new Color(saida.getRGB(x, y)).getRed() == FRACO) saida.setRGB(x, y, Color.BLACK.getRGB());
            }
            default -> {}
        }

        return saida;
    }

    public BufferedImage ThresholdImagem(BufferedImage img, int limiar) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            Color c = new Color(img.getRGB(x, y));
            int g = (int) Math.round(0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
            int novoValor = (g >= limiar) ? 255 : 0;
            saida.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
        }
        return saida;
    }

    public BufferedImage DilatacaoImagem(BufferedImage img, String estruturante){
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        int[][] matrizEstruturante = MatrizEstruturante(estruturante);


        return saida;
    }

    public BufferedImage AfinamentoImagem(BufferedImage img, String tecnica, String estruturante) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);

        int[][] matrizEstruturante = MatrizEstruturante(estruturante);

        switch (tecnica) {
            case "Stentiford" -> {

            }
            case "Zhang-Suen" -> {

            }
            case "Holt" -> {

            }
        }

        return saida;
    }

    public int[][] MatrizEstruturante(String estruturante){
        int[][] matriz = {{0,0,0},{0,0,0},{0,0,0}};

        switch (estruturante) {
            case "Disco" -> {

            }
            case "Cruz" -> {
                int[][] cruz = {
                    {0,1,0},
                    {1,1,1},
                    {0,1,0}
                };

                matriz = cruz;
            }
            case "Quadrado" -> {
                int[][] quadrado = {
                    {1,1,1},
                    {1,1,1},
                    {1,1,1}
                };
                
                matriz = quadrado;
            }
            case "Hexágono" -> {

            }
            case "Segmento de Linha" -> {
                int[][] linha = {
                    {1,1,1},
                };
                
                matriz = linha;
            }
            case "Par de Pontos" -> {
                int[][] par = {
                    {1,0,1}
                };

                matriz = par;
            }
            default -> {}
        }

        return matriz;
    }

    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProcessamentoImagens().setVisible(true));
    }
}