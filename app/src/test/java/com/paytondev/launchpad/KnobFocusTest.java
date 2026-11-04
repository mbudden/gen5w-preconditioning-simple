package com.paytondev.launchpad;

import org.junit.Test;
import static org.junit.Assert.*;

public class KnobFocusTest {
    @Test public void knobWrapsAndSkipsUnavailableControls() {
        boolean[] enabled = {true, false, true, false};
        assertEquals(0, KnobFocus.next(-1, 1, enabled));
        assertEquals(2, KnobFocus.next(0, 1, enabled));
        assertEquals(0, KnobFocus.next(2, 1, enabled));
        assertEquals(2, KnobFocus.next(0, -1, enabled));
        assertEquals(2, KnobFocus.next(-1, -1, enabled));
        assertEquals(-1, KnobFocus.next(-1, 1, new boolean[]{false, false}));
        assertEquals(-1, KnobFocus.next(-1, 1, new boolean[]{}));
    }
}
