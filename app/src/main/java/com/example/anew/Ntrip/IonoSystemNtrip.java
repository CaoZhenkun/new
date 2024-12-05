package com.example.anew.Ntrip;

public class IonoSystemNtrip extends SSRSystem implements RTCM3ClientListener,Runnable{
        private static final String TAG="IonoSystemNtrip";

        /*
            ntrip
             */
        private RTCM3Client mRTCM3Client;
        public IonoSystemNtrip(RTCM3Client rtcm3Client)
        {
            this.mRTCM3Client=rtcm3Client;
        }

        @Override
        public void onDataReceived(byte[] data) {

            DecodeIonoData.decodeionodata(data);

        }

        @Override
        public void run() {
            //建立ntrip-scoket连接
            mRTCM3Client.run();
        }
        public void stopNtrip()
        {
            this.mRTCM3Client.stop();
        }


}
