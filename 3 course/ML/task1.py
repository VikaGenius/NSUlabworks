import numpy as np
import math as m
import matplotlib.pyplot as plt
from scipy.linalg import solve

#регулируемые параметры
N = 20 #размер выборки
M = 9 #степень полинома в регрессии
eps0 = 0.1 #ошибка
mean = 0 #среднее значение
std_deviation = eps0 / 2 #стандартное отклонение
border_coef = 3 #коэф полинома от -3 до 3
border_x = 1
y_lim = 3

def f_sin(x):
  return x * m.sin(2 * m.pi * x)

coef = np.random.uniform(-border_coef, border_coef, 4)
def f_polynomial(x):
  return coef[0] * x**3 + coef[1] * x**2 + coef[2] * x + coef[3]

def sampling_by_function(x, func, eps):
  y = np.zeros(shape=(N), dtype = 'f')
  for i in range (0, N):
    y[i] = eps[i] + func(x[i])

  return y

def calc_func(size, x, func):
  y = np.zeros(shape=(size), dtype = 'f')
  for i in range (0, size):
    y[i] = func(x[i])

  return y

#генерация данных
x = np.random.uniform(-border_x, border_x, N)
eps = np.random.uniform(-eps0, eps0, N)
#eps = np.random.normal(mean, std_deviation, N)

print(f"{type(x)}\n")
print(f"массив x: {x}\n")

y_sin = sampling_by_function(x, f_sin, eps)
y_polynomial = sampling_by_function(x, f_polynomial, eps)

plt.scatter(x, y_sin)
plt.scatter(x, y_polynomial)

size = 10 * N #количество точек для отображения исходной функции
xx = np.linspace(-border_x, border_x, size)

yy_sin = calc_func(size, xx, f_sin)
yy_polynomial = calc_func(size, xx, f_polynomial)
plt.plot(xx, yy_sin, label='f_sin')
plt.plot(xx, yy_polynomial, label='f_polynomial')

plt.xlabel('x')
plt.ylabel('y')
plt.ylim(-y_lim, y_lim)

plt.legend()
plt.show()

##################################полиномиальная регрессия:

A = np.zeros(shape = (M, M))
b = np.zeros(shape = M)

def element_matrix(i, j):
  sum = 0
  for k in range(0, N):
    sum += x[k]**(i + j)

  return sum

def right_part(y, i):
  sum = 0
  for k in range(0, N):
    sum += y[k] * x[k]**i

  return sum

def calc_matrix_and_rp(A, b, y):
  for i in range(0, M):
    b[i] = right_part(y, i)
    for j in range (0, M):
      A[i][j] = element_matrix(i, j)
  return A, b

def calc_polynomial(x):
  sum = 0
  for i in range(0, M):
    sum += c[i] * x**i

  return sum

def plot_func(sample, sample_func, original):
  plt.scatter(x, sample)
  plt.plot(xx, sample_func, label='полученная функция')
  plt.plot(xx, original, label='original')
  plt.xlabel('x')
  plt.ylabel('y')
  plt.ylim(-y_lim, y_lim)

  plt.legend()
  plt.show()

#для синуса
A, b = calc_matrix_and_rp(A, b, y_sin)
c = solve(A, b)

y_new = calc_func(size, xx, calc_polynomial)
plot_func(y_sin, y_new, yy_sin)

#для полинома
A, b = calc_matrix_and_rp(A, b, y_polynomial)
c = solve(A, b)

y_new1 = calc_func(size, xx, calc_polynomial)
plot_func(y_polynomial, y_new1, yy_polynomial)