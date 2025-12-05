package model;

public class Cliente {
    private Long ID;
    private String nome;
    private String telefone;

    public Cliente() {
    }
public Cliente(Long ID, String nome, String telefone) {
    this.ID = ID;
    this.nome = nome;
    this.telefone = telefone;
}

   public Long getID() {
        return ID;
    }
    public void setID(Long iD) {
        ID = iD;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    

}
