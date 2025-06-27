import dml.common.repository.TestCommonRepository;
import dml.common.repository.TestCommonSingletonRepository;
import dml.gamecurrency.repository.GameCurrencyAccountBillItemRepository;
import dml.gamecurrency.repository.GameCurrencyAccountIdGeneratorRepository;
import dml.gamecurrency.repository.GameCurrencyAccountRepository;
import dml.gamecurrency.repository.GameUserCurrencyAccountsRepository;
import dml.gamecurrency.service.GameCurrencyAccountingService;
import dml.gamecurrency.service.repositoryset.GameCurrencyAccountingServiceRepositorySet;
import dml.id.entity.LongIdGenerator;
import dml.qipairoom.entity.QipaiRoom;
import dml.qipairoom.entity.RandomNoZeroIntegerStringRoomNoGenerator;
import dml.qipairoom.repository.PlayerRoomJoinRepository;
import dml.qipairoom.repository.QipaiRoomRepository;
import dml.qipairoom.repository.RoomNoGeneratorRepository;
import dml.qipairoomcard.entity.ClearRoomTask;
import dml.qipairoomcard.repository.*;
import dml.qipairoomcard.service.RoomCardService;
import dml.qipairoomcard.service.repositoryset.RoomCardServiceRepositorySet;
import dml.qipairoomcard.service.result.RoomCardCreateRoomResult;
import dml.qipairoomcard.service.result.RoomCardJoinRoomResult;
import dml.qipairoomcard.service.result.RoomStartedResult;
import org.junit.Test;

import java.util.List;

public class RoomCardTest {

