import dml.gamecurrency.repository.GameCurrencyAccountBillItemRepository;
import dml.gamecurrency.repository.GameCurrencyAccountIdGeneratorRepository;
import dml.gamecurrency.repository.GameCurrencyAccountRepository;
import dml.gamecurrency.repository.GameUserCurrencyAccountsRepository;
import dml.gamecurrency.service.GameCurrencyAccountingService;
import dml.gamecurrency.service.repositoryset.GameCurrencyAccountingServiceRepositorySet;
import dml.qipairoom.repository.PlayerRoomJoinRepository;
import dml.qipairoom.repository.QipaiRoomRepository;
import dml.qipairoom.repository.RoomNoGeneratorRepository;
import dml.qipairoomcard.service.RoomCardService;
import dml.qipairoomcard.service.repositoryset.RoomCardServiceRepositorySet;
import dml.qipairoomcard.service.result.RoomCardCreateRoomResult;
import org.junit.Test;

public class RoomCardTest {

    @Test
    public void test() {
        int roomCardToConsume = 1;
        Long playerId1 = 1L;
        Long playerId2 = 2L;
        Long playerId3 = 3L;
        Long playerId4 = 4L;

        // 给玩家1添加房卡
        GameCurrencyAccountingService.deposit(gameCurrencyAccountingServiceRepositorySet,
                playerId1, "roomCard", "5", new TestRoomCardAccount(),
                new TestRoomCardAccountBillItem(gameCurrencyAccountBillItemIdGenerator++));


        // 玩家1创建房间并消耗房卡
        RoomCardCreateRoomResult createRoomResult1 = RoomCardService.createRoom(roomCardServiceRepositorySet,
                playerId1, roomCardToConsume, "roomCard",
                new TestRoomCardAccountBillItem(gameCurrencyAccountBillItemIdGenerator++),
                4, new TestQipaiRoom());

        // 玩家2加入房间
        // 玩家3加入房间
        // 玩家4加入房间

        // 游戏结束，房间解散

        // 玩家1创建房间
        // 时间流逝半小时
        // 定时任务清理失效房间
        // 玩家1再次创建房间失败
        // 时间流逝半小时
        // 定时任务清理失效房间，玩家1原房间已失效
        // 玩家1再次创建房间成功


    }

    long gameCurrencyAccountBillItemIdGenerator = 1L;

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
        public QipaiRoomRepository getQipaiRoomRepository() {
            return null;
        }

        @Override
        public RoomNoGeneratorRepository getRoomNoGeneratorRepository() {
            return null;
        }

        @Override
        public PlayerRoomJoinRepository getPlayerRoomJoinRepository() {
            return null;
        }

        @Override
        public GameCurrencyAccountRepository getGameCurrencyAccountRepository() {
            return null;
        }

        @Override
        public GameCurrencyAccountIdGeneratorRepository getGameCurrencyAccountIdGeneratorRepository() {
            return null;
        }

        @Override
        public GameUserCurrencyAccountsRepository getGameUserCurrencyAccountsRepository() {
            return null;
        }

        @Override
        public GameCurrencyAccountBillItemRepository getGameCurrencyAccountBillItemRepository() {
            return null;
        }
    };
}
