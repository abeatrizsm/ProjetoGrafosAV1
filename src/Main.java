import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Garante que a pasta de saídas existe
        File pasta = new File("saidas");
        if (!pasta.exists()) {
            pasta.mkdir();
            System.out.println("-> Pasta 'saidas' criada.");
        }

        System.out.println("==========================================");
        System.out.println("   PROJETO DE ANÁLISE DE GRAFOS - MOOC   ");
        System.out.println("==========================================");
        System.out.println("1 - Gerar 'digraph.txt' e Histograma de Usuários");
        System.out.println("2 - Análise de Graus e Log-Log (DegreeAnalysis)");
        System.out.println("0 - Sair");
        System.out.print("\nEscolha uma opção: ");

        int opcao = scanner.nextInt();

        try {
            switch (opcao) {
                case 1:
                    System.out.println("\n[Executando Conversão e Histograma...]");
                    // O Convert lerá o .tsv e gerará o .txt e a imagem
                    Convert.main(new String[]{}); 
                    break;
                    
                case 2:
                    System.out.println("\n[Executando Análise de Graus...]");
                    // Passamos o digraph.txt gerado pela opção 1
                    DegreeAnalysis.main(new String[]{"digraph.txt"});
                    break;

                case 0:
                    System.out.println("Saindo...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        } catch (Exception e) {
            System.err.println("\n[ERRO]: " + e.getMessage());
        }

        scanner.close();
    }

    /**
     * Utilitário para gerar nomes versionados (a1, a2...)
     * Pode ser chamado de dentro do Convert ou DegreeAnalysis
     */
    public static String getVersionedName(String baseName, String extension) {
        int v = 1;
        while (true) {
            String name = baseName + "_a" + v + extension;
            File f = new File("saidas/" + name);
            if (!f.exists()) return "saidas/" + name;
            v++;
        }
    }
}