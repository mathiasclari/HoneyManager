package fun.mcbee.bungee.honeynetwork.data;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;

import java.util.UUID;

public class FData {

    private UUID uuid;
    private int count = 1;
    private String name;

    public FData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String GetName() {
        return name;
    }

    public void UpdateName(String name) {
        this.name = name;
    }

    public void AddConnectionCount() {
        count++;
    }

    public void RemoveConnectionCount() {
        count--;
        if(count <= 0) {
            HoneyNetwork.listFriendData.remove(uuid);
        }
    }

}