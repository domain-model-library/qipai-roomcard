package dml.qipairoomcard.entity;

import dml.largescaletaskmanagement.entity.LargeScaleTaskBase;

public class StartRoomTask extends LargeScaleTaskBase {
    private String taskName;

    @Override
    public void setName(String name) {
        this.taskName = name;
    }

    @Override
    public String getName() {
        return taskName;
    }
}
