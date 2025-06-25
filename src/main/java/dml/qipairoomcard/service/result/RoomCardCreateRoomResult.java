package dml.qipairoomcard.service.result;

public class RoomCardCreateRoomResult {
    private boolean success;
    private boolean insufficientRoomCard;
    private boolean alreadyInRoom;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
