package com.musicalgorithm;

import android.util.Log;
import java.util.Arrays;

public class MusicAlgorithm {

    public static float[] getMetrics(short[] inputStream) {
        float[] mag = new float[inputStream.length / 2];
        float average = 0f;
        boolean beat = false;
        int j = 0;
        for (int i = 0; i < inputStream.length - 1; i += 2) {
            mag[i] = (float) (Math.abs(inputStream[i]) + Math.abs(inputStream[i + 1])) / 2;
            average += mag[j];
            j++;
            if (i > 44032){
                beat = isBeat(Arrays.copyOfRange(inputStream, i, i + 44032), 1024, true);
            }
        }
        average /= mag.length;
        float[] metrics = getColorsAndOpacity(average, beat);

        return metrics;
    }
    public static boolean isBeat(short[] inputStream, int window, boolean useSensitivity) {
        // http://www.flipcode.com/misc/BeatDetectionAlgorithms.pdf (only 1st section so far)
        if (inputStream.length % window != 0)
            return false;
        int length = inputStream.length/window;
        double se_avg = 0.0; // average sound energy over 44032 sample
        double[] se_inst = new double[length]; // instantaneous sound energy over 1024 sample
        double variance = 0.0;
        double ratio = 1.3; //default good value of 1.3
        for (int i = 0; i < inputStream.length; i++)
            se_avg = se_avg + Math.pow(inputStream[i],2);
        se_avg /= inputStream.length;
        for (int i = 0; i < length; i++){
            for (int j = i * window; i < inputStream.length; i++){
                se_inst[i] = se_inst[i] + Math.pow(inputStream[j],2);
                se_inst[i] /= window;
            }
        }
        for (int i = 0; i < length; i++){
            variance = variance + Math.pow(se_inst[i]-se_avg,2);
        }
        variance /= length;
        if (useSensitivity)
            ratio = -0.0025714 * variance + 1.5142857;

        return se_inst[length - 1] > se_avg * ratio;
    }
	/*
    public static float[] getMetrics(short[] inputStream) {
        int lastBeat = 0;
        double bpm;
        float[] mag = new float[inputStream.length / 2];
        float average = 0f;
        int j = 0;
        for (int i = 0; i < inputStream.length - 1; i += 2) {
            mag[j] = (float) (Math.abs(inputStream[i]) + Math.abs(inputStream[i + 1])) / 2;
            if((j > 0) && (mag[j] - mag[j - 1]) > 8000) {
                bpm = 60 / ((double) ((j - lastBeat) / 44100));
                lastBeat = j;
                Log.e("MUSICALGORITHM", "Last beat was sample " + lastBeat + " and bpm is " + bpm);
            }
            average += mag[j];
            j++;
        }
        average /= mag.length;
        float[] metrics = getColorsAndOpacity(average);
        return metrics;
    }*/

    public static float[] getColorsAndOpacity(float amplitude, boolean isBeat) {
        float[] colors = new float[5];
        float opacity = 10f;
        /*if (!(amplitude == 0f)) {
            opacity = (amplitude / 5000) * 128;
        }*/
        colors[3] = (amplitude / 16000) * 128;
        if (amplitude <= 1000) {
            colors[0] = 82 * amplitude / 1000;
            colors[1] = 0;
            colors[2] = amplitude / 1000 * 94;
        } else if (amplitude <= 2000) {
            colors[0] = 0;
            colors[1] = amplitude /2000 *13;
            colors[2] = amplitude / 2000 * 94;
        } else if (amplitude <= 3000) {
            colors[0] = 0;
            colors[1] = 94 * amplitude/3000;
            colors[2] = 2 * amplitude / 3000;
        } else if (amplitude <= 4000) {
            colors[0] = 255 * amplitude/4000;
            colors[1] = 105 * amplitude / 4000;
            colors[2] = 252 * amplitude / 4000;
        } else if (amplitude <= 5000) {
            colors[0] = 112 * amplitude/ 5000 ;
            colors[1] = 94 * amplitude / 5000;
            colors[2] = 255 *amplitude/5000;
        } else if (amplitude <= 6000) {
            colors[0] = 105 * amplitude / 6000;
            colors[1] = 255 * amplitude / 6000;
            colors[2] = 110 * amplitude / 6000;
        } else if (amplitude <= 7000) {
            colors[0] = 151 * amplitude / 7000;
            colors[1] =140*amplitude/7000;
            colors[2] = 255 * amplitude/7000;
        } else if (amplitude <= 8000) {
            colors[0] = 159 * amplitude / 8000;
            colors[1] = 255 * amplitude / 8000;
            colors[2] =140 * amplitude/8000;
        } else if (amplitude <= 9000) {
            colors[0] = 246 * amplitude/9000;
            colors[1] = 255 *amplitude/9000;
            colors[2] = 0;
        } else if (amplitude <= 10000) {
            colors[0] = 255 * amplitude/10000;
            colors[1] = 208 * amplitude/10000;
            colors[2] = 0;
        } else if (amplitude <= 11000) {
            colors[0] = 255 * amplitude/11000;
            colors[1] = 4 * amplitude/11000;
            colors[2] = 0;
        } else if (amplitude <= 12000) {
            colors[0] = 255 * amplitude/12000;
            colors[1] = 102 * amplitude/12000;
            colors[2] = 102 * amplitude/12000;
        } else if (amplitude <= 13000) {
            colors[0] = 255 * amplitude / 13000;
            colors[1] = 255 * amplitude / 13000;
            colors[2] =  105 * amplitude / 13000;
        } else if (amplitude <= 14000) {
            colors[0] = 255 * amplitude / 14000;
            colors[1] = 228 * amplitude / 14000;
            colors[2] = 140 * amplitude / 14000;
        } else {
            colors[0] = 255;
            colors[1] = 255;
            colors[2] = 255;
            //colors[3] = (127 + (amplitude / 16000) * 128) > 255 ? 255 : (127 + (amplitude / 16000) * 128);
        }
        // if it's a beat, bright white flash like max amplitude
        if (isBeat){
            colors[0] = 255;
            colors[1] = 255;
            colors[2] = 255;
            colors[3] = (127 + (amplitude / 16000) * 128) > 255 ? 255 : (127 + (amplitude / 16000) * 128);
        }
        colors[3] = colors[3] > 128 ? colors[3] : amplitude / 16000 * 128;//too hacky bubble it up if it works better
        return colors;

    }


}
