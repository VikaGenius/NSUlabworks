import numpy as np
import matplotlib.pyplot as plt

def f(x):
    return np.log(x + 1)
    #return np.exp(x)*np.cos(x)

def trapezoid_method(f, a, b, n):
    h = (b - a) / n
    x = np.linspace(a, b, n + 1)
    y = f(x)
    integral = h * (np.sum(y) - 0.5 * (y[0] + y[-1]))
    return integral

def simpsons_method(f, a, b, n):
    h = (b - a) / n
    x = np.linspace(a, b, n + 1)
    y = f(x)
    integral = (h / 3) * (y[0] + 4 * np.sum(y[1::2]) + 2 * np.sum(y[2:-1:2]) + y[-1])
    return integral

def runge_rule_error(I1, I2, p):
    return abs(I1 - I2) / (2**p - 1)

a = 0.0
b = 2.0
n = 10

integral_trapezoidal = trapezoid_method(f, a, b, n)
integral_simpsons = simpsons_method(f, a, b, n)

n_double = 2 * n
integral_trapezoid_double = trapezoid_method(f, a, b, n_double)
integral_simpsons_double = simpsons_method(f, a, b, n_double)

error_trapezoidal = runge_rule_error(integral_trapezoid_double, integral_trapezoidal, 2)
error_simpsons = runge_rule_error(integral_simpsons_double, integral_simpsons, 4)

print("Интеграл (формула трапеций):", integral_trapezoidal)
print("Погрешность (формула трапеций):", error_trapezoidal)
print("Интеграл (формула парабол):", integral_simpsons)
print("Погрешность (формула парабол):", error_simpsons)

integral_1 = simpsons_method(f, a, b, n * 10)
print("Квазиточное решение по Симпсону", integral_1)
print("Погрешность: ", integral_1 - integral_simpsons)
