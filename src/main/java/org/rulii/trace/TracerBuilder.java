package org.rulii.trace;

public final class TracerBuilder {

    public TracerBuilder() {
        super();
    }

    public Tracer build() {
        return new DefaultTracer();
    }
}
