package com.isbit.m;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends FragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, FundsFragment.OnFragmentInteractionListener, DepositFragment.OnFragmentInteractionListener, TradeFragment.OnFragmentInteractionListener, SetActionbarInformation {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.funds_activity);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        int number = position+1;

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        switch (number){
            case 1:
                fragmentManager.beginTransaction().replace(R.id.container, FundsFragment.newInstance(position + 1)).commit();
                break;
            case 2:
                fragmentManager.beginTransaction().replace(R.id.container, TradeFragment.newInstance("one","two change this")).commit();
                break;
            case 3:
                fragmentManager.beginTransaction().replace(R.id.container, DepositFragment.newInstance("","")).commit();
                break;
            case 4:
                DS ds = new DS(MainActivity.this);
                ds.open();
                ds.erase();
                ds.close();

                finish();
                break;


        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = "Salir";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void setActionbarTitle(String tile) {
        ActionBar ab = getActionBar();
        ab.setTitle(tile);
    }
    @Override
    public void setActionbarSubtitle(String subtitle) {
        ActionBar ab = getActionBar();
        ab.setSubtitle(subtitle);
    }
}
