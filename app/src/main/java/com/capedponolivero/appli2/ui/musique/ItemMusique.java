package com.capedponolivero.appli2.ui.musique;

public class ItemMusique {

    private String nomArtiste;
    private String nomMusique;

    public ItemMusique(String nomArtiste, String nomMusique) {
        this.nomArtiste = nomArtiste;
        this.nomMusique = nomMusique;
    }

    public String getNomArtiste() {
        return nomArtiste;
    }

    public String getNomMusique() {
        return nomMusique;
    }

    public void setNomArtiste(String nomArtiste) {
        this.nomArtiste = nomArtiste;
    }

    public void setNomMusique(String nomMusique) {
        this.nomMusique = nomMusique;
    }
}
