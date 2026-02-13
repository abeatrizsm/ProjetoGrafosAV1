import java.util.NoSuchElementException;

public class Digraph {
    private static final String NEWLINE = System.getProperty("line.separator");
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
                s.append(String.format("%d ", w));
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

        int actions = 23;
        for (int v = 0; v < V; v++) {
            for (int u : adj[v]) {
                s.append((u - actions) + " -> " + v + NEWLINE);
            }
        }
        s.append("}" + NEWLINE);
        return s.toString();
    }

    public static void main(String[] args) {
        In in = new In("tinyDigraph.txt");
        Digraph graph = new Digraph(in);
        StdOut.println(graph);
        StdOut.println(graph.toDot());
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
