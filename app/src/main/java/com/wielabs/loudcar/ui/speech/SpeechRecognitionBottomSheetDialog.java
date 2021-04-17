package com.wielabs.loudcar.ui.speech;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wielabs.loudcar.R;
import com.wielabs.loudcar.databinding.FragmentBottomSheetSpeechRecognitionBinding;

import java.util.ArrayList;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SpeechRecognitionBottomSheetDialog extends BottomSheetDialogFragment implements RecognitionListener {
    private static final int RC_PERMISSION = 218;
    private static final int RC_SPEECH_RECOGNITION = 219;
    private FragmentBottomSheetSpeechRecognitionBinding binding;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBottomSheetSpeechRecognitionBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
//            askRequiredPermissions(new String[]{Manifest.permission.RECORD_AUDIO});
//        else
//            getSpeechRecognizer().startListening(getRecognizerIntent());

        startActivityForResult(getRecognizerIntent(), RC_SPEECH_RECOGNITION);

        // Setup click listeners
        binding.closeSheetIv.setOnClickListener(v -> {
            dismiss();
        });

        binding.speechRecognitionFab.setOnClickListener(v -> {
            getSpeechRecognizer().stopListening();
            getSpeechRecognizer().startListening(getRecognizerIntent());
        });
    }

    private void askRequiredPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(requireActivity(), permissions, RC_PERMISSION);
    }

    private Intent getRecognizerIntent() {
        if (speechRecognizerIntent != null)
            return speechRecognizerIntent;

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        return speechRecognizerIntent;
    }

    private SpeechRecognizer getSpeechRecognizer() {
        if (speechRecognizer != null)
            return speechRecognizer;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizer.setRecognitionListener(this);
        return speechRecognizer;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSION && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getSpeechRecognizer().startListening(getRecognizerIntent());
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
    }

    @Override
    public void onBeginningOfSpeech() {
        binding.recognisedTextTv.setText(R.string.listening);
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
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> recognizedText = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        binding.recognisedTextTv.setText(recognizedText.get(0));
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }
}
