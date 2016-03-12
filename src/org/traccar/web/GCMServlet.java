/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.traccar.Context;
import org.traccar.model.User;
import static org.traccar.web.BaseServlet.USER_ID_KEY;

/**
 *
 * @author andrey
 */
public class GCMServlet extends BaseServlet {
    @Override
    protected boolean handle(String command, HttpServletRequest req, HttpServletResponse resp) throws Exception {

        switch (command) {
            case "/register":
                register(req, resp);
                break;
            case "/update":
                update(req, resp);
                break;
            default:
                return false;
        }
        return true;
    }
    

    private void register(HttpServletRequest req, HttpServletResponse resp)  throws Exception{
        User user = Context.getDataManager().login(
        req.getParameter("login"), req.getParameter("password"));
        
        if (user != null) {
            req.getSession().setAttribute(USER_ID_KEY, user.getId());
//            Context.getDataManager().addGCMDevice(req.getParameter("id"), user.getId(), req.getParameter("code"));
            sendResponse(resp.getWriter(), true);
        } else {
            sendResponse(resp.getWriter(), false);
        }
    }
    
    private void update(HttpServletRequest req, HttpServletResponse resp)  throws Exception{
        User user = Context.getDataManager().login(
        req.getParameter("email"), req.getParameter("password"));
        
        if (user != null) {
            req.getSession().setAttribute(USER_ID_KEY, user.getId());
//            Context.getDataManager().updateGCMDevice(getDataMana);
            sendResponse(resp.getWriter(), true);
        } else {
            sendResponse(resp.getWriter(), false);
        }
    }
    
    
}
