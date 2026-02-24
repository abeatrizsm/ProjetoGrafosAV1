import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class DegreeAnalysis {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: java -cp src DegreeAnalysis <arquivo_digraph>");
            System.exit(1);
        }
        
        String path = args[0];
        Digraph g = readDigraph(path);
        int V = g.V();
        int E = g.E();
        
        System.out.println("------------------------------------------");
        System.out.printf("Análise de Grafo: V=%d, E=%d\n", V, E);

        // Cálculo de estatísticas
        long sumOut = 0, maxOut = 0;
        long sumIn = 0, maxIn = 0;
        for (int v = 0; v < V; v++) {
            int od = g.outdegree(v);
            int id = g.indegree(v);
            sumOut += od; if (od > maxOut) maxOut = od;
            sumIn += id; if (id > maxIn) maxIn = id;
        }
        
        System.out.printf("Grau de Saída (Outdegree) - Média: %.4f, Máx: %d\n", (double)sumOut/V, maxOut);
        System.out.printf("Grau de Entrada (Indegree) - Média: %.4f, Máx: %d\n", (double)sumIn/V, maxIn);

        // Obter histogramas
        int[] outHist = g.outdegreeHistogram();
        int[] inHist = g.indegreeHistogram();

        // Gerar nomes versionados usando a Main
        String fileOutTxt = Main.getVersionedName("outdegree_hist", ".txt");
        String fileInTxt  = Main.getVersionedName("indegree_hist", ".txt");
        String fileOutPng = Main.getVersionedName("outdegree_loglog", ".png");
        String fileInPng  = Main.getVersionedName("indegree_loglog", ".png");

        System.out.println("\nSalvando arquivos em 'saidas/':");
        
        // Salvar arquivos de texto
        writeHistFile(outHist, "outdegree histogram", fileOutTxt);
        writeHistFile(inHist, "indegree histogram", fileInTxt);
        System.out.println("  [TXT] " + fileOutTxt + " e " + fileInTxt);

        // Salvar gráficos Log-Log
        writeLogLogPlot(outHist, "Outdegree Distribution (Log-Log)", fileOutPng);
        writeLogLogPlot(inHist, "Indegree Distribution (Log-Log)", fileInPng);
        System.out.println("  [PNG] " + fileOutPng + " e " + fileInPng);

        // Mostrar os Top 10
        printTopK(g, 10);
        
        System.out.println("\nAnálise finalizada com sucesso.");
    }

    private static Digraph readDigraph(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        int V = Integer.parseInt(br.readLine());
        int E = Integer.parseInt(br.readLine());
        Digraph g = new Digraph(V);
        for (int i = 0; i < E; i++) {
            String line = br.readLine();
            if (line == null) break;
            String[] parts = line.split("\\s+");
            if (parts.length < 2) continue;
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            g.addEdge(u, v);
        }
        br.close();
        return g;
    }

    private static void writeHistFile(int[] hist, String title, String filename) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write(title); bw.newLine();
            for (int d = 0; d < hist.length; d++) {
                if (hist[d] > 0) {
                    bw.write(d + " " + hist[d]); bw.newLine();
                }
            }
        }
    }

    private static void writeLogLogPlot(int[] hist, String title, String filename) throws IOException {
        int n = 0;
        for (int c : hist) if (c > 0) n++;
        if (n == 0) return;

        double[] xs = new double[n];
        double[] ys = new double[n];
        int idx = 0;
        for (int d = 0; d < hist.length; d++) {
            int c = hist[d];
            if (c <= 0) continue;
            xs[idx] = Math.log10(Math.max(1, d)); // log do grau
            ys[idx] = Math.log10(c);              // log da frequência
            idx++;
        }

        int width = 800, height = 600;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // Cálculo de limites para escala
        double xmin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < xs.length; i++) {
            if (xs[i] < xmin) xmin = xs[i];
            if (xs[i] > xmax) xmax = xs[i];
            if (ys[i] < ymin) ymin = ys[i];
            if (ys[i] > ymax) ymax = ys[i];
        }
        if (xmin == xmax) { xmin -= 1; xmax += 1; }
        if (ymin == ymax) { ymin -= 1; ymax += 1; }

        int margin = 80;
        int plotW = width - margin * 2;
        int plotH = height - margin * 2;

        // Desenhar Eixos
        g.setColor(Color.BLACK);
        g.drawLine(margin, margin, margin, margin + plotH);
        g.drawLine(margin, margin + plotH, margin + plotW, margin + plotH);

        // Desenhar Pontos
        g.setColor(new Color(70, 130, 180)); // SteelBlue
        for (int i = 0; i < xs.length; i++) {
            double nx = (xs[i] - xmin) / (xmax - xmin);
            double ny = (ys[i] - ymin) / (ymax - ymin);
            int px = margin + (int) (nx * plotW);
            int py = margin + plotH - (int) (ny * plotH);
            g.fillOval(px - 3, py - 3, 6, 6);
        }

        // Títulos e Legendas
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(title, (width - g.getFontMetrics().stringWidth(title)) / 2, 40);
        
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("log10(Grau)", margin + plotW / 2 - 40, height - 25);
        g.drawString("log10(Freq)", 10, margin + plotH / 2);

        g.dispose();
        ImageIO.write(img, "png", new File(filename));
    }

    private static void printTopK(Digraph g, int k) {
        int V = g.V();
        List<int[]> outlist = new ArrayList<>();
        List<int[]> inlist = new ArrayList<>();
        for (int v = 0; v < V; v++) {
            outlist.add(new int[]{v, g.outdegree(v)});
            inlist.add(new int[]{v, g.indegree(v)});
        }
        outlist.sort((a,b) -> Integer.compare(b[1], a[1]));
        inlist.sort((a,b) -> Integer.compare(b[1], a[1]));

        System.out.println("\n--- Top " + k + " por Grau de Saída (Outdegree) ---");
        for (int i = 0; i < Math.min(k, outlist.size()); i++) {
            System.out.printf("  Nó %d: %d\n", outlist.get(i)[0], outlist.get(i)[1]);
        }

        System.out.println("\n--- Top " + k + " por Grau de Entrada (Indegree) ---");
        for (int i = 0; i < Math.min(k, inlist.size()); i++) {
            System.out.printf("  Nó %d: %d\n", inlist.get(i)[0], inlist.get(i)[1]);
        }
    }
}