package dml.qipairoomcard.service;

import dml.gamecurrency.entity.GameCurrencyAccountBillItem;
import dml.gamecurrency.service.GameCurrencyAccountingService;
import dml.gamecurrency.service.result.WithdrawResult;
import dml.keepalive.repository.AliveKeeperRepository;
import dml.keepalive.service.KeepAliveService;
import dml.keepalive.service.repositoryset.AliveKeeperServiceRepositorySet;
import dml.largescaletaskmanagement.repository.LargeScaleTaskRepository;
import dml.largescaletaskmanagement.repository.LargeScaleTaskSegmentRepository;
import dml.largescaletaskmanagement.service.LargeScaleTaskService;
import dml.largescaletaskmanagement.service.repositoryset.LargeScaleTaskServiceRepositorySet;
import dml.largescaletaskmanagement.service.result.TakeTaskSegmentToExecuteResult;
import dml.qipairoom.entity.QipaiRoom;
import dml.qipairoom.repository.QipaiRoomRepository;
import dml.qipairoom.service.RoomService;
import dml.qipairoom.service.result.CreateRoomResult;
import dml.qipairoom.service.result.JoinRoomResult;
import dml.qipairoomcard.entity.*;
import dml.qipairoomcard.repository.ClearRoomTaskSegmentIDGeneratorRepository;
import dml.qipairoomcard.repository.RoomStateRepository;
import dml.qipairoomcard.repository.StartRoomTaskSegmentIDGeneratorRepository;
import dml.qipairoomcard.service.repositoryset.RoomCardServiceRepositorySet;
import dml.qipairoomcard.service.result.RoomCardCreateRoomResult;
import dml.qipairoomcard.service.result.RoomCardJoinRoomResult;
import dml.qipairoomcard.service.result.RoomStartedResult;

import java.util.List;

public class RoomCardService {
    public static RoomCardCreateRoomResult createRoom(RoomCardServiceRepositorySet repositorySet,
                                                      Object userId, int roomCardToConsume, String roomCardCurrency,
                                                      int playersCount, QipaiRoom newQipaiRoom, long currentTime) {
        RoomStateRepository roomStateRepository = repositorySet.getRoomStateRepository();

        RoomCardCreateRoomResult result = new RoomCardCreateRoomResult();
        boolean playerInRoom = RoomService.isPlayerInRoom(repositorySet, userId);
        if (playerInRoom) {
            result.setSuccess(false);
            result.setAlreadyInRoom(true);
            return result;
        }

        boolean enoughRoomCard = GameCurrencyAccountingService.isBalanceGreaterThanOrEqualTo(repositorySet,
                userId, roomCardCurrency, String.valueOf(roomCardToConsume));
        if (!enoughRoomCard) {
            result.setSuccess(false);
            result.setInsufficientRoomCard(true);
            return result;
        }

        CreateRoomResult createRoomResult = RoomService.createRoom(repositorySet, userId, playersCount, newQipaiRoom);
        if (!createRoomResult.isSuccess()) {
            throw new RuntimeException("create room failed, user already in another room or other error");
        }

        RoomState roomState = new RoomState();
        roomState.setRoomNo(createRoomResult.getRoomNo());
        roomState.setState(RoomStateEnum.initial);
        roomStateRepository.put(roomState);

        KeepAliveService.createAliveKeeper(getAliveKeeperServiceRepositorySet(repositorySet), createRoomResult.getRoomNo(), currentTime, new RoomAliveKeeper());

        result.setRoomNo(createRoomResult.getRoomNo());
        result.setSuccess(true);
        return result;
    }

    private static AliveKeeperServiceRepositorySet getAliveKeeperServiceRepositorySet(RoomCardServiceRepositorySet repositorySet) {
        return new AliveKeeperServiceRepositorySet() {

            @Override
            public AliveKeeperRepository getAliveKeeperRepository() {
                return repositorySet.getRoomAliveKeeperRepository();
            }
        };
    }

    public static RoomCardJoinRoomResult joinRoom(RoomCardServiceRepositorySet repositorySet,
                                                  String roomNo, Object userId) {
        RoomStateRepository roomStateRepository = repositorySet.getRoomStateRepository();

        RoomCardJoinRoomResult result = new RoomCardJoinRoomResult();
        RoomState roomState = roomStateRepository.take(roomNo);
        if (!roomState.is(RoomStateEnum.initial)) {
            result.setRoomStarted(true);
            result.setSuccess(false);
            return result;
        }
        JoinRoomResult joinRoomResult = RoomService.joinRoom(repositorySet, roomNo, userId);
        result.setSuccess(joinRoomResult.isSuccess());
        result.setAlreadyInRoom(joinRoomResult.isAlreadyIn());
        result.setInAnotherRoom(joinRoomResult.isInAnotherRoom());
        result.setRoomFull(joinRoomResult.isFull());
        return result;
    }

