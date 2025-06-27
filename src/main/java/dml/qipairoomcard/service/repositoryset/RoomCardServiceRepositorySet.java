package dml.qipairoomcard.service.repositoryset;

import dml.gamecurrency.service.repositoryset.GameCurrencyAccountingServiceRepositorySet;
import dml.qipairoom.service.repositoryset.RoomServiceRepositorySet;
import dml.qipairoomcard.repository.*;

public interface RoomCardServiceRepositorySet extends GameCurrencyAccountingServiceRepositorySet, RoomServiceRepositorySet {
    RoomAliveKeeperRepository getRoomAliveKeeperRepository();

    ClearRoomTaskRepository getClearRoomTaskRepository();

    ClearRoomTaskSegmentRepository getClearRoomTaskSegmentRepository();

    ClearRoomTaskSegmentIDGeneratorRepository getClearRoomTaskSegmentIDGeneratorRepository();

    StartRoomTaskRepository getStartRoomTaskRepository();

    StartRoomTaskSegmentRepository getStartRoomTaskSegmentRepository();

    StartRoomTaskSegmentIDGeneratorRepository getStartRoomTaskSegmentIDGeneratorRepository();

    RoomStateRepository getRoomStateRepository();
}
