package by.bigsoft.brazer.quakeviewer2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import by.org.cgm.jdbf.JdbfTask;

public class InternetDialog extends DialogFragment {

    private Context context;
    private OnTaskCompleteListener listener;
    private String http;
    private static TextView text;

    public InternetDialog(Context context, OnTaskCompleteListener listener) {
        this.context = context;
        http = context.getResources().getString(R.string.url);
        this.listener = listener;
    }

    public String getUrl() {
        if (text!=null) http = text.getText().toString();
        return http;
    }

    public void setUrl(String url) {
        http = url;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_internet, null);
        final TextView textView = (TextView) v.findViewById(R.id.txtHttp);
        text = textView;
        textView.setText(http);
        builder.setView(v)
                .setTitle("Загрузка данных из интернета")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        http = textView.getText().toString();
                        loadFromHttp(http);
                        //todo: QuakeListActivity.isLoadedInternetDialog = true;
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //todo: QuakeListActivity.isLoadedInternetDialog = true;
                        dismiss();
                    }
                });
        return builder.create();
    }

    private void loadFromHttp(String url) {
        AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager(context, listener);
        mAsyncTaskManager.setupTask(new JdbfTask(context.getResources()), url);
    }
}