    public static void playerReady(RoomCardServiceRepositorySet repositorySet,
                                   String roomNo, Object userId, String startRoomTaskName) {
        RoomStateRepository roomStateRepository = repositorySet.getRoomStateRepository();

        RoomState roomState = roomStateRepository.take(roomNo);
        if (!roomState.is(RoomStateEnum.initial)) {
            return;
        }

        StartRoomTaskSegmentIDGeneratorRepository startRoomTaskSegmentIDGeneratorRepository =
                repositorySet.getStartRoomTaskSegmentIDGeneratorRepository();
        QipaiRoom room = RoomService.playerReady(repositorySet, roomNo, userId);
        if (room.isAllPlayerReady()) {
            StartRoomTaskSegment startRoomTaskSegment = new StartRoomTaskSegment();
            startRoomTaskSegment.setId(startRoomTaskSegmentIDGeneratorRepository.take().generateId());
            startRoomTaskSegment.setRoomNo(roomNo);
            LargeScaleTaskService.addTaskSegmentAndNewAndReadyTaskIfNotExists(getStartRoomTaskServiceRepositorySet(repositorySet),
                    startRoomTaskName, startRoomTaskSegment, new StartRoomTask());
        }
    }

    private static LargeScaleTaskServiceRepositorySet getStartRoomTaskServiceRepositorySet(
            RoomCardServiceRepositorySet repositorySet) {
        return new LargeScaleTaskServiceRepositorySet() {
            @Override
            public LargeScaleTaskRepository getLargeScaleTaskRepository() {
                return repositorySet.getStartRoomTaskRepository();
            }

            @Override
            public LargeScaleTaskSegmentRepository getLargeScaleTaskSegmentRepository() {
                return repositorySet.getStartRoomTaskSegmentRepository();
            }
        };
    }

    public static void dismissRoom(RoomCardServiceRepositorySet repositorySet, String roomNo) {
        RoomStateRepository roomStateRepository = repositorySet.getRoomStateRepository();

        RoomService.dismissRoom(repositorySet, roomNo);
        KeepAliveService.removeAliveKeeper(getAliveKeeperServiceRepositorySet(repositorySet), roomNo);
        roomStateRepository.remove(roomNo);
    }

    public static ClearRoomTask createClearRoomTask(RoomCardServiceRepositorySet repositorySet,
                                                    String taskName, List<String> roomNoList, long currentTime) {
        ClearRoomTaskSegmentIDGeneratorRepository clearRoomTaskSegmentIDGeneratorRepository = repositorySet.getClearRoomTaskSegmentIDGeneratorRepository();

        ClearRoomTask task = (ClearRoomTask) LargeScaleTaskService.createTask(getClearRoomTaskServiceRepositorySet(repositorySet),
                taskName, new ClearRoomTask(), currentTime);

        if (task != null) {
            if (roomNoList.isEmpty()) {
                return task;
            }
            for (String roomNo : roomNoList) {
                ClearRoomTaskSegment segment = new ClearRoomTaskSegment();
                segment.setRoomNo(roomNo);
                segment.setId(clearRoomTaskSegmentIDGeneratorRepository.take().generateId());
                LargeScaleTaskService.addTaskSegment(getClearRoomTaskServiceRepositorySet(repositorySet),
                        taskName, segment);
            }
            LargeScaleTaskService.setTaskReadyToProcess(getClearRoomTaskServiceRepositorySet(repositorySet),
                    taskName);
        }
        return task;
    }

    private static LargeScaleTaskServiceRepositorySet getClearRoomTaskServiceRepositorySet(RoomCardServiceRepositorySet repositorySet) {
        return new LargeScaleTaskServiceRepositorySet() {

            @Override
            public LargeScaleTaskRepository getLargeScaleTaskRepository() {
                return repositorySet.getClearRoomTaskRepository();
            }

            @Override
            public LargeScaleTaskSegmentRepository getLargeScaleTaskSegmentRepository() {
                return repositorySet.getClearRoomTaskSegmentRepository();
            }
        };
    }

