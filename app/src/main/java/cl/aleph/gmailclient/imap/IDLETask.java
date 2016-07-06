package cl.aleph.gmailclient.imap;

import android.os.AsyncTask;
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
 * Created by cesar[dot]parramoreno[at]gmail[dot]com on 7/5/16.
 */
public class IDLETask extends BaseIMAPTask {
    public boolean canceled = true;

    public IDLETask(String email, String pass, MainActivity listener) {
        super(email, pass, listener);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        String msg = null;
        try {
            SSLSocket socket = (SSLSocket) factory.createSocket(EmailModel.IMAP_URL, EmailModel.IMAP_PORT);
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            readLineUntilStartWith("* OK", is);
            write("TAG LOGIN " + email + " " + pass + "\r\n", os);
            readLineUntilStartWith("TAG OK", is);
            write("TAG SELECT \"INBOX\"\r\n", os);
            readLineUntilStartWith("TAG OK", is);
            write("TAG IDLE\r\n", os);
            readLine(is);
            while((msg = readLine(is)) != null){
                if (!canceled) {
                    int emailIndex = Integer.parseInt(msg.split("\\s")[1]);
                    publishProgress(getNewEmail(emailIndex));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(EmailModel... values) {
        listener.putNewEmail(values[0]);
    }

}
