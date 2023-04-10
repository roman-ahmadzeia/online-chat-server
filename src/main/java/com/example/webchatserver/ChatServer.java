package com.example.webchatserver;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * This class represents a web socket server, a new connection is created and it receives a roomID as a parameter
 * **/
@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {
    // contains a static List of ChatRoom used to control the existing rooms and their users
    private static Map<String, ChatRoom> chatRooms = new HashMap<>();

    // you may add other attributes as you see fit

    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {
        // check if the chat room exists
        ChatRoom chatRoom = chatRooms.get(roomID);
        if (chatRoom == null) {
            // create a new chat room
            chatRoom = new ChatRoom(roomID, session.getId());
            chatRooms.put(roomID, chatRoom);
        } else {
            // add the user to the existing chat room
            chatRoom.getUsers().put(session.getId(), "");
        }

        // send a message to the client to confirm they have joined the room
        session.getBasicRemote().sendText("You have joined the room: " + roomID);
    }
    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        // find the chat room the user belongs to and remove the user
        for (ChatRoom chatRoom : chatRooms.values()) {
            if (chatRoom.getUsers().containsKey(session.getId())) {
                chatRoom.getUsers().remove(session.getId());
                break;
            }
        }
    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
        // find the chat room the user belongs to
        ChatRoom chatRoom = null;
        for (ChatRoom cr : chatRooms.values()) {
            if (cr.getUsers().containsKey(session.getId())) {
                chatRoom = cr;
                break;
            }
        }
        if (chatRoom == null) {
            // if the user is not in any chat room, do nothing
            return;
        }

        // handle the message
        JSONObject jsonmsg = new JSONObject(comm);
        String type = jsonmsg.getString("type");
        if (type.equals("username")) {
            // update the user's name in the chat room
            String username = jsonmsg.getString("username");
            chatRoom.getUsers().put(session.getId(), username);
            // notify all users in the chat room that the user's name has been updated
            JSONObject updateMsg = new JSONObject();
            updateMsg.put("type", "update");
            updateMsg.put("user", session.getId());
            updateMsg.put("username", username);
            for (Session s : session.getOpenSessions()) {
                if (chatRoom.getUsers().containsKey(s.getId())) {
                    s.getBasicRemote().sendText(updateMsg.toString());
                }
            }
        } else if (type.equals("message")) {
            // broadcast the message to all users in the chat room
            String message = jsonmsg.getString("message");
            String username = chatRoom.getUsers().get(session.getId());
            JSONObject broadcastMsg = new JSONObject();
            broadcastMsg.put("type", "broadcast");
            broadcastMsg.put("user", session.getId());
            broadcastMsg.put("username", username);
            broadcastMsg.put("message", message);
            for (Session s : session.getOpenSessions()) {
                if (chatRoom.getUsers().containsKey(s.getId())) {
                    s.getBasicRemote().sendText(broadcastMsg.toString());
                }
            }
        }
    }
}
