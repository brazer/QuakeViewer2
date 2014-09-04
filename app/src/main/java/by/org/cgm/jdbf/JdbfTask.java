package by.org.cgm.jdbf;

import android.content.res.Resources;
import android.os.AsyncTask;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import by.bigsoft.brazer.quakeviewer2.IProgressTracker;
import by.bigsoft.brazer.quakeviewer2.R;

public class JdbfTask extends AsyncTask<String, String, Boolean> {

    public static ArrayList<QuakeRecord> records;
    private static DBFReader reader;
    protected final Resources mResources;
    private Boolean mResult;
    private String mProgressMessage;
    private IProgressTracker mProgressTracker;

    public JdbfTask(Resources resources) {
        mResources = resources;
        mProgressMessage = resources.getString(R.string.task_starting);
    }

    public void setProgressTracker(IProgressTracker progressTracker) {
        mProgressTracker = progressTracker;
        if (mProgressTracker != null) {
            mProgressTracker.onProgress(mProgressMessage);
            if (mResult != null) {
                mProgressTracker.onComplete();
            }
        }
    }

    /**
     * <p>Applications should preferably override {@link #onCancelled(Object)}.
     * This method is invoked by the default implementation of
     * {@link #onCancelled(Object)}.</p>
     * <p/>
     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Object[])} has finished.</p>
     *
     * @see #onCancelled(Object)
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    @Override
    protected void onCancelled() {
        mProgressTracker = null;
    }

    /**
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     *
     * @param values The values indicating progress.
     * @see #publishProgress
     * @see #doInBackground
     */
    @Override
    protected void onProgressUpdate(String... values) {
        mProgressMessage = values[0];
        if (mProgressTracker!=null)
            mProgressTracker.onProgress(mProgressMessage);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params[0].contains("http:")) readFromUrl(params[0]);
        else readFromFile(params[0]);
        boolean res = false;
        try {
            res = formRecords();
        } catch (JDBFException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }

    private void readFromUrl(String strUrl) {
        try {
            URL url = new URL(strUrl);
            reader = new DBFReader(url.openStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFromFile(String fileName) {
        try {
            reader = new DBFReader(fileName);
        } catch (JDBFException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p/>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param result The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(Boolean result) {
        mResult = result;
        if (mProgressTracker!=null)
            mProgressTracker.onComplete();
        mProgressTracker = null;
    }

    private boolean formRecords() throws JDBFException, UnsupportedEncodingException {
        if (reader==null) return false;
        int i;
        records = new ArrayList<QuakeRecord>();
        for (i=0; i<reader.getFieldCount(); i++) {
            System.out.print(reader.getField(i).getName()+" ");
        }
        System.out.println();
        boolean res;
        res = readDataAndAddRecords();
        for (i = 0; i < records.size()/2; i++)
        {
            QuakeRecord temp = records.get(i);
            records.set(i, records.get(records.size() - i - 1));
            records.set(records.size() - i - 1, temp);
        }
        return res;
    }

    private boolean readDataAndAddRecords() throws JDBFException {
        int i = 0;
        while (reader.hasNextRecord()) {
            if (isCancelled()) return false;
            String strings[] = reader.nextRecordStrings();
            QuakeRecord rec = initQuakeRecord(strings[1]);
            for (int j=0; j<strings.length; j++) {
                rec.setField(j, strings[j]);
                System.out.print(strings[j]+" ");
            }
            records.add(rec);
            System.out.println();
            publishProgress(mResources.getString(R.string.task_working, ++i));
        }
        return true;
    }

    private QuakeRecord initQuakeRecord(String str) {
        if (isEarth(str)) return new QuakeRecordEarth();
        else return new QuakeRecordBLR();
    }

    private boolean isEarth(String s) {
        return s.contains(":");
    }

    public abstract static class QuakeRecord {
        protected abstract void setField(int nField, String value);
    }

    public static class QuakeRecordEarth extends QuakeRecord
    {
        public String N, DateTime, Lon, Lat, Depth, MPSP, MPLP, MS, LocRus, Loc;

        public void setField(int nField, String value) {
            switch (nField) {
                case 0:
                    N = value;
                    break;
                case 1:
                    DateTime = value;
                    break;
                case 2:
                    Lon = value;
                    break;
                case 3:
                    Lat = value;
                    break;
                case 4:
                    Depth = value;
                    break;
                case 5:
                    MPSP = value;
                    break;
                case 6:
                    MPLP = value;
                    break;
                case 7:
                    MS = value;
                    break;
                case 8:
                    Loc = value;
                    break;
                case 9:
                    LocRus = value;
            }
        }
    }

    public static class QuakeRecordBLR extends QuakeRecord
    {
        public String N, Date, Time, Lat, Lon, Ts_p, Delta, Kp, M, Loc;
        public void setField(int nField, String value) {
            switch (nField) {
                case 0:
                    N = value;
                    break;
                case 1:
                    Date = value;
                    break;
                case 2:
                    Time = value;
                    break;
                case 3:
                    Lon = value;
                    break;
                case 4:
                    Lat = value;
                    break;
                case 5:
                    Ts_p = value;
                    break;
                case 6:
                    Delta = value;
                    break;
                case 7:
                    Kp = value;
                    break;
                case 8:
                    M = value;
                    break;
                case 9:
                    Loc = value;
            }
        }
    }
}

