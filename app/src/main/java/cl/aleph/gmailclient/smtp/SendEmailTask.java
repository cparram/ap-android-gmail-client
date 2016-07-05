package cl.aleph.gmailclient.smtp;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by cesar[dot]parramoreno[at]gmail[dot]com on 7/4/16.
 */
public class SendEmailTask extends AsyncTask<Void, Void, String> {
    private final String from;
    private final String to;
    private final String password;
    private final String data;
    private final String subject;
    private String cc;
    private final ComposeEmailActivity listener;
    private SSLSocket sslsocket;
    private DataOutputStream os;
    private BufferedReader is;

    public SendEmailTask(String from, String to, String password, String data, String subject, String cc, ComposeEmailActivity listener) {
        this.from = from;
        this.to = to;
        this.password = password;
        this.data = data;
        this.subject = subject;
        this.cc = cc;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        String response = null;
        cc = cc.replace(" ", "");
        try {
            String encode = "\000"+ from +"\000"+ password;
            String loginPlain = Base64.encodeToString(encode.getBytes(), Base64.NO_WRAP);

            sslsocket = (SSLSocket) factory.createSocket("smtp.gmail.com", 465);
            os = new DataOutputStream(sslsocket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

            readLine(is);
            write("HELO localhost\r\n", os);

            readLine(is);
            write("AUTH PLAIN "+loginPlain+"\r\n", os);
            response = readLine(is);
            if (!response.startsWith("235")) {
                return response;
            }
            write(String.format("MAIL FROM:<%s>\r\n", from), os);
            readLine(is);
            write(String.format("RCPT TO:<%s>\r\n", to), os);
            readLine(is);
            for(String email : cc.split(",")) {
                write(String.format("RCPT TO:<%s>\r\n", email), os);
                readLine(is);
            }
            write("DATA\r\n", os);
            write(String.format("From: <%s>\r\n", from), os);
            write(String.format("To: <%s>\r\n", to), os);
            write(String.format("Cc: %s\r\n", cc), os);
            write(String.format("Subject: %s\r\n", subject), os);
            write("\r\n", os);
            for(String line : data.split("\n")){
                write(line + "\r\n", os);
            }
            write("\r\n", os);
            write("Sent from GmailClient\r\n", os);
            write("\r\n.\r\n", os);
            response = readLine(is);

            os.close();
            is.close();
            sslsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Writes bites through the socket and creates a log
     * @param s
     * @param os
     * @throws IOException
     */
    private void write(String s, DataOutputStream os) throws IOException {
        Log.i("smtp", "C: " + s.replace("\r\n", ""));
        os.writeBytes(s);

    }

    /**
     * Reads a line through the socket and creates a log
     * @param is
     * @return
     * @throws IOException
     */
    private String readLine(BufferedReader is) throws IOException {
        String response = is.readLine();
        Log.i("smtp", "S: " + response);
        return response;
    }

    @Override
    protected void onPostExecute(String response) {
        listener.onPostSend(response);
    }
}
