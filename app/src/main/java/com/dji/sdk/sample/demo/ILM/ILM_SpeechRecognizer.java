package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;

public class ILM_SpeechRecognizer {
    private SpeechRecognizer speechRecognizer;
    private Context context;
    private String language;
    private ILM_Waypoints ilmWaypoints;
    private ILM_Buttons buttons;
    private ILM_MapController mapController;
    private int record = 0;
    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

    public ILM_SpeechRecognizer(Context init_context, ILM_Buttons ilmButtons, ILM_Waypoints ilmWaypoints, ILM_MapController mapController) {
        this.context = init_context;
        this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.context);
        this.language = "en-US";
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, this.language);
        this.ilmWaypoints = ilmWaypoints;
        this.buttons = ilmButtons;
        this.mapController = mapController;
        init_listeners();
    }

    private void init_listeners() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle bundle) {
                String commandKey = "";
                while(commandKey.equals("")) {
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    Log.e("ILM_SpeechRecognizer command speech", data.get(0));
                    if (language.equalsIgnoreCase("en-US"))
                        commandKey = parseCommandEnglish(data.get(0));
                    else
                        commandKey = parseCommandHebrew(data.get(0));
                    //showToast("commandKey: " + commandKey);
                    Log.e("ILM_SpeechRecognizer command heard word: ", commandKey);
                    submitCommand(commandKey);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startListening();
                            // Stop listening after 5 seconds and restart if no valid command was found
                            if (speechRecognizer != null) {
                                Log.e("ILM_SpeechRecognizer", "No command detected in 5 seconds, restarting listening");
                                speechRecognizer.stopListening();
                            }
                        }
                    }, 5000);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
    }

    private boolean list_contain(String[] lst, String target) {
        for (String str : lst) {
            if (str.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    public String parseCommandEnglish(String command) {
        String[] parts_of_command = command.split(" ");

        if (list_contain(parts_of_command, "takeoff") || list_contain(parts_of_command, "take off")) {
            return "takeoff";
        } else if (list_contain(parts_of_command, "take")) {
            if (list_contain(parts_of_command, "off")) {
                return "takeoff";
            } else {
                return "-1";
            }
        }

        if (list_contain(parts_of_command, "land")) {
            return "land";
        }

        if (list_contain(parts_of_command, "goto") || (list_contain(parts_of_command, "go") && list_contain(parts_of_command, "to"))) {
            return "goto";
        }

        if (list_contain(parts_of_command, "stop")) {
            Log.e("ILM speech", "stop -----------------------------------");
            showToast("stopping drone");
            return "stop";
        }
        if (list_contain(parts_of_command, "repeatRoute")) {
            return "repeatRoute";
        } else if (list_contain(parts_of_command, "repeat")) {
            if (list_contain(parts_of_command, "route")) {
                return "repeatRoute";
            } else {
                return "-1";
            }
        }
        if(list_contain(parts_of_command, "up")) {
            return "up";
        }
        if(list_contain(parts_of_command, "down")) {
            return "down";
        }
        if (list_contain(parts_of_command, "record")) {
            return "record";
        }
        if (list_contain(parts_of_command, "right")) {
            return "right";
        }

        return "null";
    }

    private String parseCommandHebrew(String command) {
        String[] parts_of_command = command.split(" ");

        if (list_contain(parts_of_command, "המראה") || list_contain(parts_of_command, "תמריא")) {
            return "takeoff";
        }

        if (list_contain(parts_of_command, "נחיתה") || list_contain(parts_of_command, "תנחת")) {
            return "land";
        }

        if (list_contain(parts_of_command, "עצירה") || list_contain(parts_of_command, "עצור")) {
            return "stop";
        }

        if (list_contain(parts_of_command, "למטה")) {
            return "down";
        }

        if (list_contain(parts_of_command, "למעלה")) {
            return "up";
        }
        return "null";
    }

    private void submitCommand(String command) {
        switch (command) {
            case "takeoff":
                Log.e("submitting command", "takeoff");
                showToast("Taking off");
                buttons.takeOff();
                break;
            case "land":
                Log.e("submitting command", "land");
                showToast("Landing drone");
                buttons.land();
                break;
            case "stop":
                Log.e("submitting command", "stop");
                showToast("stopping drone");
                buttons.stop();
                break;
            case "goto":
                Log.e("submitting command", "goto");
                buttons.goTo(ilmWaypoints, mapController, false);
                break;
            case "repeatRoute":
                Log.e("submitting command", "repeatroute");
                buttons.RepeatRoute(ilmWaypoints, mapController, false);
                break;
            case "up":
                Log.e("submitting command", "up");
                buttons.up();
                break;
            case "down":
                Log.e("submitting command", "down");
                buttons.down();
                break;
            case "record":
                Log.e("submitting command", "record");
                if(record == 0) {
                    buttons.record();
                    record = 1;
                }
                else {
                    buttons.stopRecording();
                    record = 0;
                }
                break;
        }
    }

    public void startListening() {
        //Log.e("ILM speech", "startListening");
        speechRecognizer.startListening(speechRecognizerIntent);

        // Start a 5-second timer for timeout
         // 5-second timeout
    }
}
