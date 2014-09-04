package by.bigsoft.brazer.quakeviewer2;

public interface IProgressTracker {
    void onProgress(String message);
    void onComplete();
}
