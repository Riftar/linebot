package com.riftar.linebot.model;

import org.springframework.lang.Nullable;

public class DataDaily {
    private int harike;
    private long tanggal;
    @Nullable
    private Integer jumlahKasusKumulatif = null;
    @Nullable
    private Integer jumlahPasienSembuh = null;
    @Nullable
    private Integer jumlahPasienMeninggal = null;
    @Nullable
    private Integer jumlahKasusBaruperHari = null;
    @Nullable
    private Integer jumlahKasusSembuhperHari = null;
    @Nullable
    private Integer jumlahKasusMeninggalperHari = null;
    @Nullable
    private Float persentasePasienSembuh = null;
    @Nullable
    private Float persentasePasienMeninggal = null;

    public int getHarike() {
        return harike;
    }

    public void setHarike(int harike) {
        this.harike = harike;
    }

    public long getTanggal() {
        return tanggal;
    }

    public void setTanggal(long tanggal) {
        this.tanggal = tanggal;
    }

    @Nullable
    public Integer getJumlahKasusKumulatif() {
        return jumlahKasusKumulatif;
    }

    public void setJumlahKasusKumulatif(@Nullable Integer jumlahKasusKumulatif) {
        this.jumlahKasusKumulatif = jumlahKasusKumulatif;
    }

    @Nullable
    public Integer getJumlahPasienSembuh() {
        return jumlahPasienSembuh;
    }

    public void setJumlahPasienSembuh(@Nullable Integer jumlahPasienSembuh) {
        this.jumlahPasienSembuh = jumlahPasienSembuh;
    }

    @Nullable
    public Integer getJumlahPasienMeninggal() {
        return jumlahPasienMeninggal;
    }

    public void setJumlahPasienMeninggal(@Nullable Integer jumlahPasienMeninggal) {
        this.jumlahPasienMeninggal = jumlahPasienMeninggal;
    }

    @Nullable
    public Integer getJumlahKasusBaruperHari() {
        return jumlahKasusBaruperHari;
    }

    public void setJumlahKasusBaruperHari(@Nullable Integer jumlahKasusBaruperHari) {
        this.jumlahKasusBaruperHari = jumlahKasusBaruperHari;
    }

    @Nullable
    public Integer getJumlahKasusSembuhperHari() {
        return jumlahKasusSembuhperHari;
    }

    public void setJumlahKasusSembuhperHari(@Nullable Integer jumlahKasusSembuhperHari) {
        this.jumlahKasusSembuhperHari = jumlahKasusSembuhperHari;
    }

    @Nullable
    public Integer getJumlahKasusMeninggalperHari() {
        return jumlahKasusMeninggalperHari;
    }

    public void setJumlahKasusMeninggalperHari(@Nullable Integer jumlahKasusMeninggalperHari) {
        this.jumlahKasusMeninggalperHari = jumlahKasusMeninggalperHari;
    }

    @Nullable
    public Float getPersentasePasienSembuh() {
        return persentasePasienSembuh;
    }

    public void setPersentasePasienSembuh(@Nullable Float persentasePasienSembuh) {
        this.persentasePasienSembuh = persentasePasienSembuh;
    }

    @Nullable
    public Float getPersentasePasienMeninggal() {
        return persentasePasienMeninggal;
    }

    public void setPersentasePasienMeninggal(@Nullable Float persentasePasienMeninggal) {
        this.persentasePasienMeninggal = persentasePasienMeninggal;
    }
}