    @Test
    public void test() {
        int roomCardToConsume = 1;
        Long playerId1 = 1L;
        Long playerId2 = 2L;
        Long playerId3 = 3L;
        Long playerId4 = 4L;
        long roomOverTime = 60 * 60 * 1000; // 房间超时时间1小时
        long currentTime = 0L;

        // 给玩家1添加房卡
        GameCurrencyAccountingService.deposit(gameCurrencyAccountingServiceRepositorySet,
                playerId1, "roomCard", "5", new TestRoomCardAccount(),
                new TestRoomCardAccountBillItem(gameCurrencyAccountBillItemIdGenerator++));


        // 玩家1创建房间
        RoomCardCreateRoomResult createRoomResult1 = RoomCardService.createRoom(roomCardServiceRepositorySet,
                playerId1, roomCardToConsume, "roomCard", 4, new TestQipaiRoom(), currentTime);
        assert createRoomResult1.isSuccess();

        // 玩家2加入房间
        RoomCardJoinRoomResult joinRoomResult1 = RoomCardService.joinRoom(roomCardServiceRepositorySet,
                createRoomResult1.getRoomNo(), playerId2);
        assert joinRoomResult1.isSuccess();
        // 玩家3加入房间
        RoomCardJoinRoomResult joinRoomResult2 = RoomCardService.joinRoom(roomCardServiceRepositorySet,
                createRoomResult1.getRoomNo(), playerId3);
        assert joinRoomResult2.isSuccess();
        // 玩家4加入房间
        RoomCardJoinRoomResult joinRoomResult3 = RoomCardService.joinRoom(roomCardServiceRepositorySet,
                createRoomResult1.getRoomNo(), playerId4);
        assert joinRoomResult3.isSuccess();

        //4个玩家都准备好了
        RoomCardService.playerReady(roomCardServiceRepositorySet,
                createRoomResult1.getRoomNo(), playerId1, "startRoomTask");
        RoomCardService.playerReady(roomCardServiceRepositorySet,
                createRoomResult1.getRoomNo(), playerId2, "startRoomTask");
        RoomCardService.playerReady(roomCardServiceRepositorySet,
                createRoomResult1.getRoomNo(), playerId3, "startRoomTask");
        RoomCardService.playerReady(roomCardServiceRepositorySet,
                createRoomResult1.getRoomNo(), playerId4, "startRoomTask");

        // 定时任务来把准备好的房间状态改为“启动中”，开始分配战斗服
        QipaiRoom room1 = RoomCardService.startRoom(roomCardServiceRepositorySet,
                "startRoomTask", currentTime);

        // 分配成功后设房间为“游戏中”
        RoomStartedResult roomStartedResult1 = RoomCardService.roomStarted(roomCardServiceRepositorySet,
                room1.getNo(), "roomCard", 1,
                new TestRoomCardAccountBillItem(gameCurrencyAccountBillItemIdGenerator++));

        // 游戏结束，房间解散
        RoomCardService.dismissRoom(roomCardServiceRepositorySet,
                createRoomResult1.getRoomNo());

        // 玩家1创建房间
        RoomCardCreateRoomResult createRoomResult2 = RoomCardService.createRoom(roomCardServiceRepositorySet,
                playerId1, roomCardToConsume, "roomCard", 4, new TestQipaiRoom(), currentTime);

        // 时间流逝半小时
        currentTime += 30 * 60 * 1000; // 半小时

        // 定时任务清理失效房间
        long maxSegmentExecutionTime = 1000;
        long maxTimeToTaskReady = 1000;
        ClearRoomTask clearRoomTask1 = RoomCardService.createClearRoomTask(roomCardServiceRepositorySet,
                "clear_room", List.of(createRoomResult2.getRoomNo()), currentTime);
        boolean executeSuccess1 = RoomCardService.executeClearRoomTask(roomCardServiceRepositorySet,
                clearRoomTask1.getName(), currentTime, maxSegmentExecutionTime, maxTimeToTaskReady, roomOverTime);
        assert executeSuccess1;
        boolean executeSuccess2 = RoomCardService.executeClearRoomTask(roomCardServiceRepositorySet,
                clearRoomTask1.getName(), currentTime, maxSegmentExecutionTime, maxTimeToTaskReady, roomOverTime);
        assert !executeSuccess2;

        // 玩家1再次创建房间失败
        RoomCardCreateRoomResult createRoomResult3 = RoomCardService.createRoom(roomCardServiceRepositorySet,
                playerId1, roomCardToConsume, "roomCard", 4, new TestQipaiRoom(), currentTime);
        assert !createRoomResult3.isSuccess();

        // 时间流逝半小时
        currentTime += 30 * 60 * 1000; // 半小时

        // 定时任务清理失效房间，玩家1原房间已失效
        ClearRoomTask clearRoomTask2 = RoomCardService.createClearRoomTask(roomCardServiceRepositorySet,
                "clear_room", List.of(createRoomResult2.getRoomNo()), currentTime);
        boolean executeSuccess3 = RoomCardService.executeClearRoomTask(roomCardServiceRepositorySet,
                clearRoomTask2.getName(), currentTime, maxSegmentExecutionTime, maxTimeToTaskReady, roomOverTime);
        assert executeSuccess3;

        // 玩家1再次创建房间成功
        RoomCardCreateRoomResult createRoomResult4 = RoomCardService.createRoom(roomCardServiceRepositorySet,
                playerId1, roomCardToConsume, "roomCard", 4, new TestQipaiRoom(), currentTime);
        assert createRoomResult4.isSuccess();

        // 玩家1直接解散房间
        RoomCardService.dismissRoomByOwner(roomCardServiceRepositorySet, playerId1);


    }

    long gameCurrencyAccountBillItemIdGenerator = 1L;
    GameCurrencyAccountRepository gameCurrencyAccountRepository = TestCommonRepository.instance(GameCurrencyAccountRepository.class);
    GameCurrencyAccountIdGeneratorRepository gameCurrencyAccountIdGeneratorRepository =
            TestCommonSingletonRepository.instance(GameCurrencyAccountIdGeneratorRepository.class, new LongIdGenerator(1));
    GameUserCurrencyAccountsRepository gameUserCurrencyAccountsRepository = TestCommonRepository.instance(GameUserCurrencyAccountsRepository.class);
    GameCurrencyAccountBillItemRepository gameCurrencyAccountBillItemRepository = TestCommonRepository.instance(GameCurrencyAccountBillItemRepository.class);
    QipaiRoomRepository qipaiRoomRepository = TestCommonRepository.instance(QipaiRoomRepository.class);
    RoomNoGeneratorRepository roomNoGeneratorRepository = TestCommonSingletonRepository.instance(RoomNoGeneratorRepository.class,
            new RandomNoZeroIntegerStringRoomNoGenerator(6));
    PlayerRoomJoinRepository playerRoomJoinRepository = TestCommonRepository.instance(PlayerRoomJoinRepository.class);
    ClearRoomTaskRepository clearRoomTaskRepository = TestCommonRepository.instance(ClearRoomTaskRepository.class);
    ClearRoomTaskSegmentRepository clearRoomTaskSegmentRepository = TestCommonRepository.instance(ClearRoomTaskSegmentRepository.class);
    RoomAliveKeeperRepository roomAliveKeeperRepository = TestCommonRepository.instance(RoomAliveKeeperRepository.class);
    ClearRoomTaskSegmentIDGeneratorRepository clearRoomTaskSegmentIDGeneratorRepository =
            TestCommonSingletonRepository.instance(ClearRoomTaskSegmentIDGeneratorRepository.class, new LongIdGenerator(1));
    StartRoomTaskRepository startRoomTaskRepository = TestCommonRepository.instance(StartRoomTaskRepository.class);
    StartRoomTaskSegmentRepository startRoomTaskSegmentRepository = TestCommonRepository.instance(StartRoomTaskSegmentRepository.class);
    StartRoomTaskSegmentIDGeneratorRepository startRoomTaskSegmentIDGeneratorRepository =
            TestCommonSingletonRepository.instance(StartRoomTaskSegmentIDGeneratorRepository.class, new LongIdGenerator(1));
    RoomStateRepository roomStateRepository = TestCommonRepository.instance(RoomStateRepository.class);


