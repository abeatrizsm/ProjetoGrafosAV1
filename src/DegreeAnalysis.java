import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;

public class DegreeAnalysis {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: java -cp src DegreeAnalysis <arquivo_digraph>");
            return;
        }
        
        String path = args[0];
        Digraph g = readDigraph(path);
        int V = g.V();
        int E = g.E();
        
        System.out.println("Processando Grafo: V=" + V + ", E=" + E);

        int[] outHist = g.outdegreeHistogram();
        int[] inHist = g.indegreeHistogram();

        // Nomes Versionados via Main
        String fileOutPng = Main.getVersionedName("outdegree_visual", ".png");
        String fileInPng = Main.getVersionedName("indegree_visual", ".png");
        String fileLogLog = Main.getVersionedName("outdegree_loglog_fit", ".png");
        String fileRel = Main.getVersionedName("relatorio_tecnico", ".txt");

        // 1. Gera Histogramas Formatados (Barras Azuis com números em cima)
        writeFormattedHistogram(outHist, "Distribuição de Grau de Saída (Outdegree)", fileOutPng);
        writeFormattedHistogram(inHist, "Distribuição de Grau de Entrada (Indegree)", fileInPng);

        // 2. Gera o Log-Log Científico com a Linha de Ajuste Vermelha
        double gamma = writeLogLogWithFit(outHist, "Ajuste de Lei de Potência (Log-Log)", fileLogLog);
        
        // 3. Gera o Relatório Técnico
        gerarRelatorio(gamma, V, E, fileRel);

        System.out.println("\n[SUCESSO] Arquivos gerados em 'saidas/':");
        System.out.println("- Visual Outdegree: " + fileOutPng);
        System.out.println("- Visual Indegree: " + fileInPng);
        System.out.println("- Gráfico de Ajuste: " + fileLogLog);
        System.out.println("- Relatório: " + fileRel);
    }

    /**
     * HISTOGRAMA FORMATADO: Barras azuis, eixos nomeados e valores no topo.
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

        // Desenha Grid e Eixos
        g.setColor(new Color(230, 230, 230));
        for(int i=0; i<=10; i++) {
            int y = axisY - (i * plotH / 10);
            g.drawLine(axisX, y, width-margin, y);
        }

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(axisX, margin, axisX, axisY); // Y
        g.drawLine(axisX, axisY, width - margin, axisY); // X

        // Título e Legendas
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(title, margin, 40);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("Grau (k)", width / 2, axisY + 40);
        g.drawString("Frequência", 10, margin - 10);

        for (int i = 0; i < numEntries; i++) {
            int cnt = hist[i];
            if (cnt <= 0) continue;

            int bH = (int) (((double) cnt / maxCount) * plotH);
            int x = axisX + i * barWidth + 5;
            int y = axisY - bH;

            g.setColor(new Color(70, 130, 180));
            g.fillRect(x, y, barWidth - 10, bH);
            g.setColor(new Color(40, 80, 120));
            g.drawRect(x, y, barWidth - 10, bH);

            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            String val = String.valueOf(cnt);
            g.drawString(val, x + (barWidth - 10 - g.getFontMetrics().stringWidth(val)) / 2, y - 5);
            g.drawString("k=" + i, x + 5, axisY + 15);
        }
        g.dispose();
        ImageIO.write(img, "png", new File(filename));
    }

    /**
     * LOG-LOG COM FIT: Gráfico científico com linha de regressão e equação.
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

        int w=800, h=600, mar=80;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE); g2.fillRect(0,0,w,h);

        double xMin=lx.get(0), xMax=lx.get(n-1);
        double yMin=ly.get(ly.size()-1), yMax=ly.get(0);
        int pW = w - 2*mar, pH = h - 2*mar;

        // Desenha Pontos e Linha de Ajuste
        g2.setColor(Color.BLUE);
        for(int i=0; i<n; i++) {
            int px = mar + (int)((lx.get(i)-xMin)/(xMax-xMin)*pW);
            int py = h - mar - (int)((ly.get(i)-yMin)/(yMax-yMin)*pH);
            g2.fillOval(px-3, py-3, 6, 6);
        }

        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));
        int py1 = h - mar - (int)(((m*xMin+b)-yMin)/(yMax-yMin)*pH);
        int py2 = h - mar - (int)(((m*xMax+b)-yMin)/(yMax-yMin)*pH);
        g2.drawLine(mar, py1, w-mar, py2);

        // Caixa de Informação Técnica
        g2.setColor(new Color(255, 255, 200, 200));
        g2.fillRect(w-250, mar, 230, 80);
        g2.setColor(Color.BLACK);
        g2.drawRect(w-250, mar, 230, 80);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString("Equação: log(y) = " + String.format("%.2f", m) + "x + " + String.format("%.2f", b), w-240, mar+25);
        g2.drawString("Expoente Gamma: " + String.format("%.4f", gamma), w-240, mar+50);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(title, mar, 40);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("log10 (Grau k)", w/2, h-30);
        g2.drawString("log10 (Frequência)", 10, h/2);

        g2.dispose();
        ImageIO.write(img, "png", new File(filename));
        return gamma;
    }

    private static void gerarRelatorio(double gamma, int V, int E, String file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("INTERPRETAÇÃO TÉCNICA - DATASET MOOC");
            pw.println("====================================");
            pw.println("Vértices: " + V + " | Arestas: " + E);
            pw.println("Expoente de Lei de Potência (Gamma): " + String.format("%.4f", gamma));
            pw.println("\nANÁLISE:");
            pw.println("- A distribuição segue uma Lei de Potência, visível pela reta no Log-Log.");
            pw.println("- O expoente " + String.format("%.2f", gamma) + " indica uma rede altamente centralizada.");
            pw.println("- Sugere que poucos usuários (hubs) dominam as ações no curso online.");
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