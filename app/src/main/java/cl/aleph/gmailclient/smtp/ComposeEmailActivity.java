package cl.aleph.gmailclient.smtp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import cl.aleph.gmailclient.LoginActivity;
import cl.aleph.gmailclient.R;

public class ComposeEmailActivity extends AppCompatActivity {
    private SendEmailTask sendEmailTask;
    private SharedPreferences userPreferences;
    private EditText emailTo;
    private EditText subject;
    private EditText data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.compose_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.send_email);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String from = userPreferences.getString(LoginActivity.USER_EMAIL, null);
                String password = userPreferences.getString(LoginActivity.USER_PASSWORD, null);
                String to = emailTo.getText().toString();
                String _subject = subject.getText().toString();
                String _data = data.getText().toString();
                if (from == null || password == null) {
                    //ToDO: send to login activity
                }
                if (TextUtils.isEmpty(to)) {
                    emailTo.setError(getString(R.string.error_field_required));
                    emailTo.requestFocus();
                }
                if (TextUtils.isEmpty(_subject)) {
                    subject.setError(getString(R.string.error_field_required));
                    subject.requestFocus();
                }
                if (TextUtils.isEmpty(_data)) {
                    data.setError(getString(R.string.error_field_required));
                    data.requestFocus();
                }
                sendEmail(from, to, password, _data, _subject);

            }
        });
        emailTo = (EditText) findViewById(R.id.compose_email_to);
        subject = (EditText) findViewById(R.id.compose_email_subject);
        data = (EditText) findViewById(R.id.compose_email_data);
        userPreferences = getSharedPreferences(LoginActivity.USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void sendEmail(String from, String to, String password, String _data, String _subject) {
        sendEmailTask = new SendEmailTask(from, to, password, _data, _subject, this);
        sendEmailTask.execute((Void) null);
    }

}
