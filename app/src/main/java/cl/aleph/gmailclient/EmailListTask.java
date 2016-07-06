package cl.aleph.gmailclient;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

/**
 * Created by root on 6/25/16.
 */
public class EmailListTask extends AsyncTask<Void, Void, List<EmailModel>> {
    private final String email;
    private final String pass;
    private int protocol;
    private MainActivity listener;

    public EmailListTask(int protocol, String email, String pass, MainActivity mainActivity) {
        this.listener = mainActivity;
        this.protocol = protocol;
        this.email = email;
        this.pass = pass;
    }


    @Override
    protected List<EmailModel> doInBackground(Void... voids) {
        return EmailModel.getHeaderEmails(10, protocol, email, pass);
    }

    @Override
    protected void onPostExecute(final List<EmailModel> emails) {
        listener.onPostExecuteEmailListTask();
    }

    @Override
    protected void onCancelled() {
        listener.onCancelledEmailListTask();
    }
}
