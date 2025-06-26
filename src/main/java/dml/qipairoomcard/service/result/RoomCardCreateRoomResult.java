package dml.qipairoomcard.service.result;

public class RoomCardCreateRoomResult {
    private boolean success;
    private String roomNo;
    private boolean insufficientRoomCard;
    private boolean alreadyInRoom;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public boolean isInsufficientRoomCard() {
        return insufficientRoomCard;
    }

    public void setInsufficientRoomCard(boolean insufficientRoomCard) {
        this.insufficientRoomCard = insufficientRoomCard;
    }

    public boolean isAlreadyInRoom() {
        return alreadyInRoom;
    }

    public void setAlreadyInRoom(boolean alreadyInRoom) {
        this.alreadyInRoom = alreadyInRoom;
    }

}
