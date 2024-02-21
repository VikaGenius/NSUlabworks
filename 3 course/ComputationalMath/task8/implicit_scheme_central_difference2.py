import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation

def initial_condition(x):
    if x < 0:
        return 2
    else:
        return 1

    # if 0 <= x < 1:
    #     return 0
    # elif 1 <= x < 4:
    #     return np.sin(np.pi * (x - 1) / 3)
    # elif 4 <= x <= 5:
    #     return 0

def f(u):
    return u**2/2

def extract_diagonals(matrix, n):
    upper_diagonal = np.zeros(n)
    lower_diagonal = np.zeros(n)

    main_diagonal = np.diag(matrix)
    upper_diagonal[0:-1] = np.diag(matrix, k=1)
    lower_diagonal[1:] = np.diag(matrix, k=-1)
    return main_diagonal, upper_diagonal, lower_diagonal

def tridiagonal_matrix_algorithm(matrix_A, vector_b, n):
    print(vector_b)
    B, C, A = extract_diagonals(matrix_A, n)
    alpha = np.zeros(n)
    beta = np.zeros(n)
    x = np.zeros(n)

    alpha[0] = -C[0] / B[0]
    beta[0] = vector_b[0] / B[0]
    for i in range(1, n):
        denominator = B[i] + A[i] * alpha[i - 1]
        alpha[i] = -C[i] / denominator
        beta[i] = (vector_b[i] - A[i] * beta[i - 1]) / denominator

    x[n - 1] = beta[n - 1]

    for i in range(n - 2, -1, -1):
        x[i] = alpha[i] * x[i + 1] + beta[i]

    return x


x_start, x_end = -2, 5
nx = 501
dx = (x_end - x_start) / (nx - 1)
x = np.linspace(x_start, x_end, nx)

t_start, t_end = 0, 5
nt = 500
dt = (t_end - t_start) / nt

u_initial = np.array([initial_condition(xi) for xi in x])
print(u_initial)
matrixA = np.zeros((nx - 2, nx - 2))
curr_layer = np.zeros(nx-2)
curr_layer = u_initial[1:-1]
fig, ax = plt.subplots()

def update(frame):
    ax.clear()
    #обновляем матрицу, тк наше а = производной от f(u) меняется (равно u)
    for i in range(0, nx - 2):
        r = (u_initial[i] * dt) / dx
        matrixA[i][i] = 1
        if (i > 0):
            matrixA[i][i - 1] = - r / 2
        if (i < nx - 3):
            matrixA[i][i + 1] = r / 2

    #обновление данных на графике на каждом временном слое
    curr_layer = u_initial[1:-1]
    curr_layer[0] += u_initial[0] * r / 2
    curr_layer[-1] -= u_initial[-1] * r / 2
    curr_layer = tridiagonal_matrix_algorithm(matrixA, curr_layer, nx - 2)

    u_initial[1:-1] = curr_layer

    ax.plot(x, u_initial, label='Solution')
    ax.set_xlabel('x')
    ax.set_ylabel('u(x, t)')
    ax.set_title('Solution of the transport equation with implicit scheme and central difference')
    ax.legend()

#анимируем
animation = FuncAnimation(fig, update, frames=nt, repeat=False)
plt.show()