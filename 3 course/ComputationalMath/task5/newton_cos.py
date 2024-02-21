import numpy as np
import matplotlib.pyplot as plt
import math

def equation(x, l):
    return x - l * np.cos(x + np.pi/2)

def derivative(x, l):
    return 1 + l * np.sin(x + np.pi/2)

def newton_method(l, initial_value, eps=1e-6, max_iterations=1000):
    x = initial_value
    iteration = 0

    while iteration < max_iterations:
        f_x = equation(x, l)
        f_prime_x = derivative(x, l)

        x_new = x - f_x / f_prime_x
        iteration += 1

        if np.abs(x_new - x) < eps:
            return x_new, iteration

        x = x_new

    print("Max iterations reached. The method did not converge.")
    return None, iteration

def plot_equation(a):
    x_values = np.linspace(-12, 12, 400)
    y_values = equation(x_values, a)

    plt.plot(x_values, y_values, label=r'$x - l \cdot \cos(x + \frac{\pi}{2})$')
    plt.axhline(0, color='black', linewidth=0.5, linestyle='--')
    plt.axvline(0, color='black', linewidth=0.5, linestyle='--')
    plt.title(r'Graph of $x - l \cdot \cos(x + \frac{\pi}{2})$')
    plt.xlabel('x')
    plt.ylabel('f(x)')
    plt.legend()
    plt.show()

l = 11
k = math.ceil(l / np.pi)

roots = set()  # Используем множество для хранения уникальных корней

for i in range(l):
    start = -np.pi/2 + np.pi/2 * i
    end = np.pi / 2 * (i + 1)
    initial_value = start + np.pi / 2
    root, iterations = newton_method(l, initial_value)
    if root is not None:
        root = round(root, 6)  # Округляем до 6 знаков после запятой
        roots.add(-1 * root)
        roots.add(root)


# Вывод уникальных корней
for root in roots:
    print(f'Root found by Newton method: {root} with {iterations} iterations')

plot_equation(l)
