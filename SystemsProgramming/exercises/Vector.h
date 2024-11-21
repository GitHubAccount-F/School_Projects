/*
 Copyright 2024
 Fardeen Azizi
 fardeena@uw.edu
*/
#ifndef VECTOR_H_
#define VECTOR_H_
#include <iostream>

namespace vector333 {
class Vector {
 public:
    // default constructor
    Vector();
    // regular constructor
    Vector(const double& x, const double& y, const double& z);
    // copy constructor
    Vector(const Vector& copy);
    // destructor
    ~Vector();
    // Override the "=" operator the change this instance
    Vector& operator=(const Vector& rhs);
    // Override the += operator to change this instance
    Vector& operator+=(const Vector& rhs);
    // Override the -= operator
    Vector& operator-=(const Vector& rhs);
    // Get x value of vector
    double get_x() const { return x_; }
    // Get y value of vector
    double get_y() const { return y_; }
    // Get z value of vector
    double get_z() const { return z_; }

 private:
  // data member
  double x_, y_, z_;
};
// non member functions
// Input: first - a vector, second - a vector
// Output: Returns the dot product of the two vectors
double operator*(const Vector& first, const Vector& second);
// Input: v - vector, scalar - any value that is a double
// Output: Returns the product between the vector v and the scalar
// This one is for vector * scalar
Vector operator*(const Vector& v, const double& scalar);
// THis one is for scalar * vector
Vector operator*(const double& scalar, const Vector& v);
// Input: lhs - vector, rhs - vector
// Output: Returns a new vector that is the result of adding these two vectors
Vector operator+(const Vector& lhs, const Vector& rhs);
// Input: lhs - vector, rhs - vector
// Output: Returns a new vector that is the result of subtracting these
// two vectors
Vector operator-(const Vector& lhs, const Vector& rhs);
// Overrides the << to print out content of vector properly in
// the form of (x,y,z)
std::ostream& operator<<(std::ostream& out, const Vector& a);
}  // namespace vector333

#endif  // VECTOR_H_
