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

    public EmailDetailTask(int protocol, String email, String pass, String id, EmailDetailActivity detailActivity) {
        this.listener = detailActivity;
        this.protocol = protocol;
        this.email = email;
        this.pass = pass;
        this.id = id;
    }

    @Override
    protected EmailDetail doInBackground(Void... voids) {
        return EmailDetail.getEmailDetails(email, pass, id);
    }

    @Override
    protected void onPostExecute(final EmailDetail email) {
        listener.onPostExecuteEmailDetailTask(email);
    }
}
