package com.capedponolivero.appli2.ui.musique;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.capedponolivero.appli2.R;
import com.capedponolivero.appli2.ui.home.HomeFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import StreamServer.Song;

public class MusiqueFragment extends Fragment {

    private MusiqueViewModel musiqueViewModel;
    private List<ItemMusique> listItemMusique;
    protected StreamServer.StreamingPrx iceStream;
    private MediaPlayer mediaPlayer = null;

    /**
     * Creation du proxy ICE
     */
    protected void createIceProxy() {
        try {
            com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize();
            String stringProxy = String.format("IceStream:default -h %1$s -p %2$s",
                    getString(R.string.host_Ice), getString(R.string.port_Ice));
            com.zeroc.Ice.ObjectPrx objectPrx = communicator.stringToProxy(stringProxy);
            iceStream = StreamServer.StreamingPrx.checkedCast(objectPrx);
            if(iceStream == null) {
                throw new Error("Invalid proxy");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la création du proxy");
        }
    }

    /**
     * Récupération de la liste des musiques du serveur
     * @return
     */
    public List<ItemMusique> getListData() {
        List<ItemMusique> list = new ArrayList<>();
        try {
            Song[] s = iceStream.getSongList();
            for (Song sIt : s) {
                System.out.println("La playlist contient " + sIt.titre + " de " + sIt.artiste);
                ItemMusique itemMusique = new ItemMusique(sIt.artiste, sIt.titre);
                list.add(itemMusique);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * lance l'écoute de la musique passé en argument
     * @param s
     */
    public void startStream(String s) {

        System.out.println("Methode start Stream");
        System.out.println("nom musique à lancer " + s);
        if( mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        try {
            Song song = iceStream.searchSong(s);
            iceStream.startStream(song);
            System.out.println("Musique " + s + "lancé sur le serveur proxy");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            String url = String.format("http://%1$s:%2$s/%3$s.mp3", getString(R.string.host_streaming), getString(R.string.port_streaming), s);
            System.out.println(url);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    /**
     * Creation de la vue
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        createIceProxy();
        musiqueViewModel = new ViewModelProvider(this).get(MusiqueViewModel.class);

        View root = inflater.inflate(R.layout.fragment_musique, container, false);

        listItemMusique = getListData();
        final ListView listView = (ListView) root.findViewById(R.id.listViewMusique);
        listView.setAdapter(new CustomListAdapter(getActivity(), listItemMusique));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemMusique itemMusique = (ItemMusique) listView.getItemAtPosition(position);
                System.out.println(itemMusique.getNomMusique());
                //Toast.makeText(getActivity(), "Selected : " + "" + itemMusique.getNomMusique(), Toast.LENGTH_LONG).show();
                startStream(itemMusique.getNomMusique());
            }
        });
        return root;
    }

}