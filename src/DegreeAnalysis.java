import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class DegreeAnalysis {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java -cp src DegreeAnalysis <digraph_file>");
            System.exit(1);
        }
        String path = args[0];
        Digraph g = readDigraph(path);
        int V = g.V();
        int E = g.E();
        System.out.printf("V=%d, E=%d\n", V, E);

        long sumOut = 0, maxOut = 0;
        long sumIn = 0, maxIn = 0;
        for (int v = 0; v < V; v++) {
            int od = g.outdegree(v);
            int id = g.indegree(v);
            sumOut += od; if (od > maxOut) maxOut = od;
            sumIn += id; if (id > maxIn) maxIn = id;
        }
        double avgOut = V == 0 ? 0 : (double) sumOut / V;
        double avgIn = V == 0 ? 0 : (double) sumIn / V;
        System.out.printf("avg outdegree=%.4f, max outdegree=%d\n", avgOut, maxOut);
        System.out.printf("avg indegree=%.4f, max indegree=%d\n", avgIn, maxIn);

        int[] outHist = g.outdegreeHistogram();
        int[] inHist = g.indegreeHistogram();

        writeHistFile(outHist, "outdegree histogram", "outdegree_hist.txt");
        writeHistFile(inHist, "indegree histogram", "indegree_hist.txt");
        writeLogLogPlot(outHist, "outdegree (log-log)", "outdegree_loglog.png");
        writeLogLogPlot(inHist, "indegree (log-log)", "indegree_loglog.png");
        printTopK(g, 10);
        System.out.println("\nDone.");
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
            xs[idx] = Math.log10(Math.max(1, d));
            ys[idx] = Math.log10(c);
            idx++;
        }

        int width = 800, height = 600;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        Font titleFont = g.getFont().deriveFont(Font.BOLD, 16f);
        g.setFont(titleFont);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(title);
        g.drawString(title, (width - tw) / 2, 30);

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
        g.drawLine(margin, margin, margin, margin + plotH);
        g.drawLine(margin, margin + plotH, margin + plotW, margin + plotH);

        g.setColor(Color.BLUE);
        for (int i = 0; i < xs.length; i++) {
            double nx = (xs[i] - xmin) / (xmax - xmin);
            double ny = (ys[i] - ymin) / (ymax - ymin);
            int px = margin + (int) (nx * plotW);
            int py = margin + plotH - (int) (ny * plotH);
            g.fillOval(px - 3, py - 3, 6, 6);
        }

        g.setColor(Color.BLACK);
        Font labelFont = g.getFont().deriveFont(Font.PLAIN, 12f);
        g.setFont(labelFont);
        g.drawString("log10(degree)", margin + plotW / 2 - 30, height - 20);
        g.drawString("log10(freq)", 10, margin + plotH / 2);
        g.dispose();
        ImageIO.write(img, "png", new File(filename));
    }

    private static void printTopK(Digraph g, int k) {
        int V = g.V();
        java.util.List<int[]> outlist = new java.util.ArrayList<>();
        java.util.List<int[]> inlist = new java.util.ArrayList<>();
        for (int v = 0; v < V; v++) {
            outlist.add(new int[]{v, g.outdegree(v)});
            inlist.add(new int[]{v, g.indegree(v)});
        }
        outlist.sort((a,b) -> Integer.compare(b[1], a[1]));
        inlist.sort((a,b) -> Integer.compare(b[1], a[1]));
        System.out.println("\nTop " + k + " by outdegree:");
        for (int i = 0; i < Math.min(k, outlist.size()); i++) {
            int[] p = outlist.get(i);
            System.out.printf("  %d: %d\n", p[0], p[1]);
        }
        System.out.println("Top " + k + " by indegree:");
        for (int i = 0; i < Math.min(k, inlist.size()); i++) {
            int[] p = inlist.get(i);
            System.out.printf("  %d: %d\n", p[0], p[1]);
        }
    }
}
