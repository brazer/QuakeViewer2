package by.bigsoft.brazer.quakeviewer2.data;

import android.database.Cursor;
import android.os.AsyncTask;

import by.org.cgm.quake.QuakeContent;

public class DataBaseTask extends AsyncTask<Integer, Void, Cursor>{

    private boolean isBelarus;
    private static DataBaseTask.OnTaskCompleteListener completeListener;

    public static void setOnTaskCompleteListener(DataBaseTask.OnTaskCompleteListener completeListener) {
        DataBaseTask.completeListener = completeListener;
    }

    public interface OnTaskCompleteListener {
        public void OnTaskComplete();
    }

    @Override
    protected Cursor doInBackground(Integer... params) {
        Cursor cursor = null;
        switch (params[0]) {
            case 0:
                isBelarus = false;
                cursor = DataBaseHelper.getQuakes();
                break;
            case 1:
                isBelarus = true;
                cursor = DataBaseHelper.getBelarusEvents();
                break;
        }
        return cursor;
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);
        if (cursor==null) return;
        if (QuakeContent.QUAKES.size()>0) QuakeContent.QUAKES.clear();
        if (isBelarus) {
            while (cursor.moveToNext())
                addBelarusEvent(cursor);
        } else {
            while (cursor.moveToNext())
                addEarthQuake(cursor);
        }
        completeListener.OnTaskComplete();
    }

    private void addBelarusEvent(Cursor cursor) {
        int N = cursor.getColumnIndex("N");
        int date = cursor.getColumnIndex("Date");
        int time = cursor.getColumnIndex("Time");
        int lat = cursor.getColumnIndex("Latitude");
        int lng = cursor.getColumnIndex("Longitude");
        int Kp = cursor.getColumnIndex("Kp");
        int M = cursor.getColumnIndex("M");
        StringBuilder builder = new StringBuilder();
        builder
                .append(cursor.getString(date))
                .append(" ")
                .append(cursor.getString(time));
        String strM = cursor.getString(M);
        if (!strM.contains("0.0"))
            builder.append("\nМагнитуда: ").append(strM);
        String strKp = cursor.getString(Kp);
        if (!strKp.contains("0"))
            builder.append("\nКласс: ").append(strKp);
        String content = builder.toString();
        QuakeContent.QUAKES.add(new QuakeContent.QuakeItem(
                cursor.getString(N),
                cursor.getDouble(lng),
                cursor.getDouble(lat),
                content,
                cursor.getString(N)
        ));
    }

    private void addEarthQuake(Cursor cursor) {
        int N = cursor.getColumnIndex("N");
        int datetime = cursor.getColumnIndex("DateTime");
        int lat = cursor.getColumnIndex("Latitude");
        int lng = cursor.getColumnIndex("Longitude");
        int depth = cursor.getColumnIndex("Depth");
        int mag = cursor.getColumnIndex("MPSP");
        int locEng = cursor.getColumnIndex("Location");
        int location = cursor.getColumnIndex("LocationRus");
        StringBuilder builder = new StringBuilder();
        builder
                .append(cursor.getString(datetime));
        String strMag = cursor.getString(mag);
        if (!strMag.contains("0.0"))
            builder.append("\nМагнитуда: ").append(strMag);
        String strDep = cursor.getString(depth);
        if (!strDep.contains("0"))
            builder.append("\nГлубина: ").append(strDep).append(" км");
        String content = builder.toString();
        String strLoc = cursor.getString(location);
        if (strLoc.equals("")) strLoc = cursor.getString(locEng);
        QuakeContent.QUAKES.add(new QuakeContent.QuakeItem(
                cursor.getString(N),
                cursor.getDouble(lng),
                cursor.getDouble(lat),
                content,
                strLoc
        ));
    }
}
