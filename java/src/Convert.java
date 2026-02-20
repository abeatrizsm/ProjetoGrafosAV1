import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Convert {
    private static final int USUARIOS = 7047;
    private static final int VERTICES = 7144;

    public static void main(String[] args){
       String entrada =  "mooc_actions.tsv";
       String saida = "digraph.txt";

       List<int[]> arestas = new ArrayList<>();

       try {
           FileReader fr = new FileReader(entrada);
           FileWriter fw = new FileWriter(saida);
           BufferedReader br = new BufferedReader(fr);
           BufferedWriter bw = new BufferedWriter(fw);
           String linha = br.readLine();

           while ((linha = br.readLine()) != null){
               if (!linha.trim().isEmpty()){
                   String[] partes = linha.split("\t");

                   int usuario = Integer.parseInt(partes[1]);
                   int alvo = Integer.parseInt(partes[2]) + USUARIOS;

                   arestas.add(new int[]{usuario, alvo});
               }
           }

           bw.write(VERTICES + "\n");
           bw.write(arestas.size() + "\n");

           for (int i = 0; i < arestas.size(); i++) {
               bw.write(arestas.get(i)[0] + " " + arestas.get(i)[1]);
               bw.newLine();
           }
       } catch (Exception e) {
           e.printStackTrace();
           return;
       }
        System.out.println("Teste");
    }
}
