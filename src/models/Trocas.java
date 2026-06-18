package models;

import java.util.Date;

/**
 *
 * @author DBC
 */
public class Trocas {
    
    public int id;
    public String nomeCliente;
    public Date dataTroca;
    public String pecaTroca;
    public Double pecaValor;
    public Double creditoCliente;
    public String obs;
    public String status;

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

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public Date getDataTroca() {
        return dataTroca;
    }

    public void setDataTroca(Date dataTroca) {
        this.dataTroca = dataTroca;
    }

    public String getPecaTroca() {
        return pecaTroca;
    }

    public void setPecaTroca(String pecaTroca) {
        this.pecaTroca = pecaTroca;
    }

    public Double getPecaValor() {
        return pecaValor;
    }

    public void setPecaValor(Double pecaValor) {
        this.pecaValor = pecaValor;
    }

    public Double getCreditoCliente() {
        return creditoCliente;
    }

    public void setCreditoCliente(Double creditoCliente) {
        this.creditoCliente = creditoCliente;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }
    
    
}
