package com.clem.burgerbox;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.clem.burgerbox.BaseActivity;

import java.util.Locale;
import java.util.Set;

public class Generer_son extends BaseActivity {
    private TextToSpeech tts;
    private Button btnSpeak;
    private EditText inputText;
    private RadioGroup rgGender;
    private RadioButton rbFemale, rbMale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generer_son);

    }
}
