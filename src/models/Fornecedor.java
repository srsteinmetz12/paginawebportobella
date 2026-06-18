
package models;

import java.util.Date;

public class Fornecedor {
    
    private String tipoforn;
    private String codforn;
    private String ultimoLote;
    private Double precoMedio;
    private String nomeforn;
    private Date dataCadastramento;
    private String cpfCnpj;
    private String cep;
    private String cidade;
    private String uf;
    private String endereco;
    private String numero;
    private String complemento;
    private String bairro;
    private String email;
    private String telefone;
    private String siteRede;
    private String observacao;

    public String getTipoforn() {
        return tipoforn;
    }

    public void setTipoforn(String tipoforn) {
        this.tipoforn = tipoforn;
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

    public Double getPrecoMedio() {
        return precoMedio;
    }

    public void setPrecoMedio(Double precoMedio) {
        this.precoMedio = precoMedio;
    }

    public String getNomeforn() {
        return nomeforn;
    }

    public void setNomeforn(String nomeforn) {
        this.nomeforn = nomeforn;
    }

    public Date getDataCadastramento() {
        return dataCadastramento;
    }

    public void setDataCadastramento(Date dataCadastramento) {
        this.dataCadastramento = dataCadastramento;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUF() {
        return uf;
    }

    public void setUF(String uf) {
        this.uf = uf;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSiteRede() {
        return siteRede;
    }

    public void setSiteRede(String siteRede) {
        this.siteRede = siteRede;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
