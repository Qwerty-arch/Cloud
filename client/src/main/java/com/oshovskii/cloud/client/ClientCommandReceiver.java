package com.oshovskii.cloud.client;

import com.oshovskii.cloud.common.CommandReceiver;
import io.netty.channel.ChannelHandlerContext;

public class ClientCommandReceiver extends CommandReceiver {
    @Override
    public void parseCommand (ChannelHandlerContext ctx, String cmd) {
        if (cmd.startsWith("/filesList ")) {
            if (cmd.equals("filesList")) MainController.serverFilesListString = "";
            else MainController.serverFilesListString = cmd.split("\\s")[1];
        }

        if (cmd.startsWith("/authok ")) {
            MainController.isAuthorized = true;
            MainController.loggedInUserName = cmd.split("\\s")[1];
            System.out.println("logged in ok");
        }
        if (cmd.startsWith("/authofall ")) {
            //
            MainController.isAuthorized = false;
            MainController.loggedInUserName = null;
            System.out.println("not logged in");
        }
    }
}
