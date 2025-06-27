package dml.qipairoomcard.service.result;

public class RoomStartedResult {
    private boolean success;
    private boolean roomNotStarting;
    private boolean insufficientRoomCard;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isRoomNotStarting() {
        return roomNotStarting;
    }

    public void setRoomNotStarting(boolean roomNotStarting) {
        this.roomNotStarting = roomNotStarting;
    }

    public boolean isInsufficientRoomCard() {
        return insufficientRoomCard;
    }

    public void setInsufficientRoomCard(boolean insufficientRoomCard) {
        this.insufficientRoomCard = insufficientRoomCard;
    }
}