    public static boolean executeClearRoomTask(RoomCardServiceRepositorySet repositorySet,
                                               String taskName, long currentTime, long maxSegmentExecutionTime, long maxTimeToTaskReady,
                                               long roomAliveTimeout) {
        TakeTaskSegmentToExecuteResult takeSegmentResult = LargeScaleTaskService.takeTaskSegmentToExecute(
                getClearRoomTaskServiceRepositorySet(repositorySet),
                taskName, currentTime, maxSegmentExecutionTime, maxTimeToTaskReady);
        if (takeSegmentResult.isTaskCompleted()) {
            LargeScaleTaskService.removeTask(getClearRoomTaskServiceRepositorySet(repositorySet), taskName);
            return false;
        }
        ClearRoomTaskSegment segment = (ClearRoomTaskSegment) takeSegmentResult.getTaskSegment();
        if (segment == null) {
            return false;
        }
        String roomNo = segment.getRoomNo();
        boolean roomAlive = KeepAliveService.isAlive(getAliveKeeperServiceRepositorySet(repositorySet),
                roomNo, currentTime, roomAliveTimeout);
        if (!roomAlive) {
            RoomService.dismissRoom(repositorySet, roomNo);
        }
        LargeScaleTaskService.completeTaskSegment(getClearRoomTaskServiceRepositorySet(repositorySet), segment.getId());
        return true;
    }

    public static void dismissRoomByOwner(RoomCardServiceRepositorySet repositorySet, Object ownerPlayerId) {
        RoomStateRepository roomStateRepository = repositorySet.getRoomStateRepository();

        QipaiRoom room = RoomService.dismissRoomByOwner(repositorySet, ownerPlayerId.toString());
        if (room != null) {
            KeepAliveService.removeAliveKeeper(getAliveKeeperServiceRepositorySet(repositorySet), room.getNo());
            roomStateRepository.remove(room.getNo());
        }
    }

    public static QipaiRoom findRoomForPlayer(RoomCardServiceRepositorySet repositorySet, Object userId) {
        return RoomService.findRoomForPlayer(repositorySet, userId);
    }

    public static QipaiRoom startRoom(RoomCardServiceRepositorySet repositorySet,
                                      String startRoomTaskName, long currentTime) {
        QipaiRoomRepository<QipaiRoom> qipaiRoomRepository = repositorySet.getQipaiRoomRepository();
        RoomStateRepository roomStateRepository = repositorySet.getRoomStateRepository();

        TakeTaskSegmentToExecuteResult taskSegmentToExecuteResult = LargeScaleTaskService.takeTaskSegmentToExecute(
                getStartRoomTaskServiceRepositorySet(repositorySet),
                startRoomTaskName, currentTime, 0, 0);
        StartRoomTaskSegment startRoomTaskSegment = (StartRoomTaskSegment) taskSegmentToExecuteResult.getTaskSegment();
        if (startRoomTaskSegment == null) {
            return null;
        }
        String roomNo = startRoomTaskSegment.getRoomNo();
        QipaiRoom room = null;
        RoomState roomState = roomStateRepository.take(roomNo);
        if (roomState.is(RoomStateEnum.initial)) {
            room = qipaiRoomRepository.take(roomNo);
            if (room.isAllPlayerReady()) {
                roomState.setState(RoomStateEnum.starting);
            }
        }
        LargeScaleTaskService.completeTaskSegment(getStartRoomTaskServiceRepositorySet(repositorySet),
                startRoomTaskSegment.getId());
        return room;
    }

    public static RoomStartedResult roomStarted(RoomCardServiceRepositorySet repositorySet, String roomNo,
                                                String roomCardCurrency, int roomCardToConsume, GameCurrencyAccountBillItem billItem) {
        QipaiRoomRepository<QipaiRoom> qipaiRoomRepository = repositorySet.getQipaiRoomRepository();
        RoomStateRepository roomStateRepository = repositorySet.getRoomStateRepository();

        RoomStartedResult result = new RoomStartedResult();
        RoomState roomState = roomStateRepository.take(roomNo);
        if (!roomState.is(RoomStateEnum.starting)) {
            result.setSuccess(false);
            result.setRoomNotStarting(true);
            return result;
        }

        //扣除房卡
        QipaiRoom room = qipaiRoomRepository.take(roomNo);
        WithdrawResult withdrawResult = GameCurrencyAccountingService.withdrawIfBalanceSufficient(repositorySet,
                room.getOwnerId(), roomCardCurrency, String.valueOf(roomCardToConsume), billItem);
        if (withdrawResult == null) {
            dismissRoom(repositorySet, roomNo);
            result.setSuccess(false);
            result.setInsufficientRoomCard(true);
            return result;
        }
        roomState.setState(RoomStateEnum.playing);
        result.setSuccess(true);
        return result;
    }
}
