package minefarts.smarttube.utils.app;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.Web;
import dev.lavalink.youtube.clients.skeleton.Client;
//import dev.lavalink.youtube.po.PoTokenLoadingResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PoTokenGate {

    public static Object mPayload = null;

    public static Object getPayload() {

        if (mPayload == null) { 
        
            Web webClient = new Web();
            YoutubeAudioSourceManager sourceManager = new YoutubeAudioSourceManager(
                /*allowSearch:*/ true, 
                new Client[]{ webClient }
            );
            
            mPayload = sourceManager.getPoTokenLoader().load(webClient);
        }

        return mPayload;
    }

    public static String getVisitorData(Object... ig) {
        return getPayload().getVisitorData();
    }

    public static String getPoToken(Object... ig) {
        return getPayload().getPoToken();
    }

}
