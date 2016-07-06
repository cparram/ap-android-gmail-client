package cl.aleph.gmailclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cl.aleph.gmailclient.imap.*;
import cl.aleph.gmailclient.smtp.ComposeEmailActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String DEFAULT_MESSAGE = "cl.aleph.gmailclient.default_message";
    private SharedPreferences userPreferences;
    private List<EmailModel> emails = new ArrayList<>();
    private cl.aleph.gmailclient.imap.EmailListTask emailListTask;
    private ListView emailList;
    private View mProgressView;
    private IDLETask idleTask; /* Only to IMAP */
    private EmailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent compose = new Intent(MainActivity.this, ComposeEmailActivity.class);
                startActivity(compose);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        // Set text of user email
        TextView vUserEmail = (TextView) header.findViewById(R.id.user_email);
        userPreferences = getSharedPreferences(LoginActivity.USER_PREFERENCES, Context.MODE_PRIVATE);
        final String email = userPreferences.getString(LoginActivity.USER_EMAIL, "default");
        String pass = userPreferences.getString(LoginActivity.USER_PASSWORD, null);
        Integer protocol = userPreferences.getInt(LoginActivity.USER_RETRIEVE_PROTOCOL, -1);

        if (protocol == EmailModel.IMAP) {
            /* enable configuration of IDLE */
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.getItem(1);
            item.setVisible(true);

        }
        vUserEmail.setText(email);
        emailList = (ListView) findViewById(R.id.emailList);
        // Event when click on one item, go to detail email activity
        emailList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        EmailModel item = (EmailModel) parent.getItemAtPosition(position);
                        Intent intent = new Intent(MainActivity.this, EmailDetailActivity.class);
                        // put id of email in extra intent, necessary to get the details
                        intent.putExtra("from", item.getFrom());
                        intent.putExtra("date", item.getDate());
                        intent.putExtra("subject", item.getJoinedSubject());
                        intent.putExtra("id", Integer.toString(item.getId()));
                        startActivity(intent);
                    }
                }
        );

        adapter = new EmailAdapter(this, this.emails);
        emailList.setAdapter(adapter);
        mProgressView = findViewById(R.id.email_list_progress);
        runEmailListTask();
        // Start idle task
        idleTask = new IDLETask(email, pass, this);
        idleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Display messages from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString(DEFAULT_MESSAGE);
            if (value != null) {
                Snackbar.make(findViewById(R.id.emailList), value, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            /*emailList.setVisibility(show ? View.GONE : View.VISIBLE);
            emailList.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    emailList.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            //emailList.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (emailListTask != null) {
            emailListTask.cancel(true);
            emailListTask = null;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_log_out) {
            logout();
        } else if (id == R.id.nav_dir_inbox) {
            runEmailListTask();
        } else if (id == R.id.toogle_idle) {
            if (idleTask != null) {
                idleTask.canceled = !idleTask.canceled;
                if (idleTask.canceled)
                    item.setTitle("Desactivado");
                else
                    item.setTitle("Activado");
            }
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void logout() {
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.clear();
        editor.commit();
        Intent mainActivityIntent = new Intent(MainActivity.this, LoginActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainActivityIntent);
        finish();
    }

    private void runEmailListTask() {
        this.emails.clear(); // remove all emails
        adapter.notifyDataSetChanged(); // refresh view

        showProgress(true);
        String pass = userPreferences.getString(LoginActivity.USER_PASSWORD, null);
        String email = userPreferences.getString(LoginActivity.USER_EMAIL, null);
        emailListTask = new cl.aleph.gmailclient.imap.EmailListTask(email, pass, this) ;
        emailListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onPostExecuteEmailListTask() {
        emailListTask = null;
        // Update list view
        adapter.notifyDataSetChanged();
        // Remove progress bar
        showProgress(false);
    }

    public void onCancelledEmailListTask() {
        emailListTask = null;
        showProgress(false);
    }

    /**
     * Insert new email at the beginning of the list
     * @param email
     */
    public void putNewEmail(EmailModel email) {
        this.emails.add(email); // Update list of emails
        adapter.notifyDataSetChanged(); // Update list view
    }
}
