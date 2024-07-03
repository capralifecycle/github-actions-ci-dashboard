package test.util

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Applies a [Tag] with `"integration"`. This tag can be used to filter tests.
 *
 * Integration tests are slower than reqular unit tests, because they integrate multiple components
 * of the system. These tests can also use external resources, like a database.
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Tag("integration")
annotation class Integration

/**
 * Works just like [Test], but also applies a [Tag] with `"integration"`. This tag can be used to
 * filter tests.
 *
 * Integration tests are slower than reqular unit tests, because they integrate multiple components
 * of the system. These tests can also use external resources, like a database.
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Test
@Tag("integration")
annotation class IntegrationTest
