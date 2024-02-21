import numpy as np
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt

def f(x):
    return x**2 + 4 * np.sin(x) - 2

def lagrange_interpolation(x, x_values, y_values):
    n = len(x_values)
    result = 0.0
    for i in range(n):
        tmp = y_values[i]
        for j in range(n):
            if i != j:
                tmp *= (x - x_values[j]) / (x_values[i] - x_values[j])
        result += tmp
    return result

start = -5
end = 5
num_nodes = 10

#  генерация узлов
x_values = np.linspace(start, end, num_nodes)
y_values = f(x_values)

# точка для проверки погрешности
test_point = 1.5

# вычисление интерполяционного полинома и погрешности на заданной точке
interpolated_value = lagrange_interpolation(test_point, x_values, y_values)
exact_value = f(test_point)
error = abs(exact_value - interpolated_value)

# удвоение количества узлов и вычисление погрешности
num_nodes2 = num_nodes*2
x_values2 = np.linspace(start, end, num_nodes2)
y_values2 = f(x_values2)
interpolated_value2 = lagrange_interpolation(test_point, x_values2, y_values2)
error2 = abs(exact_value - interpolated_value2)

print("погрешность численного решения на исходном количестве узлов:", error)
print("погрешность численного решения на удвоенном количестве узлов:", error2)

# построение графиков 
x_plot = np.linspace(start, end, num_nodes)
y_plot_exact = f(x_plot)
interpolated_plot = lagrange_interpolation(x_plot, x_values, y_values)
int2 = lagrange_interpolation(x_plot, x_values2, y_values2)
plt.plot(x_plot, int2, color='green')
plt.plot(x_plot, y_plot_exact)
plt.plot(x_plot, interpolated_plot, linestyle='dashed')
plt.xlabel('x')
plt.ylabel('y')
plt.ylim(-20, 20)
plt.xlim(-20, 20)
plt.grid(True)
plt.show()