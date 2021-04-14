package com.example.translate;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class KeyboardInput extends InputMethodService {

    InputConnection inputConnection;
    CharSequence currentText;
    String input;
    Translator translator;
    TranslatorOptions options;
    int start = 0;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            inputConnection.sendKeyEvent(event);
            if(start>0){

                start--;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        input = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
        CharSequence before = inputConnection.getTextBeforeCursor(currentText.length(),0);
        CharSequence after = inputConnection.getTextAfterCursor(currentText.length(),0);
        if(event.getKeyCode() == KeyEvent.KEYCODE_SPACE){

            translator.translate(currentText.toString().substring(start,before.length()))
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@NonNull String translatedText) {
                                    inputConnection.setComposingRegion(start,before.length());
                                    inputConnection.setComposingText(translatedText + " ",translatedText.length()+1);
                                    start += translatedText.length()+1;
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

        }
        return super.onKeyUp(keyCode, event);
    }



    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        setInputConnection(getCurrentInputConnection());
         options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.GUJARATI)
                        .build();
        translator =
                Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                Toast.makeText(KeyboardInput.this,"Start Writing",Toast.LENGTH_LONG).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                            }
                        });
        super.onStartInput(attribute, restarting);
    }

    public void setInputConnection(InputConnection inputConnection) {
        this.inputConnection = inputConnection;

    }


}
