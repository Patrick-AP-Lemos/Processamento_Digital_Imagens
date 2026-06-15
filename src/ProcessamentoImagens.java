import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ProcessamentoImagens extends JFrame {

    // ── Paleta de cores ────────────────────────────────────────────────────────
    private static final Color BG_DARK        = new Color(18, 18, 24);
    private static final Color BG_PANEL       = new Color(28, 28, 38);
    private static final Color BG_CARD        = new Color(38, 38, 52);
    private static final Color ACCENT         = new Color(99, 179, 237);
    private static final Color TEXT_PRIMARY   = new Color(230, 230, 240);
    private static final Color TEXT_SECONDARY  = new Color(140, 140, 160);
    private static final Color BORDER_COLOR    = new Color(55, 55, 75);
    private static final Color BTN_ACTION     = new Color(72, 149, 239);
    private static final Color BTN_WARN       = new Color(239, 125, 72);
    private static final Color BTN_NEUTRAL    = new Color(90, 90, 120);
    private static final Color STATUS_OK      = new Color(80, 200, 120);
    private static final Color STATUS_WARN     = new Color(239, 196, 72);

    // ── Fontes ─────────────────────────────────────────────────────────────────
    private static final Font FONT_LABEL      = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN        = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_STATUS     = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_HEADER     = new Font("Segoe UI", Font.BOLD, 18);

    // ── Componentes principais ─────────────────────────────────────────────────
    private JLabel lblImagemOriginal;
    private JLabel lblImagemTransformada;
    private JLabel lblStatus;
    private JLabel lblDimensoesOrig;
    private JLabel lblDimensoesTrans;

    private BufferedImage imgOriginal;
    private BufferedImage imgTransformada;
    private BufferedImage imgOriginalSalva;
    private final Deque<BufferedImage> historicoOriginal = new ArrayDeque<>();

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
        add(criarPainelImagens(), BorderLayout.CENTER);
        add(criarPainelRodape(), BorderLayout.SOUTH);

        criarMenus();
        definirStatus("Bem-vindo! Abra uma imagem para começar.", STATUS_OK);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOOK & FEEL
    // ══════════════════════════════════════════════════════════════════════════
    private void aplicarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("control", BG_PANEL);
                    UIManager.put("info", BG_CARD);
                    UIManager.put("nimbusBase", BG_DARK);
                    UIManager.put("nimbusBlueGrey", BG_PANEL);
                    UIManager.put("nimbusLightBackground", BG_CARD);
                    UIManager.put("text", TEXT_PRIMARY);
                    UIManager.put("menuText", TEXT_PRIMARY);
                    UIManager.put("nimbusFocus", ACCENT);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CABEÇALHO
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
        wrapper.add(criarCardImagem("ORIGINAL", true));
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
        cabCard.add(lblDim, BorderLayout.EAST);

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
            lblDimensoesOrig = lblDim;
        } else {
            lblImagemTransformada = lblImg;
            lblDimensoesTrans = lblDim;
        }

        card.add(cabCard, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RODAPÉ
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

        btnHistorico = criarBotao("Ultima imagem", BTN_NEUTRAL);
        btnOriginal = criarBotao("Imagem original", BTN_WARN);
        btnAplicar = criarBotao("Usar como original", BTN_ACTION);

        btnAplicar.setVisible(false);
        btnOriginal.setVisible(false);
        btnHistorico.setVisible(false);

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
    //  UTILITÁRIOS VISUAIS
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

    private BufferedImage clonarImagem(BufferedImage src) {
        if (src == null) return null;
        BufferedImage copia = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = copia.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return copia;
    }

    private int gray(int rgb) {
        Color c = new Color(rgb, true);
        return clamp((int) Math.round(0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()));
    }

    private BufferedImage grayCopy(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int g = gray(img.getRGB(x, y));
                out.setRGB(x, y, new Color(g, g, g).getRGB());
            }
        }
        return out;
    }

    private BufferedImage binaryCopy(BufferedImage img, int limiar) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage cinza = grayCopy(img);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int g = new Color(cinza.getRGB(x, y)).getRed();
                int v = g >= limiar ? 255 : 0;
                out.setRGB(x, y, new Color(v, v, v).getRGB());
            }
        }
        return out;
    }

    private boolean pixelBranco(BufferedImage img, int x, int y) {
        return new Color(img.getRGB(x, y), true).getRed() > 127;
    }

    private boolean pixelBrancoSeguro(BufferedImage img, int x, int y) {
        if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) return false;
        return pixelBranco(img, x, y);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LÓGICA DE CONTROLE
    // ══════════════════════════════════════════════════════════════════════════
    private void aplicarTransformadaComoOriginal() {
        if (imgTransformada == null) return;

        historicoOriginal.push(clonarImagem(imgOriginal));
        imgOriginal = clonarImagem(imgTransformada);
        imgTransformada = null;

        exibirImagem(lblImagemOriginal, imgOriginal, lblDimensoesOrig);
        limparTransformada();

        btnAplicar.setVisible(false);
        btnOriginal.setVisible(true);
        btnHistorico.setVisible(true);
        definirStatus("Transformacao aplicada como original.", ACCENT);
    }

    private void RetornaImagemOriginal() {
        if (imgOriginalSalva == null) return;
        imgOriginal = clonarImagem(imgOriginalSalva);
        imgTransformada = null;
        historicoOriginal.clear();
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
        if (img == null) return;
        label.setIcon(new ImageIcon(img));
        label.setText("");
        if (lblDim != null) lblDim.setText(formatarDimensoes(img));
        label.revalidate();
        label.repaint();
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
        menuBar.add(criarMenuTransformacoes());
        menuBar.add(criarMenuFiltros());
        menuBar.add(criarMenuMorfologia());

        JMenu menuExtra = criarMenuBase("Extracao de Caracteristicas");
        String[] desafios = {
            "Ex 1 - Relogio Analogico", 
            "Ex 2 - Contagem por Cores", 
            "Ex 3 - Letras do Alfabeto", 
            "Ex 4 - Placas de Transito", 
            "Ex 5 - Grafico de Barras"
        };
        for (String d : desafios) {
            JMenuItem item = estilizarItem(new JMenuItem(d));
            // Vamos mandar a string exata, ex: "Desafio:Ex 1 - Relogio Analogico"
            item.addActionListener(e -> ProcessaImagem("Desafio:" + d));
            menuExtra.add(item);
        }
        menuBar.add(menuExtra);

        setJMenuBar(menuBar);
    }

    private JMenu criarMenuArquivo() {
        JMenu menu = criarMenuBase("Arquivo");
        JMenuItem itemAbrir = estilizarItem(new JMenuItem("Abrir imagem..."));
        JMenuItem itemSalvar = estilizarItem(new JMenuItem("Salvar imagem..."));
        JMenuItem itemSobre = estilizarItem(new JMenuItem("Sobre"));
        JMenuItem itemSair = estilizarItem(new JMenuItem("Sair"));

        itemAbrir.addActionListener(e -> abrirImagem());
        itemSalvar.addActionListener(e -> salvarImagem());
        itemSobre.addActionListener(e -> JOptionPane.showMessageDialog(this, """
                                                                             Matricula: 0403027
                                                                             Nome: Patrick Andrei Pinheiro de Lemos
                                                                             Projeto: Sistema de Processamento de Imagens
                                                                             Disciplina: PDI"""));
        itemSair.addActionListener(e -> System.exit(0));

        menu.add(itemAbrir);
        menu.add(itemSalvar);
        menu.addSeparator();
        menu.add(itemSobre);
        menu.addSeparator();
        menu.add(itemSair);
        return menu;
    }

    private JMenu criarMenuTransformacoes() {
        JMenu menu = criarMenuBase("Transformacoes Geometricas");
        menu.add(criarItemProcessamento("Transladar"));
        menu.add(criarItemProcessamento("Rotacionar"));
        menu.addSeparator();

        JMenu Esp = criarMenuBase("Espelhar");
        Esp.add(criarItemProcessamento("Horizontal"));
        Esp.add(criarItemProcessamento("Vertical"));
        menu.add(Esp);

        menu.addSeparator();
        menu.add(criarItemProcessamento("Ampliar"));
        menu.add(criarItemProcessamento("Reduzir"));
        return menu;
    }
    
    private JMenu criarMenuFiltros() {        
        JMenu menu = criarMenuBase("Filtros");
        menu.add(criarItemProcessamento("Grayscale"));
        menu.add(criarItemProcessamento("Brilho"));
        menu.add(criarItemProcessamento("Contraste"));
        menu.addSeparator();

        JMenu pb = criarMenuBase("Passa Baixa");
        pb.add(criarItemProcessamento("Media"));
        pb.add(criarItemProcessamento("Moda"));
        pb.add(criarItemProcessamento("Mediana"));
        pb.add(criarItemProcessamento("Gauss"));
        menu.add(pb);

        JMenu pa = criarMenuBase("Passa Alta");
        pa.add(criarItemProcessamento("Roberts"));
        pa.add(criarItemProcessamento("Sobel"));
        pa.add(criarItemProcessamento("Kirsch"));
        pa.add(criarItemProcessamento("Robinson"));
        pa.add(criarItemProcessamento("Marr and Hildreth"));
        pa.add(criarItemProcessamento("Canny"));
        menu.add(pa);

        menu.addSeparator();
        menu.add(criarItemProcessamento("Threshold"));
        return menu;
    }

    private JMenu criarMenuMorfologia() {
        JMenu menu = criarMenuBase("Morfologia Matematica");
        String[] ee = {"Disco","Cruz","Quadrado","Hexágono","Segmento de Linha","Par de Pontos"};

        JMenu d = criarMenuBase("Dilatacao");
        JMenu e = criarMenuBase("Erosao");
        JMenu a = criarMenuBase("Abertura");
        JMenu f = criarMenuBase("Fechamento");

        for (String x : ee) {
            d.add(criarItemMorfologia("Dilatacao", x));
            e.add(criarItemMorfologia("Erosao", x));
            a.add(criarItemMorfologia("Abertura", x));
            f.add(criarItemMorfologia("Fechamento", x));
        }

        menu.add(d); menu.add(e); menu.add(a); menu.add(f);

        JMenu af = criarMenuBase("Afinamento");
        String[] algoritmos = {"Stentiford", "Zhang-Suen", "Holt"};

        for (String alg : algoritmos) {
            JMenuItem item = estilizarItem(new JMenuItem(alg));
            // Envia a instrução simples: "Afinamento:Zhang-Suen"
            item.addActionListener(evt -> ProcessaImagem("Afinamento:" + alg));
            af.add(item);
        }
        menu.add(af);

        return menu;
    }

    private JMenuItem criarItemMorfologia(String operacao, String estruturante){
        JMenuItem item = estilizarItem(new JMenuItem(estruturante));
        // Passa a combinação "Operação:Estruturante" (ex: "Dilatacao:Disco")
        item.addActionListener(e -> ProcessaImagem(operacao + ":" + estruturante));
        return item;
    }

    private JMenuItem criarItemProcessamento(String operacao){
        JMenuItem item = estilizarItem(new JMenuItem(operacao));
        item.addActionListener(e -> ProcessaImagem(operacao));
        return item;
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
                if (imgOriginal == null) {
                    throw new IllegalArgumentException("Formato de imagem invalido ou nao suportado.");
                }
                imgOriginal = clonarImagem(imgOriginal);
                imgOriginalSalva = clonarImagem(imgOriginal);
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
    //  PROCESSAMENTO
    // ══════════════════════════════════════════════════════════════════════════
    private void ProcessaImagem(String tecnica) {
        if (imgOriginal == null) {
            JOptionPane.showMessageDialog(this, "Abra uma imagem primeiro", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        definirStatus("Aplicando: " + tecnica + "...", STATUS_WARN);

        BufferedImage resultado = null;

        if (tecnica.contains(":")) {
            String[] partes = tecnica.split(":");
            String operacao = partes[0]; // Pode ser Dilatacao, Erosao ou Afinamento

            if (operacao.equals("Afinamento")) {
                // Para afinamento, partes[1] é o nome do algoritmo
                String algoritmo = partes[1];
                resultado = AfinamentoImagem(imgOriginal, algoritmo);
            } else if (operacao.equals("Desafio")) {
                String exercicio = partes[1];
                ResolverDesafio(imgOriginal, exercicio);
                // Retornamos direto porque o resultado será em texto (JOptionPane), e não necessariamente uma nova imagem desenhada na tela
                return;
            } else {
                // Caso seja Morfologia Básica: "Dilatacao:Disco"
                String estruturante = partes[1];
                switch (operacao) {
                    case "Dilatacao"  -> resultado = DilatacaoImagem(imgOriginal, estruturante);
                    case "Erosao"     -> resultado = ErosaoImagem(imgOriginal, estruturante);
                    case "Abertura"   -> resultado = AberturaImagem(imgOriginal, estruturante);
                    case "Fechamento" -> resultado = FechamentoImagem(imgOriginal, estruturante);
                }
            }
        } else {
            switch (tecnica) {
                case "Transladar" ->  {
                    JTextField txtX = new JTextField(5), txtY = new JTextField(5);
                    JPanel p1 = new JPanel();
                    p1.add(new JLabel("Eixo X:"));
                    p1.add(txtX);
                    p1.add(Box.createHorizontalStrut(15));
                    p1.add(new JLabel("Eixo Y:"));
                    p1.add(txtY);
                    if (JOptionPane.showConfirmDialog(this, p1, "Translacao", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        try {
                            resultado = TransladarImagem(imgOriginal, Integer.parseInt(txtX.getText()), Integer.parseInt(txtY.getText()));
                        } catch (NumberFormatException ex) {
                            erroNumero();
                        }
                    }
                }
                case "Rotacionar" ->  {
                    String s = JOptionPane.showInputDialog(this, "Angulo de rotacao (graus):", "45");
                    if (s != null && !s.trim().isEmpty()) {
                        try {
                            resultado = RotacionarImagem(imgOriginal, Double.parseDouble(s));
                        } catch (NumberFormatException ex) {
                            erroNumero();
                        }
                    }
                }
                // Espelhamento
                case "Horizontal" -> resultado = EspelharImagem(imgOriginal, true);
                case "Vertical"   -> resultado = EspelharImagem(imgOriginal, false);
                //////
                case "Ampliar", "Reduzir" -> {
                    String inputStr = JOptionPane.showInputDialog(this, "Percentual (ex: 50)%:", "50");
                    if (inputStr != null && !inputStr.trim().isEmpty()) {
                        try {
                            double percentual = Double.parseDouble(inputStr);
                            double s = "Ampliar".equals(tecnica) ? (1.0 + (percentual / 100.0)) : (1.0 - (percentual / 100.0));
                            if (s <= 0.0) {
                                JOptionPane.showMessageDialog(this, "O fator resultante precisa ser maior que zero.", "Aviso", JOptionPane.WARNING_MESSAGE);
                            }
                            resultado = AmpliarReduzirImagem(imgOriginal, s);
                        }catch (NumberFormatException ex) {
                            erroNumero();
                        }
                    }
                }
                case "Grayscale" -> resultado = GrayscaleImagem(imgOriginal);
                case "Brilho" -> {
                    String s = JOptionPane.showInputDialog(this, "Fator de brilho:", "100");
                    if (s != null && !s.trim().isEmpty()) {
                        try {
                            resultado = BrilhoImagen(imgOriginal, Double.parseDouble(s));
                        } catch (NumberFormatException ex) {
                            erroNumero();
                        }
                    }
                }
                case "Contraste" ->  {
                    String s = JOptionPane.showInputDialog(this, "Fator de contraste:", "10");
                    if (s != null && !s.trim().isEmpty()) {
                        try {
                            resultado = ContrasteImagen(imgOriginal, Double.parseDouble(s));
                        } catch (NumberFormatException ex) {
                            erroNumero();
                        }
                    }
                }
                //Passa Baixa
                case "Media" -> resultado = PassaBaixaImagem(imgOriginal, "Media");
                case "Moda" -> resultado = PassaBaixaImagem(imgOriginal, "Moda");
                case "Mediana" -> resultado = PassaBaixaImagem(imgOriginal, "Mediana");
                case "Gauss" -> resultado = PassaBaixaImagem(imgOriginal, "Gauss");
                //Passa Alta
                case "Roberts" -> resultado = PassaAltaImagem(imgOriginal, "Roberts");
                case "Sobel" -> resultado = PassaAltaImagem(imgOriginal, "Sobel");
                case "Kirsch" -> resultado = PassaAltaImagem(imgOriginal, "Kirsch");
                case "Robinson" -> resultado = PassaAltaImagem(imgOriginal, "Robinson");
                case "Marr and Hildreth" -> resultado = PassaAltaImagem(imgOriginal, "Marr and Hildreth");
                case "Canny" -> resultado = PassaAltaImagem(imgOriginal, "Canny");
                //////
                case "Threshold" -> {
                    String s = JOptionPane.showInputDialog(this, "Valor de limiar (0-255):", "128");
                    if (s != null && !s.trim().isEmpty()) {
                        try {
                            resultado = ThresholdImagem(imgOriginal, Integer.parseInt(s));
                        } catch (NumberFormatException ex) {
                            erroNumero();
                        }
                    }
                }
                default ->  {
                    JOptionPane.showMessageDialog(this, "Tecnica '" + tecnica + "' sera implementada em breve.");
                    definirStatus("Pronto.", TEXT_SECONDARY);
                }
            }
        }

        // Se alguma transformação retornou uma imagem válida, atualiza a interface
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
    private BufferedImage criarImagemEmBranco(BufferedImage orig) {
        return new BufferedImage(orig.getWidth(), orig.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    private int clamp(int v) {
        return Math.min(255, Math.max(0, v));
    }

    private void erroNumero() {
        JOptionPane.showMessageDialog(this, "Por favor, digite um numero valido.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ALGORITMOS DE PROCESSAMENTO
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

    public BufferedImage EspelharImagem(BufferedImage img, boolean horizontal) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int ox = horizontal ? (w - 1 - x) : x;
            int oy = horizontal ? y : (h - 1 - y);
            saida.setRGB(x, y, img.getRGB(ox, oy));
        }
        return saida;
    }

    public BufferedImage AmpliarReduzirImagem(BufferedImage img, double fator) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int ox = (int) Math.floor(x / fator);
            int oy = (int) Math.floor(y / fator);
            saida.setRGB(x, y, (ox >= 0 && ox < w && oy >= 0 && oy < h) ? img.getRGB(ox, oy) : Color.WHITE.getRGB());
        }
        return saida;
    }

    public BufferedImage GrayscaleImagem(BufferedImage img) {
        return grayCopy(img);
    }

    public BufferedImage BrilhoImagen(BufferedImage img, double brilho) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            Color c = new Color(img.getRGB(x, y), true);
            saida.setRGB(x, y, new Color(
                    clamp(c.getRed() + (int) brilho),
                    clamp(c.getGreen() + (int) brilho),
                    clamp(c.getBlue() + (int) brilho)).getRGB());
        }
        return saida;
    }

    public BufferedImage ContrasteImagen(BufferedImage img, double contraste) {
        int w = img.getWidth(), h = img.getHeight(), compens = 128;
        BufferedImage saida = criarImagemEmBranco(img);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            Color c = new Color(img.getRGB(x, y), true);
            saida.setRGB(x, y, new Color(
                    clamp((int) Math.round((c.getRed() - compens) * contraste + compens)),
                    clamp((int) Math.round((c.getGreen() - compens) * contraste + compens)),
                    clamp((int) Math.round((c.getBlue() - compens) * contraste + compens))).getRGB());
        }
        return saida;
    }

    public BufferedImage PassaBaixaImagem(BufferedImage img, String tecnica) {
        int w = img.getWidth(), h = img.getHeight();
        int w1 = w - 1, h1 = h - 1;

        BufferedImage cinza = GrayscaleImagem(img);
        BufferedImage saida = clonarImagem(cinza);

        int i, j, novoValor;
        double z;

        switch (tecnica) {
            case "Media" -> {
                int[][] mascara = {
                    {1, 1, 1},
                    {1, 1, 1},
                    {1, 1, 1}
                };
                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    z = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) z += new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed() * mascara[i][j];
                    novoValor = clamp((int) Math.round(z / 9.0));
                    saida.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }
            }
            case "Moda" -> {
                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    int[] frequencia = new int[256];
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) {
                        int valor = new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed();
                        frequencia[valor]++;
                    }
                    int moda = 0, maxFreq = 0;
                    for (i = 0; i < frequencia.length; i++) {
                        if (frequencia[i] > maxFreq) {
                            maxFreq = frequencia[i];
                            moda = i;
                        }
                    }
                    saida.setRGB(x, y, new Color(moda, moda, moda).getRGB());
                }
            }
            case "Mediana" -> {
                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    int[] vizinhos = new int[9];
                    int idx = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) vizinhos[idx++] = new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed();
                    Arrays.sort(vizinhos);
                    int mediana = vizinhos[4];
                    saida.setRGB(x, y, new Color(mediana, mediana, mediana).getRGB());
                }
            }
            case "Gauss" -> {
                int[][] mascara = {
                    {1, 2, 1},
                    {2, 4, 2},
                    {1, 2, 1}
                };
                for (int y = 1; y < h1; y++) for (int x = 1; x < w1; x++) {
                    z = 0;
                    for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) z += new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed() * mascara[i][j];
                    novoValor = clamp((int) Math.round(z / 16.0));
                    saida.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }
            }
            default -> {}
        }

        return saida;
    }

    public BufferedImage PassaAltaImagem(BufferedImage img, String tecnica) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage cinza = GrayscaleImagem(img);
        BufferedImage saida = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            saida.setRGB(0, y, Color.BLACK.getRGB());
            saida.setRGB(w - 1, y, Color.BLACK.getRGB());
        }
        for (int x = 0; x < w; x++) {
            saida.setRGB(x, 0, Color.BLACK.getRGB());
            saida.setRGB(x, h - 1, Color.BLACK.getRGB());
        }

        int[][] sobelX = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };
        int[][] sobelY = {
            {-1, -2, -1},
            {0, 0, 0},
            {1, 2, 1}
        };

        switch (tecnica) {
            case "Roberts" -> {
                for (int y = 0; y < h - 1; y++) {
                    for (int x = 0; x < w - 1; x++) {
                        int p00 = new Color(cinza.getRGB(x, y)).getRed();
                        int p01 = new Color(cinza.getRGB(x + 1, y)).getRed();
                        int p10 = new Color(cinza.getRGB(x, y + 1)).getRed();
                        int p11 = new Color(cinza.getRGB(x + 1, y + 1)).getRed();
                        int g1 = p00 - p11;
                        int g2 = p01 - p10;
                        int v = clamp((int) Math.round(Math.sqrt(g1 * g1 + g2 * g2)));
                        saida.setRGB(x, y, new Color(v, v, v).getRGB());
                    }
                }
            }
            case "Sobel" -> {
                for (int y = 1; y < h - 1; y++) {
                    for (int x = 1; x < w - 1; x++) {
                        double gx = 0, gy = 0;
                        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) {
                            int pixel = new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed();
                            gx += pixel * sobelX[i][j];
                            gy += pixel * sobelY[i][j];
                        }
                        int v = clamp((int) Math.round(Math.hypot(gx, gy)));
                        saida.setRGB(x, y, new Color(v, v, v).getRGB());
                    }
                }
            }
            case "Kirsch" -> {
                int[][][] masks = {
                    {{ 5,  5,  5}, { -3,  0, -3}, { -3, -3, -3}},
                    {{ 5,  5, -3}, {  5,  0, -3}, { -3, -3, -3}},
                    {{ 5, -3, -3}, {  5,  0, -3}, {  5, -3, -3}},
                    {{-3, -3, -3}, {  5,  0, -3}, {  5,  5, -3}},
                    {{-3, -3, -3}, { -3,  0, -3}, {  5,  5,  5}},
                    {{-3, -3, -3}, { -3,  0,  5}, { -3,  5,  5}},
                    {{-3, -3,  5}, { -3,  0,  5}, { -3, -3,  5}},
                    {{-3,  5,  5}, { -3,  0,  5}, { -3, -3, -3}}
                };
                for (int y = 1; y < h - 1; y++) {
                    for (int x = 1; x < w - 1; x++) {
                        int max = Integer.MIN_VALUE;
                        for (int[][] mask : masks) {
                            int acc = 0;
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    int pixel = new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed();
                                    acc += pixel * mask[i][j];
                                }
                            }
                            max = Math.max(max, acc);
                        }
                        int v = clamp(max);
                        saida.setRGB(x, y, new Color(v, v, v).getRGB());
                    }
                }
            }
            case "Robinson" -> {
                int[][][] masks = {
                    {{ 1, 1, 1}, { 1,  1, 1}, { -1, -1, -1}},
                    {{ 1, 1, 1}, { -1, 1, 1}, { -1, -1, 1}},
                    {{-1, 1, 1}, { -1, 1, 1}, { -1, 1, 1}},
                    {{-1, -1, 1}, { -1, 1, 1}, { -1, 1, 1}},
                    {{-1, -1, -1}, { 1, 1, 1}, { 1, 1, 1}},
                    {{-1, -1, 1}, { 1, 1, -1}, { 1, 1, -1}},
                    {{-1, 1, 1}, { -1, 1, 1}, { 1, 1, -1}},
                    {{ 1, 1, 1}, { 1, 1, -1}, { 1, -1, -1}}
                };
                for (int y = 1; y < h - 1; y++) {
                    for (int x = 1; x < w - 1; x++) {
                        int max = Integer.MIN_VALUE;
                        for (int[][] mask : masks) {
                            int acc = 0;
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    int pixel = new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed();
                                    acc += pixel * mask[i][j];
                                }
                            }
                            max = Math.max(max, acc);
                        }
                        int v = clamp(max);
                        saida.setRGB(x, y, new Color(v, v, v).getRGB());
                    }
                }
            }
            case "Marr and Hildreth" -> {
                int[][] mascaraGauss = {
                    {1, 2, 1},
                    {2, 4, 2},
                    {1, 2, 1}
                };
                BufferedImage gaussiana = grayCopy(img);

                for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                    double z = 0;
                    for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) z += new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed() * mascaraGauss[i][j];
                    int novoValor = clamp((int) Math.round(z / 16.0));
                    gaussiana.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }

                int[][] lap = {
                    {0, 1, 0},
                    {1, -4, 1},
                    {0, 1, 0}
                };

                for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                    double z = 0;
                    for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) z += new Color(gaussiana.getRGB(x + (j - 1), y + (i - 1))).getRed() * lap[i][j];
                    int v = clamp((int) Math.abs(Math.round(z)));
                    saida.setRGB(x, y, new Color(v, v, v).getRGB());
                }
            }
            case "Canny" -> {
                int[][] mascaraGauss = {
                    {1, 2, 1},
                    {2, 4, 2},
                    {1, 2, 1}
                };
                BufferedImage gaussiana = grayCopy(img);

                for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                    double z = 0;
                    for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) z += new Color(cinza.getRGB(x + (j - 1), y + (i - 1))).getRed() * mascaraGauss[i][j];
                    int novoValor = clamp((int) Math.round(z / 16.0));
                    gaussiana.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }

                double[] magnitude = new double[w * h];
                double[] angulo = new double[w * h];

                for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                    double gx = 0, gy = 0;
                    for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) {
                        int pixel = new Color(gaussiana.getRGB(x + (j - 1), y + (i - 1))).getRed();
                        gx += pixel * sobelX[i][j];
                        gy += pixel * sobelY[i][j];
                    }
                    magnitude[y * w + x] = Math.hypot(gx, gy);
                    angulo[y * w + x] = Math.toDegrees(Math.atan2(gy, gx));
                }

                double[] suprimido = new double[w * h];
                for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                    double ang = angulo[y * w + x];
                    if (ang < 0) ang += 180;
                    double mag = magnitude[y * w + x];
                    double n1, n2;
                    if (ang < 22.5 || ang >= 157.5) {
                        n1 = magnitude[y * w + (x + 1)];
                        n2 = magnitude[y * w + (x - 1)];
                    } else if (ang < 67.5) {
                        n1 = magnitude[(y + 1) * w + (x - 1)];
                        n2 = magnitude[(y - 1) * w + (x + 1)];
                    } else if (ang < 112.5) {
                        n1 = magnitude[(y + 1) * w + x];
                        n2 = magnitude[(y - 1) * w + x];
                    } else {
                        n1 = magnitude[(y - 1) * w + (x - 1)];
                        n2 = magnitude[(y + 1) * w + (x + 1)];
                    }
                    suprimido[y * w + x] = (mag >= n1 && mag >= n2) ? mag : 0;
                }

                final int LIMITE_ALTO = 100, LIMITE_BAIXO = 50;
                final int FRACO = 128, FORTE = 255;

                for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                    double mag = suprimido[y * w + x];
                    int novoValor;
                    if (mag >= LIMITE_ALTO) novoValor = FORTE;
                    else if (mag >= LIMITE_BAIXO) novoValor = FRACO;
                    else novoValor = 0;
                    saida.setRGB(x, y, new Color(novoValor, novoValor, novoValor).getRGB());
                }

                boolean mudou = true;
                while (mudou) {
                    mudou = false;
                    for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                        if (new Color(saida.getRGB(x, y)).getRed() == FRACO) {
                            boolean conectado = false;
                            for (int i = -1; i <= 1 && !conectado; i++) {
                                for (int j = -1; j <= 1 && !conectado; j++) {
                                    if (new Color(saida.getRGB(x + j, y + i)).getRed() == FORTE) conectado = true;
                                }
                            }
                            if (conectado) {
                                saida.setRGB(x, y, new Color(FORTE, FORTE, FORTE).getRGB());
                                mudou = true;
                            }
                        }
                    }
                }

                for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                    if (new Color(saida.getRGB(x, y)).getRed() == FRACO) {
                        saida.setRGB(x, y, Color.BLACK.getRGB());
                    }
                }
            }
            default -> {}
        }

        return saida;
    }

    public BufferedImage ThresholdImagem(BufferedImage img, int limiar) {
        return binaryCopy(img, limiar);
    }

    public BufferedImage DilatacaoImagem(BufferedImage img, String estruturante) {
        BufferedImage bin = binaryCopy(img, 128);
        int[][] m = MatrizEstruturante(estruturante);
        int h = m.length;
        int w = m[0].length;
        int oy = h / 2;
        int ox = w / 2;
        BufferedImage saida = new BufferedImage(bin.getWidth(), bin.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < bin.getHeight(); y++) {
            for (int x = 0; x < bin.getWidth(); x++) {
                boolean ativar = false;
                for (int i = 0; i < h && !ativar; i++) {
                    for (int j = 0; j < w && !ativar; j++) {
                        if (m[i][j] == 1) {
                            int px = x + (j - ox);
                            int py = y + (i - oy);
                            if (pixelBrancoSeguro(bin, px, py)) ativar = true;
                        }
                    }
                }
                int v = ativar ? 255 : 0;
                saida.setRGB(x, y, new Color(v, v, v).getRGB());
            }
        }
        return saida;
    }

    public BufferedImage ErosaoImagem(BufferedImage img, String estruturante) {
        BufferedImage bin = binaryCopy(img, 128);
        int[][] m = MatrizEstruturante(estruturante);
        int h = m.length;
        int w = m[0].length;
        int oy = h / 2;
        int ox = w / 2;
        BufferedImage saida = new BufferedImage(bin.getWidth(), bin.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < bin.getHeight(); y++) {
            for (int x = 0; x < bin.getWidth(); x++) {
                boolean manter = true;
                for (int i = 0; i < h && manter; i++) {
                    for (int j = 0; j < w && manter; j++) {
                        if (m[i][j] == 1) {
                            int px = x + (j - ox);
                            int py = y + (i - oy);
                            if (!pixelBrancoSeguro(bin, px, py)) manter = false;
                        }
                    }
                }
                int v = manter ? 255 : 0;
                saida.setRGB(x, y, new Color(v, v, v).getRGB());
            }
        }
        return saida;
    }

    public BufferedImage AberturaImagem(BufferedImage img, String estruturante) {
        return DilatacaoImagem(ErosaoImagem(img, estruturante), estruturante);
    }

    public BufferedImage FechamentoImagem(BufferedImage img, String estruturante) {
        return ErosaoImagem(DilatacaoImagem(img, estruturante), estruturante);
    }

    public BufferedImage AfinamentoImagem(BufferedImage img, String tecnica) {
        BufferedImage bin = binaryCopy(img, 128);
        BufferedImage resultado;
        
        switch (tecnica) {
            case "Stentiford" -> resultado = thinningStentiford(bin);
            case "Zhang-Suen" -> resultado = thinningZhangSuen(bin);
            case "Holt"       -> resultado = thinningHolt(bin);
            default           -> resultado = clonarImagem(bin);
        }
        return resultado;
    }

    private BufferedImage thinningStentiford(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean changed;
        BufferedImage work = clonarImagem(img);

        do {
            changed = false;
            // Stentiford realiza 4 passagens por iteração (Topo, Esquerda, Fundo, Direita)
            for (int pass = 1; pass <= 4; pass++) {
                java.util.List<Point> toRemove = new java.util.ArrayList<>();

                for (int y = 1; y < h - 1; y++) {
                    for (int x = 1; x < w - 1; x++) {
                        if (!pixelBranco(work, x, y)) continue;

                        int b = vizinhosBrancos(work, x, y);
                        int a = transicoesBrancoPreto(work, x, y);

                        boolean p2 = pixelBranco(work, x, y - 1); // Topo
                        boolean p4 = pixelBranco(work, x + 1, y); // Direita
                        boolean p6 = pixelBranco(work, x, y + 1); // Fundo
                        boolean p8 = pixelBranco(work, x - 1, y); // Esquerda

                        boolean isEdge = false;
                        if (pass == 1) isEdge = !p2; // Borda superior
                        if (pass == 2) isEdge = !p8; // Borda esquerda
                        if (pass == 3) isEdge = !p6; // Borda inferior
                        if (pass == 4) isEdge = !p4; // Borda direita

                        // Condições de Stentiford: 
                        // 1. É a borda da passagem atual
                        // 2. Não é um ponto isolado ou extremidade de linha (b > 1 e b < 8)
                        // 3. O número de conectividade (transições) é exatamente 1
                        if (isEdge && b > 1 && b < 8 && a == 1) {
                            toRemove.add(new Point(x, y));
                        }
                    }
                }

                if (!toRemove.isEmpty()) changed = true;
                for (Point p : toRemove) work.setRGB(p.x, p.y, Color.BLACK.getRGB());
            }
        } while (changed);

        return work;
    }

    private BufferedImage thinningZhangSuen(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean changed;
        BufferedImage work = clonarImagem(img);

        do {
            changed = false;
            java.util.List<Point> toRemove = new java.util.ArrayList<>();

            for (int y = 1; y < h - 1; y++) {
                for (int x = 1; x < w - 1; x++) {
                    if (!pixelBranco(work, x, y)) continue;
                    int b = vizinhosBrancos(work, x, y);
                    int a = transicoesBrancoPreto(work, x, y);
                    boolean p2 = pixelBranco(work, x, y - 1);
                    boolean p4 = pixelBranco(work, x + 1, y);
                    boolean p6 = pixelBranco(work, x, y + 1);
                    boolean p8 = pixelBranco(work, x - 1, y);
                    if (b >= 2 && b <= 6 && a == 1 && !(p2 && p4 && p6) && !(p4 && p6 && p8)) {
                        toRemove.add(new Point(x, y));
                    }
                }
            }
            if (!toRemove.isEmpty()) changed = true;
            for (Point p : toRemove) work.setRGB(p.x, p.y, Color.BLACK.getRGB());

            toRemove.clear();
            for (int y = 1; y < h - 1; y++) {
                for (int x = 1; x < w - 1; x++) {
                    if (!pixelBranco(work, x, y)) continue;
                    int b = vizinhosBrancos(work, x, y);
                    int a = transicoesBrancoPreto(work, x, y);
                    boolean p2 = pixelBranco(work, x, y - 1);
                    boolean p4 = pixelBranco(work, x + 1, y);
                    boolean p6 = pixelBranco(work, x, y + 1);
                    boolean p8 = pixelBranco(work, x - 1, y);
                    if (b >= 2 && b <= 6 && a == 1 && !(p2 && p4 && p8) && !(p2 && p6 && p8)) {
                        toRemove.add(new Point(x, y));
                    }
                }
            }
            if (!toRemove.isEmpty()) changed = true;
            for (Point p : toRemove) work.setRGB(p.x, p.y, Color.BLACK.getRGB());
        } while (changed);

        return work;
    }

    private BufferedImage thinningHolt(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean changed;
        BufferedImage work = clonarImagem(img);

        do {
            changed = false;
            java.util.List<Point> toRemove = new java.util.ArrayList<>();

            for (int y = 1; y < h - 1; y++) {
                for (int x = 1; x < w - 1; x++) {
                    if (!pixelBranco(work, x, y)) continue;

                    int b = vizinhosBrancos(work, x, y);
                    int a = transicoesBrancoPreto(work, x, y);

                    boolean p2 = pixelBranco(work, x, y - 1); // Topo
                    boolean p4 = pixelBranco(work, x + 1, y); // Direita
                    boolean p6 = pixelBranco(work, x, y + 1); // Fundo
                    boolean p8 = pixelBranco(work, x - 1, y); // Esquerda
                    
                    // Condição de borda modificada do Holt (staircase preservation)
                    boolean edgeCondition = !(p2 && p4 && p6) && !(p4 && p6 && p8) 
                                         && !(p2 && p4 && p8) && !(p2 && p6 && p8);

                    // Condições de remoção num único bloco (One-pass variation)
                    if (b >= 2 && b <= 6 && a == 1 && edgeCondition) {
                        toRemove.add(new Point(x, y));
                    }
                }
            }
            
            if (!toRemove.isEmpty()) changed = true;
            for (Point p : toRemove) {
                work.setRGB(p.x, p.y, Color.BLACK.getRGB());
            }
            
        } while (changed);

        return work;
    }

    private int vizinhosBrancos(BufferedImage img, int x, int y) {
        int count = 0;
        if (pixelBranco(img, x, y - 1)) count++;
        if (pixelBranco(img, x + 1, y - 1)) count++;
        if (pixelBranco(img, x + 1, y)) count++;
        if (pixelBranco(img, x + 1, y + 1)) count++;
        if (pixelBranco(img, x, y + 1)) count++;
        if (pixelBranco(img, x - 1, y + 1)) count++;
        if (pixelBranco(img, x - 1, y)) count++;
        if (pixelBranco(img, x - 1, y - 1)) count++;
        return count;
    }

    private int transicoesBrancoPreto(BufferedImage img, int x, int y) {
        boolean[] p = new boolean[] {
            pixelBranco(img, x, y - 1),
            pixelBranco(img, x + 1, y - 1),
            pixelBranco(img, x + 1, y),
            pixelBranco(img, x + 1, y + 1),
            pixelBranco(img, x, y + 1),
            pixelBranco(img, x - 1, y + 1),
            pixelBranco(img, x - 1, y),
            pixelBranco(img, x - 1, y - 1),
            pixelBranco(img, x, y - 1)
        };
        int count = 0;
        for (int i = 0; i < 8; i++) {
            if (!p[i] && p[i + 1]) count++;
        }
        return count;
    }

    public int[][] MatrizEstruturante(String estruturante) {
        int[][] matriz = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};

        switch (estruturante) {
            case "Disco" -> {
                matriz = new int[][]{
                    {0, 1, 0},
                    {1, 1, 1},
                    {0, 1, 0}
                };
            }
            case "Cruz" -> {
                matriz = new int[][]{
                    {0, 1, 0},
                    {1, 1, 1},
                    {0, 1, 0}
                };
            }
            case "Quadrado" -> {
                matriz = new int[][]{
                    {1, 1, 1},
                    {1, 1, 1},
                    {1, 1, 1}
                };
            }
            case "Hexágono" -> {
                matriz = new int[][]{
                    {0, 1, 1},
                    {1, 1, 1},
                    {1, 1, 0}
                };
            }
            case "Segmento de Linha" -> {
                matriz = new int[][]{
                    {1, 1, 1}
                };
            }
            case "Par de Pontos" -> {
                matriz = new int[][]{
                    {1, 0, 1}
                };
            }
            default -> {}
        }

        return matriz;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DESAFIOS
    // ══════════════════════════════════════════════════════════════════════════
    private void ResolverDesafio(BufferedImage img, String exercicio) {
        if (img == null) return;
        
        try {
            switch (exercicio) {
                case "Ex 1 - Relogio Analogico" -> resolverRelogio(img);
                case "Ex 2 - Contagem por Cores" -> resolverContagemCores(img);
                case "Ex 3 - Letras do Alfabeto" -> resolverLetras(img);
                case "Ex 4 - Placas de Transito" -> resolverPlacas(img);
                case "Ex 5 - Grafico de Barras" -> resolverGrafico(img);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao analisar a imagem: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Exercício 1: Relógio Analógico
    private void resolverRelogio(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        // 1. Binarização: pegar apenas pixels escuros (assumindo relógio escuro em fundo claro)
        BufferedImage cinza = grayCopy(img);
        boolean[][] isPreto = new boolean[w][h];
        long sumX = 0, sumY = 0;
        int countPreto = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (new Color(cinza.getRGB(x, y)).getRed() < 128) {
                    isPreto[x][y] = true;
                    sumX += x;
                    sumY += y;
                    countPreto++;
                }
            }
        }

        if (countPreto == 0) throw new RuntimeException("Nenhum pixel escuro encontrado.");

        // 2. Centro de massa (eixo do relógio)
        int cx = (int) (sumX / countPreto);
        int cy = (int) (sumY / countPreto);

        // Garante que o centro calculado caia exatamente em um pixel preto do eixo
        if (!isPreto[cx][cy]) {
            int raioBusca = 1;
            boolean achou = false;
            while (!achou && raioBusca < 20) {
                for (int i = -raioBusca; i <= raioBusca && !achou; i++) {
                    for (int j = -raioBusca; j <= raioBusca && !achou; j++) {
                        int nx = cx + i, ny = cy + j;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h && isPreto[nx][ny]) {
                            cx = nx; cy = ny; achou = true;
                        }
                    }
                }
                raioBusca++;
            }
        }

        // 3. Isolar os ponteiros (Busca em Largura - BFS)
        java.util.List<Point> ponteiros = new java.util.ArrayList<>();
        boolean[][] visitado = new boolean[w][h];
        java.util.Queue<Point> fila = new java.util.LinkedList<>();
        
        fila.add(new Point(cx, cy));
        visitado[cx][cy] = true;

        while (!fila.isEmpty()) {
            Point p = fila.poll();
            ponteiros.add(p);

            // Busca os 8 vizinhos
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int nx = p.x + j, ny = p.y + i;
                    if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                        if (isPreto[nx][ny] && !visitado[nx][ny]) {
                            visitado[nx][ny] = true;
                            fila.add(new Point(nx, ny));
                        }
                    }
                }
            }
        }

        // 4. Encontrar a ponta do ponteiro dos MINUTOS (maior distância)
        Point pontaMinuto = cx == 0 ? new Point(0,0) : new Point(cx, cy);
        double maxDistMin = -1;

        for (Point p : ponteiros) {
            double dist = p.distance(cx, cy);
            if (dist > maxDistMin) {
                maxDistMin = dist;
                pontaMinuto = p;
            }
        }

        // Calcula o ângulo do ponteiro dos minutos
        double anguloMinutoRad = Math.atan2(pontaMinuto.y - cy, pontaMinuto.x - cx);

        // 5. Encontrar a ponta do ponteiro das HORAS (segunda maior distância, ignorando a reta do minuto)
        Point pontaHora = new Point(cx, cy);
        double maxDistHora = -1;

        for (Point p : ponteiros) {
            double anguloAtual = Math.atan2(p.y - cy, p.x - cx);
            double diffAngulo = Math.abs(anguloAtual - anguloMinutoRad);
            // Corrige a diferença circular de ângulos (ex: -179 e 180 são próximos)
            if (diffAngulo > Math.PI) diffAngulo = 2 * Math.PI - diffAngulo;

            // Só considera pixels que estejam a pelo menos 20 graus de diferença do minuto
            if (Math.toDegrees(diffAngulo) > 20.0) {
                double dist = p.distance(cx, cy);
                if (dist > maxDistHora) {
                    maxDistHora = dist;
                    pontaHora = p;
                }
            }
        }
        
        double anguloHoraRad = Math.atan2(pontaHora.y - cy, pontaHora.x - cx);

        // 6. Converter ângulos matemáticos para formato de relógio
        // Em Java (e na imagem), o Y cresce para baixo.
        // O ângulo 0 (direita) é 3h. Subtraímos 90 para alinhar o topo (12h) no 0.
        // Adicionamos 360 para evitar números negativos e usamos o módulo.
        double angHoraDeg = (Math.toDegrees(anguloHoraRad) + 90 + 360) % 360;
        double angMinDeg = (Math.toDegrees(anguloMinutoRad) + 90 + 360) % 360;

        // Minutos são os mais simples. Cada marca de minuto é 6 graus (360/60)
        int minutos = (int) Math.round(angMinDeg / 6.0) % 60;

        // Horas precisam de cuidado especial. Cada hora tem 30 graus (360/12).
        // A hora base é puramente a divisão do ângulo por 30 arredondada para BAIXO.
        int horas = (int) Math.floor(angHoraDeg / 30.0);

        // Como o ponteiro das horas se move gradualmente à medida que os minutos passam,
        // às vezes um ângulo pode estar muito próximo do próximo número (ex: 10:55 estaria colado no 11).
        // Como já usamos Math.floor, pegaremos a hora "anterior".
        if (horas == 0) horas = 12; // Ajuste para 12h (ângulo entre 0 e 29)

        String horario = String.format("%02d:%02d", horas, minutos);

        JOptionPane.showMessageDialog(this, 
            "Analise Concluida!\nHorario detectado: " + horario, 
            "Resultado do Relogio", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    //Exercício 2: Contagem por Cores
    private void resolverContagemCores(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        
        boolean[][] visitado = new boolean[w][h];
        java.util.Map<String, Integer> contagem = new java.util.HashMap<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (visitado[x][y]) continue;

                Color c = new Color(img.getRGB(x, y), true);
                String nomeCor = classificarCor(c);
                
                // Se o pixel principal for fundo (Branco) ou sombra (Preto), ignoramos
                if (c.getAlpha() == 0 || nomeCor.equals("Branco") || nomeCor.equals("Preto")) {
                    visitado[x][y] = true;
                    continue;
                }

                // Inicia a varredura (Flood Fill) para medir o tamanho do objeto
                int tamanhoObjeto = 0;
                java.util.Queue<Point> fila = new java.util.LinkedList<>();
                fila.add(new Point(x, y));
                visitado[x][y] = true;

                while (!fila.isEmpty()) {
                    Point p = fila.poll();
                    tamanhoObjeto++; // Conta o pixel para a área do objeto

                    // Olha os 8 vizinhos ao redor
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int nx = p.x + j, ny = p.y + i;
                            
                            if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visitado[nx][ny]) {
                                Color nc = new Color(img.getRGB(nx, ny));
                                
                                // O vizinho pertence ao objeto se tiver a MESMA classificação de cor
                                if (classificarCor(nc).equals(nomeCor)) {
                                    visitado[nx][ny] = true;
                                    fila.add(new Point(nx, ny));
                                }
                            }
                        }
                    }
                }

                // FILTRO DE RUÍDO: Só contabiliza o objeto se ele for grande o suficiente (ex: > 15 pixels)
                // Isso elimina os artefatos de compressão JPEG e os "fantasmas" amarelos
                if (tamanhoObjeto > 15) {
                    contagem.put(nomeCor, contagem.getOrDefault(nomeCor, 0) + 1);
                }
            }
        }

        if (contagem.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum objeto colorido válido encontrado.", "Resultado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder resultado = new StringBuilder("Objetos encontrados por cor:\n\n");
        for (java.util.Map.Entry<String, Integer> entry : contagem.entrySet()) {
            resultado.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        JOptionPane.showMessageDialog(this, resultado.toString(), "Contagem de Objetos", JOptionPane.INFORMATION_MESSAGE);
    }

    private String classificarCor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        // Adicionamos Branco e Preto para "puxar" os pixels de borda e ruído para fora das cores úteis
        Object[][] coresAlvo = {
            {"Branco", 255, 255, 255},
            {"Preto", 0, 0, 0},
            {"Vermelho", 255, 0, 0},
            {"Azul", 0, 0, 255},
            {"Verde", 0, 128, 0},
            {"Verde", 0, 255, 0},
            {"Amarelo", 255, 255, 0}
        };

        String corMaisProxima = "Branco";
        double menorDistancia = Double.MAX_VALUE;

        for (Object[] alvo : coresAlvo) {
            String nome = (String) alvo[0];
            int tr = (int) alvo[1];
            int tg = (int) alvo[2];
            int tb = (int) alvo[3];

            double distancia = Math.sqrt(Math.pow(r - tr, 2) + Math.pow(g - tg, 2) + Math.pow(b - tb, 2));

            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                corMaisProxima = nome;
            }
        }

        return corMaisProxima;
    }

    //Exercício 3: Letras do Alfabeto
    private void resolverLetras(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        
        BufferedImage cinza = grayCopy(img);
        boolean[][] visitado = new boolean[w][h];
        boolean[][] isPreto = new boolean[w][h];
        
        // 1. Binarização (Limiar = 128)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (new Color(cinza.getRGB(x, y)).getRed() < 128) {
                    isPreto[x][y] = true;
                }
            }
        }

        // TreeSet organiza automaticamente em ordem alfabética e impede letras duplicadas!
        java.util.Set<String> letrasEncontradas = new java.util.TreeSet<>(); 

        // 2. Encontrar e isolar cada letra na imagem
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (isPreto[x][y] && !visitado[x][y]) {
                    
                    int minX = x, maxX = x, minY = y, maxY = y;
                    int tamanhoObjeto = 0;
                    
                    java.util.Queue<Point> fila = new java.util.LinkedList<>();
                    fila.add(new Point(x, y));
                    visitado[x][y] = true;

                    // Flood Fill para achar os limites (Bounding Box) da letra
                    while (!fila.isEmpty()) {
                        Point p = fila.poll();
                        tamanhoObjeto++;
                        
                        if (p.x < minX) minX = p.x;
                        if (p.x > maxX) maxX = p.x;
                        if (p.y < minY) minY = p.y;
                        if (p.y > maxY) maxY = p.y;

                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                int nx = p.x + j, ny = p.y + i;
                                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                                    if (isPreto[nx][ny] && !visitado[nx][ny]) {
                                        visitado[nx][ny] = true;
                                        fila.add(new Point(nx, ny));
                                    }
                                }
                            }
                        }
                    }

                    // 3. Filtro de Ruído: ignora pontinhos soltos
                    if (tamanhoObjeto > 30) {
                        int largura = maxX - minX + 1;
                        int altura = maxY - minY + 1;
                        boolean[][] bbox = new boolean[largura][altura];
                        
                        // Recorta a letra exata
                        for (int by = 0; by < altura; by++) {
                            for (int bx = 0; bx < largura; bx++) {
                                bbox[bx][by] = isPreto[minX + bx][minY + by];
                            }
                        }
                        
                        // 4. Manda a matriz recortada para reconhecimento
                        String letra = classificarLetraZoning(bbox);
                        letrasEncontradas.add(letra);
                    }
                }
            }
        }

        // Exibe o resultado formatado
        if (letrasEncontradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma letra reconhecida.", "Resultado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String resultado = String.join(", ", letrasEncontradas);
            JOptionPane.showMessageDialog(this, "Letras detectadas na imagem:\n\n" + resultado, "Resultado OCR", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String classificarLetraZoning(boolean[][] bbox) {
        int w = bbox.length;
        int h = bbox[0].length;
        double[] grid = new double[9];

        // Se a letra for muito achatada ou muito esticada, ignora (provavelmente ruído)
        if (w == 0 || h == 0) return "?";

        // Calcula a quantidade de pixels pretos em cada uma das 9 zonas (3x3)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (bbox[x][y]) {
                    int zoneX = Math.min((x * 3) / w, 2);
                    int zoneY = Math.min((y * 3) / h, 2);
                    grid[zoneY * 3 + zoneX]++;
                }
            }
        }

        // Normaliza pela área de cada zona (transforma em porcentagem de 0.0 a 1.0)
        int zoneArea = Math.max(1, (w / 3) * (h / 3));
        for (int i = 0; i < 9; i++) {
            grid[i] = Math.min(1.0, grid[i] / zoneArea);
        }

        // Perfis Matemáticos das Letras (Mapeamento Heurístico da Grade 3x3)
        // Valores de 0.0 (totalmente vazio) a 1.0 (totalmente preenchido)
        Object[][] perfis = {
            {"A", new double[]{0.2, 0.9, 0.2,   0.8, 0.6, 0.8,   0.9, 0.2, 0.9}},
            {"B", new double[]{0.9, 0.8, 0.6,   0.9, 0.8, 0.6,   0.9, 0.8, 0.6}},
            {"C", new double[]{0.6, 0.9, 0.6,   0.9, 0.1, 0.1,   0.6, 0.9, 0.6}},
            {"X", new double[]{0.8, 0.2, 0.8,   0.2, 0.9, 0.2,   0.8, 0.2, 0.8}},
            {"Y", new double[]{0.9, 0.2, 0.9,   0.3, 0.9, 0.3,   0.1, 0.9, 0.1}},
            {"Z", new double[]{0.9, 0.9, 0.9,   0.2, 0.8, 0.2,   0.9, 0.9, 0.9}},
            {"M", new double[]{0.9, 0.4, 0.9,   0.9, 0.8, 0.9,   0.9, 0.1, 0.9}}
        };

        String melhorLetra = "?";
        double menorErro = Double.MAX_VALUE;

        // Compara o vetor extraído da imagem com todos os perfis usando Erro Quadrático
        for (Object[] perfil : perfis) {
            String nome = (String) perfil[0];
            double[] valores = (double[]) perfil[1];
            double erro = 0;
            
            for (int i = 0; i < 9; i++) {
                erro += Math.pow(grid[i] - valores[i], 2);
            }
            
            if (erro < menorErro) {
                menorErro = erro;
                melhorLetra = nome;
            }
        }
        
        return melhorLetra;
    }

    //Exercício 4: Placas de Trânsito
   private void resolverPlacas(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean[][] visitado = new boolean[w][h];
        
        // 1. Usar um Set para evitar múltiplas detecções da mesma placa (Filtra os "Pares" duplicados)
        java.util.Set<String> placasEncontradas = new java.util.LinkedHashSet<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (visitado[x][y]) continue;

                Color c = new Color(img.getRGB(x, y), true);
                
                // Condição para ser "Vermelho"
                if (c.getRed() > 120 && c.getRed() > c.getGreen() * 1.5 && c.getRed() > c.getBlue() * 1.5) {
                    
                    int minX = x, maxX = x, minY = y, maxY = y;
                    int tamanhoObjeto = 0;
                    
                    java.util.Queue<Point> fila = new java.util.LinkedList<>();
                    fila.add(new Point(x, y));
                    visitado[x][y] = true;

                    // Isola o objeto vermelho (Bounding Box)
                    while (!fila.isEmpty()) {
                        Point p = fila.poll();
                        tamanhoObjeto++;
                        
                        if (p.x < minX) minX = p.x;
                        if (p.x > maxX) maxX = p.x;
                        if (p.y < minY) minY = p.y;
                        if (p.y > maxY) maxY = p.y;

                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                int nx = p.x + j, ny = p.y + i;
                                if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visitado[nx][ny]) {
                                    Color nc = new Color(img.getRGB(nx, ny));
                                    if (nc.getRed() > 120 && nc.getRed() > nc.getGreen() * 1.5 && nc.getRed() > nc.getBlue() * 1.5) {
                                        visitado[nx][ny] = true;
                                        fila.add(new Point(nx, ny));
                                    }
                                }
                            }
                        }
                    }

                    // Ignora ruídos pequenos
                    if (tamanhoObjeto > 100) {
                        int largura = maxX - minX;
                        int altura = maxY - minY;
                        
                        int pixelsPretos = 0;
                        int vermelhoNoCentro = 0;
                        long somaXPretos = 0; // Para calcular o centro de gravidade do preto
                        
                        // Margem para olhar o "miolo" (30%)
                        int margemX = (int)(largura * 0.3);
                        int margemY = (int)(altura * 0.3);

                        // Variáveis para rastrear os limites exatos do desenho preto
                        int minPx = w, maxPx = 0, minPy = h, maxPy = 0;

                        for (int by = minY; by <= maxY; by++) {
                            for (int bx = minX; bx <= maxX; bx++) {
                                Color pixel = new Color(img.getRGB(bx, by));
                                
                                // Conta pixels pretos/escuros
                                if (pixel.getRed() < 80 && pixel.getGreen() < 80 && pixel.getBlue() < 80) {
                                    pixelsPretos++;
                                    
                                    // Atualiza a Bounding Box da cor preta
                                    if (bx < minPx) minPx = bx;
                                    if (bx > maxPx) maxPx = bx;
                                    if (by < minPy) minPy = by;
                                    if (by > maxPy) maxPy = by;
                                }
                                
                                // Conta vermelhos no quadrado central
                                if (bx >= minX + margemX && bx <= maxX - margemX && 
                                    by >= minY + margemY && by <= maxY - margemY) {
                                    
                                    if (pixel.getRed() > 150 && pixel.getGreen() < 100 && pixel.getBlue() < 100) {
                                        vermelhoNoCentro++;
                                    }
                                }
                            }
                        }

                        // 2. Classificação Lógica
                        String tipoPlaca;
                        
                        if (pixelsPretos < 20) {
                            tipoPlaca = "Pare";
                        } else {
                            if (vermelhoNoCentro > 30) {
                                // Tem faixa vermelha cortando! Pode ser Seta ou 'E'.
                                
                                // Calcula a largura e a altura APENAS da parte preta
                                int larguraPreto = maxPx - minPx;
                                int alturaPreto = maxPy - minPy;
                                
                                // A letra 'E' é gordinha (a largura passa tranquilamente de 40% da altura).
                                // A Seta é bem magrinha e alta.
                                if (larguraPreto > alturaPreto * 0.4) {
                                    tipoPlaca = "Proibido estacionar";
                                } else {
                                    tipoPlaca = "Sentido proibido";
                                }
                                
                            } else {
                                tipoPlaca = "Velocidade maxima";
                            }
                        }
                        
                        placasEncontradas.add(tipoPlaca);
                    }
                }
            }
        }

        if (placasEncontradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma placa vermelha detectada.", "Resultado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String resultado = String.join(", ", placasEncontradas);
            JOptionPane.showMessageDialog(this, "Placas detectadas: \n" + resultado, "Reconhecimento de Placas", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    //Exercício 5: Gráfico de Barras
    private void resolverGrafico(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        
        // ─── PASSO 1: ISOLAR AS BARRAS COLORIDAS ───
        boolean[][] visitadoBarras = new boolean[w][h];
        java.util.List<Rectangle> barras = new java.util.ArrayList<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (visitadoBarras[x][y]) continue;

                Color c = new Color(img.getRGB(x, y), true);
                if (c.getAlpha() == 0) continue;

                int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
                int maxC = Math.max(r, Math.max(g, b));
                int minC = Math.min(r, Math.min(g, b));

                // É colorido se a diferença entre a cor mais forte e a mais fraca for grande.
                // Isso ignora perfeitamente Branco, Preto e as Linhas Cinzas!
                if (maxC - minC > 30 && maxC > 100) {
                    int minX = x, maxX = x, minY = y, maxY = y;
                    int tamanhoObjeto = 0;
                    
                    java.util.Queue<Point> fila = new java.util.LinkedList<>();
                    fila.add(new Point(x, y));
                    visitadoBarras[x][y] = true;

                    while (!fila.isEmpty()) {
                        Point p = fila.poll();
                        tamanhoObjeto++;
                        if (p.x < minX) minX = p.x;
                        if (p.x > maxX) maxX = p.x;
                        if (p.y < minY) minY = p.y;
                        if (p.y > maxY) maxY = p.y;

                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                int nx = p.x + j, ny = p.y + i;
                                if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visitadoBarras[nx][ny]) {
                                    Color nc = new Color(img.getRGB(nx, ny));
                                    int nr = nc.getRed(), ng = nc.getGreen(), nb = nc.getBlue();
                                    int nMax = Math.max(nr, Math.max(ng, nb));
                                    int nMin = Math.min(nr, Math.min(ng, nb));
                                    
                                    if (nMax - nMin > 30 && nMax > 100) {
                                        visitadoBarras[nx][ny] = true;
                                        fila.add(new Point(nx, ny));
                                    }
                                }
                            }
                        }
                    }
                    
                    if (tamanhoObjeto > 200) {
                        barras.add(new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1));
                    }
                }
            }
        }

        // ─── PASSO 2: ISOLAR E LER OS NÚMEROS DO EIXO Y ───
        boolean[][] visitadoTexto = new boolean[w][h];
        
        class Digito { String val; int cx, cy; Digito(String v, int x, int y){val=v; cx=x; cy=y;} }
        java.util.List<Digito> digitos = new java.util.ArrayList<>();

        // Procuramos texto apenas na parte esquerda da imagem (x < w/3)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w / 3; x++) {
                if (visitadoTexto[x][y]) continue;
                Color c = new Color(img.getRGB(x, y));
                
                // Pixels pretos/escuros
                if (c.getRed() < 80 && c.getGreen() < 80 && c.getBlue() < 80) {
                    int minX = x, maxX = x, minY = y, maxY = y;
                    int tamanhoObjeto = 0;
                    
                    java.util.Queue<Point> fila = new java.util.LinkedList<>();
                    fila.add(new Point(x, y));
                    visitadoTexto[x][y] = true;

                    while (!fila.isEmpty()) {
                        Point p = fila.poll();
                        tamanhoObjeto++;
                        if (p.x < minX) minX = p.x;
                        if (p.x > maxX) maxX = p.x;
                        if (p.y < minY) minY = p.y;
                        if (p.y > maxY) maxY = p.y;

                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                int nx = p.x + j, ny = p.y + i;
                                if (nx >= 0 && nx < w/3 && ny >= 0 && ny < h && !visitadoTexto[nx][ny]) {
                                    Color nc = new Color(img.getRGB(nx, ny));
                                    if (nc.getRed() < 80 && nc.getGreen() < 80 && nc.getBlue() < 80) {
                                        visitadoTexto[nx][ny] = true;
                                        fila.add(new Point(nx, ny));
                                    }
                                }
                            }
                        }
                    }
                    
                    if (tamanhoObjeto > 10) {
                        boolean[][] bbox = extrairBBoxPreto(img, minX, maxX, minY, maxY);
                        String d = classificarDigitoZoning(bbox);
                        if (!d.isEmpty()) {
                            digitos.add(new Digito(d, minX + (maxX - minX)/2, minY + (maxY - minY)/2));
                        }
                    }
                }
            }
        }

        // ─── PASSO 3: AGRUPAR DÍGITOS PARA FORMAR OS NÚMEROS (Ex: "2" e "0" -> "20") ───
        class EixoY { int valor, cy; EixoY(int v, int y){valor=v; cy=y;} }
        java.util.List<EixoY> labelsEixo = new java.util.ArrayList<>();
        boolean[] agrupado = new boolean[digitos.size()];

        for (int i = 0; i < digitos.size(); i++) {
            if (agrupado[i]) continue;
            java.util.List<Digito> linha = new java.util.ArrayList<>();
            linha.add(digitos.get(i));
            agrupado[i] = true;
            
            for (int j = i + 1; j < digitos.size(); j++) {
                // Se os dígitos estiverem na mesma altura Y (tolerância de 15px)
                if (!agrupado[j] && Math.abs(digitos.get(i).cy - digitos.get(j).cy) < 15) {
                    linha.add(digitos.get(j));
                    agrupado[j] = true;
                }
            }
            
            // Ordena os dígitos da esquerda para a direita (X) e junta o texto
            linha.sort(java.util.Comparator.comparingInt(d -> d.cx));
            StringBuilder sb = new StringBuilder();
            int somaY = 0;
            for (Digito d : linha) {
                sb.append(d.val);
                somaY += d.cy;
            }
            try {
                int valor = Integer.parseInt(sb.toString());
                labelsEixo.add(new EixoY(valor, somaY / linha.size())); // Salva o Valor e sua posição Y média
            } catch (Exception ignored) {}
        }

        // ─── PASSO 4: CALCULAR A ESCALA MATEMÁTICA E OS VALORES ───
        if (barras.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma barra encontrada.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.List<Integer> valoresFinais = new java.util.ArrayList<>();

        if (labelsEixo.isEmpty()) {
            // Se o OCR falhar e não achar números, devolve pixels como fallback
            for (Rectangle b : barras) valoresFinais.add(b.height);
        } else {
            // Encontra o maior número do eixo (Ex: 20)
            EixoY maxLabel = labelsEixo.get(0);
            for (EixoY lbl : labelsEixo) {
                if (lbl.valor > maxLabel.valor) maxLabel = lbl;
            }
            
            // A linha do 0 (Base do gráfico) é a média da posição Y inferior das barras
            int somaBaseY = 0;
            for (Rectangle b : barras) somaBaseY += (b.y + b.height);
            int yZero = somaBaseY / barras.size();

            // Evita divisão por zero caso a leitura tenha sido imperfeita
            double pixelsPorUnidade = maxLabel.valor == 0 ? 1 : (yZero - maxLabel.cy) / (double) maxLabel.valor;
            if (pixelsPorUnidade <= 0) pixelsPorUnidade = 1;

            // Transforma o tamanho em pixels para o valor real baseado na escala
            for (Rectangle b : barras) {
                int valorReal = (int) Math.round((yZero - b.y) / pixelsPorUnidade);
                valoresFinais.add(valorReal);
            }
        }

        // ─── PASSO 5: EXIBIR O PADRÃO EXATO ───
        int max = java.util.Collections.max(valoresFinais);
        int min = java.util.Collections.min(valoresFinais);

        JOptionPane.showMessageDialog(this, 
            "Maior = " + max + " | Menor = " + min, 
            "Resultado do Grafico", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean[][] extrairBBoxPreto(BufferedImage img, int minX, int maxX, int minY, int maxY) {
        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        boolean[][] bbox = new boolean[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(minX + x, minY + y));
                bbox[x][y] = (c.getRed() < 80 && c.getGreen() < 80 && c.getBlue() < 80);
            }
        }
        return bbox;
    }

    private String classificarDigitoZoning(boolean[][] bbox) {
        int w = bbox.length;
        int h = bbox[0].length;
        if (w < 3 || h < 5) return ""; // Ignora pontinhos de ruído

        double[] grid = new double[9];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (bbox[x][y]) {
                    int zoneX = Math.min((x * 3) / w, 2);
                    int zoneY = Math.min((y * 3) / h, 2);
                    grid[zoneY * 3 + zoneX]++;
                }
            }
        }

        int zoneArea = Math.max(1, (w / 3) * (h / 3));
        for (int i = 0; i < 9; i++) grid[i] = Math.min(1.0, grid[i] / zoneArea);

        // Perfis matemáticos calibrados para os números: 0, 1, 2, 5 e 7
        Object[][] perfis = {
            {"0", new double[]{0.8, 0.9, 0.8,   0.9, 0.1, 0.9,   0.8, 0.9, 0.8}},
            {"1", new double[]{0.1, 0.9, 0.1,   0.1, 0.9, 0.1,   0.1, 0.9, 0.1}},
            {"2", new double[]{0.8, 0.9, 0.8,   0.1, 0.8, 0.8,   0.9, 0.9, 0.9}},
            {"5", new double[]{0.9, 0.9, 0.9,   0.9, 0.9, 0.1,   0.8, 0.9, 0.8}},
            {"7", new double[]{0.9, 0.9, 0.9,   0.1, 0.3, 0.9,   0.1, 0.1, 0.8}}
        };

        String melhor = "";
        double menorErro = Double.MAX_VALUE;
        for (Object[] perfil : perfis) {
            double[] val = (double[]) perfil[1];
            double erro = 0;
            for (int i = 0; i < 9; i++) erro += Math.pow(grid[i] - val[i], 2);
            if (erro < menorErro) { 
                menorErro = erro; 
                melhor = (String)perfil[0]; 
            }
        }
        
        // Limite de erro para não sair classificando tudo que parece mancha
        return menorErro < 1.5 ? melhor : "";
    }

    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProcessamentoImagens().setVisible(true));
    }
}
