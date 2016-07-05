package cl.aleph.gmailclient;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.List;

/**
 * Created by matiasaraya on 7/2/16.
 */
public class EmailDetailTask extends AsyncTask<Void, Void, EmailDetail> {
    private final String email;
    private final String pass;
    private final String id;
    private int protocol;
    private EmailDetailActivity listener;
    private final String from;
    private final String subject;
    private final String date;

    public EmailDetailTask(int protocol, String email, String pass, String id, String from, String subject, String date, EmailDetailActivity detailActivity) {
        this.listener = detailActivity;
        this.protocol = protocol;
        this.email = email;
        this.pass = pass;
        this.id = id;
        this.from = from;
        this.subject = subject;
        this.date = date;
    }

    @Override
    protected EmailDetail doInBackground(Void... voids) {
        return EmailDetail.getEmailDetails(email, pass, id, from, subject, date);
    }

    @Override
    protected void onPostExecute(final EmailDetail email) {
        listener.onPostExecuteEmailDetailTask(email);
    }
}
