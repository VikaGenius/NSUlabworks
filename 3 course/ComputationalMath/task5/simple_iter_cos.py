import numpy as np
import matplotlib.pyplot as plt

def equation(x, l):
    return x - l * np.cos(x + np.pi/2)

def simple_iteration(l, init_value, eps, max_iterations=1000):
    x = init_value
    iteration = 0
    while iteration < max_iterations:
        x_new = l * np.cos(x + np.pi/2)
        iteration += 1
        if np.abs(x_new - x) < eps:
            return x_new, iteration
        x = x_new
    print("Max iterations reached. The method did not converge.")
    return None, iteration

def simple_iteration2(l, init_value, eps, max_iterations=1000):
    x = init_value
    iteration = 0
    while iteration < max_iterations:
        x_new =  np.arccos(x/l) - np.pi/2
        iteration += 1

        if np.abs(x_new - x) < eps:
            return x_new, iteration

        x = x_new
    print("Max iterations reached. The method did not converge.")
    return None, iteration

def plot_equation(a):
    x_values = np.linspace(-10, 10, 400)
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
plot_equation(l)

eps = 0.00001
roots = set()

for i in range(l):
    start = i * np.pi/2
    end = np.pi / 2 * (i + 1)
    initial_value = start
    root, iterations = simple_iteration(l, initial_value, eps)
    if root is not None:
        root = round(root, 6)
        roots.add(root)
        roots.add(-1 * root)


for i in range(l):
    start = i * np.pi/2
    end = np.pi / 2 * (i + 1)
    initial_value = start
    root, iterations = simple_iteration2(l, initial_value, eps)
    if root is not None:
        root = round(root, 6)
        roots.add(root)
        roots.add(-1 * root)


for root in roots:
    print(f'Root found by Newton method: {root} with {iterations} iterations')
