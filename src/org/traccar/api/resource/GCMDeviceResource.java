/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.api.resource;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import javax.json.JsonArray;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.helper.Log;
import org.traccar.model.Device;
import org.traccar.model.GCMDevice;
import org.traccar.model.User;
import org.traccar.web.JsonConverter;
import static org.traccar.web.JsonConverter.objectToJson;

/**
 *
 * @author andrey
 */
@Path("gcmdevices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GCMDeviceResource extends BaseResource {

    @GET
    public Collection<GCMDevice> get(
            @QueryParam("login") String login, @QueryParam("password") String password) throws SQLException {
        User u=Context.getDataManager().login(login, password);
        if (u!=null){            
            return Context.getDataManager().getUserGCMDeviceLinks(u.getId(), 0);
        }
        else 
            return null;        
    }

    @POST
    public Response add(GCMDevice entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        Context.getDataManager().addGCMDevice(entity);
        Context.getPermissionsManager().refresh();
        sendDevicesUpdate(entity);
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @PUT
    public Response update(@PathParam("id") long id, GCMDevice entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        Context.getDataManager().updateGCMDevice(entity);
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") String id) throws SQLException {
        Context.getDataManager().removeGCMDevice(getUserId(), id);
        Context.getPermissionsManager().refresh();
        return Response.noContent().build();
    }

    private void sendDevicesUpdate(GCMDevice entity) {
        try{
            
            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + Context.getConfig().getString("google.apiKey", ""));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            JsonArray a = JsonConverter.arrayToJson(Context.getDataManager().getAllDevices());
            outputStream.write(a.toString().getBytes());        
        }
        catch (Exception e){
            Log.error(e.getMessage());
        }
    }

}
