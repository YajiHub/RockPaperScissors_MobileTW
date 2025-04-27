package com.example.myapplication;

import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomCountdownTimer {

    private CountDownTimer countDownTimer;
    private TextView timerText;
    private ProgressBar countdown;
    private int totalTime;
    private int maxProgress;
    private OnTimerFinishListener listener;

    public interface OnTimerFinishListener {
        void onTimerFinish();
    }

    public CustomCountdownTimer(TextView timerText, ProgressBar countdown, int totalTime, int maxProgress, OnTimerFinishListener listener) {
        this.timerText = timerText;
        this.countdown = countdown;
        this.totalTime = totalTime;
        this.maxProgress = maxProgress;
        this.listener = listener;
        setupTimer();
    }

    private void setupTimer() {
        countdown.setMax(maxProgress);
        countdown.setProgress(maxProgress);
        timerText.setText(String.valueOf(totalTime)); // show immediately

        countDownTimer = new CountDownTimer(totalTime * 1000L, 100) {

            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                timerText.setText(String.valueOf(secondsLeft));

                float fraction = millisUntilFinished / (float)(totalTime * 1000);
                int progress = (int) (fraction * maxProgress);
                countdown.setProgress(progress);
            }

            public void onFinish() {
                timerText.setText("0");
                countdown.setProgress(0);
                if (listener != null) {
                    listener.onTimerFinish();
                }
            }
        };
    }

    public void start() {
        if (countDownTimer != null) {
            countDownTimer.start();
        }
    }

    public void cancel() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}