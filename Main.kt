package flashcards

import java.io.File
import java.io.PrintStream
import java.lang.Math.abs
import kotlin.random.Random

class CountWrongAndwers {
    companion object {
        val map: MutableMap<String?, Int?> = HashMap()
        fun increment(key: String) {
            when (val count = map[key]) {
                null -> map[key] = -1
                else -> map[key] = count - 1
            }
        }
        fun importStats(key: String, count: Int) {
            map[key] = count
        }

        fun resetStats() {
            map.clear()
            PrintAndSave.printText("Card statistics have been reset.")
        }

        fun printHardestCard() {
            val result = map.toList().sortedBy { (_, value) -> value}.toMap()
            var current = 0
            var outputList = arrayOf<String>()

            for (i in result) {
                if (current == 0) {
                    outputList += i.key.toString()
                    current = abs(i.value!!)
                } else if (abs(i.value!!) == current){
                    outputList += i.key.toString()
                } else if (abs(i.value!!) < current){
                    break
                }
            }

            if (outputList.lastIndex == 0) {
                PrintAndSave.printText("The hardest card is \"${outputList[0]}\". You have ${abs(result[outputList[0]]!!)} errors answering it.")
            } else if (outputList.lastIndex > 0){
                var output = ""
                var first = true
                for (i in outputList) {
                    if (first){
                        output += "\"" + i + "\""
                        first = false
                    } else {
                        output += ", \"" + i + "\""
                    }
                }
                PrintAndSave.printText("The hardest cards are $output. You have $current errors answering them.")

            } else {
                PrintAndSave.printText("There are no cards with errors.")
            }
        }
    }
}

class PrintAndSave {
    companion object {
        var logText = arrayOf<String>()
        fun printText(text: String) {
            println(text)
            logText += "\n" + text
        }
        fun addToLog(text: String) {

            logText += "\n" + text
        }
        fun saveLog() {
            printText("File name:")
            val filePath = readLine()!!.toString()
            addToLog(filePath)
            val newFile = File(filePath)
            var first = true
            for (i in logText) {
                if (first) {
                    newFile.writeText(i)
                    first = false
                } else {
                    newFile.appendText(i)
                }
            }
            printText("The log has been saved.")
        }
    }
}

fun ask(base: MutableMap<String, String>) {
    PrintAndSave.printText("How many times to ask?")
    val numberOfQuestions = readLine()!!.toInt()
    PrintAndSave.addToLog(numberOfQuestions.toString())
    for (z in 1..numberOfQuestions){
        val randomizer = base.keys
        val currentWord = randomizer.elementAt(Random.nextInt(0, randomizer.size))
        PrintAndSave.printText("Print the definition of \"${currentWord}\"")
        val userAnswer = readLine()!!.toString()
        PrintAndSave.addToLog(userAnswer)
        if (userAnswer == base[currentWord]) PrintAndSave.printText("Correct!") else {
            if (base.containsValue(userAnswer)) {
                for (k in base.keys) {
                    if (base[k] == userAnswer){
                        CountWrongAndwers.increment(currentWord)
                        PrintAndSave.printText("Wrong. The right answer is \"${base[currentWord]}\", but your definition is correct for \"$k\".")
                    }
                }
            } else {
                CountWrongAndwers.increment(currentWord)
                PrintAndSave.printText("Wrong. The right answer is \"${base[currentWord]}\".")
            }
        }
    }
}

fun add(base: MutableMap<String, String>): MutableMap<String, String> {
    PrintAndSave.printText("The card:")
    val input2 = readLine()!!.toString()
    PrintAndSave.addToLog(input2)
    if (base.containsKey(input2)) {
        PrintAndSave.printText("The card \"$input2\" already exists.")
        return base
    }
    PrintAndSave.printText("The definition of the card:")
    val input3 = readLine()!!.toString()
    PrintAndSave.addToLog(input3)
    if (base.containsValue(input3)) {
        PrintAndSave.printText("The definition \"$input3\" already exists.")
        return base
    }
    base[input2] = input3
    PrintAndSave.printText("The pair (\"$input2\":\"$input3\") has been added.")
    return base

}

fun remove(base: MutableMap<String, String>): MutableMap<String, String> {
    PrintAndSave.printText("Which card?")
    val input2 = readLine()!!.toString()
    PrintAndSave.addToLog(input2)
    if (base.containsKey(input2)) {
        base.remove(input2)
        PrintAndSave.printText("The card has been removed.")
        return base
    }
    PrintAndSave.printText("Can't remove \"$input2\": there is no such card.")
    return base
}

