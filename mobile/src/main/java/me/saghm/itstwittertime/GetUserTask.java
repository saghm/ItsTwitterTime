package me.saghm.itstwittertime;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

/**
 * Created by saghm on 9/21/14.
 */
public class GetUserTask extends AsyncTask<Start, Void, Void> {
    protected Void doInBackground(Start...s) {
        try {
            final Start start = s[0];
            start.twitter.setOAuthAccessToken(s[0].accessToken);
            final String user = s[0].twitter.getScreenName();

            start.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    start.textView.setText("Logged in as " + user);
                }
            });
        } catch(Exception e) {
            Log.i("exception", ExceptionParser.parse(e));
        }

        return null;
    }
}
