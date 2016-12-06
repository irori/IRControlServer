package org.irorin.ircontrolserver;

import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "IR_SERVER";
    private static final int PORT = 8080;
    private Server server;
    private IRCodePlayer player;
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        TextView logBox = (TextView)findViewById(R.id.logBox);
        logBox.setMovementMethod(new ScrollingMovementMethod());

        server = new Server();
        try {
            server.start();
        } catch (IOException ioe) {
            Log.w(TAG, "The server could not start.");
        }
        Log.w(TAG, "Web server initialized.");

        player = new IRCodePlayer();

        System.setProperty("java.io.tmpdir", getCacheDir().getPath());
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
            log("Listening on port " + PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            log("Request from " + session.getHeaders().get("remote-addr"));
            if (session.getMethod() == Method.POST) {
                final HashMap<String, String> map = new HashMap<String, String>();
                try {
                    session.parseBody(map);
                    final String json = map.get("postData");
                    JSONObject data = new JSONObject(json);
                    player.play(data);
                    log("playback started");
                } catch (IOException | ResponseException | JSONException e) {
                    e.printStackTrace();
                    log(e.toString());
                }
            }
            String answer = "OK";
            return newFixedLengthResponse(answer);
        }

        void log(final String s) {
            final TextView logBox = (TextView)findViewById(R.id.logBox);
            logBox.post(new Runnable() {
                public void run() {
                    logBox.append(dateFormat.format(Calendar.getInstance().getTime()) + s + "\n");
                }
            });
        }
    }
}
