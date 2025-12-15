package model;

public class Quadra {
    private Long id;
    private String cobertura;
    private String tamanho;
    private double preco;

    
    public Quadra() {
    }

    
    public Quadra(Long id, String cobertura, String tamanho, double preco) {
        this.id = id;
        this.cobertura = cobertura;
        this.tamanho = tamanho;
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCobertura() {
        return cobertura;
    }
    public void setCobertura(String cobertura) {
        this.cobertura = cobertura;
    }
    public String getTamanho() {
        return tamanho;
    }
    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }
    public double getPreco() {
        return preco;
    }
    public void setPreco(double preco) {
        this.preco = preco;
    }

    

    
}
