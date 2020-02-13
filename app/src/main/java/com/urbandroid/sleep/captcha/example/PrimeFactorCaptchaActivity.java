package com.urbandroid.sleep.captcha.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import com.urbandroid.sleep.captcha.CaptchaSupport;
import com.urbandroid.sleep.captcha.CaptchaSupportFactory;
import com.urbandroid.sleep.captcha.RemainingTimeListener;

import java.util.Random;

// this is the main captcha activity
public class PrimeFactorCaptchaActivity extends Activity implements OnClickListener {

    private static final long primes[] = {2, 3, 5, 7, 11, 13, 17, 19, 23};
    private static final int buttons[] = {
        R.id.button_2,
        R.id.button_3,
        R.id.button_5,
        R.id.button_7,
        R.id.button_11,
        R.id.button_13,
        R.id.button_17,
        R.id.button_19,
        R.id.button_23,
    };

    private static final int difficulties[][] = { // max count
        // (min prime count), 2, 3, 5, 7, 11, 13, 17, 19, 23
        {5, 2, 3, 2, 1, 0, 0, 0, 0, 0},
        {5, 1, 3, 1, 2, 0, 0, 0, 0, 0},
        {6, 1, 3, 1, 2, 2, 2, 0, 0, 0},
        {7, 0, 3, 0, 2, 2, 2, 2, 0, 0},
        {8, 0, 2, 0, 2, 2, 2, 2, 2, 2},
    };

    private long now_num;

    private CaptchaSupport captchaSupport; // include this in every captcha

    private final RemainingTimeListener remainingTimeListener = new RemainingTimeListener() {
        @Override
        public void timeRemain(int seconds, int aliveTimeout) {
            final TextView timeoutView = (TextView) findViewById(R.id.timeout);
            timeoutView.setText(seconds + "/" + aliveTimeout);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        captchaSupport = CaptchaSupportFactory.create(this); // include this in every captcha, in onCreate()

        // show timeout in TextView with id "timeout"
        captchaSupport.setRemainingTimeListener(remainingTimeListener);
        
        for( int i=0; i < buttons.length; i++ ) {
            final Button b = (Button) findViewById(buttons[i]);
            b.setOnClickListener(this);
        }

        reset();
    }

    private void reset() {
        now_num = randomNumber();
        final TextView captchaTextView = (TextView) findViewById(R.id.q_value);
        captchaTextView.setText(String.valueOf(now_num));
    }

    private void div(long p) {
        if(now_num%p==0){
            now_num/=p;
            final TextView captchaTextView = (TextView) findViewById(R.id.q_value);
            captchaTextView.setText(String.valueOf(now_num));
            if(now_num == 1){
                captchaSupport.solved();
                finish();
            }
        }else{
            reset();
        }
    }

    public void onClick(View v) {
        for(int i=0; i < buttons.length; i++ ) {
            if(v.getId() == buttons[i]){
                div(primes[i]);
                break;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        captchaSupport = CaptchaSupportFactory
                .create(this, intent)
                .setRemainingTimeListener(remainingTimeListener);

    }

    public long randomNumber() {
        Random generator = new Random();

        int df = captchaSupport.getDifficulty() - 1; // [1,5] => [0,4]
        int sum;
        long r;
        do{
            sum = 0;
            r = 1L;
            for(int i = 0; i < primes.length; i++ ) {
                int pc = generator.nextInt(difficulties[df][i+1]+1);
                sum += pc;
                for(int j = 0; j < pc; ++j)r*=primes[i];
            }
        }while(sum<difficulties[df][0]);
        return r;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        captchaSupport.unsolved(); // .unsolved() broadcasts an intent back to AlarmAlertFullScreen that captcha was not solved
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        captchaSupport.alive(); // .alive() refreshes captcha timeout - intended to be sent on user interaction primarily, but can be called anytime anywhere
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captchaSupport.destroy();
    }
}
