package dml.qipairoomcard.entity;

import dml.largescaletaskmanagement.entity.LargeScaleTaskSegmentBase;

public class ClearRoomTaskSegment extends LargeScaleTaskSegmentBase {
    private long id;
    private String roomNo;

    public void setId(Object id) {
        this.id = (long) id;
    }

    @Override
    public Object getId() {
        return id;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }
}
