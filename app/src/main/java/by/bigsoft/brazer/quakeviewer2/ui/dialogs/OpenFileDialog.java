package by.bigsoft.brazer.quakeviewer2.ui.dialogs;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import by.bigsoft.brazer.quakeviewer2.R;

public class OpenFileDialog extends AlertDialog.Builder {

    private final String TAG_LOG = "OpenFileDialog";
    private String currentPath = Environment.getExternalStorageDirectory().getPath();
    private static FilenameFilter filenameFilter;
    private List<File> files = new ArrayList<File>();
    private TextView title;
    private ListView listView;
    private static Drawable folderIcon;
    private static Drawable fileIcon;
    private int selectedIndex = -1;
    private OpenDialogListener listener;
    private String accessDeniedMessage;
    private Context mContext;
    private static boolean isClosed = true;

    public interface OpenDialogListener {
        public void OnSelectedFile(String fileName);
    }

    private class FileAdapter extends ArrayAdapter<File> {

        /**
         * Constructor
         *
         * @param context  The current context.
         * @param files  The objects to represent in the ListView.
         */
        public FileAdapter(Context context, List<File> files) {
            super(context, android.R.layout.simple_list_item_1, files);
        }

        /**
         * {@inheritDoc}
         *
         * @param position
         * @param convertView
         * @param parent
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            File file = getItem(position);
            if (view != null) {
                view.setText(file.getName());
                if (file.isDirectory()) {
                    setDrawable(view, folderIcon);
                } else {
                    setDrawable(view, fileIcon);
                    if (selectedIndex == position)
                        view.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
                    else
                        view.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
                }
            }
            return view;
        }

        private void setDrawable(TextView view, Drawable drawable) {
            if (view != null) {
                if (drawable != null) {
                    drawable.setBounds(0, 0, 60, 60);
                    view.setCompoundDrawables(drawable, null, null, null);
                } else {
                    view.setCompoundDrawables(null, null, null, null);
                }
            }
        }

    }

    /**
     * Constructor using a context for this builder and the {@link android.app.AlertDialog} it creates.
     *
     * @param context
     */
    public OpenFileDialog(Context context) {
        super(context);
        mContext = context;
        title = createTitle(context);
        changeTitle();
        setCancelable(false);
        LinearLayout linearLayout = createMainLayout(context);
        linearLayout.addView(createBackItem(context));
        listView = createListView(context);
        linearLayout.addView(listView);
        setCustomTitle(title)
                .setView(linearLayout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG_LOG, "Click PositiveButton");
                        if (selectedIndex > -1 && listener != null) {
                            listener.OnSelectedFile(listView.getItemAtPosition(selectedIndex).toString());
                        }
                        isClosed = true;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG_LOG, "Click NegativeButton");
                        isClosed = true;
                    }
                });
    }

    public void setCurrentPath(String path) {
        currentPath = path;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    private ListView createListView(Context context) {
        ListView listView = new ListView(context);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                final ArrayAdapter<File> adapter = (FileAdapter) adapterView.getAdapter();
                File file = adapter.getItem(index);
                if (file.isDirectory()) {
                    currentPath = file.getPath();
                    RebuildFiles(adapter);
                } else {
                    if (index != selectedIndex)
                        selectedIndex = index;
                    else
                        selectedIndex = -1;
                    adapter.notifyDataSetChanged();
                }
            }
        });
        return listView;
    }

    private TextView createTitle(Context context) {
        TextView textView = createTextView(context, android.R.style.TextAppearance_DeviceDefault);
        return textView;
    }

    private TextView createTextView(Context context, int style) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, style);
        int itemHeight = getItemHeight(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        textView.setMinHeight(itemHeight);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(15, 0, 0, 0);
        return textView;
    }

    private int getItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeightSmall, value, true);
        getDefaultDisplay(context).getMetrics(metrics);
        return (int) TypedValue.complexToDimension(value.data, metrics);
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private void changeTitle() {
        String titleText = currentPath;
        int screenWidth = getScreenSize(mContext).x;
        int maxWidth = (int) (screenWidth * 0.99);
        if (getTextWidth(titleText, title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, title.getPaint()) > maxWidth) {
                int start = titleText.indexOf("/", 2);
                if (start > 0)
                    titleText = titleText.substring(start);
                else
                    titleText = titleText.substring(2);
            }
            title.setText("..." + titleText);
        } else {
            title.setText(titleText);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private static Point getScreenSize(Context context) {
        Point screenSize = new Point();
        getDefaultDisplay(context).getSize(screenSize);
        return screenSize;
    }

    public int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.left + bounds.width() + 80;
    }

    private LinearLayout createMainLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(context));
        return linearLayout;
    }

    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSize(context).y;
    }

    private TextView createBackItem(Context context) {
        TextView textView = createTextView(context, android.R.style.TextAppearance_DeviceDefault_Small);
        Drawable drawable = mContext.getResources().getDrawable(android.R.drawable.ic_menu_revert);
        drawable.setBounds(0, 0, 60, 60);
        textView.setCompoundDrawables(drawable, null, null, null);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (selectedIndex!=-1) {
                    Toast.makeText(
                            mContext,
                            mContext.getString(R.string.deselect_file),
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                File file = new File(currentPath);
                File parentDirectory = file.getParentFile();
                if (parentDirectory != null) {
                    currentPath = parentDirectory.getPath();
                    RebuildFiles(((FileAdapter) listView.getAdapter()));
                }
            }
        });
        return textView;
    }

    private void RebuildFiles(ArrayAdapter<File> adapter) {
        try {
            List<File> fileList = getFiles(currentPath);
            files.clear();
            selectedIndex = -1;
            files.addAll(fileList);
            adapter.notifyDataSetChanged();
            changeTitle();
        } catch (NullPointerException e) {
            String message = mContext.getResources().getString(android.R.string.unknownName);
            if (!accessDeniedMessage.equals(""))
                message = accessDeniedMessage;
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    private List<File> getFiles(String directoryPath) {
        File directory = new File(directoryPath);
        List<File> fileList = Arrays.asList(directory.listFiles(filenameFilter));
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                if (file.isDirectory() && file2.isFile())
                    return -1;
                else if (file.isFile() && file2.isDirectory())
                    return 1;
                else
                    return file.getPath().compareTo(file2.getPath());
            }
        });
        return fileList;
    }

    public OpenFileDialog setFilter(final String filter) {
        filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                if (tempFile.isFile())
                    return tempFile.getName().matches(filter);
                return true;
            }
        };
        return this;
    }

    public OpenFileDialog setFolderIcon(Drawable drawable) {
        folderIcon = drawable;
        return this;
    }

    public OpenFileDialog setFileIcon(Drawable drawable) {
        fileIcon = drawable;
        return this;
    }

    public OpenFileDialog setOpenDialogListener(OpenDialogListener listener) {
        this.listener = listener;
        return this;
    }

    public OpenFileDialog setAccessDeniedMessage(String message) {
        this.accessDeniedMessage = message;
        return this;
    }

    public static boolean isClosed() {
        return isClosed;
    }

    public static void setIsClosed(boolean isClosed) {
        OpenFileDialog.isClosed = isClosed;
    }

    /**
     * Creates a {@link android.app.AlertDialog} with the arguments supplied to this builder and
     * {@link android.app.Dialog#show()}'s the dialog.
     */
    @Override
    public AlertDialog show() {
        Log.d(TAG_LOG, "show");
        isClosed = false;
        files.addAll(getFiles(currentPath));
        listView.setAdapter(new FileAdapter(mContext, files));
        return super.show();
    }

}
