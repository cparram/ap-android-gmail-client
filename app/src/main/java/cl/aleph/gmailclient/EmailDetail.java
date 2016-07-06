package cl.aleph.gmailclient;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by matiasaraya on 7/2/16.
 */
public class EmailDetail {
    public static int IMAP = 0;
    public static int POP3 = 1;
    public static String IMAP_URL = "imap.gmail.com";
    public static int IMAP_PORT = 993;
    public static String POP3_URL = "pop.gmail.com";
    public static int POP3_PORT = 995;

    private String from;
    private String subject;
    private String body;
    private String date;

    public EmailDetail(String from, String subject, String date, String body) {
        this.from = from;
        this.subject = subject;
        this.date = date;
        this.body = body;
    }

    public static EmailDetail getEmailDetails(String email, String pass, String id, String from, String subject, String date, int protocol) {
        EmailDetail emailDetail = null;
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        if (protocol == EmailDetail.IMAP) {
            try {
                SSLSocket sslsocket = (SSLSocket) factory.createSocket(IMAP_URL, IMAP_PORT);
                DataOutputStream os = new DataOutputStream(sslsocket.getOutputStream());
                BufferedReader is = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
                readLineUntilStartWith(protocol, "* OK", is);
                write(protocol, "TAG LOGIN " + email + " " + pass + "\r\n", os);
                readLineUntilStartWith(protocol, "TAG OK", is);
                write(protocol, "TAG SELECT \"INBOX\"\r\n", os);
                readLineUntilStartWith(protocol, "TAG OK", is);
                write(protocol, "TAG FETCH " + id + " BODY[TEXT]\r\n", os);
                readLine(protocol, is);
                String body = "";
                // Skip the first line (GMAIL response not necessary stuff)
                String line = "";
                while (!line.startsWith("TAG OK")) {
                    body += line;
                    body += "\n";
                    line = readLine(protocol, is);
                }
                os.close();
                is.close();
                sslsocket.close();
                emailDetail = new EmailDetail(from, subject, date, body);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return emailDetail;
        } else if(protocol == EmailDetail.POP3) {
            try {
                SSLSocket sslsocket = (SSLSocket) factory.createSocket(POP3_URL, POP3_PORT);
                DataOutputStream os = new DataOutputStream(sslsocket.getOutputStream());
                BufferedReader is = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
                readLineUntilStartWith(protocol, "+OK", is);
                write(protocol, "USER " + email + "\r\n", os);
                readLineUntilStartWith(protocol, "+OK", is);
                write(protocol, "PASS " + pass + "\r\n", os);
                readLineUntilStartWith(protocol, "+OK", is);
                write(protocol, "RETR " + id + "\r\n", os);
                String line = readLine(protocol, is);
                while (!line.equals("")){
                    line = readLine(protocol, is);
                }
                String body = "";
                while (!line.startsWith(".")) {
                    body += line;
                    body += "\n";
                    line = readLine(protocol, is);
                }
                os.close();
                is.close();
                sslsocket.close();
                emailDetail = new EmailDetail(from, subject, date, body);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return emailDetail;
        }

        return emailDetail;
    }

    private static void write(int protocol, String msg, DataOutputStream os) throws IOException {
        Log.i(protocol == IMAP ? "imap" : "pop3", "C: " + msg.replace("\r\n", ""));
        os.writeBytes(msg);

    }

    private static String readLineUntilStartWith(int protocol, String s, BufferedReader is) throws IOException {
        String msg = readLine(protocol, is);
        while (!msg.startsWith(s)) {
            msg = readLine(protocol, is);
        }
        return msg;
    }

    private static String readLine(int protocol, BufferedReader is) throws IOException {
        String line = is.readLine();
        Log.i(protocol == IMAP ? "imap" : "pop3", "S: " + line);
        return line;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }
}
