package dev.bikzxc.chunkyidle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkyIdleLogTest {
    @Test void consoleMessagesContainTheVisibleModPrefix() {
        assertEquals("[ChunkyIdle] Server is empty.", ChunkyIdleLog.prefix("Server is empty."));
    }
}
