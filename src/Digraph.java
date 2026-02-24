import java.util.NoSuchElementException;
import java.io.IOException;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Digraph {
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final int USERS_TINY = 2;
    private static final int USERS_TOTAL = 7144;

    private final int V;           // number of vertices in this digraph
    private int E;                 // number of edges in this digraph
    private Bag<Integer>[] adj;    // adj[v] = adjacency list for vertex v
    private int[] indegree;        // indegree[v] = indegree of vertex v

    public Digraph(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices in a Digraph must be non-negative");
        this.V = V;
        this.E = 0;
        indegree = new int[V];
        adj = (Bag<Integer>[]) new Bag[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new Bag<Integer>();
        }
    }

    public Digraph(In in) {
        if (in == null) throw new IllegalArgumentException("argument is null");
        try {
            this.V = in.readInt();
            if (V < 0) throw new IllegalArgumentException("number of vertices in a Digraph must be non-negative");
            indegree = new int[V];
            adj = (Bag<Integer>[]) new Bag[V];
            for (int v = 0; v < V; v++) {
                adj[v] = new Bag<Integer>();
            }
            int E = in.readInt();
            if (E < 0) throw new IllegalArgumentException("number of edges in a Digraph must be non-negative");
            for (int i = 0; i < E; i++) {
                int v = in.readInt();
                int w = in.readInt();
                addEdge(v, w);
            }
        }
        catch (NoSuchElementException e) {
            throw new IllegalArgumentException("invalid input format in Digraph constructor", e);
        }
    }

    public Digraph(Digraph digraph) {
        if (digraph == null) throw new IllegalArgumentException("argument is null");

        this.V = digraph.V();
        this.E = digraph.E();
        if (V < 0) throw new IllegalArgumentException("Number of vertices in a Digraph must be non-negative");

        // update indegrees
        indegree = new int[V];
        for (int v = 0; v < V; v++)
            this.indegree[v] = digraph.indegree(v);

        // update adjacency lists
        adj = (Bag<Integer>[]) new Bag[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new Bag<Integer>();
        }

        for (int v = 0; v < digraph.V(); v++) {
            // reverse so that adjacency list is in same order as original
            Stack<Integer> reverse = new Stack<Integer>();
            for (int w : digraph.adj[v]) {
                reverse.push(w);
            }
            for (int w : reverse) {
                adj[v].add(w);
            }
        }
    }

    public int V() {
        return V;
    }

    public int E() {
        return E;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    public void addEdge(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        adj[v].add(w);
        indegree[w]++;
        E++;
    }

    public Iterable<Integer> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    public int outdegree(int v) {
        validateVertex(v);
        return adj[v].size();
    }

    public int indegree(int v) {
        validateVertex(v);
        return indegree[v];
    }

    public Digraph reverse() {
        Digraph reverse = new Digraph(V);
        for (int v = 0; v < V; v++) {
            for (int w : adj(v)) {
                reverse.addEdge(w, v);
            }
        }
        return reverse;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(V + " vertices, " + E + " edges " + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(String.format("%d: ", v));
            for (int w : adj[v]) {
                s.append(String.format("%d ", (w - USERS_TINY)));
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    /**
     * Returns a string representation of this digraph in DOT format,
     * suitable for visualization with Graphviz.
     *
     * To visualize the digraph, install Graphviz (e.g., "brew install graphviz").
     * Then use one of the graph visualization tools
     *    - dot    (hierarchical or layer drawing)
     *    - neato  (spring model)
     *    - fdp    (force-directed placement)
     *    - sfdp   (scalable force-directed placement)
     *    - twopi  (radial layout)
     *
     * For example, the following commands will create graph drawings in SVG
     * and PDF formats
     *    - dot input.dot -Tsvg -o output.svg
     *    - dot input.dot -Tpdf -o output.pdf
     *
     * To change the digraph attributes (e.g., vertex and edge shapes, arrows, colors)
     *  in the DOT format, see https://graphviz.org/doc/info/lang.html
     *
     * @return a string representation of this digraph in DOT format
     */
    public String toDot() {
        StringBuilder s = new StringBuilder();
        s.append("digraph {" + NEWLINE);
        s.append("node[shape=circle, style=filled, fixedsize=true, width=0.3, fontsize=\"10pt\"]" + NEWLINE);
        s.append("edge[arrowhead=normal]" + NEWLINE);
        for (int v = 0; v < V; v++) {
            for (int w : adj[v]) {
                s.append(v + " -> " + (w - USERS_TINY) + NEWLINE);
            }
        }
        s.append("}" + NEWLINE);
        return s.toString();
    }

    /**
     * Returns an array H where H[d] is the number of vertices with outdegree d.
     * This can be used to build a histogram of the degree distribution.
     */
    public int[] outdegreeHistogram() {
        int max = 0;
        for (int v = 0; v < V; v++) {
            int d = outdegree(v);
            if (d > max) max = d;
        }
        int[] hist = new int[max + 1];
        for (int v = 0; v < V; v++) {
            hist[outdegree(v)]++;
        }
        return hist;
    }

    /**
     * Print an ASCII histogram of outdegree frequencies. Each star represents one vertex.
     * If counts are large the bars may be truncated or scaled manually by caller.
     */
    // common helper to render a more visual histogram with scaling
    private void printHistogram(int[] hist, String title) {
        StdOut.println(title);
        int maxCount = 0;
        for (int cnt : hist) if (cnt > maxCount) maxCount = cnt;
        if (maxCount == 0) return;
        int maxBarWidth = 50;           // adjust for terminal width
        for (int d = 0; d < hist.length; d++) {
            int cnt = hist[d];
            if (cnt == 0) continue;
            int barLen = (int) ((double) cnt / maxCount * maxBarWidth);
            if (barLen < 1) barLen = 1;
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLen; i++) bar.append('#');
            StdOut.printf("%2d (%3d) | %s%n", d, cnt, bar);
        }
    }

    public void printOutdegreeHistogram() {
        printHistogram(outdegreeHistogram(), "outdegree histogram");
    }

    /**
     * Returns an array H where H[d] is the number of vertices with indegree d.
     */
    public int[] indegreeHistogram() {
        int max = 0;
        for (int v = 0; v < V; v++) {
            int d = indegree(v);
            if (d > max) max = d;
        }
        int[] hist = new int[max + 1];
        for (int v = 0; v < V; v++) {
            hist[indegree(v)]++;
        }
        return hist;
    }

    /**
     * Prints an ASCII histogram for indegrees.
     */
    public void printIndegreeHistogram() {
        printHistogram(indegreeHistogram(), "indegree histogram");
    }

    /**
     * Write histogram data to a text file. Each line contains "degree count".
     * The title string is written at the top for context.
     */
    private static void writeHistogramToFile(int[] hist, String title, String filename) throws IOException {
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(filename))) {
            bw.write(title);
            bw.newLine();
            for (int d = 0; d < hist.length; d++) {
                if (hist[d] > 0) {
                    bw.write(d + " " + hist[d]);
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Draws a simple bar chart and writes to a PNG file.
     */
    private static void writeHistogramImage(int[] hist, String title, String filename) throws IOException {
        int barWidth = 40;           // wider bars for readability
        int margin = 60;
        int width = hist.length * barWidth + margin * 2;
        int maxCount = 0;
        for (int cnt : hist) if (cnt > maxCount) maxCount = cnt;
        int height = (maxCount * 8) + margin * 2; // taller image for counts
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        // enable antialiasing for better text/bars
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // title centered (translated for Portuguese)
        g.setColor(Color.BLACK);
        Font titleFont = g.getFont().deriveFont(Font.BOLD, 18f);
        g.setFont(titleFont);
        FontMetrics fm = g.getFontMetrics();
        String portTitle = title.replace("outdegree", "grau de saída").replace("indegree", "grau de entrada");
        int titleWidth = fm.stringWidth(portTitle);
        g.drawString(portTitle, (width - titleWidth) / 2, margin / 2 + fm.getAscent()/2);

        // axes
        int axisX = margin;
        int axisY = height - margin;
        g.drawLine(axisX, margin, axisX, axisY); // Y axis
        g.drawLine(axisX, axisY, width - margin, axisY); // X axis
        // labels in Portuguese
        Font labelFont = g.getFont().deriveFont(Font.PLAIN, 14f);
        g.setFont(labelFont);
        g.drawString("grau", width/2 - 20, height - margin + 40);
        g.drawString("frequência", margin - 40, margin - 10);

        // draw bars and numbers
        for (int d = 0; d < hist.length; d++) {
            int cnt = hist[d];
            int barHeight = (maxCount == 0) ? 0 : (cnt * (axisY - margin) / maxCount);
            int x = axisX + d * barWidth + 5;
            int y = axisY - barHeight;
            g.setColor(Color.BLUE);
            g.fillRect(x, y, barWidth - 10, barHeight);
            g.setColor(Color.BLACK);
            // draw degree label centered under bar
            String deg = String.valueOf(d);
            FontMetrics fm2 = g.getFontMetrics();
            int degW = fm2.stringWidth(deg);
            g.drawString(deg, x + ((barWidth-10) - degW)/2, axisY + fm2.getAscent() + 2);
            // draw count above bar
            String cnts = String.valueOf(cnt);
            int cntW = fm2.stringWidth(cnts);
            g.drawString(cnts, x + ((barWidth-10) - cntW)/2, y - 5);
        }

        g.dispose();
        ImageIO.write(img, "png", new java.io.File(filename));
    }

    public static void main(String[] args) {
        try {
            // read a graph from file (default to tinyDigraph.txt if no argument)
            String filename = args.length > 0 ? args[0] : "tinyDigraph.txt";
            In in = new In(filename);
            Digraph graph = new Digraph(in);

            // basic dump and dot representation
            StdOut.println(graph);
            StdOut.println(graph.toDot());

            // print a visible histogram of the outdegree distribution
            graph.printOutdegreeHistogram();
            // also show indegree frequencies
            graph.printIndegreeHistogram();

            // write histograms to disk for later inspection
            writeHistogramToFile(graph.outdegreeHistogram(), "outdegree histogram", "outdegree_hist.txt");
            writeHistogramToFile(graph.indegreeHistogram(), "indegree histogram", "indegree_hist.txt");
            writeHistogramImage(graph.outdegreeHistogram(), "outdegree histogram", "outdegree_hist.png");
            writeHistogramImage(graph.indegreeHistogram(), "indegree histogram", "indegree_hist.png");
            StdOut.println("Histograms written to outdegree_hist.txt, indegree_hist.txt and PNG images");
        } catch (IOException ioe) {
            StdOut.println("error writing histogram file: " + ioe.getMessage());
        }
    }

}

/******************************************************************************
 *  Copyright 2002-2025, Robert Sedgewick and Kevin Wayne.
 *
 *  This file is part of algs4.jar, which accompanies the textbook
 *
 *      Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 *      Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 *      http://algs4.cs.princeton.edu
 *
 *
 *  algs4.jar is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  algs4.jar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/
