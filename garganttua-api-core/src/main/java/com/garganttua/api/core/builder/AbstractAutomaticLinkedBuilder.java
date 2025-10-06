package com.garganttua.api.core.builder;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAutomaticLinkedBuilder;

public abstract class AbstractAutomaticLinkedBuilder<Built, Builder, Up>
        implements IAutomaticLinkedBuilder<Built, Up, Builder> {

    private @Nonnull Boolean autoDetect;
    private @Nonnull Up up;
    private Built built;

    protected AbstractAutomaticLinkedBuilder(Up up){
        this.up = Objects.requireNonNull(up, "Up cannot be null");
        this.autoDetect = false;
    }

    @Override
    public Up up() {
        return this.up;
    }

    @Override
    public Builder autoDetect(boolean b) throws CoreException {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return (Builder) this;
    }

    @Override
    public Built build() throws CoreException {
        if( this.built != null )
            return this.built;
        if (this.autoDetect) {
            this.doAutoDetection();
        }

        return this.built = this.doBuild();
    }

    protected abstract Built doBuild() throws CoreException;
    protected abstract void doAutoDetection() throws CoreException;
}
