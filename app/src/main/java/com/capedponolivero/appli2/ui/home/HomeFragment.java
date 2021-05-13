package com.capedponolivero.appli2.ui.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
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

import com.capedponolivero.appli2.MainActivity;
import com.capedponolivero.appli2.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import StreamServer.Song;
import cz.msebera.android.httpclient.Header;

public class HomeFragment extends Fragment {


    private String LOG_TAG = "AudioRecordTest";
    private String fileName = null;
    private MediaRecorder recorder = null;
    // proxy Ice
    protected StreamServer.StreamingPrx iceStream;


    // TODO : déplacer les methodes dans le HomeFragment ? Empéchera l'utilisation de static ?
    /**
     * action lorsque l'on appuis sur le boutton commande vocal
     * Lance ou arrête l'enregistrement vocal selon le context
     * @param start
     */
    public void onRecord(boolean start) {
        if(start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    /**
     * lance l'enregistrement vocal
     */
    public void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        System.out.println("EMPLACEMENT FILENAME : " + fileName);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recorder.start();
    }

    /**
     * Stop l'enregistrement vocal
     */
    public void stopRecording() {
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;
        //sendToStT();
    }

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
     * Send file audio de l'enregistrement au server de transcription
     * TODO : Finir l'envoie
     */
    public void sendToStT() {

        String url = String.format("http://localhost:3001/upload?file=%1$s", fileName);

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

        /*
        String url = String.format("http://localhost:3001/?file=%1$s", fileName);
        System.out.println(url);
        System.out.println(Environment.getExternalStorageDirectory().getPath());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String transcript = "";
                try {
                    transcript = (String) response.get("transcript");
                    System.out.println(transcript);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });*/
    }

    /**
     * Transcripteur Speech to Text natif à Android
     * Solution choisis car la premiere ne fonctionnais pas
     */
    public void speechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
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
        switch (requestCode) {
            case 42 :
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> resultatTranscription = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String transciption = resultatTranscription.get(0);
                    System.out.println(transciption);
                }
                break;
        }
    }

    /**
     * Ajoute une musique à la liste des musiques du serveur ICE
     * N'est vraiment utile que sur le serveur ICE
     */
    public void addSong() {
        Song song = new Song("Sandstorm", "Darude", "", "SandStorm.mp3");
        iceStream.addSong(song);
        System.out.println("Les musiques ont été initializé");
    }

    /**
     * Lance le streaming de la mussique passé en argument
     * @param s
     */
    public void startStream(String s) {
        Song song = iceStream.searchSong(s);
        iceStream.startStream(song);
    }

    /**
     * Met en pause la musique entrain d'être streamé
     * Relance la lecture si on refait appel à la fonction
     */
    public void pauseStream() {
        iceStream.pauseStream();
        System.out.println("La lecture a été mis en pause");
    }

    /**
     * Arrête le streaming de la musique en cours
     */
    public void stopStream() {
        iceStream.stopStream();
        System.out.println("La lecture a été arrété");
    }

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

        createIceProxy();
        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/record.mp3";

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
                onRecord(mStartRecording);
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