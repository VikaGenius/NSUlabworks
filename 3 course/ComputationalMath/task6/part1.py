import numpy as np
import matplotlib.pyplot as plt

#решение диффура
def f(x):
    return -1 / np.exp(x)

def y_1p(h, x):
    y = []
    for i in range(len(x)):
        y.append((-1) * (1 - h)**i)
    return y

def y_2p(h, x):
    y = []
    y.append(-1)
    y.append(h - 1)
    for i in range(1, len(x) - 1):
        y.append((1 - h) * y[i] + (h / 2) * (y[i - 1] - y[i]))
        #y.append(y[i] * ((1 + h / 2) / (1 - h / 2)))
    return y

def y_4p(h, x):
    y = []
    y.append(-1)
    y.append((h - 2) / (h + 2))
    for i in range(1, len(x) - 1):
        y.append(((3 - h) * y[i - 1] - 4 * h * y[i] ) / (3 + h))
        #y.append((y[i - 1] * (h + 3) + y[i] * h) / (3 - h))
    return y

def plot_graph(x, y):
    plt.plot(x, y, label='y(x)', marker=".", linestyle='')
    plt.xlabel('x')
    plt.ylabel('y')
    plt.title('График функции y(x)')
    plt.legend()
    plt.grid(True)
    plt.show()

start = 0
end = 10
h = 0.1
x = np.arange(start, end, h)
y_1por = y_1p(h, x)
y_2por = y_2p(h, x)
y_4por = y_4p(h, x)

plot_graph(x, y_1por)
plot_graph(x, y_2por)
plot_graph(x, y_4por)



