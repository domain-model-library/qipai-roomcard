package dml.qipairoomcard.service.result;

public class RoomCardJoinRoomResult {
    private boolean success;
    private boolean alreadyInRoom;
    private boolean inAnotherRoom;
    private boolean roomFull;
    private boolean roomStarted;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isAlreadyInRoom() {
        return alreadyInRoom;
    }

    public void setAlreadyInRoom(boolean alreadyInRoom) {
        this.alreadyInRoom = alreadyInRoom;
    }

    public boolean isInAnotherRoom() {
        return inAnotherRoom;
    }

    public void setInAnotherRoom(boolean inAnotherRoom) {
        this.inAnotherRoom = inAnotherRoom;
    }

    public boolean isRoomFull() {
        return roomFull;
    }

    public void setRoomFull(boolean roomFull) {
        this.roomFull = roomFull;
    }

    public boolean isRoomStarted() {
        return roomStarted;
    }

    public void setRoomStarted(boolean roomStarted) {
        this.roomStarted = roomStarted;
    }
}
