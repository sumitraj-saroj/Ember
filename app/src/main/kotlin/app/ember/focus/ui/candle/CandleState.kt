package app.ember.focus.ui.candle

enum class CandlePhase {
    IDLE,       // Full unlit candle
    IGNITING,   // Spark growing into full flame (~1s)
    BURNING,    // Normal active focus session
    PAUSED,     // Flame shrinks to small steady ember glow
    OVERTIME,   // Stub on wax puddle, steady flame burning
    FINISHED,   // Flame shrinks and snuffs, gentle curved smoke wisp rises and fades
    BROKEN      // Quick snuff (0.3s), thicker dark puff of smoke, cool darker wax
}

data class CandleState(
    val progress: Float = 0f, // 0.0 (fresh) to 1.0 (stub)
    val phase: CandlePhase = CandlePhase.IDLE,
    val seed: String = "ember_session",
    val phaseTimeMillis: Long = 0L // Time spent in current phase (for ignite/smoke transitions)
)

data class WaxDrip(
    val xRatio: Float,       // 0f (left) to 1f (right) of candle width
    val startProgress: Float,// burn progress when drip starts forming
    val maxLenRatio: Float,  // maximum length as fraction of candle height
    val widthDp: Float       // width of the drip
)

fun generateWaxDrips(seed: String): List<WaxDrip> {
    val hash = seed.hashCode()
    val random = java.util.Random(hash.toLong())
    val count = 4 + (random.nextInt(3)) // 4 to 6 drips
    val list = mutableListOf<WaxDrip>()
    for (i in 0 until count) {
        val xRatio = 0.12f + random.nextFloat() * 0.76f
        val startProgress = 0.10f + (i.toFloat() / count.toFloat()) * 0.55f + (random.nextFloat() * 0.1f)
        val maxLenRatio = 0.15f + random.nextFloat() * 0.35f
        val widthDp = 3f + random.nextFloat() * 4f
        list.add(WaxDrip(xRatio, startProgress.coerceIn(0.05f, 0.85f), maxLenRatio, widthDp))
    }
    return list.sortedBy { it.xRatio }
}
