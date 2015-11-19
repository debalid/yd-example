package com.debalid.yd_example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HtmlPresentingActivity extends AppCompatActivity
{
    //Represents an intent that html is received.
    public static final String ACTION_HTML_DONE = "com.debalid.yd_example.action.HTML_DONE";
    //Raw html passed by that intent.
    public static final String EXTRA_HTML = "com.debalid.yd_example.extra.HTML";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set listener for get html button.
        final Button _buttonGetHTML = (Button)findViewById(R.id.buttonGetHTML);
        _buttonGetHTML.setOnClickListener(buttonGetHTMLListener);

        //Set broadcast receiver (and intent filter) for getting ready html.
        //Local application scope.
        IntentFilter _if = new IntentFilter(ACTION_HTML_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(htmlDoneReceiver, _if);

        //Making html scrollable
        TextView _textViewHtmlRaw = (TextView)findViewById(R.id.textViewRawHtml);
        _textViewHtmlRaw.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     * Listener that sends intent to the HtmlConsumingIntentService.
     */
    protected Button.OnClickListener buttonGetHTMLListener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String urlRaw =
                    ((TextView)HtmlPresentingActivity.this.findViewById(R.id.editTextUrl))
                    .getText()
                    .toString();

            Intent intent = new Intent(HtmlPresentingActivity.this, HtmlConsumingIntentService.class);
            intent.setAction(HtmlConsumingIntentService.ACTION_HTML_CONSUME);
            intent.putExtra(HtmlConsumingIntentService.EXTRA_URL, urlRaw);

            Log.d(Config.TAG, "Sending intent to service...");
            startService(intent);
        }
    };

    /**
     * A simple broadcaster to register the answer from service made http request.
     */
    private BroadcastReceiver htmlDoneReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(Config.TAG, "Activity got intent.");
            final String action = intent.getAction();
            if (ACTION_HTML_DONE.equals(action))
            {
                String rawHtml = intent.getStringExtra(EXTRA_HTML);
                TextView tv = (TextView)HtmlPresentingActivity.this.findViewById(R.id.textViewRawHtml);
                tv.setText(rawHtml);

                tv.requestFocus();
            }
        }
    };
}
