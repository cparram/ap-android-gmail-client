package cl.aleph.gmailclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class EmailDetailActivity extends AppCompatActivity {
    private EmailDetailTask emailDetailTask;
    private SharedPreferences userPreferences;
    private EmailDetail email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userPreferences = getSharedPreferences(LoginActivity.USER_PREFERENCES, Context.MODE_PRIVATE);
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        runEmailDetailTask(id);

    }

    private void runEmailDetailTask(String id) {
        int protocol = userPreferences.getInt(LoginActivity.USER_RETRIEVE_PROTOCOL, EmailModel.IMAP);
        String pass = userPreferences.getString(LoginActivity.USER_PASSWORD, null);
        String email = userPreferences.getString(LoginActivity.USER_EMAIL, null);
        emailDetailTask = new EmailDetailTask(protocol, email, pass, id, this);
        emailDetailTask.execute();
    }

    public void onPostExecuteEmailDetailTask(EmailDetail email) {
        this.email = email;
        TextView body = (TextView) findViewById(R.id.body);
        body.setText(email.getBody());
    }

}
