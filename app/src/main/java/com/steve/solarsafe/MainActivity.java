package com.steve.solarsafe;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends Activity {

    static MenuItem disconnect;
    protected static final String CONFIG = "config.txt";
    protected static final String SOLAR_SAFE = "SolarSafe";
    protected static String FILES_DIR;
    protected static String EXT_FILES_DIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FILES_DIR = getFilesDir().getAbsolutePath();
        EXT_FILES_DIR = Environment.getExternalStoragePublicDirectory("").getAbsolutePath();
        init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final ActionBar actionBar = getActionBar();
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        if (actionBar != null) {
                            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                            actionBar.setSelectedNavigationItem(position);
                        }
                    }
                });
        disconnect = menu.findItem(R.id.disconnect);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(Bluetooth.connectedThread != null){
            disconnect.setEnabled(true);
            disconnect.setVisible(true);
        } else {
            disconnect.setEnabled(false);
            disconnect.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button1, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.action_settings:
                startActivity( new Intent(this, Settings.class) );
                break;
            case R.id.action_devices:
                startActivity( new Intent(this, Bluetooth.class) );
                break;
            case R.id.action_info:
                startActivity( new Intent(this, Info.class) );
                break;
            case R.id.disconnect:
                Bluetooth.disconnect();
                disconnect.setEnabled(false);
                disconnect.setVisible(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(){
        //this procedure works for small group of devices
        File file = new File(FILES_DIR, CONFIG);
        if(!file.exists()){
            String[] MAC_s = { "10:00:E8:C2:E6:D8;0.0003;-0.0345",
                               "10:00:E8:C2:E6:EA;0.0002;-0.0139"};
            try {
                BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter(file) );
                for(String s : MAC_s){
                    bufferedWriter.write(s);
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(isExternalStorageWritable()) {
            File ex_file = new File(EXT_FILES_DIR, SOLAR_SAFE);
            if (!ex_file.exists()) {
                if (!ex_file.mkdir()) {
                    Log.e("MainActivity", "External Directory not created");
                }
            }
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state); //return true if is RW_mode
    }

    @Override
    public void onBackPressed() {
        if (Bluetooth.connectedThread != null) {
            Bluetooth.connectedThread.write("Q");}//Stop streaming
        super.onBackPressed();
    }

    //--------------------------------------------------------------------------------------------//
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position){
                case 0:
                    return Meter.newInstance("", "");
                case 1:
                    return Graph.newInstance("", "");
                default:
                    return PlaceholderFragment.newInstance(0);
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
            }
            return null;
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(R.layout.activity_main, container, false);
        }
    }
}
