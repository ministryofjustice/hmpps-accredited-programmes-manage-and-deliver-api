package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class NonDaemonThreadLogger : AfterAllCallback {
  override fun afterAll(context: ExtensionContext) {
    val threads = Thread.getAllStackTraces().keys
      .filter { !it.isDaemon && it.isAlive } // only non-daemon alive threads
      .sortedBy { it.name }

    if (threads.isNotEmpty()) {
      println("\n=== Non-Daemon Threads Still Alive After Tests ===")
      threads.forEach { thread ->
        println("${thread.name.padEnd(40)} | alive=${thread.isAlive}")
      }
      println("=== End Non-Daemon Threads ===\n")
    } else {
      println("\n=== No Non-Daemon Threads Alive After Tests ===\n")
    }
  }
}
