package ch.idsia.ai;

/**
 * Minimal interface for a function approximator
 */
public interface FA<I, O> {

    /**
     * Resets the function approximator
     */
    public void reset();

    /**
     * Computes and returns the output of the <code>FA</code>
     * (e.g. the output of an evolvable
     * network, or the coefficients of an evolvablearray)
     * given the the input.
     *
     * @param i the input
     * @return the output.
     */

    public O approximate(I i);

}

