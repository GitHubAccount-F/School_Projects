import React, { Component } from 'react';
import { Link } from 'react-router-dom';

export default class Navbar extends Component {

  render() {
    return (
      <nav className="navbar navbar-dark bg-dark navbar-expand-lg">
        <div className="collpase navbar-collapse">
        <ul className="navbar-nav mx-auto ">
          <li className="navbar-item">
          <Link to="/" className="nav-link">Home</Link>
          </li>
          <li className="navbar-item">
          <Link to="/analog" className="nav-link">Analog</Link>
          </li>
          <li className="navbar-item">
          <Link to="/creepypasta" className="nav-link">Creepypasta</Link>
          </li>
          <li className="navbar-item">
          <Link to="/login" className="nav-link">Login</Link>
          </li>
          <li className="navbar-item">
          <Link to="signup" className="nav-link">Sign up</Link>
          </li>
        </ul>
        </div>
      </nav>
    );
  }
}