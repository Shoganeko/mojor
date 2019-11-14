package dev.shog.mojor

/**
 * Manages program arguments.
 */
class ArgsHandler {
    private val hooks = hashMapOf<String, () -> Unit>()
    private val nonHooks = hashMapOf<String, () -> Unit>()

    /**
     * Initialize the args handler with [args].
     */
    fun initWith(args: Array<String>) {
        args
                .asSequence()
                .filter { hooks.containsKey(it) }
                .forEach { hooks[it]?.invoke() }

        nonHooks
                .asSequence()
                .filter { nh -> !args.contains(nh.key) && hooks.containsKey(nh.key) }
                .forEach { nh -> nonHooks[nh.key]?.invoke() }
    }

    /**
     * Add a hook that executes when [arg] is not present, but is expected in [hooks].
     */
    fun addNonHook(arg: String, thr: () -> Unit) {
        nonHooks[arg] = thr
    }

    /**
     * Add a hook if [arg] is in the arguments.
     */
    fun addHook(arg: String, thr: () -> Unit) {
        hooks[arg] = thr
    }

    /**
     * Execute [addHook] and [addNonHook].
     */
    fun addHooks(arg: String, hook: () -> Unit, nonHook: () -> Unit) {
        addHook(arg, hook)
        addNonHook(arg, nonHook)
    }
}