    GameCurrencyAccountingServiceRepositorySet gameCurrencyAccountingServiceRepositorySet = new GameCurrencyAccountingServiceRepositorySet() {
        @Override
        public GameCurrencyAccountRepository getGameCurrencyAccountRepository() {
            return gameCurrencyAccountRepository;
        }

        @Override
        public GameCurrencyAccountIdGeneratorRepository getGameCurrencyAccountIdGeneratorRepository() {
            return gameCurrencyAccountIdGeneratorRepository;
        }

        @Override
        public GameUserCurrencyAccountsRepository getGameUserCurrencyAccountsRepository() {
            return gameUserCurrencyAccountsRepository;
        }

        @Override
        public GameCurrencyAccountBillItemRepository getGameCurrencyAccountBillItemRepository() {
            return gameCurrencyAccountBillItemRepository;
        }

    };

    RoomCardServiceRepositorySet roomCardServiceRepositorySet = new RoomCardServiceRepositorySet() {

        @Override
        public RoomAliveKeeperRepository getRoomAliveKeeperRepository() {
            return roomAliveKeeperRepository;
        }

        @Override
        public ClearRoomTaskRepository getClearRoomTaskRepository() {
            return clearRoomTaskRepository;
        }

        @Override
        public ClearRoomTaskSegmentRepository getClearRoomTaskSegmentRepository() {
            return clearRoomTaskSegmentRepository;
        }

        @Override
        public ClearRoomTaskSegmentIDGeneratorRepository getClearRoomTaskSegmentIDGeneratorRepository() {
            return clearRoomTaskSegmentIDGeneratorRepository;
        }

        @Override
        public StartRoomTaskRepository getStartRoomTaskRepository() {
            return startRoomTaskRepository;
        }

        @Override
        public StartRoomTaskSegmentRepository getStartRoomTaskSegmentRepository() {
            return startRoomTaskSegmentRepository;
        }

        @Override
        public StartRoomTaskSegmentIDGeneratorRepository getStartRoomTaskSegmentIDGeneratorRepository() {
            return startRoomTaskSegmentIDGeneratorRepository;
        }

        @Override
        public RoomStateRepository getRoomStateRepository() {
            return roomStateRepository;
        }

        @Override
        public QipaiRoomRepository getQipaiRoomRepository() {
            return qipaiRoomRepository;
        }

        @Override
        public RoomNoGeneratorRepository getRoomNoGeneratorRepository() {
            return roomNoGeneratorRepository;
        }

        @Override
        public PlayerRoomJoinRepository getPlayerRoomJoinRepository() {
            return playerRoomJoinRepository;
        }

        @Override
        public GameCurrencyAccountRepository getGameCurrencyAccountRepository() {
            return gameCurrencyAccountRepository;
        }

        @Override
        public GameCurrencyAccountIdGeneratorRepository getGameCurrencyAccountIdGeneratorRepository() {
            return gameCurrencyAccountIdGeneratorRepository;
        }

        @Override
        public GameUserCurrencyAccountsRepository getGameUserCurrencyAccountsRepository() {
            return gameUserCurrencyAccountsRepository;
        }

        @Override
        public GameCurrencyAccountBillItemRepository getGameCurrencyAccountBillItemRepository() {
            return gameCurrencyAccountBillItemRepository;
        }
    };
}
