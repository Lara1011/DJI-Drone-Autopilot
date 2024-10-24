package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class ILM_WordListening implements RecognitionListener {
    private SpeechRecognizer speechRecognizer;
    private ILM_SpeechRecognizer ilmSpeechRecognizer;
    private boolean isListening = false;

    public ILM_WordListening(SpeechRecognizer recognizer, ILM_SpeechRecognizer ilmRecognizer) {
        this.speechRecognizer = recognizer;
        this.ilmSpeechRecognizer = ilmRecognizer;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int error) {
        speechRecognizer.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null) {
            for (String result : matches) {
                Log.e("ILM_WordListening word heard", result);
                if (result.equalsIgnoreCase("rexi") || result.equalsIgnoreCase("rexy")) {
                    //showToast(result);
                    showToast("Hi, how can I help you?");
                    stopListening();
                    ilmSpeechRecognizer.startListening();
                    return;
                }
            }
            isListening = false;
        }
        startListening();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    public void startListening() {
        if (!isListening) {
            isListening = true;
            speechRecognizer.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
            Log.e("ILM_WordListening", "started listening");
        }
    }

    public void stopListening() {
        if (isListening) {
            isListening = false;
            speechRecognizer.stopListening();
            Log.e("ILM_WordListening", "stopped listening");

        }
    }
}
