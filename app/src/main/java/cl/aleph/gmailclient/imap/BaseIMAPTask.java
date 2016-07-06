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
 * Created by cesar[dot]parramoreno[at]gmail[dot]com on 7/6/16.
 */
abstract class BaseIMAPTask extends AsyncTask<Void, EmailModel, Void> {
    protected final String email;
    protected final String pass;
    protected MainActivity listener;
    protected static final String INFO_TAG = "imap";

    public BaseIMAPTask(String email, String pass, MainActivity listener) {
        this.email = email;
        this.pass = pass;
        this.listener = listener;
    }

    /**
     * Creates new socket to get new email info
     * @param emailIndex
     */
    protected EmailModel getNewEmail(int emailIndex) {
        SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        EmailModel emailModel = null;
        try {
            SSLSocket socket = (SSLSocket) factory.createSocket(EmailModel.IMAP_URL, EmailModel.IMAP_PORT);
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            readLineUntilStartWith("* OK", is);
            write("TAG LOGIN " + email + " " + pass + "\r\n", os);
            readLineUntilStartWith("TAG OK", is);
            write("TAG SELECT \"INBOX\"\r\n", os);
            readLineUntilStartWith("TAG OK", is);
            emailModel = getNewEmail(socket, os, is, emailIndex);
            socket.close();
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emailModel;
    }

    /**
     * Gets new email by a socket already created
     */
    protected EmailModel getNewEmail(SSLSocket socket, DataOutputStream os, BufferedReader is,
                                     int index) throws IOException {
        Map<String, String> fields = new HashMap<>();
        String[] headerFields = new String[]{"FROM", "DATE"};
        for (String field : headerFields) {
            write(String.format("TAG FETCH %d (BODY[HEADER.FIELDS (%s)])\r\n", index, field), os);
            readLine(is);
            String _field = field.substring(0, 1).toUpperCase() + field.substring(1).toLowerCase();
            fields.put(_field, readLine(is).split(_field + ":")[1]);
            readLineUntilStartWith("TAG OK", is);
        }
        // Subject value can be more than one line with different encodes
        List<String> subjectValues = new LinkedList<>();
        write(String.format("TAG FETCH %d BODY[HEADER.FIELDS (SUBJECT)]\r\n", index), os);
        readLine(is);
        String msg = readLine(is).split("Subject:")[1];
        subjectValues.add(msg);

        msg = readLine(is);
        while (!msg.equals(")")) {
            subjectValues.add(msg);
            msg = readLine(is);
        }
        readLineUntilStartWith("TAG OK", is);
        EmailModel emailModel = new EmailModel(fields.get("From"), subjectValues, fields.get("Date"), index);
        return emailModel;
    }

    /**
     * Reads new line from socket
     * @param is
     * @return
     */
    protected String readLine(BufferedReader is) throws IOException {
        String line = is.readLine();
        Log.i(INFO_TAG, "S: " + line);
        return line;
    }

    /**
     * Write new string throught the socket
     * @param s
     * @param os
     */
    protected void write(String s, DataOutputStream os) throws IOException {
        Log.i(INFO_TAG, "C: " + s.replace("\r\n", ""));
        os.writeBytes(s);
    }

    /**
     * Reads all lines until a specific string
     * @param s
     * @param is
     */
    protected String readLineUntilStartWith(String s, BufferedReader is) throws IOException {
        String msg = readLine(is);
        while (!msg.startsWith(s)) {
            msg = readLine(is);
        }
        return msg;
    }

}
