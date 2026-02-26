USUARIOS = 7047
VERTICES = 7144

ENTRADA = "data/mooc_actions.tsv"
SAIDA = "data/digraph.txt"


def converter(entrada=ENTRADA, saida=SAIDA):
	arestas = []

	with open(entrada, "r", encoding="utf-8") as f:
		next(f)

		for linha in f:
			linha = linha.strip()
			if not linha:
				continue

			partes = linha.split("\t")

			usuario = int(partes[1])
			alvo = int(partes[2]) + USUARIOS

			arestas.append((usuario, alvo))

	with open(saida, "w", encoding="utf-8") as f:
		f.write(f"{VERTICES}\n")
		f.write(f"{len(arestas)}\n")

		for v, w in arestas:
			f.write(f"{v} {w}\n")

	print("Conversão concluída!")
	print("Arestas gravadas:", len(arestas))


if __name__ == "__main__":
	converter()
 
#rodar: python src/convert.py