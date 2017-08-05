package com.example.denefflo.api_to_phone;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import PLC.PlcReader;
import PLC.PlcWriter;

public class PlcActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        PlcFragment.OnFragmentInteractionListener,
        OutputFragment.OnFragmentInteractionListener,
        StatsFragment.OnFragmentInteractionListener,
        TestFragment.OnFragmentInteractionListener {

    public static final String PLCWRITER = "plcWriter";
    public static final String PLCREADER = "plcReader";

    private TextView email;
    private PlcWriter plcWriter;
    private PlcReader plcReader;
    private Fragment fragment = null;
    private Class fragmentClass;
    private Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);
        //Get the Intent that started this activity and its content
        try {
            plcWriter = getIntent().getParcelableExtra(PLCWRITER);
            plcReader = getIntent().getParcelableExtra(PLCREADER);
        } catch (Exception e) {
            e.printStackTrace();
            dealError(e.toString());
        }
        plcWriter.setSimpleConnect(false);
        plcReader.setSimpleConnect(false);
        try {
            bundle.putParcelable(PLCWRITER, plcWriter);
            bundle.putParcelable(PLCREADER, plcReader);
        } catch (Exception e) {
            e.printStackTrace();
            dealError(e.toString());
        }
        /*
        * Below, it gets the toolbar, set the support by adding the toolbar to the UI,
        * then get the drawer layout and create the toggle, the thing that will do the interaction
        * with the drawer layout
        * The navigation view get its id and wait for an action on the button inside the navigation
        * menu
        */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentClass = TestFragment.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(bundle);
        } catch (Exception e) {
            e.printStackTrace();
            dealError(e.toString());
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flContent, fragment)
                .commit();
        setTitle(R.string.item_title_test_plc);
        getTitle();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_api);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        email = navigationView.getHeaderView(0).findViewById(R.id.supportMail);
        email.setOnClickListener(new TextView.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                emailSupport();
            }
        });
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (fragmentClass == PlcFragment.class)
            setTitle(R.string.item_api_activity);
        else if (fragmentClass == StatsFragment.class)
            setTitle(R.string.item_stats_activity);
        else if (fragmentClass == OutputFragment.class)
            setTitle(R.string.item_output_activity);
        else if (fragmentClass == TestFragment.class) {
            setTitle(R.string.item_title_test_plc);
            getTitle();
        }

    }


    /**
     * It adds the possibility to prepare an e-mail by clicking on the e-mail address.
     */
    private void emailSupport() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);//Intention to send something
        intent.setData(Uri.parse("mailto:"));//only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email.getText().toString()});//Prepare the mail by adding the address
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);//Start the e-mail client
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.item_settings) {
            intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.item_about) {
            intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        } else {
            switch (item.getItemId()) {
                case R.id.item_stats:
                    fragmentClass = StatsFragment.class;
                    break;
                case R.id.item_output:
                    fragmentClass = OutputFragment.class;
                    break;
                case R.id.item_api:
                    fragmentClass = PlcFragment.class;
                    break;
                case R.id.item_test_plc:
                    fragmentClass = TestFragment.class;
                    break;
                default:
                    fragmentClass = OutputFragment.class;
                    break;
            }
            try {
                bundle = fragment.getArguments();
                fragment = (Fragment) fragmentClass.newInstance();
                fragment.setArguments(bundle);
            } catch (Exception e) {
                e.printStackTrace();
                dealError(e.toString());
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.flContent, fragment)
                    .commit();
        }

        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_api);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * It makes popped an error window, displaying the error message and a button to close it.
     *
     * @param error the error to display
     */
    public void dealError(String error) {
        new AlertDialog.Builder(getApplicationContext())
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

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
