package com.example.magku.ukeuke;

import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Looper;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import android.os.Handler;

import static android.R.attr.fragment;
import static android.os.Build.VERSION_CODES.M;
import static com.example.magku.ukeuke.FFTbase.fftbase;

/**
 * Most of program logic over here
 * Signal gather and FFT call
 */
public class Tunning extends Fragment {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public int sampleRate;
    public int bufferSize;
    double freq;
    String toneName =" ";
    // GCEA
    float[] tones = new float[]{392.0f,261.63f,329.63f,440.00f};

    int[]buff = new int[bufferSize];
    public AudioRecord audioInput;
    Runnable runnable;
    Handler handler;
    boolean notFirstRun;
    TextView tx;
    TextView noteName;
    TextView txt1;
    TextView txt2;
    TextView txt3;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tuning, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isResumed()) {
            if (isVisibleToUser) {
                if (notFirstRun) {
                    handler.postDelayed(runnable, 50);
                    audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            AudioFormat.CHANNEL_IN_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize);
                    notFirstRun = false;
                }
                Log.v("T", "VISIBLE!");
            } else {
                Log.v("T", "NOT VISIBLE!");
                audioInput.stop();
                notFirstRun = true;
                handler.removeCallbacks(runnable);
                audioInput.release();
            }
        }
    }

       @Override
       public void onActivityCreated(Bundle savedInstanceState) {
           super.onActivityCreated(savedInstanceState);
         getValidSampleRates();
           notFirstRun=false;
           audioInput = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                   sampleRate,
                   AudioFormat.CHANNEL_IN_STEREO,
                   AudioFormat.ENCODING_PCM_16BIT,
                   bufferSize);
           Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(),  "fonts/ChunkfiveEx.ttf");
           tx = (TextView)getView().findViewById(R.id.pitch);
           noteName = (TextView)getView().findViewById(R.id.note);
           txt1 = (TextView)getView().findViewById(R.id.someText);
           txt2 = (TextView)getView().findViewById(R.id.someText1);
           txt3 = (TextView)getView().findViewById(R.id.nearest);

           tx.setTypeface(custom_font);
           txt1.setTypeface(custom_font);
           txt2.setTypeface(custom_font);
           txt3.setTypeface(custom_font);
           noteName.setTypeface(custom_font);
           runnable = new Runnable() {
               public void run() {

                   Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                      fftSignal();
                      freq = roundFreq(freq, 1);
                      String str = String.valueOf(freq) + " Hz";
                      tx.setText(str);
                      toneRecognizer();
                      noteName.setText(toneName);
                      toneName = " ";
                      nearestFreq();
                      handler.postDelayed(this, 50);
               }
           };
           handler = new Handler(Looper.getMainLooper());
           handler.postDelayed(runnable, 50);
       }

      @Override
       public void onPause() {
           super.onPause();
          audioInput.stop();
           handler.removeCallbacks(runnable);
           audioInput.release();
       }

       @Override
       public void onResume() {
           super.onResume();
           handler.postDelayed(runnable, 50);
           audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC,
                   sampleRate,
                   AudioFormat.CHANNEL_IN_STEREO,
                   AudioFormat.ENCODING_PCM_16BIT,
                   bufferSize);
       }
    public void fftSignal() {
        int offset = 0;
        int shortRead;
        double[] bufferIm = new double[bufferSize];
        double[] bufferRe = new double[bufferSize];
        short[] buffer = new short[bufferSize];
        Arrays.fill(buff,0);
        audioInput.startRecording();

        audioInput.read(buffer, 0, bufferSize);
        while (offset < bufferSize) {
            shortRead = audioInput.read(buffer, 0, bufferSize);
            offset += shortRead;
        }

        buff = lowPassFilter(buffer, 0.25f);
        buff = hannWindow(buff);

        for (int k = 0; k < bufferSize; k++) {
            bufferRe[k] = (double) buff[k];
            bufferIm[k] =0;
        }
        double[] bufferComplex = fftbase(bufferRe, bufferIm);

        int sizeaftFFT = bufferComplex.length/2;
        Log.v("Tunning","dlugosc: " + bufferComplex.length);
        double[] magnitude = new double[sizeaftFFT];
        for (int i = 0; i < sizeaftFFT; i+=2) {
            magnitude[i] = Math.hypot(bufferComplex[i], bufferComplex[i+1]);
        }
        freq = calculateFreq(magnitude);
        Log.v("Tunning","Calculated freq: " + Double.toString(freq));
        Log.v("Tunning", "Complex table size: " + sizeaftFFT);

    }

    public double calculateFreq(double[] magnitude) {
        double mag =-10000;
        int max_index =-1;
        for(int j = 0; j< magnitude.length; j++){
            if(magnitude[j] > mag){
                mag = magnitude[j];
                max_index = j;
            }
        }
        Log.v("Tunning","max index: " + max_index);
        return (double) max_index*sampleRate/(bufferSize);
    }

    int[] hannWindow(int [] buffer){
        for(int i=0; i<bufferSize;i++){
            double multiplier = 0.5 * (1-Math.cos(2*Math.PI*i/(bufferSize-1)));
            buffer[i] = (int)(multiplier*buffer[i]);
        }
        return buffer;
    }


    public void getValidSampleRates() {
        for (int rate : new int[] { 8000,16000,44100,11025,22050}) {  // add the rates you wish to check against
            int _bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            if (_bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                Log.v("Tunning", "Sample rate: " + rate);
                sampleRate = rate;
                bufferSize = _bufferSize;
                Log.v("Tunning", "Buffer Size: " + bufferSize);
                for(int validToFFT : new int[] {512,1024,2048,4096,8192,16384}){
                    Log.v("Tunning", "Checking: " + validToFFT);
                    if(validToFFT>=bufferSize){
                        bufferSize = validToFFT;
                        break;
                    }
                }
                bufferSize*=2;
                Log.v("Tunning", "Buffer Size: " + bufferSize);
                break;
            }
        }
    }
    int[] lowPassFilter(short[] buffer, float alpha) {
        int[] bufferAverage = new int[bufferSize];
        bufferAverage[0] =buffer[0];
        for(int i = 1; i<bufferSize;i++){
             bufferAverage[i] = (int)(bufferAverage[i-1]+ alpha*(buffer[i] - bufferAverage[i-1])) ;
        }
        return bufferAverage;
    }
    private static double roundFreq (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
    public void toneRecognizer(){

        float range = 2f;
        for(int i= 0; i<tones.length; i++){
            if((tones[i]-range)<freq && freq<(tones[i]+range)){
                switch(i){
                    case 0:
                        toneName = "G";
                        break;
                    case 1:
                        toneName = "C";
                        break;
                    case 2:
                        toneName = "E";
                        break;
                    case 3:
                        toneName = "A";
                        break;

                }

            }
        }
    }
    public void nearestFreq(){
        String nearTone;
                    if(freq<tones[1]+34 && freq> 50){
                        nearTone = "Najbliżej: C  " + tones[1];
                        txt3.setText(nearTone);
                    }
                    else if ((tones[2]-34)<freq && freq<(tones[2]+31)){
                        nearTone = "Najbliżej: E  " + tones[2];
                        txt3.setText(nearTone);
                    }
                    else if ((tones[0]-31)<freq && freq<(tones[0]+24)){
                        nearTone = "Najbliżej: G  " + tones[0];
                        txt3.setText(nearTone);
                    }else if ((tones[3]-24)<freq && freq < 700){
                        nearTone = "Najbliżej: A  " + tones[3];
                        txt3.setText(nearTone);
                    }
                    else {nearTone =" "; txt3.setText(nearTone);}
    }
}
