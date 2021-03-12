import os

import numpy as np

NUM_NODES = 100
BETA = 0.8
ONE = np.ones((NUM_NODES, 1))
NUM_ITERATIONS = 40

edges = np.zeros((NUM_NODES, NUM_NODES))
outEdges = {}
graphFilePath = os.path.join(os.path.split(os.path.abspath(__file__))[0], "graph.txt")
graphFile = open(graphFilePath)
for line in graphFile:
    source, dest = line.split()
    source = int(source) - 1
    dest = int(dest) - 1
    edges[source, dest] += 1.0

    if source in outEdges:
        outEdges[source] += 1
    else:
        outEdges[source] = 1
graphFile.close()

M = np.zeros((NUM_NODES, NUM_NODES))
for i in range(0, M.shape[0]):
    for j in range(0, M.shape[1]):
        M[j, i] += (1. / outEdges[i]) * edges[i, j]


r = (1.0 / NUM_NODES) * ONE
for i in range(0, NUM_ITERATIONS):
    tempR = r
    r = ((1 - BETA) / NUM_NODES) * ONE + BETA * M @ tempR

top5Nodes = np.flip(np.argsort(r.T))[:, :5] + 1
top5Nodes = top5Nodes[0]
bottom5Nodes = np.argsort(r.T)[:, :5] + 1
bottom5Nodes = bottom5Nodes[0]

print("TOP 5 NODES")
for node in top5Nodes:
    print(node, "\t|\t", r[node - 1][0])

print("------------------")
print("BOTTOM 5 NODES")
for node in bottom5Nodes:
    print(node, "\t|\t", r[node - 1][0])
