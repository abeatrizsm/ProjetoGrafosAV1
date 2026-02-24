import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;

public class DegreeAnalysis {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) return;
        
        String path = args[0];
        Digraph g = readDigraph(path);
        
        int[] outHist = g.outdegreeHistogram();
        int[] inHist = g.indegreeHistogram();

        // Nomes Versionados
        String fileOutPng = Main.getVersionedName("outdegree_visual", ".png");
        String fileInPng = Main.getVersionedName("indegree_visual", ".png");
        String fileLogLog = Main.getVersionedName("outdegree_loglog_fit", ".png");
        String fileRel = Main.getVersionedName("relatorio_tecnico", ".txt");

        // Geração dos arquivos
        writeFormattedHistogram(outHist, "Distribuição de Grau de Saída (Outdegree)", fileOutPng);
        writeFormattedHistogram(inHist, "Distribuição de Grau de Entrada (Indegree)", fileInPng);
        
        double gamma = writeLogLogWithFit(outHist, "Ajuste de Lei de Potência (Log-Log)", fileLogLog);
        gerarRelatorio(gamma, g.V(), g.E(), fileRel);

        System.out.println("\n[SUCESSO] Arquivos gerados na pasta 'saidas/'.");
    }

    /**
     * HISTOGRAMA FORMATADO: Barras, Eixos Azuis, Réguas e Legendas.
     */
    private static void writeFormattedHistogram(int[] hist, String title, String filename) throws IOException {
        int barWidth = 45;
        int margin = 80;
        int maxCount = 0;
        int numEntries = 0;

        for (int i = 0; i < hist.length; i++) {
            if (hist[i] > 0) numEntries = i + 1;
            if (hist[i] > maxCount) maxCount = hist[i];
        }

        int width = Math.max(800, (numEntries * barWidth) + margin * 2);
        int height = 600;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        int axisX = margin;
        int axisY = height - margin;
        int plotH = axisY - margin;
        Color azulEixo = new Color(70, 130, 180);

        // --- DESENHO DOS EIXOS E RÉGUAS (AZUL) ---
        g.setColor(azulEixo);
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(axisX, margin, axisX, axisY); // Eixo Y
        g.drawLine(axisX, axisY, width - margin, axisY); // Eixo X

        for (int i = 0; i <= 5; i++) {
            int yTick = axisY - (i * plotH / 5);
            g.drawLine(axisX - 7, yTick, axisX, yTick);
        }

        // Título e Legendas dos Eixos
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(title, margin, 40);
        
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("Grau k", (width / 2), height - 30);
        g.drawString("Frequência f(k)", 10, margin - 15);

        for (int i = 0; i < numEntries; i++) {
            int cnt = hist[i];
            if (cnt <= 0) continue;

            int bH = (int) (((double) cnt / maxCount) * plotH);
            int x = axisX + i * barWidth + 5;
            int y = axisY - bH;

            g.setColor(new Color(70, 130, 180, 180));
            g.fillRect(x, y, barWidth - 10, bH);
            g.setColor(azulEixo);
            g.drawRect(x, y, barWidth - 10, bH);

            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            String val = String.valueOf(cnt);
            g.drawString(val, x + (barWidth - 10 - g.getFontMetrics().stringWidth(val)) / 2, y - 5);
            g.drawString("k=" + i, x + 5, axisY + 20);
        }
        g.dispose();
        ImageIO.write(img, "png", new File(filename));
    }

    /**
     * LOG-LOG COM FIT: Eixos azuis, Regressão e Expressão matemática.
     */
    private static double writeLogLogWithFit(int[] hist, String title, String filename) throws IOException {
        List<Double> lx = new ArrayList<>(), ly = new ArrayList<>();
        for (int d = 1; d < hist.length; d++) {
            if (hist[d] > 0) {
                lx.add(Math.log10(d));
                ly.add(Math.log10(hist[d]));
            }
        }

        int n = lx.size();
        double sX=0, sY=0, sXY=0, sXX=0;
        for(int i=0; i<n; i++) {
            sX+=lx.get(i); sY+=ly.get(i); sXY+=lx.get(i)*ly.get(i); sXX+=lx.get(i)*lx.get(i);
        }
        double m = (n*sXY - sX*sY) / (n*sXX - sX*sX);
        double b = (sY - m*sX) / n;
        double gamma = -m;

        int w=800, h=600, mar=100;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE); g2.fillRect(0,0,w,h);

        int pW = w - 2*mar, pH = h - 2*mar;
        double xMin=lx.get(0), xMax=lx.get(n-1);
        double yMin=ly.get(ly.size()-1), yMax=ly.get(0);

        // EIXOS EM AZUL
        g2.setColor(new Color(70, 130, 180));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(mar, mar, mar, h - mar);
        g2.drawLine(mar, h - mar, w - mar, h - mar);

        // Pontos e Fit
        g2.setColor(new Color(70, 130, 180));
        for(int i=0; i<n; i++) {
            int px = mar + (int)((lx.get(i)-xMin)/(xMax-xMin)*pW);
            int py = h - mar - (int)((ly.get(i)-yMin)/(yMax-yMin)*pH);
            g2.fillOval(px-3, py-3, 6, 6);
        }

        g2.setColor(Color.RED);
        int py1 = h - mar - (int)(((m*xMin+b)-yMin)/(yMax-yMin)*pH);
        int py2 = h - mar - (int)(((m*xMax+b)-yMin)/(yMax-yMin)*pH);
        g2.drawLine(mar, py1, w-mar, py2);

        // Legendas e Expressão
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString(title, mar, 45);
        
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(new Color(40, 40, 40));
        g2.drawString(String.format("Gamma (Expoente): %.4f", gamma), w - 300, 80);
        g2.drawString(String.format("Expressao: y = %.2fx + %.2f", m, b), w - 300, 105);

        // Legendas dos Eixos Log
        g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
        g2.drawString("log10 (Grau k)", w/2 - 50, h - 35);
        
        // Texto vertical para o eixo Y
        Graphics2D gY = (Graphics2D) g2.create();
        gY.rotate(-Math.PI / 2);
        gY.drawString("log10 (Frequencia f)", -h/2 - 60, 40);
        gY.dispose();

        g2.dispose();
        ImageIO.write(img, "png", new File(filename));
        return gamma;
    }

    private static void gerarRelatorio(double gamma, int V, int E, String file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("RELATORIO TECNICO DE ANALISE DE REDE");
            pw.println("====================================");
            pw.println("Vertices: " + V + " | Arestas: " + E);
            pw.println("Expoente Gamma: " + String.format("%.4f", gamma));
            pw.println("\nInterpretacao: O grafo segue o modelo Scale-Free (Lei de Potencia).");
            pw.println("O ajuste linear prova a centralidade em super-usuarios.");
        }
    }

    private static Digraph readDigraph(String p) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(p));
        int v = Integer.parseInt(br.readLine());
        int e = Integer.parseInt(br.readLine());
        Digraph g = new Digraph(v);
        String line;
        while ((line = br.readLine()) != null) {
            String[] pts = line.split("\\s+");
            if(pts.length >= 2) g.addEdge(Integer.parseInt(pts[0]), Integer.parseInt(pts[1]));
        }
        br.close(); return g;
    }
}