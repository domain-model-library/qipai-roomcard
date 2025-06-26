package dml.qipairoomcard.entity;

import dml.largescaletaskmanagement.entity.LargeScaleTaskBase;

public class ClearRoomTask extends LargeScaleTaskBase {
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
