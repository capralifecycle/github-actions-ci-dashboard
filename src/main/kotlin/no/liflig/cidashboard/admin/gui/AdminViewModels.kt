package no.liflig.cidashboard.admin.gui

import no.liflig.cidashboard.admin.auth.CognitoUser
import no.liflig.cidashboard.common.http4k.Webjars
import org.http4k.template.ViewModel

data class CiStatusListPage(
    val title: String,
    val user: CognitoUser,
    val activePage: String,
    val statuses: List<CiStatusRow>,
) : ViewModel {
  val htmxVersion: String = Webjars.htmxVersion

  override fun template(): String = "admin-ci-statuses"
}

data class CiStatusRow(
    val id: String,
    val repoOwner: String,
    val repoName: String,
    val branch: String,
    val status: String,
    val lastUpdated: String,
)

data class IntegrationGuidePage(
    val title: String,
    val user: CognitoUser,
    val activePage: String,
    val webhookSecret: String,
) : ViewModel {
  val htmxVersion: String = Webjars.htmxVersion

  override fun template(): String = "admin-integration"
}

data class ConfigListPage(
    val title: String,
    val user: CognitoUser,
    val activePage: String,
    val configs: List<ConfigRow>,
) : ViewModel {
  val htmxVersion: String = Webjars.htmxVersion

  override fun template(): String = "admin-configs"
}

data class ConfigRow(
    val id: String,
    val displayName: String,
    val orgMatchers: String,
)
