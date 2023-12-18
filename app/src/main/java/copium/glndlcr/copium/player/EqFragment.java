package copium.glndlcr.copium.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import copium.glndlcr.copium.R;

public class EqFragment extends Fragment {

    private Equalizer mEqualizer;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eq, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefs = requireContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        view.findViewById(R.id.backBtn).setOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        if (getArguments() != null) {
            int audioSession = getArguments().getInt("id", 0);
            mEqualizer = new Equalizer(0, audioSession);
            mEqualizer.setEnabled(true);

            LinearLayout mLinearLayout = view.findViewById(R.id.liner);
            mLinearLayout.setPadding(0, 0, 0, 20);

            short numberFrequencyBands = mEqualizer.getNumberOfBands();
            final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
            final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];

            for (short i = 0; i < numberFrequencyBands; i++) {
                final short equalizerBandIndex = i;

                TextView frequencyHeaderTextview = new TextView(getContext());
                frequencyHeaderTextview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                frequencyHeaderTextview.setGravity(Gravity.CENTER_HORIZONTAL);
                frequencyHeaderTextview
                        .setText((mEqualizer.getCenterFreq(equalizerBandIndex) / 1000) + " Hz");
                mLinearLayout.addView(frequencyHeaderTextview);

                LinearLayout seekBarRowLayout = new LinearLayout(getContext());
                seekBarRowLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView lowBandTV = new TextView(getContext());

                lowBandTV.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                lowBandTV.setText("     " + (lowerEqualizerBandLevel / 100) + " dB");
                lowBandTV.setRotation(90);

                TextView upperBandTV = new TextView(getContext());
                upperBandTV.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                upperBandTV.setText("     " + (upperEqualizerBandLevel / 100) + " dB");
                upperBandTV.setRotation(90);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT - 60,
                        120);
                layoutParams.weight = 1;

                SeekBar seekBar = new SeekBar(getContext());

                seekBar.setId(i);
                ColorDrawable seekBg;
                seekBg = new ColorDrawable(Color.GREEN);
                seekBg.setAlpha(90);

                seekBar.setBackground(seekBg);
                seekBar.setPadding(35, 15, 35, 15);

                seekBar.setLayoutParams(layoutParams);
                seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

                final int seek_id = i;
                int progressBar = prefs.getInt("seek_" + seek_id, 1500);

                if (progressBar != 1500) {
                    seekBar.setProgress(progressBar);
                    mEqualizer.setBandLevel(equalizerBandIndex,
                            (short) (progressBar + lowerEqualizerBandLevel));
                } else {
                    seekBar.setProgress(mEqualizer.getBandLevel(equalizerBandIndex));
                    mEqualizer.setBandLevel(equalizerBandIndex,
                            (short) (progressBar + lowerEqualizerBandLevel));
                }

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        mEqualizer.setBandLevel(equalizerBandIndex,
                                (short) (progress + lowerEqualizerBandLevel));

                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        prefs.edit().putInt("seek_" + seek_id, seekBar.getProgress()).apply();
                    }
                });


                seekBar.setThumb(AppCompatResources.getDrawable(getContext(), R.drawable.baseline_equalizer_24));
                seekBar.setProgressDrawable(new ColorDrawable(Color.BLACK));
                LinearLayout.LayoutParams seekBarLayout = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                seekBarLayout.weight = 1;
                seekBarLayout.setMargins(5, 0, 5, 0);
                seekBarRowLayout.setLayoutParams(seekBarLayout);

                seekBarRowLayout.addView(lowBandTV);
                seekBarRowLayout.addView(seekBar);
                seekBarRowLayout.addView(upperBandTV);

                mLinearLayout.addView(seekBarRowLayout);
            }
        }
    }


    public static EqFragment newInstance(int audioId) {

        Bundle args = new Bundle();
        args.putInt("id", audioId);

        EqFragment fragment = new EqFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
