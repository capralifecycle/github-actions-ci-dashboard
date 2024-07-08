package no.liflig.cidashboard.persistence

/**
 * Marker interface to indicate that a class is persisted to the database. Changes to these classes
 * should be wary of breaking changes that need database migrations.
 */
@Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.SOURCE) annotation class Persisted
