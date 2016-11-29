package com.rong.flora.nov_26;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by rongwf1 on 2016/11/27.
 */
public class Message {
    private String ts;
    private String id;
    private String content;
    private String type;
    private Integer life;
    private Integer clentId;
    private static Queue<Message> serverMessages = new ArrayBlockingQueue<Message>(1024);
    private static Queue<Message> clientMessages = new ArrayBlockingQueue<Message>(1024);

    private Message(){
        this.ts = new Date().toString();
        this.id = UUID.randomUUID().toString();
        this.life = 10;
    }
    private Message( String content, String type, Integer clientId) {
        this();
        this.content = content;
        this.type = type;
        this.clentId =clientId;
    }

   public static class Builder{
        private String content;
        private String type;
        private Integer clientId;


        public Builder content(String content){
            this.content = content;
            return this;
        }

        public Builder type(String type){
            this.type = type;
            return this;
        }

        public Builder clientId(Integer clientId){
            this.clientId = clientId;
            return this;
        }

        public Message build(){
            Message message = new Message();
            message.content = this.content;
            message.type = this.type;
            message.clentId = this.clientId;
            return message;
        }

    }


    public static Queue<Message> getServerMessages() {
        return serverMessages;
    }

    public static Queue<Message> getClientMessages() {
        return clientMessages;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLife() {
        return life;
    }

    public void setLife(Integer life) {
        this.life = life;
    }

    public Integer getClentId() {
        return clentId;
    }

    public void setClentId(Integer clentId) {
        this.clentId = clentId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "ts='" + ts + '\'' +
                ", id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", life=" + life +
                ", clentId=" + clentId +
                '}';
    }
}