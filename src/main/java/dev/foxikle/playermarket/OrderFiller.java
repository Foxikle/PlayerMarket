package dev.foxikle.playermarket;

import java.time.Instant;
import java.util.UUID;

public record OrderFiller(UUID player, short amountFilled, long time) {

    public Instant getTime() {
        return Instant.ofEpochMilli(time);
    }
}
