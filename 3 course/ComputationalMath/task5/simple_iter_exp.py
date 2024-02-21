import numpy as np
import matplotlib.pyplot as plt

def equation(x, a):
    return np.exp(x) - x - a

def simple_iteration(a, init_value, eps):
    grade = []
    x = init_value
    iteration = 0
    while True:
        x_new = np.exp(x) - a
        grade.append(x_new)
        iteration += 1
        if np.abs(x_new - x) < eps:
            break
        x = x_new
    return x_new, iteration, grade

def simple_iteration2(a, init_value, eps, max_iterations=1000):
    grade = []
    x = init_value
    iteration = 0
    while iteration < max_iterations:
        x_new = np.log(x + a)
        grade.append(x_new)
        iteration += 1

        if np.abs(x_new - x) < eps:
            return x_new, iteration, grade
        
        x = x_new
    print("Max iterations reached. The method did not converge.")
    return None, iteration

def plot_equation(a):
    x_values = np.linspace(-2, 2, 400)
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
plot_equation(a)

init_value = 1     
eps = 0.00001      

root, iterations, grade = simple_iteration(a, init_value, eps)
root2, iterations2, grade2 = simple_iteration2(a, init_value, eps)

if root == root2 and root is not None and root2 is not None: print(f'Root found by Newton method: {root} with {iterations} iterations')
else:
    if root is not None:
        for i in range(len(grade)):
            grade[i] = abs(grade[i] - root)
    
        plot_grade(grade)
        print(f'Root found by Newton method: {root} with {iterations} iterations')
    else: print('Root not found')

    if root2 is not None:
        for i in range(len(grade2)):
            grade2[i] = abs(grade2[i] - root2)
    
        plot_grade(grade2)
        print(f'Root found by Newton method: {root2} with {iterations2} iterations')
    else: print('Root not found')

