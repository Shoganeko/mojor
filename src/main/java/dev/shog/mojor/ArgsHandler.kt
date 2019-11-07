package dev.shog.mojor

/**
 * Manages program arguments.
 */
class ArgsHandler {
    private val hooks = hashMapOf<String, () -> Unit>()

    /**
     * Initialize the args handler with [args].
     */
    fun initWith(args: Array<String>) {
        args
                .asSequence()
                .filter { hooks.containsKey(it) }
                .forEach { hooks[it]?.invoke() }
    }

    /**
     * Add a hook if [arg] is in the arguments.
     */
    fun addHook(arg: String, thr: () -> Unit) {
        hooks[arg] = thr
    }
}