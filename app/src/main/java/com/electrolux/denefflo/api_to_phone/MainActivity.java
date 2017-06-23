package com.electrolux.denefflo.api_to_phone;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import PLC.PlcReader;
import PLC.PlcWriter;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private EditText editText;
    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * Below, it gets the toolbar, set the support by adding the toolbar to the UI,
         * then get the drawer layout and create the toggle, the thing that will do the interaction
         * with the drawer layout
         * The navigation view get its id and wait for an action on the button inside the navigation
         * menu
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_log);
        navigationView.setNavigationItemSelectedListener(this);

        editText = (EditText) findViewById(R.id.login);
        Button button = (Button) findViewById(R.id.button);
        //Get the email TextView from the navigation header
        email = navigationView.getHeaderView(0).findViewById(R.id.supportMail);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect(editText);
            }
        });
        email.setOnClickListener(new TextView.OnClickListener() {

            @Override
            public void onClick(View v) {
                emailSupport();
            }
        });
    }

    //Click on button to go to the PLC activity
    private void Connect(EditText editText) {
        /*
         * Charging the address and password from the EditText array.
         */
        final PlcWriter plcWriter = new PlcWriter(
                editText.getText().toString(),
                null,
                true //simpleConnect is true
        );

        final PlcReader plcReader = new PlcReader(plcWriter.getAddress(), plcWriter.getPassword(), false);

        try {
            new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... objects) {
                    plcWriter.run();
                    return plcWriter.getMessageErr() == 0;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    if (!(boolean) o)
                        dealError(plcWriter.getMessageErr());
                    else {
                        Intent intent = new Intent(getApplicationContext(), PlcActivity.class);
                        intent.putExtra(PlcActivity.PLCWRITER, plcWriter);
                        intent.putExtra(PlcActivity.PLCREADER, plcReader);
                        startActivity(intent);
                        finish();
                    }
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
            dealError(e.toString());
        }
    }

    private void emailSupport() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));//Only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email.getText().toString()});//It works
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.about_main:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.about_main:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void dealError(String error) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title)
                .setMessage(error)
                .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    /**
     * It makes popped an error window, displaying the error message and a button to close it.
     *
     * @param errorId the id of the string to display
     */
    public void dealError(int errorId) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title)
                .setMessage(errorId)
                .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }
}