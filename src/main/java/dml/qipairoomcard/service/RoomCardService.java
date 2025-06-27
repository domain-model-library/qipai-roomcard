package dml.qipairoomcard.service;

import dml.gamecurrency.service.GameCurrencyAccountingService;
import dml.keepalive.repository.AliveKeeperRepository;
import dml.keepalive.service.KeepAliveService;
import dml.keepalive.service.repositoryset.AliveKeeperServiceRepositorySet;
import dml.largescaletaskmanagement.repository.LargeScaleTaskRepository;
import dml.largescaletaskmanagement.repository.LargeScaleTaskSegmentRepository;
import dml.largescaletaskmanagement.service.LargeScaleTaskService;
import dml.largescaletaskmanagement.service.repositoryset.LargeScaleTaskServiceRepositorySet;
import dml.largescaletaskmanagement.service.result.TakeTaskSegmentToExecuteResult;
import dml.qipairoom.entity.QipaiRoom;
import dml.qipairoom.service.RoomService;
import dml.qipairoom.service.result.CreateRoomResult;
import dml.qipairoom.service.result.JoinRoomResult;
import dml.qipairoomcard.entity.ClearRoomTask;
import dml.qipairoomcard.entity.ClearRoomTaskSegment;
import dml.qipairoomcard.entity.RoomAliveKeeper;
import dml.qipairoomcard.repository.ClearRoomTaskSegmentIDGeneratorRepository;
import dml.qipairoomcard.service.repositoryset.RoomCardServiceRepositorySet;
import dml.qipairoomcard.service.result.RoomCardCreateRoomResult;
import dml.qipairoomcard.service.result.RoomCardJoinRoomResult;

import java.util.List;

public class RoomCardService {
    public static RoomCardCreateRoomResult createRoom(RoomCardServiceRepositorySet repositorySet,
                                                      Object userId, int roomCardToConsume, String roomCardCurrency,
                                                      int playersCount, QipaiRoom newQipaiRoom, long currentTime) {
        RoomCardCreateRoomResult result = new RoomCardCreateRoomResult();
        boolean playerInRoom = RoomService.isPlayerInRoom(repositorySet, userId.toString());
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

        CreateRoomResult createRoomResult = RoomService.createRoom(repositorySet, userId.toString(), playersCount, newQipaiRoom);
        if (!createRoomResult.isSuccess()) {
            throw new RuntimeException("create room failed, user already in another room or other error");
        }

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
        RoomCardJoinRoomResult result = new RoomCardJoinRoomResult();
        JoinRoomResult joinRoomResult = RoomService.joinRoom(repositorySet, roomNo, userId.toString());
        result.setSuccess(joinRoomResult.isSuccess());
        result.setAlreadyInRoom(joinRoomResult.isAlreadyIn());
        result.setInAnotherRoom(joinRoomResult.isInAnotherRoom());
        result.setRoomFull(joinRoomResult.isFull());
        return result;
    }

    public static void playerReady(RoomCardServiceRepositorySet repositorySet,
                                   String roomNo, Object userId) {
        RoomService.playerReady(repositorySet, roomNo, userId.toString());
    }

    public static void dismissRoom(RoomCardServiceRepositorySet repositorySet, String roomNo) {
        RoomService.dismissRoom(repositorySet, roomNo);
        KeepAliveService.removeAliveKeeper(getAliveKeeperServiceRepositorySet(repositorySet), roomNo);
    }

    public static ClearRoomTask createClearRoomTask(RoomCardServiceRepositorySet repositorySet,
                                                    String taskName, List<String> roomNoList, long currentTime) {
        ClearRoomTaskSegmentIDGeneratorRepository clearRoomTaskSegmentIDGeneratorRepository = repositorySet.getClearRoomTaskSegmentIDGeneratorRepository();

        ClearRoomTask task = (ClearRoomTask) LargeScaleTaskService.createTask(getLargeScaleTaskServiceRepositorySet(repositorySet),
                taskName, new ClearRoomTask(), currentTime);

        if (task != null) {
            if (roomNoList.isEmpty()) {
                return task;
            }
            for (String roomNo : roomNoList) {
                ClearRoomTaskSegment segment = new ClearRoomTaskSegment();
                segment.setRoomNo(roomNo);
                segment.setId(clearRoomTaskSegmentIDGeneratorRepository.take().generateId());
                LargeScaleTaskService.addTaskSegment(getLargeScaleTaskServiceRepositorySet(repositorySet),
                        taskName, segment);
            }
            LargeScaleTaskService.setTaskReadyToProcess(getLargeScaleTaskServiceRepositorySet(repositorySet),
                    taskName);
        }
        return task;
    }

    private static LargeScaleTaskServiceRepositorySet getLargeScaleTaskServiceRepositorySet(RoomCardServiceRepositorySet repositorySet) {
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
                getLargeScaleTaskServiceRepositorySet(repositorySet),
                taskName, currentTime, maxSegmentExecutionTime, maxTimeToTaskReady);
        if (takeSegmentResult.isTaskCompleted()) {
            LargeScaleTaskService.removeTask(getLargeScaleTaskServiceRepositorySet(repositorySet), taskName);
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
        LargeScaleTaskService.completeTaskSegment(getLargeScaleTaskServiceRepositorySet(repositorySet), segment.getId());
        return true;
    }

    public static void dismissRoomByOwner(RoomCardServiceRepositorySet repositorySet, Object ownerPlayerId) {
        RoomService.dismissRoomByOwner(repositorySet, ownerPlayerId.toString());
    }

    public static QipaiRoom findRoomForPlayer(RoomCardServiceRepositorySet repositorySet, Object userId) {
        return RoomService.findRoomForPlayer(repositorySet, userId.toString());
    }
}
