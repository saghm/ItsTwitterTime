package me.saghm.itstwittertime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class Start extends Activity implements MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final Context context = this;
    TextView textView;

    static Twitter twitter;
    SharedPreferences prefs;
    AccessToken accessToken = null;

    GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        twitter = TwitterFactory.getSingleton();
        textView = (TextView)findViewById(R.id.usertext);

        try {
            twitter.setOAuthConsumer(
                "FklTEyH5uzp4aDfw0blKpzBoI", "6CVE3NsOE33b4beYPND7gUE4arlu8YC133Ai0fviZ5jXYcTQsJ"
            );
        } catch (IllegalStateException e) {}

        prefs         = getPreferences(MODE_PRIVATE);
        String key    = prefs.getString("key", "");
        String secret = prefs.getString("secret", "");

        if (key.isEmpty() && secret.isEmpty() && getIntent().getData() != null) {
            persistAccessToken(getIntent());
        }

        if (!key.isEmpty() && !secret.isEmpty()) {
            accessToken = new AccessToken(key, secret);
            new GetUserTask().execute(this);
        }

        client = new GoogleApiClient.Builder(this)
                 .addApi(Wearable.API)
                 .addConnectionCallbacks(this)
                 .addOnConnectionFailedListener(this)
                 .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.MessageApi.addListener(client, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        if (event.getPath().equals("/stream")) {
            new Connector().execute(
                    new Connector.ActivityAndString(this, new String(event.getData()))
            );
        } else if (event.getPath().equals("/link")) {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(new String(event.getData()))
            );

            intent.setPackage("com.twitter.android");

            startActivity(intent);
        } else {
            Log.i("mobile", "bad path: " + event.getPath());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void persistAccessToken(Intent data) {
        if (!data.getAction().equals(Intent.ACTION_VIEW) ||
            !data.getDataString().startsWith("oauth://itstwittertime")) {
            return;
        }

        String verifier = data.getData().getQueryParameter("oauth_verifier");

        if (verifier == null || verifier.isEmpty()) {
            return;
        }

        new AuthTask().execute(new AuthTask.StartAndString(this, verifier));
    }



    public void login(View view) {
        if (accessToken != null) {
            Toast.makeText(
                    this,
                    "You're already logged in!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Log.i("login", "attemped");
        new LoginTask().execute(this);
    }

    public void logout(View view) {
        if (accessToken == null) {
            Toast.makeText(this, "You aren't logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("key");
        editor.remove("secret");
        editor.commit();
        accessToken = null;
        twitter.setOAuthAccessToken(null);
        textView.setText("Not logged in");
        Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
    }

    private void makeToastOnUIThread(final String text, final String updateText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            textView.setText(updateText);
            }
        });
    }

    public void reportLoginAttempt(boolean successful) {
        if (successful) {
            accessToken = new AccessToken(
                prefs.getString("key", ""),
                prefs.getString("secret", "")
            );

            String user = "";

            try {
                user = twitter.showUser(accessToken.getUserId()).getScreenName();
            } catch (TwitterException e) {}

            makeToastOnUIThread(
                "Logged in!",
                "Logged in as: " + user
            );
            return;
        }

        makeToastOnUIThread("Invalid login", textView.getText().toString());
    }
}
