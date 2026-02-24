import java.io.*;
import java.util.*;

/**
 * SnapConverter
 *
 * Converts a SNAP-style edgelist (lines "u v", optional comments starting with '#')
 * into the Digraph plain format expected by the project's `Digraph` class:
 *   V
 *   E
 *   u v
 *   ...
 *
 * Node ids in SNAP files may be arbitrary integers; this utility remaps them to
 * contiguous vertex ids [0..V-1].
 *
 * Usage:
 *   java -cp src SnapConverter input_snap.txt output_digraph.txt
 */
public class SnapConverter {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java -cp src SnapConverter <input_snap> <output_digraph>");
            System.exit(1);
        }
        String input = args[0];
        String output = args[1];

        // first pass: collect unique node ids preserving insertion order
        LinkedHashMap<Long, Integer> map = new LinkedHashMap<>();
        ArrayList<long[]> edges = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;
                try {
                    long u = Long.parseLong(parts[0]);
                    long v = Long.parseLong(parts[1]);
                    edges.add(new long[]{u, v});
                    if (!map.containsKey(u)) map.put(u, map.size());
                    if (!map.containsKey(v)) map.put(v, map.size());
                } catch (NumberFormatException nfe) {
                    // skip non-numeric lines
                }
            }
        }

        int V = map.size();
        int E = edges.size();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            bw.write(Integer.toString(V)); bw.newLine();
            bw.write(Integer.toString(E)); bw.newLine();
            // write remapped edges
            for (long[] e : edges) {
                int u = map.get(e[0]);
                int v = map.get(e[1]);
                bw.write(u + " " + v);
                bw.newLine();
            }
        }

        System.out.printf("Converted %s -> %s (V=%d, E=%d)\n", input, output, V, E);
    }
}
