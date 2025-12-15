import React, { useState, useEffect } from 'react';
import api from '../api';

function Clientes() {
  const [clientes, setClientes] = useState([]);
  const [novoCliente, setNovoCliente] = useState({ nome: '', telefone: '' });

  // Buscar clientes ao carregar
  useEffect(() => {
    carregarClientes();
  }, []);

  const carregarClientes = async () => {
    try {
      const response = await api.get('/cliente');
      setClientes(response.data);
    } catch (error) {
      console.error("Erro ao buscar clientes", error);
    }
  };

  const handleChange = (e) => {
    setNovoCliente({ ...novoCliente, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/cliente', novoCliente);
      alert('Cliente cadastrado com sucesso!');
      setNovoCliente({ nome: '', telefone: '' });
      carregarClientes();
    } catch (error) {
      alert('Erro ao cadastrar cliente.');
    }
  };

  const deletarCliente = async (id) => {
    if(!window.confirm("Tem certeza que deseja excluir?")) return;
    try {
      await api.delete(`/cliente/${id}`);
      carregarClientes();
    } catch (error) {
      alert('Erro ao deletar (Cliente pode ter alugueis vinculados).');
    }
  };

  return (
    <div className="container">
      <div className="card">
        <h2>Cadastrar Cliente</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Nome Completo:</label>
            <input 
              type="text" name="nome" required
              value={novoCliente.nome} onChange={handleChange} 
              placeholder="Ex: João Silva"
            />
          </div>
          <div className="form-group">
            <label>Telefone:</label>
            <input 
              type="text" name="telefone" required
              value={novoCliente.telefone} onChange={handleChange} 
              placeholder="Ex: (11) 99999-9999"
            />
          </div>
          <button type="submit" className="btn btn-primary">Salvar Cliente</button>
        </form>
      </div>

      <div className="card">
        <h2>Lista de Clientes</h2>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Telefone</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {clientes.map((cliente) => (
              <tr key={cliente.ID || cliente.id_cliente}>
                <td>{cliente.ID || cliente.id_cliente}</td>
                <td>{cliente.nome}</td>
                <td>{cliente.telefone}</td>
                <td>
                  <button 
                    className="btn btn-danger" 
                    onClick={() => deletarCliente(cliente.ID || cliente.id_cliente)}
                  >
                    Excluir
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Clientes;