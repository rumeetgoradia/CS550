import numpy as np
from scipy import linalg

A = np.array([
    [0, 1, 1, 1, 0, 0, 1, 0],
    [1, 0, 1, 1, 0, 0, 0, 0],
    [1, 1, 0, 1, 0, 0, 0, 0],
    [1, 1, 1, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 1, 1, 0],
    [0, 0, 0, 0, 1, 0, 1, 0],
    [1, 0, 0, 0, 1, 1, 0, 1],
    [0, 0, 0, 0, 0, 0, 1, 0]
])

D = np.array([
    [4, 0, 0, 0, 0, 0, 0, 0],
    [0, 3, 0, 0, 0, 0, 0, 0],
    [0, 0, 3, 0, 0, 0, 0, 0],
    [0, 0, 0, 3, 0, 0, 0, 0],
    [0, 0, 0, 0, 2, 0, 0, 0],
    [0, 0, 0, 0, 0, 2, 0, 0],
    [0, 0, 0, 0, 0, 0, 4, 0],
    [0, 0, 0, 0, 0, 0, 0, 1],
])

L = np.subtract(D, A)
print(L)

Evals, Evecs = linalg.eigh(L)
print(Evals)
sortedIndices = np.argsort(Evals)
print(Evals)
Evals = Evals[sortedIndices]
Evecs = Evecs[:, sortedIndices]
print("Evals:", Evals)
print("Evecs:", Evecs)

print(Evecs[1])
