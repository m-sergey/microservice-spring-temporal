package me.mamre.model;

public enum PaymentStatus {
    CREATED(0, false),
    AUTHORIZED(10, false),
    CAPTURED(20, false),
    SETTLED(30, true),

    FAILED(100, true),
    CANCELED(100, true);

    public final int rank;
    public final boolean terminal;

    PaymentStatus(int rank, boolean terminal) {
        this.rank = rank;
        this.terminal = terminal;
    }
}
