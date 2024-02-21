#include <stdlib.h>
#include <math.h>
#include <iostream>

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

double Mult (double** L, double** U, int i, int j) {
    double result = 0;

    int n = i;
    if (i > j) n = j;

    for (int k = 0; k < n; k++) {
        result += L[i][k] * U[k][j];
    }
    return result;
}

//LU-разложение существует только в том случае, когда матрица A обратима, 
//а все ведущие (угловые) главные миноры матрицы A невырождены
void LUdecomposition (double** A, double** L, double** U) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            U[i][j] = 0;
            L[i][j] = 0;
            L[i][i] = 1;
        }
    }

    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            if (i <= j) {
                U[i][j] = A[i][j] - Mult(L, U, i, j);
            } else {
                L[i][j] = (A[i][j] - Mult(L, U, i, j)) / U[j][j];
            }
        }
    }
}

//Ly = b
void StraightStroke(double** L, double* y, double* b) {
    for (int i = 0; i < N; i++) {
        y[i] = b[i];
        for (int j = 0; j < i; j++) {
            y[i] -= L[i][j] * y[j];
        }
        y[i] /= L[i][i];
    }
}

//Ux = y
void ReverseStroke(double** U, double* x, double* y) {
    for (int i = N - 1; i >= 0; i--) {
        x[i] = y[i];
        for (int j = i + 1; j < N; j++) {
            x[i] -= U[i][j] * x[j];
        }
        x[i] /= U[i][i];
    }
} 

void PrintMatrix(double ** matrix) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            printf("%f ", matrix[i][j]);
        }
        printf("\n");
    }
    printf("\n");
}

void PrintVector(double* vector) {
    for (int j = 0; j < N; j++) {
            printf("%f ", vector[j]);
    }
    printf("\n");
}


double* CalcLU(double** A, double* b) {
    double** L = (double**)malloc(N * sizeof(double*));
    double** U = (double**)malloc(N * sizeof(double*));
    for (int i = 0; i < N; i++) {
        L[i] = (double *)malloc(N * sizeof(double));
        U[i] = (double *)malloc(N * sizeof(double));
    }

    LUdecomposition(A, L, U);

    double* y = (double *)malloc(N * sizeof(double));
    double* x = (double *)malloc(N * sizeof(double));

    PrintMatrix(L);
    PrintMatrix(U);

    StraightStroke(L, y, b);
    ReverseStroke(U, x, y);

    PrintVector(x);

    for (int i = 0; i < N; i++) {
        free(L[i]);
        free(U[i]);
    }

    free(L);
    free(U);
    free(y);
    return x;    
}

/*int main() {
    double** A = (double**)malloc(N * sizeof(double*));
    for (int i = 0; i < N; i++) {
        A[i] = (double *)malloc(N * sizeof(double));
    }

    double* b = (double*)malloc(N * sizeof(double));

    Initialize(A, b);
    double* x = CalcLU(A, b);

    for (int i = 0; i < N; i++) {
        free(A[i]);
    }
    free(A);
    free(x);

    return 0;
}*/
