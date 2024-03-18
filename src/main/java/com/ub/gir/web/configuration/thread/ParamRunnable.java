/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.thread;


import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;


/**
 * <p>Thread Pool ExecutorService Runnable</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/5/2
 */
@Slf4j
public abstract class ParamRunnable<T> implements Runnable {

    private T param;

    private final Consumer<T> paramConsumer;

    protected ParamRunnable(Supplier<T> paramSupplier) {
        this(paramSupplier, null);
        log.info("**** Thread Runable Thread Name : {}", Thread.currentThread().getName());
    }

    protected ParamRunnable(Supplier<T> paramSupplier, Consumer<T> paramConsumer) {

        if (paramSupplier!=null) {
            this.param = paramSupplier.get();
        }

        this.paramConsumer = paramConsumer;

    }

    @Override
    public void run() {

        if (this.paramConsumer!=null) {
            this.paramConsumer.accept(param);
        }

        run(param);

    }

    public abstract void run(T param);

}
