package cl.aleph.gmailclient.smtp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import cl.aleph.gmailclient.LoginActivity;
import cl.aleph.gmailclient.MainActivity;
import cl.aleph.gmailclient.R;

public class ComposeEmailActivity extends AppCompatActivity {
    private SendEmailTask sendEmailTask;
    private SharedPreferences userPreferences;
    private EditText emailTo;
    private EditText subject;
    private EditText data;
    private EditText ccEmails;
    private View mProgressView;
    private View emailForm;
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
                sendEmail();
            }
        });
        emailTo = (EditText) findViewById(R.id.compose_email_to);
        subject = (EditText) findViewById(R.id.compose_email_subject);
        data = (EditText) findViewById(R.id.compose_email_data);
        userPreferences = getSharedPreferences(LoginActivity.USER_PREFERENCES, Context.MODE_PRIVATE);
        mProgressView = findViewById(R.id.send_email_progress);
        emailForm = findViewById(R.id.send_email_form);
        ccEmails = (EditText) findViewById(R.id.compose_email_cc);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            emailForm.setVisibility(show ? View.GONE : View.VISIBLE);
            emailForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    emailForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            emailForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Creates a send email task that will send the email
     */
    private void sendEmail() {
        String from = userPreferences.getString(LoginActivity.USER_EMAIL, null);
        String password = userPreferences.getString(LoginActivity.USER_PASSWORD, null);
        String to = emailTo.getText().toString();
        String _subject = subject.getText().toString();
        String _data = data.getText().toString();
        String cc = ccEmails.getText().toString();
        if (from == null || password == null) {
            //ToDO: send to login activity
        }
        boolean send = true;
        if (TextUtils.isEmpty(to)) {
            emailTo.setError(getString(R.string.error_field_required));
            emailTo.requestFocus();
            send = false;
        }
        if (TextUtils.isEmpty(_subject)) {
            subject.setError(getString(R.string.error_field_required));
            subject.requestFocus();
            send = false;
        }
        if (TextUtils.isEmpty(_data)) {
            data.setError(getString(R.string.error_field_required));
            data.requestFocus();
            send = false;
        }
        if (send) {
            showProgress(true);
            sendEmailTask = new SendEmailTask(from, to, password, _data, _subject, cc, this);
            sendEmailTask.execute((Void) null);
        }
    }

    /**
     * After send a email, the main activity will be started
     * @param response
     */
    public void onPostSend(String response) {
        showProgress(false);
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        main.putExtra(MainActivity.DEFAULT_MESSAGE, response);
        startActivity(main);
    }
}
