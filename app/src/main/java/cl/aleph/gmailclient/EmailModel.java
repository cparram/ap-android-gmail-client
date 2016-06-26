package cl.aleph.gmailclient;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by root on 6/24/16.
 */
public class EmailModel {
    public static int IMAP = 0;
    public static int POP3 = 1;
    public static String IMAP_URL = "imap.gmail.com";
    public static int IMAP_PORT = 993;

    private String from;
    private List<String> subject;
    private String date;

    public EmailModel(String from, List<String> subject, String date) {
        this.from = from;
        this.subject = subject;
        this.date = date;
    }

    /**
     *
     * @param count
     * @param protocol
     * @param email
     *@param pass @return
     */
    public static List<EmailModel> getHeaderEmails(int count, int protocol, String email, String pass) {
        List<EmailModel> emails  = new ArrayList<>(count);
        // dummy values
        SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        String msg = null;
        try {
            SSLSocket sslsocket = (SSLSocket) factory.createSocket(IMAP_URL, IMAP_PORT);
            DataOutputStream os = new DataOutputStream(sslsocket.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
            readLineUntilStartWith("* OK", is);
            write(IMAP, "TAG LOGIN " + email + " " + pass +"\r\n", os);
            readLineUntilStartWith("TAG OK", is);
            write(IMAP, "TAG SELECT \"INBOX\"\r\n", os);
            int emailCount = 0;
            msg = readLine(IMAP, is);
            while (!msg.startsWith("TAG OK")) {
                if (msg.endsWith("EXISTS")) {
                    emailCount = Integer.parseInt(msg.split("\\s")[1]);
                }
                msg = readLine(IMAP, is);
            }
            int finish = emailCount-count < 0 ? 0 : emailCount-count;
            int initial = emailCount;
            while (finish != initial) {
                Map<String, String> fields = new HashMap<>();
                String[] headerFields = new String[] {"FROM", "DATE"};
                for (String field : headerFields) {
                    write(IMAP, String.format("TAG FETCH %d (BODY[HEADER.FIELDS (%s)])\r\n", initial, field), os);
                    readLine(IMAP, is);
                    String _field = field.substring(0, 1).toUpperCase() + field.substring(1).toLowerCase();
                    fields.put(_field, readLine(IMAP, is).split(_field + ":")[1]);
                    readLineUntilStartWith("TAG OK", is);
                }
                // Subject value can be more than one line with different encodes
                List<String> subjectValues = new LinkedList<>();
                write(IMAP, String.format("TAG FETCH %d BODY[HEADER.FIELDS (SUBJECT)]\r\n", initial), os);
                readLine(IMAP, is);
                msg = readLine(IMAP, is).split("Subject:")[1];
                subjectValues.add(msg);

                msg = readLine(IMAP, is);
                while (!msg.equals(")")) {
                    subjectValues.add(msg);
                    msg = readLine(IMAP, is);
                }
                readLineUntilStartWith("TAG OK", is);

                emails.add(new EmailModel(fields.get("From"), subjectValues, fields.get("Date")));
                initial --;
            }
            os.close();
            is.close();
            sslsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emails;
    }

    private static void write(int protocol, String msg, DataOutputStream os) throws IOException {
        Log.i(protocol == IMAP ? "imap" : "pop3", "C: " + msg.replace("\r\n", ""));
        os.writeBytes(msg);

    }

    private static String readLineUntilStartWith(String s, BufferedReader is) throws IOException {
        String msg = readLine(IMAP, is);
        while (!msg.startsWith(s)) {
            msg = readLine(IMAP, is);
        }
        return msg;
    }

    private static String readLine(int protocol, BufferedReader is) throws IOException {
        String line = is.readLine();
        Log.i(protocol == IMAP ? "imap" : "pop3", "S: " + line);
        return line;
    }

    public String getFrom() { return from; }

    public String getJoinedSubject() {
        return TextUtils.join(" ", subject);
    }

    public String getDate() { return date; }
}
