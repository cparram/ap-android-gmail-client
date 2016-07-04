package cl.aleph.gmailclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class EmailDetailActivity extends AppCompatActivity {
    public static int IMAP = 0;
    public static int POP3 = 1;
    public static String IMAP_URL = "imap.gmail.com";
    public static int IMAP_PORT = 993;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String id = intent.getStringExtra("id");
        Log.i("EMAIL FROM DETAIL: ", email);
        Log.i("PASSWORD FROM DETAIL", password);
        Log.i("ID FROM DETAIL", id);

        getEmailDetails(email, password, id);

    }

    private void getEmailDetails(String email, String pass, String id) {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            SSLSocket sslsocket = (SSLSocket) factory.createSocket(IMAP_URL, IMAP_PORT);
            DataOutputStream os = new DataOutputStream(sslsocket.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
            readLineUntilStartWith("* OK", is);
            write(IMAP, "TAG LOGIN " + email + " " + pass + "\r\n", os);
            readLineUntilStartWith("TAG OK", is);
            os.close();
            is.close();
            sslsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

}
