package cl.aleph.gmailclient.imap;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import cl.aleph.gmailclient.EmailModel;
import cl.aleph.gmailclient.MainActivity;

/**
 * Created by cesar[dot]parramoreno[at]gmail[dot]com on 6/25/16.
 */
public class EmailListTask extends BaseIMAPTask {
    private final int COUNT = 10;
    private SSLSocket socket;
    private DataOutputStream os;
    private BufferedReader is;

    public EmailListTask(String email, String pass, MainActivity listener) {
        super(email, pass, listener);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        String msg;
        try {
            socket = (SSLSocket) factory.createSocket(EmailModel.IMAP_URL, EmailModel.IMAP_PORT);
            os = new DataOutputStream(socket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            readLineUntilStartWith("* OK", is);
            write("TAG LOGIN " + email + " " + pass +"\r\n", os);
            readLineUntilStartWith("TAG OK", is);
            write("TAG SELECT \"INBOX\"\r\n", os);
            int emailCount = 0;
            msg = readLine(is);
            while (!msg.startsWith("TAG OK")) {
                if (msg.endsWith("EXISTS")) {
                    emailCount = Integer.parseInt(msg.split("\\s")[1]);
                }
                msg = readLine(is);
            }
            int finish = emailCount - COUNT < 0 ? 0 : emailCount - COUNT;
            int initial = emailCount;
            while (finish != initial && !isCancelled()) {
                publishProgress(getNewEmail(socket, os, is, initial));
                initial --;
            }
            os.close();
            is.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(EmailModel... values) {
        listener.putNewEmail(values[0]);
    }

    @Override
    protected void onPostExecute(Void values) {
        listener.onPostExecuteEmailListTask();
    }

    @Override
    protected void onCancelled() {
        Log.i(BaseIMAPTask.INFO_TAG, "on canceled email list task");
        listener.onCancelledEmailListTask();
    }
}
