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

		print(f"Arestas esperadas: {E}")
		print(f"Arestas lidas: {lidas}")

	return g


def calcular_metricas(g):
	V = g.V
	E = g.E

	graus = np.array([g.degree(v) for v in range(V)])

	grau_medio = graus.mean()
	densidade = E / (V * (V - 1))

	print("=== MÉTRICAS BÁSICAS ===")
	print(f"|V| = {V}")
	print(f"|E| = {E}")
	print(f"Densidade = {densidade:.6f}")
	print(f"Grau médio = {grau_medio:.4f}")

	return graus


def plot_histograma(graus):
	import numpy as np
	import matplotlib.pyplot as plt

	valores, contagens = np.unique(graus, return_counts=True)

	plt.figure(figsize=(22, 4))
	plt.bar(valores, contagens, width=1.0)

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

	graus = graus[graus > 0]

	# PDF empírica
	valores, contagens = np.unique(graus, return_counts=True)
	pk = contagens / contagens.sum()

	# Ajuste power-law
	fit = powerlaw.Fit(graus, discrete=True)
	gamma = fit.power_law.alpha
	xmin = fit.power_law.xmin

	print("\n=== AJUSTE LEI DE POTÊNCIA ===")
	print(f"Expoente gamma (α) = {gamma:.4f}")
	print(f"xmin = {xmin}")
	print("Expressão: P(k) ~ k^(-gamma)")

	R, p = fit.distribution_compare('power_law', 'lognormal')
	print(f"FitCompare (power law vs lognormal): R = {R:.4f}, p = {p:.4f}")

	# Plot
	plt.figure(figsize=(8, 6))

	# Pontos empíricos
	plt.scatter(valores, pk, s=20, alpha=0.8, label='P(k) empírica')

	# Reta da lei de potência (modelo ajustado pelo powerlaw)
	fit.power_law.plot_pdf(color='red', linestyle='--', linewidth=2, label='Lei de potência ajustada')

	plt.xscale('log')
	plt.yscale('log')

	plt.xlim(valores.min()*0.9, valores.max()*1.1)
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

	plot_histograma(graus)
	gamma = plot_loglog_powerlaw(graus)