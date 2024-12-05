package com.example.anew.Ntrip;


public class SSRsystemNtrip extends SSRSystem implements RTCM3ClientListener,Runnable{
    private static final String TAG="SSRsystemNtrip";

    /*
        ntrip
         */
    private RTCM3Client mRTCM3Client1;
    public SSRsystemNtrip(RTCM3Client rtcm3Client)
    {
        this.mRTCM3Client1=rtcm3Client;
    }

    @Override
    public void onDataReceived(byte[] data) {

        DecodeSSRData.decodessr(data);

    }

    @Override
    public void run() {
        mRTCM3Client1.run();
    }
    public void stopNtrip()
    {
        this.mRTCM3Client1.stop();
    }

}
