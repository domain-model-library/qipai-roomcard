package dml.qipairoomcard.service.repositoryset;

import dml.gamecurrency.service.repositoryset.GameCurrencyAccountingServiceRepositorySet;
import dml.qipairoom.service.repositoryset.RoomServiceRepositorySet;

public interface RoomCardServiceRepositorySet extends GameCurrencyAccountingServiceRepositorySet,
        RoomServiceRepositorySet {
}
