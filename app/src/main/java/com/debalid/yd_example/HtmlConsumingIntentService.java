package com.debalid.yd_example;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 *
 * P.S. I still don't like auto-generating stuff however.
 */
public class HtmlConsumingIntentService extends IntentService
{
    //Actions to perform
    public static final String ACTION_HTML_CONSUME = "com.debalid.yd_example.action.HTML_CONSUME";

    //Extra parameters passed by Intent.
    public static final String EXTRA_URL = "com.debalid.yd_example.extra.URL";

    public HtmlConsumingIntentService() { super("HtmlConsumingIntentService"); }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(Config.TAG, "Service got intent.");
        if (intent != null)
        {
            final String action = intent.getAction();
            if (ACTION_HTML_CONSUME.equals(action))
            {
                final String url = intent.getStringExtra(EXTRA_URL);
                handleHtmlConsume(url);
            }
        }
    }

    /**
     * Handles a request to specified resource.
     * @param urlRaw Raw representation of that should be an url.
     */
    protected void handleHtmlConsume(String urlRaw)
    {
        String response;
        try
        {
            response = consumeHttpConnectionWithHtml(urlRaw);
        }
        catch (MalformedURLException e)
        {
            response = "Wrong url input. Examples are: \"http://google.com\", \"https://yandex.ru etc\", etc.";
        }
        catch (IOException e)
        {
            response = "Error during the connection.";
            Log.e(Config.TAG, e.getMessage());
        }

        Log.d(Config.TAG, response);

        //Sending ready html inside intent back to activity.
        Intent ready = new Intent(HtmlPresentingActivity.ACTION_HTML_DONE);
        ready.putExtra(HtmlPresentingActivity.EXTRA_HTML, response);
        LocalBroadcastManager.getInstance(this).sendBroadcast(ready);
    }

    /**
     * Actually uses the connection to retrieve an information.
     */
    private String consumeHttpConnectionWithHtml(String urlRaw) throws IOException
    {
        HttpURLConnection connection = buildConnection(urlRaw);

        connection.connect();

        int code = connection.getResponseCode();

        //Handling redirects
        if (code == HttpURLConnection.HTTP_MOVED_TEMP
                || code == HttpURLConnection.HTTP_MOVED_PERM
                || code == HttpURLConnection.HTTP_SEE_OTHER)
        {
            //Replace url with new one.
            String movedTo = connection.getHeaderField("Location");
            connection.disconnect();
            connection = buildConnection(movedTo);
            code = connection.getResponseCode();
        }

        String response;
        //"Success" http responses codes
        if (code >= 200 && code <= 299)
        {
            //Reading response
            //Unfortunately can't use try with resource statement at API level 15.
            BufferedReader bf = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8")
            );
            StringBuilder responseBuilder = new StringBuilder();
            String val;
            while ((val = bf.readLine()) != null)
            {
                responseBuilder.append(val);
            }

            bf.close();
            response = responseBuilder.toString();
        }
        else { response = String.valueOf(code); }

        connection.disconnect();
        return response;
    }

    /**
     * Actually creates the connection with proper headers.
     */
    private HttpURLConnection buildConnection(String urlRaw) throws IOException
    {
        URL url = new URL(urlRaw);
        //Should handle HTTPS as well.
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        //Setting up request parameters.
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(Config.CONNECTION_TIMEOUT);
        connection.setRequestProperty("Accept", Config.ACCEPT_HTML_HEADER);
        connection.setRequestProperty("User-Agent", Config.USER_AGENT_HTML_HEADER);
        connection.setRequestProperty("Accept-Charset", Config.ACCEPT_CHARSET_HTML_HEADER);

        return connection;
    }
}
