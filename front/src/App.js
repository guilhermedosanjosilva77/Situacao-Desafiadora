// src/App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Home from './components/Home';
import Clientes from './components/Clientes';
import Alugueis from './components/Alugueis';
import './App.css'; // Importa seu CSS

function App() {
  return (
    <Router>
      <nav className="navbar">
        <h1>⚽ FutManager</h1>
        <div className="nav-links">
          <Link to="/">Início</Link>
          <Link to="/clientes">Clientes</Link>
          <Link to="/alugueis">Aluguéis</Link>
        </div>
      </nav>

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/clientes" element={<Clientes />} />
        <Route path="/alugueis" element={<Alugueis />} />
      </Routes>
    </Router>
  );
}

export default App;