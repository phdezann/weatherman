package org.phdezann.home.console.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AsciiProgressBarBuilderTest {

    private final AsciiProgressBarBuilder asciiProgressBarBuilder = new AsciiProgressBarBuilder();

    @Test
    void print() {
        assertThat(asciiProgressBarBuilder.build(7000, 6500, 16, 7)).containsExactly( //
                "[================]", //
                "[================]", //
                "[================]", //
                "[================]", //
                "[================]", //
                "[===========·····]", //
                "[················]");
    }
}
