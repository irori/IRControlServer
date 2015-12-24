package org.irorin.ircontrolserver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by irori on 2015/12/23.
 */
public class IRCodePlayer {
    private static final String TAG = "IR_PLAYER";
    AudioTrack audio;

    public void play() {
        if (audio != null) {
            audio.stop();
            audio.release();
            audio = null;
        }

        byte[] buf = new byte[10000];
        for (int i = 0; i < 5000; i++) {
            // buf[i*2] = buf[i*2+1] = (i % 2 == 1) ? (byte)0 : (byte)255;
            int parity = i % 2;
            buf[i * 2 + parity] = 0;
            buf[i * 2 + 1 - parity] = (byte) 0xff;
        }

        audio = new AudioTrack(AudioManager.STREAM_DTMF, 8000,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, 10000,
                AudioTrack.MODE_STATIC);
        audio.write(buf, 0, 10000);
        audio.play();
    }

    public void play(JSONObject json) throws JSONException {
        if (audio != null) {
            audio.stop();
            audio.release();
            audio = null;
        }

        int freq = json.getInt("freq") * 1000;
        int clock = 14336;
        JSONArray data = json.getJSONArray("data");
        int[] samples = new int[data.length()];
        double r = 0;
        int totalSamples = 0;
        for (int i = 0; i < samples.length; i++) {
            double x = data.getDouble(i) * freq / clock + r;
            int k = (int)Math.round(x);
            r = x - k;
            samples[i] = k;
            totalSamples += k;
        }
        Log.w(TAG, "totalSamples: " + totalSamples);

        audio = new AudioTrack(AudioManager.STREAM_MUSIC, freq,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, totalSamples * 2,
                AudioTrack.MODE_STATIC);

        byte[] buf = new byte[totalSamples * 2];
        int pos = 0;
        for (int i = 0; i < samples.length; i++) {
            if (i % 2 == 0) {
                for (int j = 0; j < samples[i]; j++)
                    buf[pos + j*2] = buf[pos + j*2+1] = (byte)0x80;
            } else {
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
