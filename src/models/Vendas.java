
package models;

import java.util.Date;

public class Vendas {
    
    private int idVenda;
    private Date dataVenda;
    private String origemVenda;
    private String tipoPag;
    private String valorVenda;
    private String codPecas;
    private String nomeCliente;    
    private String observacao;
    private String entrega;
    private String status;
    private String pedidoId;

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEntrega() {
        return entrega;
    }

    public void setEntrega(String entrega) {
        this.entrega = entrega;
    }

    public int getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(int idVenda) {
        this.idVenda = idVenda;
    }

    public Date getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Date dataVenda) {
        this.dataVenda = dataVenda;
    }

    public String getOrigemVenda() {
        return origemVenda;
    }

    public void setOrigemVenda(String origemVenda) {
        this.origemVenda = origemVenda;
    }

    public String getTipoPag() {
        return tipoPag;
    }

    public void setTipoPag(String tipoPag) {
        this.tipoPag = tipoPag;
    }
    
    public String getValorVenda() {
        return valorVenda;
    }

    public void setValorVenda(String valorVenda) {
        this.valorVenda = valorVenda;
    }

    public String getCodPecas() {
        return codPecas;
    }

    public void setCodPecas(String codPecas) {
        this.codPecas = codPecas;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
}
