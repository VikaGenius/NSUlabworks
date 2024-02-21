import numpy as np
import matplotlib.pyplot as plt
import math

def plot_graph(x, y):
    plt.plot(x, y, label='y(x)', marker=".")
    plt.xlabel('x')
    plt.ylabel('y')
    plt.title('График функции y(x)')
    plt.legend()
    plt.grid(True)
    plt.show()

def f(x):
    return (np.exp(x) * np.sin(x)) / 2 - (np.exp(x) * np.cos(x)) / 2 + 3 / 2

def g(x):
    return math.exp(x) * math.sin(x)

def y_2p(h, x):
    y = []
    y.append(1)
    y.append(h * g(x[0]) + y[0])
    for i in range(1, len(x) - 1):
        #y.append(h * g(x[i]) + y[i])
        y.append(y[i - 1] + h * (g(x[i + 1]) + g(x[i - 1])))
        #y.append(y[i] * ((1 + h / 2) / (1 - h / 2)))
    return y

def y_4p(h, x):
    y = []
    y.append(1)
    y.append(f(x[0]))
    for i in range(1, len(x) - 1):
        #y.append(h * g(x[i]) + y[i])
        y.append(y[i - 1] + h / 3 * g(x[i - 1]) + 4 * h / 3 * g(x[i]) + h / 3 * g(x[i + 1]))
        #y.append(y[i] * ((1 + h / 2) / (1 - h / 2)))
    return y


start = 0
end = 10
h = 0.5
x = np.arange(start, end, h)
y_2por = y_2p(h, x)
y_4por = y_4p(h, x)

plot_graph(x, y_2por)
plot_graph(x, y_4por)

