/*
 Copyright 2024
 Fardeen Azizi
 fardeena@uw.edu
*/
#include <iostream>
#include <sstream>
#include <string>

#include "Vector.h"

namespace vector333 {
  // Created using initalization list
  Vector::Vector(): x_(0), y_(0), z_(0) {}
  Vector::Vector(const double& x, const double& y, const double& z):
    x_(x), y_(y), z_(z) {}
  Vector::Vector(const Vector& copy):
    x_(copy.x_), y_(copy.y_), z_(copy.z_) {}
  // destructor
  Vector::~Vector() {}
  Vector& Vector::operator=(const Vector& rhs) {  // assignment
    if (this != &rhs) {
      this->x_ = rhs.x_;
      this->y_ = rhs.y_;
      this->z_ = rhs.z_;
    }
    return *this;
  }
  Vector& Vector::operator+=(const Vector& rhs) {  // assignment +=
    this->x_ += rhs.x_;
    this->y_ += rhs.y_;
    this->z_ += rhs.z_;
    return *this;
  }
  Vector& Vector::operator-=(const Vector& rhs) {  // assignment -=
    this->x_ -= rhs.x_;
    this->y_ -= rhs.y_;
    this->z_ -= rhs.z_;
    return *this;
  }
  Vector operator+(const Vector& lhs, const Vector& rhs) {  // assignment +
    Vector temp = lhs;
    temp+=rhs;
    return temp;
  }
  Vector operator-(const Vector& lhs, const Vector& rhs) {  // assignment -
    Vector temp = lhs;
    temp-=rhs;
    return temp;
  }
  // assignment dot product
  double operator*(const Vector& first, const Vector& second) {
    double x = first.get_x() * second.get_x();
    double y = first.get_y() * second.get_y();
    double z = first.get_z() * second.get_z();
    return x + y + z;
  }
  Vector operator*(const Vector& v, const double& scalar) {
    return Vector(v.get_x() * scalar, v.get_y() * scalar, v.get_z() * scalar);
  }
  Vector operator*(const double& scalar, const Vector& v) {
    return Vector(v.get_x() * scalar, v.get_y() * scalar, v.get_z() * scalar);
  }
  std::ostream& operator<<(std::ostream& out, const Vector& a) {
    out << "(" << a.get_x() << "," << a.get_y() << "," << a.get_z() << ")";
    return out;
  }
}  // namespace vector333
