package org.objectweb.asm.util

import java.io.PrintWriter

object ASMifierUtil {
    fun main(args: Array<String>, output: PrintWriter) {
        ASMifier.main(args,   output, PrintWriter(System.err, true))
    }
}