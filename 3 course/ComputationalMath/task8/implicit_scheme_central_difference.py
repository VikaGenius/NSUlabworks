import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation

def plot_graph(x, y):
    plt.plot(x, y, label='y(x)', marker=".")
    plt.xlabel('x')
    plt.ylabel('y')
    plt.title('График функции y(x)')
    plt.legend()
    plt.grid(True)
    plt.show()


def initial_condition(x):
    if -1.0 <= x < 0.0:
        return 2.0
    elif 0.0 <= x <= 1.0:
        return 1.0

    # if 0 <= x < 1:
    #     return 0
    # elif 1 <= x < 4:
    #     return np.sin(np.pi * (x - 1) / 3)
    # elif 4 <= x <= 5:
    #     return 0

def f(u, c):
    return c * u

def extract_diagonals(matrix, n):
    upper_diagonal = np.zeros(n)
    lower_diagonal = np.zeros(n)

    main_diagonal = np.diag(matrix)
    upper_diagonal[0:-1] = np.diag(matrix, k=1)
    lower_diagonal[1:] = np.diag(matrix, k=-1)
    return main_diagonal, upper_diagonal, lower_diagonal

def tridiagonal_matrix_algorithm(matrix_A, vector_b, n):
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
    #print(x)
    return x


x_start, x_end = -1, 1
nx = 10
dx = (x_end - x_start) / (nx - 1)
x = np.linspace(x_start, x_end, nx)

t_start, t_end = 0, 2
nt = 10
dt = (t_end - t_start) / nt

u_initial = np.array([initial_condition(xi) for xi in x])
plot_graph(x, u_initial)

c = 1.0
a = c
r = (a * dt) / dx
print(f'Условие устойчивости выполняется: r = {r}')

matrixA = np.zeros((nx - 2, nx - 2))
for i in range(0, nx - 2):
    matrixA[i][i] = 1
    if (i > 0):
        matrixA[i][i - 1] = - r / 2
    if (i < nx - 3):
        matrixA[i][i + 1] = r / 2


curr_layer = np.zeros(nx-2)
curr_layer = u_initial[1:-1]

#for i in range (0, 2):
    #curr_layer[0] += u_initial[0] #* r / 2
    #curr_layer[1] = u_initial[1]
    #curr_layer[2] = u_initial[2]
    
    # 2 3 4 5 6 7 8 9 
    # 2.25

   # curr_layer[-1] -= u_initial[-1] #* r / 2
    
    #curr_layer = tridiagonal_matrix_algorithm(matrixA, curr_layer, nx - 2)
    

    # curr_layer[0] = 2
    # curr_layer[1] = 2
    # curr_layer[2] = 2
    # curr_layer[3] = 2
    # curr_layer[4] = 2

    # curr_layer[-1] = 1
    # curr_layer[-2] = 1
    # curr_layer[-3] = 1
    # curr_layer[-4] = 1
    # curr_layer[-5] = 1
    #u_initial[1:-1] = curr_layer

    #plot_graph(x, u_initial)



fig, ax = plt.subplots()

def update(frame):
    ax.clear()

    #обновление данных на графике на каждом временном слое
    curr_layer = u_initial[1:-1]
    
    curr_layer[0] = u_initial[1] + u_initial[0] * r / 2.0
    curr_layer[-1] = u_initial[-2] - u_initial[-1] * r / 2.0
    # curr_layer[0] = u_initial[0]
    # curr_layer[1] = u_initial[0]
    # curr_layer[2] = u_initial[0]


    # curr_layer[-1] = u_initial[-1]
    # curr_layer[-2] = u_initial[-1]
    # curr_layer[-3] = u_initial[-1]

    
    curr_layer = tridiagonal_matrix_algorithm(matrixA, curr_layer, nx - 2)
    u_initial[1:-1] = curr_layer
  
    print("CURR LAYER")
    print(u_initial)

    ax.plot(x, u_initial, label='Solution', marker='.')
    ax.set_xlabel('x')
    ax.set_ylabel('u(x, t)')
    ax.set_title('Solution of the transport equation with implicit scheme and central difference')
    ax.legend()

#анимируем
animation = FuncAnimation(fig, update, frames=nt, repeat=False)
plt.show()
    



    # for i in range (0, 2):
    # curr_layer[0] += u_initial[0] #* r / 2
    # curr_layer[1] = u_initial[1]
    # curr_layer[2] = u_initial[2]

    # curr_layer[-1] -= u_initial[-1] * r / 2
    
    # curr_layer = tridiagonal_matrix_algorithm(matrixA, curr_layer, nx - 2)
    
    # u_initial[1:-1] = curr_layer

    # plot_graph(x, u_initial)