#include <iostream>
#include <math.h>
#define N 3

const double eps = 0.0000001; ///< желаемая точность 

void Initialize(double** matrix, double* vector) {
    printf("Введите элементы матрицы A размером %dx%d:\n", N, N);
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            scanf("%lf", &matrix[i][j]);
        }
    }

    printf("Введите элементы вектора b длиною %d:\n", N);
    for (int j = 0; j < N; j++) {
            scanf("%lf", &vector[j]);
    }

}

double** AllocateMatrix() {
    double** matrix = new double*[N];
    for (int i = 0; i < N; i++) {
        matrix[i] = new double[N];
    }
    return matrix;
}

void DeallocateMatrix(double** matrix) {
    for (int i = 0; i < N; i++) {
        delete[] matrix[i];
    }
    delete[] matrix;
}

void PrintVector(double* vec) {
    for (int i = 0; i < N; i++) {
        std::cout << vec[i] << " ";
    }
    std::cout << std::endl;
}

//сходится при ||A|| < 1
double* Jacobi(double** A, double* b) {
    double* x = new double[N];
	double* TempX = new double[N];
	double norm; // норма, определяемая как наибольшая разность компонент столбца иксов соседних итераций.

	do {
		for (int i = 0; i < N; i++) {
			TempX[i] = b[i];
			for (int j = 0; j < N; j++) {
				if (i != j)
					TempX[i] -= A[i][j] * x[j];
			}
			TempX[i] /= A[i][i];
		}

        norm = fabs(x[0] - TempX[0]);

		for (int i = 0; i < N; i++) {
			if (fabs(x[i] - TempX[i]) > norm)
				norm = fabs(x[i] - TempX[i]);
			x[i] = TempX[i];
		}

	} while (norm > eps);

	delete[] TempX;

    return x;
}

int main() {
    double** A = AllocateMatrix();
    double* b = new double[N];
    
    Initialize(A, b);
   
    double* x = Jacobi(A, b);
    PrintVector(x);

    delete[] b;
    delete[] x;
    DeallocateMatrix(A);

    return 0;
}