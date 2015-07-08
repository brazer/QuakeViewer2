package by.bigsoft.brazer.quakeviewer2;

public class Constants {

    private static final String sBaseUrl = "http://cgm.org.by:83/quake_viewer_data/";
    public static final String URL_EARTH = sBaseUrl + "earth.dbf";
    public static final String URL_EUROPE = sBaseUrl + "europe.dbf";
    public static final String URL_BLR = sBaseUrl + "belarus.dbf";

    public enum Area { EARTH, BELARUS }
    public enum Map { Google_Maps, Maps_ME }
}
