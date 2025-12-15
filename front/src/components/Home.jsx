import React from 'react';
import { Link } from 'react-router-dom';

function Home() {
  return (
    <div className="welcome-hero">
      <h1>Bem-vindo ao <span>FutManager</span></h1>
      <p>O sistema definitivo para gerenciar suas quadras de futebol.</p>
      <div style={{ marginTop: '2rem' }}>
        <Link to="/clientes">
          <button className="btn btn-primary" style={{ marginRight: '1rem' }}>Gerenciar Clientes</button>
        </Link>
        <Link to="/alugueis">
          <button className="btn btn-primary">Novo Aluguel</button>
        </Link>
      </div>
    </div>
  );
}

export default Home;