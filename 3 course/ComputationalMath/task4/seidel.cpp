#include <iostream>
#include <cmath>
#include <cstring>

#define N 3

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

double* Seidel(double** A, double* b) {
    double epsilon = 1e-6; 

    double* tmpX = new double[N];
    double* x = new double[N];
    double sum1, sum2;
    bool converge = false;

    while (!converge) {
        memcpy(x, tmpX, N * sizeof(double));
        for (int i = 0; i < N; i++) {
            sum1 = 0.0, sum2 = 0.0;
            for (int j = 0; j < i; j++) {
                sum1 += A[i][j] * x[j];
            }
            for (int j = i + 1; j < N; j++) {
                sum2 += A[i][j] * tmpX[j];
            }
            x[i] = (b[i] - sum1 - sum2) / A[i][i];
        }

        double norm = 0.0;
        for (int i = 0; i < N; i++) {
            norm += std::abs(x[i] - tmpX[i]);
        }

        converge = norm <= epsilon;
        memcpy(tmpX, x, N * sizeof(double));
    }

    delete[] tmpX;
    return x;
}

void PrintVector(double* vector) {
    for (int j = 0; j < N; j++) {
            printf("%f ", vector[j]);
    }
    printf("\n");
}

int main() {
    double** A = (double**)malloc(N * sizeof(double*));
    for (int i = 0; i < N; i++) {
        A[i] = (double *)malloc(N * sizeof(double));
    }

    double* b = (double*)malloc(N * sizeof(double));

    Initialize(A, b);
    double* x = Seidel(A, b);
    PrintVector(x);

    for (int i = 0; i < N; i++) {
        free(A[i]);
    }
    free(A);
    free(x);

    return 0;
}