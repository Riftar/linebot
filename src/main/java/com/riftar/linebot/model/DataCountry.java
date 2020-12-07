package com.riftar.linebot.model;

public class DataCountry {

    private DetailDataCovid confirmed;
    private DetailDataCovid recovered;
    private DetailDataCovid death;

    public DetailDataCovid getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(DetailDataCovid confirmed) {
        this.confirmed = confirmed;
    }

    public DetailDataCovid getRecovered() {
        return recovered;
    }

    public void setRecovered(DetailDataCovid recovered) {
        this.recovered = recovered;
    }

    public DetailDataCovid getDeath() {
        return death;
    }

    public void setDeath(DetailDataCovid death) {
        this.death = death;
    }
}
