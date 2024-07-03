# Requirements

Kopi av [Kravspesifikasjon for GitHub Actions CI Dashboard (Confluence)](https://liflig.atlassian.net/l/cp/j4AN5mnH).

> [!NOTE]
> Dette er primært for et flertall TVer i kiosk-mode på veggen til kontoret.


## Must-have

1. Nettside med byggestatus som oppdateres automatisk ved aktivitet i GitHub Actions.
1. Byggstatus må vise in-progress og sluttresultat (success/fail).
1. Kun vise actions på `master` / `main` branch. 1 status per repo.
1. Kun vise status for Workflow som brukes til CI, ikke andre ekstra workflow.
1. Tydelige farger for in-progress, success og fail. (F.eks. blå, grønn, rød).
1. Sortert på nyligste aktivitet først.
1. Trunkere byggestatuser slik at det er nok plass til å vise de siste byggene tydelig. Ikke vits å vise alle repo samtidig.
1. Viser status på alle repo i `capralifecycle` Organization.
1. Viser status på spesifikke repo utenfor `capralifecycle` Organization.
1. Medium-høy oppetid i arbeidstiden (8-16). Maks nede 2 dager i arbeidstiden per uke.
1. Fungere på Google Chrome versjon `86.0.4240.199`. (Chromebits på veggen)
1. Ingen manuell vedlikehold/drift.
1. Lav kostnad, som ikke skalerer med antall repo (under ca 2000kr/mnd).
1. Skal ikke påvirke CI (feks feile bygg hvis dashboard har nedetid).
1. Read-only mot GitHub Repos.
1. Dashboard skal sikres med auth (f.eks. token i URL) for å stoppe innsyn ved å kun kjenne domene-navn/IP-addresse. Ønsker ikke å lekke ut kunder.


### Self-Host

1. Kjent stack og infrastruktur hvis self-host
1. AWS
1. IaC
1. For en liten løsning (ikke stor Delivery Insights etc.), Open Source og Apache 2 License.

## Nice-to-have

1. Klokkeslett eller tid siden bygg startet.
1. Bruker som trigget CI.
1. Ikke trunkere feilede repoer.
1. Auto-reload ved feil (javascript-errors etc).
1. Tidsestimat på byggetid.
1. Tidsbruk etter ferdig bygg.
1. Commit hash
1. Vise andre branches enn master, men filtrert til å eksludere bots (Renovate og Snyk).
   1. Behøver nok filtering (nr 9)
1. Filtrere repoer på skjermen per team/kontekst. Whitelist.
   1. Server-side config, guid per skjerm.
   1. Config ligger i et github repo?
1. Exclude av repoer der kundene må holdes konfidensielle.
   1. Eventuelt skjule navnet på repoet så man ikke vet hvilke applikasjoner de har.
1. Ved failure, vis hvilken job/steg den feilet på.

## Will-not-have

1. Varsling til Slack.
   1. Hvorfor skal denne tjenesten varsle noe som helst? Putt varsling i hver enkelt CI Workflow.
1. CD Pipeline progress.
   1. Disse deler ikke format. CD Pipelines behøver nok et eget dashbord.
1. End-user Auth for å bli en del av Delivery Insights.
   1. End-users trenger ikke navigere til dashboardet, de skal kunne se det på en skjerm på HQ.
   1. For hjemme-kontor kan man besøke url direkte, gitt at man har en token.
1. OAuth og login for å vise skjerm.
   1. Det øker mengden manuelt arbeid, når man må logge inn på skjermene når tokens expirer osv.
1. Håndtering av manuell-approve-steg i GitHub Actions. Viser kun status på pipeline som success/fail.
