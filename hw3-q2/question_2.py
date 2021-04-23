import numpy as np
from scipy import linalg

np.set_printoptions(precision=2, suppress=True)

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
print("L=", L)
print("\n")

Evals, Evecs = linalg.eigh(L)
sortedIndices = np.argsort(Evals)
Evals = Evals[sortedIndices]
Evecs = np.transpose(Evecs)[:, sortedIndices]
print("Evals:", Evals)
print("Evecs:", Evecs)

# print(Evals[1])
# print(np.transpose(Evecs)[1])
