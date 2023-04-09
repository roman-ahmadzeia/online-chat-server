package com.example.webchatserver;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the data you may need to store about a Chat room
 * You may add more method or attributes as needed
 * **/
public class ChatRoom {
    private String  code;

    //each user has an unique ID associate to their ws session and their username
    private Map<String, String> users = new HashMap<String, String>() ;

    // when created the chat room has at least one user
    public ChatRoom(String code, String user){
        this.code = code;
        // when created the user has not entered their username yet
        this.users.put(user, "");
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    /**
     * This method will add the new userID to the room if not exists, or it will add a new userID,name pair
     * **/
    public void setUserName(String userID, String name) {
        // update the name
        if(users.containsKey(userID)){
            users.remove(userID);
            users.put(userID, name);
        }else{ // add new user
            users.put(userID, name);
        }
    }

    /**
     * This method will remove a user from this room
     * **/
    public void removeUser(String userID){
        if(users.containsKey(userID)){
            users.remove(userID);
        }

    }

    public boolean inRoom(String userID){
        return users.containsKey(userID);
    }

    public static void saveChatRoomHistory(String roomID, String log) throws IOException {
        String uriAPI = "http://localhost:8080/ChatResourceAPI-1.0-SNAPSHOT/api/history/"+roomID;
        URL url = new URL(uriAPI);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        // allows us to write content to the outputStream
        con.setDoOutput(true);

        // sending the data with the POST request
        String jsonInputString = "{\"room\":\""+roomID+"\",\"log\":\""+log+"\"}";
        System.out.println(jsonInputString);
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //reading and printing response
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * This method call the ChatResourceAPI to load the log history of a room
     * */
    public static String loadChatRoomHistory(String roomID) throws IOException {
        String uriAPI = "http://localhost:8080/ChatResourceAPI-1.0-SNAPSHOT/api/history/"+roomID;
        URL url = new URL(uriAPI);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        // we dont need to write content to the outputStream
        con.setDoOutput(false);
        // allows us to read inputstream
        con.setDoInput(true);

        // getting the inputStream
        InputStream inStream = con.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        String jsonData = buffer.toString();

        System.out.println("load the data");
        System.out.println(jsonData);

        // transforming the string into objects using org.json library
        JSONObject data = new JSONObject(jsonData);
        Map<String, Object> mapData = data.toMap();
        String content = (String) mapData.get("log");

        return content;

    }
}
