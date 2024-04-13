package org.cyclops.integrateddynamics.api.evaluate;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * Exception to indicate a failed evaluation.
 * @author rubensworks
 */
public class EvaluationException extends Exception {

    private final MutableComponent errorMessage;
    private boolean retryEvaluation;
    private final List<Runnable> resolutionListeners;

    public EvaluationException(MutableComponent errorMessage) {
        super(errorMessage.toString());
        this.errorMessage = errorMessage;
        this.retryEvaluation = false;
        this.resolutionListeners = Lists.newArrayList();
    }

    public MutableComponent getErrorMessage() {
        return errorMessage;
    }

    /**
     * This should only be set at construction time of this exception.
     * @param retryEvaluation If the evaluation may be retried again in the next tick.
     */
    public void setRetryEvaluation(boolean retryEvaluation) {
        this.retryEvaluation = retryEvaluation;
    }

    /**
     * @return If the evaluation may be retried again in the next tick.
     */
    public boolean isRetryEvaluation() {
        return retryEvaluation;
    }

    public void addResolutionListeners(Runnable listener) {
        this.resolutionListeners.add(listener);
    }

    /**
     * If evaluators halt operation due to this thrown evaluation,
     * invoking this method will cause them to remove the exception and resume operation.
     *
     * In contrast to {@link #retryEvaluation}, this may be invoked anywhere within the lifetime of evaluation
     * exceptions.
     */
    public void resolve() {
        for (Runnable resolutionListener : Lists.newArrayList(this.resolutionListeners.listIterator())) {
            resolutionListener.run();
        }
    }
}
