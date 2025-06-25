import dml.gamecurrency.entity.GameCurrencyAccountBillItemBase;

public class TestRoomCardAccountBillItem extends GameCurrencyAccountBillItemBase {
    private long id;

    public TestRoomCardAccountBillItem(long id) {
        this.id = id;
    }

    @Override
    public void setId(Object id) {
        this.id = (long) id;
    }
}
