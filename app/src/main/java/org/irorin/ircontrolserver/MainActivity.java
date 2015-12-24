package org.irorin.ircontrolserver;

import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "IR_SERVER";
    private static final int PORT = 8080;
    private Server server;
    private IRCodePlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        server = new Server();
        try {
            server.start();
        } catch (IOException ioe) {
            Log.w(TAG, "The server could not start.");
        }
        Log.w(TAG, "Web server initialized.");

        player = new IRCodePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

    private class Server extends NanoHTTPD {
        public Server() {
            super(PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (session.getMethod() == Method.POST) {
                final HashMap<String, String> map = new HashMap<String, String>();
                try {
                    session.parseBody(map);
                    final String json = map.get("postData");
                    JSONObject data = new JSONObject(json);
                    player.play(data);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ResponseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                player.play();
            }
            String answer = "OK";
            return newFixedLengthResponse(answer);
        }
    }
}
