import numpy as np
from scipy import linalg

print("HW2 Q1.e.a")
M = np.array([[1, 2], [2, 1], [3, 4], [4, 3]])
U, sigma, Vt = linalg.svd(M, False)
print("U", U)
print("Sigma:", sigma)
print("V^T", Vt)

print("------------")

print("HW2 Q1.e.b")
Mt = np.transpose(M)
Evals, Evecs = linalg.eigh(np.matmul(Mt, M))
sortedIndices = np.flip(np.argsort(Evals))
Evals = Evals[sortedIndices]
Evecs = Evecs[:, sortedIndices]
print("Evals:", Evals)
print("Evecs:", Evecs)
