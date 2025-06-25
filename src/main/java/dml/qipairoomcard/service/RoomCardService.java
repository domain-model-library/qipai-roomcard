package dml.qipairoomcard.service;

import dml.gamecurrency.entity.GameCurrencyAccountBillItem;
import dml.gamecurrency.service.GameCurrencyAccountingService;
import dml.gamecurrency.service.result.WithdrawResult;
import dml.qipairoom.entity.QipaiRoom;
import dml.qipairoom.service.RoomService;
import dml.qipairoom.service.result.CreateRoomResult;
import dml.qipairoomcard.service.repositoryset.RoomCardServiceRepositorySet;
import dml.qipairoomcard.service.result.RoomCardCreateRoomResult;

public class RoomCardService {
    public static RoomCardCreateRoomResult createRoom(RoomCardServiceRepositorySet repositorySet,
                                                      Object userId, int roomCardToConsume, String roomCardCurrency,
                                                      GameCurrencyAccountBillItem newRoomCardBillItem,
                                                      int playersCount, QipaiRoom newQipaiRoom) {
        RoomCardCreateRoomResult result = new RoomCardCreateRoomResult();
        boolean playerInRoom = RoomService.isPlayerInRoom(repositorySet, userId.toString());
        if (playerInRoom) {
            result.setSuccess(false);
            result.setAlreadyInRoom(true);
            return result;
        }

        WithdrawResult withdrawResult = GameCurrencyAccountingService.withdrawIfBalanceSufficient(repositorySet, userId,
                roomCardCurrency, String.valueOf(roomCardToConsume), newRoomCardBillItem);
        if (withdrawResult == null) {
            result.setSuccess(false);
            result.setInsufficientRoomCard(true);
            return result;
        }

        CreateRoomResult createRoomResult = RoomService.createRoom(repositorySet, userId.toString(), playersCount, newQipaiRoom);
        if (!createRoomResult.isSuccess()) {
            throw new RuntimeException("create room failed, user already in another room or other error");
        }
        result.setSuccess(true);
        return result;
    }
}
