package by.bigsoft.brazer.quakeviewer2.ui.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import by.bigsoft.brazer.quakeviewer2.R;
import by.org.cgm.quake.QuakeContent;

public class MapActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private Marker markers[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setTitle(getString(R.string.map_activity_title));
        setMap();
        setMarkers(getPosition());
    }

    private void setMap() {
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap))
                .getMap();
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(this);
    }

    private int getPosition() {
        Intent intent = getIntent();
        return intent.getIntExtra("position", -1);
    }

    private void setMarkers(int position) {
        if (position==-1) {
            int count = QuakeContent.QUAKES.size();
            markers = new Marker[count];
            for (int i = 0; i < count; i++)
                setMarker(i);
        } else {
            markers = new Marker[1];
            setMarker(position);
            map.moveCamera(CameraUpdateFactory.newLatLng(markers[0].getPosition()));
        }
    }

    private void setMarker(int position) {
        QuakeContent.QuakeItem quake = QuakeContent.QUAKES.get(position);
        double lat = quake.lat;
        double lng = quake.lon;
        LatLng location = new LatLng(lat, lng);
        int i = (markers.length==1) ? 0 : position;
        markers[i] = map.addMarker(new MarkerOptions()
                        .position(location)
                        .title(quake.title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_earthquake))
                        .snippet(quake.content)
        );
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }
}
