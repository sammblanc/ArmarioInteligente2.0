package br.com.unit.tokseg.armariointeligente.model;

public enum StatusNotification {
    PENDENTE("Pendente"),
    ENVIADA("Enviada"),
    ERRO("Erro"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusNotification(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
