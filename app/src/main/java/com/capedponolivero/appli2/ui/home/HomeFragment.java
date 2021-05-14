package com.capedponolivero.appli2.ui.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.capedponolivero.appli2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import StreamServer.Song;

public class HomeFragment extends Fragment {

    // proxy Ice
    protected StreamServer.StreamingPrx iceStream;

    private String transciption;
    private RequestQueue requestQueue;
    private MediaPlayer mediaPlayer;
    private boolean pause = false;

    /**
     * Connection à l'interface Ice
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
     * Transcripteur Speech to Text natif à Android
     * Solution choisis car la premiere ne fonctionnais pas
     */
    public void speechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ennoncer votre commande : ");
        try {
            startActivityForResult(intent, 42);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "La reconnaissance vocal n'est pas supporté par votre appareil.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 42) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<String> resultatTranscription = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                transciption = resultatTranscription.get(0);
                System.out.println("résultat transcription : " + transciption);
                analyseurRequete(transciption);
                System.out.println("après analyseur");
            }
        }
    }

    /**
     * methode d'envoie du string transcription à l'analyseur de requête
     */
    public void analyseurRequete(String text) {
        String urlAnalyseur = String.format("http://%1$s:%2$s/GetCommande?transcription=%3$s",
                getString(R.string.host_anal), getString(R.string.port_anal), text.replaceAll(" ", "+"));

        System.out.println(urlAnalyseur);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                urlAnalyseur,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String commande = "";
                        String valeurCommande = "";
                        try {
                            commande = (String) response.get("commande");
                            valeurCommande = (String) response.get("musique");
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                            valeurCommande = "";
                        }
                        System.out.println("valeur de la transcription : " + transciption);
                        System.out.println("valleur de la commande : " + commande);
                        System.out.println("valeur de la musique renvoyé : " + valeurCommande);
                        action(commande, valeurCommande);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "La commande n'a pas fonctionné. Veuillez réessayer.",
                                Toast.LENGTH_SHORT).show();
                        System.out.println(error.getMessage());
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * select la méthode Ice à lancer selon la commande
     * @param commande
     * @param valeurCommande
     */
    public void action(String commande, String valeurCommande) {
        switch (commande) {
            case "joue":
                String[] valeurCommandeSplited = valeurCommande.split(" ");
                startStream(valeurCommandeSplited[1]);
                break;
            case "pause":
                pauseStream();
                break;
            case "reprendre":
                pauseStream();
                break;
            case "stop":
                stopStream();
                break;
            default:
                System.out.println("Aucune commande de connu");
                break;
        }
    }

    /**
     * Lance le streaming de la mussique passé en argument
     * @param s
     */
    public void startStream(String s) {

        try {
            Song song = iceStream.searchSong(s);
            iceStream.startStream(song);
            System.out.println("Musique " + s + "lancé sur le serveur proxy");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            String url = String.format("http://%1$s:%2$s/%3$s.mp3", getString(R.string.host_streaming), getString(R.string.port_streaming), s);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Met en pause la musique entrain d'être streamé
     * Relance la lecture si on refait appel à la fonction
     */
    public void pauseStream() {
        if(mediaPlayer != null) {
            iceStream.pauseStream();
            if (pause) {
                mediaPlayer.start();
                pause = false;
            } else {
                mediaPlayer.pause();
                System.out.println("La lecture a été mis en pause");
                pause = true;
            }
        }
    }

    /**
     * Arrête le streaming de la musique en cours
     */
    public void stopStream() {
        iceStream.stopStream();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        System.out.println("La lecture a été arrété");
    }

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

        createIceProxy();
        this.requestQueue = Volley.newRequestQueue(getActivity());

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
                speechToText();
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