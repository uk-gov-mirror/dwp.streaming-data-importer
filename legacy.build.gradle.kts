import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.spring") version "1.4.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.batch:spring-batch-test")
//    implementation("org.springframework.boot:spring-boot-starter-batch")
//    implementation(kotlin("stdlib-jdk8"))
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.9")
//    implementation("org.jetbrains.kotlin", "kotlin-reflect", "1.4.0")
//    implementation("com.beust", "klaxon", "4.0.2")
//    implementation("com.github.everit-org.json-schema", "org.everit.json.schema", "1.12.1")
//    implementation("ch.qos.logback", "logback-classic", "1.2.3")
//    implementation("org.apache.commons", "commons-text", "1.8")
//    implementation("commons-codec","commons-codec","1.14")
//    implementation("com.github.dwp:dataworks-common-logging:0.0.5")
//
//    implementation("com.amazonaws", "aws-java-sdk-secretsmanager", "1.11.819") //1.11.316
//    implementation("mysql", "mysql-connector-java", "6.0.6")
//    implementation("org.apache.hbase", "hbase-client", "1.4.9")
//    implementation("com.amazonaws:aws-java-sdk-s3:1.11.701")
//    implementation("com.amazonaws:aws-java-sdk-core:1.11.701")
//
//    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.2.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.2.0")
    testImplementation("io.kotest:kotest-property-jvm:4.2.0")
    testImplementation("com.nhaarman.mockitokotlin2", "mockito-kotlin", "2.2.0")
    testImplementation("org.springframework.batch:spring-batch-test")
}

configurations.all {
    exclude(group = "org.slf4j", module = "slf4j-log4j12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

sourceSets {
    create("integration") {
        java.srcDir(file("src/integration/groovy"))
        java.srcDir(file("src/integration/kotlin"))
        compileClasspath += sourceSets.getByName("main").output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
    create("unit") {
        java.srcDir(file("src/test/kotlin"))
        compileClasspath += sourceSets.getByName("main").output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
}




tasks.register<Test>("integration-load-test") {
    description = "Runs the integration load tests"
    group = "verification"
    testClassesDirs = sourceSets["integration"].output.classesDirs
    classpath = sourceSets["integration"].runtimeClasspath
    filter {
        includeTestsMatching("Kafka2hbIntegrationLoadSpec*")
    }

    //copy all env vars from unix/your integration container into the test
    setEnvironment(System.getenv())
    environment("K2HB_RETRY_INITIAL_BACKOFF", "1")
    environment("K2HB_RETRY_MAX_ATTEMPTS", "3")
    environment("K2HB_RETRY_BACKOFF_MULTIPLIER", "1")
    environment("K2HB_USE_AWS_SECRETS", "false")
    environment("K2HB_WRITE_TO_METADATA_STORE", "true")

    testLogging {
        outputs.upToDateWhen {false}
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.STANDARD_OUT)
    }
}

tasks.register<Test>("unit") {
    description = "Runs the unit tests"
    group = "verification"
    testClassesDirs = sourceSets["unit"].output.classesDirs
    classpath = sourceSets["unit"].runtimeClasspath

    environment("K2HB_RETRY_INITIAL_BACKOFF", "1")
    environment("K2HB_RETRY_MAX_ATTEMPTS", "3")
    environment("K2HB_RETRY_BACKOFF_MULTIPLIER", "1")
    environment("K2HB_USE_AWS_SECRETS", "false")
    environment("K2HB_WRITE_TO_METADATA_STORE", "true")

    testLogging {
        outputs.upToDateWhen {false}
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

