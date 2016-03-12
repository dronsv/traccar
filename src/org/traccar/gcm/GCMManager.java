/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.gcm;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.traccar.Context;
import org.traccar.model.Position;
//import com.google.android.gcm.server;


/**
 *
 * @author andrey   
 */
public class GCMManager {
    private final String serverKey;
    public GCMManager(){
        serverKey = Context.getConfig().getString("gcm.serverKey");
    }
    
   public void publishTopic(String topic){
       
       
   }
   public void publishDevicePosition(Position position){
       
   }
}
