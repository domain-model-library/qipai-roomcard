package dml.qipairoomcard.service.repositoryset;

import dml.gamecurrency.service.repositoryset.GameCurrencyAccountingServiceRepositorySet;
import dml.qipairoom.service.repositoryset.RoomServiceRepositorySet;
import dml.qipairoomcard.repository.ClearRoomTaskRepository;
import dml.qipairoomcard.repository.ClearRoomTaskSegmentIDGeneratorRepository;
import dml.qipairoomcard.repository.ClearRoomTaskSegmentRepository;
import dml.qipairoomcard.repository.RoomAliveKeeperRepository;

public interface RoomCardServiceRepositorySet extends GameCurrencyAccountingServiceRepositorySet, RoomServiceRepositorySet {
    RoomAliveKeeperRepository getRoomAliveKeeperRepository();

    ClearRoomTaskRepository getClearRoomTaskRepository();

    ClearRoomTaskSegmentRepository getClearRoomTaskSegmentRepository();

    ClearRoomTaskSegmentIDGeneratorRepository getClearRoomTaskSegmentIDGeneratorRepository();
}
