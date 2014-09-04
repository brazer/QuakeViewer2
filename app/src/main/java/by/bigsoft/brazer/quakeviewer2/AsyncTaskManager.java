package by.bigsoft.brazer.quakeviewer2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import by.org.cgm.jdbf.JdbfTask;

public class AsyncTaskManager implements IProgressTracker, DialogInterface.OnCancelListener {

    private final OnTaskCompleteListener mTaskCompleteListener;
    private final ProgressDialog mProgressDialog;
    private JdbfTask mAsyncTask;

    public AsyncTaskManager(Context context, OnTaskCompleteListener taskCompleteListener) {
        mTaskCompleteListener = taskCompleteListener;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(this);
    }

    public void setupTask(JdbfTask task, String url) {
        mAsyncTask = task;
        mAsyncTask.setProgressTracker(this);
        mAsyncTask.execute(url);
    }

    @Override
    public void onProgress(String message) {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        mProgressDialog.setMessage(message);
    }

    /**
     * This method will be invoked when the dialog is canceled.
     *
     * @param dialog The dialog that was canceled will be passed into the
     * method.
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        mAsyncTask.cancel(true);
        mTaskCompleteListener.onTaskComplete(mAsyncTask);
        mAsyncTask = null;
        dialog.dismiss();
    }

    @Override
    public void onComplete() {
        mProgressDialog.dismiss();
        mAsyncTask.setProgressTracker(null);
        mTaskCompleteListener.onTaskComplete(mAsyncTask);
        mAsyncTask = null;
    }

    public boolean isWorking() {
        return mAsyncTask!=null;
    }

}
