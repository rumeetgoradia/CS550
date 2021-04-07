def modularity(A):
    m = 0
    for r in A:
        m = m + sum(r)
    m = m/2

    ans = 0
    for i in range(0, len(A)):
        for j in range(0, len(A[i])):
            ki = sum(A[i])
            kj = sum(A[j])
            si = 1
            sj = 1
            if(i > len(A)/2 - 1):
                si = -1
            if(j > len(A)/2 - 1):
                sj = -1
            ans = ans + (A[i][j] - (ki*kj)/(2*m))*si*sj

    return ans/(4*m)


A = [
    [0, 1, 1, 1, 0, 0, 0, 0],
    [1, 0, 1, 1, 0, 0, 0, 0],
    [1, 1, 0, 1, 0, 0, 0, 0],
    [1, 1, 1, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 1, 1, 0],
    [0, 0, 0, 0, 1, 0, 1, 0],
    [0, 0, 0, 0, 1, 1, 0, 1],
    [0, 0, 0, 0, 0, 0, 1, 0]
]
print(modularity(A))

A[0][6] = 1
A[6][0] = 1
A[4][7] = 1
A[7][4] = 1
print(modularity(A))

A[4][7] = 0
A[7][4] = 0
A[0][5] = 1
A[5][0] = 1
print(modularity(A))
