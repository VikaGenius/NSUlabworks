#include <iostream>
#include <cmath>
#include <cstring>

#define N 3

// Функция для выделения памяти под двумерный массив
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

double VectorNorm(double* v) {
    double norm = 0.0;
    for (int i = 0; i < N; i++) {
        norm += v[i] * v[i];
    }
    return std::sqrt(norm);
}

void NormalizeVector(double* v) {
    double norm = VectorNorm(v);
    for (int i = 0; i < N; i++) {
        v[i] /= norm;
    }
}

// Функция для вычисления скалярного произведения двух векторов
double DotProduct(double* v1, double* v2) {
    double result = 0.0;
    for (int i = 0; i < N; i++) {
        result += v1[i] * v2[i];
    }
    return result;
}

void CopyMatrix(double** source, double** destination) {
    for (int i = 0; i < N; i++) {
        memcpy(destination[i], source[i], N * sizeof(double));
    }
}

void PrintVector(double* vec) {
    for (int i = 0; i < N; i++) {
        std::cout << vec[i] << " ";
    }
    std::cout << std::endl;
}

void PrintMatrix(double ** matrix) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            std::cout << matrix[i][j] << " ";
        }
        std::cout << std::endl;
    }
    std::cout << std::endl;
}

void GetColumn(double* column, double** Q, int num) {
    for (int i = 0; i < N; i++) {
        column[i] = Q[i][num];
    }
}

void Proj(double* projection, double* a, double* b) {
    double ab = DotProduct(a, b);
    double bb = DotProduct(b, b);
    for (int i = 0; i < N; i++) {
        projection[i] = (ab / bb) * b[i]; 
    }
}

void Sub(double* a, double* b) {
    for (int i = 0; i < N; i++) {
        a[i] -= b[i];
    }
}

void SetColumn(double** dst, double* src, int num) {
    for (int i = 0; i < N; i++) {
        dst[i][num] = src[i];
    }
}

void Orthogonalization(double** A, double** Q) {
    double* a = new double[N];
    double* b = new double[N];
    double* pr = new double[N];

    CopyMatrix(A, Q);

    for (int i = 1; i < N; i++) {
            GetColumn(a, Q, i);
            for (int j = 0; j < i; j++) {
                GetColumn(b, Q, j);
                Proj(pr, a, b);
                Sub(a, pr);
                SetColumn(Q, a, i); 
            }
    }


    for (int i = 0; i < N; i++) {
        GetColumn(a, Q, i);
        NormalizeVector(a);
        SetColumn(Q, a, i);
    } 

    delete[] b;
    delete[] a;
    delete[] pr;
}

void TransposeMatrix(double** matrix, double** tmatrix) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            tmatrix[j][i] = matrix[i][j];
        }
    }
}

void MultMatrix(double** A, double** B, double** C) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            C[i][j] = 0.0;
            for (int k = 0; k < N; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        }
    }
}

void CheckAnswer(double** A, double* x, double* b) {
    double* new_b = new double[N];
    for (int i = 0; i < N; i++) {
        new_b[i] = 0;
    }

    for(int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            new_b[i] += A[i][j] * x[j];
        }
    }
    printf("Проверка ответа, A * x: \n");
    PrintVector(new_b);
    delete[] new_b;
}

void SolveLinearSystem(double** tQ, double** R, double* b, double* x) {
    // Вычисляем Q^T * b
    double* Qt_b = new double[N];
    for (int i = 0; i < N; i++) {
        Qt_b[i] = 0.0;
        for (int j = 0; j < N; j++) {
            Qt_b[i] += tQ[i][j] * b[j];
        }
    }

    // Решаем систему R * x = Q^T * b методом обратной подстановки
    for (int i = N - 1; i >= 0; i--) {
        x[i] = Qt_b[i];
        for (int j = i + 1; j < N; j++) {
            x[i] -= R[i][j] * x[j];
        }
        x[i] /= R[i][i];
    }

    delete[] Qt_b;
}

// Функция для выполнения QR-разложения
double* QRDecomposition(double** A, double* b) {
    double** Q = AllocateMatrix();
    double** R = AllocateMatrix();

    Orthogonalization(A, Q);
    double** tQ = AllocateMatrix();
    TransposeMatrix(Q, tQ);

    MultMatrix(tQ, A, R);

    //PrintMatrix(Q);
    //PrintMatrix(R);

    double* x = new double[N];
    SolveLinearSystem(tQ, R, b, x);
    
    DeallocateMatrix(tQ);
    DeallocateMatrix(Q);
    DeallocateMatrix(R);
    return x;
}


int main() {
    // Выделение памяти для матрицы A, Q и R
    double** A = AllocateMatrix();

    // Ввод матрицы A
    std::cout << "Введите элементы матрицы A: \n";
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            std::cin >> A[i][j];
        }
    }

    double* b = new double[N];
    std::cout << "Введите элементы вектора b: \n";
    for (int i = 0; i < N; i++) {
        std::cin >> b[i];
    }


    // Выполнение QR-разложения
    double* x = QRDecomposition(A, b);
    printf("Полученный вектор х: \n");
    PrintVector(x);
    printf("\n");
    CheckAnswer(A, x, b);


    DeallocateMatrix(A);
    delete[] b;
    delete[] x;
    return 0;
}
