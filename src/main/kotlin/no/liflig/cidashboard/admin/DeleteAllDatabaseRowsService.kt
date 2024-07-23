package no.liflig.cidashboard.admin

import no.liflig.cidashboard.dashboard.UseRepo

class DeleteAllDatabaseRowsService(private val ciStatusRepo: UseRepo<Unit>) {
  fun deleteAll() {
    ciStatusRepo { repo -> repo.deleteAll() }
  }
}
