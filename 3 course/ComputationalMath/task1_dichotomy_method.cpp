#include <stdlib.h>
#include <math.h>
#include <iostream>

struct Consts {
    double a;
    double b;
    double c;
    double d;
};

double Discriminant(const double& a, const double& b, const double& c) {
    return (b * b - 4 * a * c);
}

double Equation(const Consts& parameters, const double& x) {
    return (parameters.a * x * x * x + parameters.b * x * x + parameters.c * x + parameters.d);
}

//взятие производной, получение новых a b c
void Derivative(double& a, double& b, double& c) { 
    a *= 3;
    b *= 2;
    c *= 1;
} 

//функция определяет знак значения уравнения в точке
int SgnEq(const double& val, const Consts& parameters) {
    return (0 < Equation(parameters, val)) - (Equation(parameters, val) < 0);
}

void Algorithm (double& left, double& right, const Consts& parameters) {
    if (SgnEq(left, parameters) == SgnEq(right, parameters)) {
        //std::cout << "Error, the borders are incorrect" << std::endl; //вероятно мнимый корень
        return;
    }

    double mid = 0.0;
    while (right - left >= 0.0000000001) {
        mid = (left + right) / 2;
        if (SgnEq(mid, parameters) != 0 && SgnEq(left, parameters) == SgnEq(mid, parameters)) {
            left = mid;
        }
        if (SgnEq(mid, parameters) != 0 && SgnEq(right, parameters) == SgnEq(mid, parameters)) {
            right = mid;
        }
        else {
            if (SgnEq(mid, parameters) == 0) {
                std::cout << mid << std::endl;
                return;
            }
        }
    }
    
    if (left == right) {
        std::cout << left << std::endl;
    } else {
        std::cout << "[";
        std::cout << left;
        std::cout << "; ";
        std::cout << right;
        std::cout << "]" << std::endl;
    }
}

void GetRoot(double a, double b, const double& discriminant, double& x1, double& x2) {
    x1 = (- b - std::sqrt(discriminant)) / (2 * a);
    x2 = (- b + std::sqrt(discriminant)) / (2 * a);
    
    if (x2 < x1) { std::swap<double>(x1, x2); }
}

void DichotomyMethod(const Consts& parameters) {
    double new_a = parameters.a;
    double new_b = parameters.b;
    double new_c = parameters.c;
    Derivative(new_a, new_b, new_c); //новые параметры - а, б и с из производной :)

    double discr = Discriminant(new_a, new_b, new_c);

    double delta = 2147483640.0;
    double delta1 = -1 * delta;

    if (discr > 0) {
        double alpha = 0, beta = 0;
        GetRoot(new_a, new_b, discr, alpha, beta);

        /*if (SgnEq(alpha, parameters) == 0) { //проверка экстремумов
            std::cout << alpha << std::endl;
        }
        if (SgnEq(beta, parameters) == 0) {
            std::cout << beta << std::endl;
        }*/

        Algorithm(delta1, alpha, parameters);
        Algorithm(alpha, beta, parameters);
        Algorithm(beta, delta, parameters);

    } else if (discr == 0) {
        double gamma = (- new_b) / (2 * new_a);

        if (SgnEq(delta, parameters) != SgnEq(gamma, parameters)) {
            Algorithm(delta1, gamma, parameters);
        } else {
            Algorithm(gamma, delta, parameters);
        }

    } else {
        Algorithm(delta1, delta, parameters);
    }

}

int main() {
    /* std::cout << "example 1: " << std::endl; 
    struct Consts parameters;
    parameters.a = 1.0;
    parameters.b = 2.0;
    parameters.c = -1.0;
    parameters.d = -2.0;
    DichotomyMethod(parameters);

    std::cout << std::endl;

    std::cout << "example 2: " << std::endl; 
    struct Consts parameters1;
    parameters1.a = 1.0;
    parameters1.b = -100.0;
    parameters1.c = 1.0;
    parameters1.d = -100.0;
    DichotomyMethod(parameters1);

    std::cout << std::endl;

    std::cout << "example 3: " << std::endl; 
    struct Consts parameters2;
    parameters2.a = 1.0;
    parameters2.b = 2.0;
    parameters2.c = 3.0;
    parameters2.d = 4.0;
    DichotomyMethod(parameters2);

    std::cout << "example 4: " << std::endl; 
    struct Consts parameters3;
    parameters3.a = 1.0;
    parameters3.b = -0.81;
    parameters3.c = -0.991;
    parameters3.d = 0.819;
    DichotomyMethod(parameters3); */

    return 0;
}