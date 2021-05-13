package com.capedponolivero.appli2.ui.musique;

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

import java.util.ArrayList;
import java.util.List;

import StreamServer.Song;

public class MusiqueFragment extends Fragment {

    private MusiqueViewModel musiqueViewModel;
    private List<ItemMusique> listItemMusique;
    protected StreamServer.StreamingPrx iceStream;

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
                Object o = listView.getItemAtPosition(position);
                ItemMusique itemMusique = (ItemMusique) o;
                Toast.makeText(getActivity(), "Selected : " + "" + itemMusique.getNomMusique(), Toast.LENGTH_LONG).show();
                startStream(itemMusique.getNomMusique());
            }
        });


        return root;
    }

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
        Song song = iceStream.searchSong(s);
        iceStream.startStream(song);
    }
}