fun main() {
    val game = Game()
    game.addPlayer(Player("Маша"))
    game.addPlayer(Player("Медведь"))
    game.addPlayer(Player("Пандочка"))
    game.addPlayer(Player("Зайка"))

    for (n in (1..<NUM_FRAMES)) game.round(false)
    game.round(true)

    game.score(
        onStrike = { nextFrame, next2Frame -> countStrike(nextFrame, next2Frame) },
        onSpare = { nextFrame -> countSpare(nextFrame) }
    )
    game.printTable()

    game.printWinner()
}

data class Frame(
    val firstThrow: Int,
    val secondThrow: Int,
    val outThrow: Int, //третий бросок в 10 фрейме
    var total: Int = 0
) {
    val isStrike: Boolean
        get() = firstThrow == 10

    val isSpare: Boolean
        get() = (firstThrow != 10) and (firstThrow + secondThrow == 10)

    val sum: Int
        get() = firstThrow + secondThrow
}

fun countSpare(nextFrame: Frame?): Int {
    return nextFrame?.firstThrow ?: 0
}

fun countStrike(nextFrame: Frame?, next2Frame: Frame?): Int {
    return if (nextFrame != null)
        if (!nextFrame.isStrike)
            nextFrame.sum
        else
            nextFrame.sum + (next2Frame?.firstThrow ?: 0)
    else 0
}

const val NUM_FRAMES = 10

data class Player(val name: String) {
    fun throwBall(final: Boolean): Frame {
        val ft = (0..10).random()//todo
        val st = if (ft == 10) 0 else (0..(10 - ft)).random()

        if (!final)
            return Frame(ft, st, 0)
        else {
            if (ft == 10) {
                val st2 = (0..10).random() //todo
                val tt = if (st2 == 10) (0..10).random() else (0..10 - st2).random()
                return Frame(ft, st2, tt)
            } else if (ft + st == 10) {
                return Frame(ft, st, (0..10).random())//todo
            } else
                return Frame(ft, st, 0)
        }
    }
}

class Game {
    private val table: MutableMap<Player, MutableList<Frame>> = mutableMapOf()

    fun addPlayer(player: Player) {
        table[player] = mutableListOf()
    }

    fun round(final: Boolean) {
        for (player in table.keys) table[player]?.add(player.throwBall(final))
    }

    fun score(onSpare: (Frame?) -> Int, onStrike: (Frame?, Frame?) -> Int) {
        for (player in table.keys) {
            val list = table[player]
            if (list != null) for (i in list.indices) {
                val frame = list[i]
                val prevFrame = if (i > 0) list[i - 1] else null
                if (i < list.lastIndex) {
                    val nextFrame = if (i < list.lastIndex) list[i + 1] else null
                    val next2Frame = if (i < list.lastIndex - 1) list[i + 2] else null

                    frame.total = (prevFrame?.total ?: 0) + frame.sum

                    if (frame.isStrike) frame.total += onStrike(nextFrame, next2Frame)

                    if (frame.isSpare) frame.total += onSpare(nextFrame)
                } else frame.total = prevFrame!!.total + frame.firstThrow + frame.secondThrow + frame.outThrow
            }
        }
    }


    fun printTable() {

        print("+==========+")
        for (n in 0..NUM_FRAMES - 2) print("+=======+")
        print("+===========+")
        println("+==========+")

        print("|          |")
        for (n in 1..<NUM_FRAMES) print("|   $n   |")
        print("|     10    |")
        println("|   Итог   |")

        print("+==========+")
        for (n in 0..NUM_FRAMES - 2) print("+=======+")
        print("+===========+")
        println("+==========|")

        for (player in table.keys) {
            print("|          |")
            for (n in 0..<NUM_FRAMES) {
                val playerList = table[player]
                if (playerList != null) {
                    val frame = playerList[n]

                    val text1 =
                        if (frame.firstThrow == 10) "X" else if (frame.firstThrow == 0) "-" else frame.firstThrow.toString()
                    val text2 =
                        if (frame.isSpare) "/" else if (frame.secondThrow == 10) "X" else if (frame.secondThrow == 0) "-" else frame.secondThrow.toString()

                    print("| $text1 | $text2 |")

                    if (n == NUM_FRAMES - 1) {
                        val text3 =
                            if (frame.outThrow == 10) "X" else if (frame.outThrow == 0) "-" else frame.outThrow.toString()
                        print(" $text3 |")
                    }
                }
            }
            println("|          |")

            val pad = player.name.padEnd(9)
            print("| $pad|")
            for (n in 1..<NUM_FRAMES) print("+-------+")
            print("+-----------+")

            val pad2 = table[player]?.last()?.total.toString().padEnd(6)
            println("|    $pad2|")


            print("|          |")
            for (n in 0..<NUM_FRAMES) {
                val playerList = table[player]
                if (playerList != null) {
                    val frame = playerList[n]
                    val pad4 =
                        if (n != NUM_FRAMES - 1) frame.total.toString().padEnd(4) else frame.total.toString().padEnd(8)
                    print("|   $pad4|")
                }
            }
            println("|          |")

            print("+==========+")
            for (n in 1..<NUM_FRAMES) print("+=======+")
            print("+===========+")
            println("|==========|")
        }

    }

    fun printWinner() {
        var winner = ""
        var max = 0
        for (player in table.keys) {
            val res = table[player]?.last()?.total
            if (res != null) {
                if (res > max) {
                    max = res
                    winner = player.name
                }
            }
        }
        println("Победитель: $winner!")
    }
}
