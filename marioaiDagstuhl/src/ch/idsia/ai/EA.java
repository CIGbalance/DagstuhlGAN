package ch.idsia.ai;

/**
 * Interface to a Generic Evolutionary Algorithm.<br>
 * The contructor simply needs an <code>{@link Evolvable}</code>,
 * a Task (depending on which the evaluations will run in series, in parallel or in different threads)
 * and possibly few other paremeters.
 * <p/>
 * All the <code>EA</code> must assume that the best individual is the first one in ascending order,
 * where the ordering is produced by sorting according to the <code>compareTo</code> method,
 * of the individual's fitness. In most of the cases ordering according to max fitness is assumed.
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * Is STRONGLY suggested to have a <code>public static void main(String args[])</code>
 * method inside which a SELF CONTAINED bare bone example using an <code>EvolvableArray</code>
 * should be implemented. <br>
 * The class SSIndividual might be used to simplify the implementation of the algorithm,
 * however it should not be exposed in any way.
 *
 * @see Evolvable
 */

public interface EA {

    /**
     * Returns the best <code>Evolvables</code> of the population (e.g. the elite or the
     * Pareto Set). The array may have only one element (e.g. for a hill climber).
     * <br>
     * Is not a copy of the bests!!!
     * <br>
     * If you really want only the best guy, in the majority of the cases, is simply
     * possible to pick the first guy of the set <code>ES.getBests()[0]</code>.
     * This does not make ANY SENSE when there is not an absolute ordering between
     * them (e.g. Pareto set).
     *
     * @return array of <code>Evolvables</code> the array may have only one element.
     */
    public Evolvable[] getBests();

    /**
     * Returns the best <code>fitnesses</code> of the population (e.g. of the the elite or
     * of the Pareto Set).
     *
     * @return array of <code>fitnesses</code> the array may have only one element.
     */
    public double[] getBestFitnesses();

    /**
     * Steps the <code>EA</code> one generation forward  the full loop is executed:
     * <ul>
     * <li> generates a population of <code>Evolvables</code>,
     * <li> resets the evolvable
     * <li> runs the <code>Task</code> with each of the <code>Evolvables</code>,
     * <li> computes the fitnesses (e.g. averaing),
     * <li> ranks and selects the <code>Evolvables</code>.
     * </ul>
     * <p/>
     * An exception is generated if the number of objective in the task
     * does not agree with the specific ea.
     *
     * @throws Exception
     */
    public void nextGeneration() throws Exception;

    /**
     * Returns a description of this Evolutionary algorithm, as a good practise,
     * all the specific parameters of the tasks should be printed as well.
     *
     * @return tasks information.
     */
    public String toString();
}
