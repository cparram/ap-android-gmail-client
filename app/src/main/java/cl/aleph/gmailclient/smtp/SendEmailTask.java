package cl.aleph.gmailclient.smtp;

import android.os.AsyncTask;

/**
 * Created by root on 7/4/16.
 */
public class SendEmailTask extends AsyncTask<Void, Void, String> {
    private final String from;
    private final String to;
    private final String password;
    private final ComposeEmailActivity listener;

    public SendEmailTask(String from, String to, String password, ComposeEmailActivity listener) {
        this.from = from;
        this.to = to;
        this.password = password;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        //ToDo: implements smtp messages
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        listener.onPostExecuteSuccess();
    }

    @Override
    protected void onCancelled() {
        // ToDo: close socket
        listener.onCancelledEmailListTask();
    }
}
