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

void ExtractDiagonals(double** matrix, double* mainDiagonal, double* upperDiagonal, double* lowerDiagonal) {
    for (int i = 0; i < N; i++) {
        mainDiagonal[i] = matrix[i][i]; // Главная диагональ
        if (i < N - 1) {
            upperDiagonal[i] = matrix[i][i + 1]; // Диагональ над главной
        }
        if (i > 0) {
            lowerDiagonal[i] = matrix[i][i - 1]; // Диагональ под главной
        }
    }
}

//для трехдиагональных матриц с диагональным преобладанием
double* TridiagonalMatrixAlgorithm(double** matrA, double* F) {
    double* alpha = new double[N];
    double* beta = new double[N];

    double* A = new double[N];
    double* B = new double[N];
    double* C = new double[N];
    ExtractDiagonals(matrA, B, C, A);

    double* x = new double[N];

    alpha[0] = -C[0] / B[0];
    //beta[0] = F[0] / alpha[0];
    beta[0] = F[0] / B[0];

    double denominator;

    for (int i = 1; i < N; i++) {
        denominator = B[i] + A[i] * alpha[i - 1];
        alpha[i] = -C[i] / denominator;
        beta[i] = (F[i] - (A[i] * beta[i - 1])) / denominator;
    }

    x[N - 1] = beta[N - 1];

    for (int i = N - 2; i >= 0; i--) {
        x[i] = alpha[i] * x[i + 1] + beta[i];
    }

    delete[] alpha;
    delete[] beta;
    delete[] A;
    delete[] B;
    delete[] C;

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
    double* x = TridiagonalMatrixAlgorithm(A, b);
    PrintVector(x);

    for (int i = 0; i < N; i++) {
        free(A[i]);
    }
    free(A);
    free(x);

    return 0;
}