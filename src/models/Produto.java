
package models;

import java.util.Date;

public class Produto {
    
    private String tipoitem;
    private String codforn;
    private String ultimoLote;
    private String nomeforn;
    private Date data;
    private String itemdescricao;
    private String codpeca;
    private String marca;
    private String tamanho;
    private double valorpago;
    private double precosugerido;
    private double lucroestimado;
    private int percentlucro;
    private String observacao;
    private byte [] imagem;
    private String status;
    private Date datavenda;

    public Date getDatavenda() {
        return datavenda;
    }

    public void setDatavenda(Date datavenda) {
        this.datavenda = datavenda;
    }  

    public byte[] getImagem() {
        return imagem;
    }

    public void setImagem(byte[] imagem) {
        this.imagem = imagem;
    }  
   
    public String getTipoitem() {
        return tipoitem;
    }

    public void setTipoitem(String tipoitem) {
        this.tipoitem = tipoitem;
    }

    public String getCodforn() {
        return codforn;
    }

    public void setCodforn(String codforn) {
        this.codforn = codforn;
    }
       
    public String getUltimoLote() {
        return ultimoLote;
    }

    public void setUltimoLote(String ultimoLote) {
        this.ultimoLote = ultimoLote;
    }
    
    public String getNomeforn(){
        return nomeforn;
    }
    
    public void setNomeforn(String nomeforn){
        this.nomeforn = nomeforn;
    }
    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getItemdescricao() {
        return itemdescricao;
    }

    public void setItemdescricao(String itemdescricao) {
        this.itemdescricao = itemdescricao;
    }

    public String getCodpeca() {
        return codpeca;
    }

    public void setCodpeca(String codpeca) {
        this.codpeca = codpeca;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public double getValorpago() {
        return valorpago;
    }

    public void setValorpago(double valorpago) {
        this.valorpago = valorpago;
    }

    public double getPrecosugerido() {
        return precosugerido;
    }

    public void setPrecosugerido(double precosugerido) {
        this.precosugerido = precosugerido;
    }

    public double getLucroestimado() {
        return lucroestimado;
    }

    public void setLucroestimado(double lucroestimado) {
        this.lucroestimado = lucroestimado;
    }

    public int getPercentlucro() {
        return percentlucro;
    }

    public void setPercentlucro(int percentlucro) {
        this.percentlucro = percentlucro;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    
    
    
    
}
