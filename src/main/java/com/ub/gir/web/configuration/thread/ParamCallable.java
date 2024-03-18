/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.thread;


import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;


/**
 * <p>Thread Pool ExecutorService Callable</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/5/3
 */
@Slf4j
public abstract class ParamCallable<P, R> implements Callable<R> {

    private P param;

    private final Consumer<P> paramConsumer;

    protected ParamCallable(Supplier<P> paramSupplier) {
        this(paramSupplier, null);
        log.info("**** Thread Callable Thread Name : {}", Thread.currentThread().getName());
    }

    protected ParamCallable(Supplier<P> paramSupplier, Consumer<P> paramConsumer) {

        if (paramSupplier!=null) {
            this.param = paramSupplier.get();
        }

        this.paramConsumer = paramConsumer;

    }

    @Override
    public R call() {

        if (this.paramConsumer!=null) {
            this.paramConsumer.accept(param);
        }

        return call(param);

    }

    public abstract R call(P param);

}
