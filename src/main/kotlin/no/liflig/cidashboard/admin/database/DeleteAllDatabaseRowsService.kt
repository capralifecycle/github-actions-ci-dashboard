package no.liflig.cidashboard.admin.database

import no.liflig.cidashboard.dashboard.UseCiStatusRepo

class DeleteAllDatabaseRowsService(private val ciStatusRepo: UseCiStatusRepo<Unit>) {
  fun deleteAll() {
    ciStatusRepo { repo -> repo.deleteAll() }
  }
}
