#include <gtest/gtest.h>

#include "qr.cpp"
#include "lu.cpp"
#include "jacobi.cpp"
#include "seidel.cpp"
#include "tridiagonal.cpp"

typedef struct testCase {
    double matrix[3][3];
    double  vecB[3];
    double  vecX[3];
} testCase;

double epsilon = 1e-5;

double** fillMatrix(testCase tc) {
    double** matrix = AllocateMatrix();
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            matrix[i][j] = tc.matrix[i][j];
        }
    }

    return matrix;
}

double* fillVecB(testCase tc) {
    double* vec = new double[N];
    for (int i = 0; i < N; i++) {
        vec[i] = tc.vecB[i];
    }

    return vec;
}

// TEST FOR QR METHOD.
testCase testCasesQR[] = {
    {
        {
            {4.0, -1.0, -1.0},
            {-1.0, 4.0, -1.0},
            {-1.0, -1.0, 4.0}
        },
        {2, 2, 2},
        {1, 1, 1}
    },
    {
        {
            {2, 1.0, -1.0},
            {1.0, 3.0, 2.0},
            {1.0, -1.0, 3.0}
        },
        {8, 11, 6},
        {3.84, 1.56, 1.24}
    },
    {
        {
            {2, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    },
    {
        {
            {2, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    },
    {
        {
            {20.0, 20.0, 0.0},
            {15.0, 15.0, 5.0},
            {0.0, 1.0, 1.0}
        },
        {40, 35, 2},
        {1, 1, 1}
    }, 
    {
        {
            {0.0, 1.0, 2.0},
            {1.0, 0.0, 1.0},
            {2.0, 1.0, 0.0}
        },
        {2, 2, 2},
        {1, 0, 1}
    }

}; 

TEST(MyTest, TestQR) {
    for (int test = 0; test < (int)sizeof(testCasesQR)/ (int)sizeof(testCasesQR[0]); test++) {
        double** matrix = fillMatrix(testCasesQR[test]);
        double* vecB = fillVecB(testCasesQR[test]);

        double* x = QRDecomposition(matrix, vecB);

        for (int i = 0; i < N; i++) {
            ASSERT_NEAR(testCasesQR[test].vecX[i], x[i], epsilon);
        }

        delete[] x;
    }
}

testCase testCasesLU[] = {
    {
        {
            {4.0, -1.0, -1.0},
            {-1.0, 4.0, -1.0},
            {-1.0, -1.0, 4.0}
        },
        {2, 2, 2},
        {1, 1, 1}
    },
    {
        {
            {2, 1.0, -1.0},
            {1.0, 3.0, 2.0},
            {1.0, -1.0, 3.0}
        },
        {8, 11, 6},
        {3.84, 1.56, 1.24}
    },
    {
        {
            {2, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    },
    {
        {
            {2, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    }

}; 

TEST(MyTest, TestLU) {
    for (int test = 0; test < (int)sizeof(testCasesLU)/ (int)sizeof(testCasesLU[0]); test++) {
        double** matrix = fillMatrix(testCasesLU[test]);
        double* vecB = fillVecB(testCasesLU[test]);

        double* x = CalcLU(matrix, vecB);

        for (int i = 0; i < N; i++) {
            ASSERT_NEAR(testCasesLU[test].vecX[i], x[i], epsilon);
        }

        delete[] x;
    }
}

testCase testCasesJacobi[] = {
    {
        {
            {4.0, -1.0, -1.0},
            {-1.0, 4.0, -1.0},
            {-1.0, -1.0, 4.0}
        },
        {2, 2, 2},
        {1, 1, 1}
    },
    {
        {
            {2, 1.0, -1.0},
            {1.0, 3.0, 2.0},
            {1.0, -1.0, 3.0}
        },
        {8, 11, 6},
        {3.84, 1.56, 1.24}
    },
    {
        {
            {2, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    },
    {
        {
            {2, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    }

}; 

TEST(MyTest, TestJacobi) {
    for (int test = 0; test < (int)sizeof(testCasesJacobi)/ (int)sizeof(testCasesJacobi[0]); test++) {
        double** matrix = fillMatrix(testCasesJacobi[test]);
        double* vecB = fillVecB(testCasesJacobi[test]);

        double* x = Jacobi(matrix, vecB);

        for (int i = 0; i < N; i++) {
            ASSERT_NEAR(testCasesJacobi[test].vecX[i], x[i], epsilon);
        }

        delete[] x;
    }
}

testCase testCasesSeidel[] = {
    {
        {
            {4.0, -1.0, -1.0},
            {-1.0, 4.0, -1.0},
            {-1.0, -1.0, 4.0}
        },
        {2, 2, 2},
        {1, 1, 1}
    },
    {
        {
            {2, 1.0, -1.0},
            {1.0, 3.0, 2.0},
            {1.0, -1.0, 3.0}
        },
        {8, 11, 6},
        {3.84, 1.56, 1.24}
    },
    {
        {
            {2, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    },
    {
        {
            {2, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    },
    {
        {
            {20.0, 20.0, 0.0},
            {15.0, 15.0, 5.0},
            {0.0, 1.0, 1.0}
        },
        {40, 35, 2},
        {1, 1, 1}
    }

}; 

TEST(MyTest, TestSeidel) {
    for (int test = 0; test < (int)sizeof(testCasesSeidel)/ (int)sizeof(testCasesSeidel[0]); test++) {
        double** matrix = fillMatrix(testCasesSeidel[test]);
        double* vecB = fillVecB(testCasesSeidel[test]);

        double* x = Seidel(matrix, vecB);

        for (int i = 0; i < N; i++) {
            ASSERT_NEAR(testCasesSeidel[test].vecX[i], x[i], epsilon);
        }

        delete[] x;
    }
}

testCase testCasesTridiagonal[] = {
    {
        {
            {2.0, -1.0, 0.0},
            {-1.0, 2.0, -1.0},
            {0.0, -1.0, 2.0}
        },
        {2, 2, 2},
        {3, 4, 3}
    }, 
    {
        {
            {5.0, 1.0, 0.0},
            {1.0, 5.0, 1.0},
            {0.0, 1.0, 5.0}
        },
        {1, 1, 1},
        {4.0 / 23, 3.0 / 23, 4.0 / 23}
    }
}; 

TEST(MyTest, TestTridiagonal) {
    for (int test = 0; test < (int)sizeof(testCasesTridiagonal)/ (int)sizeof(testCasesTridiagonal[0]); test++) {
        double** matrix = fillMatrix(testCasesTridiagonal[test]);
        double* vecB = fillVecB(testCasesTridiagonal[test]);

        double* x = TridiagonalMatrixAlgorithm(matrix, vecB);
        for (int i = 0; i < 3; i++) {
            printf("%f ", x[i]);
        }
        printf("\n");

        for (int i = 0; i < N; i++) {
            ASSERT_NEAR(testCasesTridiagonal[test].vecX[i], x[i], epsilon);
        }

        delete[] x;
    }
}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
