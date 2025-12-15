package model;

import java.sql.Date; // Usando LocalDateTime para representar a coluna DATETIME do SQL

public class Aluguel {

    private Long id_locacao;

    // O ID da Quadra é a chave estrangeira (quadra_idquadra)
    private Long idQuadra;

    // O ID do Cliente é a chave estrangeira (cliente_idcliente)
    private Long idCliente;

    // A data e hora da locação (data_locacao)
    private Date dataLocacao;

    private Double preco;

  

    // Construtor padrão (vazio)
    public Aluguel() {
    }

    // Construtor com todos os atributos

    // --- Getters e Setters ---

    public Aluguel(Long id_locacao, Long idQuadra, Long idCliente, Date dataLocacao, Double preco) {
        this.id_locacao = id_locacao;
        this.idQuadra = idQuadra;
        this.idCliente = idCliente;
        this.dataLocacao = dataLocacao;
        this.preco = preco;
    }

    public Long getId_locacao() {
        return id_locacao;
    }

    public void setId_locacao(Long id_locacao) {
        this.id_locacao = id_locacao;
    }

    public Long getIdQuadra() {
        return idQuadra;
    }

    public void setIdQuadra(Long idQuadra) {
        this.idQuadra = idQuadra;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Date getDataLocacao() {
        return dataLocacao;
    }

    public void setDataLocacao(Date dataLocacao) {
        this.dataLocacao = dataLocacao;
    }

      public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }
}