package dml.qipairoomcard.entity;

public class RoomState {
    private String roomNo;
    private RoomStateEnum state;

    public boolean is(RoomStateEnum state) {
        return this.state == state;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public RoomStateEnum getState() {
        return state;
    }

    public void setState(RoomStateEnum state) {
        this.state = state;
    }


}
