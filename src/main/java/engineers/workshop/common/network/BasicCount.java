package engineers.workshop.common.network;

public class BasicCount implements IBitCount {

    private final int count;

    public BasicCount(int count) {
        this.count = count;
    }

    @Override
    public int getBitCount() {
        return count;
    }
}
