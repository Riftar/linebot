package com.riftar.linebot.model;

public class DataCountry {

    private DetailDataCovid confirmed;
    private DetailDataCovid recovered;
    private DetailDataCovid deaths;

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
        return deaths;
    }

    public void setDeath(DetailDataCovid deaths) {
        this.deaths = deaths;
    }
}