fun firstImport(base: MutableMap<String, String>, filePath: String): MutableMap<String, String> {
    if(!File(filePath).exists()) {
        PrintAndSave.printText("File not found.")
        return base
    }

    val newFile = File(filePath)
    val loadedLines = newFile.readLines()
    for (i in loadedLines){
        val key = i.substringBefore(':')
        val value = i.substringAfter(':')
        base[key] = value.substringBefore(":")
        if (!i.substringAfterLast(":").isEmpty()) {
            val wrongAnswers = i.substringAfterLast(":")
            CountWrongAndwers.importStats(key, wrongAnswers.toInt() * -1)
        }
    }
    PrintAndSave.printText("${loadedLines.lastIndex + 1} cards have been loaded.")
    return base
}

fun finalExport(base: MutableMap<String, String>, filePath: String) {
    val newFile = File(filePath)
    var first = true
    for (i in base.keys) {
        var lineInput: String
        if (CountWrongAndwers.map.containsKey(i)){
            lineInput = i + ":" + base[i] + ":" + abs(CountWrongAndwers.map[i]!!)
        } else {
            lineInput = i + ":" + base[i] + ":"
        }
        if (first) newFile.writeText(lineInput) else newFile.appendText("\n" + lineInput)
        first = false
    }
    PrintAndSave.printText("${base.keys.size} cards have been saved.")
}

fun import(base: MutableMap<String, String>): MutableMap<String, String> {
    PrintAndSave.printText("File name:")
    val filePath = readLine()!!.toString()
//    val filePath = "C:\\Kodowanie\\test1.txt"
    PrintAndSave.addToLog(filePath)
    if(!File(filePath).exists()) {
        PrintAndSave.printText("File not found.")
        return base
    }

    val newFile = File(filePath)
    val loadedLines = newFile.readLines()
    for (i in loadedLines){
        val key = i.substringBefore(':')
        val value = i.substringAfter(':')
        base[key] = value.substringBefore(":")
        if (!i.substringAfterLast(":").isEmpty()) {
            val wrongAnswers = i.substringAfterLast(":")
            CountWrongAndwers.importStats(key, wrongAnswers.toInt() * -1)
        }
    }
    PrintAndSave.printText("${loadedLines.lastIndex + 1} cards have been loaded.")
    return base
}

fun export(base: MutableMap<String, String>) {
    PrintAndSave.printText("File name:")
    val filePath = readLine()!!.toString()
//    val filePath = "C:\\Kodowanie\\test1.txt"
    PrintAndSave.addToLog(filePath)
    val newFile = File(filePath)
    var first = true
    for (i in base.keys) {
        var lineInput: String
        if (CountWrongAndwers.map.containsKey(i)){
            lineInput = i + ":" + base[i] + ":" + abs(CountWrongAndwers.map[i]!!)
        } else {
            lineInput = i + ":" + base[i] + ":"
        }
        if (first) newFile.writeText(lineInput) else newFile.appendText("\n" + lineInput)
        first = false
    }
    PrintAndSave.printText("${base.keys.size} cards have been saved.")
}

fun main(args: Array<String>) {
    val base = mutableMapOf<String, String>()
    var menuChoose = ""
    var checkingForArg = ""
    var finalExport = false
    var finalExportFile = ""
    for (i in args) {
        if (checkingForArg == "-import") firstImport(base, i)
        if (checkingForArg == "-export") {
            finalExport = true
            finalExportFile = i
        }
        checkingForArg = i
    }

    while (menuChoose != "exit") {
        PrintAndSave.printText("\nInput the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        menuChoose = readLine()!!.toString()
        PrintAndSave.addToLog(menuChoose)

        when (menuChoose) {
            "add" -> add(base)
            "remove" -> remove(base)
            "import" -> import(base)
            "export" -> export(base)
            "ask" -> ask(base)
            "log" -> PrintAndSave.saveLog()
            "hardest card" -> CountWrongAndwers.printHardestCard()
            "reset stats" -> CountWrongAndwers.resetStats()
        }
    }
    PrintAndSave.printText("Bye bye!")
    println(finalExportFile)
    if (finalExport) finalExport(base, finalExportFile)
}
