package dml.qipairoomcard.entity;

import dml.keepalive.entity.AliveKeeperBase;

public class RoomAliveKeeper extends AliveKeeperBase {

    private String roomNo;

    @Override
    public void setId(Object id) {
        roomNo = (String) id;
    }

    @Override
    public Object getId() {
        return roomNo;
    }
}
