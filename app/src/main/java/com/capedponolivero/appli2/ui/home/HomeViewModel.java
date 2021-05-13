package com.capedponolivero.appli2.ui.home;

import android.media.Image;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> nMusique;
    private MutableLiveData<String> nArtiste;
    private MutableLiveData<Image> jMusique;

    //TODO : remplacer le setValue par les info du serveur JS

    public HomeViewModel() {
        jMusique = new MutableLiveData<>();
        nMusique = new MutableLiveData<>();
        nArtiste = new MutableLiveData<>();
        nMusique.setValue("Musique");
        nArtiste.setValue("Artiste");
    }

    public LiveData<String> getTextMusique() {
        return nMusique;
    }

    public LiveData<String> getTextArtiste() {
        return nArtiste;
    }
}