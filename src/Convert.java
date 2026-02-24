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

    public static void main(String[] args) {
        String entrada = "mooc_actions.tsv";
        String saida = "digraph.txt";

        List<int[]> arestas = new ArrayList<>();
        Map<Integer, Integer> freq = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(entrada));
             BufferedWriter bw = new BufferedWriter(new FileWriter(saida))) {
            
            String linha = br.readLine(); // Pular cabeçalho

            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] partes = linha.split("\t");
                int usuario = Integer.parseInt(partes[1]);
                int alvo = Integer.parseInt(partes[2]) + USUARIOS;

                arestas.add(new int[]{usuario, alvo});
                freq.put(usuario, freq.getOrDefault(usuario, 0) + 1);
            }

            bw.write(VERTICES + "\n");
            bw.write(arestas.size() + "\n");
            for (int[] aresta : arestas) {
                bw.write(aresta[0] + " " + aresta[1]);
                bw.newLine();
            }
            bw.flush();
            System.out.println("Arquivo " + saida + " gerado com sucesso.");

            // Preparar dados e ordenar
            List<Map.Entry<Integer, Integer>> list = new ArrayList<>(freq.entrySet());
            list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // LÓGICA DE VERSIONAMENTO: Chama a Main para pegar o nome do arquivo
            String nomeImagemVersionada = Main.getVersionedName("user_hist", ".png");

            System.out.println("\nGerando imagem: " + nomeImagemVersionada);
            writeUserHistogramImage(list, "Histograma de Ações por Usuário", nomeImagemVersionada);
            System.out.println("Concluído.");

        } catch (Exception e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeUserHistogramImage(List<Map.Entry<Integer, Integer>> list,
                                                String title, String filename) throws java.io.IOException {
        int barWidth = 40;
        int margin = 80; 
        int width = list.size() * barWidth + margin * 2;
        int height = 800; // Altura fixa para evitar OutOfMemoryError em 8GB RAM

        int maxCount = 0;
        for (Map.Entry<Integer, Integer> e : list) {
            if (e.getValue() > maxCount) maxCount = e.getValue();
        }

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();

        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(java.awt.Color.BLACK);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        java.awt.FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (width - fm.stringWidth(title)) / 2, margin / 2);

        int axisX = margin;
        int axisY = height - margin;
        int plotAreaHeight = axisY - margin;

        g.drawLine(axisX, margin, axisX, axisY);
        g.drawLine(axisX, axisY, width - margin, axisY);

        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        g.drawString("Usuários (Ordenados por atividade)", width / 2 - 100, axisY + 50);
        g.drawString("Ações", 10, margin - 10);

        for (int i = 0; i < list.size(); i++) {
            int cnt = list.get(i).getValue();
            int barHeight = (maxCount == 0) ? 0 : (int) (((double) cnt / maxCount) * plotAreaHeight);

            int x = axisX + i * barWidth + 5;
            int y = axisY - barHeight;

            g.setColor(new java.awt.Color(70, 130, 180));
            g.fillRect(x, y, barWidth - 10, barHeight);

            g.setColor(java.awt.Color.BLACK);
            g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
            String cnts = String.valueOf(cnt);
            int cntW = g.getFontMetrics().stringWidth(cnts);
            g.drawString(cnts, x + ((barWidth - 10) - cntW) / 2, y - 5);
        }

        g.dispose();
        javax.imageio.ImageIO.write(img, "png", new java.io.File(filename));
    }
}