package by.bigsoft.brazer.quakeviewer2;

import by.org.cgm.jdbf.JdbfTask;

public interface OnTaskCompleteListener {
    // Notifies about task completeness
    void onTaskComplete(JdbfTask task);
}
