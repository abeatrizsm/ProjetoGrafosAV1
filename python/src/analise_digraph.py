import sys
import numpy as np
import matplotlib.pyplot as plt
import powerlaw

from digraph import Digraph


def carregar_grafo(caminho):
	with open(caminho, 'r', encoding='utf-8') as f:
		V = int(f.readline().strip())
		E = int(f.readline().strip())

		g = Digraph(V)

		lidas = 0
		for linha in f:
			linha = linha.strip()
			if not linha:
				continue

			partes = linha.split()
			if len(partes) != 2:
				continue

			v, w = int(partes[0]), int(partes[1])
			g.add_edge(v, w)
			lidas += 1

		print(f"teste {E} / {lidas}")
		

	return g


def calcular_metricas(g):
	V = g.V
	E = g.E

	graus = np.array([g.degree(v) for v in range(V)])

	grau_medio = graus.mean()
	densidade = E / (V * (V - 1))

	print("\n\n\n-----------------------------------")
	print("Metricas básicas: ")
	print(f" -Vertices = {V}")
	print(f" -Arestas = {E}")
	print(f" -Densidade = {densidade:.6f}")
	print(f" -Grau médio = {grau_medio:.4f}")
	print("-----------------------------------\n\n\n")

	return graus

def plot_histograma(graus):
	import numpy as np
	import matplotlib.pyplot as plt

	grau, frequencia = np.unique(graus, return_counts=True)

	plt.figure(figsize=(22, 4))
	plt.bar(grau, frequencia, width=1.0)

	plt.xlim(left=-0.5)
	plt.margins(x=0.01)

	plt.xlabel("Grau (k)")
	plt.ylabel("Frequência")
	plt.title("Histograma do Grau dos Vértices (frequência por grau)")
	plt.tight_layout()
	plt.show()

def plot_loglog_powerlaw(graus):
    
	import numpy as np
	import matplotlib.pyplot as plt
	import powerlaw

	fig, ax = plt.subplots()
	graus = graus[graus > 0]
 
	grau, frequencia = np.unique(graus, return_counts=True)
	pk = frequencia / frequencia.sum()
 
	fit = powerlaw.Fit(graus, discrete=True)
	gamma = fit.power_law.alpha
	xmin = fit.power_law.xmin
	ks = fit.power_law.D
	n_cauda = fit.power_law.n
	
	print("\n\n\n-----------------------------------")
	print("Lei de potência:")
	print(f" -Expoente gamma = {gamma:.4f}")
	print(f" -xmin = {xmin}")
	print(f" -KS = {ks:.6f}")
	print(f" -n_cauda = {n_cauda}")

	R, p = fit.distribution_compare('power_law', 'lognormal')
	print(f"FitCompare (power law vs lognormal): R = {R:.4f}, p = {p:.4f}")
	print("-----------------------------------\n\n\n")

	fit.plot_pdf(ax=ax, label='PDF')

	plt.figure(figsize=(8, 6))
	plt.scatter(grau, pk, s=20, alpha=0.8, label='P(k) empírica')
	fit.power_law.plot_pdf(ax = ax , color='red', linestyle='--', linewidth=2, label='Lei de potência')

	plt.xscale('log')
	plt.yscale('log')

	plt.xlim(grau.min()*0.9, grau.max()*1.1)
	plt.ylim(pk[pk > 0].min()*0.8, pk.max()*1.2)

	plt.xlabel("Grau (k)")
	plt.ylabel("Probabilidade P(k)")
	plt.title("Distribuição de Grau em Escala Log-Log")
	plt.legend()
	plt.grid(True, which="both", linestyle="--", alpha=0.4)
	plt.tight_layout()
	plt.show()

	return gamma


if __name__ == "__main__":
	if len(sys.argv) != 2:
		print("Uso: python analise_grafo.py input.txt")
		sys.exit(1)

	caminho = sys.argv[1]

	g = carregar_grafo(caminho)
	graus = calcular_metricas(g)

	#plot_histograma(graus)

	gamma = plot_loglog_powerlaw(graus)
 
 # rodar : python src/analise_digraph.py data/digraph.txt