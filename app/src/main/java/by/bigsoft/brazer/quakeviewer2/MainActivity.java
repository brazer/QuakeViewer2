package by.bigsoft.brazer.quakeviewer2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import by.org.cgm.jdbf.JdbfTask;
import by.org.cgm.quake.QuakeContent;
import by.org.cgm.quake.QuakeContent.QuakeItem;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks{

    private static final String TAG_LOG = "MainActivity";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private static Context mContext;
    private static OpenFileDialog fileDialog;
    private static QuakeAdapter mQuakeAdapter;
    private static Activity mActivity;
    private static SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG_LOG, "onCreate");

        mContext = this;
        mActivity = this;
        initFirst();
        initDB();
        fileDialog = new OpenFileDialog(this);
        fileDialog.setFolderIcon(getResources().getDrawable(R.drawable.abc_ic_go));
        if (savedInstanceState!=null) {
            OpenFileDialog.setIsClosed(savedInstanceState.getBoolean("isClosed"));
            mQuakeAdapter = (QuakeAdapter) savedInstanceState.getSerializable("adapter");
            if (!OpenFileDialog.isClosed()) {
                String path = savedInstanceState.getString("path");
                showOpenFileDialog(path);
            }
        }
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private void initFirst() {
        if (isFirstStarted()) {
            Log.i(TAG_LOG, "First start");
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setTitle(getString(R.string.title_first_message));
            dlgAlert.setMessage(getString(R.string.first_message));
            dlgAlert.setPositiveButton(android.R.string.ok, null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }
    }

    private static boolean isFirstStarted() {
        mSharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean("first_start", true);
    }

    private void initDB() {
        DataBaseHelper.newInstance(this);
        if (isFirstStarted()) {
            if (!isInternetConnected()) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
                dlgAlert.setTitle(getString(R.string.title_first_message));
                dlgAlert.setMessage("Check internet connection");
                dlgAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(-1);
                    }
                });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            } else Log.i(TAG_LOG, "Internet is connected. May be :(");
            final AsyncTaskManager manBLR = new AsyncTaskManager(this, new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete(JdbfTask task) {
                    if (JdbfTask.records==null) return;
                    for (JdbfTask.QuakeRecord rec : JdbfTask.records)
                        DataBaseHelper.addQuake(rec);
                }
            });
            AsyncTaskManager manEarth = new AsyncTaskManager(this, new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete(JdbfTask task) {
                    if (JdbfTask.records==null) {
                        Toast.makeText(getContext(), "Please, check internet connection", Toast.LENGTH_SHORT).show();
                        manBLR.onCancel(null);
                    } else {
                        for (JdbfTask.QuakeRecord rec : JdbfTask.records)
                            DataBaseHelper.addQuake(rec);
                        DataBaseTask.setOnTaskCompleteListener(PlaceholderFragment.getPlaceholderFragment());
                    }
                    mSharedPreferences.edit().putBoolean("first_start", false).commit();
                    new DataBaseTask().execute(0);
                }
            });
            manEarth.setupTask(new JdbfTask(getResources()), Constants.URL_EARTH);
            manBLR.setupTask(new JdbfTask(getResources()), Constants.URL_BLR);
        }
    }

    private boolean isInternetConnected() {
        ConnectivityManager conMan =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conMan.getActiveNetworkInfo();
        if (info==null) return false;
        if (!info.isConnected()) return false;
        if (!info.isAvailable()) return false;
        return true;
    }

    public static void showOpenFileDialog(String path) {
        Log.d(TAG_LOG, "showOpenFileDialog");
        if (path!=null) fileDialog.setCurrentPath(path);
        fileDialog.show();
    }

    public static Context getContext() {
        return mContext;
    }

    public static Activity getActivity() {
        return mActivity;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG_LOG, "onSaveInstanceState");
        outState.putBoolean("isClosed", OpenFileDialog.isClosed());
        String path = fileDialog.getCurrentPath();
        outState.putCharSequence("path", path);
        outState.putSerializable("adapter", mQuakeAdapter);
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
                mTitle = getString(R.string.title_earth);
                break;
            case 2:
                mTitle = getString(R.string.title_belarus);
                break;
            case 3:
                mTitle = getString(R.string.title_section_local_file);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void showQuakes(int position) {
        int map = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getString("map_list", "0")
        );
        if (map==0) {
            List<QuakeItem> quakes;
            if (position==-1) quakes = QuakeContent.QUAKES;
            else {
                quakes = new ArrayList<QuakeItem>();
                quakes.add(mQuakeAdapter.getItem(position));
            }
            MWMPoint[] points = new MWMPoint[quakes.size()];
            for (int i = 0; i < quakes.size(); i++)
                points[i] = quakes.get(i).toMWMPoint();
            final String title = (quakes.size() == 1) ? quakes.get(0).title : "Землетрясения";
            MapsWithMeApi.showPointsOnMap(mActivity, title, points);
        }
        if (map==1) {
            Intent intent = new Intent(getContext(), MapActivity.class);
            intent.putExtra("position", position);
            getActivity().overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
            getActivity().startActivity(intent);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
            implements OnTaskCompleteListener, DataBaseTask.OnTaskCompleteListener, OpenFileDialog.OpenDialogListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String TAG_LOG = "PlaceHolderFragment";
        private static int mSectionNumber;
        private static PlaceholderFragment mPlaceholderFragment;
        private static PullToRefreshListView pullToRefreshlist;
        private static ListView commonList;

        private static AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        MainActivity.showQuakes(position - 1);
                    }
                };

        public static PlaceholderFragment getPlaceholderFragment() {
            return mPlaceholderFragment;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            Log.d(TAG_LOG, "newInstance");
            PlaceholderFragment fragment = new PlaceholderFragment();
            mPlaceholderFragment = fragment;
            mSectionNumber = sectionNumber;
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
            Log.d(TAG_LOG, "onCreateView");
            View rootView;
            if (mSectionNumber==3) {
                rootView = inflater.inflate(R.layout.fragment_main_common_list, container, false);
                commonList = (ListView) rootView.findViewById(R.id.common_listview);
                commonList.setOnItemClickListener(itemClickListener);
                if (NavigationDrawerFragment.wasDrawerOpened) {
                    NavigationDrawerFragment.wasDrawerOpened = false;
                    if (OpenFileDialog.isClosed()) {
                        MainActivity.fileDialog = new OpenFileDialog(getContext());
                        MainActivity.fileDialog.setOpenDialogListener(this);
                        MainActivity.fileDialog.show();
                    }
                } else
                    if (mQuakeAdapter!=null) commonList.setAdapter(mQuakeAdapter);
            } else {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                pullToRefreshlist = (PullToRefreshListView) rootView.findViewById(R.id.pull_to_refresh_list);
                pullToRefreshlist.setOnItemClickListener(itemClickListener);
                DataBaseTask.setOnTaskCompleteListener(this);
                switch (mSectionNumber) {
                    case 1:
                        if (!MainActivity.isFirstStarted()) new DataBaseTask().execute(0);
                        pullToRefreshlist.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                new GetDataTask().execute(Constants.URL_EARTH);
                            }
                        });
                        break;
                    case 2:
                        new DataBaseTask().execute(1);
                        pullToRefreshlist.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                new GetDataTask().execute(Constants.URL_BLR);
                            }
                        });
                        break;
                }
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            Log.d(TAG_LOG, "onAttach");
            MainActivity.fileDialog.setOpenDialogListener(this);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onTaskComplete(JdbfTask task) {
            Log.d(TAG_LOG, "onTaskComplete()");
            if (task.isCancelled())
                Toast.makeText(
                        getContext(),
                        R.string.task_cancelled,
                        Toast.LENGTH_LONG
                ).show();
            else {
                Boolean result = null;
                try {
                    result = task.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Toast.makeText(
                        getContext(),
                        getString(R.string.task_completed, (result!=null) ? result.toString() : "null"),
                        Toast.LENGTH_LONG
                ).show();
            }
            if (!QuakeContent.init()) return;
            mQuakeAdapter = new QuakeAdapter(getContext(), QuakeContent.QUAKES);
            if (mSectionNumber == 3) commonList.setAdapter(mQuakeAdapter);
            else Log.e(TAG_LOG, "The section number is wrong.");
        }

        @Override
        public void OnTaskComplete() {
            Log.d(TAG_LOG, "onTaskComplete");
            mQuakeAdapter = new QuakeAdapter(getContext(), QuakeContent.QUAKES);
            if (mSectionNumber == 3) Log.e(TAG_LOG, "The section number is wrong.");
            else pullToRefreshlist.setAdapter(mQuakeAdapter);
        }

        @Override
        public void OnSelectedFile(String fileName) {
            Log.d(TAG_LOG, "OnSelectedFile");
            if (!fileName.contains("dbf")) {
                Toast.makeText(
                        getContext(),
                        getString(R.string.choose_file),
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager(getContext(), this);
                mAsyncTaskManager.setupTask(new JdbfTask(getResources()), fileName);
            }
        }

        private class GetDataTask extends JdbfTask {

            private OnTaskCompleteListener taskCompleteListener;

            public GetDataTask() {
                super(getResources());
                taskCompleteListener = PlaceholderFragment.this;
            }

            @Override
            protected Boolean doInBackground(String... params) {
                return super.doInBackground(params);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                pullToRefreshlist.onRefreshComplete();
                if (result) {
                    taskCompleteListener.onTaskComplete(this);
                    super.onPostExecute(result);
                    if (mSectionNumber-1==Constants.Area.EARTH.ordinal())
                        DataBaseHelper.deleteRecords(Constants.Area.EARTH);
                    if (mSectionNumber-1==Constants.Area.BELARUS.ordinal())
                        DataBaseHelper.deleteRecords(Constants.Area.BELARUS);
                    for (QuakeRecord rec : GetDataTask.records)
                        DataBaseHelper.addQuake(rec);
                } else
                    Toast.makeText(getContext(), "Data weren't updated :(", Toast.LENGTH_LONG)
                            .show();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                pullToRefreshlist.onRefreshComplete();
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
