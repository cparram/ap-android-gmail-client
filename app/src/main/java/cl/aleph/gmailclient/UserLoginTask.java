package cl.aleph.gmailclient;

/**
 * Created by root on 6/22/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
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
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private final String email;
    private final String password;
    private LoginActivity listener;
    private String error;

    UserLoginTask(String email, String password, LoginActivity listener) {
        this.email = email;
        this.password = password;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        boolean auth = false;
        String response = "";
        try {
            String encode = "\000"+ email +"\000"+ password;
            String loginPlain = Base64.encodeToString(encode.getBytes(), Base64.NO_WRAP);

            SSLSocket sslsocket = (SSLSocket) factory.createSocket("smtp.gmail.com", 465);
            DataOutputStream os = new DataOutputStream(sslsocket.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
            response = is.readLine();
            os.writeBytes("HELO localhost\r\n");
            response = is.readLine();
            os.writeBytes("AUTH PLAIN "+loginPlain+"\r\n" );
            response = is.readLine();
            if (response.startsWith("235")) {
                auth = true;
            } else {
                error = response;
            }
            os.close();
            is.close();
            sslsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return auth;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            listener.onSuccessLoginTask();
        } else {
            listener.onFailureLoginTask(error);
        }
    }

    @Override
    protected void onCancelled() {
        listener.onCancelledLoginTask();
    }
}
