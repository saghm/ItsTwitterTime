package me.saghm.itstwittertime;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class AuthTask extends AsyncTask<AuthTask.StartAndString, Void, Void> {

    static class StartAndString {
        Start start;
        String string;

        public StartAndString(Start start, String string) {
            this.start  = start;
            this.string = string;
        }
    }

    protected Void doInBackground(StartAndString...s) {
        Twitter twitter         = s[0].start.twitter;
        SharedPreferences prefs = s[0].start.prefs;

        try {
            AccessToken token = twitter.getOAuthAccessToken(
                s[0].string
            );

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("key", token.getToken());
            editor.putString("secret", token.getTokenSecret());
            editor.commit();

            s[0].start.reportLoginAttempt(true);
        } catch (TwitterException e) {
            s[0].start.reportLoginAttempt(false);
        } catch (IllegalStateException e) {}

        return null;
    }
}
