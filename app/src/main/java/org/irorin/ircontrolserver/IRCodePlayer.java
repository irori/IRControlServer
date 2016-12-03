package org.irorin.ircontrolserver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class IRCodePlayer {
    private static final String TAG = "IR_PLAYER";
    private static final int CLOCK = 2000000;  // Sampling rate of IRKit JSON's "data" field
    AudioTrack audio;

    public void play(JSONObject json) throws JSONException {
        if (audio != null) {
            audio.stop();
            audio.release();
            audio = null;
        }

        int freq_hz = json.getInt("freq") * 1000;
        JSONArray data = json.getJSONArray("data");
        int[] samples = new int[data.length()];
        double r = 0;
        int totalSamples = 0;
        for (int i = 0; i < samples.length; i++) {
            double x = data.getDouble(i) * freq_hz / CLOCK + r;
            int k = (int)Math.round(x);
            r = x - k;
            samples[i] = k;
            totalSamples += k;
        }
        Log.w(TAG, "totalSamples: " + totalSamples);

        audio = new AudioTrack(AudioManager.STREAM_MUSIC, freq_hz,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, totalSamples * 2,
                AudioTrack.MODE_STATIC);

        byte[] buf = new byte[totalSamples * 2];
        int pos = 0;
        for (int i = 0; i < samples.length; i++) {
            if (i % 2 == 1) {
                // Low
                for (int j = 0; j < samples[i]; j++)
                    buf[pos + j*2] = buf[pos + j*2+1] = (byte)0x80;
            } else {
                // High
                for (int j = 0; j < samples[i]; j++) {
                    int parity = j % 2;
                    buf[pos + j * 2 + parity] = 0;
                    buf[pos + j * 2 + 1 - parity] = (byte) 0xff;
                }
            }
            pos += samples[i] * 2;
        }
        audio.write(buf, 0, totalSamples * 2);

        audio.play();
    }
}
