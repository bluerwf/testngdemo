package com.rong.flora.nov_26;

import org.apache.commons.lang3.RandomUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by rongwf1 on 2016/11/27.
 */
public class Server implements IServer {
    private static final org.apache.log4j.Logger logger = Logger.getLogger(Server.class);
    private State state;
    private int connect;
    private Map<Integer, List<Integer>> clientMap;
    private List<Message> messageList;
    private int fd;
    private int id;
    private static final Server inst = new Server();
    private IMsgProxy msgProxy;
    private int[] fdArray = new int[1025];

    private Server(){
        state = State.S_CLOSED;
        connect = 0;
        fd = 0;
        id = RandomUtils.nextInt(10001, 20000);
        clientMap = new HashMap<>(MAX_CONN);
        messageList = new LinkedList<>();
//        msgProxy = new MsgProxy();
        msgProxy = LinkedListMsgProxy.getInst();
    }

    public Server(int id){
        this();
    }

    public  static Server getInst(){
        return inst;
    }

    public Map<Integer, List<Integer>> getClientMap() {
        return clientMap;
    }

    public void setClientMap(Map<Integer, List<Integer>> clientMap) {
        this.clientMap = clientMap;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean start(){
        boolean flag = true;
        logger.debug("start the server");
        try {

            state = state.next();
            Thread.sleep(5000);
        } catch (InterruptedException e){
            flag = false;
            logger.debug(e.getMessage());
        }
        state = state.next();
        logger.debug("server is running");
        return flag;
    }

    public boolean shutDown(){
        boolean flag = true;
        logger.debug(" start to close the server");
        try {
            state = state.next();
            Thread.sleep(5000);
        } catch (InterruptedException e){
            flag = false;
            logger.debug(e.getMessage());
        }
        state = state.next();
        logger.debug(" the server is closed");
        return flag;
    }

    public void read(int fd, IOncomplete action){
//        Message msg = msgProxy.read();
        Message msg = msgProxy.read(fd, id);
        if (msg == null) return;

        if (clientMap.get(msg.getSrc()) != null &&
                clientMap.get(msg.getSrc()).contains(fd) &&
                msg.getDst()==id &&
                msg.getLife() > 0){
            messageList.add(msg);
            if (action != null && !msg.getContent().equals("ack")){
                action.success();
            }
            logger.debug("message:" + msg);
        } else {
            // write back to the queue
            if (!msgProxy.write(msg) && action != null) {
                action.failure();
                logger.debug("server puts back message to queue");
            }
        }
    }

    public boolean write(int fd, Message msg){
        boolean flag = true;
        if (clientMap.get(msg.getDst()) != null && clientMap.get(msg.getDst()).contains(fd)){
            msgProxy.write(msg);
//            logger.debug("send message: " + msg);
        } else {
            flag = false;
            logger.debug(" error!");
        }
        return flag;
    }

    public State status(){
        return state;
    }

    public synchronized int accept(IClient client){

        if(status().equals(State.S_RUNNING) && connect<MAX_CONN ){
            fd = findMinFd();
            if (fd > 0){
                connect++;
                fdArray[fd] = client.getClientId();
                List<Integer> fds = clientMap.getOrDefault(client.getClientId(), new LinkedList<>());
                fds.add(fd);
                clientMap.put(client.getClientId(), fds);
            }
        }

        if (fd > MAX_CONN){
            logger.debug("fd > max countWithMap, can't accept");
        }
        return fd;
    }

    public synchronized boolean close(int fd, IClient client){
        boolean flag = true;
        if (clientMap.get(client.getClientId()) == null){
            logger.debug(" this client is not exist");
            return flag;
        }
        clientMap.get(client.getClientId()).remove(fd);
        fdArray[fd] = 0;
        if (clientMap.get(client.getClientId()).size() == 0){
            clientMap.remove(client.getClientId());
        }


        if (client.getServerMap().get(id) == null){
            logger.debug(" this server is not exist");
            return flag;
        }
        client.getServerMap().get(id).remove(fd);
        if (client.getServerMap().get(id).size() == 0){
            client.getServerMap().remove(id);
        }
        return flag;
    }

    private synchronized int findMinFd(){
        int minFd = 0;
        int i = 1;
        while (i < fdArray.length){
            if (fdArray[i] == 0){
                minFd = i;
                break;
            }
            i++;
        }
        return minFd;
    }

    public synchronized void clearFd(int fd){
        if ( fd < 0 || fd >1024) {
            logger.debug("error ! fd is not valid");
            return;
        }
        fdArray[fd] = 0;
    }
}
