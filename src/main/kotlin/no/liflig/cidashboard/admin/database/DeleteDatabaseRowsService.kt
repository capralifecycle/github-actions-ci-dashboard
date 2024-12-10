package no.liflig.cidashboard.admin.database

import no.liflig.cidashboard.dashboard.UseCiStatusRepo
import no.liflig.cidashboard.persistence.CiStatusId

class DeleteDatabaseRowsService(private val ciStatusRepo: UseCiStatusRepo<Unit>) {
  fun deleteAll() {
    ciStatusRepo { repo -> repo.deleteAll() }
  }

  fun deleteById(id: CiStatusId) {
    ciStatusRepo { repo -> repo.deleteById(id) }
  }
}
