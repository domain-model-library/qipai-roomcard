import dml.qipairoom.entity.QipaiRoomBase;

public class TestQipaiRoom extends QipaiRoomBase {
    private String no;

    @Override
    public void setNo(String no) {
        this.no = no;
    }

    @Override
    public String getNo() {
        return no;
    }
}
