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

def lax_wendroff(u, dx, dt, f):
    nx = len(u)
    un = np.zeros(nx)
    un[0] = 2
    un[nx - 1] = 1
    for i in range(1, nx - 1):
        a = u[i]
        r = (dt * a) / dx
        print(f'Число Куранта: r = {r}')
        un[i] = u[i] - a * dt / (2 * dx) * (f(u[i + 1]) - f(u[i - 1])) + \
                a**2 * dt**2 / (2 * dx**2) * (f(u[i + 1]) - 2 * f(u[i]) + f(u[i - 1]))

    return un

def update(frame):
    global u_initial
    u_initial = lax_wendroff(u_initial, dx, dt, lambda u: f(u))
    line.set_ydata(u_initial)
    return line,

#задание пространственной сетки
x_start, x_end = -2, 5
nx = 101
dx = (x_end - x_start) / (nx - 1)
x = np.linspace(x_start, x_end, nx)

#задание временной сетки
t_start, t_end = 0, 2
nt = 100
dt = (t_end - t_start) / nt

u_initial = np.array([initial_condition(xi) for xi in x])

# r = (dt * a) / dx
# if (r <= 1):
#     print(f'Условие устойчивости выполняется: r = {r}')
# else:
#     print(f'ATTENTION: Условие устойчивости НЕ выполняется: r = {r}')

# Создание графика
fig, ax = plt.subplots()
line, = ax.plot(x, u_initial, label='Решение', marker = '.')
ax.set_xlabel('x')
ax.set_ylabel('u(x, t)')
ax.set_title('Решение уравнения переноса схемой Лакса-Вендроффа')
ax.legend()

# Анимация
animation = FuncAnimation(fig, update, frames=nt, blit=True)
plt.show()
