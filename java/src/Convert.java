import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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


           // we'll also collect frequency of actions per user
           Map<Integer,Integer> freq = new java.util.HashMap<>();

           while ((linha = br.readLine()) != null){
               if (linha.trim().isEmpty()){
                   continue;
               }
               String[] partes = linha.split("\t");

               int usuario = Integer.parseInt(partes[1]);
               int alvo = Integer.parseInt(partes[2]) + USUARIOS;

               arestas.add(new int[]{usuario, alvo});

               // update histogram map
               freq.put(usuario, freq.getOrDefault(usuario, 0) + 1);
           }

           // after writing the digraph, print action histogram
           bw.write(VERTICES + "\n");
           bw.write(arestas.size() + "\n");

           for (int i = 0; i < arestas.size(); i++) {
               bw.write(arestas.get(i)[0] + " " + arestas.get(i)[1]);
               bw.newLine();
           }

           System.out.println("histograma de ações por usuário (ordenado por frequência desc):");
           // create a list of entries so we can sort by count
           java.util.List<java.util.Map.Entry<Integer,Integer>> list =
                   new java.util.ArrayList<>(freq.entrySet());
           list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
           for (java.util.Map.Entry<Integer,Integer> e : list) {
               System.out.printf("%d -> %d%n", e.getKey(), e.getValue());
           }

           // optionally produce an image of this histogram as well
           writeUserHistogramImage(list, "histograma de ações por usuário", "user_hist.png");
           System.out.println("Imagem do histograma de usuários gravada em user_hist.png");
            
           // note: writing the graph was already done above; the printout
           // occurs after we build the map.
           // write image of user histogram
           writeUserHistogramImage(list, "histograma de ações por usuário", "user_hist.png");
           System.out.println("Imagem do histograma de usuários gravada em user_hist.png");
       } catch (Exception e) {
           e.printStackTrace();
           return;
       }
        System.out.println("Teste");
    }

    /**
     * Draws a simple bar chart for user-action frequencies. List is sorted by value.
     */
    private static void writeUserHistogramImage(java.util.List<java.util.Map.Entry<Integer,Integer>> list,
                                                String title, String filename) throws java.io.IOException {
        int barWidth = 40;
        int margin = 60;
        int width = list.size() * barWidth + margin * 2;
        int maxCount = 0;
        for (java.util.Map.Entry<Integer,Integer> e : list) {
            if (e.getValue() > maxCount) maxCount = e.getValue();
        }
        int height = (maxCount * 8) + margin * 2;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(java.awt.Color.BLACK);
        java.awt.Font titleFont = g.getFont().deriveFont(java.awt.Font.BOLD, 18f);
        g.setFont(titleFont);
        java.awt.FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (width - titleWidth) / 2, margin / 2 + fm.getAscent()/2);
        int axisX = margin;
        int axisY = height - margin;
        g.drawLine(axisX, margin, axisX, axisY);
        g.drawLine(axisX, axisY, width - margin, axisY);
        java.awt.Font labelFont = g.getFont().deriveFont(java.awt.Font.PLAIN, 14f);
        g.setFont(labelFont);
        g.drawString("usuário no eixo X", width/2 - 40, height - margin + 40);
        g.drawString("ações", margin - 40, margin - 10);
        for (int i = 0; i < list.size(); i++) {
            int cnt = list.get(i).getValue();
            int barHeight = (maxCount == 0) ? 0 : (cnt * (axisY - margin) / maxCount);
            int x = axisX + i * barWidth + 5;
            int y = axisY - barHeight;
            g.setColor(java.awt.Color.BLUE);
            g.fillRect(x, y, barWidth - 10, barHeight);
            g.setColor(java.awt.Color.BLACK);
            String cnts = String.valueOf(cnt);
            int cntW = g.getFontMetrics().stringWidth(cnts);
            g.drawString(cnts, x + ((barWidth-10) - cntW)/2, y - 5);
        }
        g.dispose();
        javax.imageio.ImageIO.write(img, "png", new java.io.File(filename));
    }
}
