import React, { useState, useEffect } from 'react';
import api from '../api';

// **********************************************
// CONSTANTE DA REGRA DE NEGÓCIO (DATA MÍNIMA)
// **********************************************
const DATA_MINIMA_PERMITIDA = '2025-12-16'; 

// Helper para formatar data (Visualização: DD/MM/YYYY)
const formatDbDate = (dateString) => {
    if (!dateString) return '';
    
    const isoDatePart = dateString.split('T')[0];
    
    if (isoDatePart.match(/^\d{4}-\d{2}-\d{2}$/)) {
        const [year, month, day] = isoDatePart.split('-');
        return `${day}/${month}/${year}`;
    }
    return isoDatePart;
};

// Helper para input type="date" (Formato necessário: YYYY-MM-DD)
const formatDateForInput = (dateString) => {
    if (!dateString) return '';
    return dateString.split('T')[0];
};


function Alugueis() {
  const [clientes, setClientes] = useState([]);
  const [alugueis, setAlugueis] = useState([]);
  
  const tiposQuadra = [
    { id: 1, cobertura: 'Saibro', tamanho: 'Oficial Simples', preco: 75.00 },
    { id: 2, cobertura: 'Sintético', tamanho: 'Oficial Dupla', preco: 90.00 },
    { id: 3, cobertura: 'Sintético', tamanho: 'Oficial Dupla', preco: 90.00 }
  ];

  const [form, setForm] = useState({
    idCliente: '',
    idQuadra: '',
    // Inicializa a dataLocacao com a data mínima (YYYY-MM-DD)
    dataLocacao: DATA_MINIMA_PERMITIDA, 
    preco: 0.00
  });

  const [editingAluguel, setEditingAluguel] = useState(null);
  const [precoOriginal, setPrecoOriginal] = useState(0.00);
  const TAXA_EXTRA_DATA = 50.00; 

  useEffect(() => {
    carregarDados();
  }, []);

  // **********************************************
  // REVISÃO: FUNÇÃO DE CARREGAMENTO DE DADOS
  // **********************************************
  const carregarDados = async () => {
    try {
      // O Promise.all garante que ambas as requisições sejam feitas em paralelo
      const [respClientes, respAlugueis] = await Promise.all([
        api.get('/cliente'),
        api.get('/Aluguel')
      ]);
      
      // Os clientes devem ser carregados aqui
      setClientes(respClientes.data);
      setAlugueis(respAlugueis.data);
      
      // Se a página for carregada sem um aluguel sendo editado, 
      // garante que o formulário está limpo e com a data mínima
      if (!editingAluguel) {
        setForm(prevForm => ({ 
            ...prevForm, 
            idCliente: '', 
            idQuadra: '', 
            dataLocacao: DATA_MINIMA_PERMITIDA,
            preco: 0.00
        }));
      }

    } catch (error) {
      console.error("Erro ao carregar dados. Verifique a API e o Proxy.", error);
      // Se houver erro, zera os estados para evitar dados incompletos
      setClientes([]); 
      setAlugueis([]);
    }
  };

  const setAluguelParaEdicao = (aluguel) => {
    setEditingAluguel(aluguel);
    setPrecoOriginal(aluguel.preco); 

    setForm({
      idCliente: aluguel.idCliente.toString(), 
      idQuadra: aluguel.idQuadra.toString(),
      dataLocacao: formatDateForInput(aluguel.dataLocacao),
      preco: aluguel.preco 
    });
  };

  const cancelarEdicao = () => {
    setEditingAluguel(null);
    setPrecoOriginal(0.00);
    setForm({ idCliente: '', idQuadra: '', dataLocacao: DATA_MINIMA_PERMITIDA, preco: 0.00 }); 
  };
  
  const handleQuadraChange = (e) => {
    const quadraId = parseInt(e.target.value);
    const quadraSelecionada = tiposQuadra.find(q => q.id === quadraId);
    const novoPrecoBase = quadraSelecionada ? quadraSelecionada.preco : 0.00;

    if (editingAluguel) {
         const precoFinal = quadraSelecionada ? novoPrecoBase : precoOriginal;
         
         setForm({
            ...form,
            idQuadra: quadraId,
            preco: precoFinal
         });

    } else {
        setForm({
            ...form,
            idQuadra: quadraId,
            preco: novoPrecoBase
        });
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    let novoForm = { ...form, [name]: value };
    let precoCalculado = parseFloat(novoForm.preco);

    // LÓGICA DA REGRA DE NEGÓCIO: ACRESCIMO DE R$ 50,00 SE MUDAR A DATA
    if (editingAluguel && name === 'dataLocacao') {
        const dataOriginal = formatDateForInput(editingAluguel.dataLocacao); 

        if (value !== dataOriginal) {
            if (precoOriginal === parseFloat(novoForm.preco)) {
                 precoCalculado = precoOriginal + TAXA_EXTRA_DATA;
                 alert(`Data alterada! Acréscimo de R$ ${TAXA_EXTRA_DATA.toFixed(2)} aplicado ao preço.`);
            }
        } else {
            precoCalculado = precoOriginal;
        }
        
        novoForm.preco = precoCalculado.toFixed(2);
    }
    
    setForm(novoForm);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Verificação adicional para garantir que a data não está no passado (segurança)
    if (form.dataLocacao < DATA_MINIMA_PERMITIDA) {
        alert(`A data mínima permitida é ${formatDbDate(DATA_MINIMA_PERMITIDA)}. Por favor, selecione uma data válida.`);
        return;
    }

    const payload = {
      idQuadra: parseInt(form.idQuadra),
      idCliente: parseInt(form.idCliente),
      dataLocacao: form.dataLocacao, 
      preco: parseFloat(form.preco) 
    };

    try {
      if (editingAluguel) {
        const id = editingAluguel.id_locacao; 
        await api.put(`/Aluguel/${id}`, payload);
        alert(`Reserva ${id} atualizada com sucesso!`);
      } else {
        await api.post('/Aluguel', payload);
        alert('Reserva realizada com sucesso!');
      }

      cancelarEdicao();
      carregarDados(); 
    } catch (error) {
      console.error(error);
      const msg = error.response?.data?.mensagem || 'Erro ao salvar locação.';
      alert(msg);
    }
  };

  const deletarAluguel = async (id) => {
    if(!window.confirm("Cancelar esta reserva?")) return;
    try {
      await api.delete(`/Aluguel/${id}`); 
      carregarDados();
    } catch (error) {
      alert('Erro ao cancelar reserva.');
    }
  }

  const getNomeQuadra = (id) => {
    const q = tiposQuadra.find(x => x.id === parseInt(id));
    return q ? `ID ${q.id} (${q.cobertura} - ${q.tamanho})` : `Quadra ID ${id} (Não listada)`;
  };

  return (
    <div className="container">
      <div className="card">
        <h2>{editingAluguel ? `Editar Reserva: ${editingAluguel.id_locacao}` : 'Novo Aluguel'}</h2>
        
        <form onSubmit={handleSubmit}>
          
          <div className="form-group">
            <label>Cliente:</label>
            {/* O select agora carrega a lista de clientes corretamente */}
            <select 
              name="idCliente" 
              value={form.idCliente} 
              onChange={handleChange} 
              required 
              disabled={!!editingAluguel} 
            >
              <option value="">Selecione um cliente...</option>
              {clientes.map(c => (
                <option key={c.ID || c.id_cliente} value={c.ID || c.id_cliente}>
                  {c.nome} (ID: {c.ID || c.id_cliente})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Tipo de Quadra:</label>
            <select name="idQuadra" value={form.idQuadra} onChange={handleQuadraChange} required>
              <option value="">Selecione a quadra...</option>
              {tiposQuadra.map(q => (
                <option key={q.id} value={q.id}>
                  {q.cobertura} / {q.tamanho} - R$ {q.preco.toFixed(2)}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Data da Locação:</label>
            <input 
              type="date" 
              name="dataLocacao" 
              value={form.dataLocacao} 
              onChange={handleChange} 
              required 
              min={DATA_MINIMA_PERMITIDA} 
            />
          </div>

          <div className="form-group">
            <label>Valor Final da Locação (R$):</label>
            <input 
                type="number" 
                name="preco"
                value={form.preco} 
                onChange={handleChange} 
                step="0.01"
                required
                style={{ backgroundColor: '#fff', fontWeight: 'bold' }} 
            />
             {editingAluguel && form.preco > precoOriginal && (
                <small style={{ color: 'red' }}>Acréscimo de R$ {TAXA_EXTRA_DATA.toFixed(2)} por mudança de data.</small>
             )}
          </div>

          <button type="submit" className="btn btn-primary">
            {editingAluguel ? 'Atualizar Reserva' : 'Confirmar Reserva'}
          </button>
          
          {editingAluguel && (
            <button type="button" onClick={cancelarEdicao} className="btn btn-secondary" style={{ marginLeft: '10px' }}>
              Cancelar Edição
            </button>
          )}
        </form>
      </div>

      <div className="card">
        <h2>Reservas Ativas</h2>
        <table>
          <thead>
            <tr>
              <th>ID Locação</th>
              <th>Cliente (ID)</th>
              <th>Quadra</th>
              <th>Data</th>
              <th>Preço</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {/* Garante que a lista não quebre se alugueis for null/undefined */}
            {alugueis && alugueis.map((aluguel) => (
              <tr key={aluguel.id_locacao}>
                <td>{aluguel.id_locacao}</td>
                <td>{aluguel.idCliente}</td>
                <td>{getNomeQuadra(aluguel.idQuadra)}</td>
                <td>{formatDbDate(aluguel.dataLocacao)}</td> 
                <td>R$ {parseFloat(aluguel.preco).toFixed(2)}</td>
                <td>
                  <button className="btn btn-info" onClick={() => setAluguelParaEdicao(aluguel)}>
                    Editar
                  </button>
                  <button className="btn btn-danger" onClick={() => deletarAluguel(aluguel.id_locacao)}>
                    Cancelar
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

export default Alugueis;