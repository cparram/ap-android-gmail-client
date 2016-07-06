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
    public static String POP3_URL = "pop.gmail.com";
    public static int POP3_PORT = 995;

    private String from;
    private List<String> subject;
    private String date;
    private int id;

    public EmailModel(String from, List<String> subject, String date, int id) {
        this.from = from;
        this.subject = subject;
        this.date = date;
        this.id = id;
    }

    /**
     * @param count
     * @param protocol
     * @param email
     * @param pass     @return
     */
    public static List<EmailModel> getHeaderEmails(int count, int protocol, String email, String pass) {
        List<EmailModel> emails = new ArrayList<>(count);
        // dummy values
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        String msg = null;
        if (protocol == EmailModel.IMAP) {
            try {
                SSLSocket sslsocket = (SSLSocket) factory.createSocket(IMAP_URL, IMAP_PORT);
                DataOutputStream os = new DataOutputStream(sslsocket.getOutputStream());
                BufferedReader is = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
                readLineUntilStartWith(protocol, "* OK", is);
                write(protocol, "TAG LOGIN " + email + " " + pass + "\r\n", os);
                readLineUntilStartWith(protocol, "TAG OK", is);
                write(protocol, "TAG SELECT \"INBOX\"\r\n", os);
                int emailCount = 0;
                msg = readLine(protocol, is);
                while (!msg.startsWith("TAG OK")) {
                    if (msg.endsWith("EXISTS")) {
                        emailCount = Integer.parseInt(msg.split("\\s")[1]);
                    }
                    msg = readLine(protocol, is);
                }
                int finish = emailCount - count < 0 ? 0 : emailCount - count;
                int initial = emailCount;
                while (finish != initial) {
                    Map<String, String> fields = new HashMap<>();
                    String[] headerFields = new String[]{"FROM", "DATE"};
                    for (String field : headerFields) {
                        write(protocol, String.format("TAG FETCH %d (BODY[HEADER.FIELDS (%s)])\r\n", initial, field), os);
                        readLine(protocol, is);
                        String _field = field.substring(0, 1).toUpperCase() + field.substring(1).toLowerCase();
                        fields.put(_field, readLine(protocol, is).split(_field + ":")[1]);
                        readLineUntilStartWith(protocol, "TAG OK", is);
                    }
                    // Subject value can be more than one line with different encodes
                    List<String> subjectValues = new LinkedList<>();
                    write(protocol, String.format("TAG FETCH %d BODY[HEADER.FIELDS (SUBJECT)]\r\n", initial), os);
                    readLine(protocol, is);
                    msg = readLine(protocol, is).split("Subject:")[1];
                    subjectValues.add(msg);

                    msg = readLine(protocol, is);
                    while (!msg.equals(")")) {
                        subjectValues.add(msg);
                        msg = readLine(protocol, is);
                    }
                    readLineUntilStartWith(protocol, "TAG OK", is);

                    emails.add(new EmailModel(fields.get("From"), subjectValues, fields.get("Date"), initial));
                    initial--;
                }
                os.close();
                is.close();
                sslsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return emails;
        } else if(protocol == EmailModel.POP3) {
            try {
                SSLSocket sslsocket = (SSLSocket) factory.createSocket(POP3_URL, POP3_PORT);
                DataOutputStream os = new DataOutputStream(sslsocket.getOutputStream());
                BufferedReader is = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
                readLineUntilStartWith(protocol, "+OK", is);
                write(protocol, "USER " + email + "\r\n", os);
                readLineUntilStartWith(protocol, "+OK", is);
                write(protocol, "PASS " + pass + "\r\n", os);
                readLineUntilStartWith(protocol, "+OK", is);
                // Checking how many email are available for POP3
                write(protocol, "STAT " + pass + "\r\n", os);
                String stat = readLine(protocol, is);
                String[] split = stat.split(" ");
                int emailCount = Integer.parseInt(split[1]);
                int finish;
                int initial = emailCount;
                if (emailCount > count) {
                    finish = emailCount - 10;
                } else {
                    finish = 1;
                }
                while (finish != initial) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("TOP ");
                    sb.append(Integer.toString(initial));
                    sb.append(" ");
                    sb.append("0");
                    sb.append("\r\n");
                    write(protocol, sb.toString(), os);
                    int allSet = 0;
                    String date = "";
                    String from = "";
                    List<String> subjectValues = new LinkedList<>();
                    while (allSet != 3) {
                        msg = readLine(protocol, is);
                        if (msg.startsWith("Date:")) {
                            date = msg.split("Date:")[1];
                            allSet++;
                        } else if (msg.startsWith("From: ")) {
                            from = msg.split("From:")[1];
                            allSet++;
                        } else if (msg.startsWith("Subject: ")) {
                            subjectValues.add(msg.split("Subject:")[1]);
                            msg = readLine(protocol, is);
                            // TODO: FIX THIS!!! NO ALWAYS START WITH =?UTF-8?
                            while (msg.startsWith("=?UTF-8?")) {
                                subjectValues.add(msg);
                                msg = readLine(protocol, is);
                            }
                            allSet++;
                        }
                    }

                    emails.add(new EmailModel(from, subjectValues, date, initial));
                    initial--;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return emails;
        }

        return emails;
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

    public String getFrom() {
        return from;
    }

    public String getJoinedSubject() {
        return TextUtils.join(" ", subject);
    }

    public String getDate() {
        return date;
    }

    public int getId() {
        return id;
    }
}
