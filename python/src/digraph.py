"""
   Execution:    python digraph.py input.txt
   Dependencies: Bag.java Stack.java In.java StdOut.java
   Data files:   https://algs4.cs.princeton.edu/41graph/tinyDG.txt
                 https://algs4.cs.princeton.edu/41graph/mediumDG.txt
                 https://algs4.cs.princeton.edu/41graph/largeDG.txt
 
   A graph, implemented using an array of sets.
   Parallel edges and self-loops are permitted.
 
   % python digraph.py < tinyDG.txt
   13 vertices, 22 edges
   0: 5 1 
   1: 
   2: 0 3 
   3: 5 2 
   4: 3 2 
   5: 4 
   6: 9 4 8 0 
   7: 6 9
   8: 6 
   9: 11 10 
   10: 12 
   11: 4 12 
   12: 9 
 
 """
from bag import Bag


class Digraph:

    def __init__(self, v=0, **kwargs):
        self.V = v
        self.E = 0
        self.adj = {}
        self.indegree = [0] * (self.V if self.V > 0 else 0)
        for v in range(self.V):
            self.adj[v] = Bag()

        if 'file' in kwargs:
            # init a digraph by a file input
            in_file = kwargs['file']
            self.V = int(in_file.readline())
            for v in range(self.V):
                self.adj[v] = Bag()
            E = int(in_file.readline())
            for i in range(E):
                v, w = in_file.readline().split()
                self.add_edge(int(v), int(w))

    def __str__(self):
        users = 2
        s = "%d vertices, %d edges\n" % (self.V, self.E)
        s += "\n".join("%d: %s" % (v, " ".join(str(w - users)
                                               for w in self.adj[v])) for v in range(self.V))
        return s

    def add_edge(self, v, w):
        v, w = int(v), int(w)
        self.adj[v].add(w)
        # update indegree and edge count
        # ensure indegree list is large enough
        if len(self.indegree) < self.V:
            self.indegree = self.indegree + [0] * (self.V - len(self.indegree))
        self.indegree[w] += 1
        self.E += 1

    def degree(self, v):
        return len(self.adj[v])

    def max_degree(self):
        max_deg = 0
        for v in range(self.V):
            max_deg = max(max_deg, self.degree(v))
        return max_deg

    def number_of_self_loops(self):
        count = 0
        for v in range(self.V):
            for w in self.adj[v]:
                if w == v:
                    count += 1
        return count

    def reverse(self):
        R = Digraph(self.V)
        v = 0
        while v < self.V:
            for w in self.adj[v]:
                R.add_edge(w, v)
            v += 1
        return R

    def to_dot(self):
        users = 2
        lines = []
        lines.append("digraph {")
        lines.append('node[shape=circle, style=filled, fixedsize=true, width=0.3, fontsize="10pt"]')
        lines.append('edge[arrowhead=normal]')
        for v in range(self.V):
            for w in self.adj[v]:
                lines.append(f"{v} -> {w - users}")
        lines.append("}")
        return "\n".join(lines) + "\n"

    def histograma(self):
        # print indegree for each vertex
        for i in range(self.V):
            print(self.indegree[i] if i < len(self.indegree) else 0)

if __name__ == '__main__':
    import sys

    if len(sys.argv) != 2:
        print("Uso: python digraph.py <arquivo.txt>")
        sys.exit(1)

    caminho = sys.argv[1]

    with open(caminho, 'r', encoding='utf-8') as f:
        V = int(f.readline())
        E = int(f.readline())
        g = Digraph(V)

        for _ in range(E):
            linha = f.readline().split()
            v, w = int(linha[0]), int(linha[1])
            g.add_edge(v, w)

    print(g)
    print(g.to_dot())
    g.histograma()