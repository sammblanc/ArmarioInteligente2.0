package br.com.unit.tokseg.armariointeligente.dto;

public class ArmarioDTO {

    // Note que não há campo "id" aqui
    private String identificacao;
    private String localizacao;
    private String descricao;
    private Boolean ativo = true;
    private Long condominioId; // Apenas o ID do condomínio

    // Getters e Setters
    public String getIdentificacao() {
        return identificacao;
    }
    public void setIdentificacao(String identificacao) {
        this.identificacao = identificacao;
    }
    public String getLocalizacao() {
        return localizacao;
    }
    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public Boolean getAtivo() {
        return ativo;
    }
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    public Long getCondominioId() {
        return condominioId;
    }
    public void setCondominioId(Long condominioId) {
        this.condominioId = condominioId;
    }
}