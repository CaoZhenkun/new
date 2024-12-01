package com.example.anew;

import android.util.Log;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.GeoPoint;

public class Config {
    public static final String MAP_KEY = "689e5a577dbce4e10aa10103d39c737e";

    // 默认GeoPoint
    public static GeoPoint DEFAULT_GEO_POINT = new GeoPoint(39.909, 116.39742);

    /**
     * 天地图 有标注电子地图
     */
    public static final OnlineTileSourceBase TDTVEC_W = new XYTileSource(
            "Tian Di Tu VEC_W",
            0, 20, 256, "",
            new String[]{
                    "http://t0.tianditu.com/DataServer?T=vec_w&tk=" + MAP_KEY,
                    "http://t1.tianditu.com/DataServer?T=vec_w&tk=" + MAP_KEY,
                    "http://t2.tianditu.com/DataServer?T=vec_w&tk=" + MAP_KEY,
                    "http://t3.tianditu.com/DataServer?T=vec_w&tk=" + MAP_KEY,
                    "http://t4.tianditu.com/DataServer?T=vec_w&tk=" + MAP_KEY,
                    "http://t5.tianditu.com/DataServer?T=vec_w&tk=" + MAP_KEY,
                    "http://t6.tianditu.com/DataServer?T=vec_w&tk=" + MAP_KEY,
                    "http://t7.tianditu.com/DataServer?T=vec_w&tk=" + MAP_KEY
            }
    ) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            String url = getBaseUrl() + "&X=" + MapTileIndex.getX(pMapTileIndex) + "&Y=" + MapTileIndex.getY(pMapTileIndex)
                    + "&L=" + MapTileIndex.getZoom(pMapTileIndex);
            Log.d("TileURL", url);
            return url;
        }
    };



    public static final OnlineTileSourceBase TDTCVA_W = new XYTileSource(
            "Tian Di Tu CVA_W",
            0, 20, 256, "",
            new String[]{
                    "http://t0.tianditu.com/DataServer?T=cva_w&tk=" + MAP_KEY,
                    "http://t1.tianditu.com/DataServer?T=cva_w&tk=" + MAP_KEY,
                    "http://t2.tianditu.com/DataServer?T=cva_w&tk=" + MAP_KEY,
                    "http://t3.tianditu.com/DataServer?T=cva_w&tk=" + MAP_KEY,
                    "http://t4.tianditu.com/DataServer?T=cva_w&tk=" + MAP_KEY,
                    "http://t5.tianditu.com/DataServer?T=cva_w&tk=" + MAP_KEY,
                    "http://t6.tianditu.com/DataServer?T=cva_w&tk=" + MAP_KEY,
                    "http://t7.tianditu.com/DataServer?T=cva_w&tk=" + MAP_KEY
            }
    ) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            String url = getBaseUrl() + "&X=" + MapTileIndex.getX(pMapTileIndex) + "&Y=" + MapTileIndex.getY(pMapTileIndex)
                    + "&L=" + MapTileIndex.getZoom(pMapTileIndex);
            Log.d("TileURL", url);
            return url;
        }
    };

    private Config() {}
}