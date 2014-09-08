package by.bigsoft.brazer.quakeviewer2;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import by.org.cgm.quake.QuakeContent.QuakeItem;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private final String TAG_LOG = "MainActivity";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG_LOG, "onCreate");

        mContext = this;
        initDB();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private void initDB() {
        DataBaseHelper.newInstance(this);
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Log.d(TAG_LOG, "onNavigationDrawerSelected");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
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
                mTitle = getString(R.string.title_section_localFS);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        private static PullToRefreshListView list;
        private LinkedList<String> mListItems;

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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            list = (PullToRefreshListView) rootView.findViewById(R.id.pull_to_refresh_list);
            /*List<QuakeItem> quakes = new ArrayList<QuakeItem>();
            QuakeAdapter adapter = new QuakeAdapter(MainActivity.getContext(), quakes);
            list.setAdapter(adapter);*/
            list.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new GetDataTask().execute();
                }
            });
            mListItems = new LinkedList<String>();
            mListItems.addAll(Arrays.asList(mStrings));

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    MainActivity.getContext(),
                    android.R.layout.simple_list_item_1, mListItems
            );

            list.setAdapter(adapter);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        private String[] mStrings = {
                "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam",
                "Abondance", "Ackawi", "Acorn", "Adelost", "Affidelice au Chablis",
                "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
                "Allgauer Emmentaler"};

        private class GetDataTask extends AsyncTask<Void, Void, String[]> {

            @Override
            protected String[] doInBackground(Void... params) {
                // Simulates a background job.
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
                return mStrings;
            }

            @Override
            protected void onPostExecute(String[] result) {
                mListItems.addFirst("Added after refresh...");

                // Call onRefreshComplete when the list has been refreshed.
                //((PullToRefreshListView) getListView()).onRefreshComplete();
                list.onRefreshComplete();

                super.onPostExecute(result);
            }
        }

    }

    private static class QuakeAdapter extends ArrayAdapter<QuakeItem> implements Serializable {

        private final List<QuakeItem> data;

        public QuakeAdapter(Context context, List<QuakeItem> quakes)
        {
            super(context, android.R.layout.simple_list_item_2, android.R.id.text1, quakes);
            data = quakes;
        }
//todo: ViewHolder?
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final View view = super.getView(position, convertView, parent);
            final TextView subText = (TextView) view.findViewById(android.R.id.text2);
            final QuakeItem quake = data.get(position);
            subText.setText(quake.content);
            return view;
        }
    }

}
