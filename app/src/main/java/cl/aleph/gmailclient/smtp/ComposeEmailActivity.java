package cl.aleph.gmailclient.smtp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import cl.aleph.gmailclient.LoginActivity;
import cl.aleph.gmailclient.R;

public class ComposeEmailActivity extends AppCompatActivity {
    private SendEmailTask sendEmailTask;
    private SharedPreferences userPreferences;
    private EditText emailTo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();

            }
        });
        emailTo = (EditText) findViewById(R.id.compose_email_to);
        userPreferences = getSharedPreferences(LoginActivity.USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void sendEmail() {
        String from = userPreferences.getString(LoginActivity.USER_EMAIL, null);
        String password = userPreferences.getString(LoginActivity.USER_PASSWORD, null);
        String to = emailTo.getText().toString();
        if (from == null || password == null) {
            //ToDO: display some errors
        }
        sendEmailTask = new SendEmailTask(from, to, password, this);
        sendEmailTask.execute((Void) null);
    }

}
