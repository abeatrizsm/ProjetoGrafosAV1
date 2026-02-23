import sys
import os

USUARIOS = 7047
VERTICES = 7144


def convert(entrada_path, saida_path):
    arestas = []
    with open(entrada_path, 'r', encoding='utf-8') as fr:
        # skip header
        next(fr)
        for linha in fr:
            linha = linha.strip()
            if not linha:
                continue
            partes = linha.split('\t')
            try:
                usuario = int(partes[1])
                alvo = int(partes[2]) + USUARIOS
            except (IndexError, ValueError):
                # skip malformed lines
                continue
            arestas.append((usuario, alvo))

    os.makedirs(os.path.dirname(saida_path), exist_ok=True)
    with open(saida_path, 'w', encoding='utf-8') as fw:
        fw.write(f"{VERTICES}\n")
        fw.write(f"{len(arestas)}\n")
        for u, v in arestas:
            fw.write(f"{u} {v}\n")


if __name__ == '__main__':
    if len(sys.argv) >= 3:
        entrada = sys.argv[1]
        saida = sys.argv[2]
    else:
        entrada = os.path.join('python', 'data', 'mooc_actions.tsv')
        saida = os.path.join('python', 'data', 'digraph.txt')

    convert(entrada, saida)
    print(f"Wrote {saida}")
