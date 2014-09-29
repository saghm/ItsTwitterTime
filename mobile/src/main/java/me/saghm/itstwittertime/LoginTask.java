package me.saghm.itstwittertime;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

public class LoginTask extends AsyncTask<Start, Void, Void> {

    protected Void doInBackground(Start...s) {
        try {
            s[0].twitter.setOAuthAccessToken(null);
            RequestToken token = s[0].twitter.getOAuthRequestToken("oauth://itstwittertime");

            s[0].startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse(token.getAuthenticationURL()))
            );

        } catch(TwitterException e) {
            Log.i("exception", ExceptionParser.parse(e));
        }

        return null;
    }
}
