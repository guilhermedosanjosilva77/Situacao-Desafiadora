import axios from 'axios';

const api = axios.create({
  baseURL: '/', // O CRA usa o proxy para redirecionar '/' para 'http://localhost:4567'
});

export default api;