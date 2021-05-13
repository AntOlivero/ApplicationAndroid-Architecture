package com.capedponolivero.appli2;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import StreamServer.Song;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private static MediaRecorder recorder = null;
    // requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    // proxy Ice
    protected StreamServer.StreamingPrx iceStream;

    /**
     * demande les droits d'utilisation du micro
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    /**
     * action lorsque l'on appuis sur le boutton commande vocal
     * Lance ou arrête l'enregistrement vocal selon le context
     * @param start
     */
    public static void onRecord(boolean start) {
        if(start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    /**
     * lance l'enregistrement vocal
     */
    public static void startRecording() {
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
    public static void stopRecording() {
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;
        sendToStT();
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
    public static void sendToStT() {

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
    public void SpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ennoncer votre commande : ");
        try {
            startActivityForResult(intent, 42);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(getApplicationContext(),
                    "La reconnaissance vocal n'est pas supporté par votre appareil.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 42 :
                if (resultCode == RESULT_OK && data != null) {
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

    /**
     * Création de l'activité
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //création du lien proxy de Ice
        createIceProxy();

        //init des musiques du server ICE
        //addSong();

        //création du fichier vocal
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/record.mp3";
        //permission micro
        ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_RECORD_AUDIO_PERMISSION);

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_musique)
                .build();
        NavController navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(
                this,
                navController,
                appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    /**
     * Arrêt de l'activité
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}