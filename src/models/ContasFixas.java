package models;

import java.math.BigDecimal;
import java.util.Date;

public class ContasFixas {
    
    private int id;
    private String descricao;
    private String credor;
    private BigDecimal valor;
    private int vencimento; // Armazena apenas o dia físico de vencimento (ex: 5, 10, 15)
    private boolean pago;
    private Date dataPagamento;
    private String mesComp;

    // Métodos Getters e Setters para encapsulamento e transporte seguro de dados [links: 10]
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCredor() {
        return credor;
    }

    public void setCredor(String credor) {
        this.credor = credor;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public int getVencimento() {
        return vencimento;
    }

    // 🔥 HELPER CONTÁBIL: Retorna o dia de vencimento formatado como texto puro (ex: "Dia 05")
    // Facilita a exibição direta na primeira coluna (DATA) da sua JTable da imagem sem bugar!
    public String getDataVencimentoTexto() {
        return String.format("Dia %02d", vencimento);
    }

    public void setVencimento(int vencimento) {
        this.vencimento = vencimento;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getMesComp() {
        return mesComp;
    }

    public void setMesComp(String mesComp) {
        this.mesComp = mesComp;
    }
}
