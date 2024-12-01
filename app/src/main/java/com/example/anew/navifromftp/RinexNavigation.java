package com.example.anew.navifromftp;

import com.example.anew.coord.SatellitePosition;

import java.io.IOException;

public abstract class RinexNavigation implements NavigationIono{

    public void getFromFTP(String urltemplate) throws IOException {
    }
    public SatellitePosition getSatPositionAndVelocities(long unixTime, double range, int satID, char satType, double receiverClockError) {
        return null;
    }
}
