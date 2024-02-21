import numpy as np
import matplotlib.pyplot as plt
import math 


def equation(x, a):
    return x + a - np.exp(x)

def derivative(x):
    return 1 - np.exp(x)

def newton_method(a, initial_value, eps=1e-6, max_iterations=1000):
    grade = [] 
    x = initial_value
    iteration = 0

    while iteration < max_iterations:
        f_x = equation(x, a)
        f_prime_x = derivative(x)

        x_new = x - f_x / f_prime_x
        grade.append(x_new)
        iteration += 1

        if np.abs(x_new - x) < eps:
            return x_new, iteration, grade
        
        x = x_new

    print("Max iterations reached. The method did not converge.")
    return None, iteration, grade

def plot_equation(a):
    x_values = np.linspace(-3, 3, 400)
    y_values = equation(x_values, a)

    plt.plot(x_values, y_values, label=r'$e^x - x - a$')
    plt.axhline(0, color='black', linewidth=0.5, linestyle='--')
    plt.axvline(0, color='black', linewidth=0.5, linestyle='--')
    plt.title(f'Graph of $e^x - x - {a}$')
    plt.xlabel('x')
    plt.ylabel('f(x)')
    plt.legend()
    plt.show()


def plot_grade(grade):
    iterations = np.arange(len(grade))
    plt.plot(iterations, grade, marker='o', linestyle='-', color='b')
    plt.yscale('log')  # Логарифмическая шкала по оси y для лучшей видимости
    plt.title('Convergence Plot')
    plt.xlabel('Iteration')
    plt.ylabel(r'$|x_i - x_{\mathrm{root}}|$')
    plt.grid(True)
    plt.show()

a = 3
initial_value = 1
root, iterations, grade = newton_method(a, initial_value)

plot_equation(a)

if root is not None:
    for i in range(len(grade)):
        grade[i] = abs(grade[i] - root)
    
    plot_grade(grade)
    print(f'Root found by Newton method: {root} with {iterations} iterations')
else: print('Root not found')

initial_value = -7
root, iterations, grade = newton_method(a, initial_value)
if root is not None:
    for i in range(len(grade)):
        grade[i] = abs(grade[i] - root)
    plot_grade(grade)
    print(f'Root found by Newton method: {root} with {iterations} iterations')
else: print('Root not found')

#график количественной оценки порядка сходимости для двух методов с эксп 