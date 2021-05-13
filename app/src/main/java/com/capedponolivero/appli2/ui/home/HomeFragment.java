package com.capedponolivero.appli2.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.capedponolivero.appli2.MainActivity;
import com.capedponolivero.appli2.R;

public class HomeFragment extends Fragment {

    private boolean mStartRecording = true;
    /**
     * création de la vue principal
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView jacketMusique = root.findViewById(R.id.JacketMusique);
        TextView nMusique = root.findViewById(R.id.titreMusique);
        TextView nArtiste = root.findViewById(R.id.artiste);

        /**
         * bouton commande vocal
         */
        Button recordButton = root.findViewById(R.id.commandeVocal);

        /**
         * Listener du bouton commande vocal
         * lance la méthode d'enregistrement vocal onRecord()
         */
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("boutton commande vocal clické");
                MainActivity.onRecord(mStartRecording);
                if(mStartRecording) {
                    recordButton.setText("Stop recording");
                } else {
                    recordButton.setText("Commande vocal");
                }
                mStartRecording = !mStartRecording;
            }
        });

        /**
         * set le nom de la musique sur la vue
         */
        homeViewModel.getTextMusique().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                nMusique.setText(s);
            }
        });

        /**
         * set le nom de l'artiste sur la vue
         */
        homeViewModel.getTextArtiste().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                nArtiste.setText(s);
            }
        });

        return root;
    }
}