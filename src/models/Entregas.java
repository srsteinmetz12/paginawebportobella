package models;

import java.util.Date;

public class Entregas {
    
    private int id;
    private int idvenda;
    private Date datavenda;
    private Date dataentrega;
    private String nomecli;
    private String codpeca;
    private Double valorfrete;
    private Boolean fretepago;
    private Boolean entregue;
    private String status;
    private String tipoentrega;
    private static String canal;
    private String pedidoId;

    public int getIdvenda() {
        return idvenda;
    }

    public void setIdvenda(int idvenda) {
        this.idvenda = idvenda;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getTipoentrega() {
        return tipoentrega;
    }

    public void setTipoentrega(String tipoentrega) {
        this.tipoentrega = tipoentrega;
    }
    
    public Integer getIdVenda(){
        return idvenda;
    }
    
    public void setIdVenda (Integer idvenda){
        this.idvenda = idvenda;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDatavenda() {
        return datavenda;
    }

    public void setDatavenda(Date datavenda) {
        this.datavenda = datavenda;
    }
    
    public Date getDataentrega() {
        return dataentrega;
    }

    public void setDataentrega(Date dataentrega) {
        this.dataentrega = dataentrega;
    }

    public String getNomecli() {
        return nomecli;
    }

    public void setNomecli(String nomecli) {
        this.nomecli = nomecli;
    }

    public String getCodpeca() {
        return codpeca;
    }

    public void setCodpeca(String codpeca) {
        this.codpeca = codpeca;
    }

    public Double getValorfrete() {
        return valorfrete;
    }

    public void setValorfrete(Double valorfrete) {
        this.valorfrete = valorfrete;
    }

    public Boolean getFretepago() {
        return fretepago;
    }

    public void setFretepago(Boolean fretepago) {
        this.fretepago = fretepago;
    }

    public Boolean getEntregue() {
        return entregue;
    }

    public void setEntregue(Boolean entregue) {
        this.entregue = entregue;
    }
    
}
